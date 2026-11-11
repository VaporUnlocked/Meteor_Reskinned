// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;

import java.util.*;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.*;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.input.MouseButtonEvent;

/** Natural-width, icon-led navigation. Pages whole tabs, never shrinks short captions. */
public final class ReskinTopBar extends WTopBar {
    private final List<NavButton> tabs=new ArrayList<>();
    private NavButton prev,next,appearance;
    private ReskinScrollPane library;
    private double sideRowHeight;
    private boolean withLibrary;
    private int page,pages=1,first,last;
    private double by,bh,bx,bw,brandWidth,available,aw;
    private boolean side,twoRows;
    private final List<Integer> starts=new ArrayList<>();
    private double fontSignature;

    @Override public void init(){
        for(Tab tab:Tabs.get())tabs.add(add(new NavButton(tab)).top().widget());
        prev=add(new NavButton("‹",()->{page--;invalidate();})).top().widget();
        next=add(new NavButton("›",()->{page++;invalidate();})).top().widget();
        appearance=add(new NavButton("Appearance",()->ReskinAccess.later(()->((ReskinTheme)theme).appearanceTab().openScreen(theme)))).top().widget();
        library=add(new ReskinScrollPane()).top().widget();library.background=false;library.gap=2;
        library.add(new ReskinHeading("CATEGORIES"));
        library.add(new ReskinCategoryButton(null,"All modules",()->currentWorkspace()==null?0:currentWorkspace().all.size(),()->currentWorkspace()!=null&&currentWorkspace().category==null,()->selectCategory(null)));
        for(var category:meteordevelopment.meteorclient.systems.modules.Modules.loopCategories()) {
            library.add(new ReskinCategoryButton(category,category.name,()->currentWorkspace()==null?0:(int)currentWorkspace().all.stream().filter(m->m.category==category).count(),()->currentWorkspace()!=null&&currentWorkspace().category==category,()->selectCategory(category)));
        }
        if(parent instanceof WContainer root){var hit=root.add(new OverlayHitTest(this)).top();root.cells.remove(hit);root.cells.addFirst(hit);}
    }
    private ReskinModulesScreen.Workspace currentWorkspace(){
        return ReskinAccess.screen() instanceof ReskinModulesScreen s?s.currentWorkspace():null;
    }
    private void selectCategory(meteordevelopment.meteorclient.systems.modules.Category category){
        var w=currentWorkspace();if(w==null)return;
        w.category=category;w.filter="All";w.query="";w.search.set("");w.page=0;w.showCategories=false;
        if(category!=null)w.collapsed.remove(category);w.dirty=true;w.invalidate();w.focusSearch();
    }
    double natural(String text){return Math.ceil(theme.textWidth(text)+theme.scale(text.length()==1?22:50))+3;}
    @Override protected void onCalculateSize(){
        var t=(ReskinTheme)theme;width=Utils.getWindowWidth();height=t.navigationHeight();
        side=t.sideInset()>0;twoRows=!side&&(width<t.composition(1180)||t.style()==ReskinTheme.StylePreset.APEX_WORKSPACE);
        bh=t.navigationHeight();bx=t.composition(width<t.composition(850)?12:38);bw=Math.max(1,width-bx*2);
        by=t.style()==ReskinTheme.StylePreset.NEON_DOCK?Utils.getWindowHeight()-bh-t.composition(20):t.composition(20);
        brandWidth=Math.max(t.composition(228),Math.ceil(Math.max(theme.textWidth("METEOR RESKIN"),theme.textWidth("BY Cloudy_City")))+t.composition(82));
        aw=natural("Appearance");starts.clear();starts.add(0);
        if(side){
            bx=t.composition(16);by=t.composition(18);bw=t.sideInset()-t.composition(24);bh=Utils.getWindowHeight()-t.composition(36);
            withLibrary=t.style()==ReskinTheme.StylePreset.SLATE_SIDEBAR&&currentWorkspace()!=null;
            sideRowHeight=Math.max(t.textHeight()+t.composition(12),t.composition(34));
            int perPage=Math.max(1,(int)((bh-t.composition(220)-(withLibrary?t.composition(230):0))/sideRowHeight));
            for(int i=perPage;i<tabs.size();i+=perPage)starts.add(i);
        }else{
            withLibrary=false;
            available=Math.max(t.composition(75),bw-t.composition(78)-(twoRows?0:brandWidth+aw+t.composition(16)));
            double used=0;int start=0;
            for(int i=0;i<tabs.size();i++){
                double want=natural(tabs.get(i).title);
                if(i>start&&used+want>available){starts.add(i);start=i;used=0;}
                used+=want;
            }
        }
        pages=Math.max(1,starts.size());page=Math.max(0,Math.min(page,pages-1));first=starts.get(page);last=page+1<pages?starts.get(page+1):tabs.size();
        library.visible=withLibrary;
        if(withLibrary){
            double libraryY=by+t.composition(116)+(last-first)*sideRowHeight+t.composition(12);
            library.bounds(Math.max(1,bw-t.composition(16)),Math.max(1,by+bh-t.composition(96)-libraryY));library.calculateSize();
        }
        fontSignature=theme.textWidth("GUI HUD Config Appearance");
    }
    @Override protected void onCalculateWidgetPositions(){
        var t=(ReskinTheme)theme;
        double xx=bx+(twoRows?t.composition(39):brandWidth),yy=by+t.composition(twoRows?68:30);
        for(int i=0;i<tabs.size();i++){
            NavButton b=tabs.get(i);b.visible=i>=first&&i<last;
            if(b.visible){double ww=side?bw-t.composition(20):Math.min(natural(b.title),available);
                place(b,side?bx+t.composition(10):xx,side?by+t.composition(116)+(i-first)*sideRowHeight:yy,ww,side?sideRowHeight-t.composition(2):t.composition(38));xx+=ww;}
        }
        if(withLibrary){
            place(library,bx+t.composition(8),by+t.composition(116)+(last-first)*sideRowHeight+t.composition(12),library.viewportWidth,library.viewportHeight);
            library.calculateWidgetPositions();
        }
        prev.visible=page>0;next.visible=page<pages-1;
        if(side){
            place(prev,bx+t.composition(12),by+bh-t.composition(84),t.composition(32),t.composition(32));place(next,bx+bw-t.composition(44),by+bh-t.composition(84),t.composition(32),t.composition(32));
            place(appearance,bx+t.composition(10),by+bh-t.composition(42),bw-t.composition(20),t.composition(36));
        }else{
            double right=bx+bw-t.composition(12);
            place(appearance,right-aw,by+t.composition(twoRows?16:30),aw,t.composition(38));right-=aw+t.composition(4);
            if(twoRows){place(prev,bx+t.composition(5),yy,t.composition(30),t.composition(38));place(next,bx+bw-t.composition(36),yy,t.composition(30),t.composition(38));}
            else{place(next,right-t.composition(30),yy,t.composition(28),t.composition(38));place(prev,right-t.composition(62),yy,t.composition(28),t.composition(38));}
        }
    }
    private void place(WWidget w,double x,double y,double ww,double hh){
        w.x=Math.round(x);w.y=Math.round(y);w.width=Math.max(1,Math.ceil(ww));w.height=Math.max(1,Math.ceil(hh));
        for(Cell<?> c:cells)if(c.widget()==w){c.x=w.x;c.y=w.y;c.width=w.width;c.height=w.height;break;}
    }
    @Override public boolean render(GuiRenderer r,double mx,double my,double dt){
        if(!visible)return true;
        // Font changes must invalidate measurement rather than reuse narrower cached rectangles.
        if(Math.abs(fontSignature-theme.textWidth("GUI HUD Config Appearance"))>.75)invalidate();
        onRender(r,mx,my,dt);
        for(var b:tabs)if(b.visible)b.render(r,mx,my,dt);
        if(library.visible)library.render(r,mx,my,dt);
        for(var b:List.of(prev,next,appearance))if(b.visible)b.render(r,mx,my,dt);
        return false;
    }
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        var t=(ReskinTheme)theme;ReskinDraw.panel(r,t,bx,by,bw,bh,t.style()==ReskinTheme.StylePreset.NEON_DOCK);
        double yy=by+t.composition(twoRows?8:23),icon=t.composition(side?40:44),tx=bx+t.composition(18)+icon+t.composition(12);
        ReskinGlyph.draw(r,"logo",bx+t.composition(17),yy,icon,t.accentColor.get());
        double titleSpace=side?bw-t.composition(36):brandWidth-t.composition(78);
        if(side){tx=bx+t.composition(16);yy=by+t.composition(64);titleSpace=bw-t.composition(32);}
        ReskinDraw.text(r,t,"METEOR RESKIN",tx,yy+t.composition(2),titleSpace,t.titleTextColor.get(),false);
        ReskinDraw.text(r,t,"BY Cloudy_City",tx,yy+t.composition(24),titleSpace,t.accentColor.get(),false);
        if(twoRows&&!side)r.quad(bx+t.composition(16),by+t.composition(60),bw-t.composition(32),1,ReskinDraw.alpha(t.border(),100));
        if(side)r.quad(bx+t.composition(14),by+t.composition(105),bw-t.composition(28),1,ReskinDraw.alpha(t.border(),110));
    }
    @Override protected boolean propagateEvents(WWidget w){return w.visible;}
    @Override protected Color getButtonColor(boolean p,boolean h){return ((ReskinTheme)theme).backgroundColor.get(p,h);}
    @Override protected Color getNameColor(){return ((ReskinTheme)theme).textColor.get();}
    boolean overControls(double x,double y){return ReskinDraw.hit(x,y,bx,by,bw,bh);}
    private static final class NavButton extends WPressable {
        final Tab tab;final String title;final Runnable click;final ReskinText text=new ReskinText();
        NavButton(Tab tab){this.tab=tab;title=tab.name;click=null;tooltip=title;}
        NavButton(String title,Runnable click){tab=null;this.title=title;this.click=click;tooltip=title;}
        @Override protected void onCalculateSize(){width=Math.ceil(theme.textWidth(title)+theme.scale(title.length()==1?22:50))+3;height=theme.scale(38);}
        @Override protected void onPressed(int b){if(b!=1)return;if(click!=null)click.run();else ReskinAccess.later(()->tab.openScreen(theme));}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
            var t=(ReskinTheme)theme;boolean active=tab!=null&&ReskinAccess.screen() instanceof TabScreen ts&&ts.tab==tab;
            if(title.equals("Appearance")&&ReskinAccess.screen() instanceof ReskinAppearanceScreen)active=true;
            if(active||mouseOver){ReskinDraw.panel(r,t,x,y,width,height,active);if(active)r.quad(x+t.scale(11),y+height-2,Math.max(1,width-t.scale(22)),2,t.accentColor.get());}
            boolean icon=title.length()>1;double pad=t.scale(icon?36:7);
            if(icon)ReskinGlyph.draw(r,title,x+t.scale(10),y+(height-t.scale(19))/2,t.scale(19),active?t.accentColor.get():t.textColor.get());
            double room=Math.max(1,width-pad-t.scale(11));
            text.draw(r,t,title,x+pad,y+(height-t.textHeight())/2,room,t.textHeight(),false,active?t.titleTextColor.get():t.textColor.get(),mouseOver,dt);
        }
    }
    private static final class OverlayHitTest extends WWidget {
        final ReskinTopBar bar;boolean captured;double mx,my;OverlayHitTest(ReskinTopBar b){bar=b;}
        @Override protected void onCalculateSize(){width=Utils.getWindowWidth();height=Utils.getWindowHeight();}
        @Override public void onMouseMoved(double x,double y,double ox,double oy){mx=x;my=y;}
        @Override public boolean onMouseClicked(MouseButtonEvent e,boolean twice){if(!bar.overControls(e.x(),e.y()))return false;captured=true;bar.mouseClicked(e,twice);return true;}
        @Override public boolean onMouseReleased(MouseButtonEvent e){if(!captured)return false;captured=false;bar.mouseReleased(e);return true;}
        @Override public boolean onMouseScrolled(double a){if(bar.library.visible&&bar.library.isOver(mx,my))return bar.library.mouseScrolled(a);return false;}
    }
}
