// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import net.minecraft.client.input.MouseButtonEvent;

/** Reskin-owned viewport. Optional pinned tail keeps collection shortcuts separate from category scrolling. */
final class ReskinScrollPane extends WContainer {
    double viewportWidth=200,viewportHeight=400,scroll,contentHeight,gap=6;
    int columns=1,pinnedTail;
    boolean background=true,dragging;
    private double lastMouseY,scrollHeight;
    private int bodyEnd;
    interface FullRow {}
    void bounds(double w,double h){viewportWidth=Math.max(1,w);viewportHeight=Math.max(1,h);}
    @Override protected void onCalculateSize(){
        width=viewportWidth;height=viewportHeight;bodyEnd=cells.size();scrollHeight=height;
        if(pinnedTail>0&&cells.size()>pinnedTail){
            int start=cells.size()-pinnedTail;
            double footer=arrange(start,cells.size(),1,false,0,0);
            // Tiny windows retain a single scrollable list instead of hiding categories.
            if(footer+theme.scale(130)<=height){bodyEnd=start;scrollHeight=height-footer;}
        }
        contentHeight=arrange(0,bodyEnd,columns,false,0,0);
        scroll=Math.max(0,Math.min(scroll,Math.max(0,contentHeight-scrollHeight)));
    }
    private double arrange(int start,int end,int requestedColumns,boolean place,double offset,double scrollOffset){
        double pad=theme.scale(8),g=theme.scale(gap),yy=pad,rowHeight=0;
        int count=0,cols=Math.max(1,requestedColumns);
        double usable=Math.max(1,width-2*pad-theme.scale(8));
        double cw=Math.max(1,(usable-g*(cols-1))/cols);
        for(int i=start;i<end;i++){
            Cell<?> c=cells.get(i);WWidget w=c.widget();if(!w.visible)continue;
            boolean full=w instanceof FullRow;
            if(w instanceof ReskinReflow.WidthAware aware){aware.availableWidth(full?usable:cw);w.calculateSize();}
            if(full&&count!=0){yy+=rowHeight+g;count=0;rowHeight=0;}
            if(place){c.x=x+pad+count*(cw+g);c.y=y+offset+yy-scrollOffset;c.width=full?usable:cw;c.height=w.height;c.expandWidgetX();c.alignWidget();}
            rowHeight=Math.max(rowHeight,w.height);count++;
            if(full||count==cols){yy+=rowHeight+g;count=0;rowHeight=0;}
        }
        if(count>0)yy+=rowHeight+g;
        return Math.max(0,yy+pad-g);
    }
    @Override protected void onCalculateWidgetPositions(){
        arrange(0,bodyEnd,columns,true,0,scroll);
        if(bodyEnd<cells.size())arrange(bodyEnd,cells.size(),1,true,scrollHeight,0);
    }
    private boolean pinned(WWidget w){for(int i=bodyEnd;i<cells.size();i++)if(cells.get(i).widget()==w)return true;return false;}
    private boolean inViewport(WWidget w){
        boolean tail=pinned(w);double lo=tail?y+scrollHeight:y,hi=tail?y+height:y+scrollHeight;
        return w.y+w.height>lo&&w.y<hi&&lastMouseY>=lo&&lastMouseY<hi;
    }
    @Override protected boolean propagateEvents(WWidget w){
        return w.visible&&(w.isFocused()||mouseOver&&inViewport(w)&&w.x+w.width>x&&w.x<x+width-theme.scale(8));
    }
    @Override public void mouseMoved(double mx,double my,double ox,double oy){
        mouseOver=isOver(mx,my);
        if(dragging){double travel=Math.max(1,scrollHeight-theme.scale(8)-thumbHeight());setScroll(scroll+(my-lastMouseY)/travel*Math.max(0,contentHeight-scrollHeight));}
        lastMouseY=my;super.mouseMoved(mx,my,ox,oy);
        for(Cell<?> c:cells)if(!mouseOver||!inViewport(c.widget()))c.widget().mouseOver=false;
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
        if(!isOver(e.x(),e.y()))return false;
        if(e.button()==1&&e.y()<y+scrollHeight&&contentHeight>scrollHeight&&e.x()>=x+width-theme.scale(11)){
            double thumb=thumbHeight(),ty=thumbY();
            if(e.y()<ty||e.y()>ty+thumb)setScroll((e.y()-y-thumb/2)/Math.max(1,scrollHeight-thumb)*Math.max(0,contentHeight-scrollHeight));
            dragging=true;lastMouseY=e.y();setFocused(true);return true;
        }
        return super.mouseClicked(e,twice);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){if(dragging){dragging=false;setFocused(false);return true;}return super.mouseReleased(e);}
    @Override public boolean onMouseScrolled(double amount){
        if(!mouseOver||lastMouseY>=y+scrollHeight)return false;
        double before=scroll;setScroll(scroll-amount*theme.scale(48));return before!=scroll;
    }
    private void setScroll(double v){scroll=Math.max(0,Math.min(v,Math.max(0,contentHeight-scrollHeight)));invalidate();}
    private double thumbHeight(){return Math.min(scrollHeight,Math.max(theme.scale(24),scrollHeight*scrollHeight/Math.max(scrollHeight,contentHeight)));}
    private double thumbY(){return y+theme.scale(4)+(scrollHeight-theme.scale(8)-thumbHeight())*scroll/Math.max(1,contentHeight-scrollHeight);}
    private void renderRange(GuiRenderer r,int start,int end,double top,double h,double mx,double my,double dt){
        r.scissorStart(x,top,width,h);
        for(int i=start;i<end;i++){
            WWidget w=cells.get(i).widget();
            if(w.visible&&w.y+w.height>top&&w.y<top+h){
                if(!isOver(mx,my)||my<top||my>=top+h)w.mouseOver=false;
                w.render(r,mx,my,dt);
            }
        }
        r.scissorEnd();
    }
    @Override public boolean render(GuiRenderer r,double mx,double my,double dt){
        if(!visible)return true;ReskinTheme t=(ReskinTheme)theme;
        if(background)ReskinDraw.panel(r,t,x,y,width,height,false);
        renderRange(r,0,bodyEnd,y,scrollHeight,mx,my,dt);
        if(bodyEnd<cells.size()){
            r.quad(x+1,y+scrollHeight,Math.max(1,width-2),1,ReskinDraw.alpha(t.border(),160));
            renderRange(r,bodyEnd,cells.size(),y+scrollHeight,height-scrollHeight,mx,my,dt);
        }
        if(contentHeight>scrollHeight+.5){
            ReskinDraw.round(r,x+width-theme.scale(7),y+theme.scale(4),theme.scale(3),Math.max(1,scrollHeight-theme.scale(8)),theme.scale(2),t.border());
            ReskinDraw.round(r,x+width-theme.scale(8),thumbY(),theme.scale(4),thumbHeight(),theme.scale(2),t.accentColor.get());
        }
        return false;
    }
}
