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

import app.komunumo.business.event.entity.EventDto;
import app.komunumo.business.user.control.UserService;
import app.komunumo.data.db.tables.records.ParticipationRecord;
import app.komunumo.data.dto.MailFormat;
import app.komunumo.data.dto.MailTemplateId;
import app.komunumo.data.dto.ParticipationDto;
import app.komunumo.business.user.entity.UserDto;
import app.komunumo.data.service.confirmation.ConfirmationContext;
import app.komunumo.data.service.confirmation.ConfirmationHandler;
import app.komunumo.data.service.confirmation.ConfirmationRequest;
import app.komunumo.data.service.confirmation.ConfirmationResponse;
import app.komunumo.data.service.confirmation.ConfirmationService;
import app.komunumo.data.service.confirmation.ConfirmationStatus;
import app.komunumo.ui.TranslationProvider;
import app.komunumo.util.LinkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static app.komunumo.data.db.tables.Participation.PARTICIPATION;

@Service
public final class ParticipationService {

    @VisibleForTesting
    static final @NotNull String CONTEXT_KEY_EVENT = "event";

    private final @NotNull DSLContext dsl;
    private final @NotNull MailService mailService;
    private final @NotNull UserService userService;
    private final @NotNull ConfirmationService confirmationService;
    private final @NotNull TranslationProvider translationProvider;

    public ParticipationService(final @NotNull DSLContext dsl,
                                final @NotNull MailService mailService,
                                final @NotNull UserService userService,
                                final @NotNull ConfirmationService confirmationService,
                                final @NotNull TranslationProvider translationProvider) {
        super();
        this.dsl = dsl;
        this.mailService = mailService;
        this.userService = userService;
        this.confirmationService = confirmationService;
        this.translationProvider = translationProvider;
    }

    public void startConfirmationProcess(final @NotNull EventDto event,
                                         final @NotNull Locale locale) {
        final var actionMessage = translationProvider.getTranslation(
                "data.service.ParticipationService.actionText", locale, event.title());
        final ConfirmationHandler actionHandler = this::registerForEvent;
        final var actionContext = ConfirmationContext.of(CONTEXT_KEY_EVENT, event);
        final var confirmationRequest = new ConfirmationRequest(
                actionMessage,
                actionHandler,
                actionContext,
                locale
        );
        confirmationService.startConfirmationProcess(confirmationRequest);
    }

    @VisibleForTesting
    @NotNull ConfirmationResponse registerForEvent(final @NotNull String email,
                                                   final @NotNull ConfirmationContext context,
                                                   final @NotNull Locale locale) {
        final var event = (EventDto) context.get(CONTEXT_KEY_EVENT);
        final var user = userService.getUserByEmail(email)
                .orElseGet(() -> userService.createAnonymousUserWithEmail(email));

        @SuppressWarnings("DataFlowIssue") // event and user objects are from the DB and are guaranteed to have an ID
        final var participation = getParticipation(event, user) // try to get existing participation
                .orElseGet(() -> new ParticipationDto(event.id(), user.id(), null));
        storeParticipation(participation);

        final var eventTitle = event.title();
        final var eventLink = LinkUtil.getLink(event);

        final Map<String, String> mailVariables = Map.of("eventTitle", eventTitle, "eventLink", eventLink);
        mailService.sendMail(MailTemplateId.EVENT_REGISTRATION_SUCCESS, locale, MailFormat.MARKDOWN,
                mailVariables, email);

        final var status = ConfirmationStatus.SUCCESS;
        final var message = translationProvider.getTranslation("data.service.ParticipationService.successMessage",
                locale, eventTitle);
        return new ConfirmationResponse(status, message, eventLink);
    }

    public void storeParticipation(final @NotNull ParticipationDto participation) {
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
    }

    public @NotNull List<@NotNull ParticipationDto> getAllParticipations() {
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
