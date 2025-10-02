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
package app.komunumo.servlets.images;

import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.EventStatus;
import app.komunumo.data.dto.EventVisibility;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.EventService;
import app.komunumo.ui.IntegrationTest;
import app.komunumo.util.ImageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventImageIT extends IntegrationTest {

    @Autowired EventService eventService;
    @Autowired CommunityService communityService;

    @Test
    void testEventFallsBackToCommunityImage() {
        // create community with image
        var community = communityService.storeCommunity(
            new CommunityDto(null, "@c1", null, null, "TestC", "desc",
                             null)
        );

        // create event without image
        var event = eventService.storeEvent(
            new EventDto(null, community.id(), null, null,
                         "Event1", "desc", "Online", null, null, null,
                         EventVisibility.PUBLIC, EventStatus.PUBLISHED)
        );

        // fetch fresh community
        var freshCommunity = communityService.getCommunity(community.id()).orElseThrow();

        // resolve images
        String eventImageUrl = ImageUtil.resolveImageUrl(
            new ImageDto(event.imageId(), null) // event has no imageId, pass null for content type
        );
        String communityImageUrl = ImageUtil.resolveImageUrl(
            new ImageDto(freshCommunity.imageId(), null) // only UUID is available here
        );

        if (event.imageId() == null) {
            // event falls back to community image
            assertThat(eventImageUrl).isEqualTo(communityImageUrl);
        } else {
            // event has its own image
            assertThat(eventImageUrl).isNotNull();
        }
    }
}
