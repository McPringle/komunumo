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
import app.komunumo.data.service.ParticipationService;
import app.komunumo.ui.IntegrationTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.EmailField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static app.komunumo.util.TestUtil.findComponents;
import static com.github.mvysny.kaributesting.v10.BasicUtilsKt._fireDomEvent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class JoinEventFormIT extends IntegrationTest {

    @Autowired
    private EventService eventService;

    @MockitoBean
    private ParticipationService participationService;

    private Details joinEventForm;
    private EmailField emailField;
    private Button emailButton;

    @BeforeEach
    void prepareTests() {
        // fail sending the verification code when using email address "fail@komunumo.app"
        when(participationService.requestVerificationCode(any(), anyString(), any()))
                .thenAnswer(inv -> {
                    final var email = inv.getArgument(1, String.class);
                    return !email.equals("fail@komunumo.app");
                });

        final var testEventWithImage = eventService.getUpcomingEventsWithImage()
                .stream()
                .filter(eventWithImage -> eventWithImage.image() != null)
                .findAny()
                .orElseThrow();
        final var testEvent = testEventWithImage.event();

        UI.getCurrent().navigate("events/" + testEvent.id());

        joinEventForm = _get(Details.class, spec -> spec.withClasses("join-event-form"));
        assertThat(joinEventForm).isNotNull();
        assertThat(joinEventForm.isOpened()).isFalse();

        // open the form
        joinEventForm.getElement().setProperty("opened", true);
        _fireDomEvent(joinEventForm, "opened-changed");
        assertThat(joinEventForm.isOpened()).isTrue();

        // find components
        emailField = findComponents(joinEventForm, EmailField.class).getFirst();
        emailButton = findComponents(joinEventForm, Button.class).getFirst();

    }

    @Test
    void emptyFieldDisablesButton() {
        assertThat(emailField.getValue()).isEmpty();
        assertThat(emailButton.isEnabled()).isFalse();
    }

    @Test
    void enteringValidEmailEnablesButton() {
        emailField.setValue("test@komunumo.app");
        assertThat(emailButton.isEnabled()).isTrue();
    }

    @Test
    void enteringInvalidEmailDisablesButton() {
        emailField.setValue("test@komunumo");
        assertThat(emailButton.isEnabled()).isFalse();
    }

    @Test
    void checkErrorMessageWhenSendingEmailFails() {
        emailField.setValue("fail@komunumo.app");
        assertThat(emailButton.isEnabled()).isTrue();
        _click(emailButton);
        assertThat(emailField.getErrorMessage()).startsWith("The confirmation code could not be sent");
        assertThat(emailField.getValue()).isEqualTo("fail@komunumo.app");
        assertThat(findComponents(joinEventForm, EmailField.class)).containsExactly(emailField);
    }

    @Test
    void checkSuccessMessage() {
        emailField.setValue("test@komunumo.app");
        assertThat(emailButton.isEnabled()).isTrue();
        _click(emailButton);
        final var text = findComponents(joinEventForm, Paragraph.class).getFirst();
        assertThat(text).isNotNull();
        assertThat(text.getText()).isEqualTo("We have just sent an email to test@komunumo.app.");
        assertThat(findComponents(joinEventForm, EmailField.class)).isEmpty();
    }

}
