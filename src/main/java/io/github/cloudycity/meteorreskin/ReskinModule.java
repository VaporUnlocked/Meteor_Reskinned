// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorModule;
import meteordevelopment.meteorclient.systems.modules.Module;

/** Presentation of an existing Meteor module; inherited left/right clicks preserve Meteor behavior. */
public final class ReskinModule extends WMeteorModule {
    private final Module target;
    private final String title;
    private final ReskinText marquee = new ReskinText();
    public ReskinModule(Module target, String title) {
        super(target, title);
        this.target = target;
        this.title = title;
        this.tooltip = target.description;
    }
    @Override protected void onCalculateSize() {
        width = theme.scale(170);
        height = theme.scale(26);
    }
    @Override protected void onRender(GuiRenderer r, double mx, double my, double delta) {
        ReskinTheme t = (ReskinTheme) theme;
        boolean active = target.isActive();
        if (active) r.quad(x,y,width,height,t.moduleBackground.get());
        else if (mouseOver) r.quad(x,y,width,height,t.backgroundColor.get(false,true));
        if (active) r.quad(x,y,Math.max(2,t.scale(3)),height,t.accentColor.get());
        double left=t.scale(11);
        double right=t.scale(19);
        marquee.draw(r,t,title,x+left,y+(height-t.textHeight())/2,
            Math.max(8,width-left-right),height,false,t.textColor.get(),mouseOver,delta);
        if (active) {
            double s=t.scale(5);
            r.quad(x+width-t.scale(13),y+(height-s)/2,s,s,t.accentColor.get());
        }
        r.quad(x, y+height-Math.max(1,t.scale(.45)), width, Math.max(1,t.scale(.45)), t.outlineColor.get());
    }
}
