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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;

public final class TimeZoneUtil {

    public static final String SESSION_TIME_ZONE_KEY = "CLIENT_TIMEZONE_ID";

    /**
     * <p>Detects the client's time zone and stores it in the current Vaadin session.</p>
     *
     * <p>This method retrieves the client's time zone ID from the UI's extended client details
     * and saves it in the Vaadin session under the key {@code CLIENT_TIMEZONE_ID}.</p>
     *
     * @param ui the UI instance from which to retrieve the client details.
     */
    public static void detectClientTimeZone(final @NotNull UI ui) {
        final var timeZoneId = ui.getPage().getExtendedClientDetails().getTimeZoneId();
        final var zoneId = timeZoneId != null ? ZoneId.of(timeZoneId) : ZoneId.systemDefault();
        VaadinSession.getCurrent().setAttribute(SESSION_TIME_ZONE_KEY, zoneId);
    }

    /**
     * <p>Returns the client time zone if available, otherwise returns the server's default time zone.</p>
     *
     * @return a {@link ZoneId} representing the client's time zone, or the server's default time zone if not set.
     */
    public static @NotNull ZoneId getClientTimeZone() {
        final var value = VaadinSession.getCurrent().getAttribute(SESSION_TIME_ZONE_KEY);
        if (value instanceof ZoneId zoneId) {
            return zoneId;
        }
        return ZoneId.systemDefault();
    }

    /**
     * <p>Private constructor to prevent instantiation of this utility class.</p>
     */
    private TimeZoneUtil() {
        throw new IllegalStateException("Utility class");
    }

}

