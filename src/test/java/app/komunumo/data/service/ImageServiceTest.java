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

import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.util.ImageUtil;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Autowired
    private @NotNull ImageService imageService;

    @Test
    void happyCase() {
        assertThat(imageService.getImageCount()).isEqualTo(5);

        // store new image
        var image = imageService.storeImage(new ImageDto(null, ContentType.IMAGE_WEBP, "test.webp"));
        final var imageId = image.id();
        assertThat(imageId).isNotNull();
        assertThat(image.contentType()).isEqualTo(ContentType.IMAGE_WEBP);
        assertThat(image.filename()).isEqualTo("test.webp");
        assertThat(imageService.getImageCount()).isEqualTo(6);

        // read the image from the database
        image = imageService.getImage(imageId).orElseThrow();
        assertThat(image.id()).isEqualTo(imageId);
        assertThat(image.contentType()).isEqualTo(ContentType.IMAGE_WEBP);
        assertThat(image.filename()).isEqualTo("test.webp");
        assertThat(imageService.getImageCount()).isEqualTo(6);

        // update existing image
        image = imageService.storeImage(new ImageDto(image.id(), ContentType.IMAGE_SVG, "test.svg"));
        assertThat(image.id()).isEqualTo(imageId);
        assertThat(image.contentType()).isEqualTo(ContentType.IMAGE_SVG);
        assertThat(image.filename()).isEqualTo("test.svg");
        assertThat(imageService.getImageCount()).isEqualTo(6);

        // read the image from the database
        image = imageService.getImage(imageId).orElseThrow();
        assertThat(image.id()).isEqualTo(imageId);
        assertThat(image.contentType()).isEqualTo(ContentType.IMAGE_SVG);
        assertThat(image.filename()).isEqualTo("test.svg");
        assertThat(imageService.getImageCount()).isEqualTo(6);

        // find orphaned images
        final var orphanedImages = imageService.findOrphanedImages();
        assertThat(orphanedImages).hasSize(1);
        assertThat(orphanedImages.getFirst()).isEqualTo(image);

        // delete the existing image
        assertThat(imageService.deleteImage(image)).isTrue();
        assertThat(imageService.getImage(imageId)).isEmpty();
        assertThat(imageService.getImageCount()).isEqualTo(5);

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
        var image = imageService.storeImage(new ImageDto(null, ContentType.IMAGE_WEBP, "test.webp"));
        assertThat(image).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isNotNull();
            assertThat(testee.contentType()).isEqualTo(ContentType.IMAGE_WEBP);
            assertThat(testee.filename()).isEqualTo("test.webp");
        });

        assertThat(imageService.findOrphanedImages()).containsExactly(image);
        imageService.cleanupOrphanedImages();
        assertThat(imageService.findOrphanedImages()).isEmpty();
    }

    @Test
    void deleteUnsavedImageReturnsFalse() {
        final var image = new ImageDto(null, ContentType.IMAGE_WEBP, "test.webp");
        assertThat(imageService.deleteImage(image)).isFalse();
    }

    @Test
    void deleteImageShouldLogErrorWhenFileDeletionFails() {
        // Arrange
        final var image = new ImageDto(UUID.randomUUID(), ContentType.IMAGE_WEBP, "test.webp");
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
