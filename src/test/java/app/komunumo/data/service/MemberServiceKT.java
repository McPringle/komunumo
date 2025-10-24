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
import app.komunumo.data.dto.UserRole;
import app.komunumo.ui.KaribuTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author smzoha
 * @since 20/10/25
 **/
public class MemberServiceKT extends KaribuTest {

    @Autowired
    private @NotNull MemberService memberService;

    @Autowired
    private @NotNull CommunityService communityService;

    @Test
    @SuppressWarnings("DuplicateExpressions")
    void testStoreAndUpdateMember() {
        final var communityList = communityService.getCommunities();
        final var community = communityList.getFirst();

        final var user = getTestUser(UserRole.USER);
        final var role = "OWNER";

        assertThat(user.id()).isNotNull();
        assertThat(community.id()).isNotNull();
        assertThat(memberService.getMembersByCommunityId(community.id())).isEmpty();

        var member = new MemberDto(user.id(), community.id(), role, null);
        member = memberService.storeMember(member);

        assertThat(memberService.getMembersByCommunityId(community.id())).hasSize(1);
        assertThat(member).isNotNull().satisfies(testMemberDto -> {
            assertThat(testMemberDto.userId()).isNotNull();
            assertThat(testMemberDto.userId()).isEqualTo(user.id());

            assertThat(testMemberDto.communityId()).isNotNull();
            assertThat(testMemberDto.communityId()).isEqualTo(community.id());

            assertThat(testMemberDto.role()).isNotNull();
            assertThat(testMemberDto.role()).isNotBlank();
            assertThat(testMemberDto.role()).isEqualTo(role);

            assertThat(testMemberDto.since()).isNotNull();
            assertThat(testMemberDto.since()).isBeforeOrEqualTo(ZonedDateTime.now());
        });

        member = memberService.getMembersByCommunityId(community.id()).getFirst();
        final var since = member.since();

        member = new MemberDto(user.id(), community.id(), role, null);
        member = memberService.storeMember(member);

        assertThat(memberService.getMembersByCommunityId(community.id())).hasSize(1);
        assertThat(member).isNotNull().satisfies(testMemberDto -> {
            assertThat(testMemberDto.userId()).isNotNull();
            assertThat(testMemberDto.userId()).isEqualTo(user.id());

            assertThat(testMemberDto.communityId()).isNotNull();
            assertThat(testMemberDto.communityId()).isEqualTo(community.id());

            assertThat(testMemberDto.role()).isNotNull();
            assertThat(testMemberDto.role()).isNotBlank();
            assertThat(testMemberDto.role()).isEqualTo(role);

            assertThat(testMemberDto.since()).isNotNull();
            assertThat(testMemberDto.since()).isEqualTo(since);
        });

    }
}
