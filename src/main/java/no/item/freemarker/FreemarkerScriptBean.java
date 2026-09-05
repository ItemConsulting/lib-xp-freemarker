package no.item.freemarker;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.home.HomeDir;
import com.enonic.xp.i18n.LocaleService;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.url.PortalUrlService;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;
import com.enonic.xp.server.RunMode;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * A script bean implementation that provides FreeMarker template processing capabilities
 * for Enonic XP applications. This class manages the FreeMarker configuration, integrates
 * with XP services, and provides template processing functionality through the portal context.
 *
 * <p>The bean automatically configures FreeMarker with sensible defaults and allows for
 * additional configuration through a "freemarker.properties" file in the XP home directory.
 * It integrates with XP's portal services to provide template access to portal URLs,
 * view functions, and other XP-specific functionality.</p>
 */
public class FreemarkerScriptBean implements ScriptBean {
  private final Logger logger = LoggerFactory.getLogger(FreemarkerScriptBean.class);
  private final Configuration configuration;
  private Supplier<PortalRequest> requestSupplier;
  private Supplier<LocaleService> localeSupplier;

  /**
   * Constructs a new FreemarkerScriptBean and initializes the FreeMarker configuration.
   * Sets up default configuration settings including UTF-8 encoding, auto-detect tag syntax,
   * and Java 8 object wrapper. Also attempts to load additional configuration from a
   * "freemarker.properties" file if present in the XP home directory.
   */
  public FreemarkerScriptBean() {
    configuration = new Configuration(Configuration.VERSION_2_3_35);
    configuration.setDefaultEncoding("UTF-8");
    configuration.setLogTemplateExceptions(false);
    configuration.setLocalizedLookup(false); // Don't check for e.g. template_en.ftl
    configuration.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    configuration.setObjectWrapper(new Java8ObjectWrapper(Configuration.VERSION_2_3_35));

    if (RunMode.get() == RunMode.DEV) {
      configuration.setTemplateUpdateDelayMilliseconds(0); // Pick up template edits immediately
    }

    getPropertiesFromFile().ifPresent((properties) -> {
      try {
        logger.info("Applying configuration from the \"freemarker.properties\" file");
        configuration.setSettings(properties);
      } catch (TemplateException e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * Initializes the FreemarkerScriptBean with the provided bean context.
   * Sets up service suppliers, creates the portal object, and configures the template loader.
   * This method is called by the XP framework during bean initialization.
   *
   * @param context The bean context containing service bindings and dependencies
   * @throws RuntimeException If there's an error setting up the portal object or template loader
   */
  @Override
  public void initialize(BeanContext context) {
    this.requestSupplier = context.getBinding(PortalRequest.class);
    this.localeSupplier = context.getService(LocaleService.class);
    Supplier<ResourceService> resourceServiceSupplier = context.getService(ResourceService.class);
    Supplier<PortalUrlService> portalUrlServiceSupplier = context.getService(PortalUrlService.class);
    ApplicationKey applicationKey = context.getApplicationKey();

    try {
      FreemarkerPortalObject portal = new FreemarkerPortalObjectImpl(portalUrlServiceSupplier, this.localeSupplier, this.requestSupplier, applicationKey);

      configuration.setSharedVariable("portal", portal);
      configuration.setTemplateLoader(new ResourceTemplateLoader(resourceServiceSupplier, context.getResourceKey()));
    } catch (TemplateModelException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get the current configuration for this instance of FreeMarker.
   * @return The current configuration for this instance of FreeMarker.
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Set the portal object for use in templates.
   * @param portal The portal object to use in templates.
   * @throws TemplateModelException If the shared variable cannot be set.
   */
  public void setPortalObject(FreemarkerPortalObject portal) throws TemplateModelException {
    configuration.setSharedVariable("portal", portal);
  }

  /**
   * Create a new {@link FreemarkerProcessor} with the current configuration and portal request.
   * @return A new FreemarkerProcessor instance.
   */
  public FreemarkerProcessor newProcessor() {
    return new FreemarkerProcessor(configuration, requestSupplier.get(), localeSupplier.get());
  }

  /**
   * Load configuration properties from the "freemarker.properties" file in the XP home directory.
   * @return An Optional containing the loaded properties, or empty if the file does not exist.
   */
  private Optional<Properties> getPropertiesFromFile() {
    Path path = HomeDir.get().toPath().resolve(Path.of("config", "freemarker.properties"));

    if (!Files.isRegularFile(path)) {
      return Optional.empty();
    }

    // Files.newBufferedReader reads UTF-8 regardless of the platform default, and try-with-resources
    // closes the reader that the previous implementation leaked on every successful load.
    try (Reader reader = Files.newBufferedReader(path)) {
      Properties properties = new Properties();
      properties.load(reader);

      return Optional.of(properties);
    } catch (IOException e) {
      logger.error("Error reading freemarker.properties file: {}", e.getMessage());

      return Optional.empty();
    }
  }
}
