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

import app.komunumo.data.service.ConfirmationContext;
import app.komunumo.data.service.ConfirmationResult;
import app.komunumo.data.service.ConfirmationService;
import app.komunumo.data.service.ServiceProvider;
import com.vaadin.flow.component.ClickEvent;
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
import org.jetbrains.annotations.Nullable;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public abstract class ConfirmationDialog extends Dialog {

    private final transient @NotNull ConfirmationService confirmationService;

    private final @NotNull Markdown customMessage = new Markdown();
    private @NotNull ConfirmationContext confirmationContext = ConfirmationContext.empty();

    public ConfirmationDialog(final @NotNull ServiceProvider serviceProvider) {
        super();
        this.confirmationService = serviceProvider.confirmationService();
        addClassName("confirmation-dialog");

        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setModal(true);
        setDraggable(false);
        setResizable(false);
    }

    private void renderUI() {
        setHeaderTitle(getTranslation("ui.components.ConfirmationDialog.title"));
        final var closeDialogButton = new Button(new Icon("lumo", "cross"), this::closeDialog);
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

        final var cancelButton = new Button(getTranslation("ui.components.ConfirmationDialog.button.cancel"), this::closeDialog);
        cancelButton.addThemeVariants(LUMO_TERTIARY);
        cancelButton.addClassName("cancel-button");
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
            final var email = emailField.getValue();
            confirmationService.startConfirmationProcess(
                    email,
                    customMessage.getContent(),
                    getLocale(),
                    this::onConfirmationSuccess,
                    confirmationContext);

            removeAll();
            add(new Markdown(getTranslation("ui.components.ConfirmationDialog.email.send",
                    email, confirmationTimeout)));

            getFooter().removeAll();
            final var closeButton = new Button(getTranslation("ui.components.ConfirmationDialog.button.close"));
            closeButton.addThemeVariants(LUMO_PRIMARY);
            closeButton.addClassName("close-button");
            closeButton.addClickListener(this::closeDialog);
            getFooter().add(closeButton);
        });

        addOpenedChangeListener(evt -> {
            if (evt.isOpened()) {
                emailField.focus();
            }
        });
    }

    /**
     * <p>Sets the {@link ConfirmationContext} for this dialog.</p>
     *
     * <p>The context acts as a container for arbitrary key-value pairs that
     * are needed after the email confirmation has been completed. After the
     * user has confirmed the email address, the
     * {@link #onConfirmationSuccess(String, ConfirmationContext)} method is
     * called and the same context is passed back, allowing the caller to
     * access the stored data and perform the required post-confirmation
     * actions.</p>
     *
     * @param confirmationContext the context containing key-value pairs to
     *                            be available after successful confirmation
     */
    @SuppressWarnings("checkstyle:hiddenField") // false positive (this is a setter)
    protected void setContext(final @NotNull ConfirmationContext confirmationContext) {
        this.confirmationContext = confirmationContext;
    }

    /**
     * <p>Renders the UI components and opens the dialog.</p>
     */
    @Override
    public void open() {
        renderUI();
        super.open();
    }

    /**
     * <p>Closes the dialog.</p>
     *
     * @param event the click event (can be {@code null})
     */
    private void closeDialog(final @Nullable ClickEvent<Button> event) {
        close();
    }

    /**
     * <p>Sets a custom message above the standard info.</p>
     *
     * @param message the custom message, Markdown syntax allowed (must not be {@code null})
     */
    protected void setCustomMessage(final @NotNull String message) {
        customMessage.setContent(message);
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
     * @param email the email address that has been verified
     * @param context the context of the confirmation, containing details about the
     *                verified email address
     * @return a {@link ConfirmationResult} indicating the outcome of the confirmation
     */
    protected abstract ConfirmationResult onConfirmationSuccess(@NotNull String email,
                                                                @NotNull ConfirmationContext context);

    @SuppressWarnings("java:S2094") // DummyBean for Binder (to use validation only)
    private static final class DummyBean {
    }

}
