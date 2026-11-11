// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import java.util.*;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.input.MouseButtonEvent;

/** Registered tabs are never hardcoded. All layouts retain every third-party navigation entry. */
public final class ReskinTopBar extends WTopBar {
    private final List<NavButton> tabs=new ArrayList<>();
    private NavButton prev,next,appearance,pin,pinPrev,pinNext;
    private ReskinPinnedShelf shelf;
    private int page,pages=1,perPage=1;
    private double by,bh,bx,bw,brandWidth;
    private boolean side;
    @Override public void init(){
        for(Tab tab:Tabs.get())tabs.add(add(new NavButton(tab)).top().widget());
        prev=add(new NavButton("<",()->{page--;invalidate();})).top().widget();
        next=add(new NavButton(">",()->{page++;invalidate();})).top().widget();
        appearance=add(new NavButton("Appearance",()->ReskinAccess.later(()->((ReskinTheme)theme).appearanceTab().openScreen(theme)))).top().widget();
        pin=add(new NavButton("Panels",()->{var t=(ReskinTheme)theme;t.shelfEnabled.set(!t.shelfEnabled.get());invalidate();})).top().widget();
        pinPrev=add(new NavButton("<",()->shelf.step(-1))).top().widget();
        pinNext=add(new NavButton(">",()->shelf.step(1))).top().widget();
        shelf=add(new ReskinPinnedShelf()).top().widget();
        if(parent instanceof WContainer root){var hit=root.add(new OverlayHitTest(this)).top();root.cells.remove(hit);root.cells.addFirst(hit);}
    }
    @Override protected void onCalculateSize(){
        var t=(ReskinTheme)theme;width=Utils.getWindowWidth();height=t.navigationHeight();
        side=t.sideInset()>0;bh=t.navigationHeight();by=t.style()==ReskinTheme.StylePreset.NEON_DOCK?Utils.getWindowHeight()-bh-t.scale(8):0;
        bx=t.scale(8);bw=width-bx*2;brandWidth=Math.min(t.scale(240),bw*.29);
        if(side){
            bx=t.scale(8);by=t.scale(8);bw=t.sideInset()-t.scale(12);bh=Utils.getWindowHeight()-t.scale(16);
            perPage=Math.max(1,(int)((bh-t.scale(206))/t.scale(36)));
        }else{
            double available=bw-brandWidth-t.scale(224);
            perPage=Math.max(1,(int)Math.floor(available/t.scale(88)));
        }
        pages=Math.max(1,(tabs.size()+perPage-1)/perPage);page=Math.max(0,Math.min(page,pages-1));
        shelf.visible=t.shelfEnabled.get();shelf.calculateSize();
    }
    @Override protected void onCalculateWidgetPositions(){
        var t=(ReskinTheme)theme;
        double itemW=side?bw-t.scale(14):Math.max(t.scale(46),(bw-brandWidth-t.scale(224))/Math.max(1,perPage));
        for(int i=0;i<tabs.size();i++){
            NavButton b=tabs.get(i);b.visible=i>=page*perPage&&i<(page+1)*perPage;
            if(b.visible)place(b,side?bx+t.scale(7):bx+brandWidth+(i-page*perPage)*itemW,side?by+t.scale(92)+(i-page*perPage)*t.scale(36):by+t.scale(12),itemW,side?t.scale(32):t.scale(38));
        }
        prev.visible=page>0;next.visible=page<pages-1;
        if(side){
            place(prev,bx+t.scale(8),by+bh-t.scale(105),t.scale(32),t.scale(30));place(next,bx+bw-t.scale(40),by+bh-t.scale(105),t.scale(32),t.scale(30));
            place(appearance,bx+t.scale(8),by+bh-t.scale(70),bw-t.scale(16),t.scale(30));
            place(pin,bx+t.scale(8),by+bh-t.scale(34),bw-t.scale(16),t.scale(28));
        }else{
            double right=bx+bw-t.scale(8);
            place(pin,right-t.scale(64),by+t.scale(12),t.scale(64),t.scale(38));right-=t.scale(68);
            place(appearance,right-t.scale(98),by+t.scale(12),t.scale(98),t.scale(38));right-=t.scale(102);
            place(next,right-t.scale(24),by+t.scale(12),t.scale(24),t.scale(38));right-=t.scale(26);
            place(prev,right-t.scale(24),by+t.scale(12),t.scale(24),t.scale(38));
        }
        pinPrev.visible=pinNext.visible=t.shelfEnabled.get();
        place(pinPrev,width-t.scale(90),t.topInset(),t.scale(30),t.scale(30));place(pinNext,width-t.scale(54),t.topInset(),t.scale(30),t.scale(30));
        shelf.visible=t.shelfEnabled.get();shelf.calculateWidgetPositions();
    }
    private void place(WWidget w,double x,double y,double width,double height){
        w.x=x;w.y=y;w.width=Math.max(1,width);w.height=Math.max(1,height);
        for(Cell<?> c:cells)if(c.widget()==w){c.x=x;c.y=y;c.width=width;c.height=height;break;}
    }
    @Override public boolean render(GuiRenderer r,double mx,double my,double dt){
        if(!visible)return true;onRender(r,mx,my,dt);
        for(NavButton b:tabs)if(b.visible)b.render(r,mx,my,dt);
        for(NavButton b:List.of(prev,next,appearance,pin))if(b.visible)b.render(r,mx,my,dt);
        if(shelf.visible)r.absolutePost(()->{
            r.scissorStart(0,0,Utils.getWindowWidth(),Utils.getWindowHeight());
            shelf.render(r,mx,my,dt);pinPrev.render(r,mx,my,dt);pinNext.render(r,mx,my,dt);
            r.scissorEnd();
        });
        return false;
    }
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        var t=(ReskinTheme)theme;ReskinDraw.panel(r,t,bx,by,bw,bh,t.style()==ReskinTheme.StylePreset.NEON_DOCK);
        double x=bx+t.scale(14),y=by+t.scale(12),space=side?bw-t.scale(28):brandWidth-t.scale(24);
        ReskinDraw.text(r,t,"METEOR RESKIN",x,y,space,t.titleTextColor.get(),false);
        ReskinDraw.text(r,t,"BY Cloudy_City",x,y+t.scale(22),space,t.accentColor.get(),false);
        if(side){
            ReskinDraw.text(r,t,"SLATE SIDEBAR",x,y+t.scale(49),space,t.textSecondaryColor.get(),false);
            ReskinDraw.text(r,t,"Meteor Reskin / "+currentName(),t.sideInset()+t.scale(18),t.scale(24),Utils.getWindowWidth()-t.sideInset()-t.scale(40),t.textSecondaryColor.get(),false);
        }
    }
    private String currentName(){return ReskinAccess.screen() instanceof TabScreen ts?ts.tab.name:"Menu";}
    @Override protected boolean propagateEvents(WWidget w){return w.visible;}
    @Override protected Color getButtonColor(boolean p,boolean h){return ((ReskinTheme)theme).backgroundColor.get(p,h);}
    @Override protected Color getNameColor(){return ((ReskinTheme)theme).textColor.get();}
    boolean overControls(double x,double y){
        return ReskinDraw.hit(x,y,bx,by,bw,bh) || shelf.visible&&(shelf.overShelf(x,y)||pinPrev.isOver(x,y)||pinNext.isOver(x,y));
    }
    private static final class NavButton extends WPressable {
        final Tab tab;final String title;final Runnable click;final ReskinText text=new ReskinText();
        NavButton(Tab tab){this.tab=tab;title=tab.name;click=null;tooltip=tab.name;}
        NavButton(String title,Runnable click){this.tab=null;this.title=title;this.click=click;tooltip=title;}
        @Override protected void onCalculateSize(){width=theme.scale(88);height=theme.scale(38);}
        @Override protected void onPressed(int button){
            if(button!=1)return;
            if(click!=null)click.run();else ReskinAccess.later(()->tab.openScreen(theme));
        }
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
            var t=(ReskinTheme)theme;
            boolean active=tab!=null&&ReskinAccess.screen() instanceof TabScreen ts&&ts.tab==tab;
            if(title.equals("Appearance")&&ReskinAccess.screen() instanceof ReskinAppearanceScreen)active=true;
            if(active||mouseOver)ReskinDraw.panel(r,t,x,y,width,height,active);
            text.draw(r,t,title,x+t.scale(8),y+(height-t.textHeight())/2,Math.max(1,width-t.scale(16)),t.textHeight(),false,active?t.accentColor.get():t.textColor.get(),mouseOver,dt);
        }
    }
    private static final class OverlayHitTest extends WWidget {
        final ReskinTopBar bar;boolean captured;double mx,my;
        OverlayHitTest(ReskinTopBar bar){this.bar=bar;}
        @Override protected void onCalculateSize(){width=Utils.getWindowWidth();height=Utils.getWindowHeight();}
        @Override public void onMouseMoved(double x,double y,double ox,double oy){mx=x;my=y;}
        @Override public boolean onMouseClicked(MouseButtonEvent e,boolean twice){
            if(!bar.overControls(e.x(),e.y()))return false;captured=true;bar.mouseClicked(e,twice);return true;
        }
        @Override public boolean onMouseReleased(MouseButtonEvent e){if(!captured)return false;captured=false;bar.mouseReleased(e);return true;}
        @Override public boolean onMouseScrolled(double amount){if(bar.shelf.visible&&bar.shelf.overShelf(mx,my)){bar.shelf.mouseScrolled(amount);return true;}return false;}
    }
}
