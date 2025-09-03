package no.item.freemarker;

import freemarker.template.TemplateDirectiveModel;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the portal object that can be used in Freemarker templates.
 */
public interface FreemarkerPortalObject {
  /**
   * Exposes the component directive from the `portal` namespace
   *
   * @return An instance of the component directive
   */
  TemplateDirectiveModel getComponent();

  /**
   * This function generates a URL pointing to a page.
   *
   * @param id The ID of the page.
   * @return The generated URL.
   */
  String pageUrl(String id);

  /**
   * This function generates a URL pointing to a page.
   *
   * @param id   ID to the page.
   * @param type URL type. Either `server` (server-relative URL) or `absolute`.
   * @return The generated URL.
   */
  String pageUrl(String id, String type);

  /**
   * This function generates a URL pointing to a page.
   *
   * @param id     ID to the page.
   * @param type   URL type. Either `server` (server-relative URL) or `absolute`.
   * @param params Custom parameters to append to the url.
   * @return The generated URL.
   */
  String pageUrl(String id, String type, Map<String, String> params);

  /**
   * This function generates a URL pointing to a static file.
   *
   * @param path Path to the asset.
   * @return The generated URL.
   */
  String assetUrl(String path);

  /**
   * This function generates a URL pointing to a static file.
   *
   * @param path        Path to the asset.
   * @param type        URL type. Either `server` (server-relative URL) or `absolute`.
   * @param application Other application to reference to. Defaults to current application.
   * @param params      Custom parameters to append to the url.
   * @return The generated URL.
   */
  String assetUrl(String path, String type, String application, Map<String, String> params);

  /**
   * This function generates a URL pointing to an image.
   *
   * @param id    ID of the image content.
   * @param scale Required. Options are width(px), height(px), block(width,height) and square(px).
   * @return The generated URL.
   */
  String imageUrl(String id, String scale);

  /**
   * This function generates a URL pointing to an image.
   *
   * @param id    ID of the image content.
   * @param scale Required. Options are width(px), height(px), block(width,height) and square(px).
   * @param type  URL type. Either `server` (server-relative URL) or `absolute`.
   * @return The generated URL.
   */
  String imageUrl(String id, String scale, String type);

  /**
   * This function generates a URL pointing to an image.
   *
   * @param id         ID of the image content.
   * @param scale      Required. Options are width(px), height(px), block(width,height) and square(px).
   * @param quality    Quality for JPEG images, ranges from 0 (max compression) to 100 (min compression).
   * @param background Background color.
   * @param format     Format of the image.
   * @param filter     A number of filters are available to alter the image appearance, for example, blur(3), grayscale(), rounded(5), etc.
   * @param type       URL type. Either `server` (server-relative URL) or `absolute`.
   * @param params     Custom parameters to append to the url.
   * @return The generated URL.
   */
  String imageUrl(String id, String scale, String format, String quality, String background, String filter, String type, Map<String, String> params);

  /**
   * This function generates a URL pointing to an attachment.
   *
   * @param id   Id to the content holding the attachment.
   * @param name Name of the attachment.
   * @return The generated URL.
   */
  String attachmentUrl(String id, String name);

  /**
   * This function generates a URL pointing to an attachment.
   *
   * @param id       Id to the content holding the attachment.
   * @param name     Name of the attachment.
   * @param download Set to true if the disposition header should be set to attachment.
   * @return The generated URL.
   */
  String attachmentUrl(String id, String name, Boolean download);

  /**
   * This function generates a URL pointing to an attachment.
   *
   * @param id       Id to the content holding the attachment.
   * @param name     Name of the attachment.
   * @param download Set to true if the disposition header should be set to attachment.
   * @param type     URL type. Either `server` (server-relative URL) or `absolute`.
   * @param params   Custom parameters to append to the url.
   * @return The generated URL.
   */
  String attachmentUrl(String id, String name, Boolean download, String type, Map<String, String> params);

  /**
   * This function generates a URL pointing to a component.
   *
   * @param id        ID to the page.
   * @param component Path to the component. If not set, the current path is set.
   * @return The generated URL.
   */
  String componentUrl(String id, String component);

  /**
   * This function generates a URL pointing to a component.
   *
   * @param id        ID to the page.
   * @param component Path to the component. If not set, the current path is set.
   * @param type      URL type. Either `server` (server-relative URL) or `absolute`.
   * @param params    Custom parameters to append to the url.
   * @return The generated URL.
   */
  String componentUrl(String id, String component, String type, Map<String, String> params);

  /**
   * This function generates a URL pointing to a service.
   *
   * @param service Name of the service.
   * @return The generated URL.
   */
  String serviceUrl(String service);

  /**
   * This function generates a URL pointing to a service.
   *
   * @param service     Name of the service.
   * @param application Other application to reference to. Default is current application.
   * @param type        URL type. Either `server` (server-relative URL) or `absolute` or `websocket`.
   * @param params      Custom parameters to append to the url.
   * @return The generated URL.
   */
  String serviceUrl(String service, String application, String type, Map<String, String> params);

  /**
   * This function generates a URL pointing to the login function of an ID provider.
   * The id provider corresponding to the current execution context will be used.
   *
   * @return The generated URL.
   */
  String loginUrl();

  /**
   * This function generates a URL pointing to the login function of an ID provider.
   *
   * @param idProvider  Key of the id provider using an application.
   *                    If idProvider is not set, then the id provider corresponding to the current execution context will be used.
   * @param redirect    The URL to redirect to after the login.
   * @param contextPath Context path. Either `vhost` (using vhost target path) or `relative` to the current path.
   * @param type        URL type. Either `server` (server-relative URL) or `absolute`.
   * @param params      Custom parameters to append to the url.
   * @return The generated URL.
   */
  String loginUrl(String idProvider, String redirect, String contextPath, String type, Map<String, String> params);

  /**
   * This function generates a URL pointing to the logout function of the application corresponding to the current user.
   *
   * @param redirect    The URL to redirect to after the logout.
   * @param contextPath Context path. Either `vhost` (using vhost target path) or `relative` to the current path.
   * @param type        URL type. Either `server` (server-relative URL) or `absolute`.
   * @param params      Custom parameters to append to the url.
   * @return The generated URL.
   */
  String logoutUrl(String redirect, String contextPath, String type, Map<String, String> params);

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

  /**
   * This function generates a URL to an image placeholder.
   *
   * @param width  Width of the image in pixels.
   * @param height Height of the image in pixels.
   * @return Placeholder image URL.
   */
  String imagePlaceholder(Integer width, Integer height);
}
