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
package app.komunumo.ui.views.community;

import app.komunumo.data.dto.UserType;
import app.komunumo.ui.BrowserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommunityAddViewBT extends BrowserTest {

    private static final String CREATE_COMMUNITY_MENU_ITEM_SELECTOR =
            "vaadin-context-menu-item[role='menuitem']:has-text('Create Community')";

    @Test
    void createCommunityMenuNotVisibleForAnonymousUser() {
        final var page = getPage();

        // navigate to home page
        page.navigate(getInstanceUrl());
        page.waitForSelector(getInstanceNameSelector());

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("createCommunityMenuNotVisibleForAnonymousUser");

        // assert that "Create Community" menu item is not visible
        final var menuItem = page.locator(CREATE_COMMUNITY_MENU_ITEM_SELECTOR);
        assertThat(menuItem.isVisible()).isFalse();

        // navigate manually to community creation page should redirect to login page
        page.navigate(getInstanceUrl() + "communities/add");
        page.waitForURL("**/login");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("anonymousUserRedirected");
    }

    @Test
    void createCommunityMenuWorksWhenLoggedIn() {
        final var localUser = getTestUser(UserType.LOCAL);
        login(localUser);

        final var page = getPage();

        // navigate to home page
        page.navigate(getInstanceUrl());
        page.waitForSelector(getInstanceNameSelector());

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("createCommunityMenuIsVisibleForLocalUser");

        // assert that "Create Community" menu item is visible
        final var menuItem = page.locator(CREATE_COMMUNITY_MENU_ITEM_SELECTOR);
        assertThat(menuItem.isVisible()).isTrue();

        // click on "Create Community" menu item
        menuItem.click();
        page.waitForURL("**/communities/add");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("createCommunityMenuNavigationWorks");
    }

}
