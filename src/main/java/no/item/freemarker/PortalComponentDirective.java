package no.item.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * A directive that can be used in Freemarker templates to include an XP component based on the path.
 */
public class PortalComponentDirective implements TemplateDirectiveModel {
  private static final String PARAM_PATH = "path";

  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
    final String componentPath = (params.containsKey(PARAM_PATH) ? params.get(PARAM_PATH).toString() : "");

    Writer out = env.getOut();
    out.append("<!--# COMPONENT ").append(componentPath).append(" -->");
  }
}
