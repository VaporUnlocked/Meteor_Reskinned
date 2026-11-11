// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.input.MouseButtonEvent;

/** Transactional color dialog. Closing or canceling cannot overwrite a saved color. */
public final class ReskinColorScreen extends WidgetScreen {
    final Setting<SettingColor> setting;
    final ReskinColorDraft draft;
    final SettingColor original;
    private boolean valid=true,updating;
    private String error="Changes are saved only when you press Apply.";
    private Editor editor;
    public ReskinColorScreen(ReskinTheme theme,Setting<SettingColor> setting){
        super(theme,"Color / "+setting.title);this.setting=setting;
        SettingColor current=setting.get();original=new SettingColor(current.r,current.g,current.b,current.a);original.rainbow=current.rainbow;draft=new ReskinColorDraft(original.r,original.g,original.b,original.a);
    }
    @Override public void initWidgets(){editor=add(new Editor()).top().widget();}
    public boolean applyDraft(){
        if(!valid)return false;
        SettingColor copy=new SettingColor(draft.r(),draft.g(),draft.b(),draft.a());
        copy.rainbow=editor!=null?editor.rainbow.checked:original.rainbow;
        boolean applied=setting.set(copy);
        if(applied){((ReskinTheme)theme).saveAppearance();((ReskinTheme)theme).refresh();}
        return applied;
    }
    final class Editor extends WContainer {
        WTextBox hex;WTextBox[] rgba=new WTextBox[4];WButton apply,cancel,reset;WCheckbox rainbow;
        Plane plane,hue,alpha;ReskinView viewport;Body body;double bx,by,bw,bh,pad,planeH;
        @Override public void init(){
            viewport=add(new ReskinView()).widget();
            body=viewport.add(new Body()).widget();
            plane=body.add(new Plane(0)).widget();hue=body.add(new Plane(1)).widget();alpha=body.add(new Plane(2)).widget();
            hex=body.add(theme.textBox(draft.hex())).widget();hex.action=()->{
                if(updating)return;valid=draft.parseHex(hex.get());error=valid?"Ready to apply.":"Use #RRGGBB or #RRGGBBAA; incomplete input is not saved.";
                if(valid)sync(false,-1);
            };
            for(int i=0;i<4;i++){final int n=i;rgba[i]=body.add(theme.textBox(""+channel(i))).widget();rgba[i].action=()->{
                if(updating)return;valid=draft.channel(n,rgba[n].get());error=valid?"Ready to apply.":"Channel must be a whole number from 0 to 255.";
                if(valid)sync(true,n);
            };}
            rainbow=body.add(theme.checkbox(original.rainbow)).widget();
            apply=add(theme.button("Apply color")).widget();apply.action=()->{if(applyDraft())ReskinAccess.later(ReskinColorScreen.this::onClose);};
            cancel=add(theme.button("Cancel")).widget();cancel.action=()->ReskinAccess.later(ReskinColorScreen.this::onClose);
            reset=add(theme.button("Reset draft")).widget();reset.action=()->{SettingColor c=setting.getDefaultValue();draft.set(c.r,c.g,c.b,c.a);rainbow.checked=c.rainbow;valid=true;sync(true,-1);};
        }
        int channel(int i){return switch(i){case 0->draft.r();case 1->draft.g();case 2->draft.b();default->draft.a();};}
        void sync(boolean updateHex,int edited){updating=true;try{if(updateHex)hex.set(draft.hex());for(int i=0;i<4;i++)if(i!=edited)rgba[i].set(""+channel(i));}finally{updating=false;}}
        @Override protected void onCalculateSize(){
            var t=(ReskinTheme)theme;width=t.logicalWidth();height=t.logicalHeight();pad=t.scale(20);
            bw=Math.min(t.scale(600),width-t.scale(24));bh=Math.min(t.scale(650),height-t.scale(24));
            bx=(width-bw)/2;by=(height-bh)/2;planeH=Math.max(t.scale(110),bh-t.scale(403));
            viewport.externallyConstrainedMaxWidth=Math.max(1,bw-pad*2);viewport.externallyConstrainedMaxHeight=Math.max(1,bh-t.scale(160));viewport.calculateSize();
        }
        void put(WWidget w,double x,double y,double width,double height){w.x=x;w.y=y;w.width=Math.max(1,width);w.height=Math.max(1,height);w.calculateWidgetPositions();}
        @Override public void calculateWidgetPositions(){
            var t=(ReskinTheme)theme;double xx=bx+pad,ww=bw-pad*2;
            put(viewport,xx,by+t.scale(76),ww,Math.max(1,bh-t.scale(160)));
            double bottom=by+bh-t.scale(48);put(reset,xx,bottom,ww*.31,t.scale(32));put(cancel,xx+ww*.34,bottom,ww*.27,t.scale(32));put(apply,xx+ww*.64,bottom,ww*.36,t.scale(32));
        }
        final class Body extends WContainer implements ReskinReflow.WidthAware {
            double available=500;
            @Override public void availableWidth(double w){available=w;}
            @Override protected void onCalculateSize(){width=available;height=planeH+theme.scale(243);}
            @Override public void calculateWidgetPositions(){
                var t=(ReskinTheme)theme;double xx=x,yy=y,ww=width;
                put(plane,xx,yy,ww,planeH);put(hue,xx,yy+planeH+t.scale(12),ww,t.scale(18));put(alpha,xx,yy+planeH+t.scale(42),ww,t.scale(18));
                put(hex,xx,yy+planeH+t.scale(88),ww,t.scale(32));double col=(ww-t.scale(30))/4;
                for(int i=0;i<4;i++)put(rgba[i],xx+i*(col+t.scale(10)),yy+planeH+t.scale(155),col,t.scale(30));
                put(rainbow,xx,yy+planeH+t.scale(205),t.scale(20),t.scale(20));
            }
            @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
                var t=(ReskinTheme)theme;
                ReskinDraw.text(r,t,"HEX / RGBA",hex.x,hex.y-t.scale(22),hex.width,t.textSecondaryColor.get(),false);
                for(int i=0;i<4;i++)r.text(new String[]{"R","G","B","A"}[i],rgba[i].x,rgba[i].y-t.scale(22),t.textColor.get(),false);
                ReskinDraw.text(r,t,"Rainbow",rainbow.x+t.scale(32),rainbow.y,t.scale(120),t.textColor.get(),false);
            }
        }
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
            var t=(ReskinTheme)theme;r.quad(0,0,width,height,new Color(0,0,0,150));ReskinDraw.panel(r,t,bx,by,bw,bh,false);
            ReskinDraw.text(r,t,"COLOR / "+setting.title,bx+pad,by+t.scale(18),bw-pad*2,t.titleTextColor.get(),true);
            ReskinDraw.text(r,t,"Current",bx+pad,by+t.scale(48),t.scale(75),t.textSecondaryColor.get(),false);
            r.quad(bx+pad+t.scale(80),by+t.scale(45),t.scale(50),t.scale(20),original);
            r.quad(bx+pad+t.scale(137),by+t.scale(45),t.scale(50),t.scale(20),new Color(draft.r(),draft.g(),draft.b(),draft.a()));
            ReskinDraw.text(r,t,"Draft",bx+pad+t.scale(195),by+t.scale(48),bw-pad*2-t.scale(195),t.textSecondaryColor.get(),false);
            ReskinDraw.text(r,t,error,bx+pad,by+bh-t.scale(77),bw-pad*2,valid?t.textSecondaryColor.get():new Color(255,160,140),false);
        }
        @Override protected boolean propagateEvents(WWidget w){return w.visible;}
    }
    final class Plane extends WWidget {
        final int mode;boolean dragging;Plane(int mode){this.mode=mode;}
        @Override protected void onCalculateSize(){width=theme.scale(400);height=theme.scale(mode==0?190:18);}
        void change(double mx,double my){
            float px=(float)Math.max(0,Math.min(1,(mx-x)/Math.max(1,width))),py=(float)Math.max(0,Math.min(1,(my-y)/Math.max(1,height)));
            float[] hsv=draft.hsv();if(mode==0)draft.hsv(hsv[0],px,1-py);else if(mode==1)draft.hsv(px,hsv[1],hsv[2]);else draft.set(draft.r(),draft.g(),draft.b(),Math.round(px*255));
            valid=true;error="Ready to apply.";editor.sync(true,-1);
        }
        @Override public boolean onMouseClicked(MouseButtonEvent e,boolean twice){if(e.button()!=1||!isOver(e.x(),e.y()))return false;dragging=true;setFocused(true);change(e.x(),e.y());return true;}
        @Override public void onMouseMoved(double mx,double my,double ox,double oy){if(dragging)change(mx,my);}
        @Override public boolean onMouseReleased(MouseButtonEvent e){boolean was=dragging;dragging=false;setFocused(false);return was;}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
            float[] hsv=draft.hsv();
            if(mode==0){
                int c=java.awt.Color.HSBtoRGB(hsv[0],1,1);Color pure=new Color(c>>>16&255,c>>>8&255,c&255);
                r.quad(x,y,width,height,new Color(255,255,255),pure,new Color(0,0,0),new Color(0,0,0));
            }else if(mode==1){
                for(int i=0;i<6;i++){
                    int a=java.awt.Color.HSBtoRGB(i/6f,1,1),b=java.awt.Color.HSBtoRGB((i+1)/6f,1,1);
                    r.quad(x+i*width/6,y,width/6,height,new Color(a>>>16&255,a>>>8&255,a&255),new Color(b>>>16&255,b>>>8&255,b&255));
                }
            }else{
                double size=Math.max(4,theme.scale(6));
                for(int row=0;row<Math.ceil(height/size);row++)for(int col=0;col<Math.ceil(width/size);col++){
                    int v=(row+col)%2==0?76:102;r.quad(x+col*size,y+row*size,Math.min(size,width-col*size),Math.min(size,height-row*size),new Color(v,v,v));
                }
                r.quad(x,y,width,height,new Color(draft.r(),draft.g(),draft.b(),0),new Color(draft.r(),draft.g(),draft.b(),255));
            }
            double hx=x+(mode==0?hsv[1]:mode==1?hsv[0]:draft.a()/255.0)*width,hy=y+(1-hsv[2])*height;
            if(mode==0){ReskinDraw.round(r,hx-5,hy-5,10,10,5,new Color(255,255,255));ReskinDraw.round(r,hx-3,hy-3,6,6,3,new Color(draft.r(),draft.g(),draft.b()));}
            else r.quad(Math.max(x,Math.min(x+width-2,hx)),y,2,height,new Color(255,255,255));
        }
    }
}
