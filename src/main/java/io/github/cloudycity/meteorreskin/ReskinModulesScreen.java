// SPDX-License-Identifier: GPL-3.0-only
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

/** Eight functional compositions over Meteor's registered module instances. */
public final class ReskinModulesScreen extends TabScreen {
    private Workspace workspace;
    Workspace currentWorkspace(){return workspace;}
    public ReskinModulesScreen(GuiTheme theme){super(theme,Tabs.get().getFirst());}
    @Override public void initWidgets(){workspace=add(new Workspace()).top().expandX().widget();workspace.focusSearch();}
    @Override protected void init(){super.init();if(workspace!=null)workspace.focusSearch();}
    @Override public void tick(){super.tick();if(workspace!=null)workspace.tickWorkspace();}
    @Override public void reload(){if(workspace!=null){workspace.dirty=true;workspace.detailDirty=true;invalidate();}}
    @Override public boolean toClipboard(){return NbtUtils.toClipboard(Modules.get());}
    @Override public boolean fromClipboard(){boolean result=NbtUtils.fromClipboard(Modules.get());if(result)reload();return result;}
    @Override protected void onClosed(){super.onClosed();((ReskinTheme)theme).saveAppearance();}

    final class Workspace extends WContainer {
        final List<Category> categories=new ArrayList<>();final List<Module> all=new ArrayList<>();final List<DockColumn> docks=new ArrayList<>();
        final Set<Category> collapsed=Collections.newSetFromMap(new IdentityHashMap<>());
        final List<Module> recent=new ArrayList<>();
        ReskinScrollPane rail,content,auxiliary,summary;
        ReskinView inspector;WContainer settingsContainer;Module selected;Category category;
        int sortMode=0; boolean listMode=false; WButton sortButton,viewButton;
        WTextBox search;WButton allButton,favButton,enabledButton,categoriesButton,prev,next,appearance,edit,closeDetail;
        String query="",filter="All",cachedQuery="";
        final Map<Module,Boolean> searchCache=new IdentityHashMap<>();int resultCount;
        boolean dirty=true,detailDirty=true,showCategories,hasDetail,hasRail,hasSummary,detailOpen,detailOnly,hasAux;
        int page,pages=1,columns=1,lastModuleCount=-1;double lastDetailW=-1;
        double margin,gap,top,bodyY,bodyH,mainX,mainW,railW,contentX,contentW,detailX,detailY,detailW,detailH,toolbarHeight,auxW,rightHeader;
        ReskinTheme.StylePreset lastStyle;private final ReskinText inspectorTitle=new ReskinText();
        private ReskinTheme t(){return (ReskinTheme)theme;}
        boolean isGrid(){if(listMode)return false;return switch(t().style()){case APEX_WORKSPACE,AURORA_GLASS,PRISM_STUDIO->true;default->false;};}
        boolean isNeon(){return t().style()==ReskinTheme.StylePreset.NEON_DOCK;}
        boolean isAtlas(){return t().style()==ReskinTheme.StylePreset.ATLAS_PANELS;}
        boolean isPrism(){return t().style()==ReskinTheme.StylePreset.PRISM_STUDIO;}
        void focusSearch(){if(search!=null){search.setFocused(true);search.setCursorMax();}}
        @Override public void init(){
            search=add(theme.textBox("","Search modules or settings...")).top().expandWidgetX().widget();
            search.action=()->{query=search.get().strip();if(!query.isBlank())collapsed.clear();page=0;dirty=true;invalidate();};
            allButton=button("All",()->setFilter("All"));favButton=button("Favorites",()->setFilter("Favorites"));enabledButton=button("Enabled",()->setFilter("Enabled"));
            sortButton=button("Sort: A–Z",()->{sortMode=(sortMode+1)%3;sortButton.set(sortMode==0?"Sort: A–Z":sortMode==1?"Sort: Z–A":"Enabled first");sortModules();dirty=true;invalidate();});
            viewButton=button("List",()->{listMode=!listMode;viewButton.set(listMode?"Grid":"List");dirty=true;invalidate();});
            categoriesButton=button("Categories",()->{showCategories=!showCategories;invalidate();});
            prev=button("Prev",()->{page=Math.max(0,page-1);invalidate();});next=button("Next",()->{page=Math.min(pages-1,page+1);invalidate();});
            appearance=button("Appearance",()->ReskinAccess.later(()->t().appearanceTab().openScreen(theme)));
            edit=button("Full settings",()->{if(selected!=null)ReskinAccess.later(()->ReskinAccess.open(theme.moduleScreen(selected)));});
            closeDetail=button("Back to modules",()->{detailOpen=false;detailOnly=false;settingsContainer=null;invalidate();focusSearch();});
            rail=add(new ReskinScrollPane()).top().widget();content=add(new ReskinScrollPane()).top().widget();
            auxiliary=add(new ReskinScrollPane()).top().widget();summary=add(new ReskinScrollPane()).top().widget();
            inspector=add((ReskinView)theme.view()).top().widget();inspector.spacing=10;inspector.hasScrollBar=true;inspector.verticalOnly=true;
            detailOpen=switch(t().style()){case OBSIDIAN_COMMAND,SLATE_SIDEBAR,HORIZON_SPLIT,ATLAS_PANELS,AURORA_GLASS->true;default->false;};
            if(t().logicalWidth()-t().sideInset()<t().scale(900))detailOpen=false;
            snapshot();focusSearch();
        }
        private WButton button(String text,Runnable action){WButton b=add(theme.button(text)).top().expandWidgetX().widget();b.action=action;return b;}
        void setFilter(String value){filter=value;if(!value.equals("All"))collapsed.clear();dirty=true;page=0;invalidate();}
        void snapshot(){
            boolean firstSnapshot=lastModuleCount<0;
            categories.clear();all.clear();searchCache.clear();ReskinIcons.invalidateCategoryIcons();
            for(Category c:Modules.loopCategories()){
                List<Module> group=Modules.get().getGroup(c);if(group==null)continue;boolean found=false;
                for(Module m:group)if(!Config.get().hiddenModules.get().contains(m)){all.add(m);found=true;}
                if(found)categories.add(c);
            }
            sortModules();lastModuleCount=Modules.get().getAll().size();
            if(selected!=null&&!all.contains(selected))selected=null;if(category!=null&&!categories.contains(category))category=null;
            if(firstSnapshot)for(int i=1;i<categories.size();i++)collapsed.add(categories.get(i));
            if(selected==null&&!all.isEmpty()) {
                Category first=categories.isEmpty()?null:categories.getFirst();
                selected=all.stream().filter(m->m.category==first).findFirst().orElse(all.getFirst());
            }dirty=true;detailDirty=true;rebuildRail();rebuildAuxiliary();
        }
        void sortModules(){
            Comparator<Module> order=Comparator.comparing(m->m.title.toLowerCase(Locale.ROOT));
            if(sortMode==1)order=order.reversed();
            if(sortMode==2)order=Comparator.comparing((Module m)->!m.isActive()).thenComparing(order);
            all.sort(order);
        }
        void rebuildRail(){
            rail.clear();rail.pinnedTail=4;rail.add(new ReskinHeading(isPrism()?"COLLECTIONS":"LIBRARY"));
            rail.add(new ReskinCategoryButton(null,"All modules",()->all.size(),()->category==null,()->{category=null;filter="All";dirty=true;page=0;showCategories=false;invalidate();}));
            rail.add(new ReskinHeading("CATEGORIES"));
            for(Category c:categories)rail.add(new ReskinCategoryButton(c,c.name,()->(int)all.stream().filter(m->m.category==c).count(),()->category==c,()->{category=c;collapsed.remove(c);dirty=true;page=0;showCategories=false;invalidate();}));
            rail.add(new ReskinHeading("COLLECTIONS"));
            rail.add(new ReskinCategoryButton(null,"Favorites",()->(int)all.stream().filter(m->m.favorite).count(),()->filter.equals("Favorites"),()->{category=null;setFilter("Favorites");showCategories=false;}));
            rail.add(new ReskinCategoryButton(null,"Recent",()->recent.size(),()->filter.equals("Recent"),()->{category=null;setFilter("Recent");showCategories=false;}));
            rail.add(new ReskinCategoryButton(null,"Enabled",()->(int)all.stream().filter(Module::isActive).count(),()->filter.equals("Enabled"),()->{category=null;setFilter("Enabled");showCategories=false;}));
            for(int i=rail.cells.size()-3;i<rail.cells.size();i++)
                ((ReskinCategoryButton)rail.cells.get(i).widget()).listOnly=true;
        }
        boolean matches(Module m){
            if(filter.equals("Recent")&&!recent.contains(m))return false;
            if(query.isBlank()&&category!=null&&m.category!=category)return false;if(filter.equals("Favorites")&&!m.favorite)return false;if(filter.equals("Enabled")&&!m.isActive())return false;
            if(query.isBlank())return true;
            if(!cachedQuery.equals(query)){cachedQuery=query;searchCache.clear();}
            return searchCache.computeIfAbsent(m,this::matchesSearch);
        }
        private boolean matchesSearch(Module m){
            if(ReskinSearch.matches(query,m.title,m.name,m.description))return true;
            if(m.aliases!=null)for(String alias:m.aliases)if(ReskinSearch.matches(query,alias))return true;
            for(var group:m.settings)for(var setting:group)if(ReskinSearch.matches(query,setting.title,setting.name))return true;
            return false;
        }
        void rebuildContent(){
            resultCount=(int)all.stream().filter(this::matches).count();
            content.clear();for(DockColumn d:docks)cells.removeIf(c->c.widget()==d);docks.clear();
            if(isNeon()){
                for(Category c:categories){List<Module> group=all.stream().filter(m->m.category==c&&matches(m)).toList();if(!group.isEmpty())docks.add(add(new DockColumn(c,group)).top().widget());}
            }else if(t().style()==ReskinTheme.StylePreset.OBSIDIAN_COMMAND){
                for(Category c:categories){List<Module> group=all.stream().filter(m->m.category==c&&matches(m)).toList();if(group.isEmpty())continue;
                    content.add(new ReskinGroupHeading(c,group.size(),()->!collapsed.contains(c),()->{if(!collapsed.add(c))collapsed.remove(c);dirty=true;invalidate();}));
                    if(!collapsed.contains(c)){
                        boolean overview=category==null&&query.isBlank()&&filter.equals("All");
                        int count=overview?Math.min(6,group.size()):group.size();
                        for(int i=0;i<count;i++)content.add(new ReskinModuleCard(group.get(i),false,false,this::select,()->selected));
                        if(overview&&group.size()>count)content.add(new ReskinGroupMore("Show all "+group.size()+" "+c.name+" modules →",()->{category=c;collapsed.remove(c);dirty=true;invalidate();}));
                    }
                }
            }else{
                if(isPrism())content.add(new ReskinHeading("MODULE LIBRARY"));
                if(isAtlas())content.add(new ReskinTableHeader());
                for(Module m:all)if(matches(m))content.add(new ReskinModuleCard(m,isGrid(),isAtlas(),this::select,()->selected));
            }
            dirty=false;lastStyle=t().style();rebuildAuxiliary();
        }
        void rebuildAuxiliary(){
            auxiliary.clear();auxiliary.add(new ReskinHeading("FAVORITES"));
            for(Module m:all)if(m.favorite&&auxiliary.cells.size()<5)auxiliary.add(new ReskinModuleCard(m,false,true,this::select,()->selected));
            if(all.stream().noneMatch(m->m.favorite))auxiliary.add(new ReskinParagraph("Star modules to place them here."));
            auxiliary.add(new ReskinHeading("RECENT"));for(Module m:recent.subList(0,Math.min(3,recent.size())))auxiliary.add(new ReskinModuleCard(m,false,true,this::select,()->selected));
            if(recent.isEmpty())auxiliary.add(new ReskinParagraph("Modules you inspect appear here."));
            auxiliary.add(new ReskinHeading("QUICK ACCESS"));
            var focus=auxiliary.add(theme.button("Focus search")).expandX().widget();focus.action=this::focusSearch;
            var app=auxiliary.add(theme.button("Appearance")).expandX().widget();app.action=()->ReskinAccess.later(()->t().appearanceTab().openScreen(theme));
            summary.clear();
            if(isPrism()){
                summary.add(new ReskinHeading("STYLE & SETTINGS"));
                summary.add(new ReskinMiniPresets()).expandX();
                summary.add(new ReskinHeading("CUSTOM ACCENT"));summary.add(new ReskinColorField("Accent",t().accentColor));
                WButton appEditor=summary.add(theme.button("Appearance editor")).expandX().widget();appEditor.action=()->ReskinAccess.later(()->t().appearanceTab().openScreen(theme));
            }else{
                summary.add(new ReskinHeading("WORKSPACE SUMMARY"));summary.add(new ReskinStats(all));
                summary.add(new ReskinHeading("FAVORITES"));for(Module m:all.stream().filter(m->m.favorite).limit(5).toList())summary.add(new ReskinModuleCard(m,false,true,this::select,()->selected));
                var favorites=summary.add(theme.button("View all favorites")).expandX().widget();favorites.action=()->{category=null;setFilter("Favorites");};
                summary.add(new ReskinHeading("QUICK FILTERS"));
                for(String name:List.of("All","Enabled","Favorites")){var b=summary.add(theme.button(name)).expandX().widget();b.action=()->{category=null;setFilter(name);};}
                if(all.stream().noneMatch(m->m.favorite))summary.add(theme.label("Star modules to keep them here."));
            }
        }
        void select(Module module){selected=module;dirty=true;detailDirty=true;detailOpen=true;recent.remove(module);recent.addFirst(module);if(recent.size()>8)recent.removeLast();invalidate();}
        void rebuildInspector(){
            inspector.clear();settingsContainer=null;
            if(selected==null){inspector.add(theme.label("Select a module to inspect it."));detailDirty=false;return;}
            inspector.add(new ReskinParagraph(selected.description)).expandX();
            settingsContainer=inspector.add(theme.verticalList()).expandX().widget();
            if(selected.settings.groups.isEmpty())settingsContainer.add(new ReskinParagraph("No standard settings for this module. Full settings opens its native editor and any addon-provided controls."));else settingsContainer.add(theme.settings(selected.settings)).expandX();
            
            detailDirty=false;lastDetailW=detailW;
        }
        void tickWorkspace(){
            if(lastModuleCount!=Modules.get().getAll().size())snapshot();
            if(selected!=null&&settingsContainer!=null&&hasDetail)selected.settings.tick(settingsContainer,theme);
            if(!filter.equals("All")){int wanted=(int)all.stream().filter(this::matches).count();int actual=isNeon()?docks.stream().mapToInt(d->d.modules.size()).sum():(int)content.cells.stream().filter(c->c.widget() instanceof ReskinModuleCard).count();if(wanted!=actual&&!(!collapsed.isEmpty()&&t().style()==ReskinTheme.StylePreset.OBSIDIAN_COMMAND)){dirty=true;invalidate();}}
        }
        @Override protected void onCalculateSize(){
            var t=t();width=Utils.getWindowWidth();height=Utils.getWindowHeight();
            margin=t.composition(width<t.composition(900)?16:t.style()==ReskinTheme.StylePreset.AURORA_GLASS?54:40);
            gap=t.composition(isAtlas()?18:t.style()==ReskinTheme.StylePreset.AURORA_GLASS?20:16);top=t.topInset();
            mainX=margin+t.sideInset();mainW=Math.max(1,width-mainX-margin);
            detailY=top;detailH=Math.max(1,height-top-t.bottomInset()-t.composition(22));
            hasDetail=detailOpen&&selected!=null;
            detailOnly=hasDetail&&(mainW<t.composition(880)||detailH<t.scale(300));
            hasRail=!detailOnly&&mainW>=t.composition(hasDetail?1030:780)&&!(t.sideInset()>0);
            railW=hasRail?Math.max(t.composition(isNeon()?270:240),t.textWidth("Movement")+t.scale(94)):0;
            if(hasRail&&isNeon())railW=Math.max(railW,2*(t.textWidth("Movement")+t.scale(20))+t.scale(34));
            double remaining=mainW-(hasRail?railW+gap:0);
            double wish=t.composition(switch(t.style()){case HORIZON_SPLIT->650;case SLATE_SIDEBAR->600;case ATLAS_PANELS->450;case AURORA_GLASS->450;default->470;});
            if(t.style()==ReskinTheme.StylePreset.HORIZON_SPLIT||t.style()==ReskinTheme.StylePreset.SLATE_SIDEBAR)wish=Math.max(wish,remaining*.52);
            detailW=hasDetail?(detailOnly?mainW:Math.min(wish,remaining-t.composition(290)-gap)):0;
            hasSummary=!hasDetail&&(t.style()==ReskinTheme.StylePreset.APEX_WORKSPACE||isPrism())&&remaining>=t.composition(820);
            if(hasSummary)detailW=t.composition(isPrism()?290:300);
            hasAux=isAtlas()&&!detailOnly&&remaining-detailW>=t.composition(630);auxW=hasAux?t.composition(250):0;
            contentW=detailOnly?0:Math.max(1,remaining-(hasDetail||hasSummary?detailW+gap:0)-(hasAux?auxW+gap:0));
            contentX=mainX+(hasRail?railW+gap:0)+(hasAux?auxW+gap:0);detailX=detailOnly?mainX:mainX+mainW-detailW;
            toolbarHeight=detailOnly?0:t.composition(isPrism()?170:isNeon()?130:100);
            bodyY=top+toolbarHeight;bodyH=Math.max(1,height-bodyY-t.bottomInset()-t.composition(22));
            if(dirty||lastStyle!=t.style())rebuildContent();
            if((detailDirty||Math.abs(lastDetailW-detailW)>1)&&hasDetail)rebuildInspector();
            boolean overlay=showCategories&&!hasRail;
            rail.visible=hasRail||overlay;rail.columns=isNeon()&&hasRail?2:1;rail.gap=isNeon()?9:5;
            rail.bounds(hasRail?railW:mainW,detailH);rail.calculateSize();
            content.visible=!isNeon()&&!detailOnly&&!overlay;
            int cap=isPrism()?4:3;
            double cardMinimum=Math.max(t.composition(isPrism()?222:255),t.textWidth("Block Selection")+t.scale(112));
            content.columns=isGrid()?t.compositionColumns(Math.max(1,contentW-t.scale(24)),gap,cardMinimum,cap):1;
            content.gap=t.style()==ReskinTheme.StylePreset.AURORA_GLASS?14:isGrid()?12:3;
            content.background=true;content.bounds(Math.max(1,contentW),bodyH);content.calculateSize();
            double minColumn=Math.max(t.composition(220),t.textWidth("Inventory Tweaks")+t.scale(82));
            columns=Math.max(1,Math.min(t.maxColumns.get(),t.compositionColumns(contentW,gap,minColumn,4)));
            pages=Math.max(1,(docks.size()+columns-1)/columns);page=Math.max(0,Math.min(page,pages-1));
            double cw=Math.max(1,(contentW-gap*(columns-1))/columns);
            for(int i=0;i<docks.size();i++){var d=docks.get(i);d.visible=isNeon()&&!detailOnly&&!overlay&&i>=page*columns&&i<(page+1)*columns;d.pane.bounds(cw,Math.max(1,bodyH-t.composition(76)));d.pane.calculateSize();d.width=cw;d.height=bodyH;}
            auxiliary.visible=hasAux&&!overlay;auxiliary.bounds(Math.max(1,auxW),detailH);auxiliary.calculateSize();
            summary.visible=hasSummary&&!overlay;summary.bounds(Math.max(1,detailW),detailH);summary.calculateSize();
            rightHeader=Math.min(t.composition(t.style()==ReskinTheme.StylePreset.HORIZON_SPLIT?178:174),Math.max(t.scale(130),detailH*.48));
            inspector.visible=hasDetail&&!overlay;
            inspector.externallyConstrainedMaxWidth=Math.max(1,detailW-t.scale(32));inspector.externallyConstrainedMaxHeight=Math.max(1,detailH-rightHeader-t.scale(14));
            if(inspector.visible)inspector.calculateSize();
            edit.visible=closeDetail.visible=inspector.visible;
            prev.visible=isNeon()&&!detailOnly&&page>0;next.visible=isNeon()&&!detailOnly&&page<pages-1;categoriesButton.visible=!hasRail&&!detailOnly;
            allButton.visible=favButton.visible=enabledButton.visible=search.visible=!detailOnly;
            sortButton.visible=!detailOnly;viewButton.visible=!detailOnly&&(t.style()==ReskinTheme.StylePreset.APEX_WORKSPACE||t.style()==ReskinTheme.StylePreset.AURORA_GLASS||isPrism());
            for(var button:List.of(allButton,favButton,enabledButton,categoriesButton,prev,next,appearance,edit,closeDetail,sortButton,viewButton))button.calculateSize();search.calculateSize();
        }
        @Override protected void onCalculateWidgetPositions(){
            var t=t();double pad=t.composition(14),searchY=top+t.composition(isPrism()?82:isNeon()?54:13);
            locate(search,contentX+pad,searchY,Math.max(1,contentW-2*pad),Math.max(t.scale(38),t.textHeight()+t.composition(16)));
            double rowY=bodyY-t.composition(39),bx=contentX+pad,limit=contentX+contentW-pad-(isNeon()?t.composition(164):0);
            for(WButton button:List.of(categoriesButton,allButton,favButton,enabledButton,sortButton,viewButton))if(button.visible){
                double ww=button.width+t.scale(7)+2;
                if(bx+ww>limit+.5){button.visible=false;continue;}
                locate(button,bx,rowY,ww,t.scale(28));bx+=ww+t.scale(5);
            }
            appearance.visible=false;double arrow=contentX+contentW-t.composition(164);locate(prev,arrow,rowY,t.composition(68),t.scale(31));locate(next,arrow+t.composition(75),rowY,t.composition(68),t.scale(31));
            locate(rail,mainX,top,rail.viewportWidth,detailH);
            locate(auxiliary,mainX+(hasRail?railW+gap:0),top,Math.max(1,auxW),detailH);
            locate(content,contentX,bodyY,Math.max(1,contentW),bodyH);locate(summary,detailX,detailY,Math.max(1,detailW),detailH);
            for(int i=page*columns;i<docks.size()&&i<(page+1)*columns;i++){var d=docks.get(i);locate(d,contentX+(i-page*columns)*(d.width+gap),bodyY,d.width,bodyH);}
            locate(inspector,detailX+t.scale(16),detailY+rightHeader,Math.max(1,detailW-t.scale(32)),inspector.height);
            double ay=detailY+rightHeader-t.composition(39),left=detailX+t.scale(16),usable=detailW-t.scale(32),g=t.scale(7);
            double closeW=ReskinDraw.labelWidth(t,"Back to modules",17),editW=ReskinDraw.labelWidth(t,"Full settings",17);
            locate(closeDetail,left,ay,Math.min(closeW,usable*.55),t.scale(29));
            locate(edit,left+closeDetail.width+g,ay,Math.min(editW,Math.max(1,usable-closeDetail.width-g)),t.scale(29));
        }
        private void locate(WWidget w,double xx,double yy,double ww,double hh){for(Cell<?> c:cells)if(c.widget()==w){c.x=xx;c.y=yy;c.width=Math.max(1,ww);c.height=Math.max(1,hh);c.expandWidgetX();c.alignWidget();break;}}
        @Override protected boolean propagateEvents(WWidget w){return w.visible;}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
            var t=t();
            ReskinDraw.workspace(r,t,mainX-t.scale(12),top-t.scale(12),mainW+t.scale(24),detailH+t.scale(24));
            if(!detailOnly){
                ReskinDraw.panel(r,t,contentX,top,contentW,toolbarHeight-t.scale(6),false);
                if(isNeon()) {
                    ReskinGlyph.draw(r,"Modules",contentX+t.composition(16),top+t.composition(12),t.composition(27),t.accentColor.get());
                    ReskinDraw.text(r,t,"Modules browser",contentX+t.composition(54),top+t.composition(14),contentW*.64,t.titleTextColor.get(),true);
                    ReskinDraw.text(r,t,"PAGE "+(page+1)+" / "+pages,contentX+contentW-t.composition(130),top+t.composition(17),t.composition(116),t.accentColor.get(),false);
                }
                if(isPrism()){
                    ReskinDraw.ribbon(r,t,contentX+1,top+1,contentW-2,t.scale(63));
                    ReskinGlyph.draw(r,"logo",contentX+t.scale(14),top+t.scale(8),t.scale(42),t.accentColor.get());
                    ReskinDraw.text(r,t,"Prism Studio",contentX+t.scale(68),top+t.scale(13),Math.max(1,contentW-t.scale(90)),t.titleTextColor.get(),true);
                    ReskinDraw.text(r,t,"Browse. Configure. Make it yours.",contentX+t.scale(68),top+t.scale(40),Math.max(1,contentW-t.scale(90)),t.textSecondaryColor.get(),false);
                }
            }
            if(hasDetail&&inspector.visible){
                ReskinDraw.panel(r,t,detailX,detailY,detailW,detailH,false);
                double yy=detailY+t.composition(20);
                if(t.style()==ReskinTheme.StylePreset.HORIZON_SPLIT&&rightHeader>t.composition(165)){ReskinDraw.ribbon(r,t,detailX+1,detailY+1,detailW-2,t.composition(92));yy+=t.composition(9);}
                if(selected!=null){
                    ReskinDraw.panel(r,t,detailX+t.composition(18),yy,t.composition(52),t.composition(52),true);
                    ReskinIcons.draw(r,t,selected.category,selected,selected.title,detailX+t.composition(26),yy+t.composition(8),t.composition(36));
                    inspectorTitle.draw(r,t,selected.title,detailX+t.composition(86),yy+t.composition(2),Math.max(1,detailW-t.composition(108)),t.textHeight(true),true,t.titleTextColor.get(),ReskinDraw.hit(mx,my,detailX,yy,detailW,t.composition(60)),dt);
                    ReskinDraw.text(r,t,selected.category==null?"Addon":selected.category.name,detailX+t.composition(86),yy+t.composition(34),Math.max(1,detailW-t.composition(108)),t.textSecondaryColor.get(),false);
                    double statusY=detailY+t.composition(93);
                    ReskinGlyph.draw(r,"power",detailX+t.composition(18),statusY,t.composition(19),selected.isActive()?t.accentColor.get():t.textSecondaryColor.get());
                    ReskinDraw.text(r,t,selected.isActive()?"Enabled":"Disabled",detailX+t.composition(43),statusY,detailW*.40,t.textColor.get(),false);
                    ReskinDraw.star(r,detailX+detailW-t.composition(115),statusY,t.composition(16),selected.favorite?t.accentColor.get():t.textSecondaryColor.get());
                    ReskinDraw.text(r,t,"Favorite",detailX+detailW-t.composition(91),statusY,t.composition(82),t.textSecondaryColor.get(),false);
                    r.quad(detailX+t.composition(16),statusY+t.composition(30),Math.max(1,detailW-t.composition(32)),1,ReskinDraw.alpha(t.border(),125));
                }
            }
            if(content.visible&&content.cells.isEmpty()||isNeon()&&docks.isEmpty())ReskinDraw.text(r,t,"No matches. Clear search or choose All.",contentX+t.scale(16),bodyY+t.scale(25),Math.max(1,contentW-t.scale(32)),t.textSecondaryColor.get(),false);
            String footer=isNeon()?"CATEGORY PAGE "+(page+1)+" / "+pages+"  ·  "+resultCount+" modules":t.styleTitle()+"  /  "+resultCount+" modules  ·  Select a name to inspect  ·  Right-click for full settings";
            if(ReskinBuild.isPrerelease())ReskinDraw.text(r,t,footer,mainX,top+detailH+t.scale(10),mainW,t.textSecondaryColor.get(),false);
        }
        final class DockColumn extends WContainer {
            final Category cat;final List<Module> modules;ReskinScrollPane pane;final ReskinText title=new ReskinText();
            DockColumn(Category cat,List<Module> modules){this.cat=cat;this.modules=modules;}
            @Override public void init(){pane=add(new ReskinScrollPane()).top().widget();pane.gap=6;for(Module m:modules)pane.add(new ReskinModuleCard(m,false,true,Workspace.this::select,()->selected));}
            @Override protected void onCalculateSize(){width=pane.viewportWidth;height=pane.viewportHeight+((ReskinTheme)theme).composition(76);}
            @Override protected void onCalculateWidgetPositions(){var c=cells.getFirst();c.x=x;c.y=y+((ReskinTheme)theme).composition(76);c.width=width;c.height=pane.height;c.expandWidgetX();c.alignWidget();}
            @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){var t=t();ReskinDraw.panel(r,t,x,y,width,theme.scale(68),true);ReskinDraw.ribbon(r,t,x+2,y+2,width-4,theme.scale(64));ReskinIcons.draw(r,t,cat,null,cat.name,x+t.scale(12),y+t.scale(17),t.scale(34));title.draw(r,t,cat.name,x+t.scale(55),y+t.scale(13),width-t.scale(65),t.textHeight(),false,t.titleTextColor.get(),mouseOver,dt);ReskinDraw.text(r,t,modules.size()+" modules",x+t.scale(55),y+t.scale(38),width-t.scale(65),t.textSecondaryColor.get(),false);}
        }
    }
}
