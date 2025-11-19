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
package app.komunumo.business.user.boundary;

import app.komunumo.business.user.entity.UserType;
import app.komunumo.business.user.control.RegistrationService;
import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.business.user.control.UserService;
import app.komunumo.ui.BrowserTest;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static app.komunumo.business.core.config.entity.ConfigurationSetting.INSTANCE_REGISTRATION_ALLOWED;
import static app.komunumo.util.TestUtil.extractLinkFromText;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class RegistrationFlowBT extends BrowserTest {

    private static final String REGISTER_MENU_ITEM_SELECTOR = "vaadin-context-menu-item[role='menuitem']:has-text('Register')";
    private static final String NEW_USER_EMAIL = "new@example.com";

    @Autowired
    private @NotNull ConfigurationService configurationService;

    @Autowired
    private @NotNull UserService userService;

    @AfterEach
    void removeTestUsers() {
        userService.getUserByEmail(NEW_USER_EMAIL).ifPresent(userService::deleteUser);
    }

    @Test
    void newUserCanRegister() throws FolderException {
        testRegistrationFlow("newUserCanRegister", NEW_USER_EMAIL);
    }

    @Test
    void anonymousUserCanUpgrade() throws FolderException {
        final var anonymousUser = getTestUser(UserType.ANONYMOUS);
        assertThat(anonymousUser.email()).isNotNull();
        testRegistrationFlow("anonymousUserCanUpgrade", anonymousUser.email());
    }

    @Test
    void remoteUserCanUpgrade() throws FolderException {
        final var remoteUser = getTestUser(UserType.REMOTE);
        assertThat(remoteUser.email()).isNotNull();
        testRegistrationFlow("remoteUserCanUpgrade", remoteUser.email());
    }

    private void testRegistrationFlow(final @NotNull String screenshotPrefix,
                                      final @NotNull String email)
            throws FolderException {
        final var greenMail = getGreenMail();
        greenMail.purgeEmailFromAllMailboxes();

        // navigate to home page
        final var page = getPage();
        page.navigate(getInstanceUrl());
        page.waitForSelector(getInstanceNameSelector());

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot(screenshotPrefix + "_profile-menu-with-register-item");

        // click on register menu item
        page.click(REGISTER_MENU_ITEM_SELECTOR);

        // wait for register dialog to appear
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());
        captureScreenshot(screenshotPrefix + "_register-dialog-empty");

        // fill in email address
        final var emailInput = page.locator("vaadin-email-field").locator("input");
        emailInput.fill(email);
        captureScreenshot(screenshotPrefix + "_register-dialog-filled");

        // click on the request email button
        page.locator("vaadin-button.email-button").click();
        final var closeButton = page.locator("vaadin-button:has-text(\"Close\")");
        closeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        closeButton.click();
        captureScreenshot(screenshotPrefix + "_after-email-requested");

        // wait for the confirmation email
        final var confirmationMessage = getEmailBySubject("[Komunumo Test] Please confirm your email address");

        // extract the confirmation link
        final var confirmationMailBody = GreenMailUtil.getBody(confirmationMessage);
        final var confirmationLink = extractLinkFromText(confirmationMailBody);
        assertThat(confirmationLink).isNotNull();

        // open the confirmation link
        page.navigate(confirmationLink);
        page.waitForURL("**/confirm**");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot(screenshotPrefix + "_confirmation-page");

        // check for success message
        final var message = page.locator("vaadin-notification-card").first();
        assertThat(message.isVisible()).isTrue();
        assertThat(message.textContent()).isEqualTo(
                "Your local account has been successfully created, and you are now logged in with your new account.");

        // user should now be local
        final var localUser = userService.getUserByEmail(email);
        assertThat(localUser).isPresent();
        assertThat(localUser.orElseThrow().type()).isEqualTo(UserType.LOCAL);

        // wait for the registration email
        getEmailBySubject("[Komunumo Test] Your new local account is ready");

        // reload page and check that user is logged in
        page.reload();

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot(screenshotPrefix + "_profile-menu-without-register-item");

        // register menu item should not be visible
        assertThat(page.locator(REGISTER_MENU_ITEM_SELECTOR).count()).isZero();

        // Logout menu item should be visible
        assertThat(page.locator(LOGOUT_MENU_ITEM_SELECTOR).count()).isOne();

        logout();
    }

    @Test
    void existingLocalUserIsAlreadyRegistered() {
        final var localUser = getTestUser(UserType.LOCAL);
        login(localUser);

        final var page = getPage();

        // navigate to home page
        page.navigate(getInstanceUrl());
        page.waitForSelector(getInstanceNameSelector());

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("existingLocalUserIsAlreadyRegistered_profile-menu-without-register-item");

        // register menu item should not be visible
        assertThat(page.locator(REGISTER_MENU_ITEM_SELECTOR).count()).isZero();

        logout();
    }

    @Test
    void registrationDisabled() {
        try (var logCaptor = LogCaptor.forClass(RegistrationService.class)) {

            // new registrations are allowed by default
            assertThat(configurationService.getConfiguration(INSTANCE_REGISTRATION_ALLOWED, Boolean.class)).isTrue();

            // navigate to home page
            final var page = getPage();
            page.navigate(getInstanceUrl());
            page.waitForSelector(getInstanceNameSelector());

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(CONTEXT_MENU_SELECTOR);
            captureScreenshot("registrationDisabled_profile-menu-before-disabled");

            // check for registration menu item
            assertThat(page.locator(REGISTER_MENU_ITEM_SELECTOR).isVisible()).isTrue();

            // disable registration
            configurationService.setConfiguration(INSTANCE_REGISTRATION_ALLOWED, "false");
            assertThat(configurationService.getConfiguration(INSTANCE_REGISTRATION_ALLOWED, Boolean.class)).isFalse();

            // click on register menu item
            page.click(REGISTER_MENU_ITEM_SELECTOR);

            // check log for registration disabled message
            await().atMost(2, SECONDS).untilAsserted(
                    () -> assertThat(logCaptor.getWarnLogs())
                            .contains("Registration attempt while registration is disabled."));

            // reload page
            page.reload();
            page.waitForSelector(getInstanceNameSelector());

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(CONTEXT_MENU_SELECTOR);
            captureScreenshot("registrationDisabled_profile-menu-after-disabled");

            // check for registration menu item
            assertThat(page.locator(REGISTER_MENU_ITEM_SELECTOR).isVisible()).isFalse();
        } finally {
            // ensure registration is enabled at the end of the test
            configurationService.setConfiguration(INSTANCE_REGISTRATION_ALLOWED, "true");
            assertThat(configurationService.getConfiguration(INSTANCE_REGISTRATION_ALLOWED, Boolean.class)).isTrue();
        }
    }
}
