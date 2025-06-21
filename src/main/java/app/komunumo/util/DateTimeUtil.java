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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;

public final class DateTimeUtil {

    public static @NotNull ZonedDateTime getLocalizedDateTime(final @NotNull String timeZoneId,
                                                              final @NotNull ZonedDateTime dateTime) {
        final var zoneId = ZoneId.of(timeZoneId);
        return dateTime.withZoneSameInstant(zoneId);
    }

    public static @NotNull String getLocalizedDateTimeString(final @NotNull String timeZoneId,
                                                             final @NotNull Locale locale,
                                                             final @NotNull ZonedDateTime dateTime) {
        final var localizedDateTime = getLocalizedDateTime(timeZoneId, dateTime);
        final var zoneText = localizedDateTime.getZone().getDisplayName(TextStyle.SHORT, locale);

        return DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
                .withLocale(locale)
                .format(localizedDateTime) + " " + zoneText;
    }

    private DateTimeUtil() {
        throw new IllegalStateException("Utility class");
    }

}
