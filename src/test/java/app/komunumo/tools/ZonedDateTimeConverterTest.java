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
package app.komunumo.tools;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ZonedDateTimeConverterTest {

    private final @NotNull ZonedDateTimeConverter converter = new ZonedDateTimeConverter();

    @Test
    void from() {
        LocalDateTime databaseValue = LocalDateTime.of(2025, 5, 10, 15, 30);
        ZonedDateTime result = converter.from(databaseValue);

        assertThat(result).isNotNull().satisfies(testee -> {
            assertThat(testee.getZone()).isEqualTo(ZoneOffset.UTC);
            assertThat(testee.getOffset()).isEqualTo(ZoneOffset.UTC);
            assertThat(testee.getYear()).isEqualTo(2025);
            assertThat(testee.getMonthValue()).isEqualTo(5);
            assertThat(testee.getDayOfMonth()).isEqualTo(10);
            assertThat(testee.getHour()).isEqualTo(15);
            assertThat(testee.getMinute()).isEqualTo(30);
            assertThat(testee.getSecond()).isZero();
            assertThat(testee.getNano()).isZero();
        });
    }

    @Test
    void to() {
        ZonedDateTime zoned = ZonedDateTime.of(2025, 5, 10, 17, 30, 0, 0, ZoneId.of("Europe/Zurich"));
        LocalDateTime dbValue = converter.to(zoned);

        // in May, Zürich is UTC+2
        assertThat(dbValue).isNotNull().satisfies(testee -> {
            assertThat(testee.getYear()).isEqualTo(2025);
            assertThat(testee.getMonthValue()).isEqualTo(5);
            assertThat(testee.getDayOfMonth()).isEqualTo(10);
            assertThat(testee.getHour()).isEqualTo(15);
            assertThat(testee.getMinute()).isEqualTo(30);
            assertThat(testee.getSecond()).isZero();
            assertThat(testee.getNano()).isZero();
        });
    }

    @Test
    void nullHandling() {
        assertThat(converter.from(null)).isNull();
        assertThat(converter.to(null)).isNull();
    }

    @Test
    void typeInfo() {
        assertThat(converter.fromType()).isEqualTo(LocalDateTime.class);
        assertThat(converter.toType()).isEqualTo(ZonedDateTime.class);
    }

}
