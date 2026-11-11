// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;

import java.util.*;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.*;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;

/** An actual theme studio, rather than Carbon's four category windows. */
public final class ReskinAppearanceScreen extends TabScreen {
    public ReskinAppearanceScreen(GuiTheme theme,Tab tab){super(theme,tab);}
    @Override public void initWidgets(){add(new Studio()).top().expandX();}
    @Override protected void onClosed(){super.onClosed();((ReskinTheme)theme).saveAppearance();}
    final class Studio extends WContainer {
        ReskinScrollPane styles,colors,options,icons;WButton skins,paint,layout,art,reset,nativeSettings;
        String selected="Colors";double mainX,mainW,top,bodyY,bodyH,leftW,rightX,rightW;boolean wide;
        @Override public void init(){
            var t=(ReskinTheme)theme;styles=add(new ReskinScrollPane()).top().widget();colors=add(new ReskinScrollPane()).top().widget();options=add(new ReskinScrollPane()).top().widget();icons=add(new ReskinScrollPane()).top().widget();
            for(var s:ReskinTheme.StylePreset.values())styles.add(new ReskinStyleCard(s,()->t.chooseStyle(s)));
            colors.background=false;colors.add(new ReskinHeading("CUSTOM PALETTE / RGBA"));
            colors.add(new ReskinColorField("Accent",t.accentColor));colors.add(new ReskinColorField("Surface",t.surfaceColor));colors.add(new ReskinColorField("Header",t.headerSurfaceColor));
            colors.add(new ReskinColorField("Primary text",t.textColor));colors.add(new ReskinColorField("Secondary text",t.textSecondaryColor));colors.add(new ReskinColorField("Title text",t.titleTextColor));colors.add(new ReskinColorField("Border",t.borderColor));
            options.add(new ReskinHeading("LAYOUT & INTERACTION"));
            // These are actual appearance settings; the native factories retain their original validation.
            options.add(new Option("Reference proportions",t.referenceProportions));options.add(new Option("Match Minecraft GUI scale",t.syncMinecraftScale));options.add(new Option("Hover reveal long text",t.marquee));options.add(new Option("Reduced motion",t.reducedMotion));options.add(new Option("Accent halo",t.glow));
            options.add(new SliderOption("UI density",t.density,.70,1.45));options.add(new SliderOption("Subtle corner radius (0 = square)",t.cornerRadius,0,4));options.add(new SliderOption("Maximum Neon columns",t.maxColumns,1,8));
            icons.add(new ReskinHeading("ICONS / NATIVE ARTWORK FIRST"));
            icons.add(new Option("Use provided category artwork",t.useCategoryIcons));
            icons.add(new Option("Distinct built-in module symbols",t.semanticModuleIcons));
            WButton reload=icons.add(theme.button("Reload icon providers")).expandX().widget();
            reload.action=()->{ReskinIcons.refresh();invalidate();};
            icons.add(new ReskinParagraph("Categories use their supplied artwork automatically. Addon modules inherit it; registered module icons take priority. Missing artwork uses a visible symbol, not a blank slot."));
            icons.add(new ReskinHeading("INSTALLED CATEGORIES"));
            for(var category:meteordevelopment.meteorclient.systems.modules.Modules.loopCategories())icons.add(new IconSample(category));
            art=button("Icons",()->{selected="Icons";invalidate();});
            skins=button("Skins",()->{selected="Skins";invalidate();});paint=button("Colors",()->{selected="Colors";invalidate();});layout=button("Layout",()->{selected="Layout";invalidate();});
            reset=button("Reset palette",()->{t.paintDefaultColors();t.saveAppearance();});nativeSettings=button("Meteor GUI",()->{for(Tab tab:Tabs.get())if(tab.name.equals("GUI")){ReskinAccess.later(()->tab.openScreen(theme));break;}});
        }
        WButton button(String text,Runnable action){WButton b=add(theme.button(text)).top().widget();b.action=action;return b;}
        @Override protected void onCalculateSize(){
            var t=(ReskinTheme)theme;width=t.logicalWidth();height=t.logicalHeight();top=t.topInset();mainX=t.sideInset()+t.scale(20);mainW=Math.max(1,width-mainX-t.scale(20));
            wide=mainW>=t.scale(890);leftW=wide?mainW*.53:mainW;rightX=mainX+(wide?leftW+t.scale(20):0);rightW=wide?mainW-leftW-t.scale(20):mainW;
            bodyY=top+t.scale(115);bodyH=Math.max(1,height-bodyY-t.bottomInset()-t.scale(14));
            styles.visible=wide||selected.equals("Skins");colors.visible=selected.equals("Colors");options.visible=selected.equals("Layout");icons.visible=selected.equals("Icons");
            styles.columns=Math.max(1,(int)(leftW/t.scale(250)));styles.bounds(leftW,bodyH);styles.calculateSize();
            colors.columns=rightW>=t.scale(540)?2:1;colors.bounds(rightW,bodyH);colors.calculateSize();options.bounds(rightW,bodyH);options.calculateSize();icons.bounds(rightW,bodyH);icons.calculateSize();
            for(var b:List.of(skins,paint,art,layout,reset,nativeSettings))b.calculateSize();
        }
        void place(WWidget w,double x,double y,double width,double height){w.x=x;w.y=y;w.width=width;w.height=height;w.calculateWidgetPositions();}
        @Override protected void onCalculateWidgetPositions(){
            var t=(ReskinTheme)theme;place(styles,mainX,bodyY,leftW,bodyH);place(colors,rightX,bodyY,rightW,bodyH);place(options,rightX,bodyY,rightW,bodyH);place(icons,rightX,bodyY,rightW,bodyH);
            double xx=wide?rightX:mainX,yy=top+t.scale(67);skins.visible=!wide;
            for(var b:List.of(skins,paint,art,layout))if(b.visible){double ww=theme.textWidth(b==skins?"Skins":b==paint?"Colors":b==art?"Icons":"Layout")+t.scale(25);place(b,xx,yy,ww,t.scale(33));xx+=ww+t.scale(7);}
            reset.visible=mainW>=t.scale(710);nativeSettings.visible=mainW>=t.scale(1080);
            place(reset,mainX+mainW-t.scale(160),top+t.scale(15),t.scale(150),t.scale(32));place(nativeSettings,mainX+mainW-t.scale(295),top+t.scale(15),t.scale(125),t.scale(32));
        }
        @Override protected boolean propagateEvents(WWidget w){return w.visible;}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){var t=(ReskinTheme)theme;ReskinDraw.workspace(r,t,mainX-t.scale(10),top-t.scale(4),mainW+t.scale(20),height-top-t.bottomInset());ReskinDraw.text(r,t,"Appearance studio",mainX,top+t.scale(10),Math.max(1,mainW-(reset.visible?t.scale(320):0)),t.titleTextColor.get(),true);ReskinDraw.text(r,t,"Eight layouts. Your colors.  /  "+t.styleTitle(),mainX,top+t.scale(38),mainW,t.textSecondaryColor.get(),false);if(wide)ReskinDraw.text(r,t,"CHOOSE YOUR WORKSPACE",mainX,top+t.scale(76),leftW,t.accentColor.get(),false);}
    }
    static final class IconSample extends WWidget implements ReskinReflow.WidthAware {
        final meteordevelopment.meteorclient.systems.modules.Category category;
        final ReskinText caption=new ReskinText(); double bound=300;
        IconSample(meteordevelopment.meteorclient.systems.modules.Category category){this.category=category;tooltip=category.name;}
        @Override public void availableWidth(double w){bound=w;}
        @Override protected void onCalculateSize(){width=bound;height=Math.max(theme.scale(64),theme.textHeight()*2+theme.scale(18));}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
            var t=(ReskinTheme)theme;ReskinDraw.panel(r,t,x,y,width,height,false);
            ReskinIcons.draw(r,t,category,null,category.name,x+t.scale(12),y+(height-t.scale(36))/2,t.scale(36));
            double left=x+t.scale(60),room=Math.max(1,width-t.scale(74));
            caption.draw(r,t,category.name,left,y+t.scale(10),room,t.textHeight(),false,t.textColor.get(),mouseOver,dt);
            ReskinDraw.text(r,t,ReskinIcons.categorySource(t,category),left,y+t.scale(14)+t.textHeight(),room,t.textSecondaryColor.get(),false);
        }
    }
    static final class Option extends WContainer {
        final String label;final meteordevelopment.meteorclient.settings.Setting<Boolean> value;meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox checkbox;
        Option(String label,meteordevelopment.meteorclient.settings.Setting<Boolean> value){this.label=label;this.value=value;}
        @Override public void init(){checkbox=add(theme.checkbox(value.get())).widget();checkbox.action=()->value.set(checkbox.checked);}
        @Override protected void onCalculateSize(){width=theme.scale(350);height=theme.scale(58);}
        @Override protected void onCalculateWidgetPositions(){checkbox.x=x+width-theme.scale(42);checkbox.y=y+(height-checkbox.height)/2;}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){var t=(ReskinTheme)theme;ReskinDraw.panel(r,t,x,y,width,height,false);ReskinDraw.text(r,t,label,x+t.scale(14),y+t.scale(20),Math.max(1,width-t.scale(70)),t.textColor.get(),false);}
    }
    static final class SliderOption extends WContainer {
        final String label;final meteordevelopment.meteorclient.settings.Setting<?> value;final double min,max;meteordevelopment.meteorclient.gui.widgets.input.WSlider slider;
        SliderOption(String label,meteordevelopment.meteorclient.settings.Setting<?> value,double min,double max){this.label=label;this.value=value;this.min=min;this.max=max;}
        @SuppressWarnings("unchecked") @Override public void init(){slider=add(theme.slider(((Number)value.get()).doubleValue(),min,max)).widget();slider.action=()->{if(value.get() instanceof Integer)((meteordevelopment.meteorclient.settings.Setting<Integer>)value).set((int)Math.round(slider.get()));else ((meteordevelopment.meteorclient.settings.Setting<Double>)value).set(slider.get());};}
        @Override protected void onCalculateSize(){width=theme.scale(350);height=theme.scale(94);}
        @Override protected void onCalculateWidgetPositions(){slider.x=x+theme.scale(17);slider.y=y+theme.scale(50);slider.width=Math.max(1,width-theme.scale(34));slider.calculateWidgetPositions();}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){var t=(ReskinTheme)theme;ReskinDraw.panel(r,t,x,y,width,height,false);String number=value.get() instanceof Integer?value.get().toString():String.format(java.util.Locale.ROOT,"%.2f",((Number)value.get()).doubleValue());ReskinDraw.text(r,t,label+"  /  "+number,x+t.scale(14),y+t.scale(16),width-t.scale(28),t.textColor.get(),false);}
    }
}
