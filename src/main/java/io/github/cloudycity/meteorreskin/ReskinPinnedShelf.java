// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;

/** On-demand quick-access category columns carried between every Meteor tab. */
final class ReskinPinnedShelf extends WContainer {
    private final List<Category> categories=new ArrayList<>();
    private final List<Cell<WWindow>> windows=new ArrayList<>();
    private ReskinTheme t;
    private double panelW,gap,margin,top;
    private int count;
    private boolean rebuildPending=true;

    @Override public void init() {
        t=(ReskinTheme)theme;
        for(Category c:Modules.loopCategories())if(!Modules.get().getGroup(c).isEmpty())categories.add(c);
        visible=t.shelfEnabled.get();
    }
    void step(int delta) {
        if(categories.isEmpty())return;
        t.quickColumnIndex=Math.floorMod(t.quickColumnIndex+delta,categories.size());
        rebuildPending=true;
        invalidate();
    }
    private void rebuild() {
        clear();windows.clear();
        rebuildPending=false;
        if(categories.isEmpty())return;
        t.quickColumnIndex=Math.floorMod(t.quickColumnIndex,categories.size());
        // On large screens two category columns are available on every non-module tab.
        count=Utils.getWindowWidth()>theme.scale(1120) && categories.size()>1?2:1;
        for(int i=0;i<count;i++){
            Category category=categories.get((t.quickColumnIndex+i)%categories.size());
            Cell<WWindow> cell=ReskinWindows.addDocked(this,category.name,6,0);
            WWindow p=cell.widget();
            List<Module> modules=new ArrayList<>();
            for(Module m:Modules.get().getGroup(category))if(!Config.get().hiddenModules.get().contains(m))modules.add(m);
            modules.sort(Comparator.comparing(m->m.title,String.CASE_INSENSITIVE_ORDER));
            for(Module m:modules)p.add(theme.module(m,m.title)).expandX();
            windows.add(cell);
        }
    }
    boolean overShelf(double mx,double my) {
        if(!t.shelfEnabled.get() || windows.isEmpty())return false;
        return mx>=x && mx<=x+width && my>=y && my<=y+height;
    }
    @Override protected void onCalculateSize() {
        width=0;height=0;
        if(!t.shelfEnabled.get())return;
        int wanted=Utils.getWindowWidth()>theme.scale(1120) && categories.size()>1?2:1;
        if(rebuildPending || count!=wanted)rebuild();
        if(windows.isEmpty())return;
        double screenW=Utils.getWindowWidth(),screenH=Utils.getWindowHeight();
        margin=Math.max(6,theme.scale(12));gap=Math.max(7,theme.scale(10));
        top=t.topInset()+theme.scale(36);
        count=windows.size();
        panelW=Math.min(theme.scale(285),Math.max(theme.scale(175),(screenW-margin*2-gap*(count-1))/count));
        width=panelW*count+gap*(count-1);
        height=Math.max(theme.scale(80),screenH-top-t.bottomInset());
        for(Cell<WWindow> c:windows){
            WWindow w=c.widget();
            ReskinWindows.measure(w,panelW,height);
        }
    }
    @Override protected void onCalculateWidgetPositions(){
        if(!t.shelfEnabled.get())return;
        x=Math.max(0,Utils.getWindowWidth()-width-margin);
        y=top;
        for(int i=0;i<windows.size();i++){
            Cell<WWindow> c=windows.get(i);
            c.x=x+i*(panelW+gap);c.y=y;c.width=panelW;c.height=c.widget().height;c.alignWidget();
        }
    }
    @Override protected void onRender(GuiRenderer r,double mx,double my,double delta){
        if(!t.shelfEnabled.get())return;
        r.quad(x-theme.scale(3),y-theme.scale(3),width+theme.scale(6),height+theme.scale(6),t.backgroundColor.get(false,false));
    }
    @Override protected boolean propagateEvents(WWidget w){return t.shelfEnabled.get()&&w.visible;}
}
