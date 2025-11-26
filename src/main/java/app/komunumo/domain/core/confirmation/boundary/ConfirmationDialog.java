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
package app.komunumo.domain.core.confirmation.boundary;

import app.komunumo.domain.core.confirmation.entity.ConfirmationRequest;
import app.komunumo.domain.core.confirmation.control.ConfirmationService;
import app.komunumo.util.SecurityUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import org.jetbrains.annotations.NotNull;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public final class ConfirmationDialog extends Dialog {

    public ConfirmationDialog(final @NotNull ConfirmationService confirmationService,
                              final @NotNull ConfirmationRequest confirmationRequest) {
        super();
        addClassName("confirmation-dialog");

        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setModal(true);
        setDraggable(false);
        setResizable(false);

        getHeader().removeAll();
        getFooter().removeAll();
        removeAll();

        setHeaderTitle(getTranslation("core.confirmation.boundary.ConfirmationDialog.title"));
        final var closeDialogButton = new Button(new Icon("lumo", "cross"),
                _ -> close());
        closeDialogButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeDialogButton.addClassName("close-dialog-button");
        getHeader().add(closeDialogButton);

        final var actionMessage = new Markdown(confirmationRequest.actionMessage());
        actionMessage.addClassName("action-message");
        add(actionMessage);

        final var confirmationTimeout = confirmationService.getConfirmationTimeoutText(getLocale());
        final var infoText = new Paragraph(getTranslation("core.confirmation.boundary.ConfirmationDialog.infoText", confirmationTimeout));
        add(infoText);

        final var emailField = new EmailField();
        emailField.setPlaceholder(getTranslation("core.confirmation.boundary.ConfirmationDialog.field.email"));
        emailField.setValueChangeMode(EAGER);
        emailField.setWidthFull();
        add(emailField);

        final var emailButton = new Button(getTranslation("core.confirmation.boundary.ConfirmationDialog.button.email"));
        emailButton.addThemeVariants(LUMO_PRIMARY);
        emailButton.addClassName("email-button");
        getFooter().add(emailButton);

        final var cancelButton = new Button(getTranslation("core.confirmation.boundary.ConfirmationDialog.button.cancel"),
                _ -> close());
        cancelButton.addThemeVariants(LUMO_TERTIARY);
        cancelButton.addClassName("cancel-button");
        getFooter().add(cancelButton);

        final var binder = new Binder<DummyBean>();
        binder.forField(emailField)
                .asRequired("")
                .withValidator(new EmailValidator(getTranslation("core.confirmation.boundary.ConfirmationDialog.email.error")))
                .bind(_ -> null, (_, _) -> { });
        binder.setBean(new DummyBean());
        binder.addStatusChangeListener(_ -> emailButton.setEnabled(binder.isValid()));
        binder.validate();

        emailButton.addClickListener(_ -> {
            final var email = emailField.getValue();
            confirmationService.sendConfirmationMail(email, confirmationRequest);

            removeAll();
            add(new Markdown(getTranslation("core.confirmation.boundary.ConfirmationDialog.email.send",
                    email, confirmationTimeout)));

            getFooter().removeAll();
            final var closeButton = new Button(getTranslation("core.confirmation.boundary.ConfirmationDialog.button.close"));
            closeButton.addThemeVariants(LUMO_PRIMARY);
            closeButton.addClassName("close-button");
            closeButton.addClickListener(_ -> close());
            getFooter().add(closeButton);
        });

        SecurityUtil.getUserPrincipal().ifPresent(principal -> emailField.setValue(principal.getEmail()));

        addOpenedChangeListener(evt -> {
            if (evt.isOpened()) {
                emailField.focus();
            }
        });
    }

    @SuppressWarnings("java:S2094") // DummyBean for Binder (to use validation only)
    private static final class DummyBean { }

}
