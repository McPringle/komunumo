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

public class KomunumoMessageBox extends Div {

    public KomunumoMessageBox(final @NotNull String markdown) {
        this(markdown, MessageType.INFO);
    }

    public KomunumoMessageBox(final @NotNull String markdown,
                              final @NotNull MessageType messageType) {
        addClassName("komunumo-message-box");
        final var icon = createIcon(messageType);
        icon.addClassName("komunumo-message-box-icon");
        add(icon);
        add(new Markdown(markdown));
    }

    private @NotNull Icon createIcon(final @NotNull MessageType messageType) {
        return switch (messageType) {
            case INFO -> VaadinIcon.INFO_CIRCLE.create();
            case WARNING -> VaadinIcon.WARNING.create();
            case ERROR -> new Icon("lumo", "error");
        };
    }

    public enum MessageType {
        INFO, WARNING, ERROR
    }

}
