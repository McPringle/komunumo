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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownUtilTest {

    @Test
    void convertsPlainText() {
        final var html = MarkdownUtil.convertMarkdownToHtml("Hello, world!");
        assertThat(html).isEqualTo("<p>Hello, world!</p>\n");
    }

    @Test
    void convertsStrikethrough() {
        final var html = MarkdownUtil.convertMarkdownToHtml("This is ~~deleted~~ text.");
        assertThat(html).isEqualTo("<p>This is <del>deleted</del> text.</p>\n");
    }

    @Test
    void convertsTable() {
        final var markdown = """
                | Name  | Age |
                |-------|-----|
                | Alice |  30 |
                | Bob   |  25 |
                """;
        final var html = MarkdownUtil.convertMarkdownToHtml(markdown);
        assertThat(html).isEqualTo("""
                <table>
                <thead>
                <tr><th>Name</th><th>Age</th></tr>
                </thead>
                <tbody>
                <tr><td>Alice</td><td>30</td></tr>
                <tr><td>Bob</td><td>25</td></tr>
                </tbody>
                </table>
                """);
    }

    @Test
    void convertsLineBreaksToBr() {
        final var html = MarkdownUtil.convertMarkdownToHtml("Line 1\nLine 2");
        assertThat(html).isEqualTo("<p>Line 1<br />\nLine 2</p>\n");
    }

    @Test
    void convertsLink() {
        final var html = MarkdownUtil.convertMarkdownToHtml("[Komunumo](https://komunumo.org)");
        assertThat(html).isEqualTo("<p><a href=\"https://komunumo.org\">Komunumo</a></p>\n");
    }

}
