// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import net.minecraft.client.gui.screens.Screen;

/** Isolates the five Minecraft-facing API calls; uses the 26.3 Gui screen manager. */
final class ReskinAccess {
    private ReskinAccess(){}
    static Screen screen(){
        try{Object gui=MeteorClient.mc.getClass().getField("gui").get(MeteorClient.mc);
            return (Screen)gui.getClass().getMethod("screen").invoke(gui);
        }catch(ReflectiveOperationException|RuntimeException e){return null;}
    }
    static void open(Screen next){
        try{Object gui=MeteorClient.mc.getClass().getField("gui").get(MeteorClient.mc);
            gui.getClass().getMethod("setScreen",Screen.class).invoke(gui,next);
        }catch(ReflectiveOperationException|RuntimeException e){throw new IllegalStateException("Meteor Reskin could not open a screen",e);}
    }
    static void later(Runnable action){
        Screen current=screen();
        if(current instanceof WidgetScreen ws){
            Runnable earlier=ws.taskAfterRender;
            ws.taskAfterRender=()->{if(earlier!=null)earlier.run();action.run();};
        }else action.run();
    }
    static void reopenCurrent(ReskinTheme theme){
        Screen current=screen();
        if(current instanceof TabScreen ts)later(()->ts.tab.openScreen(theme));
        else if(current instanceof WidgetScreen ws)later(ws::reload);
    }
}
