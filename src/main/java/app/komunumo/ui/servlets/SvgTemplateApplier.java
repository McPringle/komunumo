package app.komunumo.ui.servlets;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SvgTemplateApplier {

    // Define a record to represent the tuple (prefix, suffix)
    public record TemplateWrapper(String prefix, String suffix) {}

    private final String userSvgString;  // Store the user SVG as a string (without <svg> element)
    private final double userSvgWidth;
    private final double userSvgHeight;

    private static final String SPLIT_MARKE_STRING = "___USVG__";

    // Conversion constants to px (for different units) - approx
    private static final double INCH_TO_PX = 96; // 1 inch = 96 px ie. Normal
    private static final double MM_TO_PX = INCH_TO_PX / 25.4; // 1 mm = 96 / 25.4 px
    private static final double CM_TO_PX = INCH_TO_PX / 2.54; // 1 cm = 96 / 2.54 px
    private static final double PT_TO_PX = INCH_TO_PX / 72; // 1 pt = 96 / 72 px
    private static final double PC_TO_PX = INCH_TO_PX / 6; // 1 pc = 96 / 6 px

    public SvgTemplateApplier(String userSvgResourcePath, String defaultSvgResourcePath) throws Exception {
        // Load the user SVG file or fallback to default if user SVG is missing
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(userSvgResourcePath);
        if (inputStream == null) {
            // If user SVG is not found, load default
            inputStream = getClass().getClassLoader().getResourceAsStream(defaultSvgResourcePath);
        }
        if (inputStream == null) {
            throw new FileNotFoundException("Could not find both user and default SVG resources.");
        }

        Document doc = parseSvg(inputStream);
        Element svgElement = doc.getDocumentElement();
        this.userSvgString = stripSvgShellElement(doc);
        this.userSvgWidth = deriveSvgDimension(svgElement, "width");
        this.userSvgHeight = deriveSvgDimension(svgElement, "height");
    }

    // Getter for the width and height of the user SVG
    public double getUserSvgWidth() {
        return userSvgWidth;
    }
    
    public double getUserSvgHeight() {
        return userSvgHeight;
    }

    // Parse, validate and prepare the tempalte wrapper SVG around the "Logo" group)
    public String parseTemplate(String wrapperSvg) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(wrapperSvg)));

        XPath xPath = XPathFactory.newInstance().newXPath();
        String xpathExpr=String.format("//%s[@id='%s']", "g", "Logo");
        Element gLogo=(Element)xPath.evaluate(xpathExpr, document, XPathConstants.NODE);

        if(gLogo==null){
            throw new RuntimeException("Container element not found");
        }

        while(gLogo.hasChildNodes()){
            gLogo.removeChild(gLogo.getFirstChild());
        }
        gLogo.appendChild(document.createTextNode(SPLIT_MARKE_STRING));

        StringWriter writer=new StringWriter();
        Transformer transformer=TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    // Apply the template by wrapping around custom SVG content
    public String applyTemplate(String preppedTemplate) {
        return preppedTemplate.replace(SPLIT_MARKE_STRING, userSvgString);
    }

    // Load SVG as a String from a resource path (remove <svg> element)
    private String stripSvgShellElement(Document doc) throws Exception {
        // Parse the SVG and extract its inner contents, skipping the <svg> wrapper
        StringBuilder svgContent = new StringBuilder();
        Element rootElement = doc.getDocumentElement();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        NodeList children = rootElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(child), new StreamResult(writer));
            svgContent.append(writer.toString());
        }

        return svgContent.toString();
    }

    // Parse the SVG into a Document object
    static Document parseSvg(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(inputStream);
    }

    // Parse the SVG dimension and convert it to pixels (handling various units)
    double deriveSvgDimension(final Element docElement, final String dimensionType) {
        String dim = docElement.getAttribute(dimensionType);
        if (dim == null || dim.isEmpty()) {
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
            String dimSfx = dim.substring(dim.length() - 2);
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

    private double asPixels(final String dimension, final double conversionFactor) {
        return Double.parseDouble(dimension.substring(0, dimension.length() - 2)) * conversionFactor;
    }

    // Extract the width/height from the viewBox if width/height are not provided
    private double getDimensionFromViewBox(Element docElement, String dimensionType) {
        String viewBox = docElement.getAttribute("viewBox");
        if (viewBox == null || viewBox.isEmpty()) {
            throw new IllegalArgumentException("No viewBox or dimensions found in SVG.");
        }

        String[] viewBoxParts = viewBox.split(" ");
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
