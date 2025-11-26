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
package app.komunumo.domain.core.importer.control;

import app.komunumo.KomunumoException;
import app.komunumo.domain.community.control.CommunityService;
import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.core.image.control.ImageService;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.member.control.MemberService;
import app.komunumo.domain.page.control.GlobalPageService;
import app.komunumo.domain.participation.control.ParticipationService;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.util.ImageUtil;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JSONImporterTest {

    private static final String IDENTIFIED_COUNTS_MESSAGE = "Identified 6 settings, 4 images, 5 users, 7 communities, 7 events, 25 members, 7 participations, and 3 global pages.";
    private static final UUID UUID_ZERO = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Test
    void testImporterWithURLNotFound() {
        final var jsonUrl = "http://localhost:8082/import/non-existing.json";
        final var expectedMessage = "Failed to download JSON data from URL: " + jsonUrl;
        assertThatThrownBy(() -> new JSONImporter(new ImporterLog(null), jsonUrl))
                .isInstanceOf(KomunumoException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testImporterWithFileNotFound() {
        final var jsonFile = Path.of("non-existing.json").toFile();
        final var expectedMessage = "Failed to load JSON data from file: " + jsonFile.getName();
        assertThatThrownBy(() -> new JSONImporter(new ImporterLog(null), jsonFile))
                .isInstanceOf(KomunumoException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testSettingsNotFound() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "No settings found in JSON data.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importSettings(mock(ConfigurationService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testImagesNotFound() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "No images found in JSON data.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importImages(mock(ImageService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testUsersNotFound() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "No users found in JSON data.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importUsers(mock(UserService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testCommunitiesNotFound() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "No communities found in JSON data.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importCommunities(mock(CommunityService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testMembersNotFound() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "No members found in JSON data.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importMembers(mock(MemberService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testEventsNotFound() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "No events found in JSON data.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importEvents(mock(EventService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testParticipationsNotFound() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "No event participations found in JSON data.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importParticipations(mock(ParticipationService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testGlobalPagesNotFound() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "No global pages found in JSON data.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importGlobalPages(mock(GlobalPageService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testImporterWithRealJson() {
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            new JSONImporter(new ImporterLog(null), jsonUrl);
            assertThat(logCaptor.getInfoLogs()).containsExactly(IDENTIFIED_COUNTS_MESSAGE);
        }
    }

    @Test
    void testImportSettings() {
        final var configurationService = mock(ConfigurationService.class);
        doThrow(new RuntimeException("Simulated failure"))
                .when(configurationService)
                .setConfiguration(argThat(setting -> "simulated.failure".equals(setting.setting())), any());
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importSettings(configurationService);
            verify(configurationService, times(3)).setConfiguration(any(), any(), any());
            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    IDENTIFIED_COUNTS_MESSAGE,
                    "Start importing settings...",
                    "...finished importing 3 settings.");
            assertThat(logCaptor.getWarnLogs()).containsExactly(
                    "Skipping setting 'instance.slogan' because it is language-dependent but no language was provided.",
                    "Skipping setting 'instance.custom.styles' because it is not language-dependent but a language was provided.",
                    "Failed to import setting: Unknown setting: simulated.failure");
            assertThat(logCaptor.getErrorLogs()).isEmpty();
        }
    }

    @Test
    void testImportImages() {
        final var imageService = mock(ImageService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class);
             var mockedImageUtil = mockStatic(ImageUtil.class)) {

            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importImages(imageService);

            mockedImageUtil.verify(() -> ImageUtil.storeImage(any(), any()), times(2));
            verify(imageService, times(2)).storeImage(any());

            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    IDENTIFIED_COUNTS_MESSAGE,
                    "Start importing images...",
                    "...finished importing 2 images.");
            assertThat(logCaptor.getWarnLogs()).containsExactly(
                    "Failed to import image: Failed to download file from "
                    + "'http://localhost:8082/import/non-existing.svg': HTTP status code 404",
                    "Failed to import image: Invalid data URL: data:broken");
            assertThat(logCaptor.getErrorLogs()).isEmpty();
        }
    }

    @Test
    void testImportUsers() {
        final var userService = mock(UserService.class);
        doThrow(new RuntimeException("Simulated failure"))
                .when(userService)
                .storeUser(argThat(user -> UUID_ZERO.equals(user.id())));
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importUsers(userService);
            verify(userService, times(5)).storeUser(any());
            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    IDENTIFIED_COUNTS_MESSAGE,
                    "Start importing users...",
                    "...finished importing 4 users.");
            assertThat(logCaptor.getWarnLogs()).containsExactly(
                    "Failed to import user: Simulated failure");
            assertThat(logCaptor.getErrorLogs()).isEmpty();
        }
    }

    @Test
    void testImportCommunities() {
        final var communityService = mock(CommunityService.class);
        doThrow(new RuntimeException("Simulated failure"))
                .when(communityService)
                .storeCommunity(argThat(community -> UUID_ZERO.equals(community.id())));
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importCommunities(communityService);
            verify(communityService, times(7)).storeCommunity(any());
            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    IDENTIFIED_COUNTS_MESSAGE,
                    "Start importing communities...",
                    "...finished importing 6 communities.");
            assertThat(logCaptor.getWarnLogs()).containsExactly(
                    "Failed to import community: Simulated failure");
            assertThat(logCaptor.getErrorLogs()).isEmpty();
        }
    }

    @Test
    void testImportEvents() {
        final var eventService = mock(EventService.class);
        doThrow(new RuntimeException("Simulated failure"))
                .when(eventService)
                .storeEvent(argThat(event -> UUID_ZERO.equals(event.id())));
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importEvents(eventService);
            verify(eventService, times(7)).storeEvent(any());
            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    IDENTIFIED_COUNTS_MESSAGE,
                    "Start importing events...",
                    "...finished importing 6 events.");
            assertThat(logCaptor.getWarnLogs()).containsExactly(
                    "Failed to import event: Simulated failure");
            assertThat(logCaptor.getErrorLogs()).isEmpty();
        }
    }

    @Test
    void testImportMembers() {
        final var memberService = mock(MemberService.class);
        doThrow(new RuntimeException("Simulated failure"))
                .when(memberService)
                .storeMember(argThat(member -> UUID_ZERO.equals(member.userId())));
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importMembers(memberService);
            verify(memberService, times(25)).storeMember(any());
            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    IDENTIFIED_COUNTS_MESSAGE,
                    "Start importing members...",
                    "...finished importing 24 members.");
            assertThat(logCaptor.getWarnLogs()).containsExactly(
                    "Failed to import member: Simulated failure");
            assertThat(logCaptor.getErrorLogs()).isEmpty();
        }
    }

    @Test
    void testImportParticipations() {
        final var participationService = mock(ParticipationService.class);
        doThrow(new RuntimeException("Simulated failure"))
                .when(participationService)
                .storeParticipation(argThat(participation -> UUID_ZERO.equals(participation.eventId())));
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importParticipations(participationService);
            verify(participationService, times(7)).storeParticipation(any());
            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    IDENTIFIED_COUNTS_MESSAGE,
                    "Start importing event participations...",
                    "...finished importing 6 event participations.");
            assertThat(logCaptor.getWarnLogs()).containsExactly(
                    "Failed to import event participations: Simulated failure");
            assertThat(logCaptor.getErrorLogs()).isEmpty();
        }
    }

    @Test
    void testImportGlobalPages() {
        final var globalPageService = mock(GlobalPageService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importGlobalPages(globalPageService);
            verify(globalPageService, times(2)).storeGlobalPage(any());
            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    IDENTIFIED_COUNTS_MESSAGE,
                    "Start importing global pages...",
                    "...finished importing 2 global pages.");
            assertThat(logCaptor.getWarnLogs()).containsExactly(
                    "Failed to import global page: JSONObject[\"language\"] is not a string "
                    + "(class org.json.JSONObject$Null : null).");
            assertThat(logCaptor.getErrorLogs()).isEmpty();
        }
    }

    @Test
    void testNoData() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "Identified 0 settings, 0 images, 0 users, 0 communities, 0 events, 0 members, 0 participations, and 0 global pages.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            new JSONImporter(new ImporterLog(null), jsonUrl);
            assertThat(logCaptor.getInfoLogs()).containsExactly(expectedMessage);
        }
    }

}

