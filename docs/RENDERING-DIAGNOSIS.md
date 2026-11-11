# Rendering diagnosis for 2.0.0-beta.2

## Exact input

`meteor-client-26.3-17.jar`
SHA-256: `9767e91e773ac5dfb27fdd29e5378e1ce41007dedd432db3a4223b3affb62e7f`

These facts were inspected from that supplied binary, not assumed from a different Meteor version:

- `meteordevelopment.meteorclient.renderer.MeteorRenderPipelines` static initialization calls `withCull(true)` for the pipeline assigned to `UI_COLORED` (bytecode offsets 523–533).
- `Renderer2D.quad` submits its four filled vertices in top-left, bottom-left, bottom-right, top-right order.
- `MeshBuilder.quad` emits indices `(1,2,3)` and `(3,4,1)`. `MeshBuilder.triangle` retains the caller's order.
- `Renderer2D.triangle` does not silently reverse the provided triangle.

For 2D screen coordinates, the filled native quad's first triangle has negative signed area:

```text
area = (x2-x1)*(y3-y1) - (y2-y1)*(x3-x1)
```

Beta.1's quarter-circle fan and star fan use the opposite order. Their positive-area triangles disagree with the filled rectangles. This is a concrete rendering defect consistent with the missing/oddly cut corner and switch shapes in the user screenshot. A native GPU capture was not available to independently observe each discarded primitive.

The replacement swaps vertices 2 and 3 when area is positive, retains the native order otherwise, and skips degenerate triangles. This local fix does not disable culling globally or change Meteor's renderer. The same helper serves all addon triangles, including glyph strokes and logos.

## Why earlier test renders were misleading

The AWT software adapter filled triangles in either orientation. It could therefore show intact corners even when those same triangles were back-facing under the native pipeline. It now rejects a positive signed area. Both the rejected JAR and the replacement are exercised by the same `ReskinVisualBaseline` probe; see `test-results/results.txt`.

The native OpenGL/render-pipeline execution itself is still not run by these tests.

## Caption clipping

The rejected binary rendered `G...` when `GUI` was given only a 0.01-pixel width deficit. The replacement uses matching measurement paths, upward-rounded caption allocations and a 0.75-pixel fit tolerance instead of triggering ellipsis for subpixel discrepancies. Genuine long strings still truncate and hover-scroll; text is not shrunk to fit.
