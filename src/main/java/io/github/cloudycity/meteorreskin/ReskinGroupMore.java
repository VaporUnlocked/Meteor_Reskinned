// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
/** Explicit access to every item; the grouped overview never silently drops modules. */
final class ReskinGroupMore extends WPressable implements ReskinScrollPane.FullRow {
    final String label;final Runnable click;ReskinGroupMore(String label,Runnable click){this.label=label;this.click=click;tooltip=label;}
    @Override protected void onCalculateSize(){width=theme.scale(200);height=theme.scale(32);}
    @Override protected void onPressed(int b){if(b==1)click.run();}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){var t=(ReskinTheme)theme;if(mouseOver)r.quad(x,y,width,height,ReskinDraw.alpha(t.headerSurfaceColor.get(),150));ReskinDraw.text(r,t,label,x+t.scale(15),y+t.scale(8),width-t.scale(30),t.accentColor.get(),false);}
}
