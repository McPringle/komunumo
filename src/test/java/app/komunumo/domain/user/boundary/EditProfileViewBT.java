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
package app.komunumo.domain.user.boundary;

import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.test.BrowserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EditProfileViewBT extends BrowserTest {

    private static final String MY_PROFILE_SELECTOR =
            "h2:has-text('My Profile')";
    private static final String MY_PROFILE_MENU_ITEM_SELECTOR =
            "vaadin-context-menu-item[role='menuitem']:has-text('My Profile')";

    @Test
    void noSettingsForAnonymousVisitors() {
        final var page = getPage();

        // navigate to events page
        page.navigate(getInstanceUrl() + "events");
        page.waitForURL("**/events");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("noSettingsForAnonymousVisitors_eventPageAfterLoad");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("noSettingsForAnonymousVisitors_avatarMenuOpened");

        // check that there is no settings editor menu item
        final var adminItem = page.locator(SETTINGS_MENU_ITEM_SELECTOR);
        assertThat(adminItem.isVisible()).isFalse();

        // try to navigate directly to profile page will start the authentication process
        page.navigate(getInstanceUrl() + "settings/profile");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("noSettingsForAnonymousVisitors_afterManualNavigation");
        assertThat(page.locator("h2:visible").allInnerTexts()).contains("Confirm your email address");
    }

    @Test
    void myProfileForLocalUsers() {
        login(getTestUser(UserRole.USER));
        final var page = getPage();

        // navigate to events page
        page.navigate(getInstanceUrl() + "events");
        page.waitForURL("**/events");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("myProfileForLocalUsers_eventPageAfterLoad");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("myProfileForLocalUsers_avatarMenuOpened");

        // open settings menu
        final var settingsItem = page.locator(SETTINGS_MENU_ITEM_SELECTOR);
        settingsItem.click();
        captureScreenshot("myProfileForLocalUsers_settingsMenuOpened");

        // check that there is a profile menu item
        final var myProfileItem = page.locator(MY_PROFILE_MENU_ITEM_SELECTOR);
        assertThat(myProfileItem.isVisible()).isTrue();

        // click on profile menu item
        myProfileItem.click();
        page.waitForURL("**/settings/profile");
        page.waitForSelector(MY_PROFILE_SELECTOR);
        captureScreenshot("myProfileForLocalUsers_myProfileLoaded");
    }

}
