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
package org.komunumo.ui.website;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.Test;
import org.komunumo.ui.KaribuTestBase;
import org.komunumo.ui.website.home.HomeView;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebsiteLayoutTest extends KaribuTestBase {

    @Test
    void testRouterLayoutContent() {
        UI.getCurrent().navigate(HomeView.class);
        final var uiParent = UI.getCurrent().getCurrentView().getParent().orElseThrow()
                .getParent().orElseThrow();

        final var websiteLayout = (WebsiteLayout) uiParent;
        assertThat(websiteLayout.getComponentCount()).isEqualTo(1);

        final var main = (Main) websiteLayout.getComponentAt(0);
        assertThat(main.getElement().getChildCount()).isEqualTo(1);

        websiteLayout.showRouterLayoutContent(new Paragraph("foo"));
        assertThat(main.getElement().getChildCount()).isEqualTo(1);
        assertThat(main.getElement().getChild(0).getText()).isEqualTo("foo");

        websiteLayout.showRouterLayoutContent(new Paragraph("bar"));
        assertThat(main.getElement().getChildCount()).isEqualTo(1);
        assertThat(main.getElement().getChild(0).getText()).isEqualTo("bar");

        websiteLayout.removeRouterLayoutContent(null);
        assertThat(main.getElement().getChildCount()).isZero();
    }

    @Test
    void testRouterLayoutContentException() {
        UI.getCurrent().navigate(HomeView.class);
        final var uiParent = UI.getCurrent().getCurrentView().getParent().orElseThrow()
                .getParent().orElseThrow();

        final var websiteLayout = (WebsiteLayout) uiParent;
        assertThat(websiteLayout.getComponentCount()).isEqualTo(1);

        final var content = mock(HasElement.class);
        final var element = mock(Element.class);
        when(content.getElement()).thenReturn(element);
        when(element.getComponent()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> websiteLayout.showRouterLayoutContent(content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("WebsiteLayout content must be a Component");
    }

}
