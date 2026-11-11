// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.pressable.WMeteorButton;
public final class ReskinButton extends WMeteorButton {
    private final ReskinText marquee = new ReskinText();
    public ReskinButton(String text, GuiTexture icon) { super(text,icon); }
    @Override protected void onCalculateSize() {
        super.onCalculateSize();
        width = Math.min(width, Math.max(theme.scale(80), ((ReskinTheme)theme).logicalWidth()*0.45));
        height = Math.max(height, theme.scale(28));
    }
    @Override protected void onRender(GuiRenderer r, double mx, double my, double delta) {
        ReskinTheme t=(ReskinTheme)theme;
        ReskinDraw.panel(r,t,x,y,width,height,mouseOver||pressed);
        if(pressed)ReskinDraw.round(r,x+1,y+1,width-2,height-2,t.scale(6),t.headerSurfaceColor.get());
        double pad=t.scale(6);
        if (texture != null) {
            double s=Math.max(3,height-pad*2);
            r.quad(x+(width-s)/2,y+(height-s)/2,s,s,texture,t.textColor.get());
        } else if (text != null) {
            double w=Math.max(1,width-pad*2);
            marquee.draw(r,t,text,x+pad,y+(height-t.textHeight())/2,w,height,false,t.textColor.get(),mouseOver,delta);
        }
    }
}
