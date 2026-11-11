# Meteor Reskin

**BY Cloudy_City** · **1.1.0-beta.1** · Minecraft **26.3** · Meteor Client **26.3-17**

Four distinct menu layouts in one client-side Meteor addon. This is a UI-only project: it adds no gameplay modules, packet hooks, automation, commands, or replacement gameplay implementations. Existing module switches and settings call the existing Meteor instances.

## The four layouts

| Layout | Structure |
| --- | --- |
| **Obsidian Command** | Category rail, grouped module list, and a contextual settings inspector. |
| **Neon Dock** | Bottom navigation dock, paged category panels, independent scrolling, and purple accent styling. |
| **Slate Sidebar** | Left-side navigation, category list, module list, and a wider settings inspector. |
| **Apex Workspace** | Adaptive module-card grid, category explorer, actual module/enabled/favorite counts, and a settings inspector. |

On small viewports, the inspector is replaced by the full native settings screen and categories remain reachable through the **Categories** button. The layout changes; module behavior does not.

The earlier 1.0.0 prototype primarily changed palettes. This revision replaces its module workspace and navigation with the four structures above. It is not a pixel-perfect transcription of the concept art. Illustrative live previews, simulated graphs and invented module controls from that art are not shipped.

## Install

1. Close Minecraft.
2. Remove older **Meteor Reskin** and **Carbon Columns** JARs from the instance's `mods` folder. Do not leave both installed; the metadata explicitly rejects Carbon Columns to prevent duplicate UI registrations.
3. Extract the release ZIP and put its single `meteor-reskin-26.3-17-1.1.0-beta.1.jar` in `mods`.
4. Retain Meteor Client `26.3-17`, the Fabric loader, and the Fabric API already used by the instance.
5. Restart. On first installation the theme is selected after Meteor restores GUI configuration. If another theme is deliberately selected later, it is not forcibly replaced on subsequent starts. Select **Meteor Reskin** in Meteor's GUI theme selection to return to it.

Do not delete profiles, module settings, or the Meteor configuration folder. No gameplay configuration migration is performed.

The runtime requires **Java 25** and Fabric Loader **0.19.5 or later**, as declared in the addon metadata. The addon is pinned to Meteor `26.3-17`; an arbitrary later build is not certified.

## Use and customize

Open **Appearance** in Reskin navigation and choose one of the four named buttons. An explicit style-button selection applies that style's default palette while retaining density, motion preferences and the category-column limit. Colors can then be customized. Loading saved settings does not reapply a palette or erase custom colors.

The Appearance workspace includes accent, surface, header, text, secondary text, title text, border, hexadecimal RGBA editing, native color pickers, density, corner radius, Minecraft GUI-scale following, reduced motion, icon preference, Neon accent halo, and the cross-tab panel toggle. Use the regular Meteor GUI settings for its existing font controls.

- **Module name/body:** inspect the module; on compact layouts and Neon Dock, open its native settings screen.
- **Switch on the right:** call that module's native toggle once.
- **Star on the left:** change its existing favorite flag.
- **Right click:** open the full native module screen.
- **Full settings:** access native keybinds, config sharing and addon-specific custom controls; they are not approximated by substitute gameplay settings.
- **Panels:** open or close the existing-category shelf on other tabs. The arrows change the shown category; **Pin category** picks the inspected module's category.
- **Search:** searches module names, descriptions and setting titles. All / Favorites / Enabled filter the existing registry.

Labels use clipped hover scrolling. Reduced motion substitutes stationary text and a full-text tooltip. Long tooltip text wraps, including unbroken identifiers; exceptionally tall tooltips scroll automatically when reduced motion is off. Content scrollbars occupy reserved space instead of covering controls.

## Addon categories and icons

Categories and navigation tabs are enumerated from Meteor's registered instances. There is no hardcoded six-category restriction. An addon category's `Supplier<ItemStack>` icon is used when enabled and valid. Missing suppliers, null results, empty item stacks or throwing suppliers fall back to readable initials. Broken icon suppliers are cached as unavailable to avoid per-frame exceptions. Category navigation remains usable regardless of icon availability.

