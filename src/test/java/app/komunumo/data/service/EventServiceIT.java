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
import app.komunumo.data.dto.EventWithImageDto;
import app.komunumo.ui.KaribuTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventServiceIT extends KaribuTest {

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

    @Test
    void getUpcomingEventsWithImages() {
        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        final var upcomingEvents = eventService.getUpcomingEventsWithImage();
        assertThat(upcomingEvents).hasSize(3);
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::title)
                .containsExactly("Demo Event 3", "Demo Event 5", "Demo Event 6");
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::end)
                .allSatisfy(endDate -> assertThat(endDate).isAfter(now));
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::visibility)
                .allSatisfy(visibility -> assertThat(visibility).isEqualTo(EventVisibility.PUBLIC));
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::status)
                .allSatisfy(status -> assertThat(status).isIn(EventStatus.PUBLISHED, EventStatus.CANCELED));
    }

    @Test
    void getUpcomingEventsWithImagesFilteredByCommunityWithNull() {
        final var upcomingEvents = eventService.getUpcomingEventsWithImage(null);
        assertThat(upcomingEvents).hasSize(3);
    }

    @Test
    void getUpcomingEventsWithImagesFilteredByCommunityWithNoEventShown() {
        final var community = communityService.getCommunities().getFirst();
        final var upcomingEvents = eventService.getUpcomingEventsWithImage(community);
        assertThat(upcomingEvents).isEmpty();
    }

    @Test
    void getUpcomingEventsWithImagesFilteredByCommunityWithOneEventShown() {
        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        final var community = communityService.getCommunities().getLast();
        final var upcomingEvents = eventService.getUpcomingEventsWithImage(community);
        assertThat(upcomingEvents).hasSize(1);
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::title)
                .containsExactly("Demo Event 6");
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::end)
                .allSatisfy(endDate -> assertThat(endDate).isAfter(now));
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::visibility)
                .allSatisfy(visibility -> assertThat(visibility).isEqualTo(EventVisibility.PUBLIC));
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::status)
                .allSatisfy(status -> assertThat(status).isIn(EventStatus.PUBLISHED, EventStatus.CANCELED));
    }

    @Test
    void getEventWithFallbackImage() {
        final var communityWithImage = communityService.getCommunities().stream()
                .filter(community -> community.imageId() != null)
                .findFirst()
                .orElseThrow();
        final var beginDate = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1);
        final var endDate = beginDate.plusHours(1);
        final var testEvent = eventService.storeEvent(
                new EventDto(null, communityWithImage.id(), null, null,
                "Test Event", "This is a test event.", "Test", beginDate, endDate,
                null, EventVisibility.PUBLIC, EventStatus.PUBLISHED));
        assertThat(testEvent.id()).isNotNull();
        try {
            final var testEventWithImage = eventService.getEventWithImage(testEvent.id()).orElseThrow();
            assertThat(testEventWithImage.event().imageId()).isNull();

            final var image = testEventWithImage.image();
            assertThat(image).isNotNull();
            assertThat(image.id()).isEqualTo(communityWithImage.imageId());
        } finally {
            eventService.deleteEvent(testEvent);
        }
    }

    @Test
    void getUpcomingEventsWithFallbackImage() {
        final var communityWithImage = communityService.getCommunities().stream()
                .filter(community -> community.imageId() != null)
                .findFirst()
                .orElseThrow();
        final var beginDate = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1);
        final var endDate = beginDate.plusHours(1);
        final var testEvent = eventService.storeEvent(
                new EventDto(null, communityWithImage.id(), null, null,
                "Test Event", "This is a test event.", "Test", beginDate, endDate,
                null, EventVisibility.PUBLIC, EventStatus.PUBLISHED));
        assertThat(testEvent.id()).isNotNull();
        try {
            final var testEvents = eventService.getUpcomingEventsWithImage(communityWithImage);
            final var testEventWithImage = testEvents.stream()
                    .filter(eventWithImage -> testEvent.id().equals(eventWithImage.event().id()))
                    .findAny()
                    .orElseThrow();
            assertThat(testEventWithImage.event().imageId()).isNull();

            final var image = testEventWithImage.image();
            assertThat(image).isNotNull();
            assertThat(image.id()).isEqualTo(communityWithImage.imageId());
        } finally {
            eventService.deleteEvent(testEvent);
        }
    }

}
