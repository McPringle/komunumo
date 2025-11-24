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
package app.komunumo.domain.page.boundary;

import app.komunumo.domain.page.entity.GlobalPageDto;
import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.domain.page.control.GlobalPageService;
import app.komunumo.test.KaribuTest;
import app.komunumo.ui.components.PersistentNotification;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Locale;

import static app.komunumo.test.TestUtil.findComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._assertNone;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.vaadin.flow.component.ComponentUtil.fireEvent;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalPageEditorDialogKT extends KaribuTest {

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

            // verify that a context menu is attached to the markdown content
            final var pageContent = _get(HtmlContainer.class,
                    spec -> spec.withClasses("global-page-content"));
            final var markdownContent = _get(pageContent, Markdown.class);
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            assertThat(contextMenu.getTarget()).isSameAs(markdownContent);

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

            // verify that a context menu is attached to the markdown content
            final var pageContent = _get(HtmlContainer.class,
                    spec -> spec.withClasses("global-page-content"));
            final var markdownContent = _get(pageContent, Markdown.class);
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            assertThat(contextMenu.getTarget()).isSameAs(markdownContent);

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

            // verify that a context menu is attached to the markdown content
            final var pageContent = _get(HtmlContainer.class,
                    spec -> spec.withClasses("global-page-content"));
            final var markdownContent = _get(pageContent, Markdown.class);
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            assertThat(contextMenu.getTarget()).isSameAs(markdownContent);

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

            // verify that a context menu is attached to the markdown content
            final var pageContent = _get(HtmlContainer.class,
                    spec -> spec.withClasses("global-page-content"));
            final var markdownContent = _get(pageContent, Markdown.class);
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            assertThat(contextMenu.getTarget()).isSameAs(markdownContent);

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
        } finally {
            globalPageService.storeGlobalPage(originalPage);
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_confirmDialog_keepEditing() {
        final var ui = UI.getCurrent();

        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            // important: navigate after login, so that the Vaadin request is updated
            ui.navigate("page/imprint");

            // start editing the page
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            final var i18nEdit = ui.getTranslation("ui.views.page.GlobalPageView.edit");
            final var editItem = contextMenu.getItems().stream()
                    .filter(menuItem -> i18nEdit.equals(menuItem.getText()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Edit menu item not found"));
            _click(editItem);

            // change the title and content
            final var dialog = _get(GlobalPageEditorDialog.class);
            final var titleField = _get(dialog, TextField.class);
            final var textArea = _get(dialog, TextArea.class);
            titleField.setValue("New Legal Notice");
            textArea.setValue("## New Legal Notice\n\nThis is the **updated** legal notice.");

            // click the cancel button
            final var cancelButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.cancel")));
            _click(cancelButton);

            // check that the confirmation dialog appears
            final var confirmDialog = _get(ConfirmDialog.class);
            assertThat(confirmDialog.isOpened()).isTrue();

            // keep editing by firing a CancelEvent on the confirmation dialog
            fireEvent(confirmDialog, new ConfirmDialog.CancelEvent(confirmDialog, true));

            // check that the confirmation dialog is closed and the editor dialog is still open
            assertThat(confirmDialog.isOpened()).isFalse();
            assertThat(dialog.isOpened()).isTrue();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_confirmDialog_discard() {
        final var ui = UI.getCurrent();

        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            // important: navigate after login, so that the Vaadin request is updated
            ui.navigate("page/imprint");

            // start editing the page
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            final var i18nEdit = ui.getTranslation("ui.views.page.GlobalPageView.edit");
            final var editItem = contextMenu.getItems().stream()
                    .filter(menuItem -> i18nEdit.equals(menuItem.getText()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Edit menu item not found"));
            _click(editItem);

            // change the title and content
            final var dialog = _get(GlobalPageEditorDialog.class);
            final var titleField = _get(dialog, TextField.class);
            final var textArea = _get(dialog, TextArea.class);
            titleField.setValue("New Legal Notice");
            textArea.setValue("## New Legal Notice\n\nThis is the **updated** legal notice.");

            // click the cancel button
            final var cancelButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.cancel")));
            _click(cancelButton);

            // check that the confirmation dialog appears
            final var confirmDialog = _get(ConfirmDialog.class);
            assertThat(confirmDialog.isOpened()).isTrue();

            // discard by firing a RejectEvent on the confirmation dialog
            fireEvent(confirmDialog, new ConfirmDialog.RejectEvent(confirmDialog, true));

            // check that the confirmation dialog and the editor dialog are closed
            assertThat(confirmDialog.isOpened()).isFalse();
            assertThat(dialog.isOpened()).isFalse();

            // check that the view has not changed
            assertThat(_get(view, Markdown.class).getContent()).startsWith("## Legal Notice");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_confirmDialog_save() {
        final var ui = UI.getCurrent();
        final var originalPage = globalPageService.getGlobalPage("imprint", ui.getLocale()).orElseThrow();

        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            // important: navigate after login, so that the Vaadin request is updated
            ui.navigate("page/imprint");

            // start editing the page
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            final var i18nEdit = ui.getTranslation("ui.views.page.GlobalPageView.edit");
            final var editItem = contextMenu.getItems().stream()
                    .filter(menuItem -> i18nEdit.equals(menuItem.getText()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Edit menu item not found"));
            _click(editItem);

            // change the title and content
            final var dialog = _get(GlobalPageEditorDialog.class);
            final var titleField = _get(dialog, TextField.class);
            final var textArea = _get(dialog, TextArea.class);
            titleField.setValue("New Legal Notice");
            textArea.setValue("## New Legal Notice\n\nThis is the **updated** legal notice.");

            // click the cancel button
            final var cancelButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.cancel")));
            _click(cancelButton);

            // check that the confirmation dialog appears
            final var confirmDialog = _get(ConfirmDialog.class);
            assertThat(confirmDialog.isOpened()).isTrue();

            // save by firing a ConfirmEvent on the confirmation dialog
            fireEvent(confirmDialog, new ConfirmDialog.ConfirmEvent(confirmDialog, true));

            // check that the confirmation dialog and the editor dialog are closed
            assertThat(confirmDialog.isOpened()).isFalse();
            assertThat(dialog.isOpened()).isFalse();

            // check that the view has not changed
            assertThat(_get(view, Markdown.class).getContent()).startsWith("## New Legal Notice");
        } finally {
            globalPageService.storeGlobalPage(originalPage);
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_openEditTimeoutSaveError() {
        final var ui = UI.getCurrent();

        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            // important: navigate after login, so that the Vaadin request is updated
            ui.navigate("page/imprint");

            // start editing the page
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            final var i18nEdit = ui.getTranslation("ui.views.page.GlobalPageView.edit");
            final var editItem = contextMenu.getItems().stream()
                    .filter(menuItem -> i18nEdit.equals(menuItem.getText()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Edit menu item not found"));
            _click(editItem);

            // change the title and content
            final var dialog = _get(GlobalPageEditorDialog.class);
            final var titleField = _get(dialog, TextField.class);
            final var textArea = _get(dialog, TextArea.class);
            titleField.setValue("New Legal Notice");
            textArea.setValue("## New Legal Notice\n\nThis is the **updated** legal notice.");

            // clear the security context to simulate session expiration or logout
            SecurityContextHolder.clearContext();

            // try to save the changes
            final var saveButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.save")));
            assertThat(saveButton.isEnabled()).isTrue();
            _click(saveButton);

            // check that an error notification is shown
            final var notification = _get(PersistentNotification.class);
            assertThat(notification.isOpened()).isTrue();

            // check that the notification contains the correct error message
            final var div = _get(notification, Div.class);
            assertThat(div.getText()).startsWith("You do not have permission to edit the content of this page.");

            // close the notification
            final var notificationCloseButton = _get(notification, Button.class);
            _click(notificationCloseButton);
            MockVaadin.clientRoundtrip();
            assertThat(notification.isOpened()).isFalse();
            _assertNone(PersistentNotification.class);

            // check that the dialog is still open
            assertThat(dialog.isOpened()).isTrue();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_openEditSaveError() {
        GlobalPageDto testPage = null;
        try {
            // login as admin
            final var testUser = getTestUser(UserRole.ADMIN);
            login(testUser);

            final var ui = UI.getCurrent();
            testPage = globalPageService.storeGlobalPage(new GlobalPageDto(
                    "test", Locale.ENGLISH, null, null, "Test Page", "## Test Page"));

            // important: navigate after login, so that the Vaadin request is updated
            ui.navigate("page/test");

            // start editing the page
            final var view = _get(GlobalPageView.class,
                    spec -> spec.withClasses("global-page-view"));
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            final var i18nEdit = ui.getTranslation("ui.views.page.GlobalPageView.edit");
            final var editItem = contextMenu.getItems().stream()
                    .filter(menuItem -> i18nEdit.equals(menuItem.getText()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Edit menu item not found"));
            _click(editItem);

            // change the title and content
            final var dialog = _get(GlobalPageEditorDialog.class);
            final var titleField = _get(dialog, TextField.class);
            final var textArea = _get(dialog, TextArea.class);
            titleField.setValue("New Test Page");
            textArea.setValue("## New Test Page\n\nThis is the **updated** test page.");

            // delete the page to simulate a save error
            globalPageService.deleteGlobalPage(testPage);

            // try to save the changes
            final var saveButton = _get(dialog, Button.class, spec -> spec.withText(
                    ui.getTranslation("ui.views.page.GlobalPageEditorDialog.save")));
            assertThat(saveButton.isEnabled()).isTrue();
            _click(saveButton);

            // check that an error notification is shown
            final var notification = _get(PersistentNotification.class);
            assertThat(notification.isOpened()).isTrue();

            // check that the notification contains the correct error message
            final var div = _get(notification, Div.class);
            assertThat(div.getText()).startsWith("An error occurred while saving the page content.");

            // close the notification
            final var notificationCloseButton = _get(notification, Button.class);
            _click(notificationCloseButton);
            MockVaadin.clientRoundtrip();
            assertThat(notification.isOpened()).isFalse();
            _assertNone(PersistentNotification.class);

            // check that the dialog is still open
            assertThat(dialog.isOpened()).isTrue();
        } finally {
            if (testPage != null) {
                globalPageService.deleteGlobalPage(testPage);
            }
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void editGlobalPage_checkPreview() {
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

            // verify that a context menu is attached to the markdown content
            final var pageContent = _get(HtmlContainer.class,
                    spec -> spec.withClasses("global-page-content"));
            final var markdownContent = _get(pageContent, Markdown.class);
            final var contextMenu = view.getContextMenu();
            assertThat(contextMenu).isNotNull();
            assertThat(contextMenu.getTarget()).isSameAs(markdownContent);

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

            // switch to the preview tab
            final var tabSheet = _get(dialog, TabSheet.class);
            assertThat(tabSheet).isNotNull();
            tabSheet.setSelectedIndex(1);

            // check that the preview has the correct initial content
            final var markdown = findComponent(dialog, Markdown.class);
            assertThat(markdown).isNotNull();
            assertThat(markdown.getContent()).startsWith("## Legal Notice");

            // switch back to the editor tab
            tabSheet.setSelectedIndex(0);

            // modify the title and content
            titleField.setValue("New Legal Notice");
            textArea.setValue("## New Legal Notice\n\nThis is the **updated** legal notice.");

            // switch to the preview tab again
            tabSheet.setSelectedIndex(1);

            // check that the preview has updated content
            assertThat(markdown.getContent()).startsWith("## New Legal Notice");
        } finally {
            globalPageService.storeGlobalPage(originalPage);
            SecurityContextHolder.clearContext();
        }
    }

}
