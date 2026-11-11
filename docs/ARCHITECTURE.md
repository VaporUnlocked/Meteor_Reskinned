# UI architecture

`ReskinAddon` registers one theme through the normal Meteor addon entrypoint. Its post-init selection runs after Meteor GUI configuration restoration. A first-install marker is only created after successful selection. There are no gameplay module registrations or network listeners.

`ReskinTheme` owns the four-layout enum and settings. `chooseStyle` is an explicit UI action that applies a default palette; merely loading `stylePreset` only invalidates layout. `beforeRender` synchronizes shared widget colors without invoking setting-change callbacks.

`ReskinModulesScreen.Workspace` reads existing module/category instances and owns search, filters, the category rail, main content and inspector. `ReskinModuleCard` is a view of one existing instance. Its switch calls `Module.toggle`; selecting the card does not toggle. The native module screen remains the route for custom addon widgets, keybinds and configuration sharing.

`ReskinTopBar` enumerates actual tabs and lays them out at the top, bottom, or left, with paging when needed. A pointer-routing layer prevents an underlying native window from stealing navigation clicks. Cross-tab panels are lazily constructed and rendered in a late pass.

`ReskinScrollPane` handles list/grid viewports and filters events against the visible region. `ReskinView` is a WView-compatible two-axis viewport for native settings. Scrollbar tracks have reserved space; focused descendants can be revealed without activating them. `ReskinWindows` remains the single attach-before-view-access construction path for docked native windows.

`ReskinDraw` draws original rounded geometry. `ReskinText` handles clipped hover text with cached measurement and resets on content/size/scale changes. `ReskinDropdown` preserves native option callbacks but provides a bounded scrolling popup. `ReskinTooltip` wraps text before first placement.

`ReskinIcons` uses the supplied category icon when possible, otherwise initials. The optional module-icon registry is a UI extension, not a gameplay hook. Calls to the registry should occur on the Minecraft client thread.

The regression harness loads production addon classes and the supplied Meteor widget lifecycle/Setting bytecode. Minecraft services, base-theme services, settings-to-widget conversion, colors/NBT representations, data registries and drawing submission are fixtures. The harness is deliberately not described as a game launcher.
