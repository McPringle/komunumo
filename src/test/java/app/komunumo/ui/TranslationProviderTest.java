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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationProviderTest {

    private TranslationProvider translationProvider;

    @BeforeEach
    void setUp() {
        this.translationProvider = new TranslationProvider();
    }

    @Test
    void testProviderLocales() {
        assertThat(translationProvider.getProvidedLocales())
                .containsExactlyInAnyOrder(Locale.ENGLISH, Locale.GERMAN);
    }

    @Test
    void testGetMessageInEnglish() {
        assertThat(translationProvider.getTranslation("ui.views.events.EventGridView.title", Locale.ENGLISH))
                .isEqualTo("Events");
    }

    @Test
    void testTranslationInGerman() {
        assertThat(translationProvider.getTranslation("ui.views.events.EventGridView.title", Locale.GERMAN))
                .isEqualTo("Veranstaltungen");
    }

    @Test
    void testSwissGermanFallbackToGerman() {
        assertThat(translationProvider.getTranslation("ui.views.events.EventGridView.title", Locale.forLanguageTag("de-CH")))
                .isEqualTo("Veranstaltungen");
    }

    @Test
    void testFallbackToEnglish() {
        assertThat(translationProvider.getTranslation("ui.views.events.EventGridView.title", Locale.ITALIAN))
                .isEqualTo("Events");
    }

    @Test
    void testFallbackWhenLocaleIsNull() {
        assertThat(translationProvider.getTranslation("ui.views.events.EventGridView.title", null))
                .isEqualTo("Events");
    }

    @Test
    void testWithPlaceholder() {
        assertThat(translationProvider.getTranslation("ui.views.community.CommunityDetailView.profileImage", Locale.ENGLISH, "foobar"))
                .isEqualTo("Profile picture of foobar");
    }

    @Test
    void testWithMissingPlaceholder() {
        assertThat(translationProvider.getTranslation("ui.views.community.CommunityDetailView.profileImage", Locale.ENGLISH))
                .isEqualTo("Profile picture of {0}");
    }

    @Test
    void testMissingPlaceholder() {
        assertThat(translationProvider.getTranslation("ui.views.events.EventGridView.title", Locale.ENGLISH, "foobar"))
                .isEqualTo("Events");
    }

    @Test
    void testMissingTranslation() {
        assertThat(translationProvider.getTranslation("test.missing.translation", Locale.ENGLISH))
                .isEqualTo("!en: test.missing.translation");
        assertThat(translationProvider.getTranslation("test.missing.translation", Locale.GERMAN))
                .isEqualTo("!de: test.missing.translation");
    }

}
