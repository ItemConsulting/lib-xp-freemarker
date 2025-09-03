package no.item.freemarker;

import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.script.ScriptValue;
import freemarker.template.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FreemarkerProcessorTest {
  private Configuration configuration;

  @Mock
  private ScriptValue model;

  @Mock
  private ResourceTemplateLoader loader;

  @Mock
  private ResourceTemplateSource source;

  @Mock
  private PortalRequest portalRequest;

  @Mock
  private LocaleService localeService;

  @BeforeEach
  void init() {
    configuration = new Configuration(Configuration.VERSION_2_3_34);
    configuration.setTemplateLoader(loader);
  }

  @Test
  void shouldProcessInlineTemplateSuccessfully() throws Throwable {
    FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor(configuration, portalRequest, localeService);

    when(model.getMap()).thenReturn(Map.of("title", "Freemarker is better than Thymeleaf"));

    String result = freemarkerProcessor.processInline("<h1>${title}</h1>", model, "inline-template-text.ftl");

    assertEquals("<h1>Freemarker is better than Thymeleaf</h1>", result);
  }

  @Test
  void shouldProcessResourceTemplateSuccessfully() throws Throwable {
    FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor(configuration, portalRequest, localeService);
    String templateContent = "<h1>${title}</h1>";

    when(loader.findTemplateSource(anyString())).thenReturn(source);
    when(loader.getReader(any(), anyString())).thenReturn(new StringReader(templateContent));
    when(model.getMap()).thenReturn(Map.of("title", "Freemarker is better than Thymeleaf"));

    String result = freemarkerProcessor.process("myapp:myresource", model);

    assertEquals("<h1>Freemarker is better than Thymeleaf</h1>", result);
  }


  @Test
  void shouldThrowExceptionWhenModelContainsPortalField() {
    FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor(configuration, portalRequest, localeService);

    when(model.hasMember("portal")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> {
      freemarkerProcessor.processInline("<h1>Test</h1>", model, "throwing-template.ftl");
    });

    assertThrows(IllegalArgumentException.class, () -> {
      freemarkerProcessor.process("myapp:myresource", model);
    });

    verify(model, times(2)).hasMember("portal");
  }
}
