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

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LocalTimeDurationConverterTest {

    @Test
    void from() {
        final var converter = new LocalTimeDurationConverter();
        assertNull(converter.from(null));
        assertEquals(Duration.ofMinutes(0), converter.from(LocalTime.of(0, 0)));
        assertEquals(Duration.ofMinutes(1), converter.from(LocalTime.of(0, 1)));
        assertEquals(Duration.ofMinutes(60), converter.from(LocalTime.of(1, 0)));
        assertEquals(Duration.ofMinutes(61), converter.from(LocalTime.of(1, 1)));
        assertEquals(Duration.ofMinutes(90), converter.from(LocalTime.of(1, 30)));
    }

    @Test
    void to() {
        final var converter = new LocalTimeDurationConverter();
        assertNull(converter.to(null));
        assertEquals(LocalTime.of(0, 0), converter.to(Duration.ofMinutes(0)));
        assertEquals(LocalTime.of(0, 1), converter.to(Duration.ofMinutes(1)));
        assertEquals(LocalTime.of(1, 0), converter.to(Duration.ofMinutes(60)));
        assertEquals(LocalTime.of(1, 1), converter.to(Duration.ofMinutes(61)));
        assertEquals(LocalTime.of(1, 30), converter.to(Duration.ofMinutes(90)));
    }

    @Test
    void fromType() {
        final var converter = new LocalTimeDurationConverter();
        assertEquals(LocalTime.class, converter.fromType());
    }

    @Test
    void toType() {
        final var converter = new LocalTimeDurationConverter();
        assertEquals(Duration.class, converter.toType());
    }

}
