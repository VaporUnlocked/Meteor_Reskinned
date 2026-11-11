// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

/** Original primitive-only chrome; no shader, image, or font dependency. */
final class ReskinDraw {
    private ReskinDraw() {}
    /**
     * Meteor 26.3-17 UI_COLORED enables culling. Match Renderer2D.quad's
     * TL/BL/BR winding (negative signed area in screen coordinates).
     * The previous fan order was reversed; a software-only fill hid that error.
     */
    static void triangle(GuiRenderer g,double x1,double y1,double x2,double y2,double x3,double y3,Color c) {
        double area=(x2-x1)*(y3-y1)-(y2-y1)*(x3-x1);
        if(Math.abs(area)<1e-9)return;
        if(area>0)g.triangle(x1,y1,x3,y3,x2,y2,c);
        else g.triangle(x1,y1,x2,y2,x3,y3,c);
    }
    static Color alpha(Color c, int a) { return new Color(c.r,c.g,c.b,Math.max(0,Math.min(255,a))); }
    static Color mix(Color a,Color b,double v){return new Color((int)(a.r+(b.r-a.r)*v),(int)(a.g+(b.g-a.g)*v),(int)(a.b+(b.b-a.b)*v),255);}
    static void round(GuiRenderer g,double x,double y,double w,double h,double rad,Color c){
        if(w<=0 || h<=0)return;
        double r=Math.max(0,Math.min(rad,Math.min(w,h)/2));
        if(r<.5){g.quad(x,y,w,h,c);return;}
        g.quad(x+r,y,w-2*r,h,c);
        g.quad(x,y+r,r,h-2*r,c);g.quad(x+w-r,y+r,r,h-2*r,c);
        double[] cx={x+r,x+w-r,x+w-r,x+r},cy={y+r,y+r,y+h-r,y+h-r};
        for(int k=0;k<4;k++)for(int i=0;i<12;i++){
            double a=Math.PI+(k*Math.PI/2)+(i*Math.PI/24), b=a+Math.PI/24;
            triangle(g,cx[k],cy[k],cx[k]+Math.cos(a)*r,cy[k]+Math.sin(a)*r,cx[k]+Math.cos(b)*r,cy[k]+Math.sin(b)*r,c);
        }
    }
    static void panel(GuiRenderer g,ReskinTheme t,double x,double y,double w,double h,boolean selected){
        if(w<2||h<2)return;
        // Square by default. Optional subtle rounding is respected, never imposed by an outer frame.
        double radius=t.scale(Math.min(4,t.cornerRadius.get()));
        Color edge=selected?t.accentColor.get():alpha(t.border(),115);
        if(selected&&t.glow.get()&&(t.style()==ReskinTheme.StylePreset.NEON_DOCK||t.style()==ReskinTheme.StylePreset.PRISM_STUDIO)){
            for(int i=3;i>=1;i--)round(g,x-i,y-i,w+2*i,h+2*i,radius+i,alpha(t.accentColor.get(),9));
        }
        round(g,x,y,w,h,radius,edge);
        Color top=mix(t.surface(),t.headerSurfaceColor.get(),selected?.70:.38);
        Color bottom=t.surface();
        if(radius<.5)g.quad(x+1,y+1,w-2,h-2,alpha(top,t.surface().a),alpha(top,t.surface().a),bottom,bottom);
        else round(g,x+1,y+1,w-2,h-2,Math.max(0,radius-1),bottom);
        g.quad(x+1,y+1,w-2,1,alpha(selected?t.accentColor.get():t.textColor.get(),selected?95:14));
    }
    static void workspace(GuiRenderer g,ReskinTheme t,double x,double y,double w,double h){
        if(w<=2||h<=2)return;
        double radius=t.scale(Math.min(4,t.cornerRadius.get()));
        // Glass and modular skins keep the scenery between independent panels.
        boolean detached=t.style()==ReskinTheme.StylePreset.AURORA_GLASS||t.style()==ReskinTheme.StylePreset.ATLAS_PANELS||t.style()==ReskinTheme.StylePreset.HORIZON_SPLIT;
        if(detached)return;
        g.quad(x+3,y+5,w,h,new Color(0,0,0,60));
        round(g,x,y,w,h,radius,alpha(t.border(),95));
        round(g,x+1,y+1,w-2,h-2,Math.max(0,radius-1),alpha(t.surface(),225));
    }
    static double measure(ReskinTheme t,String text,boolean title){return t.textWidth(text,text.length(),title);}
    static double labelWidth(ReskinTheme t,String text,double padding){return Math.ceil(measure(t,text,false)+t.scale(padding))+2;}
    static void ribbon(GuiRenderer g,ReskinTheme t,double x,double y,double w,double h){
        if(w<=0||h<=0)return;
        // Original abstract ornament; it is not a simulated gameplay or performance preview.
        for(int i=0;i<32;i++){
            double v=(double)i/31;
            g.quad(x+i*w/32,y,w/32+.05,h,alpha(mix(t.headerSurfaceColor.get(),t.accentColor.get(),.12+.16*Math.sin(v*3.14)),150));
        }
        for(int i=0;i<9;i++){
            double xx=x+(i+1)*w/11,hh=h*(.30+.05*i);
            triangle(g,xx,y+h,xx+w*.20,y+h,xx+w*.15,y+h-hh,alpha(t.accentColor.get(),12+i*2));
        }
    }
    static void toggle(GuiRenderer g,ReskinTheme t,double x,double y,boolean active){
        double w=t.scale(38),h=t.scale(20);
        round(g,x,y,w,h,h/2,active?t.accentColor.get():mix(t.surface(),new Color(102,114,126),.48));
        round(g,x+(active?w-h:0)+t.scale(2),y+t.scale(2),h-t.scale(4),h-t.scale(4),h/2,
            active?new Color(247,255,253):t.textSecondaryColor.get());
    }
    static void star(GuiRenderer g,double x,double y,double s,Color c){
        double cx=x+s/2,cy=y+s/2;
        for(int i=0;i<10;i++){
            double a=-Math.PI/2+i*Math.PI/5,b=a+Math.PI/5;
            double ra=(i%2==0?s*.48:s*.21),rb=(i%2==0?s*.21:s*.48);
            triangle(g,cx,cy,cx+Math.cos(a)*ra,cy+Math.sin(a)*ra,cx+Math.cos(b)*rb,cy+Math.sin(b)*rb,c);
        }
    }
    static void text(GuiRenderer g,ReskinTheme t,String text,double x,double y,double width,Color color,boolean title){
        if(width<=0||text==null)return;
        double w=t.textWidth(text,text.length(),title);
        if(w>width+0.75){
            String suffix="...";double sw=t.textWidth(suffix,suffix.length(),title);
            int end=text.length();
            while(end>0&&t.textWidth(text,end,title)+sw>width)end=text.offsetByCodePoints(end,-1);
            text=end>0?text.substring(0,end)+suffix:"";
        }
        if(!text.isEmpty())g.text(text,x,y,color,title);
    }
    static boolean hit(double mx,double my,double x,double y,double w,double h){return w>0&&h>0&&mx>=x&&my>=y&&mx<x+w&&my<y+h;}
}
