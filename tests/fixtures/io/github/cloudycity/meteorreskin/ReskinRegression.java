package io.github.cloudycity.meteorreskin;
import java.util.*;import java.lang.reflect.*;import java.nio.file.*;
import meteordevelopment.meteorclient.MeteorClient;import meteordevelopment.meteorclient.gui.*;import meteordevelopment.meteorclient.gui.tabs.*;import meteordevelopment.meteorclient.gui.utils.Cell;import meteordevelopment.meteorclient.gui.widgets.*;import meteordevelopment.meteorclient.gui.widgets.containers.*;import meteordevelopment.meteorclient.gui.widgets.pressable.*;import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;import meteordevelopment.meteorclient.systems.modules.Modules;import meteordevelopment.meteorclient.systems.modules.Category;import meteordevelopment.meteorclient.utils.Utils;import net.minecraft.client.input.MouseButtonEvent;import net.minecraft.client.gui.screens.Screen;import com.mojang.blaze3d.platform.Window;
public final class ReskinRegression {
 static int checks;
 static void check(boolean yes,String msg){checks++;if(!yes)throw new AssertionError(msg);}
 static class TestTab extends Tab {TestTab(String s){super(s);}public TabScreen createScreen(GuiTheme t){if(name.equals("Modules"))return new ReskinModulesScreen(t);return new TabScreen(t,this){public void initWidgets(){var w=add(t.window(name)).center().widget();w.add(t.label("Existing Meteor page: "+name));w.add(t.button("Existing action"));}};}public boolean isScreen(Screen s){return s instanceof TabScreen ts&&ts.tab==this;}}
 static void setup(int cats,int rows){Modules.setup(cats,rows);Tabs.tabs.clear();for(String s:List.of("Modules","Config","GUI","HUD","Friends","Macros","Profiles","Baritone","Keybinds","A third-party tab"))Tabs.tabs.add(new TestTab(s));Utils.width=1666;Utils.height=896;Window.guiScale=2;MeteorClient.mc.gui.setScreen(null);}
 static void layout(WidgetScreen s){s.root.calculateSize();s.root.calculateWidgetPositions();s.root.invalid=false;}
 static List<WWidget> widgets(WWidget w){var list=new ArrayList<WWidget>();list.add(w);if(w instanceof WContainer c)for(Cell<?> cell:new ArrayList<>(c.cells))list.addAll(widgets(cell.widget()));return list;}
 static void render(WidgetScreen s,boolean capture,String path)throws Exception{GuiRenderer r=new GuiRenderer(capture);r.theme=s.root.theme;s.root.theme.beforeRender();s.root.render(r,0,0,.016);r.finish();check(r.clipDepth==0,"balanced clipping");if(capture)r.save(path);}
 static void flush(WidgetScreen s){if(s.taskAfterRender!=null){var task=s.taskAfterRender;s.taskAfterRender=null;task.run();}}
 static void click(WidgetScreen s,WWidget w){double mx=w.x+w.width/2,my=w.y+w.height/2;s.root.mouseMoved(mx,my,mx,my);var e=new MouseButtonEvent(mx,my,1);s.root.mouseClicked(e,false);s.root.mouseReleased(e);flush(s);}
 static ReskinModulesScreen.Workspace workspace(WidgetScreen s){return (ReskinModulesScreen.Workspace)widgets(s.root).stream().filter(w->w instanceof ReskinModulesScreen.Workspace).findFirst().orElseThrow();}
 static Object field(Object o,String n)throws Exception{Class<?> c=o.getClass();while(c!=null){try{var f=c.getDeclaredField(n);f.setAccessible(true);return f.get(o);}catch(NoSuchFieldException e){c=c.getSuperclass();}}throw new NoSuchFieldException(n);}
 static boolean visible(WWidget w){for(WWidget p=w;p!=null;p=p.parent)if(!p.visible)return false;return true;}
 static ReskinTopBar bar(WidgetScreen s){return (ReskinTopBar)widgets(s.root).stream().filter(w->w instanceof ReskinTopBar).findFirst().orElseThrow();}
 static WWidget nav(WidgetScreen s,String name)throws Exception{for(Cell<?> c:bar(s).cells){WWidget w=c.widget();if(w.getClass().getSimpleName().equals("NavButton")&&visible(w)&&name.equals(field(w,"title")))return w;}return null;}
 static WidgetScreen current(){return (WidgetScreen)MeteorClient.mc.gui.screen();}
 static void bounds(WWidget w,boolean vis){
  vis=vis&&w.visible;
  if(vis&&(w instanceof WWindow||w instanceof ReskinScrollPane||w instanceof ReskinView||w.getClass().getSimpleName().equals("NavButton"))){
   check(w.x>=-1&&w.y>=-1,"viewport starts outside screen: "+w.getClass()+" "+w.x+","+w.y);
   check(w.x+w.width<=Utils.width+2,"right overflow "+w.getClass()+" "+w.x+"+"+w.width+" > "+Utils.width);
   check(w.y+w.height<=Utils.height+2,"bottom overflow "+w.getClass()+" "+w.y+"+"+w.height+" > "+Utils.height);
  }
  if(w instanceof WWindow window){check(window.view!=null,"initialized view");check(window.cells.size()==2,"initialized exactly once");}
  if(w instanceof WContainer c)for(Cell<?> child:c.cells)bounds(child.widget(),vis);
 }
 static void matrix()throws Exception{
  setup(14,45);int cases=0;int[][] sizes={{640,360},{854,480},{1280,720},{1666,896},{1920,1080},{3840,2160}};
  for(var style:ReskinTheme.StylePreset.values())for(int[] size:sizes)for(int scale:new int[]{1,2,3,4}){
   Utils.width=size[0];Utils.height=size[1];Window.guiScale=scale;var theme=new ReskinTheme();theme.stylePreset.set(style);theme.applyStylePreset(style);
   Tabs.get().getFirst().openScreen(theme);var screen=current();layout(screen);bounds(screen.root,true);render(screen,false,null);
   var work=workspace(screen);
   if(style==ReskinTheme.StylePreset.NEON_DOCK)for(int page=0;page<work.pages;page++){work.page=page;layout(screen);bounds(screen.root,true);render(screen,false,null);}
   theme.appearanceTab().openScreen(theme);var appearance=current();layout(appearance);bounds(appearance.root,true);render(appearance,false,null);
   theme.shelfEnabled.set(true);layout(appearance);bounds(appearance.root,true);render(appearance,false,null);
   theme.shelfEnabled.set(false);cases++;
  }
  System.out.println("PASS layout matrix: "+cases+" combinations; all four layouts, every Neon category page, appearance and shared panels");
 }
 static void navigation()throws Exception{
  for(var style:ReskinTheme.StylePreset.values()){
   setup(12,35);Utils.width=854;Utils.height=480;var theme=new ReskinTheme();theme.stylePreset.set(style);theme.applyStylePreset(style);
   Tabs.get().getFirst().openScreen(theme);
   for(Tab tab:Tabs.get()){
    var screen=current();layout(screen);
    for(int i=0;i<20&&nav(screen,tab.name)==null;i++){
     WWidget n=(WWidget)field(bar(screen),"next");if(!n.visible)n=(WWidget)field(bar(screen),"prev");click(screen,n);layout(screen);
    }
    WWidget target=nav(screen,tab.name);check(target!=null,"tab reachable: "+tab.name+" / "+style);click(screen,target);
    check(current() instanceof TabScreen ts&&ts.tab==tab,"real Tab callback dispatch: "+tab.name);
   }
   var screen=current();layout(screen);click(screen,nav(screen,"Appearance"));check(current() instanceof ReskinAppearanceScreen,"appearance reachable");
   layout(current());click(current(),nav(current(),"Panels"));check(theme.shelfEnabled.get(),"shared panels open");layout(current());render(current(),false,null);
   click(current(),nav(current(),"Panels"));check(!theme.shelfEnabled.get(),"shared panels close");
  }
  System.out.println("PASS navigation: ten registered tabs including addon tab, appearance, panel toggles, four layouts at 854x480");
 }
 static void interaction()throws Exception{
  setup(12,40);var theme=new ReskinTheme();Tabs.get().getFirst().openScreen(theme);var s=current();layout(s);var w=workspace(s);
  var card=(ReskinModuleCard)w.content.cells.stream().map(Cell::widget).filter(c->c instanceof ReskinModuleCard).findFirst().orElseThrow();
  int before=card.module.toggles;click(s,card);layout(s);check(card.module.toggles==before,"selecting module must not toggle it");check(w.selected==card.module,"selection updates inspector");
  double x=card.x+card.width-theme.scale(25),y=card.y+card.height/2;s.root.mouseMoved(x,y,x,y);var e=new MouseButtonEvent(x,y,1);s.root.mouseClicked(e,false);s.root.mouseReleased(e);check(card.module.toggles==before+1,"toggle area calls native module toggle exactly once");
  w.search.set("no-result-zzzz");w.search.action.run();layout(s);check(w.content.cells.isEmpty(),"no-results search");
  w.search.set("sample value");w.search.action.run();layout(s);check(!w.content.cells.isEmpty(),"search setting title");
  w.search.set("");w.search.action.run();w.setFilter("Favorites");layout(s);check(w.content.cells.stream().filter(c->c.widget() instanceof ReskinModuleCard).allMatch(c->((ReskinModuleCard)c.widget()).module.favorite),"favorite filter");
  w.setFilter("All");layout(s);w.content.mouseMoved(w.content.x+10,w.content.y+10,0,0);w.content.onMouseScrolled(-10000);layout(s);double atBottom=w.content.scroll;check(atBottom>0,"long content scrolls");check(!w.content.onMouseScrolled(-1),"scroll at bottom propagates rather than double-consuming");
  int toggles=Modules.get().getAll().stream().mapToInt(m->m.toggles).sum();s.root.mouseMoved(w.content.x+50,w.content.y-5,0,0);var outside=new MouseButtonEvent(w.content.x+50,w.content.y-5,1);w.content.mouseClicked(outside,false);w.content.mouseReleased(outside);check(Modules.get().getAll().stream().mapToInt(m->m.toggles).sum()==toggles,"clipped rows cannot toggle through header");
  setup(0,0);theme=new ReskinTheme();for(var style:ReskinTheme.StylePreset.values()){theme.stylePreset.set(style);Tabs.get().getFirst().openScreen(theme);layout(current());render(current(),false,null);check(workspace(current()).all.isEmpty(),"empty registry supported");}
  System.out.println("PASS interactions: selection vs toggle, native toggle once, favorites, setting search, empty search, clipping, wheel boundaries and empty registries");
 }
 static void icons()throws Exception{
  ReskinIcons.invalidateCategoryIcons();int[] calls={0};var good=new Category("Example",()->{calls[0]++;return new net.minecraft.world.item.ItemStack(false);});
  check(ReskinIcons.categoryIcon(good)!=null,"provided category item icon");check(ReskinIcons.categoryIcon(good)!=null&&calls[0]==1,"supplier cached");
  check(ReskinIcons.categoryIcon(new Category("Null"))==null,"missing supplier fallback");check(ReskinIcons.categoryIcon(new Category("Empty",()->new net.minecraft.world.item.ItemStack(true)))==null,"empty stack fallback");
  int[] badCalls={0};var bad=new Category("Broken addon",()->{badCalls[0]++;throw new IllegalStateException("test fixture failure");});
  check(ReskinIcons.categoryIcon(bad)==null&&ReskinIcons.categoryIcon(bad)==null&&badCalls[0]==1,"failing supplier isolated and not spammed");
  check(ReskinIcons.initials("Addon Without Icon").equals("AI"),"readable fallback initials");check(!ReskinIcons.initials("🌙 sky").isEmpty(),"Unicode fallback codepoints");check(ReskinIcons.initials("/___-").equals("?"),"separator-only category fallback");
  System.out.println("PASS icons: supplied, absent, empty, failing, cached and Unicode category fallbacks");
 }
 static void dropdown()throws Exception{
  setup(1,1);Utils.width=640;Utils.height=360;var t=new ReskinTheme();var root=new WidgetScreen.Root();root.theme=t;
  String[] options=new String[60];for(int i=0;i<options.length;i++)options[i]="A very long option label number "+i;
  var d=root.add(new ReskinDropdown<>(options,options[0])).widget();d.calculateSize();d.x=Utils.width-d.width-4;d.y=Utils.height-t.bottomInset()-d.height-5;d.calculateWidgetPositions();d.onPressed(1);
  var popup=(WContainer)field(d,"root");check(popup.y<d.y,"dropdown opens above bottom-edge anchor");check(popup.x>=0&&popup.x+popup.width<=Utils.width,"dropdown horizontal bounds");
  for(int i=0;i<100;i++)d.onMouseScrolled(-4);var last=popup.cells.getLast().widget();check(last.y+last.height<=popup.y+popup.height+2,"last dropdown option reachable");
  popup.mouseMoved(last.x+20,last.y+last.height/2,0,0);var e=new MouseButtonEvent(last.x+20,last.y+last.height/2,1);popup.mouseClicked(e,false);popup.mouseReleased(e);check(d.get().equals(options[59]),"last popup value selects correctly");
  var r=new GuiRenderer();r.theme=t;d.render(r,0,0,.02);r.finish();
  System.out.println("PASS dropdown: sixty options, upward edge flip, full scroll reach and final value selection");
 }
 static void wideViews()throws Exception{
  setup(1,1);Utils.width=854;Utils.height=480;var theme=new ReskinTheme();var root=new WidgetScreen.Root();root.theme=theme;
  var v=root.add(new ReskinView()).top().widget();v.externallyConstrainedMaxWidth=230;v.externallyConstrainedMaxHeight=180;
  final int[] clickedCount={0};for(int i=0;i<30;i++)v.add(new WPressable(){protected void onCalculateSize(){width=700;height=32;}protected void onPressed(int button){clickedCount[0]++;}});
  v.calculateSize();v.x=50;v.y=100;v.calculateWidgetPositions();
  check((boolean)field(v,"horizontal")&&(boolean)field(v,"vertical"),"wide native-content view exposes both scroll axes");
  v.mouseMoved(v.x+20,v.y+20,0,0);v.onMouseScrolled(-10000);v.calculateWidgetPositions();
  var last=v.cells.getLast().widget();double ih=(double)field(v,"innerH");check(last.y+last.height<=v.y+ih+1,"last wide input vertically reachable");
  v.mouseMoved(v.x+100,v.y+ih+8,0,0);v.onMouseScrolled(-10000);v.calculateWidgetPositions();
  check(last.x+last.width<=v.x+(double)field(v,"innerW")+1,"rightmost content horizontally reachable");
  var track=new MouseButtonEvent(v.x+80,v.y+ih+8,1);v.mouseClicked(track,false);v.mouseReleased(track);check(clickedCount[0]==0,"horizontal scrollbar never activates a covered input");
  var first=v.cells.getFirst().widget();first.setFocused(true);GuiRenderer r=new GuiRenderer();r.theme=theme;v.render(r,0,0,.02);r.finish();v.calculateWidgetPositions();check(first.y>=v.y-1,"new keyboard focus reveals hidden top field");
  System.out.println("PASS native-content viewport: both axes, last control reachability, scrollbar isolation and focus reveal");
 }
 static void tooltips()throws Exception{
  setup(0,0);Utils.width=640;Utils.height=360;var theme=new ReskinTheme();var tip=theme.tooltip("AnExtremelyLongUnbrokenIdentifier".repeat(40)+"\n"+"Another paragraph ".repeat(20));tip.init();
  check(tip.width<=Utils.width&&tip.height<=Utils.height,"large tooltip bounded before first placement");var r=new GuiRenderer();r.theme=theme;tip.render(r,0,0,.02);r.finish();
  theme.reducedMotion.set(true);tip.render(r,0,0,100);r.finish();
  System.out.println("PASS tooltip: long unbroken identifiers, multiline text, bounded first placement and reduced motion");
 }
 static void previews()throws Exception{
  setup(12,35);Utils.width=1666;Utils.height=896;Window.guiScale=2;Files.createDirectories(Path.of("test-results/previews"));
  for(var style:ReskinTheme.StylePreset.values()){var theme=new ReskinTheme();theme.stylePreset.set(style);theme.applyStylePreset(style);Tabs.get().getFirst().openScreen(theme);layout(current());render(current(),true,"test-results/previews/"+style.name().toLowerCase()+".png");}
  System.out.println("PASS software preview rendering: four PNGs generated from production widget code with substitute fonts/drawing and fixture data");
 }
 public static void main(String[] args)throws Exception{
  setup(12,35);var theme=new ReskinTheme();check(theme.name.equals("Meteor Reskin"),"production theme registration name");System.out.println("PASS production theme constructor and real Meteor setting builders");
  for(var style:ReskinTheme.StylePreset.values()){
   theme.stylePreset.set(style);theme.applyStylePreset(style);Tabs.get().getFirst().openScreen(theme);var screen=(WidgetScreen)MeteorClient.mc.gui.screen();layout(screen);render(screen,false,null);System.out.println("PASS construction and render "+style);
  }
  theme.accentColor.get().set(17,39,201,255);theme.surfaceColor.get().set(11,22,33,229);theme.density.set(1.17);var data=theme.settings.toTag();var restored=new ReskinTheme();restored.settings.fromTag(data);check(restored.style()==theme.style(),"selected layout roundtrip");check(restored.accentColor.get().r==17&&restored.accentColor.get().b==201,"custom accent survives loading");check(restored.surfaceColor.get().a==229,"opacity survives loading");check(restored.density.get()==1.17,"density survives loading");System.out.println("PASS real Meteor Settings serialization roundtrip using in-memory NBT fixtures");
  matrix();navigation();interaction();icons();dropdown();wideViews();tooltips();previews();
  System.out.println("PASS TOTAL CHECKS: "+checks);
 }
}
