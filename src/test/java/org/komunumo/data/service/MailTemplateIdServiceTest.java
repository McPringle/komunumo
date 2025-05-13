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

import org.junit.jupiter.api.Test;
import org.komunumo.data.dto.MailTemplateId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MailTemplateIdServiceTest {

    @Autowired
    private MailTemplateService mailTemplateService;

    @Test
    void getMailTemplateEnglish() {
        final var locale = Locale.ENGLISH;
        final var mailTemplate = mailTemplateService.getMailTemplate(MailTemplateId.USER_LOGIN_CONFIRMATION, locale);
        assertThat(mailTemplate).isNotNull();
        assertThat(mailTemplate.subject()).isEqualTo("Your Login Request at Komunumo");
    }

    @Test
    void getMailTemplateGerman() {
        final var locale = Locale.GERMAN;
        final var mailTemplate = mailTemplateService.getMailTemplate(MailTemplateId.USER_LOGIN_CONFIRMATION, locale);
        assertThat(mailTemplate).isNotNull();
        assertThat(mailTemplate.subject()).isEqualTo("Deine Anmeldung bei Komunumo");
    }

    @Test
    void getMailTemplateSwissGerman() {
        final var locale = Locale.forLanguageTag("de-CH");
        final var mailTemplate = mailTemplateService.getMailTemplate(MailTemplateId.USER_LOGIN_CONFIRMATION, locale);
        assertThat(mailTemplate).isNotNull();
        assertThat(mailTemplate.subject()).isEqualTo("Deine Anmeldung bei Komunumo");
    }

    @Test
    void getMailTemplateItalienFails() {
        final var locale = Locale.ITALIAN;
        final var mailTemplate = mailTemplateService.getMailTemplate(MailTemplateId.USER_LOGIN_CONFIRMATION, locale);
        assertThat(mailTemplate).isNull();
    }

}
