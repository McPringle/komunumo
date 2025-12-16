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
package app.komunumo.domain.participant.control;

import app.komunumo.data.db.tables.records.ParticipantRecord;
import app.komunumo.domain.core.confirmation.control.ConfirmationHandler;
import app.komunumo.domain.core.confirmation.control.ConfirmationService;
import app.komunumo.domain.core.confirmation.entity.ConfirmationContext;
import app.komunumo.domain.core.confirmation.entity.ConfirmationRequest;
import app.komunumo.domain.core.confirmation.entity.ConfirmationResponse;
import app.komunumo.domain.core.confirmation.entity.ConfirmationStatus;
import app.komunumo.domain.core.i18n.controller.TranslationProvider;
import app.komunumo.domain.core.mail.control.MailService;
import app.komunumo.domain.core.mail.entity.MailFormat;
import app.komunumo.domain.core.mail.entity.MailTemplateId;
import app.komunumo.domain.event.entity.EventDto;
import app.komunumo.domain.participant.entity.ParticipantDto;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.domain.user.entity.UserDto;
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
import java.util.UUID;

import static app.komunumo.data.db.tables.Participant.PARTICIPANT;

@Service
public final class ParticipantService {

    @VisibleForTesting
    static final @NotNull String CONTEXT_KEY_EVENT = "event";

    private final @NotNull DSLContext dsl;
    private final @NotNull MailService mailService;
    private final @NotNull UserService userService;
    private final @NotNull ConfirmationService confirmationService;
    private final @NotNull TranslationProvider translationProvider;

    public ParticipantService(final @NotNull DSLContext dsl,
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
                "participant.control.ParticipantService.actionText", locale, event.title());
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
        final var participant = getParticipant(event, user) // try to get existing participant
                .orElseGet(() -> new ParticipantDto(event.id(), user.id(), null));
        storeParticipant(participant);

        final var eventTitle = event.title();
        final var eventLink = LinkUtil.getLink(event);

        final Map<String, String> mailVariables = Map.of("eventTitle", eventTitle, "eventLink", eventLink);
        mailService.sendMail(MailTemplateId.EVENT_REGISTRATION_SUCCESS, locale, MailFormat.MARKDOWN,
                mailVariables, email);

        final var status = ConfirmationStatus.SUCCESS;
        final var message = translationProvider.getTranslation("participant.control.ParticipantService.successMessage",
                locale, eventTitle);
        return new ConfirmationResponse(status, message, eventLink);
    }

    public void storeParticipant(final @NotNull ParticipantDto participant) {
        final ParticipantRecord participantRecord = dsl.fetchOptional(PARTICIPANT,
                        PARTICIPANT.EVENT_ID.eq(participant.eventId())
                                .and(PARTICIPANT.USER_ID.eq(participant.userId())))
                .orElse(dsl.newRecord(PARTICIPANT));
        participantRecord.from(participant);

        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        if (participantRecord.getRegistered() == null) { // NOSONAR (false positive)
            participantRecord.setRegistered(now);
        }
        participantRecord.store();
    }

    public @NotNull List<@NotNull ParticipantDto> getAllParticipants() {
        return dsl.selectFrom(PARTICIPANT)
                .fetchInto(ParticipantDto.class);
    }

    public @NotNull Optional<ParticipantDto> getParticipant(final @NotNull EventDto event,
                                                            final @NotNull UserDto user) {
        return dsl.selectFrom(PARTICIPANT)
                .where(PARTICIPANT.EVENT_ID.eq(event.id())
                        .and(PARTICIPANT.USER_ID.eq(user.id())))
                .fetchOptionalInto(ParticipantDto.class);
    }

    public boolean deleteParticipant(final @NotNull ParticipantDto participant) {
        return dsl.delete(PARTICIPANT)
                .where(PARTICIPANT.EVENT_ID.eq(participant.eventId())
                        .and(PARTICIPANT.USER_ID.eq(participant.userId())))
                .execute() > 0;
    }

    /**
     * <p>Counts the total number of participants.</p>
     *
     * @return The total count of participants; never negative.
     */
    public int getParticipantCount() {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(PARTICIPANT)
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    /**
     * <p>Counts the total number of participants of the specified event.</p>
     *
     * @return The total count of participants of the event; never negative.
     */
    public int getParticipantCount(final @NotNull UUID eventId) {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(PARTICIPANT)
                        .where(PARTICIPANT.EVENT_ID.eq(eventId))
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }
}
