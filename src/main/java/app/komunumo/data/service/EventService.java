package app.komunumo.data.service;

import app.komunumo.data.db.tables.records.EventRecord;
import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.generator.UniqueIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static app.komunumo.data.db.tables.Community.COMMUNITY;
import static app.komunumo.data.db.tables.Event.EVENT;

@Service
public class EventService {

    private final @NotNull DSLContext dsl;
    private final @NotNull UniqueIdGenerator idGenerator;

    public EventService(final @NotNull DSLContext dsl,
                        final @NotNull UniqueIdGenerator idGenerator) {
        super();
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    public @NotNull EventDto storeEvent(final @NotNull EventDto event) {
        final EventRecord eventRecord = dsl.fetchOptional(EVENT, EVENT.ID.eq(event.id()))
                .orElse(dsl.newRecord(EVENT));
        eventRecord.from(event);

        if (eventRecord.getId() == null) {
            eventRecord.setId(idGenerator.getUniqueID(EVENT));
        }

        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        if (eventRecord.getCreated() == null) {
            eventRecord.setCreated(now);
            eventRecord.setUpdated(now);
        } else {
            eventRecord.setUpdated(now);
        }
        eventRecord.store();
        return eventRecord.into(EventDto.class);
    }


    public @NotNull Stream<@NotNull EventDto> getEvents() {
        return dsl.selectFrom(EVENT)
                .fetchStreamInto(EventDto.class);
    }
}
