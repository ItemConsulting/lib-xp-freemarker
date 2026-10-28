package no.item.freemarker;

import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.google.common.io.ByteSource;
import freemarker.ext.dom.NodeModel;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.function.Supplier;

/**
 * Parses XML documents into {@link NodeModel}s that FreeMarker templates can traverse.
 *
 * <p>The XML is often uploaded by editors or fetched from third parties, so the parser is hardened against XML
 * External Entity (XXE) attacks: external entities and DTDs are never loaded, and the JDK's secure processing limits
 * entity expansion.</p>
 */
public class XmlParser {
  private final Supplier<ResourceService> resourceServiceSupplier;

  /**
   * Create a new {@link XmlParser}.
   *
   * @param resourceServiceSupplier used to read XML files from the application when the source is a {@link ResourceKey}
   */
  public XmlParser(Supplier<ResourceService> resourceServiceSupplier) {
    this.resourceServiceSupplier = resourceServiceSupplier;
  }

  /**
   * Parse an XML document. Comments and processing instructions are removed, and adjacent text nodes are merged, like
   * {@link NodeModel#parse(InputSource)} does by default.
   *
   * @param source the XML as a {@link String}, a {@link ResourceKey} pointing to an XML file, or a {@link ByteSource}
   * @return the parsed document
   * @throws IOException       If an error occurs reading the XML
   * @throws SAXException      If the XML is not well-formed
   * @throws IllegalArgumentException If the source is of an unsupported type, or the resource does not exist
   */
  public NodeModel parse(Object source) throws IOException, SAXException {
    return switch (source) {
      case String xml -> parse(new InputSource(new StringReader(xml)));
      case ResourceKey key -> parse(getResource(key));
      case ByteSource bytes -> {
        try (InputStream stream = bytes.openStream()) {
          yield parse(new InputSource(stream));
        }
      }
      case null -> throw new IllegalArgumentException("XML source must not be null");
      default -> throw new IllegalArgumentException(
        "XML source must be a string, a ResourceKey or a ByteSource, but was " + source.getClass().getName());
    };
  }

  private NodeModel parse(Resource resource) throws IOException, SAXException {
    try (InputStream stream = resource.openStream()) {
      InputSource inputSource = new InputSource(stream);
      // Shows up in parse errors, so they point at the file
      inputSource.setSystemId(resource.getKey().toString());

      return parse(inputSource);
    }
  }

  private NodeModel parse(InputSource inputSource) throws IOException, SAXException {
    Document document = newDocumentBuilder().parse(inputSource);
    NodeModel.simplify(document);

    return NodeModel.wrap(document);
  }

  private Resource getResource(ResourceKey key) {
    Resource resource = resourceServiceSupplier.get().getResource(key);

    if (resource == null || !resource.exists()) {
      throw new IllegalArgumentException("XML file not found: " + key);
    }

    return resource;
  }

  private static DocumentBuilder newDocumentBuilder() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      // A DOCTYPE is allowed (RSS 0.91 and XHTML have one), but nothing it references is ever fetched
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);

      DocumentBuilder builder = factory.newDocumentBuilder();
      // The default handler also prints parse errors to stderr, which XP would log outside of SLF4J
      builder.setErrorHandler(new ErrorHandler() {
        @Override
        public void warning(SAXParseException e) {
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
          throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
          throw e;
        }
      });

      return builder;
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("The XML parser does not support the required security features", e);
    }
  }
}
