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
import app.komunumo.data.service.interfaces.ConfirmationHandler;
import app.komunumo.ui.IntegrationTest;
import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static app.komunumo.data.service.ConfirmationContext.CONTEXT_KEY_EMAIL;
import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ConfirmationServiceIT extends IntegrationTest {

    private static final @NotNull Pattern EXTRACT_ID_PATTERN =
            Pattern.compile("http://localhost(?::\\d+)?/confirm\\?id=([0-9a-fA-F\\-]{36})(?:&.*)?");

    @Autowired
    private ConfirmationService confirmationService;

    @Test
    void confirmationProcess() {
        final var locale = Locale.ENGLISH;
        final var emailAddress = "test@example.com";
        final var confirmationReason = "Test Reason";
        final var onSuccessMessage = "Success Message";
        final var onFailMessage = "Failed Message";
        final var confirmationHandlerCounter = new AtomicInteger(0);
        final ConfirmationHandler confirmationHandler = confirmationContext -> {
            final var callCount = confirmationHandlerCounter.incrementAndGet();
            if (callCount == 1) { // first call should throw an exception
                throw new KomunumoException("expected");
            }
            return true;
        };

        final var confirmationContext = ConfirmationContext.of(CONTEXT_KEY_EMAIL, emailAddress);
        confirmationService.startConfirmationProcess(
                confirmationReason,
                onSuccessMessage,
                onFailMessage,
                confirmationHandler,
                locale,
                confirmationContext);

        await().atMost(2, SECONDS).untilAsserted(() -> {
            confirmationHandlerCounter.set(0);
            final var receivedMessage = greenMail.getReceivedMessages()[0];

            assertThat(receivedMessage.getAllRecipients()[0])
                    .hasToString("test@example.com");

            assertThat(receivedMessage.getSubject())
                    .isEqualTo("[Your Instance Name] Please confirm your email address");

            final var body = getBody(receivedMessage);
            assertThat(body)
                    .doesNotContain("${instanceName}")

                    .doesNotContain("${confirmationTimeout}")
                    .doesNotContain("${confirmationReason}")
                    .doesNotContain("${confirmationLink}")
                    .contains("Test Reason");

            final var confirmationId = extractConfirmationId(body);
            assertThat(confirmationId).isNotNull();

            Optional<String> success;
            assertThat(confirmationHandlerCounter.get()).isZero();

            try (var logCaptor = LogCaptor.forClass(ConfirmationService.class)) {
                success = confirmationService.confirm(confirmationId);
                assertThat(success).isEmpty();
                assertThat(confirmationHandlerCounter.get()).isEqualTo(1);
                assertThat(logCaptor.getErrorLogs()).containsExactly(
                        "Error in 'confirmationHandler' for confirmation ID " + confirmationId + ": expected");
            }

            success = confirmationService.confirm(confirmationId);
            assertThat(success).isNotEmpty().contains("Success Message");
            assertThat(confirmationHandlerCounter.get()).isEqualTo(2);

            success = confirmationService.confirm(confirmationId);
            assertThat(success).isEmpty();
            assertThat(confirmationHandlerCounter.get()).isEqualTo(2);
        });
    }

    private String extractConfirmationId(final @NotNull String body) {
        Matcher matcher = EXTRACT_ID_PATTERN.matcher(body);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    @Test
    void confirmationTimeout_is5Minutes() {
        final var timeout = confirmationService.getConfirmationTimeout();
        assertThat(timeout).isNotNull();
        assertThat(timeout.toMinutes()).isEqualTo(5);
    }

    @Test
    void confirmationTimeoutText_inEnglish() {
        final var timeout = confirmationService.getConfirmationTimeoutText(Locale.ENGLISH);
        assertThat(timeout).isNotNull().isEqualTo("5 minutes");
    }

    @Test
    void confirmationTimeoutText_inGerman() {
        final var timeout = confirmationService.getConfirmationTimeoutText(Locale.GERMAN);
        assertThat(timeout).isNotNull().isEqualTo("5 Minuten");
    }

    @Test
    void confirmationTimeoutText_inFallbackLanguage() {
        final var timeout = confirmationService.getConfirmationTimeoutText(null);
        assertThat(timeout).isNotNull().isEqualTo("5 minutes");
    }

}
