// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.WQuad;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.screens.settings.ColorSettingScreen;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.screens.Screen;

/** The Meteor Reskin appearance workspace. */
public final class ReskinAppearanceScreen extends TabScreen {
    public ReskinAppearanceScreen(GuiTheme theme,Tab tab) { super(theme,tab); }
    @Override protected void onClosed(){super.onClosed();((ReskinTheme)theme).saveAppearance();}
    @Override public void initWidgets() { add(new AppearanceGrid()).top().expandX(); }

    private final class AppearanceGrid extends WContainer {
        private final List<Cell<WWindow>> panels=new ArrayList<>();
        private WButton prev,next,reset;
        private int page,columns=4,pages=1;
        private double margin,gap,top,panelWidth,availableHeight;
        private ReskinTheme t;

        @Override public void init() {
            t=(ReskinTheme)theme;
            WWindow colors=create("Colors");
            colors.add(theme.label("STYLE PRESETS")).padBottom(5);
            styleButtons(colors);
            colors.add(theme.horizontalSeparator("Custom colors")).expandX().padTop(10);
            color(colors,"Accent",t.accentColor);
            color(colors,"Surface",t.surfaceColor);
            color(colors,"Header",t.headerSurfaceColor);
            color(colors,"Text",t.textColor);
            color(colors,"Secondary text",t.textSecondaryColor);
            color(colors,"Title text",t.titleTextColor);
            color(colors,"Border",t.borderColor);
            

            WWindow typography=create("Typography");
            typography.add(theme.label("READABLE, SCALABLE TEXT")).padBottom(6);
            typography.add(theme.label("Labels automatically scroll on hover when their full text cannot fit in the available width.",
                Math.min(t.logicalWidth()/3.0,t.scale(300)))).expandX();
            toggle(typography,"Hover reveal long text",t.marquee);
            typography.add(theme.label("Active style: " + t.styleTitle())).padTop(6);
            typography.add(theme.horizontalSeparator("Sample")).expandX().padTop(10);
            typography.add(theme.label("Meteor Reskin • Cloudy_City",true)).padTop(4);
            typography.add(theme.label("AaBbCcDd 0123456789")).padTop(4);
            typography.add(theme.label("Use GUI Scale in Minecraft's Video Settings.")).padTop(6);

            WWindow layout=create("Layout");
            layout.add(theme.label("RESPONSIVE SCREEN FIT")).padBottom(6);
            toggle(layout,"Match Minecraft GUI scale",t.syncMinecraftScale);
            toggle(layout,"Use addon category icons",t.useCategoryIcons);
            slideInt(layout,"Corner radius",t.cornerRadius,0,16);
            slide(layout,"UI density",t.density,.70,1.45);
            slideInt(layout,"Max category columns",t.maxColumns,1,8);
            layout.add(theme.horizontalSeparator("Sizing")).expandX().padTop(12);
            layout.add(theme.label("Category columns use the available screen width. Overflow remains reachable with page controls and vertical scrolling.",Math.min(t.logicalWidth()/3.0,t.scale(300)))).expandX();

            WWindow motion=create("Motion & panels");
            motion.add(theme.label("INTERACTION & NAVIGATION")).padBottom(6);
            toggle(motion,"Reduced motion",t.reducedMotion);
            toggle(motion,"Neon accent halo",t.glow);
            toggle(motion,"Quick columns on other pages",t.shelfEnabled);
            motion.add(theme.horizontalSeparator("Actions")).expandX().padTop(9);
            WButton resetColors=motion.add(theme.button("Reset style colors")).expandX().padTop(5).widget();
            resetColors.action=()->{t.paintDefaultColors();t.refresh();};
            WButton show=motion.add(theme.button("Open quick columns")).expandX().padTop(6).widget();
            show.action=()->{t.shelfEnabled.set(true);t.refresh();};
            motion.add(theme.label("Changes apply to Meteor's existing menu controls. No gameplay settings, modules, or cheats are modified.",Math.min(t.logicalWidth()/3.0,t.scale(300)))).expandX().padTop(8);

            prev=add(theme.button("< Prev")).top().expandWidgetX().widget();
            next=add(theme.button("Next >")).top().expandWidgetX().widget();
            reset=add(theme.button("Meteor GUI settings")).top().expandWidgetX().widget();
            prev.action=()->{if(page>0){page--;invalidate();}};
            next.action=()->{if(page<pages-1){page++;invalidate();}};
            reset.action=()->{for(Tab tab:meteordevelopment.meteorclient.gui.tabs.Tabs.get())
                if(tab.name.equals("GUI")){tab.openScreen(theme);break;}};
        }
        private WWindow create(String name){
            Cell<WWindow> cell=ReskinWindows.addDocked(this,name,12,7);
            panels.add(cell);
            WWindow w=cell.widget();
            return w;
        }
        private void color(WWindow panel,String name,Setting<SettingColor> setting){
            panel.add(theme.label(name)).padTop(4);
            WHorizontalList row=panel.add(theme.horizontalList()).expandX().widget();
            WQuad swatch=row.add(theme.quad(setting.get())).padRight(5).widget();
            WTextBox hex=row.add(theme.textBox(hex(setting.get()))).expandX().widget();
            hex.action=()->{
                String h=hex.get().strip();
                if(h.startsWith("#"))h=h.substring(1);
                if(!h.matches("(?i)[0-9a-f]{6}([0-9a-f]{2})?"))return;
                try{
                    int r=Integer.parseInt(h.substring(0,2),16);
                    int g=Integer.parseInt(h.substring(2,4),16);
                    int b=Integer.parseInt(h.substring(4,6),16);
                    int a=h.length()==8?Integer.parseInt(h.substring(6,8),16):255;
                    setting.set(new SettingColor(r,g,b,a));
                    swatch.color=setting.get();
                }catch(NumberFormatException ignored){}
            };
            WButton pick=row.add(theme.button("Pick")).widget();
            pick.action=()->openScreen(new ColorSettingScreen(theme,setting));
        }
        private String hex(SettingColor c){return String.format("#%02X%02X%02X%02X",c.r,c.g,c.b,c.a);}
        private void toggle(WWindow panel,String name,Setting<Boolean> setting){
            WHorizontalList row=panel.add(theme.horizontalList()).expandX().padTop(6).widget();
            row.add(theme.label(name)).expandCellX();
            WCheckbox cb=row.add(theme.checkbox(setting.get())).right().widget();
            cb.action=()->{setting.set(cb.checked);t.refresh();};
        }
        private void slide(WWindow panel,String name,Setting<Double> setting,double lo,double hi){
            panel.add(theme.label(name)).padTop(10);
            WSlider slider=panel.add(theme.slider(setting.get(),lo,hi)).expandX().widget();
            slider.action=()->{setting.set(slider.get());t.refresh();};
        }
        private void slideInt(WWindow panel,String name,Setting<Integer> setting,int lo,int hi){
            panel.add(theme.label(name)).padTop(10);
            WSlider slider=panel.add(theme.slider(setting.get(),lo,hi)).expandX().widget();
            slider.action=()->{setting.set((int)Math.round(slider.get()));t.refresh();};
        }
        private void styleButtons(WWindow panel){
            for (ReskinTheme.StylePreset preset : ReskinTheme.StylePreset.values()) {
                WButton button = panel.add(theme.button(preset.title)).expandX().widget();
                button.action = () -> t.chooseStyle(preset);
            }
        }
        private void openScreen(Screen screen){
            try{
                Object gui=MeteorClient.mc.getClass().getField("gui").get(MeteorClient.mc);
                Method method=gui.getClass().getMethod("setScreen",Screen.class);
                method.invoke(gui,screen);
            }catch(ReflectiveOperationException ignored){}
        }
        @Override protected void onCalculateSize(){
            width=Utils.getWindowWidth();height=Utils.getWindowHeight();
            margin=Math.max(8,t.scale(14));gap=Math.max(8,t.scale(12));
            top=t.topInset()+t.scale(52);
            double inner=Math.max(1,width-t.sideInset()-margin*2);
            columns=Math.max(1,Math.min(4,(int)Math.floor((inner+gap)/(t.scale(256)+gap))));
            panelWidth=Math.max(1,(inner-gap*(columns-1))/columns);
            availableHeight=Math.max(t.scale(55),height-top-t.bottomInset());
            pages=Math.max(1,(panels.size()+columns-1)/columns);
            page=Math.max(0,Math.min(page,pages-1));
            for(Cell<WWindow> cell:panels){
                WWindow w=cell.widget();
                ReskinWindows.measure(w,panelWidth,availableHeight);
            }
            prev.calculateSize();next.calculateSize();reset.calculateSize();
        }
        @Override protected void onCalculateWidgetPositions(){
            for(Cell<WWindow> p:panels)p.widget().visible=false;
            int first=page*columns;
            for(int i=first;i<panels.size()&&i<first+columns;i++){
                Cell<WWindow> c=panels.get(i); c.widget().visible=true;
                c.x=t.sideInset()+margin+(i-first)*(panelWidth+gap);c.y=top;
                c.width=panelWidth;c.height=c.widget().height;c.alignWidget();
            }
            locate(prev,t.sideInset()+margin,t.topInset()+t.scale(8),t.scale(65),t.scale(27));
            locate(next,t.sideInset()+margin+t.scale(72),t.topInset()+t.scale(8),t.scale(70),t.scale(27));
            locate(reset,width-margin-t.scale(185),t.topInset()+t.scale(8),t.scale(185),t.scale(27));
            prev.visible=page>0;next.visible=page<pages-1;
        }
        private void locate(WWidget w,double x,double y,double ww,double hh){
            for(Cell<?> c:cells)if(c.widget()==w){c.x=x;c.y=y;c.width=ww;c.height=hh;c.alignWidget();break;}
        }
        @Override protected void onRender(GuiRenderer r,double mx,double my,double delta){
            r.quad(t.sideInset(),t.topInset(),width-t.sideInset(),t.scale(46),t.headerSurfaceColor.get());
            String label="METEOR RESKIN  •  " + t.styleTitle() + "  •  " + (page+1) + " / " + pages;
            if(t.sideInset()+margin+t.scale(155)+t.textWidth(label)<reset.x-t.scale(10))
                r.text(label,t.sideInset()+margin+t.scale(155),t.topInset()+t.scale(14),t.textColor.get(),false);
            
        }
        @Override protected boolean propagateEvents(WWidget w){return w.visible;}
    }
}
