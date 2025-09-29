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

import app.komunumo.data.dto.GlobalPageDto;
import app.komunumo.data.service.GlobalPageService;
import app.komunumo.ui.components.PersistentNotification;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GlobalPageEditorDialog extends Dialog {

    private final @NotNull GlobalPageService globalPageService;
    private final @NotNull GlobalPageDto globalPage;

    private final @NotNull TextArea pageEditor;
    private final @NotNull Markdown pagePreview;

    private final @NotNull Button saveButton;

    public GlobalPageEditorDialog(final @NotNull GlobalPageService globalPageService,
                                  final @NotNull GlobalPageDto globalPage) {
        super();
        this.globalPageService = globalPageService;
        this.globalPage = globalPage;

        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        addClassName("global-page-editor-dialog");
        setHeaderTitle(getTranslation("ui.views.page.GlobalPageEditorDialog.title"));

        final var closeButton = new Button(new Icon("lumo", "cross"), this::cancel);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        // TextArea for editing markdown
        pageEditor = new TextArea();
        pageEditor.setValue(globalPage.markdown());
        pageEditor.setValueChangeMode(ValueChangeMode.EAGER);
        pageEditor.setSizeFull();

        // Markdown component for preview
        pagePreview = new Markdown();

        // Tab sheet to switch between edit and preview
        final var tabSheet = new TabSheet();
        tabSheet.add(getTranslation("ui.views.page.GlobalPageEditorDialog.edit"), pageEditor);
        tabSheet.add(getTranslation("ui.views.page.GlobalPageEditorDialog.preview"), pagePreview);
        tabSheet.setSizeFull();
        add(tabSheet);

        // Update preview when text area changes
        tabSheet.addSelectedChangeListener(event -> {
            final var previewLabelText = getTranslation("ui.views.page.GlobalPageEditorDialog.preview");
            if (event.getSelectedTab().getLabel().equals(previewLabelText)) {
                pagePreview.setContent(pageEditor.getValue());
            }
        });

        // Add cancel and save buttons to footer
        final var footer = getFooter();
        final var cancelButton = new Button(getTranslation("ui.views.page.GlobalPageEditorDialog.cancel"), this::cancel);
        footer.add(cancelButton);
        saveButton = new Button(getTranslation("ui.views.page.GlobalPageEditorDialog.save"), this::save);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);
        footer.add(saveButton);

        // Enable save button only if page was modified
        pageEditor.addValueChangeListener(event -> {
            if (!saveButton.isEnabled()) {
                saveButton.setEnabled(true);
            }
        });
    }

    private void save(final @Nullable ClickEvent<Button> buttonClickEvent) {
        if (globalPageService.updateGlobalPage(globalPage, pageEditor.getValue())) {
            saveButton.setEnabled(false);
            close();
            UI.getCurrent().refreshCurrentRoute(false);
        } else {
            final var message = getTranslation("ui.views.page.GlobalPageEditorDialog.saveError");
            final var notification = new PersistentNotification(message);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            notification.open();
        }
    }

    private void cancel(final @Nullable ClickEvent<Button> buttonClickEvent) {
        if (saveButton.isEnabled()) {
            final var confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader(getTranslation("ui.views.page.GlobalPageEditorDialog.unsavedChanges.title"));
            confirmDialog.setText(getTranslation("ui.views.page.GlobalPageEditorDialog.unsavedChanges.text"));

            confirmDialog.setCancelable(true);
            confirmDialog.setCancelText(getTranslation("ui.views.page.GlobalPageEditorDialog.unsavedChanges.keepEditing"));
            confirmDialog.addCancelListener(event -> confirmDialog.close());

            confirmDialog.setRejectable(true);
            confirmDialog.setRejectText(getTranslation("ui.views.page.GlobalPageEditorDialog.unsavedChanges.discard"));
            confirmDialog.addRejectListener(event -> {
                confirmDialog.close();
                close();
            });

            confirmDialog.setConfirmText(getTranslation("ui.views.page.GlobalPageEditorDialog.unsavedChanges.save"));
            confirmDialog.addConfirmListener(event -> save(null));

            confirmDialog.open();
        } else {
            close();
        }
    }

}
