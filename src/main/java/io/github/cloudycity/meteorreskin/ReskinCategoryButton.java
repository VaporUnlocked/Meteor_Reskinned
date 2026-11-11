// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Category;

/** Measured count badges; long addon names wrap before resorting to hover reveal. */
final class ReskinCategoryButton extends WPressable implements ReskinReflow.WidthAware {
    final Category category;final String label;private final Runnable clicked;
    private final IntSupplier count;private final BooleanSupplier selected;private final ReskinText[] texts={new ReskinText(),new ReskinText()};
    boolean listOnly;
    private double bound=240,countWidth,labelWidth;private List<String> lines=List.of();
    ReskinCategoryButton(Category c,String label,IntSupplier count,BooleanSupplier selected,Runnable click){category=c;this.label=label;this.count=count;this.selected=selected;clicked=click;tooltip=label;}
    boolean tile(){return !listOnly&&((ReskinTheme)theme).style()==ReskinTheme.StylePreset.NEON_DOCK;}
    @Override public void availableWidth(double w){bound=w;}
    @Override protected void onCalculateSize(){
        width=bound;countWidth=Math.ceil(theme.textWidth(""+count.getAsInt()))+theme.scale(14)+2;
        labelWidth=Math.max(1,width-theme.scale(tile()?16:53)-(tile()?0:countWidth));
        // Keep names intact. Overflow uses one-line hover reveal, never "Moveme / nt".
        lines=List.of(label);
        height=tile()?Math.max(((ReskinTheme)theme).composition(108),theme.scale(66)+Math.min(2,lines.size())*(theme.textHeight()+theme.scale(3))):Math.max(((ReskinTheme)theme).composition(44),theme.scale(22)+Math.min(2,lines.size())*(theme.textHeight()+theme.scale(2)));
    }
    @Override protected void onPressed(int b){if(b==1)clicked.run();}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        ReskinTheme t=(ReskinTheme)theme;boolean chosen=selected.getAsBoolean();
        if(chosen||mouseOver||tile())ReskinDraw.panel(r,t,x,y,width,height,chosen);
        if(chosen&&!tile())r.quad(x,y+t.scale(8),t.scale(2),height-t.scale(16),t.accentColor.get());
        double sx=tile()?x+(width-t.scale(26))/2:x+t.scale(9),sy=tile()?y+t.scale(12):y+(height-t.scale(26))/2;
        if(category==null)ReskinGlyph.draw(r,label,sx,sy,t.scale(26),chosen?t.accentColor.get():t.textColor.get());else ReskinIcons.draw(r,t,category,null,label,sx,sy,t.scale(26));
        double tx=tile()?x+t.scale(8):x+t.scale(46),ty=tile()?y+t.scale(45):y+(height-Math.min(2,lines.size())*(t.textHeight()+t.scale(2)))/2;
        for(int i=0;i<Math.min(2,lines.size());i++){
            String line=lines.get(i);if(i==1&&lines.size()>2)line=String.join(" ",lines.subList(1,lines.size()));
            texts[i].draw(r,t,line,tx,ty+i*(t.textHeight()+t.scale(2)),labelWidth,t.textHeight(),false,chosen?t.titleTextColor.get():t.textColor.get(),mouseOver,dt);
        }
        String n=""+count.getAsInt();double nx=tile()?x+(width-countWidth)/2:x+width-countWidth-t.scale(7),ny=tile()?y+height-t.scale(26):y+(height-t.textHeight())/2-t.scale(3);
        ReskinDraw.round(r,nx,ny,countWidth,t.textHeight()+t.scale(6),0,ReskinDraw.alpha(t.border(),60));
        r.text(n,nx+(countWidth-t.textWidth(n))/2,ny+t.scale(3),t.textSecondaryColor.get(),false);
    }
}
