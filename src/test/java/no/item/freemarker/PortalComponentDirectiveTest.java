package no.item.freemarker;

import com.enonic.xp.script.ScriptValue;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * The directive writes the placeholder comment that XP's page renderer looks for when it replaces a component
 * region with the rendered component, so the exact output format matters.
 */
@ExtendWith(MockitoExtension.class)
class PortalComponentDirectiveTest {
  private Configuration configuration;

  @Mock
  private ScriptValue model;

  @BeforeEach
  void init() {
    configuration = new Configuration(Configuration.VERSION_2_3_35);
    // A TemplateDirectiveModel is a TemplateModel, so this overload does not throw
    configuration.setSharedVariable("component", new PortalComponentDirective());
  }

  private String render(String template) throws Exception {
    when(model.getMap()).thenReturn(Map.of("path", "main/1"));

    StringWriter writer = new StringWriter();
    new freemarker.template.Template("component.ftlh", template, configuration).process(model.getMap(), writer);

    return writer.toString();
  }

  @Test
  void shouldWriteComponentPlaceholder() throws Exception {
    assertEquals("<!--# COMPONENT main/0 -->", render("<@component path=\"main/0\" />"));
  }

  @Test
  void shouldResolvePathFromTheModel() throws Exception {
    assertEquals("<!--# COMPONENT main/1 -->", render("<@component path=path />"));
  }

  @Test
  void shouldWriteEmptyPathWhenTheParameterIsMissing() throws Exception {
    assertEquals("<!--# COMPONENT  -->", render("<@component />"));
  }

  @Test
  void shouldIgnoreNestedBody() throws Exception {
    assertEquals("<!--# COMPONENT main/0 -->", render("<@component path=\"main/0\">ignored</@component>"));
  }

  @Test
  void shouldRenderANumericPathSegment() throws Exception {
    // Component paths are commonly built in a loop, so a number has to come out as a plain number
    assertEquals("<!--# COMPONENT main/0 -->", render("<@component path=\"main/\" + 0 />"));
  }

  @Test
  void shouldAcceptAPlainNumberAsPath() throws Exception {
    assertEquals("<!--# COMPONENT 0 -->", render("<@component path=0 />"));
  }

  @Test
  void shouldRejectAPathThatHasNoStringForm() {
    // Anything else would land in the placeholder as a Java object's toString()
    assertThrows(TemplateException.class, () -> render("<@component path=[1, 2] />"));
    assertThrows(TemplateException.class, () -> render("<@component path={\"a\": 1} />"));
  }
}
