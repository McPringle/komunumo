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
package org.komunumo.data.service;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.komunumo.data.generator.UniqueIdGenerator;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.komunumo.data.service.getter.UniqueIdGetter;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DatabaseService implements DSLContextGetter, UniqueIdGetter, CommunityService, ImageService,
        MailTemplateService {

    private final DSLContext dsl;
    private final UniqueIdGenerator idGenerator;

    public DatabaseService(@NotNull final DSLContext dsl, @NotNull final UniqueIdGenerator idGenerator) {
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    /**
     * Get the {@link DSLContext} to access the database.
     *
     * @return the {@link DSLContext}
     */
    @Override
    public DSLContext dsl() {
        return dsl;
    }

    /**
     * Creates a unique UUID for the given table.
     * The UUID is checked against the database and the local cache.
     *
     * @param table the table for which to generate an ID
     * @return a Universally Unique Identifier (UUID, RFC 4122)
     */
    @Override
    public UUID getUniqueID(@NotNull final Table<?> table) {
        return idGenerator.getUniqueID(table);
    }

}
