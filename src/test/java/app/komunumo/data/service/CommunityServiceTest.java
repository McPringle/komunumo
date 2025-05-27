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

import app.komunumo.data.dto.CommunityDto;
import app.komunumo.ui.IntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityServiceTest extends IntegrationTest {

    @Autowired
    private @NotNull CommunityService communityService;

    @Test
    @SuppressWarnings("java:S5961")
    void happyCase() {
        // store a new community into the database
        var community = new CommunityDto(null, "@test", null, null,
                "Test Community Name", "Test Community Description", null);
        community = communityService.storeCommunity(community);
        final var communityId = community.id();
        assertThat(communityId).isNotNull().satisfies(testee -> {
            assertThat(testee.toString()).isNotEmpty();
            assertThat(testee.toString()).isNotBlank();
        });

        assertThat(community).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(communityId);
            assertThat(testee.profile()).isEqualTo("@test");
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.name()).isEqualTo("Test Community Name");
            assertThat(testee.description()).isEqualTo("Test Community Description");
            assertThat(testee.imageId()).isNull();
        });

        // read the community from the database
        community = communityService.getCommunity(communityId).orElseThrow();
        assertThat(community).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(communityId);
            assertThat(testee.profile()).isEqualTo("@test");
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.name()).isEqualTo("Test Community Name");
            assertThat(testee.description()).isEqualTo("Test Community Description");
            assertThat(testee.imageId()).isNull();
        });

        // read all communities from the database
        final var communities = communityService.getCommunities();
        assertThat(communities).contains(community);

        // update the existing community
        community = new CommunityDto(community.id(), community.profile(), community.created(), community.updated(),
                "Test Community Modified", community.description(), community.imageId());
        community = communityService.storeCommunity(community);
        assertThat(community).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(communityId);
            assertThat(testee.profile()).isEqualTo("@test");
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isAfter(testee.created());
            assertThat(testee.name()).isEqualTo("Test Community Modified");
            assertThat(testee.description()).isEqualTo("Test Community Description");
            assertThat(testee.imageId()).isNull();
        });

        // delete the existing community
        assertThat(communityService.deleteCommunity(community)).isTrue();
        assertThat(communityService.getCommunity(communityId)).isEmpty();

        // delete the non-existing community (was already deleted before)
        assertThat(communityService.deleteCommunity(community)).isFalse();
    }

}
