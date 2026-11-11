// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
/** One implementation of clipped hover marquee, reused by rows, labels and buttons. */
final class ReskinText {
    private double distance = 0;
    private double hoverTime = 0;
    private String lastText="";
    private double lastWidth=-1,lastScale=-1,cachedContentWidth;
    void draw(GuiRenderer r, ReskinTheme t, String text, double x, double y, double maxW,
              double height, boolean title, Color c, boolean hover, double delta) {
        if(!java.util.Objects.equals(lastText,text)||Math.abs(lastWidth-maxW)>.1||lastScale!=t.scale(1)){
            lastText=text;lastWidth=maxW;lastScale=t.scale(1);hoverTime=0;distance=0;
            cachedContentWidth=text==null?0:t.textWidth(text,text.length(),title);
        }
        if (maxW <= 1 || text == null || text.isEmpty()) return;
        double contentWidth = cachedContentWidth;
        if (contentWidth <= maxW) {
            distance = 0; hoverTime = 0;
            r.text(text, x, y, c, title);
            return;
        }
        if (!t.marquee.get() || t.reducedMotion.get()) {
            distance=0; hoverTime=0;
            if(hover)r.tooltip(text);
            r.scissorStart(x,y,maxW,Math.max(1,height));
            r.text(text,x,y,c,title);
            r.scissorEnd();
            return;
        }
        if (hover) hoverTime += delta;
        else { hoverTime = 0; distance = 0; }
        if (hoverTime > 0.40) {
            double max = Math.max(0, contentWidth - maxW + t.scale(5));
            double span = max / Math.max(1, t.scale(36));
            double period = 0.8 + span + 1.0 + span;
            double phase = (hoverTime - 0.40) % Math.max(1, period);
            if (phase < 0.4) distance = 0;
            else if (phase < 0.4 + span) distance = Math.min(max, (phase - 0.4) * t.scale(36));
            else if (phase < 1.2 + span) distance = max;
            else distance = Math.max(0, max - (phase - 1.2 - span) * t.scale(36));
        }
        r.scissorStart(x, y, maxW, Math.max(1,height));
        r.text(text, x - distance, y, c, title);
        r.scissorEnd();
    }
}
