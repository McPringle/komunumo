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
package org.komunumo;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.komunumo.data.dto.ContentType;
import org.komunumo.data.dto.ImageDto;
import org.komunumo.data.service.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationSchedulerTest {

    @Autowired
    private @NotNull ServiceProvider serviceProvider;

    @Test
    void cleanupOrphanedImages() {
        final var imageService = serviceProvider.imageService();

        var image = imageService.storeImage(new ImageDto(null, ContentType.IMAGE_WEBP, "test.webp"));
        assertThat(image).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isNotNull();
            assertThat(testee.contentType()).isEqualTo(ContentType.IMAGE_WEBP);
            assertThat(testee.filename()).isEqualTo("test.webp");
        });

        assertThat(imageService.findOrphanedImages().toList()).containsExactly(image);

        new ApplicationScheduler(serviceProvider).cleanupOrphanedImages();
        assertThat(imageService.findOrphanedImages().toList()).isEmpty();
    }

}
