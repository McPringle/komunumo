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
package app.komunumo.domain.core.demo.control;

import app.komunumo.domain.core.config.entity.AppConfig;
import app.komunumo.domain.core.importer.control.ImporterLog;
import app.komunumo.domain.core.importer.control.JSONImporter;
import app.komunumo.domain.community.control.CommunityService;
import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.core.mail.control.MailService;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.page.control.GlobalPageService;
import app.komunumo.domain.core.image.control.ImageService;
import app.komunumo.domain.member.control.MemberService;
import app.komunumo.domain.participant.control.ParticipantService;
import app.komunumo.domain.user.control.UserService;
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
    private final @NotNull MemberService memberService;
    private final @NotNull ParticipantService participantService;
    private final @NotNull GlobalPageService globalPageService;
    private final @NotNull MailService mailService;

    private final boolean enabled;
    private final @NotNull String jsonDataUrl;

    @SuppressWarnings("checkstyle:ParameterNumber") // constructor injection
    public DemoMode(final @NotNull AppConfig appConfig,
                    final @NotNull ConfigurationService configurationService,
                    final @NotNull UserService userService,
                    final @NotNull ImageService imageService,
                    final @NotNull CommunityService communityService,
                    final @NotNull EventService eventService,
                    final @NotNull MemberService memberService,
                    final @NotNull ParticipantService participantService,
                    final @NotNull GlobalPageService globalPageService,
                    final @NotNull MailService mailService) {
        this.configurationService = configurationService;
        this.userService = userService;
        this.imageService = imageService;
        this.communityService = communityService;
        this.eventService = eventService;
        this.memberService = memberService;
        this.participantService = participantService;
        this.globalPageService = globalPageService;
        this.mailService = mailService;

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
        participantService.getAllParticipants().forEach(participantService::deleteParticipant);
        eventService.getEvents().forEach(eventService::deleteEvent);
        memberService.getMembers().forEach(memberService::deleteMember);
        communityService.getCommunities().forEach(communityService::deleteCommunity);
        userService.getAllUsers().forEach(userService::deleteUser);
        imageService.getImages().forEach(imageService::deleteImage);
        globalPageService.getAllGlobalPages().forEach(globalPageService::deleteGlobalPage);
        LOGGER.info("Existing data deleted.");

        LOGGER.info("Importing demo data...");
        final var demoDataImporter = new JSONImporter(new ImporterLog(null), jsonDataUrl);
        demoDataImporter.importSettings(configurationService);
        demoDataImporter.importImages(imageService);
        demoDataImporter.importUsers(userService);
        demoDataImporter.importCommunities(communityService);
        demoDataImporter.importMembers(memberService);
        demoDataImporter.importEvents(eventService);
        demoDataImporter.importGlobalPages(globalPageService);
        demoDataImporter.importMailTemplates(mailService);
        LOGGER.info("Demo data imported.");

        LOGGER.info("Cleaning up orphaned image files...");
        ImageUtil.cleanupOrphanedImageFiles(imageService);
        LOGGER.info("Orphaned image files cleaned up.");
    }

}
