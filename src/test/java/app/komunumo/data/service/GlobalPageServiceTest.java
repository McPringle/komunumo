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
package app.komunumo.data.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GlobalPageServiceTest {

    @Autowired
    private @NotNull GlobalPageService globalPageService;

    @Test
    void getAboutPageInEnglishSuccess() {
        final var aboutPage = globalPageService.getGlobalPage("about", Locale.ENGLISH).orElseThrow();
        assertThat(aboutPage).isNotNull();
        assertThat(aboutPage.slot()).isEqualTo("about");
        assertThat(aboutPage.language()).isEqualTo(Locale.ENGLISH);
        assertThat(aboutPage.title()).isEqualTo("About");
        assertThat(aboutPage.markdown()).startsWith("## About *Komunumo*");
    }

    @Test
    void getAboutPageInGermanSuccess() {
        final var aboutPage = globalPageService.getGlobalPage("about", Locale.GERMAN).orElseThrow();
        assertThat(aboutPage).isNotNull();
        assertThat(aboutPage.slot()).isEqualTo("about");
        assertThat(aboutPage.language()).isEqualTo(Locale.GERMAN);
        assertThat(aboutPage.title()).isEqualTo("Über");
        assertThat(aboutPage.markdown()).startsWith("## Über *Komunumo*");
    }

    @Test
    void getAboutPageInSwissGermanFallbackToGerman() {
        final var aboutPage = globalPageService.getGlobalPage("about", Locale.forLanguageTag("de-CH")).orElseThrow();
        assertThat(aboutPage).isNotNull();
        assertThat(aboutPage.slot()).isEqualTo("about");
        assertThat(aboutPage.language()).isEqualTo(Locale.GERMAN);
        assertThat(aboutPage.title()).isEqualTo("Über");
        assertThat(aboutPage.markdown()).startsWith("## Über *Komunumo*");
    }

    @Test
    void getAboutPageInItalianFallbackToEnglish() {
        final var aboutPage = globalPageService.getGlobalPage("about", Locale.ITALIAN).orElseThrow();
        assertThat(aboutPage).isNotNull();
        assertThat(aboutPage.slot()).isEqualTo("about");
        assertThat(aboutPage.language()).isEqualTo(Locale.ENGLISH);
        assertThat(aboutPage.title()).isEqualTo("About");
        assertThat(aboutPage.markdown()).startsWith("## About *Komunumo*");
    }

    @Test
    void getNonExistingEnglishPageIsEmpty() {
        final var aboutPage = globalPageService.getGlobalPage("non-existing", Locale.ENGLISH);
        assertThat(aboutPage).isEmpty();
    }

    @Test
    void getNonExistingGermanPageIsEmpty() {
        final var aboutPage = globalPageService.getGlobalPage("non-existing", Locale.GERMAN);
        assertThat(aboutPage).isEmpty();
    }

    @Test
    void getNonExistingItalianPageIsEmpty() {
        final var aboutPage = globalPageService.getGlobalPage("non-existing", Locale.ITALIAN);
        assertThat(aboutPage).isEmpty();
    }

}
