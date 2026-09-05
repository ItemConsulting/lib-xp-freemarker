package no.item.freemarker;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.content.Content;
import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.i18n.MessageBundle;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.portal.url.ProcessHtmlParams;
import com.enonic.xp.script.ScriptValue;
import com.enonic.xp.site.Site;
import freemarker.template.Configuration;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateModelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreemarkerPortalObjectTest {
  private static final ApplicationKey BEAN_APP = ApplicationKey.from("no.item.freemarker.bean");

  @Mock
  private ResourceTemplateLoader loader;

  @Mock
  private ScriptValue model;

  @Mock
  private PortalUrlService urlService;

  @Mock
  private LocaleService localeService;

  @Mock
  private MessageBundle messageBundle;

  @Mock
  private PortalRequest portalRequest;

  @Mock
  private Site site;

  @Mock
  private Content content;

  private FreemarkerPortalObjectImpl portal;
  private Configuration configuration;

  @BeforeEach
  void init() {
    portal = new FreemarkerPortalObjectImpl(() -> urlService, () -> localeService, () -> portalRequest, BEAN_APP);

    configuration = new Configuration(Configuration.VERSION_2_3_35);
    configuration.setTemplateLoader(loader);

    try {
      configuration.setSharedVariable("portal", portal);
    } catch (TemplateModelException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Render an inline template against the configured portal object. The template is named ".ftlh" so that it gets
   * FreeMarker's HTML output format, the way real templates in an app do.
   */
  private String render(String template) throws Exception {
    when(model.getMap()).thenReturn(Map.of());

    return new FreemarkerProcessor(configuration, portalRequest, localeService)
      .processInline(template, model, "test.ftlh");
  }

  private ArgumentCaptor<String[]> bundlesReturning(String localized) {
    ArgumentCaptor<String[]> bundles = ArgumentCaptor.forClass(String[].class);
    when(localeService.getBundle(any(), any(), bundles.capture())).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn(localized);

    return bundles;
  }

  // ── localize ──────────────────────────────────────────────────────────────

  @Test
  void shouldLocalizeFromTemplate() throws Exception {
    bundlesReturning("FreeMarker is better than Thymeleaf");

    assertEquals(
      "<h1>FreeMarker is better than Thymeleaf</h1>",
      render("<h1>${portal.localize('article.title')}</h1>")
    );
  }

  @Test
  void shouldFallBackToNotTranslatedWhenKeyIsMissing() {
    when(localeService.getBundle(any(), any(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(eq("missing.key"), any(Object[].class))).thenReturn(null);

    assertEquals("NOT_TRANSLATED", portal.localize("missing.key"));
  }

  @Test
  void shouldFallBackToNotTranslatedWhenBundleIsMissing() {
    when(localeService.getBundle(any(), any(), any(String[].class))).thenReturn(null);

    assertEquals("NOT_TRANSLATED", portal.localize("article.title"));
  }

  @Test
  void shouldPassPlaceholderValuesToBundle() {
    when(localeService.getBundle(any(), any(), any(String[].class))).thenReturn(messageBundle);
    ArgumentCaptor<Object[]> values = ArgumentCaptor.forClass(Object[].class);
    when(messageBundle.localize(eq("greeting"), values.capture())).thenReturn("Hi Tom");

    assertEquals("Hi Tom", portal.localize("greeting", "en", List.of("Tom", "Jakobsen")));
    assertArrayEquals(new Object[]{"Tom", "Jakobsen"}, values.getValue());
  }

  @Test
  void shouldPassBundleNamesToLocaleService() {
    ArgumentCaptor<String[]> bundles = bundlesReturning("Hei");

    portal.localize("article.title", "no", List.of(), List.of("i18n/phrases", "i18n/extra"), null);

    assertArrayEquals(new String[]{"i18n/phrases", "i18n/extra"}, bundles.getValue());
  }

  @Test
  void shouldTolerateNullValuesAndBundles() {
    ArgumentCaptor<String[]> bundles = bundlesReturning("Hei");

    assertEquals("Hei", portal.localize("article.title", "no", null, null, null));
    assertArrayEquals(new String[0], bundles.getValue());
  }

  // ── application resolution ────────────────────────────────────────────────

  @Test
  void shouldPreferExplicitApplicationOverRequest() {
    ArgumentCaptor<ApplicationKey> application = ArgumentCaptor.forClass(ApplicationKey.class);
    when(localeService.getBundle(application.capture(), any(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn("Hei");

    portal.localize("article.title", "no", List.of(), List.of(), "no.item.freemarker.explicit");

    assertEquals(ApplicationKey.from("no.item.freemarker.explicit"), application.getValue());
  }

  @Test
  void shouldUseApplicationOfBeanRatherThanOfPortalRequest() {
    // XP's own /lib/xp/i18n resolves the default application from the calling script, never from the request
    ArgumentCaptor<ApplicationKey> application = ArgumentCaptor.forClass(ApplicationKey.class);
    when(localeService.getBundle(application.capture(), any(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn("Hei");

    portal.localize("article.title", "no", List.of());

    assertEquals(BEAN_APP, application.getValue());
    verify(portalRequest, never()).getApplicationKey();
  }

  @Test
  void shouldUseApplicationOfBeanWhenThereIsNoRequest() {
    FreemarkerPortalObjectImpl withoutRequest =
      new FreemarkerPortalObjectImpl(() -> urlService, () -> localeService, () -> null, BEAN_APP);

    ArgumentCaptor<ApplicationKey> application = ArgumentCaptor.forClass(ApplicationKey.class);
    when(localeService.getBundle(application.capture(), any(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn("Hei");

    withoutRequest.localize("article.title", "no", List.of());

    assertEquals(BEAN_APP, application.getValue());
  }

  @Test
  void shouldTreatABlankApplicationAsAbsent() {
    ArgumentCaptor<ApplicationKey> application = ArgumentCaptor.forClass(ApplicationKey.class);
    when(localeService.getBundle(application.capture(), any(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn("Hei");

    portal.localize("article.title", "no", List.of(), List.of(), "  ");

    assertEquals(BEAN_APP, application.getValue());
  }

  @Test
  void shouldTreatABlankLocaleAsAbsent() {
    when(portalRequest.getSite()).thenReturn(site);
    when(site.getLanguage()).thenReturn(Locale.forLanguageTag("nn-NO"));

    ArgumentCaptor<Locale> locale = ArgumentCaptor.forClass(Locale.class);
    when(localeService.getBundle(any(), locale.capture(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn("Hei");

    portal.localize("article.title", "  ", List.of());

    assertEquals(Locale.forLanguageTag("nn-NO"), locale.getValue());
  }

  // ── locale resolution ─────────────────────────────────────────────────────

  @Test
  void shouldUseExplicitLocale() {
    ArgumentCaptor<Locale> locale = ArgumentCaptor.forClass(Locale.class);
    when(localeService.getBundle(any(), locale.capture(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn("Hei");

    portal.localize("article.title", "no-NO", List.of());

    assertEquals(Locale.forLanguageTag("no-NO"), locale.getValue());
  }

  @Test
  void shouldFallBackToSiteLanguageWhenNoLocaleIsGiven() {
    when(portalRequest.getSite()).thenReturn(site);
    when(site.getLanguage()).thenReturn(Locale.forLanguageTag("nn-NO"));

    ArgumentCaptor<Locale> locale = ArgumentCaptor.forClass(Locale.class);
    when(localeService.getBundle(any(), locale.capture(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn("Hei");

    portal.localize("article.title", "", List.of());

    assertEquals(Locale.forLanguageTag("nn-NO"), locale.getValue());
  }

  @Test
  void shouldPassNullLocaleWhenNothingCanBeResolved() {
    when(portalRequest.getSite()).thenReturn(null);

    ArgumentCaptor<Locale> locale = ArgumentCaptor.forClass(Locale.class);
    when(localeService.getBundle(any(), locale.capture(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn("Hei");

    portal.localize("article.title", null, List.of());

    assertNull(locale.getValue());
  }

  @Test
  void shouldUseEnvironmentLocaleWhenLocalizingFromATemplate() throws Exception {
    // The processor sets the environment locale from the content language of the portal request
    when(portalRequest.getContent()).thenReturn(content);
    when(content.getLanguage()).thenReturn(Locale.forLanguageTag("nn-NO"));

    ArgumentCaptor<Locale> locale = ArgumentCaptor.forClass(Locale.class);
    when(localeService.getBundle(any(), locale.capture(), any(String[].class))).thenReturn(messageBundle);
    when(messageBundle.localize(any(), any(Object[].class))).thenReturn("Hei");

    render("${portal.localize('article.title')}");

    assertEquals(Locale.forLanguageTag("nn-NO"), locale.getValue());
  }

  // ── processHtml ───────────────────────────────────────────────────────────

  @Test
  void shouldProcessHtmlWithDefaults() {
    ArgumentCaptor<ProcessHtmlParams> params = ArgumentCaptor.forClass(ProcessHtmlParams.class);
    when(urlService.processHtml(params.capture())).thenReturn("<p>processed</p>");

    assertEquals("<p>processed</p>", portal.processHtml("<p>raw</p>"));
    assertEquals("<p>raw</p>", params.getValue().getValue());
    assertEquals("server", params.getValue().getType()); // XP's default url type
    assertNull(params.getValue().getImageWidths());
    assertNull(params.getValue().getImageSizes());
  }

  @Test
  void shouldProcessHtmlWithAllParameters() {
    ArgumentCaptor<ProcessHtmlParams> params = ArgumentCaptor.forClass(ProcessHtmlParams.class);
    when(urlService.processHtml(params.capture())).thenReturn("<p>processed</p>");

    portal.processHtml("<p>raw</p>", "absolute", List.of(360, 720), "(max-width: 960px) 720px");

    ProcessHtmlParams captured = params.getValue();
    assertEquals("<p>raw</p>", captured.getValue());
    assertEquals("absolute", captured.getType());
    assertEquals(List.of(360, 720), captured.getImageWidths());
    assertEquals("(max-width: 960px) 720px", captured.getImageSizes());
  }

  @Test
  void shouldProcessHtmlFromTemplate() throws Exception {
    when(urlService.processHtml(any())).thenReturn("<p>processed</p>");

    assertEquals("<p>processed</p>", render("${portal.processHtml('<p>raw</p>')?no_esc}"));
  }

  // ── component directive ───────────────────────────────────────────────────

  @Test
  void shouldExposeTheSameComponentDirectiveInstance() {
    TemplateDirectiveModel component = portal.getComponent();

    assertNotNull(component);
    assertSame(component, portal.getComponent());
  }

  @Test
  void shouldRenderComponentDirectiveFromTemplate() throws Exception {
    assertEquals(
      "<!--# COMPONENT main/0 -->",
      render("<@portal.component path=\"main/0\" />")
    );
  }
}
