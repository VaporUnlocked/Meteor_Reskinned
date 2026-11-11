// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Category;

final class ReskinCategoryButton extends WPressable {
    final Category category;final String label;private final Runnable clicked;
    private final IntSupplier count;private final BooleanSupplier selected;private final ReskinText text=new ReskinText();
    ReskinCategoryButton(Category category,String label,IntSupplier count,BooleanSupplier selected,Runnable clicked){
        this.category=category;this.label=label;this.count=count;this.selected=selected;this.clicked=clicked;tooltip=label;
    }
    @Override protected void onCalculateSize(){width=theme.scale(180);height=theme.scale(42);}
    @Override protected void onPressed(int button){if(button==1)clicked.run();}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        ReskinTheme t=(ReskinTheme)theme;boolean chosen=selected.getAsBoolean();
        if(chosen||mouseOver)ReskinDraw.panel(r,t,x,y,width,height,chosen);
        ReskinIcons.draw(r,t,category,null,label,x+t.scale(6),y+t.scale(7),t.scale(27));
        text.draw(r,t,label,x+t.scale(42),y+(height-t.textHeight())/2,Math.max(1,width-t.scale(81)),t.textHeight(),false,chosen?t.accentColor.get():t.textColor.get(),mouseOver,dt);
        String n=String.valueOf(count.getAsInt());
        ReskinDraw.text(r,t,n,x+width-t.scale(31),y+(height-t.textHeight())/2,t.scale(29),t.textSecondaryColor.get(),false);
    }
}
