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

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.CommunityRecord;
import org.komunumo.data.dto.CommunityDto;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.komunumo.data.service.getter.UniqueIdGetter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Community.COMMUNITY;

interface CommunityService extends DSLContextGetter, UniqueIdGetter {

    default @NotNull CommunityDto storeCommunity(final @NotNull CommunityDto community) {
        final CommunityRecord communityRecord = dsl().fetchOptional(COMMUNITY, COMMUNITY.ID.eq(community.id()))
                .orElse(dsl().newRecord(COMMUNITY));
        communityRecord.from(community);
        if (communityRecord.getId() == null) {
            communityRecord.setId(getUniqueID(COMMUNITY));
        }
        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        if (communityRecord.getCreated() == null) {
            communityRecord.setCreated(now);
            communityRecord.setUpdated(now);
        } else {
            communityRecord.setUpdated(now);
        }
        communityRecord.store();
        return communityRecord.into(CommunityDto.class);
    }

    default @NotNull Optional<@NotNull CommunityDto> getCommunity(final @NotNull UUID id) {
        return dsl().selectFrom(COMMUNITY)
                .where(COMMUNITY.ID.eq(id))
                .fetchOptionalInto(CommunityDto.class);
    }

    default @NotNull Stream<@NotNull CommunityDto> getCommunities() {
        return dsl().selectFrom(COMMUNITY)
                .orderBy(COMMUNITY.NAME)
                .fetchStreamInto(CommunityDto.class);
    }

    default int getCommunityCount() {
        return Optional.ofNullable(
                dsl()
                        .selectCount()
                        .from(COMMUNITY)
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    default boolean deleteCommunity(final @NotNull CommunityDto community) {
        return dsl().delete(COMMUNITY)
                .where(COMMUNITY.ID.eq(community.id()))
                .execute() > 0;
    }

}
