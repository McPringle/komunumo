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
package app.komunumo.ui.website.events;

import app.komunumo.data.service.EventService;
import app.komunumo.ui.IntegrationTest;
import app.komunumo.ui.website.confirmation.ConfirmationView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.QueryParameters;
import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static app.komunumo.util.TestUtil.findComponent;
import static app.komunumo.util.TestUtil.findComponents;
import static com.github.mvysny.kaributesting.v10.BasicUtilsKt._fireDomEvent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class RegisterFormIT extends IntegrationTest {

    private static final @NotNull String EMAIL_OKAY = "test@komunumo.app";
    private static final @NotNull Pattern EXTRACT_ID_PATTERN =
            Pattern.compile("http://localhost(?::\\d+)?/confirm\\?id=([0-9a-fA-F\\-]{36})(?:&.*)?");

    @Autowired
    private EventService eventService;

    private Details joinEventForm;
    private EmailField emailField;
    private Button emailButton;

    @Test
    void testJoinEventFlowSuccess() throws MessagingException {
        prepareEmailForm();

        // check toggling join form component
        openJoinForm();
        closeJoinForm();
        openJoinForm();

        // check entering email
        checkEmailFieldIsEmpty();
        enteringValidEmailEnablesButton();
        enteringInvalidEmailDisablesButton();
        checkEmailSendMessage();

        readEmailAndConfirm();
    }

    private void prepareEmailForm() {
        final var testEventWithImage = eventService.getUpcomingEventsWithImage()
                .stream()
                .filter(eventWithImage -> eventWithImage.image() != null)
                .findAny()
                .orElseThrow();
        final var testEvent = testEventWithImage.event();

        UI.getCurrent().navigate("events/" + testEvent.id());

        joinEventForm = _get(Details.class, spec -> spec.withClasses("register-form"));
        assertThat(joinEventForm).isNotNull();

        // find components
        emailField = findComponents(joinEventForm, EmailField.class).getFirst();
        emailButton = findComponents(joinEventForm, Button.class).getFirst();
    }

    private void openJoinForm() {
        assertThat(joinEventForm.isOpened()).isFalse();
        joinEventForm.getElement().setProperty("opened", true);
        _fireDomEvent(joinEventForm, "opened-changed");
        assertThat(joinEventForm.isOpened()).isTrue();
    }

    private void closeJoinForm() {
        assertThat(joinEventForm.isOpened()).isTrue();
        joinEventForm.getElement().setProperty("opened", false);
        _fireDomEvent(joinEventForm, "opened-changed");
        assertThat(joinEventForm.isOpened()).isFalse();
    }

    private void checkEmailFieldIsEmpty() {
        assertThat(emailField.getValue()).isEmpty();
        assertThat(emailButton.isEnabled()).isFalse();
    }

    private void enteringValidEmailEnablesButton() {
        emailField.setValue(EMAIL_OKAY);
        assertThat(emailButton.isEnabled()).isTrue();
    }

    private void enteringInvalidEmailDisablesButton() {
        emailField.setValue("test@komunumo");
        assertThat(emailButton.isEnabled()).isFalse();
    }

    private void checkEmailSendMessage() {
        emailField.setValue(EMAIL_OKAY);
        assertThat(emailButton.isEnabled()).isTrue();
        _click(emailButton);

        final var text = findComponents(joinEventForm, Paragraph.class).getFirst();
        assertThat(text).isNotNull();
        assertThat(text.getText()).startsWith("We have sent an email to your address \"" + EMAIL_OKAY + "\".");
        assertThat(findComponents(joinEventForm, EmailField.class)).isEmpty();
    }

    private void readEmailAndConfirm() throws MessagingException {
        await().atMost(2, SECONDS).until(() -> greenMail.getReceivedMessages().length == 1);
        final var confirmationMessage = greenMail.getReceivedMessages()[0];
        assertThat(confirmationMessage.getAllRecipients()[0])
                .hasToString(EMAIL_OKAY);

        final var body = getBody(confirmationMessage);
        final var confirmationId = extractConfirmationId(body);
        System.out.println("Confirmation id: " + confirmationId);

        UI.getCurrent().navigate(
                ConfirmationView.class,
                new QueryParameters(Map.of("id", List.of(confirmationId)))
        );

        final var main = _get(Main.class);
        final var markdown = findComponent(main, Markdown.class);
        assertThat(markdown).isNotNull();
        assertThat(markdown.getContent()).contains("You are now officially signed up for the event.");

        await().atMost(2, SECONDS).until(() -> greenMail.getReceivedMessages().length == 2);
        final var successMessage = greenMail.getReceivedMessages()[1];
        assertThat(successMessage.getAllRecipients()[0])
                .hasToString(EMAIL_OKAY);
        assertThat(getBody(successMessage)).contains("You are now officially signed up for the event.");
    }

    private String extractConfirmationId(final @NotNull String body) {
        Matcher matcher = EXTRACT_ID_PATTERN.matcher(body);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
