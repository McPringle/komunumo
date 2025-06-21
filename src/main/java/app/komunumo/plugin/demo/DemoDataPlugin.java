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
package app.komunumo.plugin.demo;

import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.EventStatus;
import app.komunumo.data.dto.EventVisibility;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.EventService;
import app.komunumo.data.service.ImageService;
import app.komunumo.plugin.KomunumoPlugin;
import app.komunumo.plugin.PluginContext;
import app.komunumo.util.ImageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public final class DemoDataPlugin implements KomunumoPlugin {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(DemoDataPlugin.class);

    /**
     * Initializes demo data after application startup.
     * <p>
     * This method is executed automatically once the Spring context is fully initialized.
     * It checks whether any communities exist in the database, and if none are found,
     * it inserts a predefined set of demo communities with predictable IDs.
     * </p>
     */
    @Override
    public void onApplicationStarted(final @NotNull PluginContext context) {
        LOGGER.info("Creating demo data...");

        final var imageService = context.getServiceProvider().imageService();
        final var communityService = context.getServiceProvider().communityService();
        final var eventService = context.getServiceProvider().eventService();

        final var images = createDemoImages(imageService);
        final var communities = createDemoCommunities(communityService, images);
        createDemoEvents(eventService, images, communities);

        LOGGER.info("Demo data created.");
    }

    private List<ImageDto> createDemoImages(final @NotNull ImageService imageService) {
        if (imageService.getImageCount() == 0) {
            for (int i = 1; i <= 5; i++) {
                final var filename = "demo-background-" + i + ".jpg";
                final var image = imageService.storeImage(new ImageDto(null, ContentType.IMAGE_JPEG, filename));
                storeDemoImage(image, filename);
            }
        }
        return imageService.getImages();
    }

    private List<CommunityDto> createDemoCommunities(final @NotNull CommunityService communityService,
                                       final @NotNull List<ImageDto> images) {
        if (communityService.getCommunityCount() == 0) {
            for (int i = 1; i <= 6; i++) {
                final var imageId = i <= 5 ? images.get(i - 1).id() : null; // demo community 6+ has no image
                communityService.storeCommunity(new CommunityDto(
                        null, "@demoCommunity" + i, null, null,
                        "Demo Community " + i, "This is a demo community.", imageId));
            }
        }
        return communityService.getCommunities();
    }

    private void createDemoEvents(final @NotNull EventService eventService,
                                  final @NotNull List<ImageDto> images,
                                  final @NotNull List<CommunityDto> communities) {
        if (eventService.getEventCount() == 0) {
            for (int i = 1; i <= 6; i++) {
                final var communityId = communities.get(i - 1).id();
                final var imageId = i <= 5 ? images.get(i - 1).id() : null; // demo community 6+ has no image
                final var beginDate = generateBeginDate(i);
                final var endDate = beginDate == null ? null : beginDate.plusMinutes(45);
                eventService.storeEvent(new EventDto(
                        null, communityId, null, null,
                        "Demo Event " + i, "This is a demo event.", "Online",
                        beginDate, endDate, imageId, EventVisibility.PUBLIC, EventStatus.PUBLISHED));
            }
        }
    }

    private @Nullable ZonedDateTime generateBeginDate(final int i) {
        final var now = ZonedDateTime.now(ZoneOffset.UTC)
                .withHour(18)
                .truncatedTo(ChronoUnit.HOURS);
        if (i <= 2) {
            return now.minusDays(i);
        } else if (i == 4) {
            return null;
        } else {
            return now.plusDays(i);
        }
    }

    @VisibleForTesting
    void storeDemoImage(final ImageDto image, final String filename) {
        // load resources from classpath
        try (InputStream inputStream = getClass().getResourceAsStream("/META-INF/resources/images/" + filename)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Demo image not found: " + filename);
            }

            // write them to a temporary file (for transfer to file-based API)
            final var tempFile = Files.createTempFile("demo-image-", ".jpg");
            tempFile.toFile().deleteOnExit();
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // finally: store the image
            ImageUtil.storeImage(image, tempFile);
        } catch (final IOException e) {
            LOGGER.warn(e.getMessage());
        }
    }

}
