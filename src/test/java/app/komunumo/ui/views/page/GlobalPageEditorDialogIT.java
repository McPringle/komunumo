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
package app.komunumo.ui.views.page;

import app.komunumo.data.dto.UserRole;
import app.komunumo.ui.IntegrationTest;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.markdown.Markdown;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import static app.komunumo.util.TestUtil.findComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalPageEditorDialogIT extends IntegrationTest {

    @Test
    void editGlobalPage_notForVisitors() {
        try {
            // make sure no user is logged in
            SecurityContextHolder.clearContext();
            UI.getCurrent().navigate("page/imprint");

            // verify that the page is loaded correctly
            final var view = _get(GlobalPageView.class);
            final var markdown = findComponent(view, Markdown.class);
            assertThat(markdown).isNotNull();
            assertThat(markdown.getContent()).startsWith("## Legal Notice");

            // verify that no context menu is attached to the page content
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNull();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_notForUsers() {
        try {
            // login as regular user
            final var testUser = getTestUser(UserRole.USER);
            login(testUser);

            // important: navigate after login, so that the Vaadin request is updated
            UI.getCurrent().navigate("page/imprint");

            // verify that the page is loaded correctly
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            final var markdown = findComponent(view, Markdown.class);
            assertThat(markdown).isNotNull();
            assertThat(markdown.getContent()).startsWith("## Legal Notice");

            // verify that no context menu is attached to the page content
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNull();

            logout();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_onlyForAdmins() {
        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            // important: navigate after login, so that the Vaadin request is updated
            UI.getCurrent().navigate("page/imprint");

            // verify that the page is loaded correctly
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            final var markdown = findComponent(view, Markdown.class);
            assertThat(markdown).isNotNull();
            assertThat(markdown.getContent()).startsWith("## Legal Notice");

            // verify that no context menu is attached to the page content
            final var pageContent = _get(HtmlContainer.class,
                    spec -> spec.withClasses("global-page-content"));
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            assertThat(contextMenu.getTarget()).isSameAs(pageContent);

            logout();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

}
