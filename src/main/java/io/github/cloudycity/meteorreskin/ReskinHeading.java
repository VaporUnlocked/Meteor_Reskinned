// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
final class ReskinHeading extends WWidget implements ReskinScrollPane.FullRow {
    private final String title;
    ReskinHeading(String title){this.title=title;}
    @Override protected void onCalculateSize(){width=theme.scale(150);height=theme.scale(35);}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        ReskinTheme t=(ReskinTheme)theme;ReskinDraw.text(r,t,title,x+t.scale(7),y+t.scale(12),width-t.scale(14),t.accentColor.get(),false);
    }
}
