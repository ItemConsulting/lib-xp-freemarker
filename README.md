# Freemarker for Enonic XP

This library lets you use [Apache Freemarker](https://freemarker.apache.org/)-templates with Enonic XP.

[![](https://repo.itemtest.no/api/badge/latest/releases/no/item/lib-xp-freemarker)](https://repo.itemtest.no/#/releases/no/item/lib-xp-freemarker)

<img src="./docs/icon.svg?sanitize=true" width="150">

## Installation

To install this library you need to add a new dependency to your app's build.gradle file.

### Gradle

```groovy
repositories {
  maven { url "https://repo.itemtest.no/releases" }
}

dependencies {
  include "no.item:lib-xp-freemarker:2.1.0-SNAPSHOT"
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

You can configure the [Freemarker settings](https://freemarker.apache.org/docs/pgui_config_settings.html) by adding an
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

type FreemarkerParams = {
  title: string;
  text: string | undefined;
}

const view = resolve("article.ftl");

export function get(): Response {
  const content = getContent()!;

  const model: FreemarkerParams = {
    title: content.data.title,
    text: content.data.text,
  };

  return {
    body: render<FreemarkerParams>(view, model),
  };
};
```

### Render from inline strings

Alternatively you can use a `string` as the template.


```typescript
import { render } from "/lib/freemarker";
import { getContent } from "/lib/xp/portal";
import type { Response } from "@enonic-types/core";

type FreemarkerParams = {
  title: string;
  text: string | undefined;
  year: string;
}

export function get(): Response {
  const content = getContent()!;

  const view = `
    [#import "/site/utils/footer.ftl" as Footer]

    <h1>\${title}</h1>

    [#if text?has_content]
      <p>\${text}</p>
    [/#if]

    [@Footer.render year=year /]
  `;

  const model: FreemarkerParams = {
    title: content.data.title,
    text: content.data.text,
    year: "2025"
  };

  return {
    body: render<FreemarkerParams>(view, model),
  };
};
```

### The `portal` object

The following utility functions are made available in the
[portal object](./src/main/java/no/item/freemarker/FreemarkerPortalObject.java):

 - `portal.pageUrl()`
 - `portal.assetUrl()`
 - `portal.imageUrl()`
 - `portal.attachmentUrl()`
 - `portal.componentUrl()`
 - `portal.serviceUrl()`
 - `portal.loginUrl()`
 - `portal.logoutUrl()`
 - `portal.localize()`
 - `portal.processHtml()`
 - `portal.imagePlaceholder()`

The portal-functions can be used within interpolations (`${}`). Example:

```ftl
[#-- @ftlvariable name="nextPageId" type="String" --]

<a href="${portal.pageUrl(nextPageId)}">
  ${portal.localize("article.nextPage")}
</a>
```

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

You will find the jar-file at _./build/libs/item.jar_

### Deploying locally

To deploy to a local sandbox, run the following command

```bash
./gradlew publishToMavenLocal
```

### Deploy to Maven

```bash
./gradlew publish -P com.enonic.xp.app.production=true
```
