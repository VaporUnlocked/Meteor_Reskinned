// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.utils.Cell;

/** Bounded option popup with edge flipping, wheel scrolling and native selection callbacks. */
public final class ReskinDropdown<T> extends WDropdown<T> {
    private final ReskinText label=new ReskinText();
    private double totalHeight,scroll,popupHeight;
    public ReskinDropdown(T[] values,T value){super(values,value);}
    @Override protected WDropdownRoot createRootWidget(){return new Popup();}
    @Override protected WDropdownValue createValueWidget(){return new Option();}
    @Override protected void onCalculateSize(){
        super.onCalculateSize();
        totalHeight=root.height;
        width=Math.min(width,Math.max(theme.scale(80),Math.min(theme.scale(310),((ReskinTheme)theme).logicalWidth()*.55)));
        height=Math.max(theme.scale(28),theme.textHeight()+theme.scale(10));
        root.width=width;
    }
    @Override protected void onCalculateWidgetPositions(){layoutPopup();}
    private void layoutPopup(){
        ReskinTheme t=(ReskinTheme)theme;
        double margin=t.scale(8), below=t.logicalHeight()-t.bottomInset()-(y+height)-margin;
        double above=y-t.topInset()-margin;
        boolean up=below<Math.min(totalHeight,t.scale(160))&&above>below;
        popupHeight=Math.max(1,Math.min(totalHeight,Math.max(1,up?above:below)));
        scroll=Math.max(0,Math.min(scroll,totalHeight-popupHeight));
        root.x=Math.max(margin,Math.min(x,t.logicalWidth()-width-margin));
        root.y=up?Math.max(margin,y-popupHeight):y+height;
        root.width=width;root.height=popupHeight;
        root.calculateWidgetPositions();
        if(scroll>0)root.moveCells(0,-scroll);
    }
    @Override protected void onPressed(int button){if(button==1){super.onPressed(button);layoutPopup();}}
    @Override public boolean onMouseScrolled(double amount){
        if(!expanded)return false;
        double before=scroll;scroll=Math.max(0,Math.min(Math.max(0,totalHeight-popupHeight),scroll-amount*theme.scale(40)));
        layoutPopup();return before!=scroll;
    }
    @Override public boolean render(GuiRenderer r,double mx,double my,double dt){
        if(!visible)return true;
        onRender(r,mx,my,dt);
        if(mouseOver&&tooltip!=null)r.tooltip(tooltip);
        if(expanded){
            layoutPopup();
            r.absolutePost(()->{
                r.scissorStart(root.x,root.y,width,popupHeight);
                root.render(r,mx,my,dt);
                r.scissorEnd();
            });
            if(root.mouseOver)theme.disableHoverColor=true;
        }
        return false;
    }
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        ReskinTheme t=(ReskinTheme)theme;
        ReskinDraw.panel(r,t,x,y,width,height,expanded||mouseOver);
        label.draw(r,t,String.valueOf(value),x+t.scale(9),y+(height-t.textHeight())/2,Math.max(1,width-t.scale(32)),t.textHeight(),false,t.textColor.get(),mouseOver,dt);
        ReskinDraw.text(r,t,expanded?"-":"v",x+width-t.scale(18),y+(height-t.textHeight())/2,t.scale(15),t.accentColor.get(),false);
    }
    private final class Popup extends WDropdownRoot {
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){ReskinDraw.panel(r,(ReskinTheme)theme,x,y,width,height,true);}
        @Override protected boolean propagateEvents(WWidget w){return w.visible&&mouseOver&&w.y+w.height>y&&w.y<y+height;}
        @Override public void mouseMoved(double mx,double my,double ox,double oy){
            mouseOver=isOver(mx,my);super.mouseMoved(mx,my,ox,oy);
            if(!mouseOver)for(Cell<?> c:cells)c.widget().mouseOver=false;
        }
    }
    private final class Option extends WDropdownValue {
        private final ReskinText text=new ReskinText();
        @Override protected void onCalculateSize(){width=theme.scale(120);height=theme.scale(30);}
        @Override protected void onPressed(int button){if(button==1)super.onPressed(button);}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
            ReskinTheme t=(ReskinTheme)theme;
            if(mouseOver)ReskinDraw.round(r,x,y,width,height,t.scale(4),t.headerSurfaceColor.get());
            text.draw(r,t,String.valueOf(value),x+t.scale(8),y+(height-t.textHeight())/2,Math.max(1,width-t.scale(16)),t.textHeight(),false,t.textColor.get(),mouseOver,dt);
        }
    }
}
