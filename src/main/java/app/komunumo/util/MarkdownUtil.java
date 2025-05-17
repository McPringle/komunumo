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
package app.komunumo.util;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class MarkdownUtil {

    private static final @NotNull MutableDataSet OPTIONS = createOptions();
    private static final @NotNull Parser MARKDOWN_PARSER = createParser();
    private static final @NotNull HtmlRenderer HTML_RENDERER = createHtmlRenderer();

    private static @NotNull MutableDataSet createOptions() {
        final var options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        return options;
    }

    private static @NotNull Parser createParser() {
        return Parser.builder(OPTIONS).build();
    }

    private static @NotNull HtmlRenderer createHtmlRenderer() {
        return HtmlRenderer.builder(OPTIONS).build();
    }

    public static @NotNull String convertMarkdownToHtml(final @NotNull String markdown) {
        final var document = MARKDOWN_PARSER.parse(markdown);
        return HTML_RENDERER.render(document);
    }

    private MarkdownUtil() {
        throw new IllegalStateException("Utility class");
    }

}
