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

import app.komunumo.data.db.Tables;
import app.komunumo.data.db.tables.records.CommunityRecord;
import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.dto.CommunityWithImageDto;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.generator.UniqueIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.komunumo.data.db.tables.Community.COMMUNITY;
import static app.komunumo.data.db.tables.Image.IMAGE;

@Service
public final class CommunityService {

    private final @NotNull DSLContext dsl;
    private final @NotNull UniqueIdGenerator idGenerator;

    public CommunityService(final @NotNull DSLContext dsl,
                            final @NotNull UniqueIdGenerator idGenerator) {
        super();
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    public @NotNull CommunityDto storeCommunity(final @NotNull CommunityDto community) {
        final CommunityRecord communityRecord = dsl.fetchOptional(COMMUNITY, COMMUNITY.ID.eq(community.id()))
                .orElse(dsl.newRecord(COMMUNITY));
        communityRecord.from(community);
        if (communityRecord.getId() == null) { // NOSONAR (false positive: ID may be null for new communities)
            communityRecord.setId(idGenerator.getUniqueID(COMMUNITY));
        }
        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        if (communityRecord.getCreated() == null) { // NOSONAR (false positive: date may be null for new communities)
            communityRecord.setCreated(now);
            communityRecord.setUpdated(now);
        } else {
            communityRecord.setUpdated(now);
        }
        communityRecord.store();
        return communityRecord.into(CommunityDto.class);
    }

    public @NotNull Optional<CommunityDto> getCommunity(final @NotNull UUID id) {
        return dsl.selectFrom(COMMUNITY)
                .where(COMMUNITY.ID.eq(id))
                .fetchOptionalInto(CommunityDto.class);
    }

    public @NotNull Optional<CommunityWithImageDto> getCommunityWithImage(final @NotNull String profile) {
        return dsl.select()
                .from(Tables.COMMUNITY)
                .leftJoin(IMAGE).on(Tables.COMMUNITY.IMAGE_ID.eq(IMAGE.ID))
                .where(COMMUNITY.PROFILE.eq(profile))
                .fetchOptional()
                .map(rec -> new CommunityWithImageDto(
                        rec.into(COMMUNITY).into(CommunityDto.class),
                        rec.get(IMAGE.ID) != null ? rec.into(IMAGE).into(ImageDto.class) : null
                ));
    }

    public @NotNull List<@NotNull CommunityDto> getCommunities() {
        return dsl.selectFrom(COMMUNITY)
                .orderBy(COMMUNITY.NAME)
                .fetchInto(CommunityDto.class);
    }

    public @NotNull List<@NotNull CommunityWithImageDto> getCommunitiesWithImage() {
        return dsl.select()
                .from(COMMUNITY)
                .leftJoin(IMAGE).on(COMMUNITY.IMAGE_ID.eq(IMAGE.ID))
                .orderBy(COMMUNITY.NAME.asc())
                .fetch(rec -> new CommunityWithImageDto(
                        rec.into(COMMUNITY).into(CommunityDto.class),
                        rec.get(IMAGE.ID) != null ? rec.into(IMAGE).into(ImageDto.class) : null
                ));
    }

    public int getCommunityCount() {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(COMMUNITY)
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    public boolean deleteCommunity(final @NotNull CommunityDto community) {
        return dsl.delete(COMMUNITY)
                .where(COMMUNITY.ID.eq(community.id()))
                .execute() > 0;
    }

}
