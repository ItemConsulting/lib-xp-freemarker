package no.item.freemarker;

import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import freemarker.cache.TemplateLoader;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A {@link TemplateLoader} that loads templates from XP resources.
 *
 * <p>Template names are either a full {@code <application>:<path>} resource URI, or a path from the root of the
 * calling application. FreeMarker resolves relative names (such as {@code <#include "fragment.ftlh">}) against the
 * including template before the loader sees them, so only the two forms above ever reach this class.</p>
 */
public class ResourceTemplateLoader implements TemplateLoader {
  private final Supplier<ResourceService> resourceServiceSupplier;
  private final ResourceKey baseResourceKey;

  /**
   * Create a new {@link ResourceTemplateLoader} with the given {@link ResourceService}.
   *
   * @param resourceServiceSupplier to use for finding resources.
   * @param baseResourceKey         the resource the templates are rendered from, used to resolve template names that
   *                                are not full resource URIs. May be null, in which case only full resource URIs
   *                                can be resolved.
   */
  public ResourceTemplateLoader(Supplier<ResourceService> resourceServiceSupplier, ResourceKey baseResourceKey) {
    this.resourceServiceSupplier = resourceServiceSupplier;
    this.baseResourceKey = baseResourceKey;
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

  /**
   * Look up the resource for a template name. Returns an empty {@link Optional} when the name cannot be resolved or
   * the resource does not exist, so that FreeMarker can report a proper "template not found" error.
   *
   * @param name the template name FreeMarker asked for.
   * @return the resource holding the template, if it exists.
   */
  private Optional<Resource> findResource(String name) {
    ResourceKey key = resolveKey(name);

    if (key == null) {
      return Optional.empty();
    }

    Resource resource = resourceServiceSupplier.get().getResource(key);

    return resource != null && resource.exists() ? Optional.of(resource) : Optional.empty();
  }

  /**
   * Turn a template name into a {@link ResourceKey}.
   *
   * @param name the template name FreeMarker asked for.
   * @return the resolved key, or null if the name cannot be resolved.
   */
  private ResourceKey resolveKey(String name) {
    try {
      return ResourceKey.from(name);
    } catch (IllegalArgumentException _) {
      // Not an "<application>:<path>" URI, so treat it as a path inside the calling application.
      if (baseResourceKey == null) {
        return null;
      }

      return ResourceKey.from(baseResourceKey.getApplicationKey(), name.startsWith("/") ? name : "/" + name);
    }
  }
}
