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

import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.EventService;
import app.komunumo.data.service.GlobalPageService;
import app.komunumo.data.service.ImageService;
import app.komunumo.util.ImageUtil;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DemoDataImporterTest {

    @Test
    void testImporterWithFileNotFound() {
        final var jsonUrl = "http://localhost:8082/import/non-existing.json";
        final var expectedMessage = "Failed to download demo data: " + jsonUrl;
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            new DemoDataImporter(jsonUrl);
            assertThat(logCaptor.getWarnLogs()).containsExactly(expectedMessage);
        }
    }

    @Test
    void testSettingsNotFound() {
        final var jsonUrl = "http://localhost:8082/import/non-existing.json";
        final var expectedMessage = "No settings found in demo data.";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            final var importer = new DemoDataImporter(jsonUrl);
            importer.importSettings(mock(ConfigurationService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testImagesNotFound() {
        final var jsonUrl = "http://localhost:8082/import/non-existing.json";
        final var expectedMessage = "No images found in demo data.";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            final var importer = new DemoDataImporter(jsonUrl);
            importer.importImages(mock(ImageService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testCommunitiesNotFound() {
        final var jsonUrl = "http://localhost:8082/import/non-existing.json";
        final var expectedMessage = "No communities found in demo data.";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            final var importer = new DemoDataImporter(jsonUrl);
            importer.importCommunities(mock(CommunityService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testEventsNotFound() {
        final var jsonUrl = "http://localhost:8082/import/non-existing.json";
        final var expectedMessage = "No events found in demo data.";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            final var importer = new DemoDataImporter(jsonUrl);
            importer.importEvents(mock(EventService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testGlobalPagesNotFound() {
        final var jsonUrl = "http://localhost:8082/import/non-existing.json";
        final var expectedMessage = "No global pages found in demo data.";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            final var importer = new DemoDataImporter(jsonUrl);
            importer.importGlobalPages(mock(GlobalPageService.class));
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void testImporterWithRealJson() {
        final var jsonUrl = "http://localhost:8082/import/data.json";
        final var expectedMessage = "Successfully loaded 5 settings, 2 communities, 2 events, 3 images, and 2 global pages";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            new DemoDataImporter(jsonUrl);
            assertThat(logCaptor.getInfoLogs()).containsExactly(expectedMessage);
        }
    }

    @Test
    void testImportSettings() {
        final var configurationService = mock(ConfigurationService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        final var expectedMessage = "Successfully loaded 5 settings, 2 communities, 2 events, 3 images, and 2 global pages";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            final var importer = new DemoDataImporter(jsonUrl);
            importer.importSettings(configurationService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(expectedMessage);
            verify(configurationService, times(3)).setConfiguration(any(), any(), any());
        }
    }

    @Test
    void testImportImages() {
        final var imageService = mock(ImageService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        final var expectedInfoMessage = "Successfully loaded 5 settings, 2 communities, 2 events, 3 images, and 2 global pages";
        final var expectedErrorMessage = "Failed to import image: Simulated failure";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class);
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

            final var importer = new DemoDataImporter(jsonUrl);
            importer.importImages(imageService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(expectedInfoMessage);
            assertThat(logCaptor.getErrorLogs()).containsExactly(expectedErrorMessage);
            mockedImageUtil.verify(() -> ImageUtil.storeImage(any(), any()), times(2));
            verify(imageService, times(1)).storeImage(any());
        }
    }

    @Test
    void testImportCommunities() {
        final var communityService = mock(CommunityService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        final var expectedMessage = "Successfully loaded 5 settings, 2 communities, 2 events, 3 images, and 2 global pages";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            final var importer = new DemoDataImporter(jsonUrl);
            importer.importCommunities(communityService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(expectedMessage);
            verify(communityService, times(2)).storeCommunity(any());
        }
    }

    @Test
    void testImportEvents() {
        final var eventService = mock(EventService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        final var expectedMessage = "Successfully loaded 5 settings, 2 communities, 2 events, 3 images, and 2 global pages";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            final var importer = new DemoDataImporter(jsonUrl);
            importer.importEvents(eventService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(expectedMessage);
            verify(eventService, times(2)).storeEvent(any());
        }
    }

    @Test
    void testImportGlobalPages() {
        final var globalPageService = mock(GlobalPageService.class);
        final var jsonUrl = "http://localhost:8082/import/data.json";
        final var expectedMessage = "Successfully loaded 5 settings, 2 communities, 2 events, 3 images, and 2 global pages";
        try (var logCaptor = LogCaptor.forClass(DemoDataImporter.class)) {
            final var importer = new DemoDataImporter(jsonUrl);
            importer.importGlobalPages(globalPageService);

            assertThat(logCaptor.getInfoLogs()).containsExactly(expectedMessage);
            verify(globalPageService, times(2)).storeGlobalPage(any());
        }
    }

}

