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
package app.komunumo.business.core.mail.control;

import app.komunumo.business.core.mail.entity.MailFormat;
import app.komunumo.business.core.mail.entity.MailTemplateId;
import app.komunumo.test.KaribuTest;
import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.MessagingException;
import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MailServiceKT extends KaribuTest {

    @Autowired
    private @NotNull MailService mailService;

    @Test
    void getMailTemplateEnglish() {
        final var locale = Locale.ENGLISH;
        final var mailTemplate = mailService.getMailTemplate(MailTemplateId.TEST, locale);
        assertThat(mailTemplate).isNotEmpty();
        assertThat(mailTemplate.orElseThrow().subject()).isEqualTo("Test mail");
    }

    @Test
    void getMailTemplateGerman() {
        final var locale = Locale.GERMAN;
        final var mailTemplate = mailService.getMailTemplate(MailTemplateId.TEST, locale);
        assertThat(mailTemplate).isNotEmpty();
        assertThat(mailTemplate.orElseThrow().subject()).isEqualTo("Testmail");
    }

    @Test
    void getMailTemplateSwissGerman() {
        final var locale = Locale.forLanguageTag("de-CH");
        final var mailTemplate = mailService.getMailTemplate(MailTemplateId.TEST, locale);
        assertThat(mailTemplate).isNotEmpty();
        assertThat(mailTemplate.orElseThrow().subject()).isEqualTo("Testmail");
    }

    @Test
    void getMailTemplateItalianFails() {
        final var locale = Locale.ITALIAN;
        final var mailTemplate = mailService.getMailTemplate(MailTemplateId.TEST, locale);
        assertThat(mailTemplate).isEmpty();
    }

    @Test
    void sendMailSuccessWithoutVariables() throws MessagingException {
        final var result = mailService.sendMail(
                MailTemplateId.TEST, Locale.ENGLISH, MailFormat.MARKDOWN,
                null, "test@komunumo.app");
        assertThat(result).isTrue();

        final var receivedMessage = getEmailBySubject("[Komunumo Test] Test mail");
        assertThat(receivedMessage.getContentType())
                .isEqualTo("text/plain; charset=UTF-8");
        assertThat(receivedMessage.getFrom()[0])
                .hasToString("sender@localhost");
        assertThat(receivedMessage.getReplyTo()[0])
                .hasToString("reply@localhost");
        assertThat(GreenMailUtil.getBody(receivedMessage))
                .isEqualTo("Hello,\r\n\r\nthis is a test mail from Komunumo Test.");
        assertThat(receivedMessage.getAllRecipients())
                .hasSize(1);
        assertThat(receivedMessage.getAllRecipients()[0])
                .hasToString("test@komunumo.app");
    }

    @Test
    void sendMailSuccessWithEmptyVariables() throws MessagingException {
        final var result = mailService.sendMail(
                MailTemplateId.TEST, Locale.ENGLISH, MailFormat.MARKDOWN,
                Map.of(), "test@komunumo.app");
        assertThat(result).isTrue();

        final var receivedMessage = getEmailBySubject("[Komunumo Test] Test mail");
        assertThat(receivedMessage.getContentType())
                .isEqualTo("text/plain; charset=UTF-8");
        assertThat(receivedMessage.getFrom()[0])
                .hasToString("sender@localhost");
        assertThat(receivedMessage.getReplyTo()[0])
                .hasToString("reply@localhost");
        assertThat(GreenMailUtil.getBody(receivedMessage))
                .isEqualTo("Hello,\r\n\r\nthis is a test mail from Komunumo Test.");
        assertThat(receivedMessage.getAllRecipients())
                .hasSize(1);
        assertThat(receivedMessage.getAllRecipients()[0])
                .hasToString("test@komunumo.app");
    }

    @Test
    void sendMailSuccessWithVariables() throws MessagingException {
        final var result = mailService.sendMail(
                MailTemplateId.TEST, Locale.ENGLISH, MailFormat.MARKDOWN,
                Map.of("password", "sEcReT"), "test@komunumo.app");
        assertThat(result).isTrue();

        final var receivedMessage = getEmailBySubject("[Komunumo Test] Test mail");
        assertThat(receivedMessage.getContentType())
                .isEqualTo("text/plain; charset=UTF-8");
        assertThat(receivedMessage.getFrom()[0])
                .hasToString("sender@localhost");
        assertThat(receivedMessage.getReplyTo()[0])
                .hasToString("reply@localhost");
        assertThat(GreenMailUtil.getBody(receivedMessage))
                .isEqualTo("Hello,\r\n\r\nthis is a test mail from Komunumo Test.");
        assertThat(receivedMessage.getAllRecipients())
                .hasSize(1);
        assertThat(receivedMessage.getAllRecipients()[0])
                .hasToString("test@komunumo.app");
    }

    @Test
    void sendMailSuccessWithVariablesAsHtml() throws MessagingException {
        final var result = mailService.sendMail(
                MailTemplateId.TEST, Locale.ENGLISH, MailFormat.HTML,
                Map.of("password", "sEcReT"), "test@komunumo.app");
        assertThat(result).isTrue();

        final var receivedMessage = getEmailBySubject("[Komunumo Test] Test mail");
        assertThat(receivedMessage.getContentType())
                .isEqualTo("text/html;charset=UTF-8");
        assertThat(receivedMessage.getFrom()[0])
                .hasToString("sender@localhost");
        assertThat(receivedMessage.getReplyTo()[0])
                .hasToString("reply@localhost");
        assertThat(GreenMailUtil.getBody(receivedMessage))
                .isEqualTo("<p>Hello,</p>\r\n<p>this is a test mail from Komunumo Test.</p>");
        assertThat(receivedMessage.getAllRecipients())
                .hasSize(1);
        assertThat(receivedMessage.getAllRecipients()[0])
                .hasToString("test@komunumo.app");
    }

    @Test
    void sendMailWithErrorOnInvalidAddressFormat() {
        try (var logCaptor = LogCaptor.forClass(MailService.class)) {
            final var result = mailService.sendMail(
                    MailTemplateId.TEST, Locale.ENGLISH, MailFormat.MARKDOWN,
                    null, "@@@");
            assertThat(result).isFalse();
            assertThat(logCaptor.getErrorLogs()).containsExactly(
                    "Unable to send mail with subject '[Komunumo Test] Test mail' to [@@@]: Missing local name");
        }
    }

}
