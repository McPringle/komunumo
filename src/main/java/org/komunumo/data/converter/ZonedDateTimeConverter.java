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
package org.komunumo.data.converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Converter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class ZonedDateTimeConverter implements Converter<LocalDateTime, ZonedDateTime> {

    @Override
    public ZonedDateTime from(@Nullable final LocalDateTime databaseObject) {
        // DB time is interpreted as UTC
        return databaseObject == null ? null : databaseObject.atZone(ZoneOffset.UTC);
    }

    @Override
    public LocalDateTime to(@Nullable final ZonedDateTime userObject) {
        // UTC time is saved without zone
        return userObject == null ? null : userObject.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    @Override
    @NotNull
    public Class<LocalDateTime> fromType() {
        return LocalDateTime.class;
    }

    @Override
    @NotNull
    public Class<ZonedDateTime> toType() {
        return ZonedDateTime.class;
    }

}
