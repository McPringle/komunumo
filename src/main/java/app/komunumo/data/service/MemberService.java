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

import app.komunumo.data.dto.MemberDto;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static app.komunumo.data.db.Tables.MEMBER;

@Service
public final class MemberService {

    private final @NotNull DSLContext dsl;

    public MemberService(final @NotNull DSLContext dsl) {
        this.dsl = dsl;
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
        memberRecord.setRole(memberDto.role());

        if (memberRecord.getSince() == null) {
            memberRecord.setSince(ZonedDateTime.now(ZoneOffset.UTC));
        }

        memberRecord.store();

        return memberRecord.into(MemberDto.class);
    }

    public @NotNull List<@NotNull MemberDto> getMembersByCommunityId(final @NotNull UUID communityId) {
        return dsl.selectFrom(MEMBER)
                .where(MEMBER.COMMUNITY_ID.eq(communityId))
                .fetchInto(MemberDto.class);
    }

}
