// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import java.util.function.BooleanSupplier;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
final class ReskinGroupHeading extends WPressable implements ReskinScrollPane.FullRow {
    meteordevelopment.meteorclient.systems.modules.Category category;
    final String title;final int count;final BooleanSupplier expanded;final Runnable toggle;
    ReskinGroupHeading(String title,int count,BooleanSupplier expanded,Runnable toggle){this.title=title;this.count=count;this.expanded=expanded;this.toggle=toggle;}
    ReskinGroupHeading(meteordevelopment.meteorclient.systems.modules.Category category,int count,BooleanSupplier expanded,Runnable toggle){this(category.name,count,expanded,toggle);this.category=category;}
    @Override protected void onCalculateSize(){width=theme.scale(300);height=theme.scale(44);}
    @Override protected void onPressed(int b){if(b==1)toggle.run();}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){var t=(ReskinTheme)theme;ReskinDraw.round(r,x,y,width,height,0,t.headerSurfaceColor.get());if(category!=null)ReskinIcons.draw(r,t,category,null,title,x+t.scale(12),y+(height-t.scale(25))/2,t.scale(25));ReskinDraw.text(r,t,title+"  ·  "+count,x+t.scale(category==null?15:48),y+t.scale(13),width-t.scale(category==null?54:87),t.titleTextColor.get(),false);r.text(expanded.getAsBoolean()?"−":"+",x+width-t.scale(25),y+t.scale(13),t.accentColor.get(),false);}
}
