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

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ZonedDateTimeConverterTest {

    private final ZonedDateTimeConverter converter = new ZonedDateTimeConverter();

    @Test
    void from() {
        LocalDateTime databaseValue = LocalDateTime.of(2025, 5, 10, 15, 30);
        ZonedDateTime result = converter.from(databaseValue);

        assertNotNull(result);
        assertEquals(ZoneOffset.UTC, result.getZone());
        assertEquals(Instant.parse("2025-05-10T15:30:00Z"), result.toInstant());
    }

    @Test
    void to() {
        ZonedDateTime zoned = ZonedDateTime.of(2025, 5, 10, 17, 30, 0, 0, ZoneId.of("Europe/Zurich"));
        LocalDateTime dbValue = converter.to(zoned);

        assertNotNull(dbValue);
        assertEquals(LocalDateTime.of(2025, 5, 10, 15, 30), dbValue); // Zürich ist UTC+2 im Mai
    }

    @Test
    void nullHandling() {
        assertNull(converter.from(null));
        assertNull(converter.to(null));
    }

    @Test
    void typeInfo() {
        assertEquals(LocalDateTime.class, converter.fromType());
        assertEquals(ZonedDateTime.class, converter.toType());
    }

}
