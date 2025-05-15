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

import static org.assertj.core.api.Assertions.assertThat;
import static org.komunumo.util.TestUtil.findComponent;

class KomunumoCardTest {

    private static class TestCard extends KomunumoCard {
        public TestCard(final @NotNull String title, final @Nullable String imageUrl) {
            super(title, imageUrl);
        }
    }

    @Test
    void cardWithTitleAndImage() {
        final var card = new TestCard("Test Title", "/images/test.jpg");
        assertThat(card.getClassNames()).contains("komunumo-card");

        final var h3 = findComponent(card, H3.class);
        assertThat(h3).isNotNull();
        assertThat(h3.getText()).isEqualTo("Test Title");

        final var bg = card.getStyle().get("background-image");
        assertThat(bg).isEqualTo("url('/images/test.jpg')");
    }

    @Test
    void cardWithTitleWithoutImage() {
        final var card = new TestCard("No Image", null);
        assertThat(card.getClassNames()).contains("komunumo-card");

        final var h3 = findComponent(card, H3.class);
        assertThat(h3).isNotNull();
        assertThat(h3.getText()).isEqualTo("No Image");

        final var bg = card.getStyle().get("background-image");
        assertThat(bg).isNull();
    }

}
