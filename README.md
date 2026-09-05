# FreeMarker for Enonic XP

This library lets you use [Apache FreeMarker](https://freemarker.apache.org/)-templates with Enonic XP.

![Build badge](https://github.com/ItemConsulting/lib-xp-freemarker/actions/workflows/main.yml/badge.svg)
[![](https://repo.itemtest.no/api/badge/latest/snapshots/no/item/lib-xp-freemarker)](https://repo.itemtest.no/#/snapshots/no/item/lib-xp-freemarker)
[![](https://repo.itemtest.no/api/badge/latest/snapshots/no/item/lib-xp-freemarker?name=javadoc)](https://repo.itemtest.no/javadoc/snapshots/no/item/lib-xp-freemarker/latest)
[![](https://img.shields.io/npm/types/%40item-enonic-types%2Flib-freemarker)](https://www.npmjs.com/package/@item-enonic-types/lib-freemarker)

<img src="./docs/icon.svg?sanitize=true" width="150">

## Installation

To install this library you need to add a new dependency to your app's build.gradle file.

### Gradle

```groovy
repositories {
  maven { url "https://repo.itemtest.no/releases" }
  maven { url "https://repo.itemtest.no/snapshots" }
}

dependencies {
  include "no.item:lib-xp-freemarker:3.0.0-SNAPSHOT"
}
```

### TypeScript

To update the version of *enonic-types* in *package.json* using npm, run the following command:
```bash
npm i -D @item-enonic-types/lib-freemarker
```

You can add the following changes to your *tsconfig.json* to get TypeScript-support.

```diff
{
  "compilerOptions": {
+   "baseUrl": "./",
+   "paths": {
+     "/lib/xp/*": ["./node_modules/@enonic-types/lib-*"],
+     "/lib/*": [ "./node_modules/@item-enonic-types/lib-*" ,"./src/main/resources/lib/*"],
+   }
  }
}
```

### Add types for IntelliJ

If you are using IDEs from IntelliJ, you can get the correct type for the `portal` object by adding a file
_src/main/resources/freemarker_implicit.ftl_ with this content:

```ftl
[#ftl]
[#-- @implicitly included --]
[#-- @ftlvariable name="portal" type="no.item.freemarker.FreemarkerPortalObject" --]
```

### Configuration file

You can configure the [FreeMarker settings](https://freemarker.apache.org/docs/pgui_config_settings.html) by adding an
_XP_HOME/config/freemarker.properties_ file.

To preserve backwards compatibility in an existing project you can set the `incompatible_improvements` version like this:

```properties
incompatible_improvements=2.3.25
```

Developers should use the [HTML Debug Exception Handler](https://freemarker.apache.org/docs/pgui_config_errorhandling.html#autoid_44)
in their local development environments.

```properties
template_exception_handler=html_debug
```

> [!CAUTION]
> **Do not** use the `html_debug` exception handler in production!
> It should only be used for development as it shows technical information about your system.


## Usage

### Render from template files

You can use `resolve` to get the `ResourceKey` of a template file in your application. Then you can pass in a data model
to render that template.

```TypeScript
import { render } from "/lib/freemarker";
import { getContent } from "/lib/xp/portal";
import type { Response } from "@enonic-types/core";

type FreeMarkerParams = {
  title: string;
  text: string | undefined;
}

const view = resolve("article.ftl");

export function get(): Response {
  const content = getContent()!;

  const model: FreeMarkerParams = {
    title: content.data.title,
    text: content.data.text,
  };

  return {
    body: render<FreeMarkerParams>(view, model),
  };
};
```

### Render from inline strings

Alternatively you can use a `string` as the template.


```typescript
import { render } from "/lib/freemarker";
import { getContent } from "/lib/xp/portal";
import type { Response } from "@enonic-types/core";

type FreeMarkerParams = {
  title: string;
  text: string | undefined;
  year: string;
}

// The FreeMarker template as a string. Notice how the $ needs to be escaped.
const view = `
    [#import "/site/utils/footer.ftl" as Footer]

    <h1>\${title}</h1>

    [#if text?has_content]
      <p>\${text}</p>
    [/#if]

    [@Footer.render year=year /]
  `;

export function get(): Response {
  const content = getContent()!;
  const virtualTemplateName = "my-inline-template.ftl";

  const model: FreeMarkerParams = {
    title: content.data.title,
    text: content.data.text,
    year: "2025"
  };

  return {
    body: render<FreeMarkerParams>(view, model, virtualTemplateName),
  };
};
```

### The `portal` object

The following utility functions are made available in the
[portal object](https://repo.itemtest.no/javadoc/snapshots/no/item/lib-xp-freemarker/3.0.0-SNAPSHOT/raw/no/item/freemarker/FreemarkerPortalObject.html):

 - `portal.localize()`
 - `portal.processHtml()`

The portal-functions can be used within interpolations (`${}`). Example:

```ftl
[#-- @ftlvariable name="nextPageUrl" type="String" --]

<a href="${nextPageUrl}">
  ${portal.localize("article.nextPage")}
</a>
```

> [!WARNING]
> Always use the [`?no_esc`](https://freemarker.apache.org/docs/ref_builtins_string.html#ref_builtin_no_esc) built-in
> with `portal.processHtml()`. It prevents auto-escaping the markup if an output format is set.

### The `portal.component` directive

When rendering components from regions in _pages_ and _layouts_ you can use the `portal.component` directive.

```ftl
[#-- @ftlvariable name="mainRegion" type="com.enonic.xp.region.Region" --]

<div class="my-layout">
  [#list mainRegion.components as component]
    [@portal.component path=component.path /]
  [/#list]
</div>
```

If you are creating a view to preview [fragments](https://developer.enonic.com/docs/xp/stable/cms/pages/fragments),
you can use the `portal.component` directive like this:

```ftl
[@portal.component path="fragment" /]
```

## Localization

This library will use the first `locale` it finds, checking in the following order:

| Order | Source of `locale`                                                                          | Use case              |
|------:|---------------------------------------------------------------------------------------------|-----------------------|
|     1 | The `language` field of the current content                                                 | Normal content        |
|     2 | The `language` field of the current site                                                    | E.g. the error page   |
|     3 | The end users `"Accept-Language"` that matches a supported language in the _i18n_ directory | E.g. an admin tool    |
|     4 | Default to `"en-US"`                                                                        | E.g. the main.js file |

So you can use the [special variables](https://freemarker.apache.org/docs/ref_specvar.html) `${.locale}` and `${.lang}`
to access the "language tag" resolved based on the list above.

You also don't need to pass in a locale for `portal.localize()` unless you want a different locale then automatically
selected locale.

```ftl
<!doctype html>
<html lang="${.lang}">
  <body>
    <h1>The language tag is: ${.locale}</h1>

    <p>${portal.localize("article.text")}</p>
  </body>
</html>
```

If you need another `locale` to be used (han that of the current content), you can set it with the
[#setting directive](https://freemarker.apache.org/docs/ref_directive_setting.html).

```ftl
[#-- Changes the `locale` to Norwegian for the remainder of the template.  --]
[#-- This will affect the `portal.localize()` and date formatting. --]
[#setting locale="no"]

[#-- Setting the `time_zone` to get the correct time when formatting dates  --]
[#setting time_zone="Europe/Oslo"]
```

## Deploying

### Building

To build the project, run the following command

```bash
enonic project build
```

You will find the jar-file at _./build/libs/lib-xp-freemarker-[version].jar_

### Deploying locally

Deploy locally for testing purposes:

```bash
./gradlew publishToMavenLocal
```

## Releasing

Releases are driven by [Changesets](https://github.com/changesets/changesets) and run in GitHub Actions.

1. Describe your change in a changeset and commit it with the change itself:

   ```bash
   npx changeset
   ```

2. When that lands on `main`, the *Publish* workflow opens (or updates) a **Version Packages** pull
   request. It bumps the version in *package.json*, *package-lock.json* and *gradle.properties*, and
   folds the changesets into *CHANGELOG.md*.

3. Merging that pull request makes the same workflow do the release:

   - publish the type definitions to npm as `@item-enonic-types/lib-freemarker`, with
     [build provenance](https://docs.npmjs.com/generating-provenance-statements) attached
     automatically
   - tag the commit `v[version]` and create the matching GitHub release
   - publish the jar to <https://repo.itemtest.no/releases>

### Release credentials

npm is published with [trusted publishing](https://docs.npmjs.com/trusted-publishers): no token is
stored anywhere, the npm CLI trades the workflow's OIDC token for a short-lived one. It is
configured on npmjs.com under the package's *Settings > Trusted publisher*, pointing at
organization `ItemConsulting`, repository `lib-xp-freemarker` and workflow `publish.yml`. All three
are case-sensitive, and renaming the workflow file breaks publishing until it is updated there.

The Maven repository still uses credentials, so the repository needs two secrets:
`REPOSILITE_USERNAME` and `REPOSILITE_PASSWORD`.

### Releasing by hand

Only needed if the automated release fails. The Maven publish reads its credentials from
`itemtestRepositoryUsername` / `itemtestRepositoryPassword` in *~/.gradle/gradle.properties*.

```bash
./gradlew publishAllPublicationsToItemtestRepositoryRepository
npm publish
# npm publish --tag beta
```

## Acknowledgments

This library was inspired by the [lib-freemarker library by TINE IKT](https://github.com/tineikt/xp-lib-freemarker/).
