// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import java.util.*;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;

/** Four real layouts over the same existing Meteor module and setting instances. */
public final class ReskinModulesScreen extends TabScreen {
    private Workspace workspace;
    public ReskinModulesScreen(GuiTheme theme){super(theme,Tabs.get().getFirst());}
    @Override public void initWidgets(){workspace=add(new Workspace()).top().expandX().widget();}
    @Override public void tick(){super.tick();if(workspace!=null)workspace.tickWorkspace();}
    @Override public void reload(){if(workspace!=null){workspace.dirty=true;invalidate();}}
    @Override public boolean toClipboard(){return NbtUtils.toClipboard(Modules.get());}
    @Override public boolean fromClipboard(){boolean changed=NbtUtils.fromClipboard(Modules.get());if(changed)reload();return changed;}
    @Override protected void onClosed(){super.onClosed();((ReskinTheme)theme).saveAppearance();}

    final class Workspace extends WContainer {
        final List<Category> categories=new ArrayList<>();
        final List<Module> all=new ArrayList<>();
        final List<DockColumn> docks=new ArrayList<>();
        ReskinScrollPane rail,content;
        ReskinView inspector;
        WContainer settingsContainer;
        Module selected;
        Category category;
        WTextBox search;
        WButton allButton,favButton,enabledButton,categoriesButton,prev,next,appearance,edit,pin;
        String query="",filter="All";
        boolean dirty=true,detailDirty=true,showCategories,hasDetail,hasRail,hasSummary;
        int page,pages=1,columns=1,lastModuleCount=-1;
        double margin,gap,top,bodyY,bodyH,mainX,mainW,railW,contentX,contentW,detailX,detailW,toolbarHeight;
        ReskinTheme.StylePreset lastStyle;
        private final ReskinText inspectorTitle=new ReskinText();
        private ReskinTheme t(){return (ReskinTheme)theme;}
        @Override public void init(){
            search=add(theme.textBox("","Search names, descriptions, settings...")).top().expandWidgetX().widget();
            search.action=()->{query=search.get().strip().toLowerCase(Locale.ROOT);page=0;dirty=true;invalidate();};
            allButton=button("All",()->setFilter("All"));
            favButton=button("Favorites",()->setFilter("Favorites"));
            enabledButton=button("Enabled",()->setFilter("Enabled"));
            categoriesButton=button("Categories",()->{showCategories=!showCategories;invalidate();});
            prev=button("<",()->{page=Math.max(0,page-1);invalidate();});
            next=button(">",()->{page=Math.min(pages-1,page+1);invalidate();});
            appearance=button("Appearance",()->ReskinAccess.later(()->t().appearanceTab().openScreen(theme)));
            edit=button("Full settings",()->{if(selected!=null)ReskinAccess.later(()->ReskinAccess.open(theme.moduleScreen(selected)));});
            pin=button("Pin category",()->{
                if(selected!=null&&selected.category!=null)t().quickColumnIndex=Math.max(0,categories.indexOf(selected.category));
                t().shelfEnabled.set(true);invalidate();
            });
            rail=add(new ReskinScrollPane()).top().widget();
            content=add(new ReskinScrollPane()).top().widget();
            inspector=add((ReskinView)theme.view()).top().widget();
            inspector.spacing=9;inspector.hasScrollBar=true;
            snapshot();
        }
        private WButton button(String text,Runnable action){WButton b=add(theme.button(text)).top().expandWidgetX().widget();b.action=action;return b;}
        void setFilter(String value){filter=value;dirty=true;page=0;invalidate();}
        void snapshot(){
            categories.clear();all.clear();ReskinIcons.invalidateCategoryIcons();
            for(Category c:Modules.loopCategories()){
                List<Module> group=Modules.get().getGroup(c);
                if(group==null)continue;
                boolean found=false;
                for(Module m:group){if(!Config.get().hiddenModules.get().contains(m)){all.add(m);found=true;}}
                if(found)categories.add(c);
            }
            all.sort(Comparator.comparing(m->m.title.toLowerCase(Locale.ROOT)));
            lastModuleCount=Modules.get().getAll().size();
            if(selected!=null&&!all.contains(selected))selected=null;
            if(category!=null&&!categories.contains(category))category=null;
            if(selected==null&&!all.isEmpty())selected=all.getFirst();
            dirty=true;detailDirty=true;
            rebuildRail();
        }
        void rebuildRail(){
            rail.clear();
            rail.add(new ReskinHeading("LIBRARY"));
            rail.add(new ReskinCategoryButton(null,"All modules",()->all.size(),()->category==null,()->{category=null;dirty=true;page=0;showCategories=false;invalidate();}));
            rail.add(new ReskinHeading("CATEGORIES"));
            for(Category c:categories)rail.add(new ReskinCategoryButton(c,c.name,()->(int)all.stream().filter(m->m.category==c).count(),()->category==c,()->{
                category=c;dirty=true;page=0;showCategories=false;invalidate();
            }));
        }
        boolean matches(Module m){
            if(category!=null&&m.category!=category)return false;
            if(filter.equals("Favorites")&&!m.favorite)return false;
            if(filter.equals("Enabled")&&!m.isActive())return false;
            if(query.isEmpty())return true;
            if((m.title+" "+m.name+" "+m.description).toLowerCase(Locale.ROOT).contains(query))return true;
            for(var group:m.settings)for(var setting:group)if(setting.title.toLowerCase(Locale.ROOT).contains(query))return true;
            return false;
        }
        void rebuildContent(){
            content.clear();
            for(DockColumn d:docks)cells.removeIf(c->c.widget()==d);
            docks.clear();
            ReskinTheme.StylePreset style=t().style();
            if(style==ReskinTheme.StylePreset.NEON_DOCK){
                for(Category c:categories){
                    List<Module> group=all.stream().filter(m->m.category==c&&matches(m)).toList();
                    if(!group.isEmpty())docks.add(add(new DockColumn(c,group)).top().widget());
                }
            }else{
                if(style==ReskinTheme.StylePreset.OBSIDIAN_COMMAND){
                    for(Category c:categories){
                        List<Module> group=all.stream().filter(m->m.category==c&&matches(m)).toList();
                        if(group.isEmpty())continue;
                        content.add(new ReskinHeading(c.name+"  /  "+group.size()));
                        for(Module m:group)content.add(new ReskinModuleCard(m,false,false,this::select,()->selected));
                    }
                }else{
                    for(Module m:all)if(matches(m))content.add(new ReskinModuleCard(m,style==ReskinTheme.StylePreset.APEX_WORKSPACE,false,this::select,()->selected));
                }
            }
            dirty=false;lastStyle=style;
        }
        void select(Module module){
            selected=module;detailDirty=true;
            if(!hasDetail||t().style()==ReskinTheme.StylePreset.NEON_DOCK)ReskinAccess.later(()->ReskinAccess.open(theme.moduleScreen(module)));
            else invalidate();
        }
        void rebuildInspector(){
            inspector.clear();settingsContainer=null;
            if(selected==null){inspector.add(theme.label("Select a module to inspect it."));detailDirty=false;return;}
            inspector.add(theme.label(selected.description,Math.max(t().scale(100),detailW-t().scale(50)))).expandX();
            inspector.add(theme.horizontalSeparator("Settings")).expandX();
            settingsContainer=inspector.add(theme.verticalList()).expandX().widget();
            if(selected.settings.groups.isEmpty())settingsContainer.add(theme.label("No standard settings."));
            else settingsContainer.add(theme.settings(selected.settings)).expandX();
            inspector.add(theme.horizontalSeparator("Existing controls")).expandX();
            inspector.add(theme.label("Full settings opens Meteor's original editor, including addon-specific controls, keybinds and config sharing.",Math.max(t().scale(100),detailW-t().scale(50)))).expandX();
            detailDirty=false;
        }
        void tickWorkspace(){
            if(lastModuleCount!=Modules.get().getAll().size())snapshot();
            if(selected!=null&&settingsContainer!=null&&hasDetail)selected.settings.tick(settingsContainer,theme);
            // Live status/filter data is checked without rebuilding the widget tree every frame.
            if(!filter.equals("All")){
                int wanted=(int)all.stream().filter(this::matches).count();
                int actual=t().style()==ReskinTheme.StylePreset.NEON_DOCK?docks.stream().mapToInt(d->d.modules.size()).sum():(int)content.cells.stream().filter(c->c.widget() instanceof ReskinModuleCard).count();
                if(wanted!=actual){dirty=true;invalidate();}
            }
        }
        @Override protected void onCalculateSize(){
            ReskinTheme t=t();width=Utils.getWindowWidth();height=Utils.getWindowHeight();
            margin=t.scale(14);gap=t.scale(12);top=t.topInset();
            mainX=margin+t.sideInset();mainW=Math.max(1,width-mainX-margin);
            toolbarHeight=t.scale(mainW<t.scale(650)?130:92);
            bodyY=top+toolbarHeight;bodyH=Math.max(1,height-bodyY-t.bottomInset()-t.scale(24));
            boolean neon=t.style()==ReskinTheme.StylePreset.NEON_DOCK;
            hasRail=mainW>=t.scale(neon?900:850);
            railW=hasRail?t.scale(t.style()==ReskinTheme.StylePreset.SLATE_SIDEBAR?178:202):0;
            double remaining=mainW-(hasRail?railW+gap:0);
            hasSummary=t.style()==ReskinTheme.StylePreset.APEX_WORKSPACE && remaining>=t.scale(900);
            hasDetail=!neon && remaining>=t.scale(t.style()==ReskinTheme.StylePreset.SLATE_SIDEBAR?650:760);
            detailW=hasDetail?Math.min(t.scale(t.style()==ReskinTheme.StylePreset.SLATE_SIDEBAR?490:390),remaining*.48):0;
            contentW=Math.max(1,remaining-(hasDetail?detailW+gap:0));
            contentX=mainX+(hasRail?railW+gap:0);detailX=contentX+contentW+gap;
            if(dirty||lastStyle!=t.style())rebuildContent();
            if(detailDirty&&hasDetail)rebuildInspector();
            rail.visible=hasRail||showCategories;
            rail.bounds(hasRail?railW:mainW,bodyH);rail.calculateSize();
            content.visible=!neon&&!(!hasRail&&showCategories);
            content.columns=t.style()==ReskinTheme.StylePreset.APEX_WORKSPACE?Math.max(1,(int)Math.floor(contentW/t.scale(252))):1;
            content.bounds(contentW,bodyH);content.calculateSize();
            columns=Math.max(1,Math.min(t.maxColumns.get(),(int)Math.floor((remaining+gap)/(t.scale(218)+gap))));
            pages=Math.max(1,(docks.size()+columns-1)/columns);page=Math.max(0,Math.min(page,pages-1));
            double colW=Math.max(1,(remaining-gap*(columns-1))/columns);
            for(int i=0;i<docks.size();i++){
                DockColumn d=docks.get(i);d.visible=i>=page*columns&&i<(page+1)*columns&&!(!hasRail&&showCategories);
                d.pane.bounds(colW,Math.max(1,bodyH-t.scale(49)));d.pane.calculateSize();d.width=colW;d.height=bodyH;
            }
            inspector.visible=hasDetail&&!(!hasRail&&showCategories);
            inspector.externallyConstrainedMaxWidth=Math.max(1,detailW-t.scale(24));
            inspector.externallyConstrainedMaxHeight=Math.max(1,bodyH-t.scale(hasSummary?204:132));
            if(inspector.visible)inspector.calculateSize();
            edit.visible=pin.visible=inspector.visible&&selected!=null;
            prev.visible=neon&&page>0;next.visible=neon&&page<pages-1;
            categoriesButton.visible=!hasRail;
            for(WButton b:List.of(allButton,favButton,enabledButton,categoriesButton,prev,next,appearance,edit,pin))b.calculateSize();
            search.calculateSize();
        }
        @Override protected void onCalculateWidgetPositions(){
            ReskinTheme t=t();boolean small=mainW<t.scale(650);
            double searchW=small?mainW:Math.min(t.scale(400),mainW*.44);
            locate(search,small?mainX:mainX+mainW-searchW,top+t.scale(small?39:7),searchW,t.scale(32));
            double rowY=top+t.scale(small?82:54),bx=mainX;
            if(categoriesButton.visible){locate(categoriesButton,bx,rowY,t.scale(99),t.scale(30));bx+=t.scale(105);}
            locate(allButton,bx,rowY,t.scale(47),t.scale(30));bx+=t.scale(53);
            locate(favButton,bx,rowY,t.scale(90),t.scale(30));bx+=t.scale(96);
            locate(enabledButton,bx,rowY,t.scale(80),t.scale(30));
            appearance.visible=mainW>=t.scale(530);
            if(appearance.visible)locate(appearance,mainX+mainW-t.scale(116),rowY,t.scale(116),t.scale(30));
            double arrowX=mainX+mainW-(appearance.visible?t.scale(206):t.scale(88));
            locate(prev,arrowX,rowY,t.scale(32),t.scale(30));locate(next,arrowX+t.scale(40),rowY,t.scale(32),t.scale(30));
            locate(rail,mainX,bodyY,rail.viewportWidth,bodyH);
            locate(content,contentX,bodyY,contentW,bodyH);
            for(int i=page*columns;i<docks.size()&&i<(page+1)*columns;i++){
                DockColumn d=docks.get(i);locate(d,contentX+(i-page*columns)*(d.width+gap),bodyY,d.width,bodyH);
            }
            double offset=t.scale(hasSummary?204:132);
            locate(inspector,detailX+t.scale(12),bodyY+offset,Math.max(1,detailW-t.scale(24)),inspector.height);
            locate(edit,detailX+t.scale(14),bodyY+offset-t.scale(43),Math.max(1,detailW*.52-t.scale(18)),t.scale(30));
            locate(pin,detailX+detailW*.52,bodyY+offset-t.scale(43),Math.max(1,detailW*.48-t.scale(14)),t.scale(30));
        }
        private void locate(WWidget w,double xx,double yy,double ww,double hh){
            for(Cell<?> c:cells)if(c.widget()==w){c.x=xx;c.y=yy;c.width=Math.max(1,ww);c.height=Math.max(1,hh);c.expandWidgetX();c.alignWidget();break;}
        }
        @Override protected boolean propagateEvents(WWidget w){return w.visible;}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
            ReskinTheme t=t();
            // A restrained full-workspace dimmer keeps the world from competing with text.
            r.quad(mainX-margin/2,top-margin/2,mainW+margin,Math.max(1,height-top-t.bottomInset()+margin/2),ReskinDraw.alpha(t.surface(),170));
            String title=switch(t.style()){
                case OBSIDIAN_COMMAND->"MODULE LIBRARY";case NEON_DOCK->"NEON DOCK";case SLATE_SIDEBAR->"MODULES / "+(category==null?"ALL":category.name);case APEX_WORKSPACE->"APEX WORKSPACE";
            };
            ReskinDraw.text(r,t,title,mainX+t.scale(3),top+t.scale(7),Math.max(t.scale(100),mainW*.53),t.titleTextColor.get(),true);
            if(mainW>=t.scale(650))ReskinDraw.text(r,t,t.styleTitle()+"  /  "+filter+"  /  "+all.stream().filter(this::matches).count()+" results",mainX+t.scale(3),top+t.scale(31),mainW*.52,t.textSecondaryColor.get(),false);
            if(hasDetail&&!(!hasRail&&showCategories)){
                ReskinDraw.panel(r,t,detailX,bodyY,detailW,bodyH,false);
                double yy=bodyY+t.scale(16);
                if(hasSummary){
                    ReskinDraw.text(r,t,"WORKSPACE",detailX+t.scale(16),yy,detailW-t.scale(32),t.accentColor.get(),false);
                    yy+=t.scale(26);
                    ReskinDraw.text(r,t,all.size()+" modules   /   "+all.stream().filter(Module::isActive).count()+" enabled   /   "+all.stream().filter(m->m.favorite).count()+" favorites",detailX+t.scale(16),yy,detailW-t.scale(32),t.textColor.get(),false);
                    yy+=t.scale(47);
                }
                if(selected!=null){
                    ReskinIcons.draw(r,t,selected.category,selected,selected.title,detailX+t.scale(16),yy,t.scale(34));
                    inspectorTitle.draw(r,t,selected.title,detailX+t.scale(62),yy,detailW-t.scale(77),t.textHeight(true),true,t.titleTextColor.get(),ReskinDraw.hit(mx,my,detailX,yy,detailW,t.scale(35)),dt);
                    ReskinDraw.text(r,t,(selected.category==null?"Addon":selected.category.name)+"  /  "+(selected.isActive()?"Enabled":"Disabled"),detailX+t.scale(62),yy+t.scale(25),detailW-t.scale(77),t.textSecondaryColor.get(),false);
                }
            }
            if(content.visible&&content.cells.isEmpty()||t.style()==ReskinTheme.StylePreset.NEON_DOCK&&docks.isEmpty())
                ReskinDraw.text(r,t,"No matching modules. Clear search or choose All.",contentX+t.scale(22),bodyY+t.scale(32),contentW-t.scale(44),t.textSecondaryColor.get(),false);
            String footer=t.style()==ReskinTheme.StylePreset.NEON_DOCK?"CATEGORY PAGE "+(page+1)+" / "+pages+"   •   Right click a module for full settings":"Select: inspect   •   Switch: toggle   •   Star: favorite   •   Right click: full settings";
            ReskinDraw.text(r,t,footer,mainX,bodyY+bodyH+t.scale(7),mainW,t.textSecondaryColor.get(),false);
        }
        final class DockColumn extends WContainer {
            final Category cat;final List<Module> modules;ReskinScrollPane pane;final ReskinText title=new ReskinText();
            DockColumn(Category cat,List<Module> modules){this.cat=cat;this.modules=modules;}
            @Override public void init(){pane=add(new ReskinScrollPane()).top().widget();pane.gap=4;
                for(Module m:modules)pane.add(new ReskinModuleCard(m,false,true,Workspace.this::select,()->selected));}
            @Override protected void onCalculateSize(){width=pane.viewportWidth;height=pane.viewportHeight+theme.scale(49);}
            @Override protected void onCalculateWidgetPositions(){Cell<?> c=cells.getFirst();c.x=x;c.y=y+theme.scale(49);c.width=width;c.height=pane.height;c.expandWidgetX();c.alignWidget();}
            @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
                ReskinTheme t=t();ReskinDraw.panel(r,t,x,y,width,theme.scale(43),true);
                ReskinIcons.draw(r,t,cat,null,cat.name,x+t.scale(8),y+t.scale(6),t.scale(29));
                title.draw(r,t,cat.name,x+t.scale(45),y+t.scale(6),width-t.scale(54),t.textHeight(),false,t.titleTextColor.get(),ReskinDraw.hit(mx,my,x,y,width,t.scale(43)),dt);
                ReskinDraw.text(r,t,modules.size()+" modules",x+t.scale(45),y+t.scale(24),width-t.scale(54),t.textSecondaryColor.get(),false);
            }
        }
    }
}
