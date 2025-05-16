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
package org.komunumo.data.service;

import com.icegreen.greenmail.util.GreenMailUtil;
import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.komunumo.data.dto.MailFormat;
import org.komunumo.data.dto.MailTemplateId;
import org.komunumo.ui.KaribuTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class MailServiceTest extends KaribuTestBase {

    @Autowired
    private @NotNull MailService mailService;

    @Test
    void getMailTemplateEnglish() {
        final var locale = Locale.ENGLISH;
        final var mailTemplate = mailService.getMailTemplate(MailTemplateId.USER_LOGIN_CONFIRMATION, locale);
        assertThat(mailTemplate).isNotEmpty();
        assertThat(mailTemplate.orElseThrow().subject()).isEqualTo("Your Login Request at Komunumo");
    }

    @Test
    void getMailTemplateGerman() {
        final var locale = Locale.GERMAN;
        final var mailTemplate = mailService.getMailTemplate(MailTemplateId.USER_LOGIN_CONFIRMATION, locale);
        assertThat(mailTemplate).isNotEmpty();
        assertThat(mailTemplate.orElseThrow().subject()).isEqualTo("Deine Anmeldung bei Komunumo");
    }

    @Test
    void getMailTemplateSwissGerman() {
        final var locale = Locale.forLanguageTag("de-CH");
        final var mailTemplate = mailService.getMailTemplate(MailTemplateId.USER_LOGIN_CONFIRMATION, locale);
        assertThat(mailTemplate).isNotEmpty();
        assertThat(mailTemplate.orElseThrow().subject()).isEqualTo("Deine Anmeldung bei Komunumo");
    }

    @Test
    void getMailTemplateItalianFails() {
        final var locale = Locale.ITALIAN;
        final var mailTemplate = mailService.getMailTemplate(MailTemplateId.USER_LOGIN_CONFIRMATION, locale);
        assertThat(mailTemplate).isEmpty();
    }

    @Test
    void sendMailSuccessWithoutVariables() {
        final var result = mailService.sendMail(
                MailTemplateId.USER_LOGIN_CONFIRMATION, Locale.ENGLISH, MailFormat.MARKDOWN,
                null, "test@komunumo.org");
        assertThat(result).isTrue();
        await().atMost(2, SECONDS).untilAsserted(() -> {
            final var receivedMessage = greenMail.getReceivedMessages()[0];
            assertThat(receivedMessage.getContentType())
                    .isEqualTo("text/plain; charset=UTF-8");
            assertThat(receivedMessage.getFrom()[0])
                    .hasToString("sender@localhost");
            assertThat(receivedMessage.getReplyTo()[0])
                    .hasToString("reply@localhost");
            assertThat(receivedMessage.getSubject())
                    .isEqualTo("Your Login Request at Komunumo");
            assertThat(GreenMailUtil.getBody(receivedMessage))
                    .isEqualTo("Please click on the following link to log in to Komunumo:\r\n${login_link}");
            assertThat(receivedMessage.getAllRecipients())
                    .hasSize(1);
            assertThat(receivedMessage.getAllRecipients()[0])
                    .hasToString("test@komunumo.org");
        });
    }

    @Test
    void sendMailSuccessWithEmptyVariables() {
        final var result = mailService.sendMail(
                MailTemplateId.USER_LOGIN_CONFIRMATION, Locale.ENGLISH, MailFormat.MARKDOWN,
                Map.of(), "test@komunumo.org");
        assertThat(result).isTrue();
        await().atMost(2, SECONDS).untilAsserted(() -> {
            final var receivedMessage = greenMail.getReceivedMessages()[0];
            assertThat(receivedMessage.getContentType())
                    .isEqualTo("text/plain; charset=UTF-8");
            assertThat(receivedMessage.getFrom()[0])
                    .hasToString("sender@localhost");
            assertThat(receivedMessage.getReplyTo()[0])
                    .hasToString("reply@localhost");
            assertThat(receivedMessage.getSubject())
                    .isEqualTo("Your Login Request at Komunumo");
            assertThat(GreenMailUtil.getBody(receivedMessage))
                    .isEqualTo("Please click on the following link to log in to Komunumo:\r\n${login_link}");
            assertThat(receivedMessage.getAllRecipients())
                    .hasSize(1);
            assertThat(receivedMessage.getAllRecipients()[0])
                    .hasToString("test@komunumo.org");
        });
    }

    @Test
    void sendMailSuccessWithVariables() {
        final var result = mailService.sendMail(
                MailTemplateId.USER_LOGIN_CONFIRMATION, Locale.ENGLISH, MailFormat.MARKDOWN,
                Map.of("login_link", "http://localhost/foobar"), "test@komunumo.org");
        assertThat(result).isTrue();
        await().atMost(2, SECONDS).untilAsserted(() -> {
            final var receivedMessage = greenMail.getReceivedMessages()[0];
            assertThat(receivedMessage.getContentType())
                    .isEqualTo("text/plain; charset=UTF-8");
            assertThat(receivedMessage.getFrom()[0])
                    .hasToString("sender@localhost");
            assertThat(receivedMessage.getReplyTo()[0])
                    .hasToString("reply@localhost");
            assertThat(receivedMessage.getSubject())
                    .isEqualTo("Your Login Request at Komunumo");
            assertThat(GreenMailUtil.getBody(receivedMessage))
                    .isEqualTo("Please click on the following link to log in to Komunumo:\r\n" +
                            "http://localhost/foobar");
            assertThat(receivedMessage.getAllRecipients())
                    .hasSize(1);
            assertThat(receivedMessage.getAllRecipients()[0])
                    .hasToString("test@komunumo.org");
        });
    }

    @Test
    void sendMailSuccessWithVariablesAsHtml() {
        final var result = mailService.sendMail(
                MailTemplateId.USER_LOGIN_CONFIRMATION, Locale.ENGLISH, MailFormat.HTML,
                Map.of("login_link", "http://localhost/foobar"), "test@komunumo.org");
        assertThat(result).isTrue();
        await().atMost(2, SECONDS).untilAsserted(() -> {
            final var receivedMessage = greenMail.getReceivedMessages()[0];
            assertThat(receivedMessage.getContentType())
                    .isEqualTo("text/html;charset=UTF-8");
            assertThat(receivedMessage.getFrom()[0])
                    .hasToString("sender@localhost");
            assertThat(receivedMessage.getReplyTo()[0])
                    .hasToString("reply@localhost");
            assertThat(receivedMessage.getSubject())
                    .isEqualTo("Your Login Request at Komunumo");
            assertThat(GreenMailUtil.getBody(receivedMessage))
                    .isEqualTo("<p>Please click on the following link to log in to Komunumo:<br />\r\n" +
                            "http://localhost/foobar</p>");
            assertThat(receivedMessage.getAllRecipients())
                    .hasSize(1);
            assertThat(receivedMessage.getAllRecipients()[0])
                    .hasToString("test@komunumo.org");
        });
    }

    @Test
    void sendMailWithErrorOnInvalidAddressFormat() {
        try (var logCaptor = LogCaptor.forClass(MailService.class)) {
            final var result = mailService.sendMail(
                    MailTemplateId.USER_LOGIN_CONFIRMATION, Locale.ENGLISH, MailFormat.MARKDOWN,
                    null, "@@@");
            assertThat(result).isFalse();
            assertThat(logCaptor.getErrorLogs()).containsExactly(
                    "Unable to send mail with subject 'Your Login Request at Komunumo' to [@@@]: Missing local name");
        }
    }

}
