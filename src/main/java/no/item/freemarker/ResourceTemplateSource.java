package no.item.freemarker;

import com.enonic.xp.resource.Resource;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

public class ResourceTemplateSource implements Closeable {
  private final Resource resource;
  private final Reader reader;

  public ResourceTemplateSource(Resource resource) {
    super();
    this.resource = resource;
    this.reader = this.resource.openReader();
  }

  public long getLastModified() {
    return this.resource.getTimestamp();
  }

  public Reader getReader() {
    return this.reader;
  }

  @Override
  public void close() throws IOException {
    this.getReader().close();
  }
}
