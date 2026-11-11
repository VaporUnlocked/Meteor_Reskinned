# Meteor Reskin — Release 1 (2.0.0)

**BY Cloudy_City** · Minecraft **26.3** · Meteor Client **26.3-17** · client-side only

Eight selectable UI workspaces for Meteor: Obsidian Command, Aurora Glass, Neon Dock, Slate Sidebar, Apex Workspace, Horizon Split, Atlas Panels, and Prism Studio. Existing module settings and callbacks remain Meteor's; Reskin does not register gameplay modules or change pathing, networking, combat, or movement behavior.

## Install / upgrade

Close Minecraft. Remove earlier Meteor Reskin or Carbon Columns JARs, then extract `meteor-reskin-26.3-17-2.0.0.jar` from the install ZIP into the same instance's `mods` folder. Keep your existing Meteor and Fabric API. Do not delete configuration, profiles, or activation flags. Only one Reskin version should be installed.

Open **Appearance → Icons** for artwork options, the installed-category preview list, and **Reload icon providers**. Native supplied artwork is enabled by default. An explicitly saved preference to disable supplied icons is retained; enable **Use provided category artwork** to restore native artwork.

## Release 1 icons

- Meteor/addon category icons are used automatically, without integrations or name allowlists. Explicit registrations can override a category deliberately.
- Native item icons now use Meteor's clipped post-render stage. The previous immediate submission could be covered by panel geometry.
- Addon modules inherit their category artwork unless an explicit per-module icon is registered. Recognized built-in Meteor modules can use distinct original semantic symbols; this is independently configurable.
- The original vector library contains 58 symbols for navigation, actions, categories and modules. Unknown/missing artwork uses a visible addon symbol instead of a blank square or unreadably small initials.
- Positive/empty results are cached briefly. Delayed suppliers recover automatically. Supplier and synchronous item-submission exceptions use a rate-limited fallback. Reloading the icon cache does not delete registrations or settings.
- Suppliers and registrations are keyed by object identity, so equal category names from different objects do not collide.
- Optional integrations support native item artwork, Meteor-packed textures and original vector symbols. See `docs/ICON-API.md`.

The supplied artwork is not recolored. Vector symbols use the active palette. Textured integrations explicitly choose whether their texture is tinted.

## Release cleanup

The runtime version is **2.0.0**, without a beta/prerelease suffix. The bottom help/status text is absent. Panels, Pin category and the cross-tab overlay remain removed. Navigation shows **METEOR RESKIN / BY Cloudy_City**, not the selected skin name. The author metadata also supplies Meteor's ordinary title-screen addon credit. Skin names remain in Appearance where they identify choices.

The corrected settings scrolling, null/empty-table handling, measured numeric widths, staged color editing, initial search focus, forgiving search, square-first chrome, eight layouts and compatibility isolation are retained.

## Verification and limits

The release passes the isolated regression suite at two font-metric profiles, including the exact icon-submission and blank-form regressions. The tests use actual Meteor `Category`, `WItem`, widget lifecycle, settings and color implementation bytecode where documented, with controlled game services and draw submission. The optional Baritone audit checks 34 direct binary references against the supplied `baritone-meteor-26.3.jar`.

**No complete Minecraft 26.3 / Java 25 / GPU session was run for this release.** Native item models, resource packs and the full addon collection have not been end-to-end tested here. Baritone was inspected and statically audited, not run. The source includes `TEST-REPORT.md`, the exact provenance, and a runtime checklist. Software previews are not Minecraft screenshots.

These workspaces follow the approved layout directions, not pixel-perfect concept imagery. They do not include GPU background blur, live world-preview scenes or freeform window docking.

## Build

Python 3.10+ and JDK 25 are recommended. Supply the exact Meteor JAR listed in `dependencies.lock.json`:

```sh
python build.py --meteor /path/to/meteor-client-26.3-17.jar
```

A mapped Minecraft client JAR can replace the compile-only game headers:

```sh
python build.py --meteor /path/to/meteor-client-26.3-17.jar --minecraft /path/to/minecraft-26.3-client.jar
```

The compiler available for this delivery was JDK 21. The explicit offline build was:

```sh
python build.py --meteor /path/to/meteor-client-26.3-17.jar --offline-java21-api-view
```

That opt-in tool creates a separate class-header compatibility view for the older compiler. It does not change the original dependency, emulate Java 25, or run Minecraft. Addon bytecode targets Java 21, while the Minecraft runtime still requires Java 25. Dependency copies and compile-only headers are never packaged into the addon.

Output: `dist/meteor-reskin-26.3-17-2.0.0.jar`.

## Tests

```sh
python tests/run.py --meteor /path/to/meteor-client-26.3-17.jar --offline-java21-api-view
python tests/run.py --meteor /path/to/meteor-client-26.3-17.jar --offline-java21-api-view --font-size 22
python tests/audit_baritone.py --meteor /path/to/meteor-client-26.3-17.jar --baritone /path/to/baritone-meteor-26.3.jar --out test-results/baritone-api-audit.json
```

Under JDK 25 omit the adapter flag. Optional `--icon-baseline` accepts the preflight JAR to reproduce the immediate item submission. `--scroll-baseline` / `--numeric-baseline` accept Beta 3, and `--table-baseline` accepts Beta 2. Baseline JARs are not redistributed.

## Public source distribution

GPL-3.0-only; see `LICENSE` and `NOTICE.md`. The source contains build/test tooling, compile-only headers, documentation and original icon assets. It contains no dependency/game JARs, fonts, user logs, credentials or private account information. The install ZIP contains one addon JAR. A manually triggered JDK 25 GitHub workflow is provided but has not been run here. No repository URL or claim of official Meteor endorsement is invented.
