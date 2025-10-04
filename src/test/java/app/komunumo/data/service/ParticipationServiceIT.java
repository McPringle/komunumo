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
package app.komunumo.data.service;

import app.komunumo.data.dto.UserDto;
import app.komunumo.data.dto.UserRole;
import app.komunumo.data.dto.UserType;
import app.komunumo.data.service.confirmation.ConfirmationContext;
import app.komunumo.data.service.confirmation.ConfirmationStatus;
import app.komunumo.ui.IntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Locale;

import static app.komunumo.data.service.ParticipationService.CONTEXT_KEY_EVENT;
import static org.assertj.core.api.Assertions.assertThat;

class ParticipationServiceIT extends IntegrationTest {

    @Autowired
    private @NotNull ParticipationService participationService;

    @Autowired
    private @NotNull EventService eventService;

    @Autowired
    private @NotNull UserService userService;

    @Test
    void joinEventExistingUser() {
        assertThat(participationService.getParticipations()).isEmpty();

        final var email = "test@komunumo.app";
        assertThat(userService.getUserByEmail(email)).isEmpty();

        userService.storeUser(new UserDto(null, null, null, null, email, "", "",
                null, UserRole.USER, UserType.LOCAL));
        final var user = userService.getUserByEmail(email).orElseThrow();
        assertThat(user).isNotNull();

        final var event = eventService.getUpcomingEventsWithImage().getFirst().event();
        assertThat(event).isNotNull();

        final var context = ConfirmationContext.of(CONTEXT_KEY_EVENT, event);
        final var locale = Locale.ENGLISH;
        final var confirmationResponse = participationService.registerForEvent(email, context, locale);
        assertThat(confirmationResponse.confirmationStatus()).isEqualTo(ConfirmationStatus.SUCCESS);

        final var participations = participationService.getParticipations();
        assertThat(participations).hasSize(1);

        final var participation = participations.getFirst();
        assertThat(participation).isNotNull().satisfies(testee -> {
            assertThat(testee.eventId()).isEqualTo(event.id());
            assertThat(testee.userId()).isEqualTo(user.id());
            assertThat(testee.registered()).isNotNull();
            assertThat(testee.registered()).isBeforeOrEqualTo(ZonedDateTime.now());
        });

        assertThat(participationService.deleteParticipation(participation)).isTrue();
        assertThat(participationService.deleteParticipation(participation)).isFalse();
        assertThat(participationService.getParticipations()).isEmpty();

        assertThat(userService.deleteUser(user)).isTrue();
        assertThat(userService.deleteUser(user)).isFalse();
    }

    @Test
    void joinEventAnonymousUser() {
        assertThat(participationService.getParticipations()).isEmpty();

        final var email = "test@komunumo.app";
        assertThat(userService.getUserByEmail(email)).isEmpty();

        final var event = eventService.getUpcomingEventsWithImage().getFirst().event();
        assertThat(event).isNotNull();

        final var context = ConfirmationContext.of(CONTEXT_KEY_EVENT, event);
        final var locale = Locale.ENGLISH;
        final var confirmationResponse1 = participationService.registerForEvent(email, context, locale);
        assertThat(confirmationResponse1.confirmationStatus()).isEqualTo(ConfirmationStatus.SUCCESS);

        // try to join again with the same email
        final var confirmationResponse2 = participationService.registerForEvent(email, context, locale);
        assertThat(confirmationResponse2.confirmationStatus()).isEqualTo(ConfirmationStatus.SUCCESS);

        final var participations = participationService.getParticipations();
        assertThat(participations).hasSize(1);

        final var user = userService.getUserByEmail(email).orElseThrow();
        assertThat(user.email()).isEqualTo(email);

        final var participation = participations.getFirst();
        assertThat(participation).isNotNull().satisfies(testee -> {
            assertThat(testee.eventId()).isEqualTo(event.id());
            assertThat(testee.userId()).isEqualTo(user.id());
            assertThat(testee.registered()).isNotNull();
            assertThat(testee.registered()).isBeforeOrEqualTo(ZonedDateTime.now());
        });

        assertThat(participationService.deleteParticipation(participation)).isTrue();
        assertThat(participationService.deleteParticipation(participation)).isFalse();
        assertThat(participationService.getParticipations()).isEmpty();

        assertThat(userService.deleteUser(user)).isTrue();
        assertThat(userService.deleteUser(user)).isFalse();
    }

}
