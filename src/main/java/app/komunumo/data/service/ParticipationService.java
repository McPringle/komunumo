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

import app.komunumo.data.db.tables.records.ParticipationRecord;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.MailFormat;
import app.komunumo.data.dto.MailTemplateId;
import app.komunumo.data.dto.ParticipationDto;
import app.komunumo.data.dto.UserDto;
import app.komunumo.util.CodeUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static app.komunumo.data.db.tables.Participation.PARTICIPATION;

@Service
public final class ParticipationService {

    private final @NotNull DSLContext dsl;
    private final @NotNull MailService mailService;
    private final @NotNull UserService userService;

    private final @NotNull Cache<@NotNull String, @NotNull String> verificationCodeCache;

    public ParticipationService(final @NotNull DSLContext dsl,
                                final @NotNull MailService mailService,
                                final @NotNull UserService userService) {
        super();
        this.dsl = dsl;
        this.mailService = mailService;
        this.userService = userService;

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

    public boolean joinEvent(final @NotNull EventDto event,
                             final @NotNull String email,
                             final @NotNull Locale locale) {
        final var user = userService.getUserByEmail(email)
                .orElseGet(() -> userService.createAnonymousUserWithEmail(email));

        @SuppressWarnings("DataFlowIssue") // event and user objects are from the DB and are guaranteed to have an ID
        final var participation = getParticipation(event, user) // try to get existing participation
                .orElseGet(() -> new ParticipationDto(event.id(), user.id(), null));
        storeParticipation(participation);

        final var eventTitle = event.title();
        final Map<String, String> mailVariables = Map.of("eventTitle", eventTitle);
        return mailService.sendMail(MailTemplateId.JOIN_EVENT_SUCCESS, locale, MailFormat.MARKDOWN,
                mailVariables, email);
    }

    public @NotNull ParticipationDto storeParticipation(final @NotNull ParticipationDto participation) {
        final ParticipationRecord participationRecord = dsl.fetchOptional(PARTICIPATION,
                        PARTICIPATION.EVENT_ID.eq(participation.eventId())
                                .and(PARTICIPATION.USER_ID.eq(participation.userId())))
                .orElse(dsl.newRecord(PARTICIPATION));
        participationRecord.from(participation);

        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        if (participationRecord.getRegistered() == null) { // NOSONAR (false positive)
            participationRecord.setRegistered(now);
        }
        participationRecord.store();
        return participationRecord.into(ParticipationDto.class);
    }

    public @NotNull List<@NotNull ParticipationDto> getParticipations() {
        return dsl.selectFrom(PARTICIPATION)
                .fetchInto(ParticipationDto.class);
    }

    public @NotNull Optional<ParticipationDto> getParticipation(final @NotNull EventDto event,
                                                                final @NotNull UserDto user) {
        return dsl.selectFrom(PARTICIPATION)
                .where(PARTICIPATION.EVENT_ID.eq(event.id())
                        .and(PARTICIPATION.USER_ID.eq(user.id())))
                .fetchOptionalInto(ParticipationDto.class);
    }

    public boolean deleteParticipation(final @NotNull ParticipationDto participation) {
        return dsl.delete(PARTICIPATION)
                .where(PARTICIPATION.EVENT_ID.eq(participation.eventId())
                        .and(PARTICIPATION.USER_ID.eq(participation.userId())))
                .execute() > 0;
    }

}
