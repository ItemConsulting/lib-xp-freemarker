# @item-enonic-types/lib-freemarker

## 4.0.0

### Major Changes

- ceb8b84: `portal.localize` now resolves the default application from the app that renders the template, and no longer falls
  back to the application of the current portal request.
  
  This matches XP's own `/lib/xp/i18n`. Previously a template rendered by app B during a request belonging to app A
  looked for phrases in A. If you relied on that, pass the application explicitly:
  `portal.localize(key, locale, values, bundles, "com.example.app")`.
- ceb8b84: `render()` now requires the model to be an object.
  
  The model is dereferenced on the Java side, so `null` and `undefined` were never valid and failed at runtime. The
  type parameter is now `Model extends object`, which turns that into a compile-time error and lets the model type
  resolve cleanly under `strict`.
- ceb8b84: Support Enonic XP 8, and drop support for XP 7.

### Minor Changes

- ceb8b84: Template edits are now picked up immediately when XP runs in development mode, instead of being served from
  FreeMarker's cache until its update delay elapses.
- ceb8b84: Template names may now be a path inside the calling application, not only a full `<application>:<path>` resource URI.
  
  This makes absolute includes such as `<#include "/site/shared/fragment.ftlh">` work; FreeMarker strips the leading
  slash before the loader sees the name, so these previously could not be resolved. Relative includes are unaffected --
  FreeMarker already resolves those against the including template.

## 3.0.0

### Minor Changes

- f86f0df: Set incompatible improvements version to 2.3.34
- f86f0df: Allow configuration to be read from the freemarker.properties file
- f86f0df: Add functionality to render Freemarker from resources or inline strings
- 8ae9f7d: If no context found, use Accept-Language header to resolve the locale
- 43669e9: Require name argument when using `render` for inline templates.
- f86f0df: Use locale from current content being rendered
- 43669e9: Allow render to accept a URI as the view parameter, so that other TemplateLoaders can be used.
- f86f0df: Add portal object to access utility files
