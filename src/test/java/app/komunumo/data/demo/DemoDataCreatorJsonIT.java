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
package app.komunumo.data.demo;

import app.komunumo.ui.BrowserTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "komunumo.demo.json=http://localhost:8082/import/data.json")
class DemoDataCreatorJsonIT extends BrowserTest {

    @Test
    void testDemoDataImporterWithJson() {
        final var page = getPage();

        page.navigate(getInstanceUrl());
        page.waitForSelector("vaadin-card");
        captureScreenshot("home-page-with-demo-data");

        final var eventCards = page.locator("vaadin-card");
        assertThat(eventCards.count()).isEqualTo(2);

        for (var entry : Map.of(
                        0, 1,
                        1, 2) // position and event number
                .entrySet()) {
            final var position = entry.getKey();
            final var number = entry.getValue();
            final var eventCard = eventCards.nth(position);

            final var title = eventCard.locator("div[slot='title']");
            assertThat(title.textContent()).isEqualTo("Demo Event " + number);

            final var image = eventCard.locator("img[slot='media']");
            assertThat(image.count())
                    .as("image should be set")
                    .isEqualTo(1);

            if (number == 1) {
                final var imageSrc = image.getAttribute("src");
                assertThat(imageSrc)
                        .as("expected to contain an image but was: " + imageSrc)
                        .startsWith("/images/")
                        .endsWith(".png")
                        .doesNotContain("placeholder");

                final var imageAlt = image.getAttribute("alt");
                assertThat(imageAlt)
                        .as("expected to contain an alt text but was: " + imageSrc)
                        .isEqualTo("Demo Event " + number);
            } else { // demo event 2 has a placeholder image
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

}
