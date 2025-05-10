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
package org.komunumo.ui.component;

import com.vaadin.flow.component.html.H3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.komunumo.ui.KaribuTestBase.findComponent;

class KomunumoCardTest {

    private static class TestCard extends KomunumoCard {
        public TestCard(@NotNull final String title, @Nullable final String imageUrl) {
            super(title, imageUrl);
        }
    }

    @Test
    void cardWithTitleAndImage() {
        final var card = new TestCard("Test Title", "/images/test.jpg");
        assertTrue(card.getClassNames().contains("komunumo-card"));

        final var bg = card.getStyle().get("background-image");
        assertEquals("url('/images/test.jpg')", bg);

        final var h3 = findComponent(card, H3.class);
        assertNotNull(h3);
        assertEquals("Test Title", h3.getText());
    }

    @Test
    void cardWithTitleWithoutImage() {
        final var card = new TestCard("No Image", null);
        assertNull(card.getStyle().get("background-image"));
    }

}
