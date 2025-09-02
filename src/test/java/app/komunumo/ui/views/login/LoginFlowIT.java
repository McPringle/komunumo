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
package app.komunumo.ui.views.login;

import app.komunumo.data.dto.UserDto;
import app.komunumo.data.dto.UserRole;
import app.komunumo.data.dto.UserType;
import app.komunumo.data.service.UserService;
import app.komunumo.ui.BrowserTest;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static app.komunumo.util.TestUtil.extractLinkFromText;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class LoginFlowIT extends BrowserTest {

    protected static final String INSTANCE_NAME_SELECTOR = "h1:has-text('Your Instance Name')";
    protected static final String AVATAR_SELECTOR = "vaadin-avatar";
    protected static final String LOGIN_MENU_ITEM_SELECTOR = "vaadin-context-menu-item[role='menuitem']:has-text('Login')";
    protected static final String LOGOUT_MENU_ITEM_SELECTOR = "vaadin-context-menu-item[role='menuitem']:has-text('Logout')";
    private UserDto testUser;

    @BeforeAll
    void createTestUser() {
        final var userService = getBean(UserService.class);
        testUser = userService.storeUser(new UserDto(null, null, null,
                "@loginLogoutFlow", "success@example.com", "Test User", "", null,
                UserRole.USER, UserType.LOCAL));
    }

    @AfterAll
    void removeTestUser() {
        getBean(UserService.class).deleteUser(testUser);
    }

    @Test
    @SuppressWarnings({"java:S2925", "java:S2699"})
    void loginAndLogoutWorks() throws InterruptedException, MessagingException {
        final var page = getPage();

        // navigate to events page
        page.navigate("http://localhost:8081/events");
        page.waitForURL("**/events");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("loginAndLogoutWorks_before-login");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(LOGIN_MENU_ITEM_SELECTOR);
        captureScreenshot("loginAndLogoutWorks_profile-menu-before-login");

        // click on login menu item
        page.click(LOGIN_MENU_ITEM_SELECTOR);

        // wait for login dialog to appear
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());
        captureScreenshot("loginAndLogoutWorks_login-dialog-empty");

        // fill in email address
        final var emailInput = page.locator("vaadin-email-field").locator("input");
        emailInput.fill("success@example.com");
        captureScreenshot("loginAndLogoutWorks_login-dialog-filled");

        // click on the request email button
        page.locator("vaadin-button.email-button").click();

        // close the dialog
        final var closeButton = page.locator("vaadin-button:has-text(\"Close\")");
        closeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        captureScreenshot("loginAndLogoutWorks_after-email-requested");
        closeButton.click();

        // wait for the confirmation email
        await().atMost(2, SECONDS).untilAsserted(() -> {
            greenMail.waitForIncomingEmail(1);
        });
        final var receivedMessage = greenMail.getReceivedMessages()[0];
        assertThat(receivedMessage.getSubject())
                .isEqualTo("[Your Instance Name] Please confirm your email address");

        // extract the confirmation link
        final var mailBody = GreenMailUtil.getBody(receivedMessage);
        final var confirmationLink = extractLinkFromText(mailBody);
        assertThat(confirmationLink).isNotNull();

        // open the confirmation link
        page.navigate(confirmationLink);
        page.waitForURL("**/confirm**");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("loginAndLogoutWorks_confirmation-page");

        // navigate back to the events page
        page.navigate("http://localhost:8081/events");
        page.waitForURL("**/events");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("loginAndLogoutWorks_after-login");

        page.click(AVATAR_SELECTOR);
        page.waitForSelector(LOGOUT_MENU_ITEM_SELECTOR);
        captureScreenshot("loginAndLogoutWorks_profile-menu-after-login");

        page.click(LOGOUT_MENU_ITEM_SELECTOR);
        Thread.sleep(500); // wait for the logout process to complete
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(LOGIN_MENU_ITEM_SELECTOR);
        captureScreenshot("loginAndLogoutWorks_after-logout");
    }

    @Test
    @SuppressWarnings({"java:S2925", "java:S2699"})
    void loginFails() throws MessagingException {
        final var page = getPage();

        // navigate to events page
        page.navigate("http://localhost:8081/events");
        page.waitForURL("**/events");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("loginFails_before-login");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(LOGIN_MENU_ITEM_SELECTOR);
        captureScreenshot("loginFails_profile-menu-before-login");

        // click on login menu item
        page.click(LOGIN_MENU_ITEM_SELECTOR);

        // wait for login dialog to appear
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());
        captureScreenshot("loginFails_login-dialog-empty");

        // fill in email address
        final var emailInput = page.locator("vaadin-email-field").locator("input");
        emailInput.fill("fail@example.com");
        captureScreenshot("loginFails_login-dialog-filled");

        // click on the request email button
        page.locator("vaadin-button.email-button").click();
        final var closeButton = page.locator("vaadin-button:has-text(\"Close\")");
        closeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        closeButton.click();
        captureScreenshot("loginFails_after-email-requested");

        // wait for the confirmation email
        await().atMost(2, SECONDS).untilAsserted(() -> {
            greenMail.waitForIncomingEmail(1);
        });
        final var receivedMessage = greenMail.getReceivedMessages()[0];
        assertThat(receivedMessage.getSubject())
                .isEqualTo("[Your Instance Name] Please confirm your email address");

        // extract the confirmation link
        final var mailBody = GreenMailUtil.getBody(receivedMessage);
        final var confirmationLink = extractLinkFromText(mailBody);
        assertThat(confirmationLink).isNotNull();

        // open the confirmation link
        page.navigate(confirmationLink);
        page.waitForURL("**/confirm**");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("loginFails_confirmation-page");

        // check for error message
        final var message = page.locator("vaadin-markdown.success-message").textContent();
        assertThat(message).startsWith("The login to your profile was not successful.");
    }

    @Test
    void loginDialogOpenAndClose() {
        final var page = getPage();

        // navigate to events page
        page.navigate("http://localhost:8081/events");
        page.waitForURL("**/events");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("loginDialogOpenAndClose_before-login");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(LOGIN_MENU_ITEM_SELECTOR);
        captureScreenshot("loginDialogOpenAndClose_profile-menu-before-login");

        // click on login menu item
        page.click(LOGIN_MENU_ITEM_SELECTOR);

        // wait for login dialog to appear
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());
        captureScreenshot("loginDialogOpenAndClose_login-dialog-empty");

        // close the dialog
        final var closeButton = page.locator("vaadin-button.close-dialog-button");
        closeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        closeButton.click();
    }

    @Test
    void loginDialogCancel() {
        final var page = getPage();

        // navigate to events page
        page.navigate("http://localhost:8081/events");
        page.waitForURL("**/events");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("loginDialogCancel_before-login");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(LOGIN_MENU_ITEM_SELECTOR);
        captureScreenshot("loginDialogCancel_profile-menu-before-login");

        // click on login menu item
        page.click(LOGIN_MENU_ITEM_SELECTOR);

        // wait for login dialog to appear
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());
        captureScreenshot("loginDialogCancel_login-dialog-empty");

        // cancel the dialog
        final var cancelButton = page.locator("vaadin-button.cancel-button");
        cancelButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        cancelButton.click();
    }

}
