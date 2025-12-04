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
package app.komunumo.vaadin.components;

import com.vaadin.flow.data.value.ValueChangeMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownEditorTest {

    @Test
    void setLabel() {
        final var markdownEditor = new MarkdownEditor();
        assertThat(markdownEditor.getLabel()).isNull();
        final var label = "Test Label";
        markdownEditor.setLabel(label);
        assertThat(markdownEditor.getLabel()).isEqualTo(label);
    }

    @Test
    void setReadOnly() {
        final var markdownEditor = new MarkdownEditor();
        assertThat(markdownEditor.isReadOnly()).isFalse();
        markdownEditor.setReadOnly(true);
        assertThat(markdownEditor.isReadOnly()).isTrue();
        markdownEditor.setReadOnly(false);
        assertThat(markdownEditor.isReadOnly()).isFalse();
    }

    @Test
    void setRequiredIndicatorVisible() {
        final var markdownEditor = new MarkdownEditor();
        assertThat(markdownEditor.isRequiredIndicatorVisible()).isFalse();
        markdownEditor.setRequiredIndicatorVisible(true);
        assertThat(markdownEditor.isRequiredIndicatorVisible()).isTrue();
        markdownEditor.setRequiredIndicatorVisible(false);
        assertThat(markdownEditor.isRequiredIndicatorVisible()).isFalse();
    }

    @Test
    void setValueChangeMode() {
        final var markdownEditor = new MarkdownEditor();
        assertThat(markdownEditor.getValueChangeMode()).isEqualTo(ValueChangeMode.ON_CHANGE);
        markdownEditor.setValueChangeMode(ValueChangeMode.EAGER);
        assertThat(markdownEditor.getValueChangeMode()).isEqualTo(ValueChangeMode.EAGER);
        markdownEditor.setValueChangeMode(ValueChangeMode.ON_BLUR);
        assertThat(markdownEditor.getValueChangeMode()).isEqualTo(ValueChangeMode.ON_BLUR);
    }

    @Test
    void getEmptyValue() {
        final var markdownEditor = new MarkdownEditor();
        assertThat(markdownEditor.getEmptyValue()).isEqualTo("");
    }

    @Test
    void generateModelValue() {
        final var markdownEditor = new MarkdownEditor();
        assertThat(markdownEditor.generateModelValue()).isEqualTo("");
        final var testValue = "Test Markdown Content";
        markdownEditor.setValue(testValue);
        assertThat(markdownEditor.generateModelValue()).isEqualTo(testValue);
    }
}
