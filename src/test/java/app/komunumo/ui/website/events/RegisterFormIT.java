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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static app.komunumo.util.TestUtil.findComponents;
import static com.github.mvysny.kaributesting.v10.BasicUtilsKt._fireDomEvent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class RegisterFormIT extends IntegrationTest {

    private static final @NotNull String EMAIL_OKAY = "test@komunumo.app";

    @Autowired
    private EventService eventService;

    @MockitoBean
    private ParticipationService participationService;

    private Details joinEventForm;
    private EmailField emailField;
    private Button emailButton;

    @Test
    void testJoinEventFlowSuccess() {
        prepareEmailForm();

        // check toggling join form component
        openJoinForm();
        closeJoinForm();
        openJoinForm();

        // check entering email
        checkEmailFieldIsEmpty();
        enteringValidEmailEnablesButton();
        enteringInvalidEmailDisablesButton();
        checkEmailSuccessMessage();
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

    private void checkEmailSuccessMessage() {
        emailField.setValue(EMAIL_OKAY);
        assertThat(emailButton.isEnabled()).isTrue();
        _click(emailButton);

        final var text = findComponents(joinEventForm, Paragraph.class).getFirst();
        assertThat(text).isNotNull();
        assertThat(text.getText()).startsWith("We have sent an email to your address \"" + EMAIL_OKAY + "\".");
        assertThat(findComponents(joinEventForm, EmailField.class)).isEmpty();
    }

}
