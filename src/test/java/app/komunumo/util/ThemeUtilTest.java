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
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.theme.lumo.Lumo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThemeUtilTest {

    private ThemeList themeListMock;

    @BeforeEach
    void setup() {
        final var uiMock = Mockito.mock(UI.class);
        themeListMock = Mockito.mock(ThemeList.class);

        final var elementMock = Mockito.mock(Element.class);
        when(uiMock.getElement()).thenReturn(elementMock);
        when(elementMock.getThemeList()).thenReturn(themeListMock);

        UI.setCurrent(uiMock);
    }

    @Test
    void testIsDarkModeActive() {
        when(themeListMock.contains(Lumo.DARK)).thenReturn(true);
        assertThat(ThemeUtil.isDarkModeActive()).isTrue();

        when(themeListMock.contains(Lumo.DARK)).thenReturn(false);
        assertThat(ThemeUtil.isDarkModeActive()).isFalse();
    }

    @Test
    void testToggleDarkModeActivatesDarkMode() {
        try (MockedStatic<LocalStorageUtil> mockedLocalStorage = mockStatic(LocalStorageUtil.class)) {
            when(themeListMock.contains(Lumo.DARK)).thenReturn(false);

            ThemeUtil.toggleDarkMode();

            verify(themeListMock).add(Lumo.DARK);
            mockedLocalStorage.verify(() -> LocalStorageUtil.setBoolean("dark-mode", true));
        }
    }

    @Test
    void testToggleDarkModeDeactivatesDarkMode() {
        try (MockedStatic<LocalStorageUtil> mockedLocalStorage = mockStatic(LocalStorageUtil.class)) {
            when(themeListMock.contains(Lumo.DARK)).thenReturn(true);

            ThemeUtil.toggleDarkMode();

            verify(themeListMock).remove(Lumo.DARK);
            mockedLocalStorage.verify(() -> LocalStorageUtil.setBoolean("dark-mode", false));
        }
    }

    @Test
    void testInitializeDarkModeActivates() {
        try (MockedStatic<LocalStorageUtil> mockedLocalStorage = mockStatic(LocalStorageUtil.class)) {
            mockedLocalStorage.when(() -> LocalStorageUtil.getBoolean(eq("dark-mode"), any())
            ).thenAnswer(invocation -> {
                final var callback = invocation.<Consumer<Boolean>>getArgument(1);
                callback.accept(true);
                return null;
            });

            when(themeListMock.contains(Lumo.DARK)).thenReturn(false);

            ThemeUtil.initializeDarkMode();

            verify(themeListMock).add(Lumo.DARK);
        }
    }

    @Test
    void testInitializeDarkModeDoesNothingIfAlreadyActive() {
        try (MockedStatic<LocalStorageUtil> mockedLocalStorage = mockStatic(LocalStorageUtil.class)) {
            mockedLocalStorage.when(() ->
                    LocalStorageUtil.getBoolean(eq("dark-mode"), any())
            ).thenAnswer(invocation -> {
                var callback = invocation.<Consumer<Boolean>>getArgument(1);
                callback.accept(true);
                return null;
            });

            when(themeListMock.contains(Lumo.DARK)).thenReturn(true);

            ThemeUtil.initializeDarkMode();

            verify(themeListMock, never()).add(anyString());
        }
    }

}
