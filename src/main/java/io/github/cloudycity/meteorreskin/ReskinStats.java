// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import java.util.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
/** Only observed registry state; no invented FPS, latency, hardware numbers or graphs. */
final class ReskinStats extends WWidget {
    final List<Module> modules;ReskinStats(List<Module> list){modules=list;}
    @Override protected void onCalculateSize(){width=theme.scale(260);height=theme.scale(194);}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        var t=(ReskinTheme)theme;long[] v={modules.size(),modules.stream().filter(Module::isActive).count(),modules.stream().filter(m->m.favorite).count(),modules.stream().map(m->m.category).distinct().count()};
        String[] labels={"Modules","Enabled","Favorites","Categories"},icons={"grid","power","star","cube"};
        double gap=t.scale(8),cw=(width-gap)/2,ch=t.scale(88);
        for(int i=0;i<4;i++){
            double xx=x+(i%2)*(cw+gap),yy=y+(i/2)*(ch+gap);ReskinDraw.panel(r,t,xx,yy,cw,ch,false);
            ReskinGlyph.draw(r,icons[i],xx+t.scale(11),yy+t.scale(12),t.scale(20),t.accentColor.get());
            ReskinDraw.text(r,t,""+v[i],xx+t.scale(39),yy+t.scale(12),cw-t.scale(48),t.titleTextColor.get(),true);
            ReskinDraw.text(r,t,labels[i],xx+t.scale(12),yy+t.scale(53),cw-t.scale(24),t.textSecondaryColor.get(),false);
        }
    }
}
