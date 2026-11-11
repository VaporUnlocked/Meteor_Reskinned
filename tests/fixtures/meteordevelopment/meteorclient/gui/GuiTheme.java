package meteordevelopment.meteorclient.gui;
import meteordevelopment.meteorclient.gui.widgets.*;import meteordevelopment.meteorclient.gui.widgets.containers.*;import meteordevelopment.meteorclient.gui.widgets.pressable.*;import meteordevelopment.meteorclient.gui.widgets.input.*;import meteordevelopment.meteorclient.gui.utils.*;import meteordevelopment.meteorclient.gui.tabs.TabScreen;import meteordevelopment.meteorclient.utils.render.color.Color;import meteordevelopment.meteorclient.systems.modules.Module;import meteordevelopment.meteorclient.settings.*;import io.github.cloudycity.meteorreskin.*;import net.minecraft.client.gui.screens.Screen;
public class GuiTheme {
 public final String name;public GuiTheme(){name=new String("Meteor");}public final Settings settings=new Settings();public SettingsWidgetFactory settingsFactory;public boolean disableHoverColor;
 public double scale(double v){return v;}public double pad(){return scale(6);}public void beforeRender(){disableHoverColor=false;}
 public double textHeight(){return scale(15);}public double textHeight(boolean t){return textHeight()*(t?1.25:1);}
 public double textWidth(String s){return textWidth(s,s.length(),false);}public double textWidth(String s,int n,boolean t){return meteordevelopment.meteorclient.gui.renderer.GuiRenderer.measure(s.substring(0,n),scale(15)*(t?1.25:1));}
 public <W extends WWidget> W w(W w){w.theme=this;return w;}
 public WWindow window(String s){return window(null,s);}public WWindow window(WWidget i,String s){return w(new ReskinWindow(i,s));}
 public WView view(){return w(new ReskinView());}public WLabel label(String s){return label(s,false,0);}public WLabel label(String s,boolean t){return label(s,t,0);}public WLabel label(String s,double width){return label(s,false,width);}
 public WLabel label(String s,boolean t,double max){if(max>0||s.contains("\n"))return w(new Wrapped(s,t,max));return w(new ReskinLabel(s,t));}
 public WButton button(String s){return w(new ReskinButton(s,null));}public WTriangle triangle(){return w(new WTriangle(){});}
 public WWidget module(Module m,String s){return w(new ReskinModule(m,s));}public WWidget module(Module m){return module(m,m.title);}
 public WHorizontalList horizontalList(){return w(new WHorizontalList());}public WVerticalList verticalList(){return w(new WVerticalList());}public WTable table(){return w(new WTable());}
 public WTextBox textBox(String s){return w(new WTextBox(s));}public WTextBox textBox(String s,String p){return textBox(s);}
 public WCheckbox checkbox(boolean b){return w(new ReskinCheckbox(b));}public WSlider slider(double v,double lo,double hi){return w(new WSlider(v,lo,hi){protected void onRender(meteordevelopment.meteorclient.gui.renderer.GuiRenderer r,double x,double y,double d){r.quad(this.x,this.y+height/2,width,3,new Color(80,120,110));}});}
 public WQuad quad(Color c){return w(new WQuad(c){protected void onRender(meteordevelopment.meteorclient.gui.renderer.GuiRenderer r,double x,double y,double d){r.quad(this,color);}});}
 public WHorizontalSeparator horizontalSeparator(String s){return w(new WHorizontalSeparator(s){});}public WHorizontalSeparator horizontalSeparator(){return horizontalSeparator(null);}
 public WTooltip tooltip(String s){return w(new ReskinTooltip(s));}
 public WTopBar topBar(){return w(new ReskinTopBar());}public WindowConfig getWindowConfig(String s){return new WindowConfig();}
 public boolean isModulesScreen(Screen s){return s instanceof ReskinModulesScreen;}public TabScreen modulesScreen(){return new ReskinModulesScreen(this);}
 public WidgetScreen moduleScreen(Module m){return new WidgetScreen(this){public void initWidgets(){add(label("Full settings: "+m.title));}};}
 public WWidget settings(Settings s){return settings(s,"");}public WWidget settings(Settings s,String f){return settingsFactory.create(this,s,f);}
 private static class Wrapped extends WLabel {final double cap;java.util.List<String> lines=new java.util.ArrayList<>();Wrapped(String s,boolean title,double cap){super(s,title);this.cap=cap;}protected void onCalculateSize(){double limit=cap>0?cap:theme.scale(250);lines.clear();String line="";for(String word:text.split("\s+")){if(!line.isEmpty()&&theme.textWidth(line+" "+word)>limit){lines.add(line);line=word;}else line+=(line.isEmpty()?"":" ")+word;}if(!line.isEmpty())lines.add(line);width=limit;height=lines.size()*theme.textHeight();}protected void onRender(meteordevelopment.meteorclient.gui.renderer.GuiRenderer r,double mx,double my,double dt){int i=0;for(String line:lines)r.text(line,x,y+i++*theme.textHeight(),new Color(165,180,195),false);}}
}
