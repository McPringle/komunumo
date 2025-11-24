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
import com.icegreen.greenmail.util.GreenMailUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;

import static app.komunumo.test.TestUtil.extractLinkFromText;
import static org.assertj.core.api.Assertions.assertThat;

class LoginFlowBT extends BrowserTest {

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

    @Test
    @SuppressWarnings({"java:S2925", "java:S2699"})
    void loginShouldFail() {
        final var page = getPage();

        // navigate to events page
        page.navigate(getInstanceUrl() + "events");
        page.waitForURL("**/events");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("loginFails_before-login");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
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
        final var confirmationMessage = getEmailBySubject("[Komunumo Test] Please confirm your email address");

        // extract the confirmation link
        final var mailBody = GreenMailUtil.getBody(confirmationMessage);
        final var confirmationLink = extractLinkFromText(mailBody);
        assertThat(confirmationLink).isNotNull();

        // open the confirmation link
        page.navigate(confirmationLink);
        page.waitForURL("**/confirm**");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("loginFails_confirmation-page");

        // check for error message
        final var message = page.locator("vaadin-notification-card").first();
        assertThat(message.isVisible()).isTrue();
        assertThat(message.textContent()).startsWith("The login to your profile was not successful.");
    }

    @Test
    void loginDialogOpenAndClose() {
        final var page = getPage();

        // navigate to events page
        page.navigate(getInstanceUrl() + "events");
        page.waitForURL("**/events");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("loginDialogOpenAndClose_before-login");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
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
        page.navigate(getInstanceUrl() + "events");
        page.waitForURL("**/events");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("loginDialogCancel_before-login");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
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
