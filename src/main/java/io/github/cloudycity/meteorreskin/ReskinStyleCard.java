// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
/** Schematic layout thumbnail made from UI primitives, not a misleading game screenshot. */
final class ReskinStyleCard extends WPressable {
    final ReskinTheme.StylePreset style;final Runnable select;
    ReskinStyleCard(ReskinTheme.StylePreset style,Runnable select){this.style=style;this.select=select;tooltip=description(style);}
    static String description(ReskinTheme.StylePreset s){return switch(s){
        case OBSIDIAN_COMMAND->"Grouped accordion list + settings inspector";
        case AURORA_GLASS->"Floating glass cards + contextual settings";
        case NEON_DOCK->"Bottom dock + glowing category panels";
        case SLATE_SIDEBAR->"Left navigation + list + wide settings";
        case APEX_WORKSPACE->"Dense card grid + live library summary";
        case HORIZON_SPLIT->"Warm editorial header + broad settings";
        case ATLAS_PANELS->"Category, favorites, table and inspector";
        case PRISM_STUDIO->"Gradient studio + cards + style inspector";};}
    @Override protected void onCalculateSize(){width=theme.scale(245);height=theme.scale(172);}
    @Override protected void onPressed(int button){if(button==1)select.run();}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        var t=(ReskinTheme)theme;boolean selected=t.style()==style;ReskinDraw.panel(r,t,x,y,width,height,selected||mouseOver);
        double p=t.scale(13),sx=x+p,sy=y+p,sw=Math.max(1,width-2*p),sh=t.scale(92);
        Color panel=new Color(36,43,55),a=accent(style),base=new Color(13,17,25);
        ReskinDraw.round(r,sx,sy,sw,sh,0,base);
        double u=sw/100,v=sh/60;
        boolean sidebar=style==ReskinTheme.StylePreset.SLATE_SIDEBAR||style==ReskinTheme.StylePreset.PRISM_STUDIO;
        if(sidebar)r.quad(sx+3*u,sy+3*v,14*u,54*v,panel);else r.quad(sx+3*u,sy+(style==ReskinTheme.StylePreset.NEON_DOCK?51:3)*v,94*u,7*v,panel);
        double yy=style==ReskinTheme.StylePreset.NEON_DOCK?4:sidebar?4:14;
        if(style==ReskinTheme.StylePreset.NEON_DOCK){for(int i=0;i<4;i++){r.quad(sx+(4+i*24)*u,sy+yy*v,21*u,42*v,panel);r.quad(sx+(4+i*24)*u,sy+yy*v,21*u,3*v,a);}}
        else if(style==ReskinTheme.StylePreset.ATLAS_PANELS){for(int i=0;i<4;i++)r.quad(sx+(3+i*24)*u,sy+yy*v,21*u,42*v,panel);}
        else{
            double start=sidebar?21:4;r.quad(sx+start*u,sy+yy*v,15*u,(55-yy)*v,panel);
            boolean cards=style==ReskinTheme.StylePreset.APEX_WORKSPACE||style==ReskinTheme.StylePreset.AURORA_GLASS||style==ReskinTheme.StylePreset.PRISM_STUDIO;
            if(cards){for(int row=0;row<3;row++)for(int col=0;col<2;col++)r.quad(sx+(start+18+col*18)*u,sy+(yy+row*13)*v,15*u,10*v,panel);}
            else{for(int row=0;row<5;row++)r.quad(sx+(start+18)*u,sy+(yy+row*8)*v,31*u,5*v,panel);}
            r.quad(sx+76*u,sy+yy*v,20*u,(55-yy)*v,panel);r.quad(sx+start*u,sy+yy*v,15*u,2*v,a);
        }
        ReskinDraw.text(r,t,style.title,x+p,y+t.scale(118),width-2*p,t.titleTextColor.get(),false);
        ReskinDraw.text(r,t,description(style),x+p,y+t.scale(143),width-2*p,t.textSecondaryColor.get(),false);
    }
    static Color accent(ReskinTheme.StylePreset s){return switch(s){case OBSIDIAN_COMMAND,AURORA_GLASS,SLATE_SIDEBAR->new Color(90,229,204);case NEON_DOCK->new Color(235,70,255);case APEX_WORKSPACE->new Color(41,236,150);case HORIZON_SPLIT->new Color(244,177,89);case ATLAS_PANELS->new Color(74,203,255);case PRISM_STUDIO->new Color(202,144,255);};}
}
