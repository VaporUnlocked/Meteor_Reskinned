package meteordevelopment.meteorclient.gui.themes.meteor;
import meteordevelopment.meteorclient.gui.GuiTheme;import meteordevelopment.meteorclient.settings.*;import meteordevelopment.meteorclient.utils.render.color.SettingColor;
public class MeteorGuiTheme extends GuiTheme {
 public final Setting<Double> scale=new DoubleSetting.Builder().name("scale").defaultValue(1.0).min(.01).build();
 @Override public double scale(double v){return v*scale.get();}
 public class ThreeStateColorSetting {private final SettingColor normal=new SettingColor(30,40,40,255),hover=new SettingColor(60,70,70,255),pressed=new SettingColor(80,90,90,255);public SettingColor get(){return normal;}public SettingColor get(boolean p,boolean h){return p?pressed:h?hover:normal;}public SettingColor get(boolean p,boolean h,boolean x){return get(p,h);}}
 public ThreeStateColorSetting backgroundColor=new ThreeStateColorSetting(),outlineColor=new ThreeStateColorSetting(),scrollbarColor=new ThreeStateColorSetting(),sliderHandle=new ThreeStateColorSetting();
 public Setting<SettingColor> textColor=c("text"),titleTextColor=c("title"),accentColor=c("accent"),moduleBackground=c("module-background"),textSecondaryColor=c("text-secondary"),checkboxColor=c("checkbox"),separatorCenter=c("separator-center"),separatorEdges=c("separator-edges"),sliderLeft=c("slider-left"),sliderRight=c("slider-right");
 private Setting<SettingColor> c(String name){return settings.getDefaultGroup().add(new ColorSetting.Builder().name(name).defaultValue(new SettingColor(121,223,181,255)).build());}
}
