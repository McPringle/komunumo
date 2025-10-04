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

import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.IntegrationTest;
import nl.altindag.log.LogCaptor;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DemoDataCreatorIT extends IntegrationTest {

    @Autowired
    private ServiceProvider serviceProvider;

    @Autowired
    private @NotNull DemoDataCreator demoDataCreator;

    @Test
    void resetDemoData() {
        assertDemoDataCount();
        demoDataCreator.resetDemoData();
        assertDemoDataCount();
    }

    private void assertDemoDataCount() {
        Assertions.assertThat(serviceProvider.communityService().getCommunityCount())
                .isEqualTo(6);
        Assertions.assertThat(serviceProvider.eventService().getEventCount())
                .isEqualTo(6);
        Assertions.assertThat(serviceProvider.imageService().getImageCount())
                .isEqualTo(10);
        Assertions.assertThat(serviceProvider.globalPageService().getGlobalPageCount())
                .isEqualTo(2);
    }

    @Test
    void storeDemoImageWithWarning() {
        try (var logCaptor = LogCaptor.forClass(DemoDataCreator.class)) {
            final var filename = "non-existing.gif";
            final var image = new ImageDto(null, ContentType.IMAGE_GIF);
            demoDataCreator.storeDemoImage(image, filename);
            Assertions.assertThat(logCaptor.getWarnLogs()).containsExactly("Demo image not found: non-existing.gif");
        }
    }

}
