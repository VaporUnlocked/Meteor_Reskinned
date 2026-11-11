# Release 1 — 2.0.0

- Fixed native item-icon submission: use Meteor's per-scissor post stage rather than draw beneath the panel mesh.
- Honor category suppliers automatically, including categories whose names match built-in labels.
- Added identity-keyed item, packed-texture and vector integrations for categories/modules, with backwards-compatible module registration.
- Added 58 original vector symbols, distinct built-in module symbols, category header artwork and a mod-list PNG.
- Added Appearance → Icons: native-art and semantic-symbol options, installed-category previews, cache refresh.
- Added short-lived positive/empty caches, recoverable supplier failures and producer cooldowns after native submission failures.
- Removed prerelease footer in the actual stable binary; Panels and header skin-name filler remain absent.
- Preserved existing configuration, table isolation, blank-form scrolling fixes, numeric widths, colors, search and eight skins.
- Added icon-order baseline, native WItem/Category tests and optional static Baritone audit. Full Minecraft/GPU validation is not claimed.

---

## Earlier development history

# 2.0.0-beta.4-preflight.1 — validation candidate, official Beta 4 held

- Removed Panels, its overlay/flag and Pin category.
- Removed skin names from navigation headers; retained brand/author.
- Made footer prerelease-only with generated build version and stable-binary regression check.
- Reproduced/fixed blank settings forms after scrolling beyond a collapsed first group.
- Preserved the previous addon table-ownership fix.
- Added native fullscreen-root component stress: 240 cases, 17,217 rendered frames per font profile.
- Preserved numeric input widths, wrapped sliders at narrow sizes, refined card header spacing and pinned collection shortcuts.
- Exact Baritone addon and full Minecraft menu verification remain pending.

---

# 2.0.0-beta.3

- Null-safe WTable reflow for empty, trailing and cleared rows.
- Preserve native/addon view layout; only owned inspector opts into reflow.
- Invoke native child positioning in non-reflow views.
- Reference viewport sizing and per-skin card/column caps.
- Integrated Slate category sidebar; no mid-word category wrapping.
- Readable native search placeholder; clearer switches and explicit Neon page controls.
- Initial inspector selection belongs to first displayed category.
- Dependency-font metric profiles and saved-scale regression cases.

# Changelog

## 2.0.0-beta.2 — 2026-11-11

- Correct opposite-winding corner/star geometry to match Meteor 26.3-17 UI_COLORED face culling. Route all addon triangles through one orientation-normalizing helper.
- Add strict front-face checks to software draw submission and a baseline probe that reproduces beta.1 failures before accepting the replacement.
- Default to square panels; remove forced outer-frame rounding. Migrate legacy radius values above four to zero without clearing palettes or profiles.
- Allocate complete navigation captions with icons, padding, integer-ceiling widths and a consistent subpixel fit rule. Recalculate after font metric changes.
- Add original geometric iconography, stronger spacing hierarchy and understated layered panel fills.
- Widen category rails; wrap long names, preserve addon icons and readable fallbacks.
- Recompose all eight skins; give Obsidian a six-row accordion overview with explicit access to full categories, Apex a card dashboard, Atlas a real table and tools, Neon category tiles and bottom dock, and distinct Slate/Horizon/Aurora/Prism structures.
- Add working A–Z/Z–A/Enabled-first sorting and grid/list switching; preserve normalized global search and autofocus.
- Compact settings into inline simple rows or vertically reflowed complex controls, instead of oversized bordered boxes. Keep inspector priority without requiring it to consume more width than the entire module browser.
- Give card gear icons a dedicated native-editor action; clicking them does not toggle a module.
- Retain transactional colors, dynamic addon tabs/categories, lifecycle initialization fixes and vertical-only inspector scrolling.
- Validate 192 layout/viewport/scale combinations plus targeted typography, navigation, geometry and interaction regressions. No full game session performed.

## 2.0.0-beta.1 — prior rejected visual iteration

Eight initial layout variants, transactional color editor, normalized search and vertical settings reflow. Its software rendering lacked culling checks, so passing those tests did not establish correct native geometric appearance. Replaced by beta.2.
