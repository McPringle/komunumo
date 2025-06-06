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
package app.komunumo.ui.website.login;

import app.komunumo.data.dto.UserDto;
import app.komunumo.data.dto.UserRole;
import app.komunumo.data.service.SecurityService;
import app.komunumo.data.service.UserService;
import app.komunumo.ui.BrowserTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LoginFlowTest extends BrowserTest {

    private UserDto testUser;

    @BeforeAll
    void createTestUser() {
        final var userService = getBean(UserService.class);
        final var securityService = getBean(SecurityService.class);
        final var encodedPassword = securityService.encodePassword("foobar");
        testUser = userService.storeUser(new UserDto(null, null, null,
                "@loginLogoutFlow", "login-logout-flow@localhost", "Test User", "", null,
                UserRole.USER, encodedPassword));
    }

    @AfterAll
    void removeTestUser() {
        getBean(UserService.class).deleteUser(testUser);
    }

    @Test
    void loginAndLogoutWorks() {
        final var page = getPage();

        page.navigate("http://localhost:8081/");
        page.waitForSelector("h1:has-text('Komunumo')");
        captureScreenshot("home-before-login");

        assertLinkIsVisible("login");
        page.click("a[href='login']");
        page.waitForURL("**/login");
        page.waitForSelector("div:has-text('Log in')");

        page.fill("input[name='username']", "login-logout-flow@localhost");
        page.fill("input[name='password']", "foobar");
        captureScreenshot("login");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();

        page.waitForSelector("h1:has-text('Komunumo')");
        captureScreenshot("home-after-login");
        assertLinkIsVisible("logout");

        page.click("a[href='logout']");
        page.waitForSelector("a[href='login']");
        captureScreenshot("home-after-logout");
    }

}
