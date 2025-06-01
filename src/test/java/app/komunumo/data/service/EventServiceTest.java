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

import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.EventStatus;
import app.komunumo.data.dto.EventVisibility;
import app.komunumo.ui.IntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class EventServiceTest extends IntegrationTest {

    @Autowired
    private @NotNull EventService eventService;

    @Autowired
    private @NotNull CommunityService communityService;

    @Test
    @SuppressWarnings("java:S5961")
    void happyCase() {
        // get a community from the database
        final var community = communityService.getCommunities().getFirst();
        final var communityId = community.id();

        // store a new event into the database
        var event = new EventDto(null, communityId, null, null,
                "Test Event Title", "", "", null, null,
                null, EventVisibility.PUBLIC, EventStatus.DRAFT);
        event = eventService.storeEvent(event);
        final var eventId = event.id();
        assertThat(eventId).isNotNull().satisfies(testee -> {
            assertThat(testee.toString()).isNotEmpty();
            assertThat(testee.toString()).isNotBlank();
        });

        assertThat(event).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(eventId);
            assertThat(testee.communityId()).isEqualTo(communityId);
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.title()).isEqualTo("Test Event Title");
            assertThat(testee.description()).isBlank();
            assertThat(testee.location()).isBlank();
            assertThat(testee.begin()).isNull();
            assertThat(testee.end()).isNull();
            assertThat(testee.imageId()).isNull();
            assertThat(testee.visibility()).isEqualTo(EventVisibility.PUBLIC);
            assertThat(testee.status()).isEqualTo(EventStatus.DRAFT);
        });

        // read the event from the database
        event = eventService.getEvent(eventId).orElseThrow();
        assertThat(event).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(eventId);
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.title()).isEqualTo("Test Event Title");
            assertThat(testee.description()).isBlank();
            assertThat(testee.location()).isBlank();
            assertThat(testee.begin()).isNull();
            assertThat(testee.end()).isNull();
            assertThat(testee.imageId()).isNull();
            assertThat(testee.visibility()).isEqualTo(EventVisibility.PUBLIC);
            assertThat(testee.status()).isEqualTo(EventStatus.DRAFT);
        });

        // read all events from the database
        final var events = eventService.getEvents();
        assertThat(events).contains(event);

        // update the existing event
        event = new EventDto(event.id(), event.communityId(), event.created(), event.updated(),
                "Test Event Modified", event.description(), event.location(), event.begin(), event.end(),
                event.imageId(), event.visibility(), event.status());
        event = eventService.storeEvent(event);
        assertThat(event).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(eventId);
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isAfter(testee.created());
            assertThat(testee.title()).isEqualTo("Test Event Modified");
            assertThat(testee.description()).isBlank();
            assertThat(testee.location()).isBlank();
            assertThat(testee.begin()).isNull();
            assertThat(testee.end()).isNull();
            assertThat(testee.imageId()).isNull();
            assertThat(testee.visibility()).isEqualTo(EventVisibility.PUBLIC);
            assertThat(testee.status()).isEqualTo(EventStatus.DRAFT);
        });

        // delete the existing event
        assertThat(eventService.deleteEvent(event)).isTrue();
        assertThat(eventService.getEvent(eventId)).isEmpty();

        // delete the non-existing event (was already deleted before)
        assertThat(eventService.deleteEvent(event)).isFalse();
    }

}
