package no.item.freemarker;

import com.enonic.xp.home.HomeDir;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.portal.view.ViewFunctionService;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public class FreemarkerScriptBean implements ScriptBean {
  private final Logger logger = LoggerFactory.getLogger(FreemarkerScriptBean.class);
  private final Configuration configuration;
  private Supplier<PortalRequest> requestSupplier;

  public FreemarkerScriptBean() {
    configuration = new Configuration(Configuration.VERSION_2_3_34);
    configuration.setDefaultEncoding("UTF-8");
    configuration.setLogTemplateExceptions(false);
    configuration.setLocalizedLookup(false); // Don't check for e.g. template_en.ftl
    configuration.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    configuration.setObjectWrapper(new Java8ObjectWrapper(Configuration.VERSION_2_3_34));

    getPropertiesFromFile().ifPresent((properties) -> {
      try {
        logger.info("Applying configuration from the \"freemarker.properties\" file");
        configuration.setSettings(properties);
      } catch (TemplateException e) {
        throw new RuntimeException(e);
      }
    });
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

  private Optional<Properties> getPropertiesFromFile() {
    final File xpHome = HomeDir.get().toFile();
    Path path = Paths.get( xpHome.getAbsolutePath(), "config", "freemarker.properties" );
    File propertiesFile = path.toFile();

    if(propertiesFile.exists()) {
      try {
        Properties properties = new Properties();
        properties.load(new FileReader(path.toFile()));
        return Optional.of(properties);
      } catch (IOException e) {
        logger.error("Error reading freemarker.properties file: {}", e.getMessage());
      }
    }

    return Optional.empty();
  }
}
