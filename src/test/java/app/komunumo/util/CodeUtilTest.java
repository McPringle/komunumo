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

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

final class CodeUtilTest {

    @Test
    void nextCode_hasExpectedLengthAndDigitsOnly() {
        final var code = CodeUtil.nextCode();

        // Expect exactly 6 characters, digits only
        assertThat(code)
                .isNotNull()
                .hasSize(6)
                .matches("^\\d{6}$");

        // Normalized code should be identical (already digits only)
        final var normalized = CodeUtil.normalizeInput(code);
        assertThat(normalized).isEqualTo(code);
    }

    @Test
    void nextCode_producesDifferentCodes() {
        // Very low collision probability — light sanity check
        final var c1 = CodeUtil.nextCode();
        final var c2 = CodeUtil.nextCode();
        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    void normalizeInput_stripsNonDigits() {
        // Non-digits are removed; digits are kept as-is (including leading zeros)
        assertThat(CodeUtil.normalizeInput("12 34-56")).isEqualTo("123456");
        assertThat(CodeUtil.normalizeInput(" 00-1 2 3 ")).isEqualTo("00123");

        // If there are no digits, result is empty
        assertThat(CodeUtil.normalizeInput("abc")).isEmpty();

        // Idempotency: normalizing an already normalized string yields the same result
        final var once = CodeUtil.normalizeInput("1-2-3-4-5-6");
        final var twice = CodeUtil.normalizeInput(once);
        assertThat(twice).isEqualTo(once);

        // Empty input yields empty output
        assertThat(CodeUtil.normalizeInput("")).isEmpty();
    }

    @Test
    void normalizedCode_containsDigitsOnly() {
        final var normalized = CodeUtil.normalizeInput(CodeUtil.nextCode());

        // Every character must be a digit
        for (final var ch : normalized.toCharArray()) {
            assertThat(Character.isDigit(ch))
                    .as("unexpected char: '%s'", ch)
                    .isTrue();
        }
    }

    @RepeatedTest(3)
    void nextCode_multipleSamplesLookDistinct() {
        // Collect several codes and ensure all are unique (extremely unlikely to collide)
        final var unique = new HashSet<String>();
        for (var i = 0; i < 12; i++) {
            unique.add(CodeUtil.nextCode());
        }
        assertThat(unique).hasSize(12);
    }

}
