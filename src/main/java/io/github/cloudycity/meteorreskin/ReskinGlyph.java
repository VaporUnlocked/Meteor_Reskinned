// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;

import java.util.Locale;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

/** Original resolution-independent UI symbols. No font glyphs, textures or bundled fonts. */
final class ReskinGlyph {
    private ReskinGlyph() {}
    static void line(GuiRenderer r,double x1,double y1,double x2,double y2,double stroke,Color c) {
        double length=Math.hypot(x2-x1,y2-y1); if(length<.001)return;
        double dx=-(y2-y1)*stroke/(2*length),dy=(x2-x1)*stroke/(2*length);
        ReskinDraw.triangle(r,x1+dx,y1+dy,x2+dx,y2+dy,x2-dx,y2-dy,c);
        ReskinDraw.triangle(r,x1+dx,y1+dy,x2-dx,y2-dy,x1-dx,y1-dy,c);
    }
    static void draw(GuiRenderer r,String name,double x,double y,double s,Color c) {
        String key=name==null?"":name.toLowerCase(Locale.ROOT);
        double a=x+s*.18,b=x+s*.82,top=y+s*.18,bot=y+s*.82,cx=x+s*.5,cy=y+s*.5,st=Math.max(1,s*.065);
        String symbolKey=switch(key){case "appearance"->"palette";case "profiles"->"profile";default->key;};
        if (special(r,symbolKey,x,y,s,c)) return;
        if(key.contains("meteor")||key.equals("logo")) {
            ReskinDraw.triangle(r,x+s*.15,y+s*.76,x+s*.36,y+s*.40,x+s*.88,y+s*.08,c);
            ReskinDraw.triangle(r,x+s*.15,y+s*.76,x+s*.52,y+s*.85,x+s*.90,y+s*.36,c);
            ReskinDraw.triangle(r,x+s*.36,y+s*.40,x+s*.52,y+s*.85,x+s*.70,y+s*.32,c); return;
        }
        if(key.contains("search")){circle(r,cx-s*.08,cy-s*.08,s*.27,st,c);line(r,cx+s*.13,cy+s*.13,b,bot,st,c);return;}
        if(key.contains("combat")||key.contains("pvp")){line(r,a,top,b,bot,st,c);line(r,a,bot,b,top,st,c);line(r,a,bot-s*.18,a+s*.18,bot,st,c);line(r,b-s*.18,bot,b,bot-s*.18,st,c);return;}
        if(key.contains("player")||key.contains("friend")||key.contains("account")){circle(r,cx,y+s*.30,s*.14,st,c);line(r,a,bot,a,y+s*.65,st,c);line(r,a,y+s*.65,cx,y+s*.5,st,c);line(r,cx,y+s*.5,b,y+s*.65,st,c);line(r,b,y+s*.65,b,bot,st,c);return;}
        if(key.contains("render")||key.equals("eye")){line(r,a,cy,cx,top+s*.13,st,c);line(r,cx,top+s*.13,b,cy,st,c);line(r,b,cy,cx,bot-s*.13,st,c);line(r,cx,bot-s*.13,a,cy,st,c);circle(r,cx,cy,s*.11,st,c);return;}
        if(key.contains("movement")||key.contains("walk")){circle(r,x+s*.61,y+s*.18,s*.09,st,c);line(r,x+s*.55,y+s*.33,x+s*.43,y+s*.57,st,c);line(r,x+s*.43,y+s*.57,x+s*.18,y+s*.79,st,c);line(r,x+s*.43,y+s*.57,x+s*.66,y+s*.65,st,c);line(r,x+s*.66,y+s*.65,x+s*.68,y+s*.87,st,c);line(r,x+s*.55,y+s*.33,x+s*.74,y+s*.48,st,c);line(r,x+s*.40,y+s*.34,x+s*.22,y+s*.46,st,c);return;}
        if(key.contains("favorite")||key.equals("star")){ReskinDraw.star(r,x,y,s,c);return;}
        if(key.contains("recent")||key.equals("clock")){circle(r,cx,cy,s*.33,st,c);line(r,cx,cy,cx,y+s*.27,st,c);line(r,cx,cy,x+s*.67,cy,st,c);return;}
        if(key.contains("enabled")||key.equals("power")){for(int i=0;i<18;i++){double aa=Math.PI*.22+i*Math.PI*1.56/18,bb=Math.PI*.22+(i+1)*Math.PI*1.56/18;line(r,cx+Math.sin(aa)*s*.32,cy-Math.cos(aa)*s*.32,cx+Math.sin(bb)*s*.32,cy-Math.cos(bb)*s*.32,st,c);}line(r,cx,y+s*.06,cx,cy,st,c);return;}
        if(key.contains("gui")||key.contains("hud")){box(r,a,top,b-a,(bot-top)*.70,st,c);line(r,cx,y+s*.65,cx,bot,st,c);line(r,x+s*.33,bot,x+s*.67,bot,st,c);return;}
        if(key.contains("config")||key.contains("setting")||key.contains("appearance")||key.contains("misc")||key.contains("filter")){for(int i=0;i<3;i++){double yy=top+i*s*.29;line(r,a,yy,b,yy,st,c);double xx=x+s*(i==1?.63:.36);box(r,xx-s*.06,yy-s*.07,s*.12,s*.14,st,c);}return;}
        if(key.contains("key")||key.contains("macro")){box(r,a,top,b-a,bot-top,st,c);for(int i=0;i<3;i++)for(int j=0;j<2;j++)r.quad(x+s*(.29+i*.21),y+s*(.31+j*.21),st,st,c);line(r,x+s*.30,y+s*.70,x+s*.70,y+s*.70,st,c);return;}
        if(key.contains("baritone")||key.contains("route")){line(r,a,bot,cx,cy,st,c);line(r,cx,cy,b,top,st,c);box(r,a-s*.06,bot-s*.06,s*.12,s*.12,st,c);box(r,cx-s*.06,cy-s*.06,s*.12,s*.12,st,c);box(r,b-s*.06,top-s*.06,s*.12,s*.12,st,c);return;}
        if(key.equals("close")){line(r,a,top,b,bot,st,c);line(r,a,bot,b,top,st,c);return;}
        if(key.equals("chevron")){line(r,x+s*.35,top,x+s*.67,cy,st,c);line(r,x+s*.67,cy,x+s*.35,bot,st,c);return;}
        if(key.equals("grid")||key.startsWith("all")){for(int i=0;i<2;i++)for(int j=0;j<2;j++)box(r,x+s*(.14+i*.43),y+s*(.14+j*.43),s*.28,s*.28,st,c);return;}
        if(key.equals("list")){for(int i=0;i<3;i++){double yy=y+s*(.23+i*.27);r.quad(a,yy,st*1.3,st,c);line(r,x+s*.37,yy,b,yy,st,c);}return;}
        if(key.contains("satellite")){circle(r,cx,cy,s*.12,st,c);for(int k=0;k<2;k++)for(int i=0;i<9;i++){double aa=-Math.PI*.6+i*Math.PI*1.2/9,bb=aa+Math.PI*1.2/9,rr=s*(.25+k*.15);line(r,cx+Math.cos(aa)*rr,cy+Math.sin(aa)*rr,cx+Math.cos(bb)*rr,cy+Math.sin(bb)*rr,st,c);}return;}
        // Cube is the neutral, non-semantic fallback for registered addon pages.
        line(r,cx,top,b,y+s*.35,st,c);line(r,b,y+s*.35,b,y+s*.68,st,c);line(r,b,y+s*.68,cx,bot,st,c);line(r,cx,bot,a,y+s*.68,st,c);line(r,a,y+s*.68,a,y+s*.35,st,c);line(r,a,y+s*.35,cx,top,st,c);line(r,a,y+s*.35,cx,cy,st,c);line(r,cx,cy,b,y+s*.35,st,c);line(r,cx,cy,cx,bot,st,c);
    }
    static void box(GuiRenderer r,double x,double y,double w,double h,double st,Color c){line(r,x,y,x+w,y,st,c);line(r,x+w,y,x+w,y+h,st,c);line(r,x+w,y+h,x,y+h,st,c);line(r,x,y+h,x,y,st,c);}
    static void circle(GuiRenderer r,double x,double y,double radius,double st,Color c){for(int i=0;i<24;i++){double a=i*Math.PI/12,b=(i+1)*Math.PI/12;line(r,x+Math.cos(a)*radius,y+Math.sin(a)*radius,x+Math.cos(b)*radius,y+Math.sin(b)*radius,st,c);}}
    /** Original 24-unit path artwork. Kept separate from name-based navigation aliases. */
    private static boolean special(GuiRenderer r,String key,double x,double y,double size,Color c) {
        double unit=size/24,stroke=Math.max(.8,size*.065);
        switch(key) {
            case "hud" -> {path(r,x,y,unit,stroke,c,3,9,3,3,9,3);path(r,x,y,unit,stroke,c,15,3,21,3,21,9);path(r,x,y,unit,stroke,c,21,15,21,21,15,21);path(r,x,y,unit,stroke,c,9,21,3,21,3,15);path(r,x,y,unit,stroke,c,8,12,16,12);path(r,x,y,unit,stroke,c,12,8,12,16);}
            case "gui" -> {path(r,x,y,unit,stroke,c,2,3,22,3,22,21,2,21,2,3);path(r,x,y,unit,stroke,c,2,8,22,8);path(r,x,y,unit,stroke,c,8,8,8,21);}
            case "settings" -> {ring(r,x,y,unit,stroke,c,12,12,4);ring(r,x,y,unit,stroke,c,12,12,8);for(int i=0;i<8;i++){double a=i*Math.PI/4;line(r,x+unit*(12+8*Math.cos(a)),y+unit*(12+8*Math.sin(a)),x+unit*(12+11*Math.cos(a)),y+unit*(12+11*Math.sin(a)),stroke*1.7,c);}}
            case "addon" -> {
                path(r,x,y,unit,stroke,c,3,7,8,7,8,4,12,4,12,7,17,7,17,12,20,12,20,16,17,16,17,21,3,21,3,16,6,16,6,12,3,12,3,7);
            }
            case "shield" -> path(r,x,y,unit,stroke,c,12,2,21,6,19,16,12,22,5,16,3,6,12,2);
            case "anchor" -> {ring(r,x,y,unit,stroke,c,12,5,3);path(r,x,y,unit,stroke,c,12,8,12,21);path(r,x,y,unit,stroke,c,7,11,17,11);path(r,x,y,unit,stroke,c,3,14,5,18,12,21,19,18,21,14);}
            case "anvil" -> {path(r,x,y,unit,stroke,c,3,5,21,5,20,10,15,12,15,17,19,19,19,21,5,21,5,19,9,17,9,12,3,9,3,5);}
            case "bed" -> {path(r,x,y,unit,stroke,c,3,4,3,21);path(r,x,y,unit,stroke,c,3,16,21,16,21,21);path(r,x,y,unit,stroke,c,3,9,9,9,9,15);path(r,x,y,unit,stroke,c,9,8,19,8,21,11,21,16);}
            case "robot" -> {path(r,x,y,unit,stroke,c,6,7,18,7,20,9,20,19,4,19,4,9,6,7);path(r,x,y,unit,stroke,c,12,7,12,3);ring(r,x,y,unit,stroke,c,12,2,1);ring(r,x,y,unit,stroke,c,8,12,1);ring(r,x,y,unit,stroke,c,16,12,1);path(r,x,y,unit,stroke,c,8,16,16,16);}
            case "arrow" -> {path(r,x,y,unit,stroke,c,3,21,21,3,13,4);path(r,x,y,unit,stroke,c,21,3,20,11);path(r,x,y,unit,stroke,c,3,13,3,21,11,21);}
            case "swap" -> {path(r,x,y,unit,stroke,c,3,7,21,7,17,3);path(r,x,y,unit,stroke,c,21,17,3,17,7,21);}
            case "armor" -> path(r,x,y,unit,stroke,c,8,3,10,7,14,7,16,3,22,7,19,12,16,10,16,21,8,21,8,10,5,12,2,7,8,3);
            case "apple" -> {path(r,x,y,unit,stroke,c,12,8,8,6,4,8,3,13,6,20,10,21,12,19,14,21,18,20,21,13,20,8,16,6,12,8);path(r,x,y,unit,stroke,c,12,8,12,4,16,2);}
            case "fish" -> {path(r,x,y,unit,stroke,c,3,7,3,17,8,13,13,18,18,17,22,12,18,7,13,6,8,11,3,7);ring(r,x,y,unit,stroke,c,17,11,1);}
            case "tool" -> {path(r,x,y,unit,stroke,c,4,5,10,2,17,4,21,10);path(r,x,y,unit,stroke,c,12,5,3,21,6,22,15,6);}
            case "potion" -> {path(r,x,y,unit,stroke,c,9,2,15,2,15,9,20,16,19,21,5,21,4,16,9,9,9,2);path(r,x,y,unit,stroke,c,7,14,17,14);path(r,x,y,unit,stroke,c,8,5,16,5);}
            case "crystal" -> {path(r,x,y,unit,stroke,c,12,2,20,8,18,18,12,22,6,18,4,8,12,2,8,9,12,22,16,9,12,2);path(r,x,y,unit,stroke,c,4,8,20,8);}
            case "totem" -> {path(r,x,y,unit,stroke,c,9,3,15,3,16,7,14,9,20,11,20,15,14,13,14,21,10,21,10,13,4,15,4,11,10,9,8,7,9,3);}
            case "wings" -> {path(r,x,y,unit,stroke,c,12,9,4,3,2,4,4,15,10,19,12,9,14,19,20,15,22,4,20,3,12,9);path(r,x,y,unit,stroke,c,12,9,12,22);}
            case "camera" -> {path(r,x,y,unit,stroke,c,3,7,7,7,9,3,15,3,17,7,21,7,21,20,3,20,3,7);ring(r,x,y,unit,stroke,c,12,13,4);}
            case "sun" -> {ring(r,x,y,unit,stroke,c,12,12,5);for(int i=0;i<8;i++){double a=i*Math.PI/4;line(r,x+unit*(12+8*Math.cos(a)),y+unit*(12+8*Math.sin(a)),x+unit*(12+11*Math.cos(a)),y+unit*(12+11*Math.sin(a)),stroke,c);}}
            case "chest" -> {path(r,x,y,unit,stroke,c,3,6,21,6,21,21,3,21,3,6);path(r,x,y,unit,stroke,c,3,12,10,12,10,16,14,16,14,12,21,12);path(r,x,y,unit,stroke,c,10,10,14,10,14,16,10,16,10,10);}
            case "tracer" -> {path(r,x,y,unit,stroke,c,3,20,10,14,15,7,21,4);ring(r,x,y,unit,stroke,c,10,14,2);ring(r,x,y,unit,stroke,c,21,4,2);path(r,x,y,unit,stroke,c,3,16,3,20,7,20);}
            case "crosshair" -> {ring(r,x,y,unit,stroke,c,12,12,7);path(r,x,y,unit,stroke,c,12,1,12,7);path(r,x,y,unit,stroke,c,12,17,12,23);path(r,x,y,unit,stroke,c,1,12,7,12);path(r,x,y,unit,stroke,c,17,12,23,12);}
            case "speed" -> {for(int i=0;i<18;i++){double a=Math.PI+i*Math.PI/18,b=a+Math.PI/18;line(r,x+unit*(12+9*Math.cos(a)),y+unit*(17+9*Math.sin(a)),x+unit*(12+9*Math.cos(b)),y+unit*(17+9*Math.sin(b)),stroke,c);}path(r,x,y,unit,stroke,c,12,17,17,8);path(r,x,y,unit,stroke,c,3,20,21,20);}
            case "feather" -> {path(r,x,y,unit,stroke,c,3,22,18,4,22,2,22,8,15,17,7,18,3,22);path(r,x,y,unit,stroke,c,10,14,15,14);path(r,x,y,unit,stroke,c,14,9,19,9);}
            case "chat" -> path(r,x,y,unit,stroke,c,3,4,21,4,21,17,9,17,4,22,4,17,3,17,3,4);
            case "cloud" -> {path(r,x,y,unit,stroke,c,5,19,2,16,3,12,7,11,7,7,11,4,16,5,18,10,21,11,23,16,20,19,5,19);}
            case "book" -> {path(r,x,y,unit,stroke,c,12,5,3,3,3,19,12,21,21,19,21,3,12,5,12,21);}
            case "refresh" -> {for(int i=0;i<20;i++){double a=i*Math.PI*1.6/20,b=a+Math.PI*1.6/20;line(r,x+unit*(12+8*Math.cos(a)),y+unit*(12+8*Math.sin(a)),x+unit*(12+8*Math.cos(b)),y+unit*(12+8*Math.sin(b)),stroke,c);}path(r,x,y,unit,stroke,c,21,4,21,12,15,12);}
            case "copy" -> {path(r,x,y,unit,stroke,c,8,7,21,7,21,21,8,21,8,7);path(r,x,y,unit,stroke,c,16,7,16,2,3,2,3,16,8,16);}
            case "paste" -> {path(r,x,y,unit,stroke,c,8,4,4,4,4,22,20,22,20,4,16,4);path(r,x,y,unit,stroke,c,8,2,16,2,16,7,8,7,8,2);}
            case "save" -> {path(r,x,y,unit,stroke,c,3,2,17,2,21,6,21,22,3,22,3,2);path(r,x,y,unit,stroke,c,7,2,7,8,16,8,16,2);path(r,x,y,unit,stroke,c,7,22,7,14,17,14,17,22);}
            case "folder" -> path(r,x,y,unit,stroke,c,2,5,9,5,12,8,22,8,22,21,2,21,2,5);
            case "download", "upload" -> {boolean down=key.equals("download");path(r,x,y,unit,stroke,c,12,3,12,17);path(r,x,y,unit,stroke,c,7,down?12:8,12,down?17:3,17,down?12:8);path(r,x,y,unit,stroke,c,3,17,3,22,21,22,21,17);}
            case "check" -> path(r,x,y,unit,stroke,c,3,12,9,19,21,4);
            case "info" -> {ring(r,x,y,unit,stroke,c,12,12,10);path(r,x,y,unit,stroke,c,12,10,12,18);ring(r,x,y,unit,stroke,c,12,6,1);}
            case "palette" -> {path(r,x,y,unit,stroke,c,12,2,5,4,2,11,3,17,9,22,13,21,12,17,14,15,20,15,22,11,20,5,12,2);ring(r,x,y,unit,stroke,c,7,8,1);ring(r,x,y,unit,stroke,c,13,6,1);ring(r,x,y,unit,stroke,c,18,10,1);}
            case "profile" -> {path(r,x,y,unit,stroke,c,2,8,12,2,22,8,12,14,2,8);path(r,x,y,unit,stroke,c,2,13,12,19,22,13);path(r,x,y,unit,stroke,c,2,18,12,24,22,18);}
            default -> {return false;}
        }
        return true;
    }
    private static void path(GuiRenderer r,double x,double y,double u,double st,Color c,double... points){
        for(int i=2;i<points.length;i+=2)line(r,x+points[i-2]*u,y+points[i-1]*u,x+points[i]*u,y+points[i+1]*u,st,c);
    }
    private static void ring(GuiRenderer r,double x,double y,double u,double st,Color c,double cx,double cy,double radius){circle(r,x+cx*u,y+cy*u,radius*u,st,c);}

}
