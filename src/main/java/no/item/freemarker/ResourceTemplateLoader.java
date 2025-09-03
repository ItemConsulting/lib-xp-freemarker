package no.item.freemarker;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKeys;
import com.enonic.xp.resource.ResourceService;
import freemarker.cache.TemplateLoader;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

/**
 * A {@link TemplateLoader} that loads templates from XP resources.
 */
public class ResourceTemplateLoader implements TemplateLoader {
  private final ResourceService resourceService;

  /**
   * Create a new {@link ResourceTemplateLoader} with the given {@link ResourceService}.
   * @param resourceService to use for finding resources.
   */
  public ResourceTemplateLoader(ResourceService resourceService) {
    this.resourceService = resourceService;
  }

  @Override
  public Object findTemplateSource(String name) {
    return findResource(name)
      .map(ResourceTemplateSource::new)
      .orElse(null);
  }

  @Override
  public long getLastModified(Object templateSource) {
    return ((ResourceTemplateSource) templateSource).getLastModified();
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) {
    return ((ResourceTemplateSource) templateSource).getReader();
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {
    ((ResourceTemplateSource) templateSource).close();
  }

  private Optional<Resource> findResource(String name) {
    String[] parts = name.split(":", 2);

    if (parts.length != 2) {
      return Optional.empty();
    }

    ApplicationKey applicationKey = ApplicationKey.from(parts[0]);
    ResourceKeys keys = resourceService.findFiles(applicationKey, parts[1]);

    return Optional.ofNullable(keys.get(0)).map(this.resourceService::getResource);
  }
}
