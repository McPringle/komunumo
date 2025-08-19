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

    // Human-friendly alphabet from CodeUtil (no 0/1/i/l/o)
    private static final String SAFE_ALPHABET = "23456789abcdefghjkmnpqrstuvwxyz";

    // Regex char class reflecting the same alphabet
    private static final String SAFE_CHARCLASS = "[2-9a-hjkmnpqrstuvwxyz]";

    @Test
    @SuppressWarnings("java:S5853") // better readability
    void nextCode_hasExpectedFormatAndAlphabet() {
        final var code = CodeUtil.nextCode();

        // Expect grouped format: 5 chars, '-', 5 chars (total length 11)
        assertThat(code).hasSize(11);
        assertThat(code.charAt(5)).isEqualTo('-');
        assertThat(code).matches("^" + SAFE_CHARCLASS + "{5}-" + SAFE_CHARCLASS + "{5}$");

        // Normalized code must be exactly 10 chars from the safe alphabet
        final var normalized = CodeUtil.normalizeInput(code);
        assertThat(normalized).hasSize(10);
        assertThat(normalized).matches("^" + SAFE_CHARCLASS + "{10}$");

        // No confusing characters should appear (in either formatted or normalized forms)
        assertThat(code).doesNotContain("0", "1", "i", "l", "o");
        assertThat(normalized).doesNotContain("0", "1", "i", "l", "o");

        // All characters should be lowercase
        assertThat(normalized).isEqualTo(normalized.toLowerCase());
    }

    @Test
    void nextCode_producesDifferentCodes() {
        // Very low collision probability — light sanity check
        final var c1 = CodeUtil.nextCode();
        final var c2 = CodeUtil.nextCode();
        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    void normalizeInput_stripsNonAlphanumericAndLowercases() {
        // Non-alphanumerics are removed; letters are lowercased
        assertThat(CodeUtil.normalizeInput("AB12- cd34 _!?"))
                .isEqualTo("ab12cd34");

        assertThat(CodeUtil.normalizeInput("QwE-9Z  8X*Y_7"))
                .isEqualTo("qwe9z8xy7");

        // Non-ASCII letters are stripped (only [a-z0-9] remains)
        assertThat(CodeUtil.normalizeInput("ÄÖÜ-ß! Foo-123"))
                .isEqualTo("foo123");

        // Idempotency: normalizing an already normalized string yields the same result
        final var once = CodeUtil.normalizeInput("A- B- C");
        final var twice = CodeUtil.normalizeInput(once);
        assertThat(twice).isEqualTo(once);

        // Empty input yields empty output
        assertThat(CodeUtil.normalizeInput("")).isEmpty();
    }

    @Test
    void normalizedCodeContainsOnlySafeAlphabet() {
        final var normalized = CodeUtil.normalizeInput(CodeUtil.nextCode());

        // Every character must come from the safe alphabet
        for (final var ch : normalized.toCharArray()) {
            assertThat(SAFE_ALPHABET.indexOf(ch))
                    .as("unexpected char: '%s'", ch)
                    .isGreaterThanOrEqualTo(0);
        }
    }

    @RepeatedTest(3)
    void nextCode_multipleSamplesLookDistinct() {
        // Collect several codes and ensure more than one unique value is produced
        final var unique = new HashSet<String>();
        for (var i = 0; i < 12; i++) {
            unique.add(CodeUtil.nextCode());
        }
        assertThat(unique).hasSize(12);
    }
}
