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

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import org.komunumo.data.dto.ContentType;
import org.komunumo.data.dto.ImageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DemoDataServiceTest {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private DemoDataService demoDataService;

    @Test
    void createDemoData() {
        var communityCount = databaseService.getCommunities().count();
        assertThat(communityCount).isEqualTo(6);

        // should not create new data because it was already executed using the `@PostConstruct` method
        demoDataService.createDemoData();

        communityCount = databaseService.getCommunities().count();
        assertThat(communityCount).isEqualTo(6);
    }

    @Test
    void storeDemoImageWithWarning() {
        try (var logCaptor = LogCaptor.forClass(DemoDataService.class)) {
            final var filename = "non-existing.gif";
            final var image = new ImageDto(null, ContentType.IMAGE_GIF, filename);
            demoDataService.storeDemoImage(image, filename);
            assertThat(logCaptor.getWarnLogs()).containsExactly("Demo image not found: non-existing.gif");
        }
    }

}
