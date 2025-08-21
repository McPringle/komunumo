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

import app.komunumo.KomunumoException;
import app.komunumo.ui.IntegrationTest;
import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ConfirmationServiceIT extends IntegrationTest {

    private static final @NotNull Pattern EXTRACT_ID_PATTERN =
            Pattern.compile("http://localhost(?::\\d+)?/confirm/([0-9a-fA-F\\-]{36})");

    @Autowired
    private ConfirmationService confirmationService;

    @Test
    void confirmationProcess() {
        final var locale = Locale.ENGLISH;
        final var emailAddress = "test@example.com";
        final var confirmationReason = "Test Reason";
        final var onSuccessMessage = "Success Message";
        final var onSuccessHandlerCounter = new AtomicInteger(0);
        final Runnable onSuccessHandler = () -> {
            final var callCount = onSuccessHandlerCounter.incrementAndGet();
            if (callCount == 1) { // first call should throw an exception
                throw new KomunumoException("expected");
            }
        };

        confirmationService.startConfirmationProcess(
                emailAddress,
                confirmationReason,
                onSuccessMessage,
                onSuccessHandler,
                locale);

        await().atMost(2, SECONDS).untilAsserted(() -> {
            onSuccessHandlerCounter.set(0);
            final var receivedMessage = greenMail.getReceivedMessages()[0];

            assertThat(receivedMessage.getAllRecipients()[0])
                    .hasToString("test@example.com");

            assertThat(receivedMessage.getSubject())
                    .isEqualTo("[Your Instance Name] Please confirm your email address");

            final var body = getBody(receivedMessage);
            assertThat(body)
                    .doesNotContain("${instanceName}")
                    .doesNotContain("${confirmationReason}")
                    .doesNotContain("${confirmationLink}")
                    .contains("Test Reason");

            final var confirmationId = extractConfirmationId(body);
            assertThat(confirmationId).isNotNull();

            Optional<String> success;
            assertThat(onSuccessHandlerCounter.get()).isZero();

            try (var logCaptor = LogCaptor.forClass(ConfirmationService.class)) {
                success = confirmationService.confirm(confirmationId);
                assertThat(success).isEmpty();
                assertThat(onSuccessHandlerCounter.get()).isEqualTo(1);
                assertThat(logCaptor.getErrorLogs()).containsExactly(
                        "Error in 'onSuccessHandler' for confirmation ID " + confirmationId + ": expected");
            }

            success = confirmationService.confirm(confirmationId);
            assertThat(success).isNotEmpty().contains("Success Message");
            assertThat(onSuccessHandlerCounter.get()).isEqualTo(2);

            success = confirmationService.confirm(confirmationId);
            assertThat(success).isEmpty();
            assertThat(onSuccessHandlerCounter.get()).isEqualTo(2);
        });
    }

    private UUID extractConfirmationId(final @NotNull String body) {
        Matcher matcher = EXTRACT_ID_PATTERN.matcher(body);
        assertThat(matcher.find()).isTrue();
        return UUID.fromString(matcher.group(1));
    }
}
