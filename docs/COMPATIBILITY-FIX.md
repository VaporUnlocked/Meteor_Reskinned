# Beta.3 table/view contract correction

The exact Meteor artifact's `WTable.getRow(int)` returns null for indices outside its allocated rows. `rowI()` is an insertion cursor. A call to `row()` after adding the last row increases that cursor without allocating another row. An empty or cleared table may have no allocated rows at all.

Beta.2 called `row.isEmpty()` without checking null. Its broad default `ReskinView.verticalOnly=true` also applied Reskin's constraint reflow to native and third-party tables.

Beta.3 handles null rows, and changes the generic view default to native-layout mode. `ReskinModulesScreen` explicitly opts its owned inspector into vertical-only reflow. The non-reflow positioning path calls each child's native `calculateWidgetPositions()` after alignment; custom table semantics therefore remain owned by the custom widget. This does not swallow arbitrary exceptions or replace failures with empty UI.

Tests deliberately use the supplied WTable implementation, including empty construction, a trailing row(), clear/repopulate cycles, removing every row, and preserving custom positioning. These tests do not launch the third-party proxy addon or make a network connection.
