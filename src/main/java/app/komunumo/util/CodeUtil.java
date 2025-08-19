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

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

public final class CodeUtil {

    // Digits only, allows leading zeros.
    private static final String DIGITS = "0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    // 6-digit verification code
    private static final int LENGTH = 6;

    /**
     * <p>Generates a six-digit numeric verification code as a {@link String}.</p>
     *
     * <p>The code consists exclusively of ASCII digits {@code '0'..'9'} and may contain
     * leading zeros (e.g. {@code "003917"}). A cryptographically strong
     * {@link SecureRandom} is used as the source of randomness.</p>
     *
     * @return a six-character string containing only digits {@code 0-9}
     * @see #normalizeInput(String)
     */
    public static @NotNull String nextCode() {
        final var code = new StringBuilder(LENGTH);
        for (var i = 0; i < LENGTH; i++) {
            code.append(DIGITS.charAt(RNG.nextInt(DIGITS.length())));
        }
        return code.toString();
    }

    /**
     * <p>Normalizes user input of a verification code by removing every non-digit character.</p>
     *
     * <p>This is intended to make user entry tolerant to formatting such as spaces or
     * hyphens (e.g. from copying/pasting or visually grouped codes). The method does
     * <strong>not</strong> perform any length checks or padding; it simply strips all
     * characters except {@code 0-9}.</p>
     *
     * @param code non-null raw user input (may contain spaces, hyphens, or other characters)
     * @return the input with all non-digits removed; returns an empty string if no digits are present
     * @see #nextCode()
     */
    public static @NotNull String normalizeInput(final @NotNull String code) {
        return code.replaceAll("\\D", "");
    }

    private CodeUtil() {
        throw new IllegalStateException("Utility class");
    }

}
