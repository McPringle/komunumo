/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.komunumo.data.generator;

import com.github.benmanes.caffeine.cache.Cache;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.komunumo.data.dto.ContentType;
import org.komunumo.data.dto.ImageDto;
import org.komunumo.data.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.komunumo.data.db.tables.Config.CONFIG;
import static org.komunumo.data.db.tables.Image.IMAGE;

@SpringBootTest
class UniqueIdGeneratorTest {

    @Autowired
    private DSLContext dsl;

    @Autowired
    private DatabaseService databaseService;

    @Test
    void returnsGeneratedId() {
        // Arrange supplier with fixed ID
        final var fixedId = UUID.randomUUID().toString();
        final UniqueIdGenerator.IdSupplier supplier = () -> fixedId;
        final UniqueIdGenerator generator = new UniqueIdGenerator(dsl, supplier);

        // Act
        final String result = generator.getUniqueID(IMAGE);

        // Assert
        assertEquals(fixedId, result);
    }

    @Test
    void skipsIdIfAlreadyExistsInDatabase() {
        // Arrange
        final var existingId = UUID.randomUUID().toString();
        final var freshId = UUID.randomUUID().toString();
        final var existingImage = new ImageDto(existingId, ContentType.IMAGE_WEBP, "test.webp");

        try {
            databaseService.storeImage(existingImage);

            // Supplier that first returns an existing ID, then a fresh ID
            final AtomicInteger callCount = new AtomicInteger(0);
            final UniqueIdGenerator.IdSupplier supplier = () -> {
                int n = callCount.getAndIncrement();
                return (n == 0) ? existingId : freshId;
            };

            final UniqueIdGenerator generator = new UniqueIdGenerator(dsl, supplier);

            // Act
            final String result = generator.getUniqueID(IMAGE);

            // Assert
            assertEquals(freshId, result);
            assertEquals(2, callCount.get(), "Supplier should have been called twice due to database hit");
        } finally {
            // Clean up: remove the test image from the database
            databaseService.deleteImage(existingImage);
        }
    }

    @Test
    void skipsIdIfInCache() throws Exception {
        // Arrange: two IDs, one manually put into the cache
        final var cachedId = UUID.randomUUID().toString();
        final var freshId = UUID.randomUUID().toString();

        // Supplier that first returns a cached ID, then a fresh ID
        final AtomicInteger callCount = new AtomicInteger(0);
        final UniqueIdGenerator.IdSupplier supplier = () -> {
            int n = callCount.getAndIncrement();
            return (n == 0) ? cachedId : freshId;
        };

        final UniqueIdGenerator generator = new UniqueIdGenerator(dsl, supplier);

        // Access the private cache field via reflection
        final Field cacheField = UniqueIdGenerator.class.getDeclaredField("idCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final var cache = (Cache<String, Boolean>) cacheField.get(generator);

        // Simulate a recently used ID
        cache.put(IMAGE.getName() + ":" + cachedId, true);

        // Act
        final String result = generator.getUniqueID(IMAGE);

        // Assert
        assertEquals(freshId, result);
        assertEquals(2, callCount.get(), "Supplier should have been called twice due to cache hit");
    }

    @Test
    void throwsExceptionIfTableHasNoIdField() {
        // Arrange: generator with default UUID supplier
        final UniqueIdGenerator generator = new UniqueIdGenerator(dsl);

        // Act + Assert
        final var expectedException = assertThrows(IllegalArgumentException.class, () -> generator.getUniqueID(CONFIG));
        assertTrue(expectedException.getMessage().contains("Table 'config' does not have a String 'id' field"));
    }

    @Test
    void generatesUniqueIdsInParallel() throws InterruptedException {
        final int threadCount = 100;
        final UniqueIdGenerator.IdSupplier supplier = () -> UUID.randomUUID().toString();
        final UniqueIdGenerator generator = new UniqueIdGenerator(dsl, supplier);

        final Set<String> ids = Collections.synchronizedSet(new HashSet<>());
        final CountDownLatch latch = new CountDownLatch(threadCount);

        try (final ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        final String id = generator.getUniqueID(IMAGE);
                        ids.add(id);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
        }

        assertEquals(threadCount, ids.size(), "All generated IDs must be unique");
    }

}