Meteor's base Module API does **not** have a universal module-icon member. Modules normally inherit their category icon. A presentation addon can opt into the small API below on the client thread:

```java
ReskinIcons.registerModuleIcon(existingModule, module -> itemStack);
// Remove a provider:
ReskinIcons.registerModuleIcon(existingModule, null);
// Refresh late-created category icons:
ReskinIcons.invalidateCategoryIcons();
```

Third-party icons stored in an unrelated private API are not automatically guessed or extracted. Third-party screens that bypass Meteor's theme/widget system may retain their own rendering.

## Branding

The addon name and theme name are **Meteor Reskin**. Its navigation header reads **METEOR RESKIN / BY Cloudy_City**. The metadata author is `Cloudy_City`, which Meteor can display in its existing title-screen addon-credit list when that list is enabled. No fictional repository URL or update endpoint is embedded.

## Build from source

The canonical, dependency-light build uses Python 3 and a **JDK 25** installation on PATH:

```sh
python build.py --meteor /path/to/meteor-client-26.3-17.jar
```

Output: `dist/meteor-reskin-26.3-17-1.1.0-beta.1.jar`.

For compilation against a real mapped Minecraft client artifact rather than the six minimal compile headers:

```sh
python build.py --meteor /path/to/meteor-client-26.3-17.jar --minecraft /path/to/minecraft-26.3-client.jar
```

The compile headers under `stubs/` are never packaged. The JAR contains only this project's classes, metadata and GPL license. Minecraft, Meteor, Fabric API, fonts and other third-party binaries are not redistributed here.

### Exact build provenance for this supplied binary

The available build environment had JDK 21, no Minecraft runtime, and could not resolve the JDK download host. The supplied binary was therefore built using the explicit offline tooling mode also used in the working 3.0.1 lineage:

```sh
python build.py --meteor /path/to/meteor-client-26.3-17.jar --offline-java21-api-view
```

This creates a private compiler-view copy of the supplied Meteor JAR with Java 21 class-version headers. It does **not** alter the original artifact or include that copy in the release. No dependency method bytecode is patched. Addon classes target Java 21; the actual Minecraft runtime requirement stays Java 25. This workaround is not evidence of an in-game Java 25 test. Prefer a normal JDK 25 rebuild for a public release candidate.

`build/build-info.json` records the compiler, adapter use and SHA-256 values. `dependencies.lock.json` records the exact reference artifact hashes.

## Tests

```sh
python tests/run.py --meteor /path/to/meteor-client-26.3-17.jar
# Only for JDK 21 tooling:
python tests/run.py --meteor /path/to/meteor-client-26.3-17.jar --offline-java21-api-view
```

See **TEST-REPORT.md** for what passed and what was not tested. The source includes the harness and its results. `test-results/previews/` contains software widget renders produced by production UI code with substitute fonts/drawing and fixture module data. These are explicitly marked and are **not Minecraft screenshots**.

## GitHub publication

Extract this source ZIP into the repository root; keep `src`, `tests`, `stubs`, `build.py`, the license and notices. Do not commit your `mods` folder, user logs or private game configuration. `.gitignore` excludes build outputs, archives and dependency JARs.

A manually triggered `.github/workflows/build.yml` is included. Supply a public HTTPS URL for the exact Meteor reference artifact when running it. It verifies SHA-256 before building with JDK 25, runs the headless tests and uploads the compiled JAR as an Actions artifact. No URL is fabricated and nothing is published automatically. The workflow has not been executed in GitHub from this environment.

## Known limits and release status

This is a **beta**, not a flawless or fully certified release. No full Minecraft 26.3 / Java 25 / GPU session was available for this revision. Native GPU text/texture rendering, actual DPI behavior, all 33 native setting types and the user's complete installed addon set still need in-game acceptance testing. The headless settings-to-widget converter is a test double; the production addon delegates to Meteor's real factory.

Standard Meteor theme-based pages share the new controls and palette. This is not a replacement of every vanilla Minecraft screen, every HUD editor overlay, or arbitrary custom-rendered addon screens. No live per-module world preview or performance chart is claimed. The native full module editor is deliberately retained for complete addon control coverage.

## License

GPL-3.0-only. See `LICENSE` and `NOTICE.md`.
