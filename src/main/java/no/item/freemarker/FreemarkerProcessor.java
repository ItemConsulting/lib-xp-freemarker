package no.item.freemarker;

import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.RenderMode;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.script.ScriptValue;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * A processor for rendering FreeMarker templates.
 */
public class FreemarkerProcessor {
  private final Logger logger = LoggerFactory.getLogger(FreemarkerProcessor.class);
  private final Configuration configuration;
  private final PortalRequest portalRequest;
  private final LocaleService localeService;

  /**
   * Constructs a new FreemarkerProcessor with the specified configuration and services.
   * This processor is responsible for rendering FreeMarker templates with the provided
   * configuration, portal context, and locale support.
   *
   * @param configuration the FreeMarker configuration containing template settings and directives
   * @param portalRequest the portal request context providing access to content, site, and request information
   * @param localeService the service for retrieving available locales and internationalization support
   */
  public FreemarkerProcessor(Configuration configuration, PortalRequest portalRequest, LocaleService localeService) {
    this.configuration = configuration;
    this.portalRequest = portalRequest;
    this.localeService = localeService;
  }

  /**
   * Process a template found in the build
   *
   * @param view A URI pointing to the view (Most likely {@link ResourceKey}.getUri() object)
   * @param model A Map representing the model
   * @return The processed template as a string
   * @throws IOException If an error occurs reading the template
   * @throws TemplateException If an error occurs processing the template
   */
  public String process(String view, ScriptValue model) throws IOException, TemplateException {
    if (model.hasMember("portal")) {
      throw new IllegalArgumentException("Model must not contain a 'portal' member");
    }

    StringWriter writer = new StringWriter();

    try {
      Template template = configuration.getTemplate(view);
      Environment environment = template.createProcessingEnvironment(model.getMap(), writer);
      Locale locale = getLocaleFromPortalRequest();
      if (locale != null) {
        environment.setLocale(locale);
      }
      environment.process();
    } catch (final TemplateException e) {
      // Don't throw exception with the HTML debug handler. It will instead render the error as part of the HTML
      if (configuration.getTemplateExceptionHandler() == TemplateExceptionHandler.HTML_DEBUG_HANDLER) {
        logger.error("Error processing template: {}", e.getMessage(), e);
      } else {
        throw e;
      }
    }

    return writer.toString();
  }

  /**
   * Process an inline template
   *
   * @param source The template as a string
   * @param model A Map representing the model
   * @param name A name used for the template in logs or debugging
   * @return The processed template as a string
   * @throws IOException If an error occurs reading the template
   * @throws TemplateException If an error occurs processing the template
   */
  public String processInline(String source, ScriptValue model, String name) throws IOException, TemplateException {
    if (model.hasMember("portal")) {
      throw new IllegalArgumentException("Model must not contain a 'portal' member");
    }

    StringWriter writer = new StringWriter();

    try {
      Template template = new Template(name, source, configuration);
      Environment environment = template.createProcessingEnvironment(model.getMap(), writer);
      Locale locale = getLocaleFromPortalRequest();
      if (locale != null) {
        environment.setLocale(locale);
      }
      environment.process();
    } catch (final TemplateException e) {
      // Throw exception on the live site, but allow rendering of HTML error message (done in writer) in Content Studio
      if (portalRequest.getMode().equals(RenderMode.LIVE)) {
        throw e;
      }
    }

    return writer.toString();
  }

  /**
   * Returns the source code for the specified region of the template.
   *
   * @param view        A ResourceKey pointing to the view
   * @param beginColumn the first column of the requested source, 1-based
   * @param beginLine   the first line of the requested source, 1-based
   * @param endColumn   the last column of the requested source, 1-based. If this is beyond the last character of the
   *                    line, it assumes that you want to whole line.
   * @param endLine     the last line of the requested source, 1-based
   * @return The source code for the specified region of the template.
   * @throws IOException If an error occurs while reading the template file or if the template cannot be found
   */
  public String getSource(
    ResourceKey view,
    int beginColumn,
    int beginLine,
    int endColumn,
    int endLine
  ) throws IOException {
    Template template = configuration.getTemplate(view.toString());
    return template.getSource(beginColumn, beginLine, endColumn, endLine);
  }

  /**
   * If the current content has a language set, return it as a {@link Locale}. If not, try to get the language from the site, otherwise
   * try to get the language from the "Accept-Language" header. If all else fails see what languages are used by the
   * application and return the first one.
   * @return The locale from the portal request or null if none can be determined
   */
  private Locale getLocaleFromPortalRequest() {
    if (portalRequest == null) {
      return null;
    }

    try {
      if (portalRequest.getContent() != null) {
        return portalRequest.getContent().getLanguage();
      } else if (portalRequest.getSite() != null) {
        return portalRequest.getSite().getLanguage();
      } else if (portalRequest.getRawRequest().getHeader("Accept-Language") != null) {
        String acceptLanguage = portalRequest.getRawRequest().getHeader("Accept-Language");

        return Locale.filter(
          Locale.LanguageRange.parse(acceptLanguage),
          this.localeService.getLocales(portalRequest.getApplicationKey(), "i18n/phrases")
        ).get(0);
      }
    } catch (Exception ex) {
      // Do nothing, just return null
    }

    return null;
  }
}
