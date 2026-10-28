package no.item.freemarker;

import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.google.common.io.ByteSource;
import freemarker.ext.dom.NodeModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XmlParserTest {
  private static final String XML = "<article><!-- note --><title>Hello</title></article>";
  private static final ResourceKey KEY = ResourceKey.from("myapp:/data/article.xml");

  @Mock
  private ResourceService resourceService;

  @Mock
  private Resource resource;

  private XmlParser parser() {
    return new XmlParser(() -> resourceService);
  }

  private static String rootName(NodeModel model) {
    return ((Document) model.getNode()).getDocumentElement().getNodeName();
  }

  @Test
  void shouldParseString() throws Exception {
    assertEquals("article", rootName(parser().parse(XML)));
  }

  @Test
  void shouldParseByteSource() throws Exception {
    NodeModel model = parser().parse(ByteSource.wrap(XML.getBytes(StandardCharsets.UTF_8)));

    assertEquals("article", rootName(model));
  }

  @Test
  void shouldParseResource() throws Exception {
    when(resourceService.getResource(KEY)).thenReturn(resource);
    when(resource.exists()).thenReturn(true);
    when(resource.getKey()).thenReturn(KEY);
    when(resource.openStream()).thenReturn(new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8)));

    assertEquals("article", rootName(parser().parse(KEY)));
  }

  @Test
  void shouldHonorTheEncodingDeclaredInTheXml() throws Exception {
    byte[] latin1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><title>Blåbær</title>".getBytes(StandardCharsets.ISO_8859_1);

    Document document = (Document) parser().parse(ByteSource.wrap(latin1)).getNode();

    assertEquals("Blåbær", document.getDocumentElement().getTextContent());
  }

  @Test
  void shouldRemoveComments() throws Exception {
    Document document = (Document) parser().parse(XML).getNode();

    assertEquals(1, document.getDocumentElement().getChildNodes().getLength());
  }

  @Test
  void shouldThrowWhenResourceDoesNotExist() {
    when(resourceService.getResource(KEY)).thenReturn(resource);
    when(resource.exists()).thenReturn(false);

    XmlParser parser = parser();

    assertThrows(IllegalArgumentException.class, () -> parser.parse(KEY));
  }

  @Test
  void shouldThrowOnUnsupportedSource() {
    XmlParser parser = parser();

    assertThrows(IllegalArgumentException.class, () -> parser.parse(42));
    assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
  }

  @Test
  void shouldThrowOnMalformedXml() {
    XmlParser parser = parser();

    assertThrows(SAXException.class, () -> parser.parse("<article>"));
  }

  @Test
  void shouldNotResolveExternalEntities(@TempDir Path dir) throws Exception {
    Path secret = Files.writeString(dir.resolve("secret.txt"), "top-secret");
    String xml = "<!DOCTYPE article [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]><article>&xxe;</article>";

    Document document = (Document) parser().parse(xml).getNode();

    assertFalse(document.getDocumentElement().getTextContent().contains("top-secret"));
  }
}
