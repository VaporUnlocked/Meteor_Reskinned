// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.input.MouseButtonEvent;

/** Two-axis viewport for native settings. Scrollbars occupy their own area, never cover inputs. */
public final class ReskinView extends WView {
    public double externallyConstrainedMaxHeight=-1,externallyConstrainedMaxWidth=-1;
    private double totalWidth,totalHeight,sx,sy,innerW,innerH;
    private double mx,my,dragStart,scrollStart;
    private boolean horizontal,vertical,dragX,dragY;
    private WWidget lastFocus;
    @Override public void init(){maxHeight=Utils.getWindowHeight();}
    @Override protected void onCalculateSize(){
        totalWidth=0;totalHeight=0;int n=0;
        for(Cell<?> c:cells){if(!c.widget().visible)continue;
            totalWidth=Math.max(totalWidth,c.padLeft()+c.widget().width+c.padRight());
            totalHeight+=c.padTop()+c.widget().height+c.padBottom();if(n++>0)totalHeight+=spacing();
        }
        double safeH=Math.max(1,Utils.getWindowHeight()-((ReskinTheme)theme).topInset()-((ReskinTheme)theme).bottomInset()-theme.scale(24));
        double safeW=Math.max(1,Utils.getWindowWidth()-((ReskinTheme)theme).sideInset()-theme.scale(40));
        double capH=externallyConstrainedMaxHeight<0?Math.min(maxHeight,safeH):Math.min(safeH,externallyConstrainedMaxHeight);
        double capW=externallyConstrainedMaxWidth<0?safeW:Math.min(safeW,externallyConstrainedMaxWidth);
        horizontal=totalWidth>capW;vertical=totalHeight>Math.max(1,capH-(horizontal?theme.scale(18):0));
        width=Math.max(1,Math.min(capW,totalWidth+(vertical?theme.scale(12):0)));
        height=Math.max(1,Math.min(capH,totalHeight+(horizontal?theme.scale(18):0)));
        area();
    }
    private void area(){
        double track=theme.scale(12);
        horizontal=totalWidth>width-(vertical?track:0)+.5;
        vertical=totalHeight>height-(horizontal?theme.scale(18):0)+.5;
        innerW=Math.max(1,width-(vertical?track:0));innerH=Math.max(1,height-(horizontal?theme.scale(18):0));
        horizontal=totalWidth>innerW+.5;
        sx=Math.max(0,Math.min(sx,Math.max(0,totalWidth-innerW)));sy=Math.max(0,Math.min(sy,Math.max(0,totalHeight-innerH)));
    }
    @Override protected void onCalculateWidgetPositions(){
        area();double yy=y-sy;int n=0;
        double rowW=Math.max(innerW,totalWidth);
        for(Cell<?> c:cells){if(!c.widget().visible)continue;
            if(n++>0)yy+=spacing();yy+=c.padTop();
            c.x=x+c.padLeft()-sx;c.y=yy;c.width=Math.max(1,rowW-c.padLeft()-c.padRight());c.height=c.widget().height;c.alignWidget();
            yy+=c.widget().height+c.padBottom();
        }
    }
    @Override public boolean isWidgetInView(WWidget w){return w.visible&&w.y<y+innerH&&w.y+w.height>y&&w.x<x+innerW&&w.x+w.width>x;}
    @Override protected boolean propagateEvents(WWidget w){return w.visible&&(w.isFocused()||ReskinDraw.hit(mx,my,x,y,innerW,innerH)&&isWidgetInView(w));}
    @Override public void mouseMoved(double px,double py,double ox,double oy){
        mx=px;my=py;mouseOver=isOver(px,py);
        if(dragX){sx=scrollStart+(px-dragStart)*(Math.max(1,totalWidth-innerW)/Math.max(1,innerW-thumbX()));clampScroll();}
        if(dragY){sy=scrollStart+(py-dragStart)*(Math.max(1,totalHeight-innerH)/Math.max(1,innerH-thumbY()));clampScroll();}
        super.mouseMoved(px,py,ox,oy);
        if(!ReskinDraw.hit(px,py,x,y,innerW,innerH))clearHovers(this);
    }
    // WView's private one-axis handlers are intentionally not used.
    @Override public void onMouseMoved(double px,double py,double ox,double oy){}
    @Override public boolean onMouseClicked(MouseButtonEvent event,boolean twice){return false;}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
        if(!isOver(e.x(),e.y()))return false;
        if(e.button()==1){
            if(horizontal&&e.y()>=y+innerH){
                sx=(e.x()-x-thumbX()/2)/Math.max(1,innerW-thumbX())*Math.max(0,totalWidth-innerW);clampScroll();
                dragX=true;dragStart=e.x();scrollStart=sx;setFocused(true);return true;
            }
            if(vertical&&e.x()>=x+innerW){
                sy=(e.y()-y-thumbY()/2)/Math.max(1,innerH-thumbY())*Math.max(0,totalHeight-innerH);clampScroll();
                dragY=true;dragStart=e.y();scrollStart=sy;setFocused(true);return true;
            }
        }
        return super.mouseClicked(e,twice);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){
        if(dragX||dragY){dragX=dragY=false;setFocused(false);return true;}return super.mouseReleased(e);
    }
    @Override public boolean onMouseReleased(MouseButtonEvent event){return false;}
    @Override public boolean onMouseScrolled(double amount){
        if(scrollOnlyWhenMouseOver&&!isOver(mx,my))return false;
        double before;
        if(horizontal&&my>=y+innerH){before=sx;sx-=amount*theme.scale(48);clampScroll();return sx!=before;}
        before=sy;sy-=amount*theme.scale(48);clampScroll();return sy!=before;
    }
    private void clampScroll(){area();invalidate();}
    private double thumbX(){return Math.min(innerW,Math.max(theme.scale(26),innerW*innerW/Math.max(1,totalWidth)));}
    private double thumbY(){return Math.min(innerH,Math.max(theme.scale(26),innerH*innerH/Math.max(1,totalHeight)));}
    private static void clearHovers(WContainer c){for(Cell<?> cell:c.cells){WWidget w=cell.widget();w.mouseOver=false;if(w instanceof WContainer wc)clearHovers(wc);}}
    private WWidget focusedLeaf(WContainer c){for(Cell<?> cell:c.cells){WWidget w=cell.widget();if(w instanceof WContainer cc){WWidget f=focusedLeaf(cc);if(f!=null)return f;}else if(w.isFocused())return w;}return null;}
    private void revealFocus(){
        WWidget w=focusedLeaf(this);if(w==null){lastFocus=null;return;}if(w==lastFocus)return;lastFocus=w;
        double oldX=sx,oldY=sy;
        if(w.y<y)sy-=y-w.y;else if(w.y+w.height>y+innerH)sy+=w.y+w.height-y-innerH;
        if(w.x<x)sx-=x-w.x;else if(w.x+w.width>x+innerW)sx+=w.x+w.width-x-innerW;
        area();if(oldX!=sx||oldY!=sy)invalidate();
    }
    @Override public boolean render(GuiRenderer r,double px,double py,double dt){
        if(!visible)return true;revealFocus();
        r.scissorStart(x,y,innerW,innerH);
        for(Cell<?> c:cells)if(isWidgetInView(c.widget())){
            if(!ReskinDraw.hit(px,py,x,y,innerW,innerH))c.widget().mouseOver=false;
            c.widget().render(r,px,py,dt);
        }
        r.scissorEnd();
        ReskinTheme t=(ReskinTheme)theme;
        if(horizontal){
            double yy=y+innerH+theme.scale(5),len=thumbX();
            ReskinDraw.round(r,x,yy,innerW,theme.scale(5),theme.scale(2),t.border());
            ReskinDraw.round(r,x+(innerW-len)*sx/Math.max(1,totalWidth-innerW),yy,len,theme.scale(5),theme.scale(2),t.accentColor.get());
        }
        if(vertical&&hasScrollBar){
            double xx=x+innerW+theme.scale(4),len=thumbY();
            ReskinDraw.round(r,xx,y,theme.scale(4),innerH,theme.scale(2),t.border());
            ReskinDraw.round(r,xx,y+(innerH-len)*sy/Math.max(1,totalHeight-innerH),theme.scale(4),len,theme.scale(2),t.accentColor.get());
        }
        return false;
    }
}
