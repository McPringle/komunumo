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
package app.komunumo.ui.views.community;

import app.komunumo.ui.KaribuTest;
import app.komunumo.ui.views.events.EventCard;
import app.komunumo.ui.views.events.EventGrid;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.tabs.TabSheet;
import org.junit.jupiter.api.Test;

import static app.komunumo.util.TestUtil.findComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._find;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class CommunityDetailViewKT extends KaribuTest {

    @Test
    void communityWithImage() {
        UI.getCurrent().navigate("communities/@demoCommunity1");

        final var h2 = _get(H2.class, spec -> spec.withClasses("community-name"));
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Demo Community 1");

        final var created = _get(Paragraph.class, spec -> spec.withClasses("community-created"));
        assertThat(created).isNotNull();
        assertThat(created.getText()).isEqualTo("created moments ago");

        final var image = _get(Image.class, spec -> spec.withClasses("community-image"));
        assertThat(image).isNotNull();
        assertThat(image.getSrc()).isNotBlank().startsWith("/images/");
        assertThat(image.getAlt().orElseThrow()).isEqualTo("Profile picture of Demo Community 1");
    }

    @Test
    void communityWithoutImage() {
        UI.getCurrent().navigate("communities/@demoCommunity6");

        final var h2 = _get(H2.class, spec -> spec.withClasses("community-name"));
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Demo Community 6");

        final var image = _find(Image.class, spec -> spec.withClasses("community-image"));
        assertThat(image).isEmpty();
    }

    @Test
    void communityWithoutEventsShown() {
        UI.getCurrent().navigate("communities/@demoCommunity1");

        final var h2 = _get(H2.class, spec -> spec.withClasses("community-name"));
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Demo Community 1");

        final var tabSheet = _get(TabSheet.class);
        final var eventGrids = _find(tabSheet, EventGrid.class);
        assertThat(eventGrids).isEmpty();
        final var eventText = _get(tabSheet, Paragraph.class);
        assertThat(eventText.getText()).isEqualTo("No events are currently planned");
    }

    @Test
    void communityWithFutureEventsShown() {
        UI.getCurrent().navigate("communities/@demoCommunity3");

        final var h2 = _get(H2.class, spec -> spec.withClasses("community-name"));
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Demo Community 3");

        final var eventGrid = _get(EventGrid.class);
        final var eventCards = _find(eventGrid, EventCard.class);
        assertThat(eventCards).hasSize(1);

        final var eventCard = eventCards.getFirst();
        _get(eventCard, Image.class, spec -> spec.withAttribute("alt", "Demo Event 3"));

        final var eventTabs = _get(TabSheet.class);
        assertThat(eventTabs).isNotNull();
        eventTabs.setSelectedIndex(1);

        final var tabLabel = eventTabs.getSelectedTab().getLabel();
        assertThat(tabLabel).isEqualTo("Past Events");

        final var tabContent = _get(eventTabs, Paragraph.class);
        assertThat(tabContent.getText()).isEqualTo("No past events");
    }

    @Test
    void communityWithPastEventsShown() {
        UI.getCurrent().navigate("communities/@demoCommunity1");

        final var h2 = _get(H2.class, spec -> spec.withClasses("community-name"));
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Demo Community 1");

        final var eventTabs = _get(TabSheet.class);
        assertThat(_get(eventTabs, Paragraph.class).getText()).isEqualTo("No events are currently planned");

        eventTabs.setSelectedIndex(1);
        assertThat(eventTabs.getSelectedTab().getLabel()).isEqualTo("Past Events");

        var eventGrid = _get(EventGrid.class);
        var eventCards = _find(eventGrid, EventCard.class);
        assertThat(eventCards).hasSize(1);

        final var eventCard = eventCards.getFirst();
        _get(eventCard, Image.class, spec -> spec.withAttribute("alt", "Demo Event 1"));
    }

    @Test
    void communityEventsTabSwitchMultipleTimesForCoverage() {
        UI.getCurrent().navigate("communities/@demoCommunity1");
        final var eventTabs = _get(TabSheet.class);
        for (int i = 0; i < 3; i++) {
            eventTabs.setSelectedIndex(1);
            eventTabs.setSelectedIndex(0);
        }
    }

    @Test
    void nonExistingCommunity() {
        UI.getCurrent().navigate("communities/@nonExisting");

        final var main = _get(Main.class);
        final var h2 = findComponent(main, H2.class);
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Page not found");
    }

}
