// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.pressable.WMeteorCheckbox;
/** Native checked/action behavior with an accessible, stationary toggle hitbox. */
public final class ReskinCheckbox extends WMeteorCheckbox {
    public ReskinCheckbox(boolean checked){super(checked);}
    @Override protected void onCalculateSize(){width=theme.scale(46);height=theme.scale(30);}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        ReskinDraw.toggle(r,(ReskinTheme)theme,x+theme.scale(4),y+theme.scale(5),checked);
    }
}
