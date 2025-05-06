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
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import org.junit.jupiter.api.Test;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.KaribuTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeViewIT extends KaribuTestBase {

    @Autowired
    private DatabaseService databaseService;

    @Test
    void homeViewTest() {
        UI.getCurrent().navigate(HomeView.class);
        UI.getCurrent().getPage().reload();

        final var title = _get(H2.class, spec -> spec.withText("Home")).getText();
        assertEquals("Home", title);

        final var communityList = _get(UnorderedList.class);
        assertEquals(5, communityList.getChildren().count());
        for (int i = 1; i <= 5; i++) {
            final var communityName = "Demo Community " + i;
            final var communityItem = _get(ListItem.class, spec -> spec.withText(communityName));
            assertEquals(communityName, communityItem.getText());
        }
    }

}
