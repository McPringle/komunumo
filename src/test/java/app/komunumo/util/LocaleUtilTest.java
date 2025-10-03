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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocaleUtilTest {

    private VaadinSession mockSession;

    @BeforeEach
    void setup() {
        mockSession = mock(VaadinSession.class);
        VaadinSession.setCurrent(mockSession);
    }

    @Test
    void returnsClientLocaleIfSet() {
        final var clientLocale = Locale.JAPANESE;
        when(mockSession.getAttribute("CLIENT_LOCALE")).thenReturn(clientLocale);
        final var result = LocaleUtil.getClientLocale();
        assertThat(result).isEqualTo(clientLocale);
    }

    @Test
    void returnsSystemDefaultLocaleIfNotSet() {
        when(mockSession.getAttribute("CLIENT_LOCALE")).thenReturn(null);
        final var result = LocaleUtil.getClientLocale();
        assertThat(result).isEqualTo(Locale.getDefault());
    }

    @ParameterizedTest
    @MethodSource("localeProvider")
    @SuppressWarnings("java:S6068") // false positive for mock verification
    void detectClientLocaleStoresInSession(final @NotNull Locale locale) {
        final var mockUI = mock(UI.class);
        when(mockUI.getLocale()).thenReturn(locale);
        LocaleUtil.detectClientLocale(mockUI);
        verify(mockSession).setAttribute(eq("CLIENT_LOCALE"), eq(locale));
    }

    static Stream<Locale> localeProvider() {
        return Stream.of(Locale.ENGLISH, Locale.GERMANY, Locale.FRENCH, Locale.ITALIAN, Locale.JAPANESE);
    }

    @Test
    void shouldReturnEmptyStringIfLocaleIsNull() {
        assertThat(LocaleUtil.getLanguageCode(null)).isEmpty();
    }

    @Test
    void shouldReturnLanguageCodeInUpperCaseForEnglish() {
        assertThat(LocaleUtil.getLanguageCode(Locale.ENGLISH)).isEqualTo("EN");
    }

    @Test
    void shouldReturnLanguageCodeInUpperCaseForGerman() {
        assertThat(LocaleUtil.getLanguageCode(Locale.GERMAN)).isEqualTo("DE");
    }

    @Test
    void shouldReturnLanguageCodeInUpperCaseForFrench() {
        assertThat(LocaleUtil.getLanguageCode(Locale.FRENCH)).isEqualTo("FR");
    }

    @Test
    void shouldNormalizeLowerCaseLanguage() {
        final var custom = Locale.of("fr");
        assertThat(LocaleUtil.getLanguageCode(custom)).isEqualTo("FR");
    }

    @Test
    void shouldIgnoreCountryAndVariant() {
        final var swissGerman = Locale.of("de", "CH");
        assertThat(LocaleUtil.getLanguageCode(swissGerman)).isEqualTo("DE");
    }

}
