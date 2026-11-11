package net.minecraft.client.gui;
import net.minecraft.client.gui.screens.Screen;
public class Gui {private Screen current; public Screen screen(){return current;} public void setScreen(Screen s){ current=s; if(s instanceof meteordevelopment.meteorclient.gui.WidgetScreen w)w.initWidgets(); }}
