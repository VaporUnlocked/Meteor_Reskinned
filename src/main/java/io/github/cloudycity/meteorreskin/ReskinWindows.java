// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

/** Shared safe construction order for docked reskin windows. */
final class ReskinWindows {
    private ReskinWindows() {}

    static Cell<WWindow> addDocked(WContainer parent, String title, double padding, double spacing) {
        WWindow window = parent.theme.window(title);
        window.id = null;
        window.padding = padding;
        window.spacing = spacing;
        if (window instanceof ReskinWindow reskin) reskin.docked = true;

        // GuiTheme.window() only constructs the widget. WContainer.add() assigns its
        // parent/theme and calls init(), which creates the header and scroll view.
        // Never call init() manually: attaching it would initialize it a second time.
        Cell<WWindow> cell = parent.add(window).top().expandWidgetX();
        if (window.view == null) {
            throw new IllegalStateException("Reskin window has no view after attachment: " + title);
        }
        window.view.hasScrollBar = true;
        window.view.scrollOnlyWhenMouseOver = true;
        if (spacing == 0) window.view.spacing = 0;
        return cell;
    }
    static void measure(WWindow window, double width, double height) {
        if (window instanceof ReskinWindow reskin) reskin.maximumHeight = Math.max(1, height);
        window.calculateSize();
        window.width = Math.max(1, width);
        window.height = Math.min(window.height, Math.max(1, height));
    }

}
