import type { ByteSource, ResourceKey, ScriptValue } from "@enonic-types/core";

export interface Configuration {
  setTemplateLoader(templateLoader: unknown): void;
  getTemplateLoader(): unknown;
  setSharedVariable(name: string, value: unknown): void;
  setTemplateExceptionHandler(templateHandler: unknown): void;
}

declare const xmlDocumentBrand: unique symbol;

/**
 * A parsed XML document. Only used to pass the document from `parseXml()` to the processor.
 */
type XmlDocument = { [xmlDocumentBrand]: true };

/**
 * An XML document: the XML itself as a string, a ResourceKey pointing to an XML file, or a ByteSource, such as an
 * attachment from `getAttachmentStream()` in `/lib/xp/content`.
 */
export type XmlSource = string | ResourceKey | ByteSource;

declare class FreemarkerService {
  newProcessor(): {
    process(view: string, model: ScriptValue): string;
    processInline(template: string, model: ScriptValue, name: string): string;
    processXml(view: string, xml: XmlDocument): string;
    processXmlInline(template: string, xml: XmlDocument, name: string): string;
    getSource(view: ResourceKey, beginColumn: number, beginLine: number, endColumn: number, endLine: number): string;
  };
  parseXml(source: XmlSource): XmlDocument;
  setPortalObject(portal: unknown): void;
  getConfiguration(): Configuration;
}

const service = __.newBean<FreemarkerService>("no.item.freemarker.FreemarkerScriptBean");

/**
 * Renders a FreeMarker template string with an XML document as the model. The document is the root node for
 * `[#recurse]` and `[#visit]`, and is also available in the template as `doc`.
 *
 * @param view The template content as a string
 * @param xml The XML document: a string of XML, a ResourceKey pointing to an XML file, or a ByteSource
 * @param name The name of the template as it will appear in error logs.
 * @returns The rendered template as a string
 */
export function render(view: string, xml: XmlSource, name: string): string;

/**
 * Renders a FreeMarker template with an XML document as the model. The document is the root node for
 * `[#recurse]` and `[#visit]`, and is also available in the template as `doc`.
 *
 * @param view A ResourceKey pointing to the template file or a URL that the template loader can resolve
 * @param xml The XML document: a string of XML, a ResourceKey pointing to an XML file, or a ByteSource
 * @returns The rendered template as a string
 */
export function render(view: ResourceKey | string, xml: XmlSource): string;

/**
 * Renders a FreeMarker template string with the provided model and configuration.
 *
 * @param view The template content as a string
 * @param model The data model to be used in the template
 * @param name The name of the template as it will appear in error logs.
 * @returns The rendered template as a string
 */
export function render<Model extends object>(view: string, model: Model, name: string): string;

/**
 * Renders a FreeMarker template with the provided model and configuration.
 *
 * @param view A ResourceKey pointing to the template file or a URL that the template loader can resolve
 * @param model The data model to be used in the template
 * @returns The rendered template as a string
 */
export function render<Model extends object>(view: ResourceKey | string, model: Model): string;

/**
 * Renders a FreeMarker template with the provided model and configuration.
 *
 * @param view Either a ResourceKey pointing to a template file or the template content as a string
 * @param model The data model to be used in the template, or an XML document to use as the model
 * @param name The name of the template as it will appear in error logs.
 * @returns The rendered template as a string
 */
export function render<Model extends object>(
  view: string | ResourceKey,
  model: Model | XmlSource,
  name?: string,
): string {
  const processor = service.newProcessor();
  const viewUri = typeof view === "string" ? view : view.getUri();

  if (isXmlSource(model)) {
    const xml = service.parseXml(model);

    return name && typeof view === "string"
      ? processor.processXmlInline(view, xml, name)
      : processor.processXml(viewUri, xml);
  }

  if (name && typeof view === "string") {
    return processor.processInline(view, __.toScriptValue(model), name);
  } else {
    return processor.process(viewUri, __.toScriptValue(model));
  }
}

/**
 * A string, or a Java ResourceKey or ByteSource. The Java objects are recognized by their methods, since they are not
 * JavaScript classes that `instanceof` could check. The parser does the exact type check.
 */
function isXmlSource(value: unknown): value is XmlSource {
  if (typeof value === "string") {
    return true;
  }

  const candidate = value as Partial<Record<"getUri" | "getApplicationKey" | "openStream", unknown>>;

  return (
    (typeof candidate.getUri === "function" && typeof candidate.getApplicationKey === "function") ||
    typeof candidate.openStream === "function"
  );
}

/**
 * Returns the source code for the specified region of the template.
 *
 * @param view        A ResourceKey pointing to the view
 * @param beginColumn the first column of the requested source, 1-based
 * @param beginLine   the first line of the requested source, 1-based
 * @param endColumn   the last column of the requested source, 1-based. If this is beyond the last character of the
 *                    line, it assumes that you want to whole line.
 * @param endLine     the last line of the requested source, 1-based
 */
export function getSource(
  view: ResourceKey,
  beginColumn: number,
  beginLine: number,
  endColumn: number,
  endLine: number,
): string {
  const processor = service.newProcessor();

  return processor.getSource(view, beginColumn, beginLine, endColumn, endLine);
}

/**
 * Returns the current configuration for the FreeMarker service.
 */
export function getConfiguration(): Configuration {
  return service.getConfiguration();
}
