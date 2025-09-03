import type { ResourceKey, ScriptValue } from "@enonic-types/core";

export interface Configuration {
  setTemplateLoader(templateLoader: unknown): void;
  getTemplateLoader(): unknown;
  setSharedVariable(name: string, value: unknown): void;
  setTemplateExceptionHandler(templateHandler: unknown): void;
}

declare class FreemarkerService {
  newProcessor(): {
    process(view: string, model: ScriptValue): string;
    processInline(template: string, model: ScriptValue, name: string): string;
    getSource(view: ResourceKey, beginColumn: number, beginLine: number, endColumn: number, endLine: number): string;
  };
  setPortalObject(portal: unknown): void;
  getConfiguration(): Configuration;
}

const service = __.newBean<FreemarkerService>("no.item.freemarker.FreemarkerScriptBean");

/**
 * Renders a Freemarker template string with the provided model and configuration.
 *
 * @param view The template content as a string
 * @param model The data model to be used in the template
 * @param name The name of the template as it will appear in error logs.
 * @returns The rendered template as a string
 */
export function render<Model>(view: string, model: Model, name: string): string;

/**
 * Renders a Freemarker template with the provided model and configuration.
 *
 * @param view A ResourceKey pointing to the template file
 * @param model The data model to be used in the template
 * @returns The rendered template as a string
 */
export function render<Model>(view: ResourceKey | string, model: Model): string;

/**
 * Renders a Freemarker template with the provided model and configuration.
 *
 * @param view Either a ResourceKey pointing to a template file or the template content as a string
 * @param model The data model to be used in the template
 * @param name The name of the template as it will appear in error logs.
 * @returns The rendered template as a string
 */
export function render<Model>(view: string | ResourceKey, model: Model, name?: string): string {
  const processor = service.newProcessor();

  if (name && typeof view === "string") {
    return processor.processInline(view, __.toScriptValue(model), name);
  } else {
    return processor.process(typeof view === "string" ? view : view.getUri(), __.toScriptValue(model));
  }
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
 * Returns the current configuration for the Freemarker service.
 */
export function getConfiguration(): Configuration {
  return service.getConfiguration();
}
