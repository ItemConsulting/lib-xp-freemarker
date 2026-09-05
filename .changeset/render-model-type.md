---
"@item-enonic-types/lib-freemarker": major
---

`render()` now requires the model to be an object.

The model is dereferenced on the Java side, so `null` and `undefined` were never valid and failed at runtime. The
type parameter is now `Model extends object`, which turns that into a compile-time error and lets the model type
resolve cleanly under `strict`.
