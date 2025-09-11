package no.item.freemarker;

import freemarker.template.TemplateDirectiveModel;

import java.util.List;

/**
 * This interface represents the portal object that can be used in FreeMarker templates.
 */
public interface FreemarkerPortalObject {
  /**
   * Exposes the component directive from the `portal` namespace
   *
   * @return An instance of the component directive
   */
  TemplateDirectiveModel getComponent();

  /**
   * This function localizes a phrase.
   *
   * @param key The property key.
   * @return The localized string.
   */
  String localize(String key);

  /**
   * This function localizes a phrase.
   *
   * @param key    The property key.
   * @param values Placeholder values.
   * @return The localized string.
   */
  String localize(String key, List<String> values);

  /**
   * This function localizes a phrase.
   *
   * @param key    The property key.
   * @param locale A string-representation of a locale. If the locale is not set, the content language is used.
   * @param values Placeholder values.
   * @return The localized string.
   */
  String localize(String key, String locale, List<String> values);

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
  String localize(String key, String locale, List<String> values, List<String> bundles, String application);

  /**
   * This function replaces abstract internal links contained in an HTML text by generated URLs.
   * When outputting processed HTML in *.ftlh files remember to unescape the output. ${portal.processHtml(value)?no_esc}
   *
   * @param value Html value string to process.
   * @return The processed HTML.
   */
  String processHtml(String value);

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
  String processHtml(String value, String type, List<Integer> imageWidths, String imageSizes);
}
