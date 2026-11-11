# Meteor Reskin 1.1.0-beta.1 — validation report

## Result

The supplied binary compiled successfully and passed **20,530 assertions/checks** in the isolated regression harness. This is not a claim that every Minecraft UI interaction or third-party addon was tested.

| Area | Result |
| --- | --- |
| Production theme constructor and real Meteor setting builders | Passed |
| Four layout construction and software rendering paths | Passed |
| Layout matrix | 96 combinations: four layouts × six viewport sizes × four GUI-scale inputs |
| Viewports | 640×360, 854×480, 1280×720, 1666×896, 1920×1080, 3840×2160 |
| Every Neon category page, Appearance, shared panels | Passed across the matrix |
| Navigation | Ten registered tabs, including an addon tab, plus Appearance and Panels in all four layouts at 854×480 |
| Module interaction routing | Selecting does not toggle; switch calls existing toggle once; favorite/search/empty states and clipped clicks passed |
| Addon icons | Supplied, missing, null/empty, throwing, cached, Unicode-initial fallbacks passed |
| Dropdowns | 60 options; upward edge placement, horizontal containment, last option selection passed |
| Two-axis native-content viewport | Both axes reachable; scrollbar does not activate covered inputs; new focus reveals hidden field |
| Tooltip | Long identifiers, multiline wrapping, first-placement containment, reduced motion passed |
| Settings serialization | Real Meteor Settings/Setting round trip using in-memory NBT fixtures; selected style, custom colors, opacity and density retained |
| Binary API check | 810 addon/Meteor member references resolved against the unchanged original Meteor artifact |
| Runtime archive | 36 addon classes; zero dependency/test/compile-header classes packaged |

The deliberately failing icon supplier is a test fixture. Its single fallback log line is expected, not a startup error from a real addon.

## What is real in these tests

Production Reskin layout, drawing, navigation, icon, dropdown, tooltip and viewport classes are loaded from the shipped JAR. Meteor's WWidget, WContainer, WWindow, Cell, WPressable, Tab, Settings, Setting and relevant setting-builder implementations come from the supplied Meteor artifact. The test's class-version adapter does not rewrite their method bytecode.

## What is substituted

Minecraft/GUI services, base theme services, fonts and drawing submission, data registries, settings-to-widget conversion, item stacks, color/NBT representations and the screen manager use test fixtures. Software PNGs in `test-results/previews` run the production widget drawing code but do not show Minecraft's actual font renderer, item renderer, textures, game background or GPU output. They are watermarked accordingly.

The member checker does not resolve Minecraft/window/input/item methods or reflective access to the Gui screen manager. These external references are enumerated in `test-results/api-report.json`; they are not silently counted as verified Meteor methods. A Java enum ordinal reference is resolved as a standard-library method.

## Build environment and limitations

- Compiler available: JDK **21**. Addon bytecode target: **21**.
- Explicit offline Meteor API-header adapter used: **True**.
- Minimal compile-only Minecraft headers used: **True**.
- Required actual game runtime: **Java 25 / Minecraft 26.3 / Meteor 26.3-17**.
- **No complete Minecraft session or GPU validation was performed for this revision.**
- **The complete user addon set and all native setting types were not runtime-tested.**
- The manually triggered GitHub workflow is provided but was not run on GitHub here.

Use `docs/IN-GAME-CHECKLIST.md` before promoting this beta to a stable public release. No guarantee of flawless operation is made.

## Reference hashes

Meteor SHA-256: `9767e91e773ac5dfb27fdd29e5378e1ce41007dedd432db3a4223b3affb62e7f`

Release JAR SHA-256: `5051ce5b71be48f24b715d59f1bbacf2b8042a46deb62b59cf1d4926cca79d59`

See `test-results/results.txt` for the executed test output and `test-results/build-info.json` for machine-readable build provenance.
