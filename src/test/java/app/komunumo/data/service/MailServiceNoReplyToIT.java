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

import app.komunumo.data.dto.MailFormat;
import app.komunumo.data.dto.MailTemplateId;
import app.komunumo.ui.KaribuTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.Locale;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestPropertySource(properties = {
        "komunumo.mail.replyTo="
})
class MailServiceNoReplyToIT extends KaribuTest {

    @Autowired
    private @NotNull MailService mailService;

    @Test
    void sendMailSuccessWithoutReplyTo() {
        final var result = mailService.sendMail(
                MailTemplateId.TEST, Locale.ENGLISH, MailFormat.MARKDOWN,
                null, "test@komunumo.app");
        assertThat(result).isTrue();
        await().atMost(2, SECONDS).untilAsserted(() -> {
            final var receivedMessage = getGreenMail().getReceivedMessages()[0];
            assertThat(receivedMessage.getFrom()[0])
                    .hasToString("sender@localhost");
            assertThat(receivedMessage.getReplyTo()[0])
                    .hasToString("sender@localhost");
            assertThat(receivedMessage.getAllRecipients()[0])
                    .hasToString("test@komunumo.app");
        });
    }

}
