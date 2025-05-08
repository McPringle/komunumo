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
package org.komunumo.ui.website.home;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.junit.jupiter.api.Test;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.KaribuTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeViewIT extends KaribuTestBase {

    @Autowired
    private DatabaseService databaseService;

    @Test
    void homeViewTest() {
        UI.getCurrent().navigate(HomeView.class);

        final var title = _get(H2.class, spec -> spec.withText("Home")).getText();
        assertEquals("Home", title);

        final var communityList = _get(HorizontalLayout.class);
        final var communityCards = communityList.getChildren().toList();
        assertEquals(6, communityCards.size());

        for (int i = 1; i <= 6; i++) {
            final var communityCard = communityCards.get(i - 1);
            final var h3 = Objects.requireNonNull(findComponent(communityCard, H3.class));
            assertTrue(h3.getText().startsWith("Demo Community " + i));

            final var backgroundImage = communityCard.getElement().getStyle().get("background-image");
            assertNotNull(backgroundImage, "background-image should be set");
            if (i <= 5) {
                assertTrue(backgroundImage.contains("demo-background-" + i + ".jpg"),
                        "expected to contain a demo background but was: " + backgroundImage);
            } else { // demo community 6+ has no image
                assertTrue(backgroundImage.contains("placeholder-400x300.jpg"),
                        "expected to contain the placeholder background but was: " + backgroundImage);
            }
        }
    }

}
