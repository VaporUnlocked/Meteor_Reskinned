// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import java.util.function.Consumer;
import java.util.function.Supplier;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;

/** Renders an existing module; no behavior beyond its native toggle, favorite flag, and settings screen. */
final class ReskinModuleCard extends WPressable {
    final Module module;
    private final Consumer<Module> select;
    private final Supplier<Module> selected;
    final boolean card,compact;
    private double mouseX;
    private final ReskinText name=new ReskinText(),description=new ReskinText();
    ReskinModuleCard(Module module,boolean card,boolean compact,Consumer<Module> select,Supplier<Module> selected){
        this.module=module;this.card=card;this.compact=compact;this.select=select;this.selected=selected;
        tooltip=module.title+"\n"+module.description+"\nClick name: inspect   |   Right click: full settings";
    }
    @Override protected void onCalculateSize(){width=theme.scale(card?240:320);height=theme.scale(card?102:compact?34:60);}
    @Override public void onMouseMoved(double mx,double my,double ox,double oy){super.onMouseMoved(mx,my,ox,oy);mouseX=mx;}
    @Override protected void onPressed(int button){
        if(button==3){ReskinAccess.later(()->ReskinAccess.open(theme.moduleScreen(module)));return;}
        if(button!=1)return;
        double tx=x+width-theme.scale(46);
        if(mouseX>=tx){module.toggle();return;}
        if(mouseX<x+theme.scale(23)){module.favorite=!module.favorite;return;}
        select.accept(module);
    }
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        ReskinTheme t=(ReskinTheme)theme;boolean active=module.isActive(),chosen=selected.get()==module;
        ReskinDraw.panel(r,t,x,y,width,height,chosen);
        if(mouseOver&&!chosen)ReskinDraw.round(r,x+1,y+1,width-2,height-2,t.scale(7),t.headerSurfaceColor.get());
        double star=t.scale(13),sy=y+t.scale(card?13:compact?10:14);
        ReskinDraw.star(r,x+t.scale(7),sy,star,module.favorite?t.accentColor.get():t.textSecondaryColor.get());
        double left=x+t.scale(compact?28:64);
        if(!compact)ReskinIcons.draw(r,t,module.category,module,module.title,x+t.scale(27),y+t.scale(10),t.scale(28));
        double titleY=y+t.scale(compact?8:10);
        name.draw(r,t,module.title,left,titleY,Math.max(1,x+width-t.scale(51)-left),t.textHeight(),false,t.textColor.get(),mouseOver,dt);
        if(card){
            ReskinDraw.text(r,t,module.category==null?"Addon":module.category.name,x+t.scale(14),y+t.scale(45),width-t.scale(28),t.accentColor.get(),false);
            description.draw(r,t,module.description,x+t.scale(14),y+t.scale(69),Math.max(1,width-t.scale(28)),t.textHeight(),false,t.textSecondaryColor.get(),mouseOver,dt);
        }else if(!compact){
            description.draw(r,t,(module.category==null?"Addon":module.category.name)+"  /  "+module.description,left,y+t.scale(34),Math.max(1,width-(left-x)-t.scale(14)),t.textHeight(),false,t.textSecondaryColor.get(),mouseOver,dt);
        }
        ReskinDraw.toggle(r,t,x+width-t.scale(42),y+t.scale(compact?9:14),active);
    }
}
