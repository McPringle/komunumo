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
package app.komunumo.data.demo;

import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.EventStatus;
import app.komunumo.data.dto.EventVisibility;
import app.komunumo.data.dto.GlobalPageDto;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.EventService;
import app.komunumo.data.service.GlobalPageService;
import app.komunumo.data.service.ImageService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.security.SystemAuthenticator;
import app.komunumo.util.ImageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public final class DemoDataCreator {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(DemoDataCreator.class);

    private final @NotNull SystemAuthenticator systemAuthenticator;
    private final @NotNull ServiceProvider serviceProvider;
    private final boolean enabled;
    private final @NotNull String demoDataUrl;

    public DemoDataCreator(final @NotNull SystemAuthenticator systemAuthenticator,
                           final @NotNull ServiceProvider serviceProvider) {
        this.systemAuthenticator = systemAuthenticator;
        this.serviceProvider = serviceProvider;

        final var demoConfig = serviceProvider.getAppConfig().demo();
        this.enabled = demoConfig.enabled();
        this.demoDataUrl = demoConfig.json();
    }

    @Scheduled(cron = "0 0 * * * *")
    public void resetDemoData() {
        systemAuthenticator.runAsAdmin(this::resetDemoDataAsAdmin);
    }

    private void resetDemoDataAsAdmin() {
        if (!enabled) {
            LOGGER.info("Demo data plugin is disabled, skipping demo data reset.");
            return;
        }

        final var configurationService = serviceProvider.configurationService();
        final var imageService = serviceProvider.imageService();
        final var communityService = serviceProvider.communityService();
        final var eventService = serviceProvider.eventService();
        final var participationService = serviceProvider.participationService();
        final var globalPageService = serviceProvider.globalPageService();

        LOGGER.info("Deleting existing data...");
        configurationService.deleteAllConfigurations();
        participationService.getParticipations().forEach(participationService::deleteParticipation);
        eventService.getEvents().forEach(eventService::deleteEvent);
        communityService.getCommunities().forEach(communityService::deleteCommunity);
        imageService.getImages().forEach(imageService::deleteImage);
        globalPageService.getAllGlobalPages().forEach(globalPageService::deleteGlobalPage);
        LOGGER.info("Existing data deleted.");

        LOGGER.info("Creating demo data...");
        if (demoDataUrl.isBlank()) {
            final var communityImages = createDemoImages(imageService, "community");
            final var communities = createDemoCommunities(communityService, communityImages);
            final var eventImages = createDemoImages(imageService, "event");
            createDemoEvents(eventService, eventImages, communities);
            createGlobalPages(globalPageService);
        } else {
            final var demoDataImporter = new DemoDataImporter(demoDataUrl);
            demoDataImporter.importSettings(configurationService);
            demoDataImporter.importImages(imageService);
            demoDataImporter.importCommunities(communityService);
            demoDataImporter.importEvents(eventService);
            demoDataImporter.importGlobalPages(globalPageService);
            LOGGER.info("Demo data created.");
        }

        LOGGER.info("Cleaning up orphaned image files...");
        ImageUtil.cleanupOrphanedImageFiles(imageService);
        LOGGER.info("Orphaned image files cleaned up.");
    }

    private List<ImageDto> createDemoImages(final @NotNull ImageService imageService, final @NotNull String filenamePrefix) {
        final List<ImageDto> images = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final var filename = "%s-%d.webp".formatted(filenamePrefix, i);
            final var image = imageService.storeImage(new ImageDto(null, ContentType.IMAGE_WEBP));
            storeDemoImage(image, filename);
            images.add(image);
        }
        return images;
    }

    private List<CommunityDto> createDemoCommunities(final @NotNull CommunityService communityService,
                                       final @NotNull List<ImageDto> images) {
        for (int i = 1; i <= 6; i++) {
            final var imageId = i <= 5 ? images.get(i - 1).id() : null; // demo community 6+ has no image
            communityService.storeCommunity(new CommunityDto(
                    null, "@demoCommunity" + i, null, null,
                    "Demo Community " + i, "This is a demo community.", imageId));
        }
        return communityService.getCommunities();
    }

    private void createDemoEvents(final @NotNull EventService eventService,
                                  final @NotNull List<ImageDto> images,
                                  final @NotNull List<CommunityDto> communities) {
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
    void storeDemoImage(final @NotNull ImageDto image, final @NotNull String filename) {
        // load resources from classpath
        try (InputStream inputStream = getClass().getResourceAsStream("/META-INF/resources/images/demo/" + filename)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Demo image not found: " + filename);
            }

            // write them to a temporary file (for transfer to file-based API)
            final var tempFile = Files.createTempFile("demo-image-", ".webp");
            tempFile.toFile().deleteOnExit();
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // finally: store the image
            ImageUtil.storeImage(image, tempFile);
        } catch (final IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private void createGlobalPages(final @NotNull GlobalPageService globalPageService) {
        globalPageService.storeGlobalPage(new GlobalPageDto("imprint", Locale.ENGLISH, null, null,
                "Legal Notice", "## Legal Notice\n\nThis is a demo for testing purposes **only**."));
        globalPageService.storeGlobalPage(new GlobalPageDto("imprint", Locale.GERMAN, null, null,
                "Impressum", "## Impressum\n\nDies ist eine Demo **nur** für Testzwecke."));
    }

}
