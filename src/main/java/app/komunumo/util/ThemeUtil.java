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
import com.vaadin.flow.theme.lumo.Lumo;

public final class ThemeUtil {

    private static final String DARK_MODE = "dark-mode";

    @SuppressWarnings("java:S5411") // Boolean value is never null
    public static void initializeDarkMode() {
        LocalStorageUtil.getBoolean(DARK_MODE, isDarkModeEnabled -> {
            if (isDarkModeEnabled && !isDarkModeActive()) {
                toggleDarkMode();
            }
        });
    }

    public static boolean isDarkModeActive() {
        return UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK);
    }

    public static void toggleDarkMode() {
        final var themeList = UI.getCurrent().getElement().getThemeList();
        if (isDarkModeActive()) {
            themeList.remove(Lumo.DARK);
            LocalStorageUtil.setBoolean(DARK_MODE, false);
        } else {
            themeList.add(Lumo.DARK);
            LocalStorageUtil.setBoolean(DARK_MODE, true);
        }
    }


    /**
     * <p>Private constructor to prevent instantiation of this utility class.</p>
     */
    private ThemeUtil() {
        throw new IllegalStateException("Utility class");
    }

}
