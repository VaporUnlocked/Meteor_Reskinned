// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorWindow;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

/** Reskin window chrome. Uses Meteor's native view and actions, replacing only its appearance. */
public final class ReskinWindow extends WMeteorWindow {
    /** Set for auto-layout columns. Their headers may collapse but cannot be dragged out of the grid. */
    public boolean docked;
    /** Total panel height available after its parent reserves navigation and margins. */
    public double maximumHeight = Double.POSITIVE_INFINITY;

    public ReskinWindow(WWidget icon, String title) { super(icon, title); }

    @Override protected WHeader header(WWidget icon) { return new ReskinHeader(icon); }

    private final class ReskinHeader extends WHeader {
        private ReskinHeader(WWidget icon) { super(icon); }

        @Override protected void onCalculateSize() {
            super.onCalculateSize();
            height = Math.max(height, theme.scale(29));
        }

        @Override public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (!docked) super.onMouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
        }

        @Override protected void onRender(GuiRenderer r, double mx, double my, double delta) {
            ReskinTheme t = (ReskinTheme) theme;
            r.quad(x, y, width, height, t.headerSurfaceColor.get());
            r.quad(x, y, Math.max(2, t.scale(2)), height, t.accentColor.get());
            r.quad(x, y + height - Math.max(1,t.scale(.65)), width, Math.max(1,t.scale(.65)), t.outlineColor.get());
        }
    }

    @Override protected void onCalculateSize() {
        super.onCalculateSize();
        ReskinTheme t = (ReskinTheme) theme;
        double screenW = t.logicalWidth();
        double screenH = t.logicalHeight();
        double allowed = Math.max(1, Math.min(maximumHeight, screenH - t.topInset() - t.bottomInset() - t.scale(12)));
        // Measure the real header, spacing and padding. Capping only the outer
        // height leaves the inner scrolling view hanging below its panel.
        double chrome = height - view.height;
        double viewCap = Math.max(1, allowed - chrome);
        if (view instanceof ReskinView reskinView) reskinView.externallyConstrainedMaxHeight = viewCap;
        view.maxHeight = viewCap;
        view.calculateSize();
        super.onCalculateSize();
        width = Math.min(width, Math.max(1, screenW - t.sideInset() - t.scale(28)));
        height = Math.min(height, allowed);
    }

    @Override protected void onCalculateWidgetPositions() {
        super.onCalculateWidgetPositions();
        ReskinTheme t=(ReskinTheme)theme;
        // Keep even saved/dragged windows within every edge after resolution changes.
        if (!docked) {
            double xx=Math.max(t.sideInset()+t.scale(10), Math.min(x, Math.max(t.sideInset()+t.scale(10), t.logicalWidth()-width-t.scale(10))));
            double yy=Math.max(t.topInset(), Math.min(y, Math.max(t.topInset(), t.logicalHeight()-t.bottomInset()-height)));
            if (xx!=x || yy!=y) move(xx-x, yy-y);
        }
    }

    @Override protected void onRender(GuiRenderer r, double mx, double my, double delta) {
        ReskinTheme t = (ReskinTheme) theme;
        Color surface = t.backgroundColor.get();
        Color border = t.outlineColor.get();
        if (width <= 1 || height <= 1) return;
        double drawnHeight = Math.max(0, Math.min(height, header.height + (height-header.height)*animProgress));
        ReskinDraw.panel(r,t,x,y,width,drawnHeight,false);
    }
}
