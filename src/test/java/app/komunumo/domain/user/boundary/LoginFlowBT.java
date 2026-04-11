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

class LoginFlowBT extends BrowserTest {

    @Test
    void clickLinkInAvatarMenu() {
        final var page = getPage();
        page.navigate(getInstanceUrl() + "events");
        page.waitForURL("**/events");
        page.waitForSelector(getInstanceNameSelector());

        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        page.click(LOGIN_MENU_ITEM_SELECTOR);
        page.waitForURL("**/login");

        assertThat(page.url()).contains("/login");
    }

    @Test
    @SuppressWarnings({"java:S2925", "java:S2699"})
    void loginAndLogoutWorks() {
        final var testUser = getTestUser(UserRole.USER);
        login(testUser);

        final var page = getPage();
        page.navigate(getInstanceUrl() + "events");
        page.waitForURL("**/events");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("loginWorks_event-page");

        logout();
    }

}
