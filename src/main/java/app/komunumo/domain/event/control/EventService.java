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
package app.komunumo.domain.event.control;

import app.komunumo.domain.event.entity.EventDto;
import app.komunumo.domain.event.entity.EventStatus;
import app.komunumo.domain.event.entity.EventVisibility;
import app.komunumo.domain.event.entity.EventWithImageDto;
import app.komunumo.data.db.tables.Image;
import app.komunumo.data.db.tables.records.EventRecord;
import app.komunumo.domain.community.entity.CommunityDto;
import app.komunumo.domain.core.image.entity.ContentType;
import app.komunumo.domain.core.image.entity.ImageDto;
import app.komunumo.data.generator.UniqueIdGenerator;
import app.komunumo.data.service.StorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.komunumo.data.db.tables.Community.COMMUNITY;
import static app.komunumo.data.db.tables.Event.EVENT;
import static app.komunumo.data.db.tables.Image.IMAGE;
import static org.jooq.impl.DSL.noCondition;

@Service
public final class EventService extends StorageService {

    private final @NotNull DSLContext dsl;

    public EventService(final @NotNull DSLContext dsl,
                        final @NotNull UniqueIdGenerator idGenerator) {
        super(idGenerator);
        this.dsl = dsl;
    }

    public @NotNull EventDto storeEvent(final @NotNull EventDto event) {
        final EventRecord eventRecord = dsl.fetchOptional(EVENT, EVENT.ID.eq(event.id()))
                .orElse(dsl.newRecord(EVENT));
        createOrUpdate(EVENT, event, eventRecord);
        return eventRecord.into(EventDto.class);
    }

    public @NotNull Optional<EventDto> getEvent(final @NotNull UUID id) {
        return dsl.selectFrom(EVENT)
                .where(EVENT.ID.eq(id))
                .fetchOptionalInto(EventDto.class);
    }

    public @NotNull Optional<EventWithImageDto> getEventWithImage(final @NotNull UUID id) {
        var communityImage = IMAGE.as("COMMUNITY_IMAGE");
        return dsl.select()
                .from(EVENT)
                .leftJoin(IMAGE).on(EVENT.IMAGE_ID.eq(IMAGE.ID))
                .leftJoin(COMMUNITY).on(EVENT.COMMUNITY_ID.eq(COMMUNITY.ID))
                .leftJoin(communityImage).on(COMMUNITY.IMAGE_ID.eq(communityImage.ID))
                .where(EVENT.ID.eq(id)
                        .and(EVENT.VISIBILITY.eq(EventVisibility.PUBLIC))
                        .and(EVENT.STATUS.in(EventStatus.PUBLISHED, EventStatus.CANCELED)))
                .fetchOptional(record -> mapRecordToEventWithImage(record, communityImage));
    }

    public @NotNull List<@NotNull EventDto> getEvents() {
        return dsl.selectFrom(EVENT)
                .fetchInto(EventDto.class);
    }

    public @NotNull List<@NotNull EventWithImageDto> getUpcomingEventsWithImage() {
        return getUpcomingEventsWithImage(null);
    }

    public @NotNull List<@NotNull EventWithImageDto> getUpcomingEventsWithImage(final @Nullable CommunityDto community) {
        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        var communityImage = IMAGE.as("COMMUNITY_IMAGE");
        return dsl.select()
                .from(EVENT)
                .leftJoin(IMAGE).on(EVENT.IMAGE_ID.eq(IMAGE.ID))
                .leftJoin(COMMUNITY).on(EVENT.COMMUNITY_ID.eq(COMMUNITY.ID))
                .leftJoin(communityImage).on(COMMUNITY.IMAGE_ID.eq(communityImage.ID))
                .where(
                        EVENT.BEGIN.isNotNull()
                                .and(EVENT.END.isNotNull())
                                .and(EVENT.END.gt(now))
                                .and(EVENT.VISIBILITY.eq(EventVisibility.PUBLIC))
                                .and(EVENT.STATUS.in(EventStatus.PUBLISHED, EventStatus.CANCELED))
                                .and(community != null ? EVENT.COMMUNITY_ID.eq(community.id()) : noCondition()))
                .orderBy(EVENT.BEGIN.asc())
                .fetch(record -> mapRecordToEventWithImage(record, communityImage));
    }

    public @NotNull List<@NotNull EventWithImageDto> getPastEventsWithImage() {
        return getPastEventsWithImage(null);
    }

    public @NotNull List<@NotNull EventWithImageDto> getPastEventsWithImage(final @Nullable CommunityDto community) {
        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        var communityImage = IMAGE.as("COMMUNITY_IMAGE");
        return dsl.select()
                .from(EVENT)
                .leftJoin(IMAGE).on(EVENT.IMAGE_ID.eq(IMAGE.ID))
                .leftJoin(COMMUNITY).on(EVENT.COMMUNITY_ID.eq(COMMUNITY.ID))
                .leftJoin(communityImage).on(COMMUNITY.IMAGE_ID.eq(communityImage.ID))
                .where(
                        EVENT.BEGIN.isNotNull()
                                .and(EVENT.END.isNotNull())
                                .and(EVENT.END.lt(now))
                                .and(EVENT.VISIBILITY.eq(EventVisibility.PUBLIC))
                                .and(EVENT.STATUS.in(EventStatus.PUBLISHED, EventStatus.CANCELED))
                                .and(community != null ? EVENT.COMMUNITY_ID.eq(community.id()) : noCondition()))
                .orderBy(EVENT.BEGIN.desc())
                .fetch(record -> mapRecordToEventWithImage(record, communityImage));
    }

    private @NotNull EventWithImageDto mapRecordToEventWithImage(final @NotNull Record record,
                                                                 final @NotNull Image communityImage) {
        final ImageDto image;
        if (record.get(IMAGE.ID) != null) {
            image = new ImageDto(
                    record.get(IMAGE.ID, UUID.class),
                    record.get(IMAGE.CONTENT_TYPE, ContentType.class)
            );
        } else if (record.get(communityImage.ID) != null) {
            image = new ImageDto(
                    record.get(communityImage.ID, UUID.class),
                    record.get(communityImage.CONTENT_TYPE, ContentType.class)
            );
        } else {
            image = null;
        }

        final var event = record.into(EVENT).into(EventDto.class);
        return new EventWithImageDto(event, image);
    }

    public int getEventCount() {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(EVENT)
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    public boolean deleteEvent(final @NotNull EventDto event) {
        return dsl.delete(EVENT)
                .where(EVENT.ID.eq(event.id()))
                .execute() > 0;
    }

}
