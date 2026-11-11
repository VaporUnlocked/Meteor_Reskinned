// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorLabel;
import meteordevelopment.meteorclient.utils.Utils;
public final class ReskinLabel extends WMeteorLabel {
    private final ReskinText scroller = new ReskinText();
    public ReskinLabel(String text, boolean title) { super(text, title); }
    @Override protected void onCalculateSize() {
        super.onCalculateSize();
        width = Math.min(width, Math.max(theme.scale(65), Utils.getWindowWidth() * 0.28));
    }
    @Override protected void onRender(GuiRenderer r, double mx, double my, double delta) {
        ReskinTheme t = (ReskinTheme) theme;
        scroller.draw(r, t, get(), x, y, width, height, title,
            color != null ? color : (title ? t.titleTextColor.get() : t.textColor.get()),
            mouseOver, delta);
    }
}
