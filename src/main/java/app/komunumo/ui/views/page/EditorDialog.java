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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;

public class EditorDialog extends Dialog {
    public EditorDialog() {
        setHeaderTitle("Edit Page");
        setModal(true);
        Button closeButton = new Button(new Icon("lumo", "cross"),
                e -> new ConfirmDialogBasic());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        // Add Edit and Preview tabs
        TabSheet tabSheet = new TabSheet();

        // Edit area for plain text markdown content
        TextArea textArea = new TextArea();
        textArea.setValueChangeMode(ValueChangeMode.EAGER);
        textArea.setValue("Great job. This is excellent!");

        // Rendered markdown preview
        String markdownText = """
                # Hello!

                This is a **bold** text and this is *italic* text.
                """;
        Markdown markdown = new Markdown(markdownText);

        tabSheet.add("Edit", textArea);
        tabSheet.add("Preview", markdown);
        add(tabSheet);

        // Add a footer with buttons
        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", e -> new ConfirmDialogBasic());
        HorizontalLayout buttonLayout = new HorizontalLayout(save,
                cancel);
        add(buttonLayout);
    }
}
