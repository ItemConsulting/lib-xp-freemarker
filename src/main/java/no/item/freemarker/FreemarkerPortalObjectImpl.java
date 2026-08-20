package no.item.freemarker;

import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.url.AssetUrlParams;
import com.enonic.xp.portal.url.GenerateUrlParams;
import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.portal.url.ProcessHtmlParams;
import com.enonic.xp.portal.view.ViewFunctionParams;
import com.enonic.xp.portal.view.ViewFunctionService;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveModel;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The default portal object that can be used in FreeMarker templates.
 */
public class FreemarkerPortalObjectImpl implements FreemarkerPortalObject {
  private final TemplateDirectiveModel component = new PortalComponentDirective();
  private final Supplier<PortalUrlService> portalUrlServiceSupplier;
  private final Supplier<ViewFunctionService> viewFunctionServiceSupplier;
  private final Supplier<PortalRequest> requestSupplier;

  /**
   * This object provides portal-related functionality for FreeMarker templates,
   * including URL generation, localization, and HTML processing capabilities.
   *
   * @param portalUrlServiceSupplier Service for URL generation.
   * @param viewFunctionService      Service for executing view functions.
   * @param requestSupplier          Supplier for the current portal request.
   */
  public FreemarkerPortalObjectImpl(
    Supplier<PortalUrlService> portalUrlServiceSupplier,
    Supplier<ViewFunctionService> viewFunctionService,
    Supplier<PortalRequest> requestSupplier
  ) {
    this.portalUrlServiceSupplier = portalUrlServiceSupplier;
    this.viewFunctionServiceSupplier = viewFunctionService;
    this.requestSupplier = requestSupplier;
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
    return localize(key, Lists.newArrayList());
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
    return localize(key, environment.getLocale().toLanguageTag(), values, Lists.newArrayList(), null);
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
    return localize(key, locale, values, Lists.newArrayList(), null);
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
    Multimap<String, String> args = HashMultimap.create(5, 1);
    args.put("_key", key);
    args.put("_locale", locale);
    args.putAll("_values", values);
    args.putAll("_bundles", bundles);
    args.put("_application", application);

    ViewFunctionParams params = new ViewFunctionParams()
      .name("i18n.localize")
      .args(args)
      .portalRequest(requestSupplier.get());

    return (String) viewFunctionServiceSupplier.get().execute(params);
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
    ProcessHtmlParams params = new ProcessHtmlParams()
      .value(value)
      .type(type)
      .imageWidths(imageWidths)
      .imageSizes(imageSizes)
      .portalRequest(requestSupplier.get());

    return portalUrlServiceSupplier.get().processHtml(params);
  }
}

