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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Test
    void happyCase() {
        var image = imageService.storeImage(new ImageDto(null, ContentType.IMAGE_WEBP, "test.webp"));
        assertNotNull(image.id());

        final var orphanedImages = imageService.findOrphanedImages().toList();
        assertEquals(1, orphanedImages.size());
        assertEquals(image, orphanedImages.getFirst());

        assertTrue(imageService.deleteImage(image));
        assertFalse(imageService.deleteImage(image));

        assertTrue(imageService.findOrphanedImages().toList().isEmpty());
    }
}
