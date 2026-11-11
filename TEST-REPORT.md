# Meteor Reskin — Release 1 (2.0.0) validation

## What changed

Native supplied item icons now use `GuiRenderer.post` inside their owning scissor, as Meteor's `WItem` does. The shipped preflight binary was tested first: it submitted one immediate item and zero deferred items. The release submitted zero immediate items and one deferred item after panel geometry. This verifies the scheduling correction, not Minecraft item-model pixels.

## Executed checks

| Area | Result |
| --- | --- |
| Actual Category and WItem classes | Loaded from the supplied Meteor dependency, not test substitutes |
| Original icon-submission regression | Reproduced against preflight; replacement passes |
| Icon placement matrix | 96 cases: 8 skins × 3 scale values × 4 icon slots; deferred submission and intersected nested clips |
| Icon resolution | Supplied native art, explicit item/vector/texture overrides, null/empty, equal-name identity isolation, native-module semantics |
| Icon recovery | Empty-at-start becomes valid, changing providers, mutated-to-empty items, thrown suppliers, queued item failures, fresh-stack allocation cooldown |
| Vector library | 58 symbols × 5 sizes = 290 finite/front-facing geometry checks |
| Icons studio | Category catalog and options constructed/rendered/navigated across all 8 skins |
| Existing layout matrix | 192 combinations: 8 skins × 6 window sizes × 4 GUI-scale inputs |
| Saved scale/density cases | 96 cases at 1920×1009 |
| Blank-menu regression | No blank viewport after the original 80-step wheel sequence |
| Native menu component stress | 240 structure cases; 17,217 frames per font profile |
| Table compatibility | 110 checks including empty/cleared/trailing rows and native-view ownership |
| Stable chrome | Actual stable binary omits footer; no Panels, overlay or skin-name header filler |
| Other retained regressions | Navigation, settings-first layout, numeric width, color drafts, search, focus, scroll endpoints, click isolation and triangle winding |
| Two font profiles | Comfortaa Java2D metrics at 18.6666666667 and 22 logical pixels; both passed |
| Addon/Meteor member check | 1334 references resolved against original Meteor and output addon |
| Baritone interface audit | 34 direct references resolved against the supplied exact Baritone artifact; no runtime execution |
| Runtime archive | 68 own classes; no dependency/stub/test/font classes or fonts packaged |

The normal-profile main suite reported 27,710 checks; the separate icon suite reported 1,612. The larger profile reported 28,264 main and 1,597 icon checks. Counts vary with wrapping and visible-widget bounds; they are not counts of Minecraft sessions or unique menus.

Expected fallback warnings come from deliberately failing test providers. They are not observed errors from the user's installed addons.

## Scope and remaining limits

**No complete Minecraft 26.3 / Java 25 / GPU session was run for this release.** Game services, data registries, portions of setting conversion, text metrics, native item models and draw submission are substituted in the harness. Category/WItem and the documented Meteor lifecycle, settings, color and table bytecode are real. Test-side deferred scheduling models the inspected per-scissor contract. Item-submission fixtures draw placeholders, not actual Minecraft models.

The reference JAR inventory does not establish that every native or custom addon screen was instantiated. The supplied Baritone JAR is present and its direct interface was audited; it was not run in game. Reflective conversions, resource packs, GPU/render-pipeline details and the complete addon combination remain unverified here. No gameplay routines or proxy connections were executed. See `docs/IN-GAME-CHECKLIST.md`.

## Build provenance

Compiler: JDK 21. Addon bytecode target: 21. Explicit Java 21 API-header view: True. Compile-only Minecraft headers: True. Actual required game runtime: Java 25. The original dependencies are unmodified and not redistributed. The manually triggered JDK 25 GitHub workflow is provided but not executed here.

Meteor SHA-256: `9767e91e773ac5dfb27fdd29e5378e1ce41007dedd432db3a4223b3affb62e7f`

Baritone SHA-256: `8804988ac4f6c70871a174c3c90002bfcdfc485b59f96d43f4b4ec942820f2b4`

Release JAR SHA-256: `0b11d5e6f0fc40fb10f73404c3e20abc7e9d5240f10c5d75bdb9f87cbc573977`

See the executed logs and machine-readable reports in `test-results/`.

## Clean-source verification

The source package was extracted into a separate directory, rebuilt, and the complete normal-font regression suite rerun successfully. After refreshing the packaged notice in the original build, the install JAR and clean-source JAR matched byte-for-byte. No class entries differed during that packaging check. The final archive was extracted and rebuilt once more to verify the exact delivered package.
