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
package app.komunumo.util;

import app.komunumo.business.core.config.entity.AppConfig;
import app.komunumo.business.core.config.entity.DemoConfig;
import app.komunumo.business.core.config.entity.FilesConfig;
import app.komunumo.business.core.config.entity.InstanceConfig;
import app.komunumo.business.core.config.entity.MailConfig;
import app.komunumo.business.core.image.control.ImageService;
import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImageUtilTest {

    @TempDir
    private Path tempDir;

    private Path uploadImagePath;

    @BeforeEach
    void setUp() {
        final var demoConfig = new DemoConfig(false, "");
        final var filesConfig = new FilesConfig(tempDir);
        final var mailConfig = new MailConfig("noreply@foo.bar", "support@foo.bar");
        final var instanceConfig = new InstanceConfig("admin@foo.bar");
        final var appConfig = new AppConfig("0.0.0", demoConfig, filesConfig, instanceConfig, mailConfig);
        ImageUtil.initialize(appConfig);
        uploadImagePath = tempDir.resolve("uploads/images");
    }

    @Test
    void shouldDeleteOrphanedFilesAndEmptyDirectories() throws IOException {
        final var known = UUID.randomUUID();
        final var orphan = java.util.UUID.randomUUID();

        final var knownDir = uploadImagePath.resolve(known.toString().substring(0, 2))
                .resolve(known.toString().substring(2, 4));
        final var orphanDir = uploadImagePath.resolve(orphan.toString().substring(0, 2))
                .resolve(orphan.toString().substring(2, 4));
        Files.createDirectories(knownDir);
        Files.createDirectories(orphanDir);

        final var knownFile = knownDir.resolve(known + ".jpg");
        final var orphanFile = orphanDir.resolve(orphan + ".png");

        Files.createFile(knownFile);
        Files.createFile(orphanFile);

        final var imageService = mock(ImageService.class);
        when(imageService.getAllImageIds()).thenReturn(List.of(UUID.fromString(known.toString())));

        ImageUtil.cleanupOrphanedImageFiles(imageService);

        assertThat(Files.exists(knownFile)).isTrue();
        assertThat(Files.exists(orphanFile)).isFalse();
        assertThat(Files.exists(orphanDir)).isFalse(); // has been emptied → deleted
        assertThat(Files.exists(knownDir)).isTrue();   // contains known file → remains
    }

    @Test
    void shouldSkipInvalidUuidFilenames() throws IOException {
        final var dir = Files.createDirectories(uploadImagePath.resolve("xx/yy"));
        final var file = dir.resolve("not-a-uuid.jpg");
        Files.createFile(file);

        final var imageService = mock(ImageService.class);
        when(imageService.getAllImageIds()).thenReturn(List.of());

        ImageUtil.cleanupOrphanedImageFiles(imageService);

        assertThat(Files.exists(file)).isTrue(); // file name is not a UUID → remains
    }

    @Test
    void shouldHandleDeleteFileIOException() throws IOException {
        final var uuid = UUID.randomUUID();
        final var dir = Files.createDirectories(uploadImagePath
                .resolve(uuid.toString().substring(0, 2))
                .resolve(uuid.toString().substring(2, 4)));
        final var file = dir.resolve(uuid + ".jpg");
        Files.createFile(file);

        final var imageService = mock(ImageService.class);
        when(imageService.getAllImageIds()).thenReturn(List.of());

        var spy = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS);
        spy.when(() -> Files.delete(Mockito.eq(file)))
                .thenThrow(new IOException("Mocked delete failure"));

        assertThatCode(() -> ImageUtil.cleanupOrphanedImageFiles(imageService))
                .doesNotThrowAnyException();

        spy.close();
    }

    @Test
    void shouldHandleUnreadableDirectory() throws IOException {
        final var dir = Files.createDirectories(uploadImagePath.resolve("unreadable"));
        Files.createFile(dir.resolve(UUID.randomUUID() + ".jpg"));

        final var imageService = mock(ImageService.class);
        when(imageService.getAllImageIds()).thenReturn(List.of());

        var spy = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS);
        spy.when(() -> Files.delete(Mockito.eq(dir)))
                .thenThrow(new IOException("Mocked delete failure"));

        assertThatCode(() -> ImageUtil.cleanupOrphanedImageFiles(imageService))
                .doesNotThrowAnyException();

        spy.close();
    }

    @Test
    void shouldHandleException() throws IOException {
        final var dir = Files.createDirectories(uploadImagePath);

        final var imageService = mock(ImageService.class);
        when(imageService.getAllImageIds()).thenReturn(List.of());

        try (var spy = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            //noinspection resource // false positive, Mockito handles resource management
            spy.when(() -> Files.walk(Mockito.eq(dir)))
                    .thenThrow(new IOException("Mocked failure"));

            assertThatCode(() -> ImageUtil.cleanupOrphanedImageFiles(imageService))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void shouldHandleMissingBaseDirectoryGracefully() {
        final var imageService = mock(ImageService.class);
        when(imageService.getAllImageIds()).thenReturn(List.of());

        try (var logCaptor = LogCaptor.forClass(ImageUtil.class)) {
            assertThatCode(() -> ImageUtil.cleanupOrphanedImageFiles(imageService))
                    .doesNotThrowAnyException();

            assertThat(logCaptor.getInfoLogs())
                    .contains("No images to clean, directory '" + uploadImagePath + "' does not exist.");
        }
    }

    private static Stream<Arguments> provideTestData_convertToPixels() {
        return Stream.of(
                Arguments.of("10", 0, 10),
                Arguments.of("10px", 0, 10),
                Arguments.of("10in", 0, 960),
                Arguments.of("10mm", 0, 38),
                Arguments.of("10cm", 0, 378),
                Arguments.of("10pt", 0, 13),
                Arguments.of("10pc", 0, 160),
                Arguments.of("50%", 500, 250)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestData_convertToPixels")
    void convertToPixels(final @NotNull String dimension, final double viewBoxReference, final long expectedValue) {
        assertThat(ImageUtil.convertToPixels(dimension, viewBoxReference)).isEqualTo(expectedValue);
    }

    @Test
    void convertToPixelsWithInvalidDimensionThrowsException() {
        assertThatThrownBy(() -> ImageUtil.convertToPixels("1x", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dimension '1x' must have a valid unit (e.g., 'px', 'in', 'mm', 'cm', 'pt', 'pc', '%').");
    }

    @Test
    void convertToPixelsWithInvalidUnitThrowsException() {
        assertThatThrownBy(() -> ImageUtil.convertToPixels("10ab", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported unit: ab");
    }

    @Test
    void convertPercentToPixelsWithoutViewBoxThrowsException() {
        assertThatThrownBy(() -> ImageUtil.convertToPixels("50%", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot convert percentage without view box reference");
    }

    @Test
    void convertToPixelsWithEmptyDimensionThrowsException() {
        assertThatThrownBy(() -> ImageUtil.convertToPixels("", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dimension must not be blank");
    }

    @Test
    void convertToPixelsWithBlankDimensionThrowsException() {
        assertThatThrownBy(() -> ImageUtil.convertToPixels("     ", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dimension must not be blank");
    }

}
