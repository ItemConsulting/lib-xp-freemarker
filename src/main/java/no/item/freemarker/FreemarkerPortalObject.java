package no.item.freemarker;

import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.url.*;
import com.enonic.xp.portal.view.ViewFunctionParams;
import com.enonic.xp.portal.view.ViewFunctionService;
import com.enonic.xp.security.IdProviderKey;
import com.enonic.xp.web.vhost.VirtualHost;
import com.enonic.xp.web.vhost.VirtualHostHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveModel;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FreemarkerPortalObject {
  private final TemplateDirectiveModel component = new PortalComponentDirective();
  private final PortalUrlService urlService;
  private final ViewFunctionService viewFunctionService;
  private final Supplier<PortalRequest> requestSupplier;

  /**
   * This object provides portal-related functionality for Freemarker templates,
   * including URL generation, localization, and HTML processing capabilities.
   */
  public FreemarkerPortalObject(PortalUrlService urlService, ViewFunctionService viewFunctionService, Supplier<PortalRequest> requestSupplier) {
    this.urlService = urlService;
    this.viewFunctionService = viewFunctionService;
    this.requestSupplier = requestSupplier;
  }

  /**
   * Exposes the component directive from the `portal` namespace
   *
   * @return An instance of the component directive
   */
  public TemplateDirectiveModel getComponent() {
    return component;
  }

  /**
   * This function generates a URL pointing to a page.
   *
   * @param id The ID of the page.
   * @return The generated URL.
   */
  public String pageUrl(String id) {
    return pageUrl(id, null, null);
  }


  /**
   * This function generates a URL pointing to a page.
   *
   * @param id   ID to the page.
   * @param type URL type. Either `server` (server-relative URL) or `absolute`.
   * @return The generated URL.
   */
  public String pageUrl(String id, String type) {
    return pageUrl(id, type, null);
  }

  /**
   * This function generates a URL pointing to a page.
   *
   * @param id     ID to the page.
   * @param type   URL type. Either `server` (server-relative URL) or `absolute`.
   * @param params Custom parameters to append to the url.
   * @return The generated URL.
   */
  public String pageUrl(String id, String type, Map<String, String> params) {
    PageUrlParams serviceParams = new PageUrlParams()
      .id(id)
      .type(type)
      .portalRequest(requestSupplier.get());

    if (params != null) {
      params.forEach(serviceParams::param);
    }

    return urlService.pageUrl(serviceParams);
  }

  /**
   * This function generates a URL pointing to a static file.
   *
   * @param path Path to the asset.
   * @return The generated URL.
   */
  public String assetUrl(String path) {
    return assetUrl(path, null, null, null);
  }

  /**
   * This function generates a URL pointing to a static file.
   *
   * @param path        Path to the asset.
   * @param type        URL type. Either `server` (server-relative URL) or `absolute`.
   * @param application Other application to reference to. Defaults to current application.
   * @param params      Custom parameters to append to the url.
   * @return The generated URL.
   */
  public String assetUrl(String path, String type, String application, Map<String, String> params) {
    AssetUrlParams serviceParams = new AssetUrlParams()
      .path(path)
      .type(type)
      .application(application)
      .portalRequest(requestSupplier.get());

    if (params != null) {
      params.forEach(serviceParams::param);
    }

    return urlService.assetUrl(serviceParams);
  }

  /**
   * This function generates a URL pointing to an image.
   *
   * @param id    ID of the image content.
   * @param scale Required. Options are width(px), height(px), block(width,height) and square(px).
   * @return The generated URL.
   */
  public String imageUrl(String id, String scale) {
    return imageUrl(id, scale, null, null, null, null, null, null);
  }


  /**
   * This function generates a URL pointing to an image.
   *
   * @param id    ID of the image content.
   * @param scale Required. Options are width(px), height(px), block(width,height) and square(px).
   * @param type  URL type. Either `server` (server-relative URL) or `absolute`.
   * @return The generated URL.
   */
  public String imageUrl(String id, String scale, String type) {
    return imageUrl(id, scale, null, null, null, null, type, null);
  }

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
  public String imageUrl(String id, String scale, String format, String quality, String background, String filter, String type, Map<String, String> params) {
    ImageUrlParams serviceParams = new ImageUrlParams()
      .id(id)
      .scale(scale)
      .format(format)
      .quality(quality)
      .background(background)
      .filter(filter)
      .type(type)
      .portalRequest(requestSupplier.get());

    if (params != null) {
      params.forEach(serviceParams::param);
    }

    return urlService.imageUrl(serviceParams);
  }

  /**
   * This function generates a URL pointing to an attachment.
   *
   * @param id   Id to the content holding the attachment.
   * @param name Name of the attachment.
   * @return The generated URL.
   */
  public String attachmentUrl(String id, String name) {
    return attachmentUrl(id, name, null, null, null);
  }

  /**
   * This function generates a URL pointing to an attachment.
   *
   * @param id       Id to the content holding the attachment.
   * @param name     Name of the attachment.
   * @param download Set to true if the disposition header should be set to attachment.
   * @return The generated URL.
   */
  public String attachmentUrl(String id, String name, Boolean download) {
    return attachmentUrl(id, name, download, null, null);
  }

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
  public String attachmentUrl(String id, String name, Boolean download, String type, Map<String, String> params) {
    AttachmentUrlParams serviceParams = new AttachmentUrlParams()
      .id(id)
      .name(name)
      .download(download != null && download)
      .type(type)
      .portalRequest(requestSupplier.get());

    if (params != null) {
      params.forEach(serviceParams::param);
    }

    return urlService.attachmentUrl(serviceParams);
  }

  /**
   * This function generates a URL pointing to a component.
   *
   * @param id        ID to the page.
   * @param component Path to the component. If not set, the current path is set.
   * @return The generated URL.
   */
  public String componentUrl(String id, String component) {
    return componentUrl(id, component, null, null);
  }

  /**
   * This function generates a URL pointing to a component.
   *
   * @param id        ID to the page.
   * @param component Path to the component. If not set, the current path is set.
   * @param type      URL type. Either `server` (server-relative URL) or `absolute`.
   * @param params    Custom parameters to append to the url.
   * @return The generated URL.
   */
  public String componentUrl(String id, String component, String type, Map<String, String> params) {
    ComponentUrlParams serviceParams = new ComponentUrlParams()
      .id(id)
      .component(component)
      .type(type)
      .portalRequest(requestSupplier.get());

    if (params != null) {
      params.forEach(serviceParams::param);
    }

    return urlService.componentUrl(serviceParams);
  }

  /**
   * This function generates a URL pointing to a service.
   *
   * @param service Name of the service.
   * @return The generated URL.
   */
  public String serviceUrl(String service) {
    return serviceUrl(service, null, null, null);
  }

  /**
   * This function generates a URL pointing to a service.
   *
   * @param service     Name of the service.
   * @param application Other application to reference to. Default is current application.
   * @param type        URL type. Either `server` (server-relative URL) or `absolute` or `websocket`.
   * @param params      Custom parameters to append to the url.
   * @return The generated URL.
   */
  public String serviceUrl(String service, String application, String type, Map<String, String> params) {
    ServiceUrlParams serviceParams = new ServiceUrlParams()
      .service(service)
      .application(application)
      .type(type)
      .portalRequest(requestSupplier.get());

    if (params != null) {
      params.forEach(serviceParams::param);
    }

    return urlService.serviceUrl(serviceParams);
  }

  /**
   * This function generates a URL pointing to the login function of an ID provider.
   * The id provider corresponding to the current execution context will be used.
   *
   * @return The generated URL.
   */
  public String loginUrl() {
    return loginUrl(null, null, null, null, null);
  }

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
  public String loginUrl(String idProvider, String redirect, String contextPath, String type, Map<String, String> params) {
    PortalRequest portalRequest = requestSupplier.get();

    IdentityUrlParams serviceParams = new IdentityUrlParams()
      .idProviderKey((idProvider != null)
        ? IdProviderKey.from(idProvider)
        : retrieveIdProviderKey(portalRequest))
      .idProviderFunction("login")
      .redirectionUrl(redirect)
      .contextPathType(contextPath)
      .type(type)
      .portalRequest(portalRequest);

    if (params != null) {
      params.forEach(serviceParams::param);
    }

    return urlService.identityUrl(serviceParams);
  }

  /**
   * This function generates a URL pointing to the logout function of the application corresponding to the current user.
   *
   * @param redirect    The URL to redirect to after the logout.
   * @param contextPath Context path. Either `vhost` (using vhost target path) or `relative` to the current path.
   * @param type        URL type. Either `server` (server-relative URL) or `absolute`.
   * @param params      Custom parameters to append to the url.
   * @return The generated URL.
   */
  public String logoutUrl(String redirect, String contextPath, String type, Map<String, String> params) {
    PortalRequest portalRequest = requestSupplier.get();

    IdentityUrlParams serviceParams = new IdentityUrlParams()
      .idProviderKey(retrieveIdProviderKey(portalRequest))
      .idProviderFunction("logout")
      .redirectionUrl(redirect)
      .contextPathType(contextPath)
      .type(type)
      .portalRequest(portalRequest);

    if (params != null) {
      params.forEach(serviceParams::param);
    }

    return urlService.identityUrl(serviceParams);
  }

  /**
   * This function localizes a phrase.
   *
   * @param key The property key.
   * @return The localized string.
   */
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
  public String localize(String key, List<String> values) {
    Environment environment = Environment.getCurrentEnvironment();
    return localize(key, environment.getLocale().toLanguageTag(), values);
  }

  /**
   * This function localizes a phrase.
   *
   * @param key    The property key.
   * @param locale A string-representation of a locale. If the locale is not set, the content language is used.
   * @param values Placeholder values.
   * @return The localized string.
   */
  public String localize(String key, String locale, List<String> values) {
    Multimap<String, String> args = HashMultimap.create(3, 1);
    args.put("_key", key);
    args.put("_locale", locale);
    args.putAll("_values", values);

    ViewFunctionParams params = new ViewFunctionParams()
      .name("i18n.localize")
      .args(args)
      .portalRequest(requestSupplier.get());

    return (String) viewFunctionService.execute(params);
  }

  /**
   * This function replaces abstract internal links contained in an HTML text by generated URLs.
   * When outputting processed HTML in *.ftlh files remember to unescape the output. ${portal.processHtml(value)?no_esc}
   *
   * @param value Html value string to process.
   * @return The processed HTML.
   */
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
  public String processHtml(String value, String type, List<Integer> imageWidths, String imageSizes) {
    ProcessHtmlParams params = new ProcessHtmlParams()
      .value(value)
      .type(type)
      .imageWidths(imageWidths)
      .imageSizes(imageSizes)
      .portalRequest(requestSupplier.get());

    return urlService.processHtml(params);
  }

  /**
   * This function generates a URL to an image placeholder.
   *
   * @param width  Width of the image in pixels.
   * @param height Height of the image in pixels.
   * @return Placeholder image URL.
   */
  public String imagePlaceholder(Integer width, Integer height) {
    Multimap<String, String> args = HashMultimap.create(2, 1);
    args.put("width", width.toString());
    args.put("height", height.toString());

    ViewFunctionParams params = new ViewFunctionParams()
      .name("imagePlaceholder")
      .args(args)
      .portalRequest(requestSupplier.get());

    return (String) viewFunctionService.execute(params);
  }

  private IdProviderKey retrieveIdProviderKey(final PortalRequest portalRequest) {
    final VirtualHost virtualHost = VirtualHostHelper.getVirtualHost(portalRequest.getRawRequest());
    if (virtualHost != null) {
      return virtualHost.getDefaultIdProviderKey();
    }
    return null;
  }
}

