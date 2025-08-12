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
package app.komunumo.servlets.images;

import app.komunumo.KomunumoException;
import app.komunumo.configuration.AppConfig;
import app.komunumo.configuration.DemoConfig;
import app.komunumo.configuration.FilesConfig;
import app.komunumo.configuration.InstanceConfig;
import app.komunumo.configuration.MailConfig;
import app.komunumo.data.service.ImageService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.util.ResourceUtil;
import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.K;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PlaceholderImageGeneratorTest {

    private static final @NotNull String TEST_SVG = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="500" height="400" viewBox="0 0 500 400" xmlns="http://www.w3.org/2000/svg">
              <circle cx="50" cy="50" r="40" fill="red" />
            </svg>""";

    private ServiceProvider getServiceProviderMock() {
        final var userHome = System.getProperty("user.home");
        final var basedir = Path.of(userHome, ".komunumo", "test");

        final var serviceProvider = mock(ServiceProvider.class);
        final var imageService = mock(ImageService.class);

        final var demoConfig = new DemoConfig(false, "");
        final var filesConfig = new FilesConfig(basedir);
        final var mailConfig = new MailConfig("noreply@foo.bar", "support@foo.bar");
        final var instanceConfig = new InstanceConfig("admin@foo.bar", "", false);
        final var appConfig = new AppConfig("0.0.0", demoConfig, filesConfig, instanceConfig, mailConfig);


        when(serviceProvider.imageService()).thenReturn(imageService);
        when(serviceProvider.getAppConfig()).thenReturn(appConfig);

        return serviceProvider;
    }

    @Test
    void generateHorizontalPlaceholderImage() {
        final var serviceProvider = getServiceProviderMock();
        final var generator = new PlaceholderImageGenerator(serviceProvider);
        final var image = generator.getPlaceholderImage(200, 100);
        assertThat(image)
                .isNotNull()
                .contains("width=\"200\"")
                .contains("height=\"100\"");
    }

    @Test
    void generateVerticalPlaceholderImage() {
        final var serviceProvider = getServiceProviderMock();
        final var generator = new PlaceholderImageGenerator(serviceProvider);
        final var image = generator.getPlaceholderImage(100, 200);
        assertThat(image)
                .isNotNull()
                .contains("width=\"100\"")
                .contains("height=\"200\"");
    }

    @Test
    void useCustomLogoWhenAvailable() {
        final var serviceProvider = getServiceProviderMock();
        final var customLogoPath = serviceProvider.getAppConfig().files().basedir()
                .resolve(Path.of("custom", "images", "logo.svg"));

        try (MockedStatic<Files> mocked = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> Files.exists(customLogoPath))
                    .thenReturn(TRUE);
            mocked.when(() -> Files.readString(customLogoPath))
                    .thenReturn(TEST_SVG);

            final var generator = new PlaceholderImageGenerator(serviceProvider);
            final var image = generator.getPlaceholderImage(200, 100);

            assertThat(image)
                    .isNotNull()
                    .contains("<circle")
                    .contains("cx=\"50\"")
                    .contains("cy=\"50\"")
                    .contains("r=\"40\"")
                    .contains("fill=\"red\"");
        }
    }

    @Test
    void exceptionWhenReadingCustomLogo() {
        final var serviceProvider = getServiceProviderMock();
        final var customLogoPath = serviceProvider.getAppConfig().files().basedir()
                .resolve(Path.of("custom", "images", "logo.svg"));

        try (MockedStatic<Files> mocked = mockStatic(Files.class, CALLS_REAL_METHODS);
             var logCaptor = LogCaptor.forClass(PlaceholderImageGenerator.class)) {
            mocked.when(() -> Files.exists(customLogoPath))
                    .thenReturn(TRUE);
            mocked.when(() -> Files.readString(customLogoPath))
                    .thenThrow(new IOException("boom"));

            new PlaceholderImageGenerator(serviceProvider);

            final var expectedMessage = "Failed to read custom logo from '" + customLogoPath + "', fallback to default logo.";
            assertThat(logCaptor.getWarnLogs()).contains(expectedMessage);
        }
    }

    @Test
    void exceptionWhenReadingDefaultLogo() {
        final var serviceProvider = getServiceProviderMock();
        final var inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };

        try (MockedStatic<ResourceUtil> mocked = mockStatic(ResourceUtil.class, CALLS_REAL_METHODS);
             var logCaptor = LogCaptor.forClass(PlaceholderImageGenerator.class)) {
            //noinspection resource
            mocked.when(() -> ResourceUtil.openResourceStream("/META-INF/resources/images/komunumo.svg"))
                    .thenReturn(inputStream);

            assertThatThrownBy(() -> new PlaceholderImageGenerator(serviceProvider))
                    .isInstanceOf(KomunumoException.class)
                    .hasMessageStartingWith("Failed to initialize template parser:");
            assertThat(logCaptor.getInfoLogs()).contains("No custom logo found, using default logo.");
            assertThat(logCaptor.getWarnLogs()).contains("Failed to read default logo from '/META-INF/resources/images/komunumo.svg', fallback to empty logo.");
        }
    }

}
