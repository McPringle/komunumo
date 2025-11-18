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
package app.komunumo.data.generator;

import app.komunumo.data.dto.ContentType;
import app.komunumo.business.core.image.entity.ImageDto;
import app.komunumo.business.core.image.control.ImageService;
import app.komunumo.ui.KaribuTest;
import com.github.benmanes.caffeine.cache.Cache;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static app.komunumo.data.db.tables.Config.CONFIG;
import static app.komunumo.data.db.tables.Image.IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UniqueIdGeneratorKT extends KaribuTest {

    @Autowired
    private @NotNull DSLContext dsl;

    @Autowired
    private @NotNull ImageService imageService;

    @Test
    void returnsGeneratedId() {
        // Arrange supplier with fixed ID
        final UUID fixedId = UUID.randomUUID();
        final UniqueIdGenerator.IdSupplier supplier = () -> fixedId;
        final UniqueIdGenerator generator = new UniqueIdGenerator(dsl, supplier);

        // Act
        final UUID result = generator.getUniqueID(IMAGE);

        // Assert
        assertThat(result).isEqualTo(fixedId);
    }

    @Test
    void skipsIdIfAlreadyExistsInDatabase() {
        // Arrange
        final UUID existingId = UUID.randomUUID();
        final UUID freshId = UUID.randomUUID();
        final ImageDto existingImage = new ImageDto(existingId, ContentType.IMAGE_WEBP);

        try {
            imageService.storeImage(existingImage);

            // Supplier that first returns an existing ID, then a fresh ID
            final AtomicInteger callCount = new AtomicInteger(0);
            final UniqueIdGenerator.IdSupplier supplier = () -> {
                int n = callCount.getAndIncrement();
                return (n == 0) ? existingId : freshId;
            };

            final UniqueIdGenerator generator = new UniqueIdGenerator(dsl, supplier);

            // Act
            final UUID result = generator.getUniqueID(IMAGE);

            // Assert
            assertThat(result).isEqualTo(freshId);
            assertThat(callCount.get())
                    .as("Supplier should have been called twice due to database hit")
                    .isEqualTo(2);
        } finally {
            // Clean up: remove the test image from the database
            imageService.deleteImage(existingImage);
        }
    }

    @Test
    void skipsIdIfInCache() throws Exception {
        // Arrange: two IDs, one manually put into the cache
        final UUID cachedId = UUID.randomUUID();
        final UUID freshId = UUID.randomUUID();

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
        final var cache = (Cache<@NotNull UUID, @NotNull Boolean>) cacheField.get(generator);

        // Simulate a recently used ID
        cache.put(cachedId, true);

        // Act
        final UUID result = generator.getUniqueID(IMAGE);

        // Assert
        assertThat(result).isEqualTo(freshId);
        assertThat(callCount.get())
                .as("Supplier should have been called twice due to cache hit")
                .isEqualTo(2);
    }

    @Test
    void throwsExceptionIfTableHasNoIdField() {
        // Arrange: generator with default UUID supplier
        final UniqueIdGenerator generator = new UniqueIdGenerator(dsl);

        // Act + Assert
        assertThatThrownBy(() -> generator.getUniqueID(CONFIG))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Table 'config' does not have a String 'id' field");
    }

    @Test
    void generatesUniqueIdsInParallel() throws InterruptedException {
        final int threadCount = 100;
        final UniqueIdGenerator generator = new UniqueIdGenerator(dsl);

        final Set<UUID> ids = Collections.synchronizedSet(new HashSet<>());
        final CountDownLatch latch = new CountDownLatch(threadCount);

        try (final ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        final UUID id = generator.getUniqueID(IMAGE);
                        ids.add(id);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
        }

        assertThat(ids)
                .as("All generated IDs must be unique")
                .hasSize(threadCount);
    }

}
