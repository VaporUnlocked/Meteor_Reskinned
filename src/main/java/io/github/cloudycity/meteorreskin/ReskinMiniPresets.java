// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
/** Compact functional style browser for Prism, drawn without external assets. */
final class ReskinMiniPresets extends WContainer implements ReskinReflow.WidthAware {
    double bound=280;
    @Override public void availableWidth(double width){bound=width;}
    @Override public void init(){for(var style:ReskinTheme.StylePreset.values())add(new Tile(style)).top().expandWidgetX();}
    @Override protected void onCalculateSize(){width=bound;height=theme.scale(400);}
    @Override protected void onCalculateWidgetPositions(){double gap=theme.scale(8),cw=(width-gap)/2;for(int i=0;i<cells.size();i++){var c=cells.get(i);c.x=x+(i%2)*(cw+gap);c.y=y+(i/2)*theme.scale(100);c.width=cw;c.height=theme.scale(92);c.alignWidget();}}
    final class Tile extends WPressable {
        final ReskinTheme.StylePreset style;Tile(ReskinTheme.StylePreset s){style=s;tooltip=s.title;}
        @Override protected void onCalculateSize(){width=theme.scale(130);height=theme.scale(92);}
        @Override protected void onPressed(int b){if(b==1)ReskinAccess.later(()->((ReskinTheme)theme).chooseStyle(style));}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){var t=(ReskinTheme)theme;var accent=ReskinStyleCard.accent(style);ReskinDraw.panel(r,t,x,y,width,height,t.style()==style);double yy=y+theme.scale(8),ww=width-theme.scale(16);
            r.quad(x+theme.scale(8),yy,ww,theme.scale(40),ReskinDraw.alpha(accent,110),ReskinDraw.alpha(t.surface(),150));
            ReskinGlyph.draw(r,style==ReskinTheme.StylePreset.PRISM_STUDIO?"logo":"grid",x+width/2-theme.scale(13),yy+theme.scale(7),theme.scale(26),accent);
            String shortName=style.title.split(" ")[0];ReskinDraw.text(r,t,shortName,x+theme.scale(10),y+theme.scale(63),width-theme.scale(20),t.textColor.get(),false);
        }
    }
}
