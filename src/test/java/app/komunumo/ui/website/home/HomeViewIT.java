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
package app.komunumo.ui.website.home;

import app.komunumo.ui.IntegrationTest;
import app.komunumo.ui.component.CommunityGrid;
import app.komunumo.ui.component.EventGrid;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static app.komunumo.util.TestUtil.findComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._find;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class HomeViewIT extends IntegrationTest {

    @Test
    void homeViewHasTitle() {
        UI.getCurrent().navigate(HomeView.class);
        assertThat(_get(H1.class).getText()).isEqualTo("Komunumo");
        assertThat(_get(H2.class).getText()).isEqualTo("Open Source Community Management");
    }

    @Test
    void checkUpcomingEvents() {
        final var ui = UI.getCurrent();
        ui.navigate(HomeView.class);

        final var eventGrid = _get(EventGrid.class);
        final var eventCards = eventGrid.getChildren().toList();
        assertThat(eventCards).hasSize(3);

        for (var entry : Map.of(
                0, 3,
                1, 5,
                2, 6) // position and event number
                .entrySet()) {
            final var position = entry.getKey();
            final var number = entry.getValue();
            final var eventCard = eventCards.get(position);
            final var h3 = Objects.requireNonNull(findComponent(eventCard, H3.class));
            assertThat(h3.getText()).isEqualTo("Demo Event " + number);

            final var backgroundImage = eventCard.getElement().getStyle().get("background-image");
            if (number <= 5) {
                assertThat(backgroundImage)
                        .as("background-image should be set")
                        .isNotNull();
                assertThat(backgroundImage)
                        .as("expected to contain a demo background but was: " + backgroundImage)
                        .startsWith("url('")
                        .endsWith(".jpg')");
            } else { // demo community 6+ has no image
                assertThat(backgroundImage)
                        .as("there should be no background-image")
                        .isNull();
            }
        }

        final var communityGrid = _find(CommunityGrid.class);
        assertThat(communityGrid).isEmpty();
    }

}
