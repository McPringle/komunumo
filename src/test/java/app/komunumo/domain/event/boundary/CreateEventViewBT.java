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
package app.komunumo.domain.event.boundary;

import app.komunumo.domain.user.entity.UserType;
import app.komunumo.test.BrowserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateEventViewBT extends BrowserTest {

    private static final String CREATE_EVENT_MENU_ITEM_SELECTOR =
            "vaadin-context-menu-item[role='menuitem']:has-text('Create Event')";

    @Test
    void createEventMenuNotVisibleForAnonymousUser() {
        final var page = getPage();

        // navigate to home page
        page.navigate(getInstanceUrl());
        page.waitForSelector(getInstanceNameSelector());

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("createEventMenuNotVisibleForAnonymousUser");

        // assert that "Create Event" menu item is not visible
        final var menuItem = page.locator(CREATE_EVENT_MENU_ITEM_SELECTOR);
        assertThat(menuItem.isVisible()).isFalse();

        // navigate manually to event creation page should redirect to login page
        page.navigate(getInstanceUrl() + "events/new");
        page.waitForURL("**/login");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("anonymousUserRedirected");
    }

    @Test
    void createEventMenuWorksWhenLoggedIn() {
        final var localUser = getTestUser(UserType.LOCAL);
        login(localUser);

        final var page = getPage();

        // navigate to home page
        page.navigate(getInstanceUrl());
        page.waitForSelector(getInstanceNameSelector());

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("createEventMenuIsVisibleForLocalUser");

        // assert that "Create Event" menu item is visible
        final var menuItem = page.locator(CREATE_EVENT_MENU_ITEM_SELECTOR);
        assertThat(menuItem.isVisible()).isTrue();

        // click on "Create Event" menu item
        menuItem.click();
        page.waitForURL("**/events/new");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("createEventMenuNavigationWorks");
    }

}
