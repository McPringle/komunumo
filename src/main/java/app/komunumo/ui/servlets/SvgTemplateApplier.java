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

import app.komunumo.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

final class SvgTemplateApplier {

    // Store the user SVG as a string (without <svg> element)
    private final String userSvgString;

    private final double userSvgWidth;
    private final double userSvgHeight;

    private static final String SPLIT_MARKE_STRING = "___USVG__";

    // Conversion constants to px (for different units) - approx
    private static final double INCH_TO_PX = 96; // 1 inch = 96 px ie. Normal
    private static final double MM_TO_PX = INCH_TO_PX / 25.4; // 1 mm = 96 / 25.4 px
    private static final double CM_TO_PX = INCH_TO_PX / 2.54; // 1 cm = 96 / 2.54 px
    private static final double PT_TO_PX = INCH_TO_PX / 72; // 1 pt = 96 / 72 px
    private static final double PC_TO_PX = INCH_TO_PX / 6; // 1 pc = 96 / 6 px

    SvgTemplateApplier(final @NotNull String userSvgPath,
                       final @NotNull String defaultSvgResourcePath)
            throws Exception {
        // Load the user SVG file or fallback to default if user SVG is missing
        InputStream inputStream = null;
        if (!userSvgPath.isBlank()) {
            final var path = Path.of(userSvgPath);
            if (Files.exists(path)) {
                inputStream = Files.newInputStream(path);
            }
        }

        //   or fallback to default if user SVG is missing
        if (inputStream == null) {
            inputStream = ResourceUtil.class.getResourceAsStream(defaultSvgResourcePath);
        }

        if (inputStream == null) {
            throw new FileNotFoundException("Could not find both user and default SVG resources.");
        }

        final var svgDocument = parseSvg(inputStream);
        final var svgElement = svgDocument.getDocumentElement();

        userSvgString = stripSvgShellElement(svgDocument);
        userSvgWidth = deriveSvgDimension(svgElement, "width");
        userSvgHeight = deriveSvgDimension(svgElement, "height");
    }

    double getUserSvgWidth() {
        return userSvgWidth;
    }

    double getUserSvgHeight() {
        return userSvgHeight;
    }

    /*
     * Parse, validate and prepare the template wrapper SVG around the "Logo" group
     */
    String parseTemplate(final @NotNull String wrapperSvg) throws Exception {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        final var builder = factory.newDocumentBuilder();
        final var document = builder.parse(new InputSource(new StringReader(wrapperSvg)));

        final var xPath = XPathFactory.newInstance().newXPath();
        final var xpathExpr = String.format("//%s[@id='%s']", "g", "Logo");
        final var gLogo = (Element) xPath.evaluate(xpathExpr, document, XPathConstants.NODE);

        if (gLogo == null) {
            throw new RuntimeException("Container element not found");
        }

        while (gLogo.hasChildNodes()) {
            gLogo.removeChild(gLogo.getFirstChild());
        }
        gLogo.appendChild(document.createTextNode(SPLIT_MARKE_STRING));

        final var writer = new StringWriter();
        final var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    /*
     * Apply the applyable(expanded) template string by wrapping it around custom SVG content
     */
    String applyTemplate(final @NotNull String preppedTemplate) {
        return preppedTemplate.replace(SPLIT_MARKE_STRING, userSvgString);
    }

    // Parse the SVG and extract its inner contents, stripping away the <svg> wrapper
    private String stripSvgShellElement(final @NotNull Document doc) throws Exception {
        final var svgContent = new StringBuilder();
        final var rootElement = doc.getDocumentElement();

        final var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        final var children = rootElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final var child = children.item(i);
            final var writer = new StringWriter();
            transformer.transform(new DOMSource(child), new StreamResult(writer));
            svgContent.append(writer.toString());
        }

        return svgContent.toString();
    }

    // Parse the SVG into a Document object
    static Document parseSvg(final @NotNull InputStream inputStream) throws Exception {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final var builder = factory.newDocumentBuilder();
        return builder.parse(inputStream);
    }

    // Parse the SVG dimension and convert it to pixels (handling various units)
    double deriveSvgDimension(final @NotNull Element docElement,
                              final @NotNull String dimensionType) {
        final var dim = docElement.getAttribute(dimensionType);
        if (dim == null || dim.isBlank()) {
            // If width/height is not provided, fall back to the viewBox values
            return getDimensionFromViewBox(docElement, dimensionType);
        }

        if (Character.isDigit(dim.charAt(dim.length() - 1))) {
            return Double.parseDouble(dim);
        }

        // Handle % unit (percentage of the viewBox)
        if (dim.endsWith("%")) {
            return getDimensionFromViewBox(docElement, dimensionType)
                    * (Double.parseDouble(dim.substring(0, dim.length() - 1)) / 100);
        }

        // Handle different units like in, mm, cm, pt, pc
        if (dim.length() > 1) {
            final var dimSfx = dim.substring(dim.length() - 2);
            switch (dimSfx) {
                case "px": return asPixels(dim, 1);
                case "in": return asPixels(dim, INCH_TO_PX);
                case "mm": return asPixels(dim, MM_TO_PX);
                case "cm": return asPixels(dim, CM_TO_PX);
                case "pt": return asPixels(dim, PT_TO_PX);
                case "pc": return asPixels(dim, PC_TO_PX);
                default: break;
            }
        }

        throw new IllegalArgumentException("Unsupported unit conversion: " + dim);
    }

    private double asPixels(final @NotNull String dimension,
                            final double conversionFactor) {
        return Double.parseDouble(dimension.substring(0, dimension.length() - 2)) * conversionFactor;
    }

    // Extract the width/height from the viewBox if width/height are not provided
    private double getDimensionFromViewBox(final @NotNull Element docElement,
                                           final @NotNull String dimensionType) {
        final var viewBox = docElement.getAttribute("viewBox");
        if (viewBox == null || viewBox.isBlank()) {
            throw new IllegalArgumentException("No viewBox or dimensions found in SVG.");
        }

        final var viewBoxParts = viewBox.split(" ");
        if (viewBoxParts.length < 4) {
            throw new IllegalArgumentException("Invalid viewBox attribute in SVG.");
        }

        // Return the appropriate dimension from the viewBox (width or height)
        if ("width".equals(dimensionType)) {
            return Double.parseDouble(viewBoxParts[2]);
        } else if ("height".equals(dimensionType)) {
            return Double.parseDouble(viewBoxParts[3]);
        }

        return 0;
    }

}
