# Meteor Reskin — Release 1 architecture

## Integration and safety of state changes

`ReskinAddon` registers one theme using Meteor's addon entrypoint. Selection occurs after GUI configuration restoration. The existing first-run marker is retained. No gameplay module registration, network listener or gameplay replacement is introduced.

`ReskinTheme` owns eight enum layouts and appearance settings. Explicit `chooseStyle` applies a palette and square panel defaults; loading serialized settings does not reapply defaults over custom colors. `beforeRender` synchronizes shared widget colors and normalizes legacy excessive corner radii.

## Layouts

`ReskinModulesScreen.Workspace` reads the live registries. It owns the category rail, normalized search, module view, favorite/summary panels and priority inspector. Obsidian uses `ReskinGroupHeading` accordions; Apex uses real module cards and a summary; Neon has category docks and bottom navigation; Slate/Prism use side navigation; Atlas adds a favorites pane; Horizon enlarges the inspector; Aurora uses a roomier translucent card treatment.

`ReskinTopBar` measures caption widths using Meteor's font metrics, reserves essential controls, and pages registered tabs. It chooses horizontal, two-row, left-side or bottom placement. Existing pointer-priority routing is retained so underlying windows cannot consume navigation clicks.

`ReskinScrollPane` is the fixed list/card viewport. `ReskinView` is the native-compatible settings viewport; its default is vertical-only. `ReskinSettingsFactory` uses Meteor's native/custom setting factories but lays simple booleans/enums beside labels and complex controls below wrapped labels. `ReskinReflow` handles tables, horizontal rows, sliders, text inputs and stacked containers. It preserves callbacks rather than reproducing setting semantics.

Meteor's WContainer layout method performs a second child traversal. Width-aware custom form/row/view containers override the complete positioning method so this traversal cannot restore native natural widths after reflow. `ReskinWindows` keeps the attach-before-view-access construction order that fixed the earlier Carbon null-view crash.

## Search / colors

`ReskinSearch` normalizes Unicode case/accents and non-alphanumeric separators, supports compact substring matching and unordered tokens. It executes no user-supplied regex. Titles, names, aliases, descriptions and setting labels are searched globally when a query is nonempty.

`ReskinColorDraft` stores primitive channel drafts. `ReskinColorField` commits only after explicit Apply. `ReskinColorScreen` uses a separate copy with HSV/alpha controls and a vertical viewport. Apply constructs a fresh SettingColor; cancel/escape discards the draft. Native ColorSetting values shown by the new factory route through this editor. A custom addon that constructs its own color screen can bypass that route.

## Rendering and icons

`ReskinDraw` contains square-first layered panels, optional subtle rounding, gradients and decorative ribbons. All triangles are normalized to the negative signed screen-space area used by native filled quads, because Meteor UI_COLORED enables face culling. `ReskinGlyph` supplies original line/filled icons without font assets. No font files, shader dependency or simulated world preview is used. `ReskinText` gives stationary ellipsis and clipped hover reveal. `ReskinWrap`/`ReskinParagraph` wrap long labels without shrinking text. Dropdowns and tooltips use bounded layouts.

`ReskinIcons` uses identity-keyed category/module registrations and native category icon suppliers. Original vector fallbacks replace missing artwork. Native ItemStacks use the clipped `GuiRenderer.post` stage before `item`, matching `WItem`. Expiring positive/negative caches and failed-source cooldowns avoid permanent initialization failures and repeated render exceptions. `Appearance → Icons` exposes category previews and cache refresh. See `docs/ICON-API.md` for priority, texture ownership, and failure boundaries. The stable build does not submit the prerelease footer.

## Tests

The isolated harness loads production addon classes and supplied Meteor Category/WItem, widget lifecycle, table, settings and color-class bytecode. Game services, native settings-to-widget conversion, fonts/drawing (now including strict triangle-winding rejection), registry data, NBT storage and small fastutil/JOML signature dependencies are fixtures. The JDK 21 header adapter changes class version headers only. No complete Minecraft session or GPU behavior is claimed by these tests.

## Regression evidence

`ReskinVisualBaseline` runs unchanged against beta.1 and beta.2. The rejected JAR produces opposite-winding corner, star and toggle triangles and truncates GUI at a 0.01-pixel fit boundary. The replacement passes these exact probes. `ReskinFidelityRegression` also verifies core captions, category readability, sorting, native gear actions, zero-radius panels and view structures.

`docs/RENDERING-DIAGNOSIS.md` records the native binary evidence. Matching native winding addresses that specific defect; it does not certify GPU blending, antialiasing, font rendering or screenshots.
