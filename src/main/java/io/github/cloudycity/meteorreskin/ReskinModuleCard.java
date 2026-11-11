// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;

/** Real registered modules, with distinct accordion, table, list, and card presentations. */
final class ReskinModuleCard extends WPressable implements ReskinReflow.WidthAware {
    final Module module;private final Consumer<Module> select;private final Supplier<Module> selected;
    final boolean card,compact;private double mouseX,mouseY,bound=320;
    private double titleTop,categoryTop,descriptionTop,actionTop;
    private List<String> descriptionLines=List.of();
    private final ReskinText name=new ReskinText(),description=new ReskinText();
    ReskinModuleCard(Module m,boolean card,boolean compact,Consumer<Module> select,Supplier<Module> selected){module=m;this.card=card;this.compact=compact;this.select=select;this.selected=selected;tooltip=m.title+"\n"+m.description+"\nSelect name: settings  |  Switch: toggle  |  Star: favorite";}
    @Override public void availableWidth(double w){bound=Math.max(1,w);}
    @Override protected void onCalculateSize(){
        var t=(ReskinTheme)theme;width=bound;
        if(card){
            titleTop=t.scale(12);
            categoryTop=titleTop+t.textHeight(true)+t.scale(4);
            descriptionTop=Math.max(categoryTop+t.textHeight()+t.scale(12),titleTop+t.scale(29)+t.scale(12));
            descriptionLines=ReskinWrap.lines(theme,module.description,Math.max(1,width-t.scale(30)),false);
            int maxLines=t.style()==ReskinTheme.StylePreset.PRISM_STUDIO?3:2;
            if(descriptionLines.size()>maxLines)descriptionLines=descriptionLines.subList(0,maxLines);
            double descriptionBottom=descriptionTop+descriptionLines.size()*(t.textHeight()+t.scale(3));
            // Space is measured from the actual font, not a second independent scale.
            height=Math.max(t.composition(t.style()==ReskinTheme.StylePreset.APEX_WORKSPACE?112:126),
                descriptionBottom+t.scale(33));
            actionTop=height-t.scale(27);
        }else{
            height=compact?Math.max(t.composition(38),t.textHeight()+t.scale(18)):
                t.style()==ReskinTheme.StylePreset.OBSIDIAN_COMMAND&&width>theme.scale(620)?
                Math.max(t.composition(48),t.textHeight()+t.scale(22)):
                Math.max(t.composition(70),2*t.textHeight()+t.scale(30));
        }
    }
    @Override public void onMouseMoved(double mx,double my,double ox,double oy){super.onMouseMoved(mx,my,ox,oy);mouseX=mx;mouseY=my;}
    void changed(){for(var p=parent;p!=null;p=p.parent)if(p instanceof ReskinModulesScreen.Workspace w){w.dirty=true;w.invalidate();break;}}
    @Override protected void onPressed(int b){
        if(b==3){ReskinAccess.later(()->ReskinAccess.open(theme.moduleScreen(module)));return;}if(b!=1)return;
        var t=(ReskinTheme)theme;
        // Separate the actual switch/gear/star hitboxes from the rest of the row.
        // Clicking description text near the right edge must only select, not toggle.
        if(card&&ReskinDraw.hit(mouseX,mouseY,x+width-t.scale(36),y+actionTop-t.scale(3),t.scale(34),t.scale(29))){
            ReskinAccess.later(()->ReskinAccess.open(theme.moduleScreen(module)));return;
        }
        double sy=switchY(t);
        if(ReskinDraw.hit(mouseX,mouseY,x+width-t.scale(46),sy-t.scale(5),t.scale(46),t.scale(30))){
            module.toggle();changed();return;
        }
        if(ReskinDraw.hit(mouseX,mouseY,x,y+t.scale(compact?6:card?8:12),t.scale(24),t.scale(26))){
            module.favorite=!module.favorite;changed();return;
        }
        select.accept(module);
    }
    private double switchY(ReskinTheme t){return card?y+titleTop:y+(height-t.scale(20))/2;}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        var t=(ReskinTheme)theme;boolean active=module.isActive(),chosen=selected.get()==module;
        boolean table=t.style()==ReskinTheme.StylePreset.ATLAS_PANELS&&compact&&width>t.scale(300);
        boolean inline=t.style()==ReskinTheme.StylePreset.OBSIDIAN_COMMAND&&!card&&!compact&&width>t.scale(620);
        boolean plain=!card&&(t.style()==ReskinTheme.StylePreset.OBSIDIAN_COMMAND||t.style()==ReskinTheme.StylePreset.SLATE_SIDEBAR||table);
        if(!plain||chosen)ReskinDraw.panel(r,t,x,y,width,height,chosen||card&&active);
        if(mouseOver&&!chosen)r.quad(x+1,y+1,Math.max(0,width-2),Math.max(0,height-2),ReskinDraw.alpha(t.headerSurfaceColor.get(),135));
        if(plain&&!chosen)r.quad(x+t.scale(12),y+height-1,Math.max(1,width-t.scale(24)),1,ReskinDraw.alpha(t.border(),65));
        if(card&&t.style()==ReskinTheme.StylePreset.PRISM_STUDIO)ReskinDraw.ribbon(r,t,x+1,y+1,width-2,Math.max(t.scale(38),categoryTop+t.textHeight()+t.scale(7)));
        ReskinDraw.star(r,x+t.scale(7),y+t.scale(compact?13:card?16:18),t.scale(12),module.favorite?t.accentColor.get():ReskinDraw.alpha(t.textSecondaryColor.get(),85));
        double tw;
        if(card){
            ReskinIcons.draw(r,t,module.category,module,module.title,x+t.scale(28),y+titleTop,t.scale(29));
            tw=Math.max(1,width-t.scale(112));
            name.draw(r,t,module.title,x+t.scale(65),y+titleTop,tw,t.textHeight(true),true,t.titleTextColor.get(),mouseOver,dt);
            ReskinDraw.text(r,t,module.category==null?"Addon":module.category.name,x+t.scale(65),y+categoryTop,tw,t.textSecondaryColor.get(),false);
            for(int i=0;i<descriptionLines.size();i++)ReskinDraw.text(r,t,descriptionLines.get(i),x+t.scale(15),y+descriptionTop+i*(t.textHeight()+t.scale(3)),width-t.scale(30),t.textSecondaryColor.get(),false);
            ReskinGlyph.draw(r,"settings",x+width-t.scale(27),y+actionTop+t.scale(2),t.scale(16),t.textSecondaryColor.get());
        }else if(table){
            tw=width*.48-t.scale(32);
            name.draw(r,t,module.title,x+t.scale(27),y+(height-t.textHeight())/2,Math.max(1,tw),t.textHeight(),false,t.textColor.get(),mouseOver,dt);
            ReskinDraw.text(r,t,module.category==null?"Addon":module.category.name,x+width*.49,y+(height-t.textHeight())/2,Math.max(1,width*.28-t.scale(10)),t.textSecondaryColor.get(),false);
        }else{
            double tx=x+t.scale(compact||inline?28:60);
            if(!compact&&!inline)ReskinIcons.draw(r,t,module.category,module,module.title,x+t.scale(26),y+t.scale(15),t.scale(26));
            double titleY=inline?y+(height-t.textHeight())/2:y+t.scale(compact?12:13);
            tw=inline?width*.28:Math.max(1,x+width-t.scale(52)-tx);
            name.draw(r,t,module.title,tx,titleY,tw,t.textHeight(),false,t.titleTextColor.get(),mouseOver,dt);
            if(!compact){double dx=inline?x+width*.36:tx,dy=inline?titleY:y+t.scale(39),dw=inline?width*.64-t.scale(58):width-(tx-x)-t.scale(15);
                description.draw(r,t,module.description,dx,dy,Math.max(1,dw),t.textHeight(),false,t.textSecondaryColor.get(),mouseOver,dt);}
        }
        ReskinDraw.toggle(r,t,x+width-t.scale(42),switchY(t),active);
    }
}
