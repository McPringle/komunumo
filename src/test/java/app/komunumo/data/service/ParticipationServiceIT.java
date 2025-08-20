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
import app.komunumo.ui.IntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ParticipationServiceIT extends IntegrationTest {

    private static final Pattern EXTRACT_CODE_FROM_BODY =
            Pattern.compile("\\bcode\\b[\\p{Punct}\\s]*\"(\\d{6})\"",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Autowired
    private @NotNull ParticipationService participationService;

    @Autowired
    private @NotNull EventService eventService;

    @Autowired
    private @NotNull UserService userService;

    @Test
    void requestVerificationCode() {
        final var event = eventService.getUpcomingEventsWithImage().getFirst().event();
        final var locale = Locale.ENGLISH;

        assertThat(participationService.requestVerificationCode(event, "", locale)).isFalse();
        assertThat(participationService.requestVerificationCode(event, "test@komunumo.app", locale)).isTrue();

        await().atMost(2, SECONDS).untilAsserted(() -> {
            final var receivedMessage = greenMail.getReceivedMessages()[0];
            assertThat(receivedMessage.getAllRecipients()[0]).hasToString("test@komunumo.app");
            assertThat(receivedMessage.getSubject()).isEqualTo("Confirm your event join request");

            final var body = getBody(receivedMessage);
            assertThat(body)
                    .doesNotContain("${eventTitle}")
                    .doesNotContain("${verificationCode}")
                    .doesNotContain("${verificationLink}")
                    .contains(event.title());

            final var code = extractCodeFromBody(body);
            verifyGoodCode(code);
        });
    }

    private @Nullable String extractCodeFromBody(final @NotNull String body) {
        final var m = EXTRACT_CODE_FROM_BODY.matcher(body);
        return m.find() ? m.group(1) : null;
    }

    private void verifyGoodCode(final @Nullable String code) {
        assertThat(code).isNotNull();

        // Verify that the code is INVALID for the given email
        assertThat(participationService.verifyCode("fail@komunumo.app", code)).isFalse();

        // Verify that the code is valid for the given email
        assertThat(participationService.verifyCode("test@komunumo.app", code)).isTrue();

        // Verify that the code is INVALID for the given email after it has been used
        assertThat(participationService.verifyCode("test@komunumo.app", code)).isFalse();
    }

    @Test
    void verifyBadCode() {
        assertThat(participationService.verifyCode("", "")).isFalse();
        assertThat(participationService.verifyCode("test@komunumo.app", "")).isFalse();
        assertThat(participationService.verifyCode("", "123456")).isFalse();
        assertThat(participationService.verifyCode("test@komunumo.app", "123456")).isFalse();
    }

    @Test
    void joinEventExistingUser() {
        assertThat(participationService.getParticipations()).isEmpty();

        final var email = "test@komunumo.app";
        assertThat(userService.getUserByEmail(email)).isEmpty();

        userService.storeUser(new UserDto(null, null, null, null, email, "", "",
                null, UserRole.USER, UserType.LOCAL, null));
        final var user = userService.getUserByEmail(email).orElseThrow();
        assertThat(user).isNotNull();

        final var event = eventService.getUpcomingEventsWithImage().getFirst().event();
        assertThat(event).isNotNull();

        final var locale = Locale.ENGLISH;
        assertThat(participationService.joinEvent(event, email, locale)).isTrue();

        final var participations = participationService.getParticipations();
        assertThat(participations).hasSize(1);

        final var participation = participations.getFirst();
        assertThat(participation).isNotNull().satisfies(testee -> {
            assertThat(testee.eventId()).isEqualTo(event.id());
            assertThat(testee.userId()).isEqualTo(user.id());
            assertThat(testee.registered()).isNotNull();
            assertThat(testee.registered()).isBeforeOrEqualTo(ZonedDateTime.now());
        });

        participationService.deleteParticipation(participation);
        assertThat(participationService.getParticipations()).isEmpty();
    }

}
