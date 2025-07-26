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

import app.komunumo.configuration.AppConfig;
import app.komunumo.configuration.DemoConfig;
import app.komunumo.configuration.FilesConfig;
import app.komunumo.configuration.InstanceConfig;
import app.komunumo.configuration.MailConfig;
import app.komunumo.data.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
        final var instanceConfig = new InstanceConfig("admin@foo.bar", "", false);
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
    void shouldHandleMissingBaseDirectoryGracefully() {
        final var imageService = mock(ImageService.class);
        when(imageService.getAllImageIds()).thenReturn(List.of());

        assertThatCode(() -> ImageUtil.cleanupOrphanedImageFiles(imageService))
                .doesNotThrowAnyException();
    }

}
