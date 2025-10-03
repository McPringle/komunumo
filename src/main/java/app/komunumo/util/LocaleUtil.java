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
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class LocaleUtil {

    private static final String SESSION_LOCALE_KEY = "CLIENT_LOCALE";

    public static void detectClientLocale(final @NotNull UI ui) {
        VaadinSession.getCurrent().setAttribute(SESSION_LOCALE_KEY, ui.getLocale());
    }

    public static @NotNull Locale getClientLocale() {
        final var value = VaadinSession.getCurrent().getAttribute(SESSION_LOCALE_KEY);
        if (value instanceof Locale locale) {
            return locale;
        }
        return Locale.getDefault();
    }

    public static @NotNull String getLanguageCode(final @Nullable Locale locale) {
        return locale == null ? "" : locale.getLanguage().toUpperCase(Locale.ROOT);
    }

    private LocaleUtil() {
        throw new IllegalStateException("Utility class");
    }

}
