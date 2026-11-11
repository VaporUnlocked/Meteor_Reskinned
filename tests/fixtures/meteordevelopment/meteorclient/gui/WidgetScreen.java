package meteordevelopment.meteorclient.gui;
import meteordevelopment.meteorclient.gui.widgets.*;import meteordevelopment.meteorclient.gui.widgets.containers.*;import meteordevelopment.meteorclient.gui.utils.*;
public abstract class WidgetScreen extends net.minecraft.client.gui.screens.Screen {
 public final Root root=new Root();protected final GuiTheme theme;public Runnable taskAfterRender;
 public WidgetScreen(GuiTheme t){theme=t;root.theme=t;}
 public <T extends WWidget> Cell<T> add(T w){return root.add(w);}public void clear(){root.clear();}public void invalidate(){root.invalidate();}public void reload(){clear();initWidgets();}public boolean toClipboard(){return false;}public boolean fromClipboard(){return false;}public abstract void initWidgets();protected void onClosed(){}
 public static class Root extends WContainer implements WRoot {public boolean invalid;public void invalidate(){invalid=true;}protected void onCalculateSize(){width=meteordevelopment.meteorclient.utils.Utils.getWindowWidth();height=meteordevelopment.meteorclient.utils.Utils.getWindowHeight();}protected void onCalculateWidgetPositions(){for(Cell<?> c:cells){c.x=0;c.y=0;c.width=width;c.height=height;c.alignWidget();}}}
}
