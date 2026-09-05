package no.item.freemarker;

import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceTemplateLoaderTest {
  private static final String APP = "no.item.freemarker.test";
  private static final ResourceKey BASE = ResourceKey.from(APP + ":/site/parts/hello/hello.js");

  @Mock
  private ResourceService resourceService;

  @Mock
  private Resource resource;

  private ResourceTemplateLoader loader() {
    return new ResourceTemplateLoader(() -> resourceService, BASE);
  }

  /**
   * Make {@code uri} resolve to an existing resource.
   */
  private void existsAt(String uri) {
    when(resource.exists()).thenReturn(true);
    when(resourceService.getResource(ResourceKey.from(uri))).thenReturn(resource);
  }

  // ── name resolution ───────────────────────────────────────────────────────

  @Test
  void shouldResolveFullResourceUri() {
    existsAt(APP + ":/site/parts/hello/hello.ftlh");

    assertNotNull(loader().findTemplateSource(APP + ":/site/parts/hello/hello.ftlh"));
  }

  @Test
  void shouldResolveUnprefixedNameAgainstCallingApplication() {
    existsAt(APP + ":/site/shared/fragment.ftlh");

    // FreeMarker strips the leading slash before handing the name to the loader
    assertNotNull(loader().findTemplateSource("site/shared/fragment.ftlh"));
  }

  @Test
  void shouldResolveResourceFromAnotherApplication() {
    existsAt("com.example.other:/site/shared/fragment.ftlh");

    assertNotNull(loader().findTemplateSource("com.example.other:/site/shared/fragment.ftlh"));
  }

  @Test
  void shouldReturnNullWhenNameCannotBeResolvedWithoutABaseKey() {
    ResourceTemplateLoader loaderWithoutBase = new ResourceTemplateLoader(() -> resourceService, null);

    assertNull(loaderWithoutBase.findTemplateSource("site/parts/hello/hello.ftlh"));
    verify(resourceService, never()).getResource(any());
  }

  // ── missing templates ─────────────────────────────────────────────────────

  @Test
  void shouldReturnNullWhenTemplateDoesNotExist() {
    // XP hands back a Resource for any key; whether the file is there is only visible through exists()
    when(resource.exists()).thenReturn(false);
    when(resourceService.getResource(any())).thenReturn(resource);

    assertNull(loader().findTemplateSource(APP + ":/site/parts/hello/missing.ftlh"));
    assertNull(loader().findTemplateSource("site/parts/hello/missing.ftlh"));
  }

  @Test
  void shouldReturnNullWhenResourceServiceReturnsNothing() {
    when(resourceService.getResource(any())).thenReturn(null);

    assertNull(loader().findTemplateSource(APP + ":/site/parts/hello/missing.ftlh"));
  }

  // ── reading ───────────────────────────────────────────────────────────────

  @Test
  void shouldReadTemplateThroughTheResource() throws IOException {
    existsAt(APP + ":/site/parts/hello/hello.ftlh");
    when(resource.openReader()).thenReturn(new StringReader("<h1>Hello</h1>"));

    ResourceTemplateLoader loader = loader();
    Object source = loader.findTemplateSource(APP + ":/site/parts/hello/hello.ftlh");

    try (Reader reader = loader.getReader(source, "UTF-8")) {
      assertEquals("<h1>Hello</h1>", readFully(reader));
    }
  }

  @Test
  void shouldExposeLastModifiedFromResource() {
    existsAt(APP + ":/site/parts/hello/hello.ftlh");
    when(resource.getTimestamp()).thenReturn(1234L);

    ResourceTemplateLoader loader = loader();
    Object source = loader.findTemplateSource(APP + ":/site/parts/hello/hello.ftlh");

    assertEquals(1234L, loader.getLastModified(source));
  }

  @Test
  void shouldCloseTheReaderWithTheTemplateSource() throws IOException {
    existsAt(APP + ":/site/parts/hello/hello.ftlh");
    Reader reader = new StringReader("<h1>Hello</h1>");
    when(resource.openReader()).thenReturn(reader);

    ResourceTemplateLoader loader = loader();
    Object source = loader.findTemplateSource(APP + ":/site/parts/hello/hello.ftlh");

    loader.closeTemplateSource(source);

    assertThrows(IOException.class, reader::read);
  }

  private static String readFully(Reader reader) throws IOException {
    StringBuilder builder = new StringBuilder();
    int c;
    while ((c = reader.read()) != -1) {
      builder.append((char) c);
    }

    return builder.toString();
  }
}
