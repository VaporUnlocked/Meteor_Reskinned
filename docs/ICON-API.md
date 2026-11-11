# Icon system — Release 1

## Automatic support

No integration is needed for a normal `Category.icon` supplier. Reskin reads that supplier directly. `null`, `ItemStack.EMPTY` and caught supplier failures produce a vector fallback. The supplier is not evaluated every frame. Valid artwork is refreshed about every two seconds; an empty result is retried after about one second; caught failures are held for about thirty seconds. Appearance → Icons → Reload icon providers requests an immediate fresh lookup.

The artwork and its stack are not mutated or recolored. Items use `GuiRenderer.post` under the enclosing scissor, followed by `GuiRenderer.item` at the measured slot size with count overlays off. This matches the submission stage used by Meteor's `WItem`. `absolutePost` is deliberately not used for items in scrolling panels.

Category resolution, with supplied icons enabled:

1. Explicit category registration, when valid.
2. Native `Category.icon`, when valid.
3. Native-category vector or a generic addon symbol.

Module resolution:

1. Explicit module registration, when valid and supplied artwork is enabled.
2. A recognized built-in Meteor module's semantic vector, when that option is enabled.
3. The category resolution above.

Names are not enough to identify a native module: the module's implementation class must belong to Meteor's native module package. Arbitrary private fields in another addon are not inspected. A third-party icon system not exposed through `Category.icon` or this registration API is not automatically adopted.

## Optional integration

The public API is `io.github.cloudycity.meteorreskin.ReskinIcons`. A direct integration must declare Meteor Reskin as a dependency or isolate this code behind a load check so the JVM does not eagerly load missing classes when Reskin is absent. Register on the client thread; suppliers must only provide presentation data, not trigger gameplay.

The following methods are available:

```java
ReskinIcons.registerCategoryIcon(category, itemStackSupplier);
ReskinIcons.registerModuleIcon(module, currentModule -> itemStack);
ReskinIcons.registerCategorySymbol(category, ReskinIcons.Symbol.SHIELD);
ReskinIcons.registerModuleSymbol(module, ReskinIcons.Symbol.CAMERA);
```

For example, with your existing `category` and `module` instances:

```java
// A category-provided ItemStack already works without either of these calls.
ReskinIcons.registerCategoryIcon(category, category.icon);
ReskinIcons.registerModuleSymbol(module, ReskinIcons.Symbol.BOOK);
```

For a texture that your addon has packed using Meteor's renderer:

```java
ReskinIcons.registerCategory(category,
    () -> ReskinIcons.texture(myPackedGuiTexture, false));
```

`false` preserves texture colors; `true` tints with the selected Reskin accent. Supply an already valid Meteor `GuiTexture`, not a filename or an unmanaged GPU texture. Texture lifetime and registration in Meteor's packer remain the owning addon's responsibility. Lazy suppliers permit safe initialization timing.

The generic methods can return any of:

```java
ReskinIcons.item(itemStack);
ReskinIcons.texture(packedGuiTexture, tintWithAccent);
ReskinIcons.symbol(ReskinIcons.Symbol.PALETTE);
```

Remove an override with `registerCategory(category, null)` or `registerModule(module, null)`. `registerModuleIcon(module, null)` remains compatible with the beta API. `ReskinIcons.refresh()` clears cached values/failure cooldowns but keeps registrations; `invalidateCategoryIcons()` remains available for category caches. All identity-keyed registrations can be removed by the owning addon when no longer needed.

## Failure boundaries

Supplier exceptions and synchronous item-submission failures are handled without repeated per-frame log spam. An item that fails during the queued native call can leave a blank slot in that frame; subsequent frames use the fallback until the cooldown expires. The supplier cache itself is quarantined, so allocating a fresh stack cannot bypass that cooldown.

This is not protection against arbitrary native renderer/GPU failures occurring after extraction or against an addon performing unsafe actions inside its own supplier. The registration API is not a sandbox.

## Artwork

The 58 `Symbol` constants use original vector geometry, not a font. The mod-list PNG uses the same original Meteor Reskin mark. No third-party icons or fonts are redistributed. The inspection sheet in `test-results/icon-symbols.png` is a software render of the vector library, not a demonstration of Minecraft's item renderer.
