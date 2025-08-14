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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.textfield.EmailField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static app.komunumo.util.TestUtil.findComponents;
import static com.github.mvysny.kaributesting.v10.BasicUtilsKt._fireDomEvent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class JoinEventFormIT extends IntegrationTest {

    @Autowired
    private EventService eventService;

    @Test
    void joinEventFlow() {
        final var testEventWithImage = eventService.getUpcomingEventsWithImage()
                .stream()
                .filter(eventWithImage -> eventWithImage.image() != null)
                .findAny()
                .orElseThrow();
        final var testEvent = testEventWithImage.event();

        UI.getCurrent().navigate("events/" + testEvent.id());

        final var joinEventForm = _get(Details.class, spec -> spec.withClasses("join-event-form"));
        assertThat(joinEventForm).isNotNull();
        assertThat(joinEventForm.isOpened()).isFalse();

        // open the form
        joinEventForm.getElement().setProperty("opened", true);
        _fireDomEvent(joinEventForm, "opened-changed");
        assertThat(joinEventForm.isOpened()).isTrue();

        // find components
        final var emailField = findComponents(joinEventForm, EmailField.class).getFirst();
        final var joinButton = findComponents(joinEventForm, Button.class).getFirst();

        // email field is empty and button is disabled
        assertThat(emailField.getValue()).isEmpty();
        assertThat(joinButton.isEnabled()).isFalse();

        // entering invalid email address keeps the button disabled
        emailField.setValue("foobar@example");
        assertThat(joinButton.isEnabled()).isFalse();

        // entering valid email address enables the button
        emailField.setValue("foobar@example.eu");
        assertThat(joinButton.isEnabled()).isTrue();
    }

}
