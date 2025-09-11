package no.item.freemarker;

import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.portal.view.ViewFunctionService;
import com.enonic.xp.script.ScriptValue;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreemarkerPortalObjectTest {
  private Configuration configuration;

  @Mock
  private ResourceTemplateLoader loader;

  @Mock
  private ScriptValue model;

  @Mock
  private ViewFunctionService viewFunctionService;

  @Mock
  private PortalUrlService urlService;

  @Mock
  private LocaleService localeService;

  @Mock
  private PortalRequest portalRequest;

  @BeforeEach
  void init() {
    configuration = new Configuration(Configuration.VERSION_2_3_34);
    configuration.setTemplateLoader(loader);

    try {
      configuration.setSharedVariable("portal", new FreemarkerPortalObjectImpl(() -> urlService, () -> viewFunctionService, () -> portalRequest));
    } catch (TemplateModelException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testLocalize() throws Throwable {
    FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor(configuration, portalRequest, localeService);

    when(viewFunctionService.execute(any())).thenReturn("FreeMarker is better than Thymeleaf");
    when(model.getMap()).thenReturn(Map.of());
    String result = freemarkerProcessor.processInline("<h1>${portal.localize('article.title')}</h1>", model, "localize-test.ftl");

    assertEquals("<h1>FreeMarker is better than Thymeleaf</h1>", result);
  }
}
