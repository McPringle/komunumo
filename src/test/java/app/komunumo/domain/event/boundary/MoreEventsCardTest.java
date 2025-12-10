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
package app.komunumo.domain.event.boundary;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H3;

class MoreEventsCardTest {

    private UI ui;

    @BeforeEach
    void setUp() {
        ui = new UI();
        ui.setLocale(Locale.ENGLISH);
        UI.setCurrent(ui);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void shouldInitializeCardWithCorrectClassName() {
        // Act
        var card = new MoreEventsCard();

        // Assert
        assertThat(card.getClassNames()).contains("more-events-card", "komunumo-card");
    }

    @Test
    void shouldDisplayTitleElement() {
        // Act
        var card = new MoreEventsCard();

        // Assert
        var children = card.getChildren().toList();
        assertThat(children).isNotEmpty();

        var titleElement = children.stream()
                .filter(component -> component instanceof H3)
                .map(component -> (H3) component)
                .findFirst();

        assertThat(titleElement).isPresent();
        assertThat(titleElement.orElseThrow().getText()).isNotEmpty();
    }
}
