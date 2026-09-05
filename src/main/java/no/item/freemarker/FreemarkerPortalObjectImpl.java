package no.item.freemarker;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.i18n.MessageBundle;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.portal.url.ProcessHtmlParams;
import com.enonic.xp.site.Site;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveModel;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * The default portal object that can be used in FreeMarker templates.
 */
public class FreemarkerPortalObjectImpl implements FreemarkerPortalObject {
  private static final String NOT_TRANSLATED = "NOT_TRANSLATED";
  private static final String[] NO_BUNDLES = new String[0];

  private final TemplateDirectiveModel component = new PortalComponentDirective();
  private final Supplier<PortalUrlService> portalUrlServiceSupplier;
  private final Supplier<LocaleService> localeServiceSupplier;
  private final Supplier<PortalRequest> requestSupplier;
  private final ApplicationKey applicationKey;

  /**
   * This object provides portal-related functionality for FreeMarker templates,
   * including URL generation, localization, and HTML processing capabilities.
   *
   * @param portalUrlServiceSupplier Service for URL generation.
   * @param localeServiceSupplier    Service for resolving message bundles.
   * @param requestSupplier          Supplier for the current portal request.
   * @param applicationKey           The application the templates are rendered for. Used to look up message bundles
   *                                 unless an explicit application is passed to localize.
   */
  public FreemarkerPortalObjectImpl(
    Supplier<PortalUrlService> portalUrlServiceSupplier,
    Supplier<LocaleService> localeServiceSupplier,
    Supplier<PortalRequest> requestSupplier,
    ApplicationKey applicationKey
  ) {
    this.portalUrlServiceSupplier = portalUrlServiceSupplier;
    this.localeServiceSupplier = localeServiceSupplier;
    this.requestSupplier = requestSupplier;
    this.applicationKey = applicationKey;
  }

  /**
   * Exposes the component directive from the `portal` namespace
   *
   * @return An instance of the component directive
   */
  @Override
  public TemplateDirectiveModel getComponent() {
    return component;
  }

  /**
   * This function localizes a phrase.
   *
   * @param key The property key.
   * @return The localized string.
   */
  @Override
  public String localize(String key) {
    return localize(key, List.of());
  }

  /**
   * This function localizes a phrase.
   *
   * @param key    The property key.
   * @param values Placeholder values.
   * @return The localized string.
   */
  @Override
  public String localize(String key, List<String> values) {
    Environment environment = Environment.getCurrentEnvironment();
    String locale = environment != null ? environment.getLocale().toLanguageTag() : null;

    return localize(key, locale, values, List.of(), null);
  }

  /**
   * This function localizes a phrase.
   *
   * @param key    The property key.
   * @param locale A string-representation of a locale. If the locale is not set, the content language is used.
   * @param values Placeholder values.
   * @return The localized string.
   */
  @Override
  public String localize(String key, String locale, List<String> values) {
    return localize(key, locale, values, List.of(), null);
  }

  /**
   * This function localizes a phrase.
   *
   * @param key         The property key.
   * @param locale      A string-representation of a locale. If the locale is not set, the content language is used.
   * @param values      Placeholder values.
   * @param bundles     Optional list of bundle names
   * @param application Application key where to find resource bundles. Defaults to current application
   * @return The localized string.
   */
  @Override
  public String localize(String key, String locale, List<String> values, List<String> bundles, String application) {
    MessageBundle bundle = localeServiceSupplier.get().getBundle(
      resolveApplicationKey(application),
      resolveLocale(locale),
      bundles != null ? bundles.toArray(NO_BUNDLES) : NO_BUNDLES
    );

    if (bundle == null) {
      return NOT_TRANSLATED;
    }

    String localized = bundle.localize(key, values != null ? values.toArray() : new Object[0]);

    return localized != null ? localized : NOT_TRANSLATED;
  }

  /**
   * This function replaces abstract internal links contained in an HTML text by generated URLs.
   * When outputting processed HTML in *.ftlh files remember to unescape the output. ${portal.processHtml(value)?no_esc}
   *
   * @param value Html value string to process.
   * @return The processed HTML.
   */
  @Override
  public String processHtml(String value) {
    return processHtml(value, null, null, null);
  }

  /**
   * This function replaces abstract internal links contained in an HTML text by generated URLs.
   * When outputting processed HTML in *.ftlh files remember to unescape the output. ${portal.processHtml(value)?no_esc}
   *
   * @param value       Html value string to process.
   * @param type        URL type. Either `server` (server-relative URL) or `absolute`.
   * @param imageWidths List of image width. Allows to generate image URLs for given image widths and use them in the `srcset` attribute of a `img` tag.
   * @param imageSizes  Specifies the width for an image depending on browser dimensions. The value has the following format: (media-condition) width. Multiple sizes are comma-separated.
   * @return The processed HTML.
   */
  @Override
  public String processHtml(String value, String type, List<Integer> imageWidths, String imageSizes) {
    // Since XP 8 the portal request is resolved from the current context by the service itself.
    ProcessHtmlParams params = new ProcessHtmlParams()
      .type(type)
      .value(value)
      .imageWidths(imageWidths)
      .imageSizes(imageSizes);

    return portalUrlServiceSupplier.get().processHtml(params);
  }

  /**
   * Resolve the application to look up message bundles in. An explicitly passed application wins, otherwise the
   * application the bean was created for is used. This matches XP's own /lib/xp/i18n, which resolves the default
   * application from the calling script and never from the portal request: the app that renders a template is the
   * app that ships its phrases, even when the request belongs to another application.
   *
   * @param application Application key as a string, or null.
   * @return The resolved application key.
   */
  private ApplicationKey resolveApplicationKey(String application) {
    return application != null && !application.isBlank()
      ? ApplicationKey.from(application)
      : applicationKey;
  }

  /**
   * Resolve the locale to localize in. Falls back to the language of the site in the current portal request.
   *
   * @param locale A string-representation of a locale, or null.
   * @return The resolved locale, or null if none can be determined.
   */
  private Locale resolveLocale(String locale) {
    if (locale != null && !locale.isBlank()) {
      return Locale.forLanguageTag(locale);
    }

    PortalRequest request = requestSupplier.get();
    if (request == null) {
      return null;
    }

    Site site = request.getSite();

    return site != null ? site.getLanguage() : null;
  }
}
