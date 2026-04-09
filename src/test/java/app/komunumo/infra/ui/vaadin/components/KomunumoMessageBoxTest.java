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

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.markdown.Markdown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KomunumoMessageBoxTest {

    @Test
    void defaultVariant() {
        assertMessageBoxVariant(null, "vaadin:info-circle", "komunumo-message-box-info");
    }

    @Test
    void infoVariant() {
        assertMessageBoxVariant(KomunumoMessageBox.MessageType.INFO, "vaadin:info-circle", "komunumo-message-box-info");
    }

    @Test
    void successVariant() {
        assertMessageBoxVariant(KomunumoMessageBox.MessageType.SUCCESS, "vaadin:check-circle", "komunumo-message-box-success");
    }

    @Test
    void warningVariant() {
        assertMessageBoxVariant(KomunumoMessageBox.MessageType.WARNING, "vaadin:warning", "komunumo-message-box-warning");
    }

    @Test
    void errorVariant() {
        assertMessageBoxVariant(KomunumoMessageBox.MessageType.ERROR, "lumo:error", "komunumo-message-box-error");
    }

    private void assertMessageBoxVariant(final @Nullable KomunumoMessageBox.MessageType messageType,
                                         final @NotNull String expectedIcon,
                                         final @NotNull String expectedTypeClass) {
        final var markdownText = "Message";
        final var messageBox = messageType == null ?
                new KomunumoMessageBox(markdownText) :
                new KomunumoMessageBox(markdownText, messageType);

        assertThat(messageBox.getClassNames()).contains("komunumo-message-box", expectedTypeClass);

        final var icon = messageBox.getChildren()
                .filter(Icon.class::isInstance)
                .map(Icon.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(icon.getElement().getAttribute("icon")).isEqualTo(expectedIcon);

        final var markdown = messageBox.getChildren()
                .filter(Markdown.class::isInstance)
                .map(Markdown.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(markdown.getContent()).isEqualTo(markdownText);
    }

}
