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
package org.komunumo.data.service.getter;

import org.jetbrains.annotations.NotNull;
import org.jooq.Table;

public interface UniqueIdGetter {

    /**
     * Creates a unique UUID for the given table.
     * The UUID is checked against the database and the local cache.
     *
     * @param table the table for which to generate an ID
     * @return a unique ID in UUID format (RFC 4122)
     */
    String getUniqueID(@NotNull Table<?> table);

}
