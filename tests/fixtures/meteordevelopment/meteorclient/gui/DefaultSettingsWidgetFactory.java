package meteordevelopment.meteorclient.gui;
import meteordevelopment.meteorclient.settings.*;import meteordevelopment.meteorclient.gui.widgets.*;import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
/** Test double for data-to-widget conversion only. Native data Settings/Setting bytecode is real. */
public class DefaultSettingsWidgetFactory extends SettingsWidgetFactory {
 public DefaultSettingsWidgetFactory(GuiTheme t){}
 public WWidget create(GuiTheme t,Settings settings,String filter){var list=t.verticalList();for(var g:settings)for(var s:g){if(!s.isVisible())continue;list.add(t.label(s.title));if(s instanceof BoolSetting b){var cb=list.add(t.checkbox(b.get())).widget();cb.action=()->b.set(cb.checked);}else if(s instanceof DoubleSetting d)list.add(t.slider(d.get(),0,100)).expandX();else if(s instanceof IntSetting i)list.add(t.slider(i.get(),0,100)).expandX();else list.add(t.textBox(String.valueOf(s.get()))).expandX();}return list;}
}
