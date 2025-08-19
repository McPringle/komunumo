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
import java.util.Locale;

public final class CodeUtil {

    private static final String ALPHABET = "23456789abcdefghjkmnpqrstuvwxyz";
    private static final SecureRandom RNG = new SecureRandom();

    private static final int LENGTH = 10;
    private static final int GROUP_SIZE = 5;
    private static final char SEPARATOR = '-';

    public static String nextCode() {
        final var code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(RNG.nextInt(ALPHABET.length())));
        }
        return formatGrouped(code.toString());
    }

    private static String formatGrouped(final @NotNull String code) {
        final var formatted = new StringBuilder(code.length() + code.length() / GROUP_SIZE);
        for (int i = 0; i < code.length(); i++) {
            if (i > 0 && i % GROUP_SIZE == 0) {
                formatted.append(SEPARATOR);
            }
            formatted.append(code.charAt(i));
        }
        return formatted.toString();
    }

    public static String normalizeInput(final @NotNull String code) {
        return code.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private CodeUtil() {
        throw new IllegalStateException("Utility class");
    }

}
