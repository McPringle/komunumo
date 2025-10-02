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

import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.ImageService;
import app.komunumo.ui.IntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.io.File.separator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class ImageUtilIT extends IntegrationTest {

    @Autowired
    private @NotNull ImageService imageService;

    private static String getSubFolder(final @NotNull UUID imageId) {
        final String id = imageId.toString();
        final String prefix1 = id.substring(0, 2);
        final String prefix2 = id.substring(2, 4);
        return prefix1 + java.io.File.separator + prefix2;
    }

    @Test
    void resolveImageUrl() {
        // null should give placeholder now
        assertThat(ImageUtil.resolveImageUrl(null)).isEqualTo("/images/komunumo.svg");

        final var imageWithoutId = new ImageDto(null, ContentType.IMAGE_JPEG);
        // no id → placeholder
        assertThat(ImageUtil.resolveImageUrl(imageWithoutId)).isEqualTo("/images/komunumo.svg");

        final var imageId = UUID.randomUUID();
        final var imageWithId = new ImageDto(imageId, ContentType.IMAGE_JPEG);
        assertThat(ImageUtil.resolveImageUrl(imageWithId)).isEqualTo("/images/" + imageId + ".jpg");
    }


    @Test
    void resolveImagePath() {
        assertThat(ImageUtil.resolveImagePath(null)).isNull();

        final var imageWithoutId = new ImageDto(null, ContentType.IMAGE_JPEG);
        // path should still be null because placeholder isn’t a real file
        assertThat(ImageUtil.resolveImagePath(imageWithoutId)).isNull();

        final var imageId = UUID.randomUUID();
        final var imageWithId = new ImageDto(imageId, ContentType.IMAGE_JPEG);
        final var path = ImageUtil.resolveImagePath(imageWithId);
        assertThat(path).isNotNull();
        assertThat(path.toString()).endsWith(
            separator + "images" + separator + getSubFolder(imageId) + separator + imageId + ".jpg"
        );
    }

    @Test
    void loadImage() {
        assertThat(ImageUtil.loadImage(null)).isEmpty();

        final var imageWithoutId = new ImageDto(null, ContentType.IMAGE_JPEG);
        // placeholder isn’t on disk → should still be empty
        assertThat(ImageUtil.loadImage(imageWithoutId)).isEmpty();

        final var randomImageId = UUID.randomUUID();
        final var imageWithRandomId = new ImageDto(randomImageId, ContentType.IMAGE_JPEG);
        assertThat(ImageUtil.loadImage(imageWithRandomId)).isEmpty();

        final var existingImageId = imageService.getImages().getFirst().id();
        final var imageWithExistingId = new ImageDto(existingImageId, ContentType.IMAGE_JPEG);
        assertThat(ImageUtil.loadImage(imageWithExistingId)).isNotEmpty();
    }

    @Test
    @SuppressWarnings("resource")
    void loadImageWithException() {
        // Arrange
        final var image = new ImageDto(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                ContentType.IMAGE_JPEG
        );

        final var path = Path.of("/fake/path.jpg");

        // Mock resolveImagePath(...) to return the fake path
        try (var mockedImageUtil = mockStatic(ImageUtil.class, CALLS_REAL_METHODS);
             var mockedFiles = mockStatic(Files.class)) {

            mockedImageUtil.when(() -> ImageUtil.resolveImagePath(image)).thenReturn(path);
            mockedFiles.when(() -> Files.exists(path)).thenReturn(true);
            mockedFiles.when(() -> Files.newInputStream(path)).thenThrow(new IOException("simulated failure"));

            // Act
            var result = ImageUtil.loadImage(image);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Test
    void extractImageIdFromUrl() {
        assertThat(ImageUtil.extractImageIdFromUrl("")).isNull();
        assertThat(ImageUtil.extractImageIdFromUrl(" ")).isNull();
        assertThat(ImageUtil.extractImageIdFromUrl("/images/test.jpg")).isNull();
        assertThat(ImageUtil.extractImageIdFromUrl("/images/11111111-1111-1111-1111-111111111111.jpg"))
                .isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(ImageUtil.extractImageIdFromUrl("/images/afc3478d-2c92-41b5-b89f-2a9111d79c73.jpg"))
                .isEqualTo(UUID.fromString("afc3478d-2c92-41b5-b89f-2a9111d79c73"));
    }

    @Test
    void storeImageWithException() {
        final var imageWithoutId = new ImageDto(null, ContentType.IMAGE_JPEG);
        final var testPath = Path.of(".");
        assertThatThrownBy(() -> ImageUtil.storeImage(imageWithoutId, testPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ImageDto must have an ID!");
    }

}
