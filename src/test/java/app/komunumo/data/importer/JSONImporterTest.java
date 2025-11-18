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
package app.komunumo.data.importer;

import app.komunumo.KomunumoException;
import app.komunumo.business.core.image.entity.ImageDto;
import app.komunumo.business.community.control.CommunityService;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.business.event.control.EventService;
import app.komunumo.data.service.GlobalPageService;
import app.komunumo.business.core.image.control.ImageService;
import app.komunumo.data.service.MemberService;
import app.komunumo.data.service.UserService;
import app.komunumo.util.ImageUtil;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JSONImporterTest {

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
        final var expectedMessage = "Identified 5 settings, 3 images, 4 users, 6 communities, 6 events, 24 members, and 2 global pages.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            new JSONImporter(new ImporterLog(null), jsonUrl);
            assertThat(logCaptor.getInfoLogs()).containsExactly(expectedMessage);
        }
    }

    @Test
    void testImportSettings() {
        final var configurationService = mock(ConfigurationService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importSettings(configurationService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    "Identified 5 settings, 3 images, 4 users, 6 communities, 6 events, 24 members, and 2 global pages.",
                    "Start importing settings...",
                    "...finished importing 3 settings.");
            verify(configurationService, times(3)).setConfiguration(any(), any(), any());
        }
    }

    @Test
    void testImportImages() {
        final var imageService = mock(ImageService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class);
             var mockedImageUtil = mockStatic(ImageUtil.class)) {
            mockedImageUtil.when(() -> ImageUtil.storeImage(any(), any())).thenAnswer(invocation -> {
                final var image = invocation.getArgument(0, ImageDto.class);
                final var imageId = image.id();
                assertThat(imageId).isNotNull();
                if (imageId.toString().equals("4ca05a55-de1e-4571-a833-c9e5e4f4bfba")) {
                    throw new IOException("Simulated failure");
                }
                return null;
            });

            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importImages(imageService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    "Identified 5 settings, 3 images, 4 users, 6 communities, 6 events, 24 members, and 2 global pages.",
                    "Start importing images...",
                    "...finished importing 3 images.");
            assertThat(logCaptor.getErrorLogs()).containsExactly(
                    "Failed to import image: Simulated failure");
            mockedImageUtil.verify(() -> ImageUtil.storeImage(any(), any()), times(2));
            verify(imageService, times(1)).storeImage(any());
        }
    }

    @Test
    void testImportUsers() {
        final var userService = mock(UserService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importUsers(userService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    "Identified 5 settings, 3 images, 4 users, 6 communities, 6 events, 24 members, and 2 global pages.",
                    "Start importing users...",
                    "...finished importing 4 users.");
            verify(userService, times(4)).storeUser(any());
        }
    }

    @Test
    void testImportCommunities() {
        final var communityService = mock(CommunityService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importCommunities(communityService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    "Identified 5 settings, 3 images, 4 users, 6 communities, 6 events, 24 members, and 2 global pages.",
                    "Start importing communities...",
                    "...finished importing 6 communities.");
            verify(communityService, times(6)).storeCommunity(any());
        }
    }

    @Test
    void testImportEvents() {
        final var eventService = mock(EventService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importEvents(eventService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    "Identified 5 settings, 3 images, 4 users, 6 communities, 6 events, 24 members, and 2 global pages.",
                    "Start importing events...",
                    "...finished importing 6 events.");
            verify(eventService, times(6)).storeEvent(any());
        }
    }

    @Test
    void testImportMembers() {
        final var memberService = mock(MemberService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importMembers(memberService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    "Identified 5 settings, 3 images, 4 users, 6 communities, 6 events, 24 members, and 2 global pages.",
                    "Start importing members...",
                    "...finished importing 24 events.");
            verify(memberService, times(24)).storeMember(any());
        }
    }

    @Test
    void testImportGlobalPages() {
        final var globalPageService = mock(GlobalPageService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            final var importer = new JSONImporter(new ImporterLog(null), jsonUrl);
            importer.importGlobalPages(globalPageService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(
                    "Identified 5 settings, 3 images, 4 users, 6 communities, 6 events, 24 members, and 2 global pages.",
                    "Start importing global pages...",
                    "...finished importing 2 global pages.");
            verify(globalPageService, times(2)).storeGlobalPage(any());
        }
    }

    @Test
    void testNoData() {
        final var jsonUrl = "http://localhost:8082/import/no-data.json";
        final var expectedMessage = "Identified 0 settings, 0 images, 0 users, 0 communities, 0 events, 0 members, and 0 global pages.";
        try (var logCaptor = LogCaptor.forClass(ImporterLog.class)) {
            new JSONImporter(new ImporterLog(null), jsonUrl);
            assertThat(logCaptor.getInfoLogs()).containsExactly(expectedMessage);
        }
    }

}

