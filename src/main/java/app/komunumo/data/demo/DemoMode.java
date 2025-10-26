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

import app.komunumo.configuration.AppConfig;
import app.komunumo.data.importer.JSONImporter;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.EventService;
import app.komunumo.data.service.GlobalPageService;
import app.komunumo.data.service.ImageService;
import app.komunumo.data.service.ParticipationService;
import app.komunumo.data.service.UserService;
import app.komunumo.util.ImageUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public final class DemoMode {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(DemoMode.class);

    private final @NotNull ConfigurationService configurationService;
    private final @NotNull UserService userService;
    private final @NotNull ImageService imageService;
    private final @NotNull CommunityService communityService;
    private final @NotNull EventService eventService;
    private final @NotNull ParticipationService participationService;
    private final @NotNull GlobalPageService globalPageService;

    private final boolean enabled;
    private final @NotNull String jsonDataUrl;

    @SuppressWarnings("checkstyle:ParameterNumber") // constructor injection
    public DemoMode(final @NotNull AppConfig appConfig,
                    final @NotNull ConfigurationService configurationService,
                    final @NotNull UserService userService,
                    final @NotNull ImageService imageService,
                    final @NotNull CommunityService communityService,
                    final @NotNull EventService eventService,
                    final @NotNull ParticipationService participationService,
                    final @NotNull GlobalPageService globalPageService) {
        this.configurationService = configurationService;
        this.userService = userService;
        this.imageService = imageService;
        this.communityService = communityService;
        this.eventService = eventService;
        this.participationService = participationService;
        this.globalPageService = globalPageService;

        final var demoConfig = appConfig.demo();
        this.enabled = demoConfig.enabled();
        this.jsonDataUrl = demoConfig.json();
    }

    @Scheduled(cron = "0 0 * * * *")
    public void resetDemoData() {
        if (!enabled) {
            LOGGER.info("Demo mode plugin is disabled, skipping demo data reset.");
            return;
        }

        if (jsonDataUrl.isBlank()) {
            LOGGER.warn("Disabling demo mode plugin automatically, because no JSON data URL is configured.");
            return;
        }

        LOGGER.info("Deleting existing data...");
        configurationService.deleteAllConfigurations();
        participationService.getAllParticipations().forEach(participationService::deleteParticipation);
        eventService.getEvents().forEach(eventService::deleteEvent);
        communityService.getCommunities().forEach(communityService::deleteCommunity);
        userService.getAllUsers().forEach(userService::deleteUser);
        imageService.getImages().forEach(imageService::deleteImage);
        globalPageService.getAllGlobalPages().forEach(globalPageService::deleteGlobalPage);
        LOGGER.info("Existing data deleted.");

        LOGGER.info("Importing demo data...");
        final var demoDataImporter = new JSONImporter(jsonDataUrl);
        demoDataImporter.importSettings(configurationService);
        demoDataImporter.importUsers(userService);
        demoDataImporter.importImages(imageService);
        demoDataImporter.importCommunities(communityService);
        demoDataImporter.importEvents(eventService);
        demoDataImporter.importGlobalPages(globalPageService);
        LOGGER.info("Demo data imported.");

        LOGGER.info("Cleaning up orphaned image files...");
        ImageUtil.cleanupOrphanedImageFiles(imageService);
        LOGGER.info("Orphaned image files cleaned up.");
    }

}
