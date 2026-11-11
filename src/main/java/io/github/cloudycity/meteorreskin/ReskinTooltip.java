// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;

/** Measured before first placement; wraps identifiers too, and never creates a screen-sized tooltip. */
public final class ReskinTooltip extends WTooltip {
    private final List<String> lines=new ArrayList<>();
    private double elapsed,totalHeight;
    public ReskinTooltip(String text){super(text==null?"":text);}
    @Override public void init(){calculateSize();calculateWidgetPositions();}
    @Override protected void onCalculateSize(){
        ReskinTheme t=(ReskinTheme)theme;double pad=t.scale(12),cap=Math.max(1,Math.min(t.scale(420),t.logicalWidth()-pad*2));
        lines.clear();
        for(String paragraph:text.split("\\n",-1)){
            String remaining=paragraph;
            if(remaining.isEmpty()){lines.add("");continue;}
            while(!remaining.isEmpty()){
                int end=0,lastSpace=-1;
                while(end<remaining.length()){
                    int next=remaining.offsetByCodePoints(end,1);
                    if(end>0&&t.textWidth(remaining,next,false)>cap-pad*2)break;
                    if(Character.isWhitespace(remaining.codePointAt(end)))lastSpace=end;
                    end=next;
                }
                if(end<remaining.length()&&lastSpace>0)end=lastSpace;
                if(end==0)end=remaining.offsetByCodePoints(0,1);
                lines.add(remaining.substring(0,end));remaining=remaining.substring(end).stripLeading();
            }
        }
        double widest=1;for(String line:lines)widest=Math.max(widest,t.textWidth(line));
        width=Math.min(cap,widest+pad*2);totalHeight=lines.size()*(t.textHeight()+t.scale(3));
        height=Math.min(Math.max(1,t.logicalHeight()-pad*2),totalHeight+pad*2);
    }
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        ReskinTheme t=(ReskinTheme)theme;double pad=t.scale(12);elapsed+=Math.max(0,dt);
        ReskinDraw.panel(r,t,x,y,width,height,false);
        double inner=Math.max(1,height-pad*2),overflow=Math.max(0,totalHeight-inner),offset=0;
        if(overflow>0&&!t.reducedMotion.get()){
            double travel=overflow/Math.max(1,t.scale(18));double phase=elapsed%(travel+4);
            offset=Math.min(overflow,Math.max(0,phase-2)*t.scale(18));
        }
        r.scissorStart(x+pad,y+pad,Math.max(1,width-pad*2),inner);
        double yy=y+pad-offset;
        for(String line:lines){if(yy+t.textHeight()>=y+pad&&yy<y+height-pad)r.text(line,x+pad,yy,t.textColor.get(),false);yy+=t.textHeight()+t.scale(3);}
        r.scissorEnd();
    }
}
