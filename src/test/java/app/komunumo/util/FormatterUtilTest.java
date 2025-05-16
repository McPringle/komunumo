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
package app.komunumo.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormatterUtilTest {

    @Test
    void testFormatNumber() {
        assertThat(FormatterUtil.formatNumber(0L)).isEqualTo("0");
        assertThat(FormatterUtil.formatNumber(1L)).isEqualTo("1");
        assertThat(FormatterUtil.formatNumber(12L)).isEqualTo("12");
        assertThat(FormatterUtil.formatNumber(123L)).isEqualTo("123");
        assertThat(FormatterUtil.formatNumber(1234L)).isEqualTo("1'234");
        assertThat(FormatterUtil.formatNumber(12345L)).isEqualTo("12'345");
        assertThat(FormatterUtil.formatNumber(123456L)).isEqualTo("123'456");
        assertThat(FormatterUtil.formatNumber(1234567L)).isEqualTo("1'234'567");
        assertThat(FormatterUtil.formatNumber(12345678L)).isEqualTo("12'345'678");
        assertThat(FormatterUtil.formatNumber(123456789L)).isEqualTo("123'456'789");
    }

}
