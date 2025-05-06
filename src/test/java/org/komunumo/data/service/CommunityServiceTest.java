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
package org.komunumo.data.service;

import org.junit.jupiter.api.Test;
import org.komunumo.data.dto.CommunityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CommunityServiceTest {

    @Autowired
    private CommunityService communityService;

    @Test
    @SuppressWarnings("java:S5961")
    void happyCase() {
        // store a new community into the database
        var community = new CommunityDto(null, "@test", null, null,
                "Test Community Name", "Test Community Description", null);
        community = communityService.storeCommunity(community);
        final var communityId = community.id();

        assertNotNull(communityId);
        assertEquals("@test", community.profile());

        assertNotNull(community.created());
        assertNotNull(community.updated());
        assertEquals(community.created(), community.updated());

        assertEquals("Test Community Name", community.name());
        assertEquals("Test Community Description", community.description());
        assertNull(community.imageId());

        // read the community from the database
        community = communityService.getCommunity(communityId).orElseThrow();
        assertEquals(communityId, community.id());
        assertEquals("@test", community.profile());

        assertNotNull(community.created());
        assertNotNull(community.updated());
        assertEquals(community.created(), community.updated());

        assertEquals("Test Community Name", community.name());
        assertEquals("Test Community Description", community.description());
        assertNull(community.imageId());

        // read all communities from the database
        final var communities = communityService.getCommunities().toList();
        assertThat(communities).contains(community);

        // update the existing community
        community = new CommunityDto(community.id(), community.profile(), community.created(), community.updated(),
                "Test Community Modified", community.description(), community.imageId());
        community = communityService.storeCommunity(community);
        assertEquals(communityId, community.id());
        assertEquals("@test", community.profile());

        assertNotNull(community.created());
        assertNotNull(community.updated());
        assertTrue(community.updated().isAfter(community.created()));

        assertEquals("Test Community Modified", community.name());
        assertEquals("Test Community Description", community.description());
        assertNull(community.imageId());

        // delete the existing community
        assertTrue(communityService.deleteCommunity(community));
        assertTrue(communityService.getCommunity(communityId).isEmpty());

        // delete the non-existing community (was already deleted before)
        assertFalse(communityService.deleteCommunity(community));
    }

}
