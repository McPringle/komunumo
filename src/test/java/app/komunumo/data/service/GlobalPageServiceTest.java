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

import app.komunumo.data.dto.GlobalPageDto;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
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

    @Test
    void getEnglishPagesOnly() {
        final var pages = globalPageService.getGlobalPages(Locale.ENGLISH).toList();
        assertThat(pages).hasSize(1);

        final var about = pages.getFirst();
        assertThat(about.slot()).isEqualTo("about");
        assertThat(about.language()).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void getGermanPagesWithEnglishFallback() {
        var imprint = new GlobalPageDto("contact", Locale.ENGLISH, null, null,
                "Contact", "## Contact"); // to test english fallback
        try {
            imprint = globalPageService.storeGlobalPage(imprint);
            final var pages = globalPageService.getGlobalPages(Locale.GERMAN)
                    .collect(toMap(GlobalPageDto::slot, Function.identity()));
            assertThat(pages).hasSize(2);
            assertThat(pages.keySet()).containsExactlyInAnyOrder("about", "contact");

            final var about = pages.get("about");
            assertThat(about.slot()).isEqualTo("about");
            assertThat(about.language()).isEqualTo(Locale.GERMAN);

            final var contact = pages.get("contact");
            assertThat(contact.slot()).isEqualTo("contact");
            assertThat(contact.language()).isEqualTo(Locale.ENGLISH);
        } finally {
            final var returnValue = globalPageService.deleteGlobalPage(imprint);
            assertThat(returnValue).isTrue();
        }
    }

    @Test
    void getSwissGermanPages() {
        final var pages = globalPageService.getGlobalPages(Locale.forLanguageTag("de-CH")).toList();
        assertThat(pages).hasSize(1);
        assertThat(pages.getFirst().slot()).isEqualTo("about");
    }

    @Test
    void getItalianPagesWithEnglishFallback() {
        final var pages = globalPageService.getGlobalPages(Locale.ITALIAN).toList();
        assertThat(pages).hasSize(1);

        final var about = pages.getFirst();
        assertThat(about.slot()).isEqualTo("about");
        assertThat(about.language()).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void storeUpdateAndDeleteGlobalPage() {
        var testee = new GlobalPageDto("test", Locale.ENGLISH, null, null,
                "Test", "## Test");
        assertThat(testee.created()).isNull();
        assertThat(testee.updated()).isNull();

        try {
            testee = globalPageService.storeGlobalPage(testee);
            assertThat(testee).isNotNull();
            assertThat(testee.slot()).isEqualTo("test");
            assertThat(testee.language()).isEqualTo(Locale.ENGLISH);
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.title()).isEqualTo("Test");
            assertThat(testee.markdown()).isEqualTo("## Test");

            testee = new GlobalPageDto(testee.slot(), testee.language(), testee.created(), testee.updated(),
                    testee.title(), "## Updated Test");
            testee = globalPageService.storeGlobalPage(testee);
            assertThat(testee).isNotNull();
            assertThat(testee.slot()).isEqualTo("test");
            assertThat(testee.language()).isEqualTo(Locale.ENGLISH);
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isAfter(testee.created());
            assertThat(testee.title()).isEqualTo("Test");
            assertThat(testee.markdown()).isEqualTo("## Updated Test");

        } finally {
            assertThat(testee).isNotNull();
            assertThat(globalPageService.deleteGlobalPage(testee)).isTrue();
            assertThat(globalPageService.deleteGlobalPage(testee)).isFalse();
        }
    }
}
