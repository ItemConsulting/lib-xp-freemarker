package no.item.freemarker;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.content.Content;
import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.RenderMode;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.script.ScriptValue;
import com.enonic.xp.site.Site;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreemarkerProcessorTest {
  private static final String VIEW = "myapp:/site/parts/hello/hello.ftlh";

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

  @Mock
  private Content content;

  @Mock
  private Site site;

  @BeforeEach
  void init() {
    configuration = new Configuration(Configuration.VERSION_2_3_35);
    configuration.setTemplateLoader(loader);
  }

  private FreemarkerProcessor processor() {
    return new FreemarkerProcessor(configuration, portalRequest, localeService);
  }

  /**
   * Make the template loader serve {@code content} for any requested template.
   */
  private void serveTemplate(String content) {
    when(loader.findTemplateSource(anyString())).thenReturn(source);
    when(loader.getReader(any(), anyString())).thenReturn(new StringReader(content));
  }

  // ── rendering ─────────────────────────────────────────────────────────────

  @Test
  void shouldProcessInlineTemplateSuccessfully() throws Throwable {
    when(model.getMap()).thenReturn(Map.of("title", "FreeMarker is better than Thymeleaf"));

    String result = processor().processInline("<h1>${title}</h1>", model, "inline-template-text.ftl");

    assertEquals("<h1>FreeMarker is better than Thymeleaf</h1>", result);
  }

  @Test
  void shouldProcessResourceTemplateSuccessfully() throws Throwable {
    serveTemplate("<h1>${title}</h1>");
    when(model.getMap()).thenReturn(Map.of("title", "FreeMarker is better than Thymeleaf"));

    String result = processor().process(VIEW, model);

    assertEquals("<h1>FreeMarker is better than Thymeleaf</h1>", result);
  }

  @Test
  void shouldThrowExceptionWhenModelContainsPortalField() {
    when(model.hasMember("portal")).thenReturn(true);

    FreemarkerProcessor processor = processor();

    assertThrows(IllegalArgumentException.class, () ->
      processor.processInline("<h1>Test</h1>", model, "throwing-template.ftl"));

    assertThrows(IllegalArgumentException.class, () -> processor.process(VIEW, model));

    verify(model, times(2)).hasMember("portal");
  }

  @Test
  void shouldReturnSourceForRegionOfTemplate() throws Throwable {
    serveTemplate("<h1>Hello</h1>\n<p>World</p>");

    String source = processor().getSource(ResourceKey.from(VIEW), 1, 2, 12, 2);

    assertEquals("<p>World</p>", source);
  }

  // ── error handling ────────────────────────────────────────────────────────

  @Test
  void shouldRethrowTemplateErrorsFromResourceTemplates() {
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    serveTemplate("<h1>${missing.member}</h1>");
    when(model.getMap()).thenReturn(Map.of());

    FreemarkerProcessor processor = processor();

    assertThrows(TemplateException.class, () -> processor.process(VIEW, model));
  }

  @Test
  void shouldRethrowInlineTemplateErrorsOnTheLiveSite() {
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    when(portalRequest.getMode()).thenReturn(RenderMode.LIVE);
    when(model.getMap()).thenReturn(Map.of());

    FreemarkerProcessor processor = processor();

    assertThrows(TemplateException.class, () ->
      processor.processInline("<h1>${missing.member}</h1>", model, "broken.ftl"));
  }

  @Test
  void shouldSwallowInlineTemplateErrorsOutsideTheLiveSite() throws Throwable {
    // In Content Studio the error is rendered as part of the output instead of failing the request
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    when(portalRequest.getMode()).thenReturn(RenderMode.EDIT);
    when(model.getMap()).thenReturn(Map.of());

    String result = processor().processInline("<p>before</p>${missing.member}", model, "broken.ftl");

    assertEquals("<p>before</p>", result);
  }

  @Test
  void shouldRethrowInlineTemplateErrorsWhenThereIsNoPortalRequest() {
    // No portal request means no Content Studio to render the error in, so it must not be swallowed
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    when(model.getMap()).thenReturn(Map.of());

    FreemarkerProcessor processor = new FreemarkerProcessor(configuration, null, localeService);

    assertThrows(TemplateException.class, () ->
      processor.processInline("<h1>${missing.member}</h1>", model, "broken.ftl"));
  }

  @Test
  void shouldRethrowInlineTemplateErrorsWhenTheRequestHasNoMode() {
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    when(portalRequest.getMode()).thenReturn(null);
    when(model.getMap()).thenReturn(Map.of());

    FreemarkerProcessor processor = processor();

    assertThrows(TemplateException.class, () ->
      processor.processInline("<h1>${missing.member}</h1>", model, "broken.ftl"));
  }

  // ── locale resolution ─────────────────────────────────────────────────────

  @Test
  void shouldUseLanguageOfContent() throws Throwable {
    when(portalRequest.getContent()).thenReturn(content);
    when(content.getLanguage()).thenReturn(Locale.forLanguageTag("nn-NO"));

    assertEquals("nn_NO", renderLocale());
  }

  @Test
  void shouldFallBackToLanguageOfSite() throws Throwable {
    when(portalRequest.getContent()).thenReturn(null);
    when(portalRequest.getSite()).thenReturn(site);
    when(site.getLanguage()).thenReturn(Locale.forLanguageTag("sv-SE"));

    assertEquals("sv_SE", renderLocale());
  }

  @Test
  void shouldFallBackToLocalesRequestedByTheClient() throws Throwable {
    when(portalRequest.getContent()).thenReturn(null);
    when(portalRequest.getSite()).thenReturn(null);
    when(portalRequest.getLocales()).thenReturn(List.of(Locale.forLanguageTag("da-DK")));
    when(portalRequest.getApplicationKey()).thenReturn(ApplicationKey.from("myapp"));
    when(localeService.getSupportedLocale(any(), any(), eq("i18n/phrases")))
      .thenReturn(Locale.forLanguageTag("da-DK"));

    assertEquals("da_DK", renderLocale());
  }

  @Test
  void shouldLeaveTheDefaultLocaleWhenNothingCanBeResolved() throws Throwable {
    when(portalRequest.getContent()).thenReturn(null);
    when(portalRequest.getSite()).thenReturn(null);
    when(portalRequest.getLocales()).thenReturn(List.of());

    configuration.setLocale(Locale.forLanguageTag("en-GB"));

    assertEquals("en_GB", renderLocale());
  }

  @Test
  void shouldLeaveTheDefaultLocaleWhenThereIsNoPortalRequest() throws Throwable {
    configuration.setLocale(Locale.forLanguageTag("en-GB"));
    when(model.getMap()).thenReturn(Map.of());

    String result = new FreemarkerProcessor(configuration, null, localeService)
      .processInline("${.locale}", model, "locale.ftl");

    assertEquals("en_GB", result);
  }

  /**
   * Render a template that prints the locale the processing environment ended up with.
   */
  private String renderLocale() throws Throwable {
    when(model.getMap()).thenReturn(Map.of());

    return processor().processInline("${.locale}", model, "locale.ftl");
  }
}
