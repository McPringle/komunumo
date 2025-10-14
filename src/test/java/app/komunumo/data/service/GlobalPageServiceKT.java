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
import app.komunumo.data.dto.UserRole;
import app.komunumo.ui.KaribuTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.Locale;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalPageServiceKT extends KaribuTest {

    @Autowired
    private @NotNull GlobalPageService globalPageService;

    @Test
    void getImprintPageInEnglishSuccess() {
        final var imprintPage = globalPageService.getGlobalPage("imprint", Locale.ENGLISH).orElseThrow();
        assertThat(imprintPage).isNotNull();
        assertThat(imprintPage.slot()).isEqualTo("imprint");
        assertThat(imprintPage.language()).isEqualTo(Locale.ENGLISH);
        assertThat(imprintPage.title()).isEqualTo("Legal Notice");
        assertThat(imprintPage.markdown()).startsWith("## Legal Notice");
    }

    @Test
    void getImprintPageInGermanSuccess() {
        final var imprintPage = globalPageService.getGlobalPage("imprint", Locale.GERMAN).orElseThrow();
        assertThat(imprintPage).isNotNull();
        assertThat(imprintPage.slot()).isEqualTo("imprint");
        assertThat(imprintPage.language()).isEqualTo(Locale.GERMAN);
        assertThat(imprintPage.title()).isEqualTo("Impressum");
        assertThat(imprintPage.markdown()).startsWith("## Impressum");
    }

    @Test
    void getImprintPageInSwissGermanFallbackToGerman() {
        final var imprintPage = globalPageService.getGlobalPage("imprint", Locale.forLanguageTag("de-CH")).orElseThrow();
        assertThat(imprintPage).isNotNull();
        assertThat(imprintPage.slot()).isEqualTo("imprint");
        assertThat(imprintPage.language()).isEqualTo(Locale.GERMAN);
        assertThat(imprintPage.title()).isEqualTo("Impressum");
        assertThat(imprintPage.markdown()).startsWith("## Impressum");
    }

    @Test
    void getImprintPageInItalianFallbackToEnglish() {
        final var imprintPage = globalPageService.getGlobalPage("imprint", Locale.ITALIAN).orElseThrow();
        assertThat(imprintPage).isNotNull();
        assertThat(imprintPage.slot()).isEqualTo("imprint");
        assertThat(imprintPage.language()).isEqualTo(Locale.ENGLISH);
        assertThat(imprintPage.title()).isEqualTo("Legal Notice");
        assertThat(imprintPage.markdown()).startsWith("## Legal Notice");
    }

    @Test
    void getNonExistingEnglishPageIsEmpty() {
        final var nonExistingPage = globalPageService.getGlobalPage("non-existing", Locale.ENGLISH);
        assertThat(nonExistingPage).isEmpty();
    }

    @Test
    void getNonExistingGermanPageIsEmpty() {
        final var nonExistingPage = globalPageService.getGlobalPage("non-existing", Locale.GERMAN);
        assertThat(nonExistingPage).isEmpty();
    }

    @Test
    void getNonExistingItalianPageIsEmpty() {
        final var nonExistingPage = globalPageService.getGlobalPage("non-existing", Locale.ITALIAN);
        assertThat(nonExistingPage).isEmpty();
    }

    @Test
    void getEnglishPagesOnly() {
        final var pages = globalPageService.getGlobalPages(Locale.ENGLISH);
        assertThat(pages).hasSize(1);

        final var imprintPage = pages.getFirst();
        assertThat(imprintPage.slot()).isEqualTo("imprint");
        assertThat(imprintPage.language()).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void getGermanPagesWithEnglishFallback() {
        var contactPage = new GlobalPageDto("contact", Locale.ENGLISH, null, null,
                "Contact", "## Contact"); // to test english fallback
        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            contactPage = globalPageService.storeGlobalPage(contactPage);
            final var pages = globalPageService.getGlobalPages(Locale.GERMAN)
                    .stream()
                    .collect(toMap(GlobalPageDto::slot, Function.identity()));
            assertThat(pages).hasSize(2);
            assertThat(pages.keySet()).containsExactlyInAnyOrder("imprint", "contact");

            final var imprintPage = pages.get("imprint");
            assertThat(imprintPage.slot()).isEqualTo("imprint");
            assertThat(imprintPage.language()).isEqualTo(Locale.GERMAN);

            contactPage = pages.get("contact");
            assertThat(contactPage.slot()).isEqualTo("contact");
            assertThat(contactPage.language()).isEqualTo(Locale.ENGLISH);
        } finally {
            final var returnValue = globalPageService.deleteGlobalPage(contactPage);
            assertThat(returnValue).isTrue();
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getSwissGermanPages() {
        final var pages = globalPageService.getGlobalPages(Locale.forLanguageTag("de-CH"));
        assertThat(pages).hasSize(1);
        assertThat(pages.getFirst().slot()).isEqualTo("imprint");
    }

    @Test
    void getItalianPagesWithEnglishFallback() {
        final var pages = globalPageService.getGlobalPages(Locale.ITALIAN);
        assertThat(pages).hasSize(1);

        final var imprintPage = pages.getFirst();
        assertThat(imprintPage.slot()).isEqualTo("imprint");
        assertThat(imprintPage.language()).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void storeReplaceAndDeleteGlobalPage() throws InterruptedException {
        var testee = new GlobalPageDto("test", Locale.ENGLISH, null, null,
                "Test", "## Test");
        assertThat(testee.created()).isNull();
        assertThat(testee.updated()).isNull();

        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            testee = globalPageService.storeGlobalPage(testee);
            assertThat(testee).isNotNull();
            assertThat(testee.slot()).isEqualTo("test");
            assertThat(testee.language()).isEqualTo(Locale.ENGLISH);
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.title()).isEqualTo("Test");
            assertThat(testee.markdown()).isEqualTo("## Test");

            Thread.sleep(Duration.ofSeconds(1)); // ensure updated timestamp is different

            testee = new GlobalPageDto(testee.slot(), testee.language(), testee.created(), testee.updated(),
                    testee.title(), "## Replaced Test");
            testee = globalPageService.storeGlobalPage(testee);
            assertThat(testee).isNotNull();
            assertThat(testee.slot()).isEqualTo("test");
            assertThat(testee.language()).isEqualTo(Locale.ENGLISH);
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isAfter(testee.created());
            assertThat(testee.title()).isEqualTo("Test");
            assertThat(testee.markdown()).isEqualTo("## Replaced Test");

        } finally {
            assertThat(testee).isNotNull();
            assertThat(globalPageService.deleteGlobalPage(testee)).isTrue();
            assertThat(globalPageService.deleteGlobalPage(testee)).isFalse();
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void updateGlobalPageSuccess() throws InterruptedException {
        final var slot = "test";
        final var locale = Locale.ENGLISH;
        final var title = "Test Title";
        final var titleUpdated = "Test Title Updated";
        final var markdown = "## Test Page Content";
        final var markdownUpdated = "## Test Page Content Updated";

        final var testPage = new GlobalPageDto(slot, locale, null, null, title, markdown);
        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            final var originalTestPage = globalPageService.storeGlobalPage(testPage);
            assertThat(originalTestPage).isNotNull();

            final var testPageBeforeUpdate = globalPageService.getGlobalPage(slot, locale).orElseThrow();
            assertThat(testPageBeforeUpdate).isNotNull();
            assertThat(testPageBeforeUpdate.slot()).isEqualTo(slot);
            assertThat(testPageBeforeUpdate.language()).isEqualTo(locale);
            assertThat(testPageBeforeUpdate.created()).isNotNull();
            assertThat(testPageBeforeUpdate.updated()).isNotNull();
            assertThat(testPageBeforeUpdate.updated()).isEqualTo(testPageBeforeUpdate.created());
            assertThat(testPageBeforeUpdate.title()).isEqualTo(title);
            assertThat(testPageBeforeUpdate.markdown()).isEqualTo(markdown);

            Thread.sleep(Duration.ofSeconds(1)); // ensure updated timestamp is different

            final var updateResult = globalPageService.updateGlobalPage(testPageBeforeUpdate, titleUpdated, markdownUpdated);
            assertThat(updateResult).isTrue();

            final var testPageAfterUpdate = globalPageService.getGlobalPage(slot, locale).orElseThrow();
            assertThat(testPageAfterUpdate).isNotNull();
            assertThat(testPageAfterUpdate.slot()).isEqualTo(slot);
            assertThat(testPageAfterUpdate.language()).isEqualTo(locale);
            assertThat(testPageAfterUpdate.created()).isNotNull();
            assertThat(testPageAfterUpdate.updated()).isNotNull();
            assertThat(testPageAfterUpdate.updated()).isAfter(testPageAfterUpdate.created());
            assertThat(testPageAfterUpdate.title()).isEqualTo(titleUpdated);
            assertThat(testPageAfterUpdate.markdown()).isEqualTo(markdownUpdated);
        } finally {
            globalPageService.deleteGlobalPage(testPage);
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void updateGlobalPageError() {
        final var slot = "test";
        final var locale = Locale.ENGLISH;
        final var title = "Test Title";
        final var titleUpdated = "Test Title Updated";
        final var markdown = "## Test Page Content";
        final var markdownUpdated = "## Test Page Content Updated";

        final var testPage = new GlobalPageDto(slot, locale, null, null, title, markdown);

        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            final var updateResult = globalPageService.updateGlobalPage(testPage, titleUpdated, markdownUpdated);
            assertThat(updateResult).isFalse();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
