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
package org.komunumo.data.service;

import org.junit.jupiter.api.Test;
import org.komunumo.data.dto.ContentType;
import org.komunumo.data.dto.ImageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Test
    void happyCase() {
        assertEquals(5, imageService.getImageCount());

        // store new image
        var image = imageService.storeImage(new ImageDto(null, ContentType.IMAGE_WEBP, "test.webp"));
        final var imageId = image.id();
        assertNotNull(imageId);
        assertEquals(ContentType.IMAGE_WEBP, image.contentType());
        assertEquals("test.webp", image.filename());
        assertEquals(6, imageService.getImageCount());

        // read the image from the database
        image = imageService.getImage(imageId).orElseThrow();
        assertEquals(imageId, image.id());
        assertEquals(ContentType.IMAGE_WEBP, image.contentType());
        assertEquals("test.webp", image.filename());
        assertEquals(6, imageService.getImageCount());

        // update existing image
        image = imageService.storeImage(new ImageDto(image.id(), ContentType.IMAGE_SVG, "test.svg"));
        assertEquals(imageId, image.id());
        assertEquals(ContentType.IMAGE_SVG, image.contentType());
        assertEquals("test.svg", image.filename());
        assertEquals(6, imageService.getImageCount());

        // read the image from the database
        image = imageService.getImage(imageId).orElseThrow();
        assertEquals(imageId, image.id());
        assertEquals(ContentType.IMAGE_SVG, image.contentType());
        assertEquals("test.svg", image.filename());
        assertEquals(6, imageService.getImageCount());

        // find orphaned images
        final var orphanedImages = imageService.findOrphanedImages().toList();
        assertEquals(1, orphanedImages.size());
        assertEquals(image, orphanedImages.getFirst());

        // delete the existing image
        assertTrue(imageService.deleteImage(image));
        assertTrue(imageService.getImage(imageId).isEmpty());
        assertEquals(5, imageService.getImageCount());

        // find orphaned images again
        assertTrue(imageService.findOrphanedImages().toList().isEmpty());

        // delete the non-existing image (was already deleted before)
        assertFalse(imageService.deleteImage(image));
    }

    @Test
    void noImageWithNullId() {
        assertTrue(imageService.getImage(null).isEmpty());
    }
}
