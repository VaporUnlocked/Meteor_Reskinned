// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
final class ReskinTableHeader extends WWidget implements ReskinScrollPane.FullRow {
    @Override protected void onCalculateSize(){width=theme.scale(300);height=theme.scale(39);}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        var t=(ReskinTheme)theme;r.quad(x,y,width,height,t.headerSurfaceColor.get());
        r.text("Name",x+t.scale(27),y+t.scale(12),t.titleTextColor.get(),false);
        if(width>t.scale(300))r.text("Category",x+width*.49,y+t.scale(12),t.textSecondaryColor.get(),false);
        ReskinDraw.text(r,t,"State",x+width-t.scale(46),y+t.scale(12),t.scale(44),t.textSecondaryColor.get(),false);
    }
}
