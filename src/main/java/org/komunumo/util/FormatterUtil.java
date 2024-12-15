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
package org.komunumo.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class FormatterUtil {

    private static final DecimalFormat LARGE_NUMBERS = createLargeNumberFormatter();

    private static DecimalFormat createLargeNumberFormatter() {
        final var formatter = (DecimalFormat) NumberFormat.getInstance();
        final var symbols = formatter.getDecimalFormatSymbols();

        symbols.setGroupingSeparator('\'');
        formatter.setDecimalFormatSymbols(symbols);

        return formatter;
    }

    public static String formatNumber(final long number) {
        return LARGE_NUMBERS.format(number);
    }

    private FormatterUtil() {
        throw new IllegalStateException("Utility class");
    }

}
