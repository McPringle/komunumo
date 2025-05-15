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
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class UniqueIdGenerator {

    private final DSLContext dsl;
    private final IdSupplier idSupplier;

    @Autowired
    public UniqueIdGenerator(final @NotNull  DSLContext dsl) {
        this(dsl, new RandomUUIDSupplier());
    }

    UniqueIdGenerator(final @NotNull  DSLContext dsl, final @NotNull  IdSupplier idSupplier) {
        this.dsl = dsl;
        this.idSupplier = idSupplier;
    }

    // Cache for recently generated UUIDs in the format “table:id”
    private final Cache<UUID, Boolean> idCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    // Synchronization locks per table
    private final ConcurrentHashMap<String, ReentrantLock> tableLocks = new ConcurrentHashMap<>();

    /**
     * Creates a unique UUID for the given table.
     * The UUID is checked against the database and the local cache.
     *
     * @param table the table for which to generate an ID
     * @return a Universally Unique Identifier (UUID, RFC 4122)
     */
    public UUID getUniqueID(final @NotNull  Table<?> table) {
        final var tableName = table.getName();
        final var idField = table.field("id", String.class);

        if (idField == null) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not have a String 'id' field");
        }

        // Lock per table for thread safety
        final var lock = tableLocks.computeIfAbsent(tableName, t -> new ReentrantLock(true));
        lock.lock();

        try {
            UUID uuid;

            do {
                uuid = idSupplier.getId();
            } while (idCache.asMap().containsKey(uuid) || idExistsInDatabase(table, idField, uuid));

            idCache.put(uuid, true);
            return uuid;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks whether a UUID already exists in the table.
     */
    private boolean idExistsInDatabase(final @NotNull  Table<?> table,
                                       final @NotNull  Field<String> idField,
                                       final @NotNull  UUID uuid) {
        return dsl.selectOne()
                .from(table)
                .where(idField.eq(uuid.toString()))
                .limit(1)
                .fetchOptional()
                .isPresent();
    }

    /** Interface for ID generators. */
    public interface IdSupplier {
        @NotNull UUID getId();
    }

    /** Default implementation with UUID.randomUUID(). */
    public static final class RandomUUIDSupplier implements IdSupplier {
        @Override
        public @NotNull UUID getId() {
            return UUID.randomUUID();
        }
    }

}
