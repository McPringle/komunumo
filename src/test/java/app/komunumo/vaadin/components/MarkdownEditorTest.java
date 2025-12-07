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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownEditorTest {

    private MarkdownEditor markdownEditor;

    @BeforeEach
    void setUp() {
        markdownEditor = new MarkdownEditor(Locale.ENGLISH);
    }

    @Test
    void setLabel() {
        assertThat(markdownEditor.getLabel()).isNull();
        final var label = "Test Label";
        markdownEditor.setLabel(label);
        assertThat(markdownEditor.getLabel()).isEqualTo(label);
    }

    @Test
    void setReadOnly() {
        assertThat(markdownEditor.isReadOnly()).isFalse();
        markdownEditor.setReadOnly(true);
        assertThat(markdownEditor.isReadOnly()).isTrue();
        markdownEditor.setReadOnly(false);
        assertThat(markdownEditor.isReadOnly()).isFalse();
    }

    @Test
    void setRequired() {
        assertThat(markdownEditor.isRequired()).isFalse();
        markdownEditor.setRequired(true);
        assertThat(markdownEditor.isRequired()).isTrue();
        markdownEditor.setRequired(false);
        assertThat(markdownEditor.isRequired()).isFalse();
    }

    @Test
    void setRequiredIndicatorVisible() {
        assertThat(markdownEditor.isRequiredIndicatorVisible()).isFalse();
        markdownEditor.setRequiredIndicatorVisible(true);
        assertThat(markdownEditor.isRequiredIndicatorVisible()).isTrue();
        markdownEditor.setRequiredIndicatorVisible(false);
        assertThat(markdownEditor.isRequiredIndicatorVisible()).isFalse();
    }

    @Test
    void placeholder() {
        assertThat(markdownEditor.getPlaceholder()).isNull();
        markdownEditor.setPlaceholder("test");
        assertThat(markdownEditor.getPlaceholder()).isEqualTo("test");
        markdownEditor.setPlaceholder(null);
        assertThat(markdownEditor.getPlaceholder()).isEmpty();
    }

    @Test
    void setValueChangeMode() {
        assertThat(markdownEditor.getValueChangeMode()).isEqualTo(ValueChangeMode.ON_CHANGE);
        markdownEditor.setValueChangeMode(ValueChangeMode.EAGER);
        assertThat(markdownEditor.getValueChangeMode()).isEqualTo(ValueChangeMode.EAGER);
        markdownEditor.setValueChangeMode(ValueChangeMode.ON_BLUR);
        assertThat(markdownEditor.getValueChangeMode()).isEqualTo(ValueChangeMode.ON_BLUR);
    }

    @Test
    void getEmptyValue() {
        assertThat(markdownEditor.getEmptyValue()).isEqualTo("");
    }

    @Test
    void generateModelValue() {
        assertThat(markdownEditor.generateModelValue()).isEqualTo("");
        final var testValue = "Test Markdown Content";
        markdownEditor.setValue(testValue);
        assertThat(markdownEditor.generateModelValue()).isEqualTo(testValue);
    }
}
