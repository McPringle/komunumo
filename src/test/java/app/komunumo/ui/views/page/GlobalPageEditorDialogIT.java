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
import app.komunumo.data.service.GlobalPageService;
import app.komunumo.ui.IntegrationTest;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static app.komunumo.util.TestUtil.findComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalPageEditorDialogIT extends IntegrationTest {

    @Autowired
    private GlobalPageService globalPageService;

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

            // verify that a context menu is attached to the page content
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

    @Test
    void editGlobalPage_openAndCloseDialog() {
        try {
            final var ui = UI.getCurrent();

            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            // important: navigate after login, so that the Vaadin request is updated
            ui.navigate("page/imprint");

            // verify that the page is loaded correctly
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            assertThat(_get(view, Markdown.class).getContent()).startsWith("## Legal Notice");

            // verify that a context menu is attached to the page content
            final var pageContent = _get(HtmlContainer.class,
                    spec -> spec.withClasses("global-page-content"));
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            assertThat(contextMenu.getTarget()).isSameAs(pageContent);

            // click the "Edit page content" menu item to open the editor dialog
            final var i18nEdit = ui.getTranslation("ui.views.page.GlobalPageView.edit");
            final var editItem = contextMenu.getItems().stream()
                    .filter(menuItem -> i18nEdit.equals(menuItem.getText()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Edit menu item not found"));
            _click(editItem);

            // check that the dialog opens correctly
            final var dialog = _get(GlobalPageEditorDialog.class);
            assertThat(dialog.isOpened()).isTrue();

            // check that the dialog contains the correct initial values
            final var titleField = _get(dialog, TextField.class);
            assertThat(titleField).isNotNull();
            assertThat(titleField.getValue()).isEqualTo("Legal Notice");
            final var textArea = _get(dialog, TextArea.class);
            assertThat(textArea).isNotNull();
            assertThat(textArea.getValue()).startsWith("## Legal Notice");

            // check the buttons: "Cancel" should be enabled, "Save" should be disabled
            final var cancelButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.cancel")));
            assertThat(cancelButton).isNotNull();
            assertThat(cancelButton.isEnabled()).isTrue();
            final var saveButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.save")));
            assertThat(saveButton).isNotNull();
            assertThat(saveButton.isEnabled()).isFalse();

            // click the close button with class name "close-button" to close the dialog without saving
            final var closeButton = _get(dialog, Button.class,
                    spec -> spec.withClasses("close-button"));
            assertThat(closeButton).isNotNull();
            _click(closeButton);

            // check that the dialog is closed
            assertThat(dialog.isOpened()).isFalse();

            // check that the view has not changed
            assertThat(_get(view, Markdown.class).getContent()).startsWith("## Legal Notice");

            logout();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_openAndCancelDialog() {
        try {
            final var ui = UI.getCurrent();

            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            // important: navigate after login, so that the Vaadin request is updated
            ui.navigate("page/imprint");

            // verify that the page is loaded correctly
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            assertThat(_get(view, Markdown.class).getContent()).startsWith("## Legal Notice");

            // verify that a context menu is attached to the page content
            final var pageContent = _get(HtmlContainer.class,
                    spec -> spec.withClasses("global-page-content"));
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            assertThat(contextMenu.getTarget()).isSameAs(pageContent);

            // click the "Edit page content" menu item to open the editor dialog
            final var i18nEdit = ui.getTranslation("ui.views.page.GlobalPageView.edit");
            final var editItem = contextMenu.getItems().stream()
                    .filter(menuItem -> i18nEdit.equals(menuItem.getText()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Edit menu item not found"));
            _click(editItem);

            // check that the dialog opens correctly
            final var dialog = _get(GlobalPageEditorDialog.class);
            assertThat(dialog.isOpened()).isTrue();

            // check that the dialog contains the correct initial values
            final var titleField = _get(dialog, TextField.class);
            assertThat(titleField).isNotNull();
            assertThat(titleField.getValue()).isEqualTo("Legal Notice");
            final var textArea = _get(dialog, TextArea.class);
            assertThat(textArea).isNotNull();
            assertThat(textArea.getValue()).startsWith("## Legal Notice");

            // check the buttons: "Cancel" should be enabled, "Save" should be disabled
            final var cancelButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.cancel")));
            assertThat(cancelButton).isNotNull();
            assertThat(cancelButton.isEnabled()).isTrue();
            final var saveButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.save")));
            assertThat(saveButton).isNotNull();
            assertThat(saveButton.isEnabled()).isFalse();

            // click the "Cancel" button to close the dialog without saving
            _click(cancelButton);

            // check that the dialog is closed
            assertThat(dialog.isOpened()).isFalse();

            // check that the view has not changed
            assertThat(_get(view, Markdown.class).getContent()).startsWith("## Legal Notice");

            logout();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_openEditAndSave() {
        final var ui = UI.getCurrent();
        final var originalPage = globalPageService.getGlobalPage("imprint", ui.getLocale()).orElseThrow();

        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            // important: navigate after login, so that the Vaadin request is updated
            ui.navigate("page/imprint");

            // verify that the page is loaded correctly
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            assertThat(_get(view, Markdown.class).getContent()).startsWith("## Legal Notice");

            // verify that a context menu is attached to the page content
            final var pageContent = _get(HtmlContainer.class,
                    spec -> spec.withClasses("global-page-content"));
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            assertThat(contextMenu.getTarget()).isSameAs(pageContent);

            // click the "Edit page content" menu item to open the editor dialog
            final var i18nEdit = ui.getTranslation("ui.views.page.GlobalPageView.edit");
            final var editItem = contextMenu.getItems().stream()
                    .filter(menuItem -> i18nEdit.equals(menuItem.getText()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Edit menu item not found"));
            _click(editItem);

            // check that the dialog opens correctly
            final var dialog = _get(GlobalPageEditorDialog.class);
            assertThat(dialog.isOpened()).isTrue();

            // check that the dialog contains the correct initial values
            final var titleField = _get(dialog, TextField.class);
            assertThat(titleField).isNotNull();
            assertThat(titleField.getValue()).isEqualTo("Legal Notice");
            final var textArea = _get(dialog, TextArea.class);
            assertThat(textArea).isNotNull();
            assertThat(textArea.getValue()).startsWith("## Legal Notice");

            // check the buttons: "Cancel" should be enabled, "Save" should be disabled
            final var cancelButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.cancel")));
            assertThat(cancelButton).isNotNull();
            assertThat(cancelButton.isEnabled()).isTrue();
            final var saveButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.save")));
            assertThat(saveButton).isNotNull();
            assertThat(saveButton.isEnabled()).isFalse();

            // modify the title and content to enable the "Save" button
            titleField.setValue("New Legal Notice");
            textArea.setValue("## New Legal Notice\n\nThis is the **updated** legal notice.");
            assertThat(saveButton.isEnabled()).isTrue();

            // click the "Save" button to save changes and close the dialog
            _click(saveButton);

            // check that the dialog is closed
            assertThat(dialog.isOpened()).isFalse();

            // check that the view has changed
            assertThat(_get(view, Markdown.class).getContent()).startsWith("## New Legal Notice");

            logout();
        } finally {
            globalPageService.storeGlobalPage(originalPage);
            SecurityContextHolder.clearContext();
        }
    }

}
