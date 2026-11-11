package meteordevelopment.meteorclient.gui.tabs;
import meteordevelopment.meteorclient.gui.*;import meteordevelopment.meteorclient.gui.widgets.*;import meteordevelopment.meteorclient.gui.utils.*;
public abstract class TabScreen extends WidgetScreen {public final Tab tab;public TabScreen(GuiTheme theme,Tab tab){super(theme);this.tab=tab;}public <T extends WWidget> Cell<T> addDirect(T w){return super.add(w);}}
