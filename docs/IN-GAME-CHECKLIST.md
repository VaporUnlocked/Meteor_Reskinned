# Release 1 runtime checklist

Version: Meteor Reskin 2.0.0 / Minecraft 26.3 / Meteor 26.3-17 / Java 25.

The preflight's spacing and blank-form corrections were reported working by the user. The new Release 1 icon path has isolated regression coverage but has not been launched inside Minecraft here. These checklist rows are not marked as passed by software tests.

| Area | Runtime checks | Release 1 in-game status here |
| --- | --- | --- |
| Category artwork | Check core and addon-provided icons in all eight skins, scrolling, resource reload and scale changes | Not run |
| Icons studio | Native-art toggle, native-module symbol toggle, reload providers, absent icons | Not run |
| Config / GUI / HUD | Scroll to both endpoints, collapse first/middle groups, conditional visibility, resize | Not run |
| Friends / Macros / Profiles | Empty and populated lists, editors, binding capture, return navigation | Not run |
| Accounts / Proxies | Empty/populated lists, add/edit/remove without sharing credentials | Not run |
| Baritone | Actual addon settings, nested lists/selectors, scrolling and return paths | Static interface audited; runtime not run |
| Module settings | Numeric typing, sliders, resets, colors, lists, keybinds, custom widgets | Not run |
| Custom proxy-list addon | Original failing menu, empty rows, add/edit/remove and scrolling | Shared table regression passed; full addon not run |

For every menu: wheel down/up, drag scrollbar, collapse a group above the viewport, change a setting which hides controls, resize, close/reopen. Verify clipped controls do not respond to clicks and focus stays reachable. No gameplay automation or proxy connection needs to be enabled to inspect menus.

Stable chrome: no Panels/Pin category, no skin-name filler in the navigation header, no bottom help/status footer; brand and author credit remain. Keep configuration and profiles. Report version, addon filename and GUI/font scale with any failure; do not include access tokens or account credentials.
