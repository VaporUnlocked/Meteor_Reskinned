// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

/** Original primitive-only chrome; no shader, image, or font dependency. */
final class ReskinDraw {
    private ReskinDraw() {}
    static Color alpha(Color c, int a) { return new Color(c.r,c.g,c.b,Math.max(0,Math.min(255,a))); }
    static Color mix(Color a,Color b,double v){return new Color((int)(a.r+(b.r-a.r)*v),(int)(a.g+(b.g-a.g)*v),(int)(a.b+(b.b-a.b)*v),255);}
    static void round(GuiRenderer g,double x,double y,double w,double h,double rad,Color c){
        if(w<=0 || h<=0)return;
        double r=Math.max(0,Math.min(rad,Math.min(w,h)/2));
        if(r<.5){g.quad(x,y,w,h,c);return;}
        g.quad(x+r,y,w-2*r,h,c);
        g.quad(x,y+r,r,h-2*r,c);g.quad(x+w-r,y+r,r,h-2*r,c);
        double[] cx={x+r,x+w-r,x+w-r,x+r},cy={y+r,y+r,y+h-r,y+h-r};
        for(int k=0;k<4;k++)for(int i=0;i<7;i++){
            double a=Math.PI+(k*Math.PI/2)+(i*Math.PI/14), b=a+Math.PI/14;
            g.triangle(cx[k],cy[k],cx[k]+Math.cos(a)*r,cy[k]+Math.sin(a)*r,cx[k]+Math.cos(b)*r,cy[k]+Math.sin(b)*r,c);
        }
    }
    static void panel(GuiRenderer g,ReskinTheme t,double x,double y,double w,double h,boolean selected){
        double r=t.scale(t.cornerRadius.get());
        if(t.style()==ReskinTheme.StylePreset.NEON_DOCK && selected && t.glow.get()){
            for(int i=3;i>=1;i--)round(g,x-i,y-i,w+i*2,h+i*2,r+i,alpha(t.accentColor.get(),18));
        }
        round(g,x,y,w,h,r,selected?t.accentColor.get():t.border());
        round(g,x+1,y+1,w-2,h-2,Math.max(0,r-1),t.surface());
    }
    static void toggle(GuiRenderer g,ReskinTheme t,double x,double y,boolean active){
        double w=t.scale(30),h=t.scale(16);
        round(g,x,y,w,h,h/2,active?t.accentColor.get():t.border());
        round(g,x+(active?w-h:0)+t.scale(2),y+t.scale(2),h-t.scale(4),h-t.scale(4),h/2,
            active?new Color(247,255,253):t.textSecondaryColor.get());
    }
    static void star(GuiRenderer g,double x,double y,double s,Color c){
        double cx=x+s/2,cy=y+s/2;
        for(int i=0;i<10;i++){
            double a=-Math.PI/2+i*Math.PI/5,b=a+Math.PI/5;
            double ra=(i%2==0?s*.48:s*.21),rb=(i%2==0?s*.21:s*.48);
            g.triangle(cx,cy,cx+Math.cos(a)*ra,cy+Math.sin(a)*ra,cx+Math.cos(b)*rb,cy+Math.sin(b)*rb,c);
        }
    }
    static void text(GuiRenderer g,ReskinTheme t,String text,double x,double y,double width,Color color,boolean title){
        if(width<=0||text==null)return;
        double w=t.textWidth(text,text.length(),title);
        if(w>width){
            String suffix="...";double sw=t.textWidth(suffix,suffix.length(),title);
            int end=text.length();
            while(end>0&&t.textWidth(text,end,title)+sw>width)end=text.offsetByCodePoints(end,-1);
            text=end>0?text.substring(0,end)+suffix:"";
        }
        if(!text.isEmpty())g.text(text,x,y,color,title);
    }
    static boolean hit(double mx,double my,double x,double y,double w,double h){return w>0&&h>0&&mx>=x&&my>=y&&mx<x+w&&my<y+h;}
}
