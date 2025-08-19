package no.item.freemarker;

import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.RenderMode;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.script.ScriptValue;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Locale;

public class FreemarkerProcessor {
  private final Logger logger = LoggerFactory.getLogger(FreemarkerProcessor.class);
  private final Configuration configuration;
  private final PortalRequest portalRequest;

  public FreemarkerProcessor(Configuration configuration, PortalRequest portalRequest) {
    this.configuration = configuration;
    this.portalRequest = portalRequest;
  }

  /**
   * Process a template found in the build
   *
   * @param view  A ResourceKey pointing to the view
   * @param model A Map representing the model
   * @return The processed template as a string
   */
  public String process(ResourceKey view, ScriptValue model) throws Throwable {
    if (model.hasMember("portal")) {
      throw new IllegalArgumentException("Model must not contain a 'portal' member");
    }

    StringWriter writer = new StringWriter();

    try {
      Template template = configuration.getTemplate(view.toString());
      Environment environment = template.createProcessingEnvironment(model.getMap(), writer);
      Locale locale = getLocaleFromPortalRequest();
      if (locale != null) {
        environment.setLocale(locale);
      }
      environment.process();
    } catch (final Exception e) {
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
   * @param model  A Map representing the model
   * @return The processed template as a string
   */
  public String process(String source, ScriptValue model) throws Throwable {
    if (model.hasMember("portal")) {
      throw new IllegalArgumentException("Model must not contain a 'portal' member");
    }

    StringWriter writer = new StringWriter();

    try {
      Template template = new Template(null, source, configuration);
      Environment environment = template.createProcessingEnvironment(model.getMap(), writer);
      Locale locale = getLocaleFromPortalRequest();
      if (locale != null) {
        environment.setLocale(locale);
      }
      environment.process();
    } catch (final Exception e) {
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
   */
  public String getSource(
    ResourceKey view,
    int beginColumn,
    int beginLine,
    int endColumn,
    int endLine
  ) throws Throwable {
    Template template = configuration.getTemplate(view.toString());
    return template.getSource(beginColumn, beginLine, endColumn, endLine);
  }

  private Locale getLocaleFromPortalRequest() {
    try {
      return portalRequest.getContent().getLanguage();
    } catch (Exception ex) {
      return null;
    }
  }
}
