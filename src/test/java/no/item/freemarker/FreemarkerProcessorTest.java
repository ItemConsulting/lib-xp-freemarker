package no.item.freemarker;

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
  private ResourceKey view;

  @Mock
  private ResourceTemplateSource source;

  @Mock
  private PortalRequest portalRequest;

  @BeforeEach
  void init() {
    configuration = new Configuration(Configuration.VERSION_2_3_34);
    configuration.setTemplateLoader(loader);
  }

  @Test
  void shouldProcessInlineTemplateSuccessfully() throws Throwable {
    FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor(configuration, portalRequest);

    when(model.getMap()).thenReturn(Map.of("title", "Freemarker is better than Thymeleaf"));

    String result = freemarkerProcessor.process("<h1>${title}</h1>", model);

    assertEquals("<h1>Freemarker is better than Thymeleaf</h1>", result);
  }

  @Test
  void shouldProcessResourceTemplateSuccessfully() throws Throwable {
    FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor(configuration, portalRequest);
    String templateContent = "<h1>${title}</h1>";

    when(loader.findTemplateSource(anyString())).thenReturn(source);
    when(loader.getReader(any(), anyString())).thenReturn(new StringReader(templateContent));
    when(model.getMap()).thenReturn(Map.of("title", "Freemarker is better than Thymeleaf"));

    String result = freemarkerProcessor.process(view, model);

    assertEquals("<h1>Freemarker is better than Thymeleaf</h1>", result);
  }


  @Test
  void shouldThrowExceptionWhenModelContainsPortalField() {
    FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor(configuration, portalRequest);

    when(model.hasMember("portal")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> {
      freemarkerProcessor.process("<h1>Test</h1>", model);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      freemarkerProcessor.process(view, model);
    });

    verify(model, times(2)).hasMember("portal");
  }
}
