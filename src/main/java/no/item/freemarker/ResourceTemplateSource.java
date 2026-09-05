package no.item.freemarker;

import com.enonic.xp.resource.Resource;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

/**
 * A wrapper around an XP {@link Resource} that provides a {@link Reader} for reading the template content.
 */
public class ResourceTemplateSource implements Closeable {
  private final Resource resource;
  private final Reader reader;

  /**
   * Create a new {@link ResourceTemplateSource} for the given XP {@link Resource}.
   *
   * @param resource the XP {@link Resource} for the template.
   */
  public ResourceTemplateSource(Resource resource) {
    this.resource = resource;
    this.reader = this.resource.openReader();
  }

  /**
   * Get the last modification timestamp of the template.
   *
   * @return the last modification timestamp of the template.
   */
  public long getLastModified() {
    return this.resource.getTimestamp();
  }

  /**
   * Get a reader for reading the template content.
   *
   * @return the reader for reading the template content.
   */
  public Reader getReader() {
    return this.reader;
  }

  /**
   * Close the reader.
   *
   * @throws IOException if an error occurs closing the reader.
   */
  @Override
  public void close() throws IOException {
    this.getReader().close();
  }
}
