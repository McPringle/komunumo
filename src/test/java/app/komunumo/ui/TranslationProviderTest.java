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
package app.komunumo.ui;

import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TranslationProviderTest.TestLocaleConfiguration.class)
class TranslationProviderTest {

    @Autowired
    private @NotNull TranslationProvider translationProvider;

    @Test
    void testProviderLocales() {
        assertThat(translationProvider.getProvidedLocales())
                .containsExactlyInAnyOrder(Locale.ENGLISH, Locale.GERMAN);
    }

    @Test
    void testGetMessageInEnglish() {
        assertThat(translationProvider.getTranslation("home.title", Locale.ENGLISH))
                .isEqualTo("Overview");
    }

    @Test
    void testTranslationInGerman() {
        assertThat(translationProvider.getTranslation("home.title", Locale.GERMAN))
                .isEqualTo("Übersicht");
    }

    @Test
    void testSwissGermanFallbackToGerman() {
        assertThat(translationProvider.getTranslation("home.title", Locale.forLanguageTag("de-CH")))
                .isEqualTo("Übersicht");
    }

    @Test
    void testFallbackToEnglish() {
        assertThat(translationProvider.getTranslation("home.title", Locale.ITALIAN))
                .isEqualTo("Overview");
    }

    // TODO testWithPlaceholder()

    @Test
    void testMissingPlaceholder() {
        assertThat(translationProvider.getTranslation("home.title", Locale.ENGLISH, "foobar"))
                .isEqualTo("Overview");
    }

    @Test
    void testMissingTranslation() {
        assertThat(translationProvider.getTranslation("test.missing.translation", Locale.ENGLISH))
                .isEqualTo("!!test.missing.translation!!");
    }

    @TestConfiguration
    public static class TestLocaleConfiguration {

        @PostConstruct
        public void setDefaultLocale() {
            Locale.setDefault(Locale.ENGLISH);
        }

    }

}
