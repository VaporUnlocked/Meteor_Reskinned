// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import net.minecraft.client.input.MouseButtonEvent;

/** Fixed viewport, reflowing rows/cards, visible scrollbar, and clipped pointer routing. */
final class ReskinScrollPane extends WContainer {
    double viewportWidth=200,viewportHeight=400,scroll,contentHeight,gap=6;
    int columns=1;
    boolean background=true,dragging;
    private double lastMouseY;
    interface FullRow {}
    void bounds(double w,double h){viewportWidth=Math.max(1,w);viewportHeight=Math.max(1,h);}
    @Override protected void onCalculateSize(){
        width=viewportWidth;height=viewportHeight;
        contentHeight=arrange(false);
        scroll=Math.max(0,Math.min(scroll,Math.max(0,contentHeight-height)));
    }
    private double arrange(boolean place){
        double pad=theme.scale(8),g=theme.scale(gap),yy=pad,rowHeight=0;
        int count=0,cols=Math.max(1,columns);
        double usable=Math.max(1,width-2*pad-theme.scale(8));
        double cw=Math.max(1,(usable-g*(cols-1))/cols);
        for(Cell<?> c:cells){
            WWidget w=c.widget();if(!w.visible)continue;
            boolean full=w instanceof FullRow;
            if(full&&count!=0){yy+=rowHeight+g;count=0;rowHeight=0;}
            if(place){c.x=x+pad+count*(cw+g);c.y=y+yy-scroll;c.width=full?usable:cw;c.height=w.height;c.expandWidgetX();c.alignWidget();}
            rowHeight=Math.max(rowHeight,w.height);count++;
            if(full||count==cols){yy+=rowHeight+g;count=0;rowHeight=0;}
        }
        if(count>0)yy+=rowHeight+g;
        return Math.max(0,yy+pad-g);
    }
    @Override protected void onCalculateWidgetPositions(){arrange(true);}
    @Override protected boolean propagateEvents(WWidget w){
        return w.visible && (w.isFocused() || mouseOver && w.y+w.height>y && w.y<y+height && w.x+w.width>x && w.x<x+width-theme.scale(8));
    }
    @Override public void mouseMoved(double mx,double my,double ox,double oy){
        mouseOver=isOver(mx,my);
        if(dragging){
            double thumb=thumbHeight(),travel=Math.max(1,height-theme.scale(8)-thumb);
            setScroll(scroll+(my-lastMouseY)/travel*Math.max(0,contentHeight-height));
        }
        lastMouseY=my;super.mouseMoved(mx,my,ox,oy);
        if(!mouseOver)for(Cell<?> c:cells)c.widget().mouseOver=false;
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
        if(!isOver(e.x(),e.y()))return false;
        if(e.button()==1&&contentHeight>height&&e.x()>=x+width-theme.scale(11)){
            double thumb=thumbHeight(),ty=thumbY();
            if(e.y()<ty||e.y()>ty+thumb)setScroll((e.y()-y-thumb/2)/Math.max(1,height-thumb)*Math.max(0,contentHeight-height));
            dragging=true;lastMouseY=e.y();setFocused(true);return true;
        }
        return super.mouseClicked(e,twice);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){
        if(dragging){dragging=false;setFocused(false);return true;}
        return super.mouseReleased(e);
    }
    @Override public boolean onMouseScrolled(double amount){
        if(!mouseOver)return false;
        double before=scroll;setScroll(scroll-amount*theme.scale(48));return before!=scroll;
    }
    private void setScroll(double v){scroll=Math.max(0,Math.min(v,Math.max(0,contentHeight-height)));invalidate();}
    private double thumbHeight(){return Math.min(height,Math.max(theme.scale(24),height*height/Math.max(height,contentHeight)));}
    private double thumbY(){return y+theme.scale(4)+(height-theme.scale(8)-thumbHeight())*scroll/Math.max(1,contentHeight-height);}
    @Override public boolean render(GuiRenderer r,double mx,double my,double dt){
        if(!visible)return true;
        ReskinTheme t=(ReskinTheme)theme;
        if(background)ReskinDraw.panel(r,t,x,y,width,height,false);
        r.scissorStart(x,y,width,height);
        // WContainer's default render can prematurely stop at a hidden row. Render only intersecting rows.
        for(Cell<?> c:cells){WWidget w=c.widget();if(w.visible&&w.y+w.height>y&&w.y<y+height){
            if(!isOver(mx,my))w.mouseOver=false;
            w.render(r,mx,my,dt);
        }}
        r.scissorEnd();
        if(contentHeight>height+.5){
            ReskinDraw.round(r,x+width-theme.scale(7),y+theme.scale(4),theme.scale(3),Math.max(1,height-theme.scale(8)),theme.scale(2),t.border());
            ReskinDraw.round(r,x+width-theme.scale(8),thumbY(),theme.scale(4),thumbHeight(),theme.scale(2),t.accentColor.get());
        }
        return false;
    }
}
