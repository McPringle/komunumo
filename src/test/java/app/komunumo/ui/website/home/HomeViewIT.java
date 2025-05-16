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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import org.junit.jupiter.api.Test;
import app.komunumo.ui.KaribuTestBase;
import app.komunumo.ui.component.CommunityGrid;

import java.util.Objects;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;
import static app.komunumo.util.TestUtil.findComponent;

class HomeViewIT extends KaribuTestBase {

    @Test
    void homeViewTest() {
        UI.getCurrent().navigate(HomeView.class);

        assertThat(_get(H1.class).getText()).isEqualTo("Komunumo");
        assertThat(_get(H2.class).getText()).isEqualTo("Open Source Community Management");

        final var communityList = _get(CommunityGrid.class);
        final var communityCards = communityList.getChildren().toList();
        assertThat(communityCards).hasSize(6);

        for (int i = 1; i <= 6; i++) {
            final var communityCard = communityCards.get(i - 1);
            final var h3 = Objects.requireNonNull(findComponent(communityCard, H3.class));
            assertThat(h3.getText()).isEqualTo("Demo Community " + i);

            final var backgroundImage = communityCard.getElement().getStyle().get("background-image");
            if (i <= 5) {
                assertThat(backgroundImage)
                        .as("background-image should be set")
                        .isNotNull();
                assertThat(backgroundImage)
                        .as("expected to contain a demo background but was: " + backgroundImage)
                        .contains(i + ".jpg");
            } else { // demo community 6+ has no image
                assertThat(backgroundImage)
                        .as("there should be no background-image")
                        .isNull();
            }
        }
    }

}
