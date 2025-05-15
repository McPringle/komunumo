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
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.db.Tables;
import org.komunumo.data.db.tables.records.ImageRecord;
import org.komunumo.data.dto.ImageDto;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.komunumo.data.service.getter.UniqueIdGetter;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.selectOne;
import static org.komunumo.data.db.tables.Community.COMMUNITY;
import static org.komunumo.data.db.tables.Event.EVENT;
import static org.komunumo.data.db.tables.Image.IMAGE;
import static org.komunumo.data.db.tables.User.USER;

@Service
interface ImageService extends DSLContextGetter, UniqueIdGetter {

    @NotNull
    default ImageDto storeImage(final @NotNull  ImageDto image) {
        final ImageRecord imageRecord = dsl()
                .fetchOptional(Tables.IMAGE, Tables.IMAGE.ID.eq(image.id()))
                .orElse(dsl().newRecord(Tables.IMAGE));
        imageRecord.from(image);
        if (imageRecord.getId() == null) {
            imageRecord.setId(getUniqueID(Tables.IMAGE));
        }
        imageRecord.store();
        return imageRecord.into(ImageDto.class);
    }

    @NotNull
    default Optional<ImageDto> getImage(final @Nullable UUID id) {
        return id == null ? Optional.empty() : dsl()
                .selectFrom(IMAGE)
                .where(IMAGE.ID.eq(id))
                .fetchOptionalInto(ImageDto.class);
    }

    default int getImageCount() {
        return Optional.ofNullable(
                dsl()
                        .selectCount()
                        .from(IMAGE)
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    default @NotNull Stream<ImageDto> findOrphanedImages() {
        return dsl()
                .selectFrom(IMAGE)
                .whereNotExists(
                        selectOne()
                                .from(COMMUNITY)
                                .where(COMMUNITY.IMAGE_ID.eq(IMAGE.ID))
                )
                .andNotExists(
                        selectOne()
                                .from(EVENT)
                                .where(EVENT.IMAGE_ID.eq(IMAGE.ID))
                )
                .andNotExists(
                        selectOne()
                                .from(USER)
                                .where(USER.IMAGE_ID.eq(IMAGE.ID))
                )
                .fetchStreamInto(ImageDto.class);
    }

    default boolean deleteImage(final @NotNull  ImageDto image) {
        return dsl().delete(Tables.IMAGE)
                .where(Tables.IMAGE.ID.eq(image.id()))
                .execute() > 0;
    }
}
