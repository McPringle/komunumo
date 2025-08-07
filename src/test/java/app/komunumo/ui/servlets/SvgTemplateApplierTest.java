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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SvgTemplateApplierTest {

    private SvgTemplateApplier applier;

    @BeforeEach
    void setUp() throws Exception {
        // Assuming we have test SVGs in the resources folder.
        Path userSvgPath = Path.of(getClass().getResource("/testUserSvg.svg").toURI()).toRealPath();
        applier = new SvgTemplateApplier(userSvgPath.toString(), "notprovided.svg");
    }

    @Test
    void testGetUserSvgWidth() {
        // Test that the user SVG width is correctly parsed
        double width = applier.getUserSvgWidth();
        assertThat(width).isEqualTo(500.0);
    }

    @Test
    void testGetUserSvgHeight() {
        // Test that the user SVG height is correctly parsed
        double height = applier.getUserSvgHeight();
        assertThat(height).isEqualTo(400.0);
    }

    @Test
    void testPrepareTemplateSplitWithValidSvg() throws Exception {
        String wrapperSvg = "<svg width=\"500\" height=\"500\"><g id=\"Logo\"><circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"red\" /></g></svg>";

        String preppedTemplate = applier.parseTemplate(wrapperSvg);

        // Check that the split contains the expected parts
        assertThat(preppedTemplate).isNotNull();

        // Further check if prefix and suffix contain the expected parts of the SVG
        assertThat(preppedTemplate.contains("<circle")).isFalse();
    
        XPath xpath = XPathFactory.newInstance().newXPath();
        String idValue = xpath.evaluate("/svg/g/@id", asDoc(preppedTemplate));
        assertThat(idValue).isEqualTo("Logo");
    }

    @Test
    void testApplyTemplate() throws Exception {
        String wrapperSvg = "<svg width=\"500\" height=\"500\"><g id=\"Logo\"></g></svg>";

        // Split the template first
        String preppedString = applier.parseTemplate(wrapperSvg);

        // Now apply the user SVG content into the template
        String finalSvg = applier.applyTemplate(preppedString);

        // Check if the final SVG contains the user SVG content
        XPath xpath = XPathFactory.newInstance().newXPath();
        String cx = xpath.evaluate("/svg/g/circle/@cx", asDoc(finalSvg));
        assertThat(cx).isEqualTo("50");
    }

    private static Document asDoc(String xml) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        try{
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
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
        String svgContent = "<svg width=\"500\" height=\"500\" viewBox=\"0 0 500 500\"></svg>";
        Document doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(svgContent.getBytes()));

        // Assuming width/height from viewBox is 500px
        double width = applier.deriveSvgDimension(doc.getDocumentElement(), "width");
        assertThat(width).isEqualTo(500.0);

        double height = applier.deriveSvgDimension(doc.getDocumentElement(), "height");
        assertThat(height).isEqualTo(500.0);
    }

    @Test
    void testInvalidDimensionUnit() throws Exception{
        // Test for invalid dimension unit in the SVG
        String invalidSvgContent = "<svg width=\"500xyz\" height=\"400px\"></svg>";
        Document doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(invalidSvgContent.getBytes()));

        assertThatThrownBy(() -> applier.deriveSvgDimension(doc.getDocumentElement(), "width"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
