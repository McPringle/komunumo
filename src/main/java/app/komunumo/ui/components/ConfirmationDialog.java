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
package app.komunumo.ui.components;

import app.komunumo.data.dto.ConfirmationContext;
import app.komunumo.data.service.ConfirmationService;
import app.komunumo.data.service.ServiceProvider;
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

public abstract class ConfirmationDialog extends Dialog {

    private final transient @NotNull ConfirmationService confirmationService;

    private final @NotNull Paragraph customMessage = new Paragraph();

    public ConfirmationDialog(final @NotNull ServiceProvider serviceProvider,
                              final @NotNull String confirmationReasonKey,
                              final @NotNull String successMessageKey,
                              final @NotNull String failedMessageKey) {
        super();
        this.confirmationService = serviceProvider.confirmationService();
        addClassName("confirmation-dialog");

        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setModal(true);
        setDraggable(false);
        setResizable(false);

        setHeaderTitle(getTranslation("ui.components.ConfirmationDialog.title"));
        final var closeDialogButton = new Button(new Icon("lumo", "cross"),
                (evt) -> close());
        closeDialogButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeDialogButton.addClassName("close-dialog-button");
        getHeader().add(closeDialogButton);

        customMessage.addClassName("custom-message");
        add(customMessage);

        final var confirmationTimeout = confirmationService.getConfirmationTimeoutText(getLocale());
        final var infoText = new Paragraph(getTranslation("ui.components.ConfirmationDialog.infoText", confirmationTimeout));
        add(infoText);

        final var emailField = new EmailField();
        emailField.setPlaceholder(getTranslation("ui.components.ConfirmationDialog.field.email"));
        emailField.setValueChangeMode(EAGER);
        emailField.setWidthFull();
        add(emailField);

        final var emailButton = new Button(getTranslation("ui.components.ConfirmationDialog.button.email"));
        emailButton.addThemeVariants(LUMO_PRIMARY);
        emailButton.addClassName("email-button");
        getFooter().add(emailButton);

        final var cancelButton = new Button(getTranslation("ui.components.ConfirmationDialog.button.cancel"),
                (evt) -> close());
        cancelButton.addThemeVariants(LUMO_TERTIARY);
        getFooter().add(cancelButton);

        final var binder = new Binder<DummyBean>();
        binder.forField(emailField)
                .asRequired("")
                .withValidator(new EmailValidator(getTranslation("ui.components.ConfirmationDialog.email.error")))
                .bind(dummy -> null, (dummy, value) -> { });
        binder.setBean(new DummyBean());
        binder.addStatusChangeListener(evt -> emailButton.setEnabled(binder.isValid()));
        binder.validate();

        emailButton.addClickListener(evt -> {
            final var emailAddress = emailField.getValue();
            confirmationService.startConfirmationProcess(
                    emailAddress,
                    getTranslation(confirmationReasonKey),
                    getTranslation(successMessageKey),
                    getTranslation(failedMessageKey),
                    this::onConfirmationSuccess,
                    getLocale());

            removeAll();
            add(new Markdown(getTranslation("ui.components.ConfirmationDialog.email.send",
                    emailAddress, confirmationTimeout)));

            getFooter().removeAll();
            final var closeButton = new Button(getTranslation("ui.components.ConfirmationDialog.button.close"));
            closeButton.addThemeVariants(LUMO_PRIMARY);
            closeButton.addClassName("close-button");
            closeButton.addClickListener(ev -> close());
            getFooter().add(closeButton);
        });

        addOpenedChangeListener(evt -> {
            if (evt.isOpened()) {
                emailField.focus();
            }
        });
    }

    /**
     * <p>Sets a custom message above the standard info.</p>
     *
     * @param message the custom message
     */
    protected void setCustomMessage(final @NotNull String message) {
        customMessage.setText(message);
    }

    /**
     * <p>Callback that is invoked by the {@link ConfirmationService} after an email
     * address has been successfully verified.</p>
     *
     * <p>Implementations can perform any required actions in response to the confirmation,
     * such as logging in the user, updating application state, or triggering further
     * workflows. The return value determines which feedback message is shown to the user
     * in the UI.</p>
     *
     * @param confirmationContext the context of the confirmation, containing details
     *                            about the verified email address
     * @return {@code true} if the user should see a success message in the UI,
     *         {@code false} if a failure message should be displayed instead
     */
    protected abstract boolean onConfirmationSuccess(@NotNull ConfirmationContext confirmationContext);

    @SuppressWarnings("java:S2094") // DummyBean for Binder (to use validation only)
    private static final class DummyBean {
    }

}
