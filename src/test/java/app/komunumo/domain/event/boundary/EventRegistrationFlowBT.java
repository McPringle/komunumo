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

import app.komunumo.domain.user.entity.UserDto;
import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.domain.user.entity.UserType;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.test.BrowserTest;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static app.komunumo.test.TestUtil.extractLinkFromText;
import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.assertj.core.api.Assertions.assertThat;

class EventRegistrationFlowBT extends BrowserTest {

    private static final String REGISTRATION_BUTTON_SELECTOR = "vaadin-button:has-text('Register')";

    @Autowired
    private @NotNull EventService eventService;

    @Autowired
    private @NotNull UserService userService;

    @Test
    void testRegistrationFlowSuccess_withAnonymousUser() {
        // prepare a test event
        final var testEventWithImage = eventService.getUpcomingEventsWithImage()
                .stream()
                .filter(eventWithImage -> eventWithImage.image() != null)
                .findAny()
                .orElseThrow();
        final var testEvent = testEventWithImage.event();

        // navigate to events page
        final var page = getPage();
        page.navigate(getInstanceUrl() + "events/" + testEvent.id());
        page.waitForURL("**/events/" + testEvent.id());
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("events-page");

        // click on registration button
        page.click(REGISTRATION_BUTTON_SELECTOR);

        // wait for registration dialog to appear
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());
        captureScreenshot("registration-dialog");

        // fill in email address
        final var emailInput = page.locator("vaadin-email-field").locator("input");
        emailInput.fill("anonymous@example.com");
        captureScreenshot("registration-dialog-filled");

        // click on the request email button
        page.locator("vaadin-button.email-button").click();

        // close the dialog
        final var closeButton = page.locator("vaadin-button:has-text(\"Close\")");
        closeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        captureScreenshot("registration-dialog-after-email-requested");
        closeButton.click();

        // wait for email confirmation mail
        final var confirmationMessage = getEmailBySubject("[Komunumo Test] Please confirm your email address");

        // extract the confirmation link
        final var mailBody = GreenMailUtil.getBody(confirmationMessage);
        final var confirmationLink = extractLinkFromText(mailBody);
        assertThat(confirmationLink).isNotNull();

        // open the confirmation link
        page.navigate(confirmationLink);
        page.waitForURL("**/confirm**");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("confirmation-page");

        // wait for registration confirmation mail
        final var successMessage = getEmailBySubject("[Komunumo Test] Your registration is confirmed");
        assertThat(getBody(successMessage)).contains("You are now officially signed up for the event.");
    }

    @Test
    void testRegistrationFlowSuccess_withLocalUser() throws FolderException {
        final var greenMail = getGreenMail();

        // prepare a test event
        final var testEventWithImage = eventService.getUpcomingEventsWithImage()
                .stream()
                .filter(eventWithImage -> eventWithImage.image() != null)
                .findAny()
                .orElseThrow();
        final var testEvent = testEventWithImage.event();

        // prepare a test user
        final var testUser = userService.storeUser(new UserDto(null, null, null,
                "@test@example.com", "test@example.com", "Test User", "This is a test user.", null,
                UserRole.USER, UserType.LOCAL));

        // log in as the test user
        login(testUser);
        greenMail.purgeEmailFromAllMailboxes();

        // navigate to events page
        final var page = getPage();
        page.navigate(getInstanceUrl() + "events/" + testEvent.id());
        page.waitForURL("**/events/" + testEvent.id());
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("events-page");

        // click on registration button
        page.click(REGISTRATION_BUTTON_SELECTOR);

        // wait for registration dialog to appear
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());

        // check prefilled email address
        final var emailInput = page.locator("vaadin-email-field").locator("input");
        captureScreenshot("registration-dialog-filled");
        assertThat(emailInput.inputValue()).isEqualTo("test@example.com");

        // click on the request email button
        page.locator("vaadin-button.email-button").click();

        // close the dialog
        final var closeButton = page.locator("vaadin-button:has-text(\"Close\")");
        closeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        captureScreenshot("registration-dialog-after-email-requested");
        closeButton.click();

        // wait for email confirmation mail
        final var confirmationMessage = getEmailBySubject("[Komunumo Test] Please confirm your email address");

        // extract the confirmation link
        final var mailBody = GreenMailUtil.getBody(confirmationMessage);
        final var confirmationLink = extractLinkFromText(mailBody);
        assertThat(confirmationLink).isNotNull();

        // open the confirmation link
        page.navigate(confirmationLink);
        page.waitForURL("**/confirm**");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("confirmation-page");

        // wait for registration confirmation mail
        final var successMessage = getEmailBySubject("[Komunumo Test] Your registration is confirmed");
        assertThat(getBody(successMessage)).contains("You are now officially signed up for the event.");

        // logout the test user
        logout();
    }

}
