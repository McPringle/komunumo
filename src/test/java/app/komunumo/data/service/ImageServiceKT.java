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
package app.komunumo.data.service;

import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.ui.KaribuTest;
import app.komunumo.util.ImageUtil;
import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ImageServiceKT extends KaribuTest {

    @Autowired
    private @NotNull ImageService imageService;

    @Test
    void happyCase() {
        assertThat(imageService.getImageCount()).isEqualTo(10);

        // store new image
        var image = imageService.storeImage(new ImageDto(null, ContentType.IMAGE_WEBP));
        final var imageId = image.id();
        assertThat(imageId).isNotNull();
        assertThat(image.contentType()).isEqualTo(ContentType.IMAGE_WEBP);
        assertThat(imageService.getImageCount()).isEqualTo(11);

        // read the image from the database
        image = imageService.getImage(imageId).orElseThrow();
        assertThat(image.id()).isEqualTo(imageId);
        assertThat(image.contentType()).isEqualTo(ContentType.IMAGE_WEBP);
        assertThat(imageService.getImageCount()).isEqualTo(11);

        // update existing image
        image = imageService.storeImage(new ImageDto(image.id(), ContentType.IMAGE_SVG));
        assertThat(image.id()).isEqualTo(imageId);
        assertThat(image.contentType()).isEqualTo(ContentType.IMAGE_SVG);
        assertThat(imageService.getImageCount()).isEqualTo(11);

        // read the image from the database
        image = imageService.getImage(imageId).orElseThrow();
        assertThat(image.id()).isEqualTo(imageId);
        assertThat(image.contentType()).isEqualTo(ContentType.IMAGE_SVG);
        assertThat(imageService.getImageCount()).isEqualTo(11);

        // read all images from the database
        final var images = imageService.getImages();
        assertThat(images).contains(image);

        // find orphaned images
        final var orphanedImages = imageService.findOrphanedImages();
        assertThat(orphanedImages).hasSize(1);
        assertThat(orphanedImages.getFirst()).isEqualTo(image);

        // delete the existing image
        assertThat(imageService.deleteImage(image)).isTrue();
        assertThat(imageService.getImage(imageId)).isEmpty();
        assertThat(imageService.getImageCount()).isEqualTo(10);

        // find orphaned images again
        assertThat(imageService.findOrphanedImages()).isEmpty();

        // delete the non-existing image (was already deleted before)
        assertThat(imageService.deleteImage(image)).isFalse();
    }

    @Test
    void noImageWithNullId() {
        assertThat(imageService.getImage(null)).isEmpty();
    }

    @Test
    void cleanupOrphanedImages() {
        var image = imageService.storeImage(new ImageDto(null, ContentType.IMAGE_WEBP));
        assertThat(image).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isNotNull();
            assertThat(testee.contentType()).isEqualTo(ContentType.IMAGE_WEBP);
        });

        assertThat(imageService.findOrphanedImages()).containsExactly(image);
        imageService.cleanupOrphanedImages();
        assertThat(imageService.findOrphanedImages()).isEmpty();
    }

    @Test
    void deleteUnsavedImageReturnsFalse() {
        final var image = new ImageDto(null, ContentType.IMAGE_WEBP);
        assertThat(imageService.deleteImage(image)).isFalse();
    }

    @Test
    void deleteImageShouldLogErrorWhenFileDeletionFails() {
        // Arrange
        final var image = new ImageDto(UUID.randomUUID(), ContentType.IMAGE_WEBP);
        final var path = ImageUtil.resolveImagePath(image);
        assertThat(path).isNotNull();

        try (var filesMock = mockStatic(Files.class);
             var logCaptor = LogCaptor.forClass(ImageService.class)) {

            filesMock.when(() -> Files.deleteIfExists(path)).thenThrow(new IOException("Disk I/O error"));

            // Act
            imageService.deleteImage(image);

            // Assert
            final List<String> errorLogs = logCaptor.getErrorLogs();
            assertThat(errorLogs).anySatisfy(log ->
                    assertThat(log).isEqualTo("Failed to delete image file: " + path.toAbsolutePath()));
        }
    }

}
