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
package app.komunumo.business.community.boundary;

import app.komunumo.ui.BrowserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityGridViewBT extends BrowserTest {

    @Test
    void showCommunities() {
        final var page = getPage();

        page.navigate(getInstanceUrl() + "communities");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("community-grid-view");
        assertThat(page.title()).isEqualTo("Communities – Komunumo Test");

        assertThat(page.locator("a[href='communities']").getAttribute("highlight")).isNotNull().isBlank();

        final var communityCards = page.locator("vaadin-card");
        assertThat(communityCards.count()).isEqualTo(6);

        for (int i = 1; i <= 6; i++) {
            final var communityCard = communityCards.nth(i - 1);

            final var title = communityCard.locator("div[slot='title']");
            assertThat(title.textContent()).isEqualTo("Demo Community " + i);

            final var subtitle = communityCard.locator("div[slot='subtitle']");
            assertThat(subtitle.textContent()).isEqualTo("@demoCommunity" + i);

            final var image = communityCard.locator("img[slot='media']");
            if (i <= 5) {
                assertThat(image.count())
                        .as("image should be set")
                        .isEqualTo(1);

                final var imageSrc = image.getAttribute("src");
                assertThat(imageSrc)
                        .as("expected to contain an image but was: " + imageSrc)
                        .startsWith("/images/")
                        .endsWith(".svg")
                        .doesNotContain("placeholder");

                final var imageAlt = image.getAttribute("alt");
                assertThat(imageAlt)
                        .as("expected to contain an alt text but was: " + imageSrc)
                        .isEqualTo("Demo Community " + i);
            } else { // demo community 6+ has a placeholder image
                final var imageSrc = image.getAttribute("src");
                assertThat(imageSrc)
                        .as("expected to contain an image but was: " + imageSrc)
                        .startsWith("/images/placeholder")
                        .endsWith(".svg");

                final var imageAlt = image.getAttribute("alt");
                assertThat(imageAlt)
                        .as("expected to contain an alt text but was: " + imageSrc)
                        .isEqualTo("Placeholder Image");
            }
        }
    }

    @Test
    void clickOnCommunityCard() {
        final var page = getPage();

        page.navigate(getInstanceUrl() + "communities");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("community-grid-view");

        final var communityCards = page.locator("vaadin-card");
        assertThat(communityCards.count()).isNotZero();

        final var communityCard = communityCards.first();
        final var subtitle = communityCard.locator("div[slot='subtitle']");
        assertThat(subtitle.textContent()).isEqualTo("@demoCommunity1");

        communityCard.click();
        page.waitForURL("**/communities/@demoCommunity1");
        captureScreenshot("community-detail-view");

        assertThat(page.url()).contains("/communities/@demoCommunity1");
        page.waitForSelector("h2:has-text('Demo Community 1')");
    }

}
