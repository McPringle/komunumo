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
package app.komunumo.domain.core.layout.boundary;

import static org.assertj.core.api.Assertions.assertThat;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import app.komunumo.domain.event.control.EventService;
import app.komunumo.test.BrowserTest;

class HomeViewBT extends BrowserTest {

    @Autowired
    private @NotNull EventService eventService;

    @Test
    void shouldLoadHomeViewWithCorrectElements() {
        final var page = getPage();

        // Navigate to home page
        page.navigate(getInstanceUrl() + "home");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("home-view-loaded");

        // Verify page has the correct ID
        final var homeView = page.locator("#home-view");
        assertThat(homeView.count()).isEqualTo(1);
    }

    @Test
    void shouldNavigateToEventsWhenClickingMoreEventsCard() {
        final var page = getPage();

        page.navigate(getInstanceUrl() + "home");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("home-view-before-click");

        // Find and click MoreEventsCard
        final var moreEventsCard = page.locator(".more-events-card");
        assertThat(moreEventsCard.count()).isEqualTo(1);

        moreEventsCard.click();

        // Verify navigation to events page
        page.waitForURL("**/events");
        captureScreenshot("events-page-after-click");

        assertThat(page.url()).contains("/events");

        // Verify we're on the events page
        final var eventsPageTitle = page.title();
        assertThat(eventsPageTitle).contains("Events");
    }

    @Test
    void shouldNavigateToEventDetailWhenClickingEventCard() {
        final var page = getPage();

        page.navigate(getInstanceUrl() + "home");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("home-view-before-event-click");

        // Find event cards (excluding MoreEventsCard)
        final var grid = page.locator(".komunumo-grid");
        final var eventCards = grid.locator(".event-card");

        // Only proceed if there are event cards
        if (eventCards.count() > 0) {
            final var firstEventCard = eventCards.first();
            firstEventCard.click();

            // Wait for navigation to event detail page
            page.waitForURL("**/events/**");
            captureScreenshot("event-detail-after-click");

            // Verify we're on an event detail page
            assertThat(page.url()).contains("/events/");
        }
    }
}
