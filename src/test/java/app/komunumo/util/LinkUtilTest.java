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
package app.komunumo.util;

import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.EventStatus;
import app.komunumo.data.dto.EventVisibility;
import app.komunumo.data.service.ConfigurationService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LinkUtilTest {

    @AfterEach
    void resetLinkUtil() {
        LinkUtil.initialize(null);
    }

    @Test
    void getEventLink() {
        final var event = new EventDto(UUID.fromString("8232a4f1-3f02-4db3-bf78-18387734c81c"),
                UUID.fromString("e315c669-95ac-4231-96b6-55fb4c30e0e2"), null, null,
                "Test Event Title", "Test Event Description", "", null, null, null,
                EventVisibility.PUBLIC, EventStatus.PUBLISHED);
        final var link = LinkUtil.getLink(event);
        assertThat(link).isEqualTo("/events/8232a4f1-3f02-4db3-bf78-18387734c81c");
    }

    @Test
    void getCommunityLink() {
        final var community = new CommunityDto(null, "@test", null, null,
                "Test Community Name", "Test Community Description", null);
        final var link = LinkUtil.getLink(community);
        assertThat(link).isEqualTo("/communities/@test");
    }

    @Test
    void getEventLinkWithPrefix() {
        LinkUtil.initialize(getMockedConfigurationService());
        final var event = new EventDto(UUID.fromString("8232a4f1-3f02-4db3-bf78-18387734c81c"),
                UUID.fromString("e315c669-95ac-4231-96b6-55fb4c30e0e2"), null, null,
                "Test Event Title", "Test Event Description", "", null, null, null,
                EventVisibility.PUBLIC, EventStatus.PUBLISHED);
        final var link = LinkUtil.getLink(event);
        assertThat(link).isEqualTo("http://localhost:8080/events/8232a4f1-3f02-4db3-bf78-18387734c81c");
    }

    @Test
    void getCommunityLinkWithPrefix() {
        LinkUtil.initialize(getMockedConfigurationService());
        final var community = new CommunityDto(null, "@test", null, null,
                "Test Community Name", "Test Community Description", null);
        final var link = LinkUtil.getLink(community);
        assertThat(link).isEqualTo("http://localhost:8080/communities/@test");
    }

    private @NotNull ConfigurationService getMockedConfigurationService() {
        final var configurationService = mock(ConfigurationService.class);
        when(configurationService.getConfiguration(ConfigurationSetting.INSTANCE_URL))
                .thenReturn("http://localhost:8080/");
        return configurationService;
    }

}
