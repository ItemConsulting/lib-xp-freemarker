package no.item.freemarker;

import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.portal.view.ViewFunctionService;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import no.api.freemarker.java8.Java8ObjectWrapper;

import java.util.function.Supplier;

public class FreemarkerScriptBean implements ScriptBean {
  private final Configuration configuration;
  private Supplier<PortalRequest> requestSupplier;

  public FreemarkerScriptBean() {
    // TODO We could read a file in the config directory to update this configuration (freemarker.properties)
    configuration = new Configuration(Configuration.VERSION_2_3_34);
    configuration.setDefaultEncoding("UTF-8");
    configuration.setLogTemplateExceptions(false);
    configuration.setLocalizedLookup(false); // Don't check for e.g. template_en.ftl
    configuration.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    configuration.setObjectWrapper(new Java8ObjectWrapper(Configuration.VERSION_2_3_34));
  }

  @Override
  public void initialize(BeanContext context) {
    this.requestSupplier = context.getBinding(PortalRequest.class);
    ViewFunctionService viewFunctionService = context.getService(ViewFunctionService.class).get();
    ResourceService resourceService = context.getService(ResourceService.class).get();
    PortalUrlService urlService = context.getService(PortalUrlService.class).get();

    try {
      FreemarkerPortalObject portal = new FreemarkerPortalObject(urlService, viewFunctionService, this.requestSupplier);

      configuration.setSharedVariable("portal", portal);
      configuration.setTemplateLoader(new ResourceTemplateLoader(resourceService));
    } catch (TemplateModelException e) {
      throw new RuntimeException(e);
    }
  }

  public FreemarkerProcessor newProcessor() {
    return new FreemarkerProcessor(configuration, requestSupplier.get());
  }
}
