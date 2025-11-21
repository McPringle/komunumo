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

import app.komunumo.business.community.entity.CommunityDto;
import app.komunumo.business.core.mail.control.MailService;
import app.komunumo.business.user.control.UserService;
import app.komunumo.business.core.mail.entity.MailFormat;
import app.komunumo.business.core.mail.entity.MailTemplateId;
import app.komunumo.data.dto.MemberDto;
import app.komunumo.data.dto.MemberRole;
import app.komunumo.business.user.entity.UserDto;
import app.komunumo.business.core.confirmation.entity.ConfirmationContext;
import app.komunumo.business.core.confirmation.control.ConfirmationHandler;
import app.komunumo.business.core.confirmation.entity.ConfirmationRequest;
import app.komunumo.business.core.confirmation.entity.ConfirmationResponse;
import app.komunumo.business.core.confirmation.control.ConfirmationService;
import app.komunumo.business.core.confirmation.entity.ConfirmationStatus;
import app.komunumo.business.core.i18n.controller.TranslationProvider;
import app.komunumo.util.LinkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

import static app.komunumo.data.db.Tables.MEMBER;

@Service
public final class MemberService {

    @VisibleForTesting
    static final @NotNull String CONTEXT_KEY_COMMUNITY = "community";

    private final @NotNull DSLContext dsl;
    private final @NotNull MailService mailService;
    private final @NotNull UserService userService;
    private final @NotNull ConfirmationService confirmationService;
    private final @NotNull TranslationProvider translationProvider;

    public MemberService(final @NotNull DSLContext dsl,
                         final @NotNull MailService mailService,
                         final @NotNull UserService userService,
                         final @NotNull ConfirmationService confirmationService,
                         final @NotNull TranslationProvider translationProvider) {
        this.dsl = dsl;
        this.mailService = mailService;
        this.userService = userService;
        this.confirmationService = confirmationService;
        this.translationProvider = translationProvider;
    }

    /**
     * <p>Stores/Updates the Member record to the database.</p>
     *
     * @param memberDto a DTO representation of the Member information
     * @return the persisted Member information in DTO form
     */
    public @NotNull MemberDto storeMember(final @NotNull MemberDto memberDto) {
        final var memberRecord = dsl.fetchOptional(MEMBER,
                        MEMBER.USER_ID.eq(memberDto.userId())
                                .and(MEMBER.COMMUNITY_ID.eq(memberDto.communityId())))
                .orElse(dsl.newRecord(MEMBER));

        memberRecord.setUserId(memberDto.userId());
        memberRecord.setCommunityId(memberDto.communityId());
        memberRecord.setRole(memberDto.role().name());

        if (memberRecord.getSince() == null && memberDto.since() != null) {
            memberRecord.setSince(memberDto.since());
        } else if (memberRecord.getSince() == null) {
            memberRecord.setSince(ZonedDateTime.now(ZoneOffset.UTC));
        }

        memberRecord.store();

        return memberRecord.into(MemberDto.class);
    }

    public @NotNull List<@NotNull MemberDto> getMembers() {
        return dsl.selectFrom(MEMBER)
                .fetchInto(MemberDto.class);
    }

    public Optional<MemberDto> getMember(final @NotNull UserDto user,
                                         final @NotNull CommunityDto community) {
        return dsl.selectFrom(MEMBER)
                .where(MEMBER.USER_ID.eq(user.id())
                        .and(MEMBER.COMMUNITY_ID.eq(community.id())))
                .fetchOptionalInto(MemberDto.class);
    }

    public @NotNull List<@NotNull MemberDto> getMembersByCommunityId(final @NotNull UUID communityId) {
        return dsl.selectFrom(MEMBER)
                .where(MEMBER.COMMUNITY_ID.eq(communityId))
                .fetchInto(MemberDto.class);
    }

