---
"@item-enonic-types/lib-freemarker": minor
---

`render()` can now take an XML document as the model: a string of XML, a `ResourceKey` pointing to an XML file, or a
`ByteSource`. The document is the root node for `[#recurse]` and `[#visit]`, so templates can process XML much like
XSLT, and it is also available in the template as `doc`. External entities and DTDs are never loaded.
