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
import app.komunumo.data.dto.MemberRole;
import app.komunumo.data.dto.UserDto;
import app.komunumo.data.dto.UserRole;
import app.komunumo.data.dto.UserType;
import app.komunumo.ui.KaribuTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MemberServiceKT extends KaribuTest {

    @Autowired
    private @NotNull MemberService memberService;

    @Autowired
    private @NotNull CommunityService communityService;
    @Autowired
    private UserService userService;

    @Test
    @SuppressWarnings("DuplicateExpressions")
    void testStoreUpdateDeleteMember() {
        final var communityList = communityService.getCommunities();
        final var community = communityList.getFirst();

        final var user = createRandomUser(UserRole.USER, UserType.LOCAL);
        final var role = MemberRole.OWNER;

        assertThat(user.id()).isNotNull();
        assertThat(community.id()).isNotNull();
        assertThat(memberService.getMembersByCommunityId(community.id())).hasSize(4);

        var member = new MemberDto(user.id(), community.id(), role, null);
        member = memberService.storeMember(member);

        assertThat(memberService.getMembersByCommunityId(community.id())).hasSize(5);
        assertThat(member).isNotNull().satisfies(testMemberDto -> {
            assertThat(testMemberDto.userId()).isNotNull();
            assertThat(testMemberDto.userId()).isEqualTo(user.id());

            assertThat(testMemberDto.communityId()).isNotNull();
            assertThat(testMemberDto.communityId()).isEqualTo(community.id());

            assertThat(testMemberDto.role()).isNotNull();
            assertThat(testMemberDto.role()).isEqualTo(role);

            assertThat(testMemberDto.since()).isNotNull();
            assertThat(testMemberDto.since()).isBeforeOrEqualTo(ZonedDateTime.now());
        });

        member = memberService.getMembersByCommunityId(community.id()).getFirst(); // order since desc
        final var since = member.since();

        member = new MemberDto(user.id(), community.id(), role, null);
        member = memberService.storeMember(member);

        assertThat(memberService.getMembersByCommunityId(community.id())).hasSize(5);
        assertThat(member).isNotNull().satisfies(testMemberDto -> {
            assertThat(testMemberDto.userId()).isNotNull();
            assertThat(testMemberDto.userId()).isEqualTo(user.id());

            assertThat(testMemberDto.communityId()).isNotNull();
            assertThat(testMemberDto.communityId()).isEqualTo(community.id());

            assertThat(testMemberDto.role()).isNotNull();
            assertThat(testMemberDto.role()).isEqualTo(role);

            assertThat(testMemberDto.since()).isNotNull();
            assertThat(testMemberDto.since()).isEqualTo(since);
        });

        assertThat(memberService.deleteMember(member)).isTrue();
        assertThat(memberService.deleteMember(member)).isFalse();
        assertThat(memberService.getMembersByCommunityId(community.id())).hasSize(4);
    }

    @Test
    void joinCommunityWithUserThrowsException() {
        final var communityList = communityService.getCommunities();
        final var community = communityList.getFirst();

        final var profile = "anon-" + System.currentTimeMillis() + "@example.com";
        final var user = userService.storeUser(new UserDto(null, null, null, profile, null,
                "Anonymous", "", null, UserRole.USER, UserType.REMOTE));

        assertThatThrownBy(() -> memberService.joinCommunityWithUser(user, community, Locale.ENGLISH))
                .isInstanceOf(UnsupportedOperationException.class);
    }

}
