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
import app.komunumo.util.CodeUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

@Service
public final class ParticipationService {

    private final @NotNull MailService mailService;
    private final @NotNull Cache<@NotNull String, @NotNull String> verificationCodeCache;

    public ParticipationService(final @NotNull MailService mailService) {
        this.mailService = mailService;
        this.verificationCodeCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(15))
                .maximumSize(1_000) // prevent memory overflow (DDOS attack)
                .build();
    }

    public boolean requestVerificationCode(final @NotNull EventDto event,
                                           final @NotNull String email,
                                           final @NotNull Locale locale) {
        final var eventTitle = event.title();
        final var verificationCode = generateVerificationCode(email);
        final var verificationLink = "LINK NOT IMPLEMENTED YET";
        final Map<String, String> mailVariables = Map.of(
                "eventTitle", eventTitle,
                "verificationCode", verificationCode,
                "verificationLink", verificationLink);
        return mailService.sendMail(MailTemplateId.JOIN_EVENT_VERIFICATION_CODE, locale, MailFormat.MARKDOWN,
                mailVariables, email);
    }

    private @NotNull String generateVerificationCode(final @NotNull String email) {
        final var code = CodeUtil.nextCode();
        verificationCodeCache.put(email, code);
        return code;
    }

    public boolean verifyCode(final @NotNull String email, final @NotNull String code) {
        final var cachedCode = verificationCodeCache.getIfPresent(email);
        if (CodeUtil.normalizeInput(code).equals(cachedCode)) {
            verificationCodeCache.invalidate(email);
            return true;
        }
        return false;
    }
}