    public @NotNull List<@NotNull MemberDto> getMembersByCommunityId(final @NotNull UUID communityId,
                                                                     final @NotNull MemberRole role) {
        return dsl.selectFrom(MEMBER)
                .where(MEMBER.COMMUNITY_ID.eq(communityId)
                        .and(MEMBER.ROLE.eq(role.name())))
                .orderBy(MEMBER.SINCE.desc())
                .fetchInto(MemberDto.class);
    }

    public int getMemberCount(final @Nullable UUID communityId) {
        return dsl.fetchCount(MEMBER, MEMBER.COMMUNITY_ID.eq(communityId));
    }

    public void joinCommunityStartConfirmationProcess(final @NotNull CommunityDto community,
                                                      final @NotNull Locale locale) {
        final var actionMessage = translationProvider.getTranslation(
                "data.service.MemberService.join.actionText", locale, community.name());
        final ConfirmationHandler actionHandler = this::joinCommunityWithEmail;
        final var actionContext = ConfirmationContext.of(CONTEXT_KEY_COMMUNITY, community);
        final var confirmationRequest = new ConfirmationRequest(
                actionMessage,
                actionHandler,
                actionContext,
                locale
        );
        confirmationService.startConfirmationProcess(confirmationRequest);
    }

    private @NotNull ConfirmationResponse joinCommunityWithEmail(final @NotNull String email,
                                                                 final @NotNull ConfirmationContext context,
                                                                 final @NotNull Locale locale) {
        final var community = (CommunityDto) context.get(CONTEXT_KEY_COMMUNITY);
        final var user = userService.getUserByEmail(email)
                .orElseGet(() -> userService.createAnonymousUserWithEmail(email));

        joinCommunityWithUser(user, community, locale);

        final var communityName = community.name();
        final var communityLink = LinkUtil.getLink(community);
        final var status = ConfirmationStatus.SUCCESS;
        final var message = translationProvider.getTranslation("data.service.MemberService.join.successMessage",
                locale, communityName);
        return new ConfirmationResponse(status, message, communityLink);
    }

    public void joinCommunityWithUser(final @NotNull UserDto user,
                                      final @NotNull CommunityDto community,
                                      final @NotNull Locale locale) {
        //noinspection DataFlowIssue // community and user objects are from the DB and are guaranteed to have an ID
        final var member = getMember(user, community) // try to get existing member
                .orElseGet(() -> new MemberDto(user.id(), community.id(), MemberRole.MEMBER, null));
        storeMember(member);

        final var communityName = community.name();
        final var communityLink = LinkUtil.getLink(community);

        final Map<String, String> mailVariables = Map.of(
                "communityName", communityName,
                "communityLink", communityLink,
                "memberCount", Integer.toString(getMemberCount(community.id())));
        if (user.email() != null) {
            mailService.sendMail(MailTemplateId.COMMUNITY_JOIN_SUCCESS_MEMBER, locale, MailFormat.MARKDOWN,
                    mailVariables, user.email());
        } else {
            // User has no email address; cannot send mail
            // Support for remote users without email address could be added here in the future
            throw new UnsupportedOperationException("Cannot send community join mail to user without email address.");
        }

        //noinspection DataFlowIssue // community object is from the DB and guaranteed to have an ID
        getMembersByCommunityId(community.id(), MemberRole.OWNER)
                .forEach(communityOwner -> {
                    //noinspection DataFlowIssue // community owners are always local users with email address set
                    userService.getUserById(communityOwner.userId()).ifPresent(owner ->
                            mailService.sendMail(MailTemplateId.COMMUNITY_JOIN_SUCCESS_OWNER, locale, MailFormat.MARKDOWN,
                                    mailVariables, owner.email()));
                });
    }

    public boolean deleteMember(final @NotNull MemberDto communityOwner) {
        return dsl.delete(MEMBER)
                .where(MEMBER.USER_ID.eq(communityOwner.userId())
                        .and(MEMBER.COMMUNITY_ID.eq(communityOwner.communityId())))
                .execute() > 0;
    }

}
