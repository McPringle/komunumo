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
package app.komunumo.domain.participant.control;

import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.domain.user.entity.UserDto;
import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.domain.user.entity.UserType;
import app.komunumo.domain.core.confirmation.entity.ConfirmationContext;
import app.komunumo.domain.core.confirmation.entity.ConfirmationStatus;
import app.komunumo.test.KaribuTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Locale;

import static app.komunumo.domain.participant.control.ParticipantService.CONTEXT_KEY_EVENT;
import static org.assertj.core.api.Assertions.assertThat;

class ParticipantServiceKT extends KaribuTest {

    @Autowired
    private @NotNull ParticipantService participantService;

    @Autowired
    private @NotNull EventService eventService;

    @Autowired
    private @NotNull UserService userService;

    @Test
    void joinEventExistingUser() {
        final var event = eventService.getUpcomingEventsWithImage().getFirst().event();
        assertThat(event).isNotNull();
        assertThat(event.id()).isNotNull();

        assertThat(participantService.getParticipantsCount(event.id())).isZero();

        final var email = "test@komunumo.app";
        assertThat(userService.getUserByEmail(email)).isEmpty();

        userService.storeUser(new UserDto(null, null, null, null, email, "", "",
                null, UserRole.USER, UserType.LOCAL));
        final var user = userService.getUserByEmail(email).orElseThrow();
        assertThat(user).isNotNull();

        final var context = ConfirmationContext.of(CONTEXT_KEY_EVENT, event);
        final var locale = Locale.ENGLISH;
        final var confirmationResponse = participantService.registerForEvent(email, context, locale);
        assertThat(confirmationResponse.confirmationStatus()).isEqualTo(ConfirmationStatus.SUCCESS);

        assertThat(participantService.getParticipantsCount(event.id())).isOne();
        final var participants = participantService.getAllParticipants();
        assertThat(participants).hasSize(1);

        final var participant = participants.getFirst();
        assertThat(participant).isNotNull().satisfies(testee -> {
            assertThat(testee.eventId()).isEqualTo(event.id());
            assertThat(testee.userId()).isEqualTo(user.id());
            assertThat(testee.registered()).isNotNull();
            assertThat(testee.registered()).isBeforeOrEqualTo(ZonedDateTime.now());
        });

        assertThat(participantService.deleteParticipant(participant)).isTrue();
        assertThat(participantService.deleteParticipant(participant)).isFalse();
        assertThat(participantService.getAllParticipants()).isEmpty();
        assertThat(participantService.getParticipantsCount(event.id())).isZero();

        assertThat(userService.deleteUser(user)).isTrue();
        assertThat(userService.deleteUser(user)).isFalse();
    }

    @Test
    void joinEventAnonymousUser() {
        assertThat(participantService.getAllParticipants()).isEmpty();

        final var email = "test@komunumo.app";
        assertThat(userService.getUserByEmail(email)).isEmpty();

        final var event = eventService.getUpcomingEventsWithImage().getFirst().event();
        assertThat(event).isNotNull();

        final var context = ConfirmationContext.of(CONTEXT_KEY_EVENT, event);
        final var locale = Locale.ENGLISH;
        final var confirmationResponse1 = participantService.registerForEvent(email, context, locale);
        assertThat(confirmationResponse1.confirmationStatus()).isEqualTo(ConfirmationStatus.SUCCESS);

        // try to join again with the same email
        final var confirmationResponse2 = participantService.registerForEvent(email, context, locale);
        assertThat(confirmationResponse2.confirmationStatus()).isEqualTo(ConfirmationStatus.SUCCESS);

        final var participants = participantService.getAllParticipants();
        assertThat(participants).hasSize(1);

        final var user = userService.getUserByEmail(email).orElseThrow();
        assertThat(user.email()).isEqualTo(email);

        final var participant = participants.getFirst();
        assertThat(participant).isNotNull().satisfies(testee -> {
            assertThat(testee.eventId()).isEqualTo(event.id());
            assertThat(testee.userId()).isEqualTo(user.id());
            assertThat(testee.registered()).isNotNull();
            assertThat(testee.registered()).isBeforeOrEqualTo(ZonedDateTime.now());
        });

        assertThat(participantService.deleteParticipant(participant)).isTrue();
        assertThat(participantService.deleteParticipant(participant)).isFalse();
        assertThat(participantService.getAllParticipants()).isEmpty();

        assertThat(userService.deleteUser(user)).isTrue();
        assertThat(userService.deleteUser(user)).isFalse();
    }

}
