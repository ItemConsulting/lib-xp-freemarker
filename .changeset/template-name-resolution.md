---
"@item-enonic-types/lib-freemarker": minor
---

Template names may now be a path inside the calling application, not only a full `<application>:<path>` resource URI.

This makes absolute includes such as `<#include "/site/shared/fragment.ftlh">` work; FreeMarker strips the leading
slash before the loader sees the name, so these previously could not be resolved. Relative includes are unaffected --
FreeMarker already resolves those against the including template.
