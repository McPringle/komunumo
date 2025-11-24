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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumByNameConverterTest {

    @Test
    void from() {
        final var converter = new TestEnumConverter();
        assertThat(converter.from(null)).isNull();
        assertThat(converter.from("")).isNull();
        assertThat(converter.from("FOOBAR")).isEqualTo(TestEnum.FOOBAR);
        assertThat(converter.from("foobar")).isEqualTo(TestEnum.FOOBAR);
        assertThat(converter.from("fOoBaR")).isEqualTo(TestEnum.FOOBAR);
        assertThat(converter.from("   fOObAr   ")).isEqualTo(TestEnum.FOOBAR);

        assertThatThrownBy(() -> converter.from("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No enum constant app.komunumo.data.converter.EnumByNameConverterTest.TestEnum.UNKNOWN");
    }

    @Test
    @SuppressWarnings("ConstantValue")
    void to() {
        final var converter = new TestEnumConverter();
        assertThat(converter.to(null)).isNull();
        assertThat(converter.to(TestEnum.FOOBAR)).isEqualTo("FOOBAR");
    }

    @Test
    void fromType() {
        final var converter = new TestEnumConverter();
        assertThat(converter.fromType()).isEqualTo(String.class);
    }

    @Test
    void toType() {
        final var converter = new TestEnumConverter();
        assertThat(converter.toType()).isEqualTo(TestEnum.class);
    }

    enum TestEnum { FOOBAR }

    static class TestEnumConverter extends EnumByNameConverter<TestEnum> {
        public TestEnumConverter() {
            super(TestEnum.class);
        }
    }

}
