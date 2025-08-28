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

import app.komunumo.data.service.ConfirmationService;
import app.komunumo.data.service.ServiceProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class ConfirmationDialog extends Dialog {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ConfirmationDialog.class);

    private final transient @NotNull ConfirmationService confirmationService;

    private final @NotNull Paragraph customMessage = new Paragraph();

    public ConfirmationDialog(final @NotNull ServiceProvider serviceProvider) {
        super();
        this.confirmationService = serviceProvider.confirmationService();
        addClassName("confirmation-dialog");

        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setModal(true);
        setDraggable(false);
        setResizable(false);

        buildUserInterface();
    }

    private void buildUserInterface() {
        setHeaderTitle(getTranslation("ui.components.ConfirmationDialog.title"));
        final var closeButton = new Button(new Icon("lumo", "cross"),
                (evt) -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        customMessage.addClassName("custom-message");
        add(customMessage);

        final var confirmationTimeout = confirmationService.getConfirmationTimeoutText(getLocale());
        final var infoText = new Paragraph(getTranslation("ui.components.ConfirmationDialog.info", confirmationTimeout));
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
                .withValidator(new EmailValidator(getTranslation("ui.components.ConfirmationDialog.error.email"), true))
                .bind(dummy -> null, (dummy, value) -> { });
        binder.setBean(new DummyBean());
        binder.addStatusChangeListener(evt ->
                emailButton.setEnabled(!emailField.getValue().isBlank() && binder.isValid())
        );
        binder.validate();

        emailButton.addClickListener(evt -> {
            LOGGER.info("Login with email: {}", emailField.getValue());
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

    @SuppressWarnings("java:S2094") // DummyBean for Binder (to use validation only)
    private static final class DummyBean { }

}
