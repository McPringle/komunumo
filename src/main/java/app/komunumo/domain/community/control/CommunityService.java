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
package app.komunumo.domain.community.control;

import app.komunumo.data.db.Tables;
import app.komunumo.data.db.tables.records.CommunityRecord;
import app.komunumo.domain.community.entity.CommunityDto;
import app.komunumo.domain.community.entity.CommunityWithImageDto;
import app.komunumo.domain.core.image.entity.ImageDto;
import app.komunumo.domain.user.entity.UserDto;
import app.komunumo.jooq.UniqueIdGenerator;
import app.komunumo.jooq.StorageService;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.komunumo.data.db.Tables.MEMBER;
import static app.komunumo.data.db.tables.Community.COMMUNITY;
import static app.komunumo.data.db.tables.Image.IMAGE;
import static app.komunumo.domain.member.entity.MemberRole.ORGANIZER;
import static app.komunumo.domain.member.entity.MemberRole.OWNER;

@Service
public final class CommunityService extends StorageService {

    private final @NotNull DSLContext dsl;

    public CommunityService(final @NotNull DSLContext dsl,
                            final @NotNull UniqueIdGenerator idGenerator) {
        super(idGenerator);
        this.dsl = dsl;
    }

    public @NotNull CommunityDto storeCommunity(final @NotNull CommunityDto community) {
        final CommunityRecord communityRecord = dsl.fetchOptional(COMMUNITY, COMMUNITY.ID.eq(community.id()))
                .orElse(dsl.newRecord(COMMUNITY));
        createOrUpdate(COMMUNITY, community, communityRecord);
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

    public @NotNull List<@NotNull CommunityDto> getCommunitiesForOrganizer(final @NotNull UserDto user) {
        return dsl.select(COMMUNITY.fields())
                .from(COMMUNITY)
                .join(MEMBER).on(MEMBER.COMMUNITY_ID.eq(COMMUNITY.ID))
                .where(MEMBER.USER_ID.eq(user.id())
                        .and(MEMBER.ROLE.in(OWNER.name(), ORGANIZER.name())))
                .orderBy(COMMUNITY.NAME)
                .fetchInto(CommunityDto.class);
    }

    public int getCommunityCount() {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(COMMUNITY)
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    public boolean isProfileNameAvailable(final @NotNull String profile) {
        return dsl.fetchCount(COMMUNITY, COMMUNITY.PROFILE.eq(profile)) == 0;
    }

    public boolean deleteCommunity(final @NotNull CommunityDto community) {
        // Clean-up members that are associated with the Community
        dsl.delete(MEMBER)
                .where(MEMBER.COMMUNITY_ID.eq(community.id()))
                .execute();

        return dsl.delete(COMMUNITY)
                .where(COMMUNITY.ID.eq(community.id()))
                .execute() > 0;
    }

    public boolean canCreateNewEvents(final @NotNull UserDto user) {
        return !getCommunitiesForOrganizer(user).isEmpty();
    }
}
