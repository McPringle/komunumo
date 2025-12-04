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

import app.komunumo.domain.page.control.GlobalPageService;
import app.komunumo.domain.page.entity.GlobalPageDto;
import app.komunumo.util.SecurityUtil;
import app.komunumo.vaadin.components.MarkdownEditor;
import app.komunumo.vaadin.components.PersistentNotification;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class GlobalPageEditorDialog extends Dialog {

    private final @NotNull GlobalPageService globalPageService;
    private final @NotNull GlobalPageDto globalPage;
    private final @NotNull Consumer<GlobalPageDto> onSavedCallback;

    private final @NotNull TextField pageTitle;
    private final @NotNull MarkdownEditor pageEditor;

    private final @NotNull Button saveButton;

    public GlobalPageEditorDialog(final @NotNull GlobalPageService globalPageService,
                                  final @NotNull GlobalPageDto globalPage,
                                  final @NotNull Consumer<GlobalPageDto> onSavedCallback) {
        super();
        this.globalPageService = globalPageService;
        this.globalPage = globalPage;
        this.onSavedCallback = onSavedCallback;

        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        addClassName("global-page-editor-dialog");
        setHeaderTitle(getTranslation("page.boundary.GlobalPageEditorDialog.title"));

        final var closeButton = new Button(new Icon("lumo", "cross"), this::cancel);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClassName("close-button");
        getHeader().add(closeButton);

        // TextField for editing title
        pageTitle = new TextField();
        pageTitle.setPlaceholder(getTranslation("page.boundary.GlobalPageEditorDialog.pageTitle"));
        pageTitle.setValue(globalPage.title());
        pageTitle.setValueChangeMode(ValueChangeMode.EAGER);
        pageTitle.setWidthFull();
        add(pageTitle);

        // MarkdownEditor for editing markdown
        pageEditor = new MarkdownEditor(getLocale());
        pageEditor.setPlaceholder(getTranslation("page.boundary.GlobalPageEditorDialog.pageEditor"));
        pageEditor.setValue(globalPage.markdown());
        pageEditor.setValueChangeMode(ValueChangeMode.EAGER);
        pageEditor.setSizeFull();
        add(pageEditor);

        // Add cancel and save buttons to footer
        final var footer = getFooter();
        final var cancelButton = new Button(getTranslation("page.boundary.GlobalPageEditorDialog.cancel"), this::cancel);
        footer.add(cancelButton);
        saveButton = new Button(getTranslation("page.boundary.GlobalPageEditorDialog.save"), this::save);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);
        footer.add(saveButton);

        // Enable save button only if changes are made
        ValueChangeListener<? super ValueChangeEvent<String>> valueChangeListener = _ -> {
            if (!saveButton.isEnabled()) {
                saveButton.setEnabled(true);
            }
        };
        pageTitle.addValueChangeListener(valueChangeListener);
        pageEditor.addValueChangeListener(valueChangeListener);
    }

    private void save(final @Nullable ClickEvent<Button> buttonClickEvent) {
        if (SecurityUtil.isAdmin()) {
            if (globalPageService.updateGlobalPage(globalPage, pageTitle.getValue(), pageEditor.getValue())) {
                onSavedCallback.accept(globalPageService.getGlobalPage(globalPage.slot(), globalPage.language())
                        .orElse(globalPage));
                saveButton.setEnabled(false);
                close();
            } else {
                final var message = getTranslation("page.boundary.GlobalPageEditorDialog.saveError");
                final var notification = new PersistentNotification(message);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.open();
            }
        } else {
            final var message = getTranslation("page.boundary.GlobalPageEditorDialog.permissionError");
            final var notification = new PersistentNotification(message);
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            notification.open();
        }
    }

    private void cancel(final @Nullable ClickEvent<Button> buttonClickEvent) {
        if (saveButton.isEnabled()) {
            final var confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader(getTranslation("page.boundary.GlobalPageEditorDialog.ConfirmDialog.title"));
            confirmDialog.setText(getTranslation("page.boundary.GlobalPageEditorDialog.ConfirmDialog.text"));

            confirmDialog.setCancelable(true);
            confirmDialog.setCancelText(getTranslation("page.boundary.GlobalPageEditorDialog.ConfirmDialog.keepEditing"));
            confirmDialog.addCancelListener(_ -> confirmDialog.close());

            confirmDialog.setRejectable(true);
            confirmDialog.setRejectText(getTranslation("page.boundary.GlobalPageEditorDialog.ConfirmDialog.discard"));
            confirmDialog.addRejectListener(_ -> {
                confirmDialog.close();
                close();
            });

            confirmDialog.setConfirmText(getTranslation("page.boundary.GlobalPageEditorDialog.ConfirmDialog.save"));
            confirmDialog.addConfirmListener(_ -> {
                confirmDialog.close();
                save(null);
            });

            confirmDialog.open();
        } else {
            close();
        }
    }

}
