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

import app.komunumo.data.service.CommunityService;
import app.komunumo.ui.BrowserTest;
import app.komunumo.util.LinkUtil;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static app.komunumo.util.TestUtil.extractLinkFromText;
import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class JoinCommunityFlowBT extends BrowserTest {

    private static final String JOIN_BUTTON_SELECTOR = "vaadin-button:has-text('Join Community')";

    @Autowired
    private @NotNull CommunityService communityService;

    @Test
    void joinCommunityAnonymously() throws MessagingException {
        final var demoCommunity = communityService.getCommunities().getFirst();
        final var demoCommunityLink = LinkUtil.getLink(demoCommunity);
        final var demoCommunityNameSelector = "h2.community-name";

        final var page = getPage();
        page.navigate(demoCommunityLink);
        page.waitForURL("**/communities/" + demoCommunity.profile());
        page.waitForSelector(demoCommunityNameSelector);
        captureScreenshot("joinCommunityAnonymously_detailViewLoaded");

        // click the join button
        page.click(JOIN_BUTTON_SELECTOR);

        // wait for join dialog to appear
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());
        captureScreenshot("joinCommunityAnonymously_dialog");

        // fill in email address
        final var emailInput = page.locator("vaadin-email-field").locator("input");
        emailInput.fill("anonymous@example.com");
        captureScreenshot("joinCommunityAnonymously_dialogFilled");

        // click on the request email button
        page.locator("vaadin-button.email-button").click();

        // close the dialog
        final var closeButton = page.locator("vaadin-button:has-text(\"Close\")");
        closeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        captureScreenshot("joinCommunityAnonymously_dialogAfterEmailRequested");
        closeButton.click();

        // wait for email confirmation mail
        final var greenMail = getGreenMail();
        await().atMost(2, SECONDS).untilAsserted(() -> greenMail.waitForIncomingEmail(1));
        final var receivedMessage = greenMail.getReceivedMessages()[0];
        assertThat(receivedMessage.getSubject()).isEqualTo("[Komunumo Test] Please confirm your email address");

        // extract the confirmation link
        final var mailBody = GreenMailUtil.getBody(receivedMessage);
        final var confirmationLink = extractLinkFromText(mailBody);
        assertThat(confirmationLink).isNotNull();

        // open the confirmation link
        page.navigate(confirmationLink);
        page.waitForURL("**/confirm**");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("joinCommunityAnonymously_confirmationPage");

        // wait for join confirmation mail
        await().atMost(2, SECONDS).until(() -> greenMail.getReceivedMessages().length == 2);
        final var successMessage = greenMail.getReceivedMessages()[1];
        assertThat(successMessage.getSubject()).isEqualTo("[Komunumo Test] You have joined a community");
        assertThat(getBody(successMessage)).contains("You are now part of the community \"%s\".".formatted(demoCommunity.name()));
    }

}
