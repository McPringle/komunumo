package app.komunumo.ui.servlets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.xml.HasXPath.hasXPath;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SvgTemplateApplierTest {

    private SvgTemplateApplier applier;

    @BeforeEach
    void setUp() throws Exception {
        // Assuming we have test SVGs in the resources folder.
        applier = new SvgTemplateApplier("testUserSvg.svg", "defaultSvg.svg");
    }

    @Test
    void testGetUserSvgWidth() {
        // Test that the user SVG width is correctly parsed
        double width = applier.getUserSvgWidth();
        assertEquals(500.0, width, "Width should be 500px.");
    }

    @Test
    void testGetUserSvgHeight() {
        // Test that the user SVG height is correctly parsed
        double height = applier.getUserSvgHeight();
        assertEquals(400.0, height, "Height should be 400px.");
    }

    @Test
    void testPrepareTemplateSplitWithValidSvg() throws Exception {
        String wrapperSvg = "<svg width=\"500\" height=\"500\"><g id=\"Logo\"><circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"red\" /></g></svg>";

        String preppedTemplate = applier.parseTemplate(wrapperSvg);

        // Check that the split contains the expected parts
        assertNotNull(preppedTemplate, "Prefix should not be null.");

        // Further check if prefix and suffix contain the expected parts of the SVG
        assertFalse("Should not contain the circle element", preppedTemplate.contains("<circle"));
        assertThat(asDoc(preppedTemplate), hasXPath("/svg/g/@id", Matchers.equalTo("Logo")));
    }

    @Test
    void testApplyTemplate() throws Exception {
        String wrapperSvg = "<svg width=\"500\" height=\"500\"><g id=\"Logo\"></g></svg>";

        // Split the template first
        String preppedString = applier.parseTemplate(wrapperSvg);

        // Now apply the user SVG content into the template
        String finalSvg = applier.applyTemplate(preppedString);

        // Check if the final SVG contains the user SVG content
        assertThat(asDoc(finalSvg), hasXPath("/svg/g/circle/@cx", Matchers.equalTo("50")));
    }

    private static Document asDoc(String xml) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
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
        assertThrows(FileNotFoundException.class, () -> {
            new SvgTemplateApplier("invalidSvg.svg", "defaultSvg.svg");
        });
    }

    @Test
    void testParseSvgDimensionWithMissingUnits() throws Exception {
        // Test for dimensions without units, i.e., in the viewBox
        String svgContent = "<svg width=\"500\" height=\"500\" viewBox=\"0 0 500 500\"></svg>";
        Document doc = SvgTemplateApplier.parseSvg(new ByteArrayInputStream(svgContent.getBytes()));

        // Assuming width/height from viewBox is 500px
        double width = applier.deriveSvgDimension(doc.getDocumentElement(), "width");
        assertEquals(500.0, width, "Width should be derived from the viewBox.");

        double height = applier.deriveSvgDimension(doc.getDocumentElement(), "height");
        assertEquals(500.0, height, "Height should be derived from the viewBox.");
    }

    @Test
    void testInvalidDimensionUnit() throws Exception{
        // Test for invalid dimension unit in the SVG
        String invalidSvgContent = "<svg width=\"500xyz\" height=\"400px\"></svg>";
        Document doc = applier.parseSvg(new ByteArrayInputStream(invalidSvgContent.getBytes()));

        assertThrows(IllegalArgumentException.class, () -> {
            applier.deriveSvgDimension(doc.getDocumentElement(), "width");
        });
    }
}
