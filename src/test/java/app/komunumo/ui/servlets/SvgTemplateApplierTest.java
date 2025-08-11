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
package app.komunumo.ui.servlets;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SvgTemplateApplierTest {

    private SvgTemplateApplier applier;

    @BeforeEach
    void setUp() throws Exception {
        // Assuming we have test SVGs in the resources folder.
        final var userSvgPath = Path.of(getClass().getResource("/testUserSvg.svg").toURI()).toRealPath();
        applier = new SvgTemplateApplier(userSvgPath.toString(), "notprovided.svg");
    }

    @Test
    void testGetUserSvgWidth() {
        // Test that the user SVG width is correctly parsed
        final var width = applier.getUserSvgWidth();
        assertThat(width).isEqualTo(500.0);
    }

    @Test
    void testGetUserSvgHeight() {
        // Test that the user SVG height is correctly parsed
        final var height = applier.getUserSvgHeight();
        assertThat(height).isEqualTo(400.0);
    }

    @Test
    void testPrepareTemplateSplitWithValidSvg() throws Exception {
        final var wrapperSvg = "<svg width=\"500\" height=\"500\"><g id=\"Logo\"><circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"red\" /></g></svg>";

        final var preppedTemplate = applier.parseTemplate(wrapperSvg);

        // Check that the split contains the expected parts
        assertThat(preppedTemplate).isNotNull();

        // Further check if prefix and suffix contain the expected parts of the SVG
        assertThat(preppedTemplate.contains("<circle")).isFalse();

        final var xpath = XPathFactory.newInstance().newXPath();
        final var idValue = xpath.evaluate("/svg/g/@id", asDoc(preppedTemplate));
        assertThat(idValue).isEqualTo("Logo");
    }

    @Test
    void testPrepareTemplateSplitWithNoGLogoSvg() throws Exception {
        final var wrapperSvg = "<svg width=\"500\" height=\"500\"><circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"red\" /></svg>";

        assertThatThrownBy(() -> applier.parseTemplate(wrapperSvg))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testApplyTemplate() throws Exception {
        final var wrapperSvg = "<svg width=\"500\" height=\"500\"><g id=\"Logo\"></g></svg>";

        // Split the template first
        final var preppedString = applier.parseTemplate(wrapperSvg);

        // Now apply the user SVG content into the template
        final var finalSvg = applier.applyTemplate(preppedString);

        // Check if the final SVG contains the user SVG content
        final var xpath = XPathFactory.newInstance().newXPath();
        final var cx = xpath.evaluate("/svg/g/circle/@cx", asDoc(finalSvg));
        assertThat(cx).isEqualTo("50");
    }

    private static Document asDoc(String xml) throws ParserConfigurationException, IOException, SAXException {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);

        final var builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    @Test
    void testInvalidSvgResource() {
        // Attempt to load an invalid user SVG path
        assertThatThrownBy(() -> new SvgTemplateApplier("invalidSvg.svg", "defaultSvg.svg"))
            .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    void testParseSvgDimensionWithMissingUnits() throws Exception {
        // Test for dimensions without units, i.e., in the viewBox
        final var svgContent = "<svg width=\"500\" height=\"500\" viewBox=\"0 0 500 500\"></svg>";
        final var doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(svgContent.getBytes()));

        // Assuming width/height from viewBox is 500px
        final var width = applier.deriveSvgDimension(doc.getDocumentElement(), "width");
        assertThat(width).isEqualTo(500.0);

        final var height = applier.deriveSvgDimension(doc.getDocumentElement(), "height");
        assertThat(height).isEqualTo(500.0);
    }

    @Test
    void testInvalidDimensionUnit() throws Exception{
        // Test for invalid dimension unit in the SVG
        final var invalidSvgContent = "<svg width=\"500xyz\" height=\"400px\"></svg>";
        final var doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(invalidSvgContent.getBytes()));

        assertThatThrownBy(() -> applier.deriveSvgDimension(doc.getDocumentElement(), "width"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "<svg width=\"10px\"></svg>, 10",
        "<svg width=\"10in\"></svg>, 960",
        "<svg width=\"10mm\"></svg>, 38",
        "<svg width=\"10cm\"></svg>, 378",
        "<svg width=\"10pt\"></svg>, 13",
        "<svg width=\"10pc\"></svg>, 160",
        "<svg width=\"50%\" viewBox=\"0 0 500 500\"></svg>, 250",
        "<svg viewBox=\"0 0 500 500\"></svg>, 500"
    })
    void testSvgDimensionDerivationOnWidth(final @NotNull String svgContent, int widthPxApprox) throws Exception {
        final var doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(svgContent.getBytes()));
        final var width = applier.deriveSvgDimension(doc.getDocumentElement(), "width");
        assertThat((int)width).isEqualTo(widthPxApprox);
    }

    @ParameterizedTest
    @CsvSource({
        "<svg viewBox=\"0 0 500 300\"></svg>, height, 300",
        "<svg width=\"50%\" height=\"100%\" viewBox=\"0 0 500 500\"></svg>, absent, 0",
    })
    void testSvgDimensionDerivationUsingNonWidthCases(final @NotNull String svgContent,
                                                      final @NotNull String dim,
                                                      final int widthPxApprox)
            throws Exception{
        final var doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(svgContent.getBytes()));
        final var width = applier.deriveSvgDimension(doc.getDocumentElement(), dim);
        assertThat((int)width).isEqualTo(widthPxApprox);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<svg width=\"50%\"></svg>",
        "<svg width=\"50%\" viewBox=\"0 500 500\"></svg>"
    })
    void testSvgDimensionDerivationWithInvalidViewBox(final @NotNull String svgContent) throws Exception{
        final var doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(svgContent.getBytes()));
        assertThatThrownBy(() -> applier.deriveSvgDimension(doc.getDocumentElement(), "width"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testIllegalArgumentForNullViewBox() {
        final var docElement = mock(Element.class);
        when(docElement.getAttribute("xyz")).thenReturn(null);
        when(docElement.getAttribute("viewBox")).thenReturn(null);

        assertThatThrownBy(() -> applier.deriveSvgDimension(docElement, "xyz"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUnsupportedConversionUnit() throws Exception {
        final var svgContent = "<svg width=\"10xy\"></svg>";
        final var doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(svgContent.getBytes()));

        assertThatThrownBy(() -> applier.deriveSvgDimension(doc.getDocumentElement(), "width"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testInvalidDimension() throws Exception {
        final var svgContent = "<svg width=\"x\"></svg>";
        final var doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(svgContent.getBytes()));

        assertThatThrownBy(() -> applier.deriveSvgDimension(doc.getDocumentElement(), "width"))
            .isInstanceOf(IllegalArgumentException.class);
    }

}
