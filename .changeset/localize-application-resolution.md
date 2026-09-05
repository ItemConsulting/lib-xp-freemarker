---
"@item-enonic-types/lib-freemarker": major
---

`portal.localize` now resolves the default application from the app that renders the template, and no longer falls
back to the application of the current portal request.

This matches XP's own `/lib/xp/i18n`. Previously a template rendered by app B during a request belonging to app A
looked for phrases in A. If you relied on that, pass the application explicitly:
`portal.localize(key, locale, values, bundles, "com.example.app")`.
