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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebsiteLayoutTest extends KaribuTestBase {

    @Test
    void testRouterLayoutContent() {
        UI.getCurrent().navigate(HomeView.class);
        final var uiParent = UI.getCurrent().getCurrentView().getParent().orElseThrow()
                .getParent().orElseThrow();

        final var websiteLayout = (WebsiteLayout) uiParent;
        assertEquals(1, websiteLayout.getComponentCount());

        final var main = (Main) websiteLayout.getComponentAt(0);
        assertEquals(1, main.getElement().getChildCount());

        websiteLayout.showRouterLayoutContent(new Paragraph("foo"));
        assertEquals(1, main.getElement().getChildCount());
        assertEquals("foo", main.getElement().getChild(0).getText());

        websiteLayout.showRouterLayoutContent(new Paragraph("bar"));
        assertEquals(1, main.getElement().getChildCount());
        assertEquals("bar", main.getElement().getChild(0).getText());

        websiteLayout.removeRouterLayoutContent(null);
        assertEquals(0, main.getElement().getChildCount());
    }

    @Test
    void testRouterLayoutContentException() {
        UI.getCurrent().navigate(HomeView.class);
        final var uiParent = UI.getCurrent().getCurrentView().getParent().orElseThrow()
                .getParent().orElseThrow();

        final var websiteLayout = (WebsiteLayout) uiParent;
        assertEquals(1, websiteLayout.getComponentCount());

        final var content = mock(HasElement.class);
        final var element = mock(Element.class);
        when(content.getElement()).thenReturn(element);
        when(element.getComponent()).thenReturn(Optional.empty());

        final var exception = assertThrows(IllegalArgumentException.class,
                () -> websiteLayout.showRouterLayoutContent(content));
        assertEquals("WebsiteLayout content must be a Component", exception.getMessage());
    }
}
