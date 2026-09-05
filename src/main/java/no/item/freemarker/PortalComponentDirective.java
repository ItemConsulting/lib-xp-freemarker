package no.item.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * A directive that can be used in FreeMarker templates to include an XP component based on the path.
 */
public class PortalComponentDirective implements TemplateDirectiveModel {
  private static final String PARAM_PATH = "path";

  /**
   * Creates a directive instance. The directive is stateless and can be shared between templates.
   */
  public PortalComponentDirective() {
  }

  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
    final String componentPath = asString(params.get(PARAM_PATH));

    Writer out = env.getOut();
    out.append("<!--# COMPONENT ").append(componentPath).append(" -->");
  }

  /**
   * Read a directive parameter as a string. Only scalars and numbers have a meaningful string form; anything else
   * would end up as a Java object's toString() inside the component placeholder, so it is rejected instead.
   *
   * @param value the parameter as FreeMarker passed it, or null if it was not given.
   * @return the parameter as a string, or an empty string if it was not given.
   * @throws TemplateModelException if the parameter cannot be represented as a string.
   */
  private static String asString(Object value) throws TemplateModelException {
    return switch (value) {
      case null -> "";
      case TemplateScalarModel scalar -> scalar.getAsString();
      case TemplateNumberModel number -> number.getAsNumber().toString();
      default -> throw new TemplateModelException(
        "The \"" + PARAM_PATH + "\" parameter must be a string or a number, was: " + value.getClass().getName()
      );
    };
  }
}
