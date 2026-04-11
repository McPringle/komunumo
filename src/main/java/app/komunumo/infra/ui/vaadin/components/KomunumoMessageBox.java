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
package app.komunumo.infra.ui.vaadin.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Visual message component for informational and status messages in Komunumo views.</p>
 *
 * <p>The message content is rendered as {@link Markdown} and decorated with a type-specific icon.</p>
 */
public class KomunumoMessageBox extends Div {

    /**
     * <p>Raw message text rendered by this component.</p>
     */
    private final @NotNull String messageText;

    /**
     * <p>Creates a message box with the given text using the default type {@link MessageType#INFO}.</p>
     *
     * <p>The text is rendered as {@link Markdown} content.</p>
     *
     * @param messageText the message text to display
     */
    public KomunumoMessageBox(final @NotNull String messageText) {
        this(messageText, MessageType.INFO);
    }

    /**
     * <p>Creates a message box with the given text and message type.</p>
     *
     * <p>The component adds type-specific CSS classes and renders the text as {@link Markdown}.</p>
     *
     * @param messageText the message text to display
     * @param messageType the visual type defining color and icon
     */
    public KomunumoMessageBox(final @NotNull String messageText,
                              final @NotNull MessageType messageType) {
        this.messageText = messageText;
        addClassName("komunumo-message-box");
        addClassName("komunumo-message-box-" + messageType.name().toLowerCase());
        final var icon = createIcon(messageType);
        icon.addClassName("komunumo-message-box-icon");
        add(icon);
        add(new Markdown(messageText));
    }

    /**
     * <p>Creates the icon representing the given {@link MessageType}.</p>
     *
     * @param messageType the message type to map to an icon
     * @return the icon for the given message type
     */
    private @NotNull Icon createIcon(final @NotNull MessageType messageType) {
        return switch (messageType) {
            case INFO -> VaadinIcon.INFO_CIRCLE.create();
            case SUCCESS -> VaadinIcon.CHECK_CIRCLE.create();
            case WARNING -> VaadinIcon.WARNING.create();
            case ERROR -> new Icon("lumo", "error");
        };
    }

    /**
     * <p>Returns the message text currently displayed by this component.</p>
     *
     * @return the configured message text
     */
    @Override
    public @NotNull String getText() {
        return messageText;
    }

    /**
     * <p>Defines the available visual message types for {@link KomunumoMessageBox}.</p>
     */
    public enum MessageType {
        INFO, SUCCESS, WARNING, ERROR
    }

}
