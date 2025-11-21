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
package app.komunumo.domain.core.image.control;

import app.komunumo.data.db.Tables;
import app.komunumo.data.db.tables.records.ImageRecord;
import app.komunumo.domain.core.image.entity.ImageDto;
import app.komunumo.data.generator.UniqueIdGenerator;
import app.komunumo.util.ImageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.komunumo.data.db.tables.Community.COMMUNITY;
import static app.komunumo.data.db.tables.Event.EVENT;
import static app.komunumo.data.db.tables.Image.IMAGE;
import static app.komunumo.data.db.tables.User.USER;
import static org.jooq.impl.DSL.selectOne;

@Service
public final class ImageService {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private final @NotNull DSLContext dsl;
    private final @NotNull UniqueIdGenerator idGenerator;

    public ImageService(final @NotNull DSLContext dsl,
                        final @NotNull UniqueIdGenerator idGenerator) {
        super();
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    public @NotNull ImageDto storeImage(final @NotNull ImageDto image) {
        final ImageRecord imageRecord = dsl
                .fetchOptional(Tables.IMAGE, Tables.IMAGE.ID.eq(image.id()))
                .orElse(dsl.newRecord(Tables.IMAGE));
        imageRecord.from(image);
        if (imageRecord.getId() == null) { // NOSONAR (false positive: ID may be null for new images)
            imageRecord.setId(idGenerator.getUniqueID(Tables.IMAGE));
        }
        imageRecord.store();
        return imageRecord.into(ImageDto.class);
    }

    public @NotNull Optional<ImageDto> getImage(final @Nullable UUID id) {
        return id == null ? Optional.empty() : dsl
                .selectFrom(IMAGE)
                .where(IMAGE.ID.eq(id))
                .fetchOptionalInto(ImageDto.class);
    }

    public List<ImageDto> getImages() {
        return dsl.selectFrom(IMAGE)
                .fetchInto(ImageDto.class);
    }

    public int getImageCount() {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(IMAGE)
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    public @NotNull List<@NotNull ImageDto> findOrphanedImages() {
        return dsl.selectFrom(IMAGE)
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
                .fetchInto(ImageDto.class);
    }

    public void cleanupOrphanedImages() {
        LOGGER.info("Cleaning up orphaned images...");
        findOrphanedImages().forEach(this::deleteImage);
        ImageUtil.cleanupOrphanedImageFiles(this);
        LOGGER.info("Orphaned images cleaned.");
    }

    public List<UUID> getAllImageIds() {
        return dsl.select(IMAGE.ID)
                .from(IMAGE)
                .stream()
                .map(r -> r.get(IMAGE.ID))
                .toList();
    }

    public boolean deleteImage(final @NotNull ImageDto image) {
        final var path = ImageUtil.resolveImagePath(image);
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (final IOException e) {
                LOGGER.error("Failed to delete image file: {}", path.toAbsolutePath(), e);
            }
        }

        return dsl.delete(Tables.IMAGE)
                .where(Tables.IMAGE.ID.eq(image.id()))
                .execute() > 0;
    }

}
