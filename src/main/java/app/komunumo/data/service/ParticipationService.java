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

import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.MailFormat;
import app.komunumo.data.dto.MailTemplateId;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public final class ParticipationService {

    private final @NotNull MailService mailService;

    public ParticipationService(final @NotNull MailService mailService) {
        this.mailService = mailService;
    }

    public boolean requestVerificationCode(final @NotNull EventDto event,
                                           final @NotNull String email,
                                           final @NotNull Locale locale) {
        final var eventTitle = event.title();
        final var verificationCode = UUID.randomUUID().toString();
        final var verificationLink = "LINK NOT IMPLEMENTED YET";
        final Map<String, String> mailVariables = Map.of(
                "eventTitle", eventTitle,
                "verificationCode", verificationCode,
                "verificationLink", verificationLink);
        return mailService.sendMail(MailTemplateId.JOIN_EVENT_VERIFICATION_CODE, locale, MailFormat.MARKDOWN,
                mailVariables, email);
    }

}
