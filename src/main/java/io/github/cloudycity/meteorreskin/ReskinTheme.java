// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import java.lang.reflect.Field;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.Utils;

/** Meteor Reskin is a presentation-only GuiTheme. Existing gameplay behavior is untouched. */
public final class ReskinTheme extends MeteorGuiTheme {
    public enum StylePreset {
        OBSIDIAN_COMMAND("Obsidian Command"),
        AURORA_GLASS("Aurora Glass"),
        NEON_DOCK("Neon Dock"),
        SLATE_SIDEBAR("Slate Sidebar"),
        APEX_WORKSPACE("Apex Workspace"),
        HORIZON_SPLIT("Horizon Split"),
        ATLAS_PANELS("Atlas Panels"),
        PRISM_STUDIO("Prism Studio");

        public final String title;
        StylePreset(String title) { this.title = title; }
        @Override public String toString() { return title; }
    }

    private final SettingGroup reskinSettings = settings.createGroup("Meteor Reskin");
    public final Setting<StylePreset> stylePreset = reskinSettings.add(new EnumSetting.Builder<StylePreset>()
        .name("style-preset").description("Choose which Meteor Reskin concept style to use.")
        .defaultValue(StylePreset.OBSIDIAN_COMMAND)
        .onChanged(v -> refresh())
        .build());
    public final Setting<Boolean> referenceProportions = reskinSettings.add(new BoolSetting.Builder()
        .name("reference-proportions").description("Keep the approved skin proportions and readable card counts even with small GUI scales. Disable for dense layouts.")
        .defaultValue(true).onChanged(v -> refresh()).build());
    public final Setting<Boolean> useCategoryIcons = reskinSettings.add(new BoolSetting.Builder()
        .name("addon-category-icons").description("Prefer the icons supplied by Meteor and installed addons. Missing artwork uses a vector fallback.")
        .defaultValue(true).build());
    public final Setting<Boolean> semanticModuleIcons = reskinSettings.add(new BoolSetting.Builder()
        .name("distinct-native-module-icons").description("Use distinct symbols for recognized built-in Meteor modules. Addon modules inherit their real category artwork unless an icon is registered.")
        .defaultValue(true).build());
    public final Setting<Boolean> glow = reskinSettings.add(new BoolSetting.Builder()
        .name("accent-glow").description("Subtle accent halo in Neon Dock. No shader dependency.")
        .defaultValue(true).build());
    public final Setting<Integer> cornerRadius = reskinSettings.add(new IntSetting.Builder()
        .name("corner-radius").description("Panel and button corner radius.")
        .defaultValue(0).min(0).sliderRange(0,4).onChanged(v -> refresh()).build());
    public final Setting<Boolean> syncMinecraftScale = reskinSettings.add(new BoolSetting.Builder()
        .name("match-minecraft-gui-scale").description("Use Minecraft's effective GUI scale, including Auto.")
        .defaultValue(true).onChanged(v -> refresh()).build());
    public final Setting<Double> density = reskinSettings.add(new DoubleSetting.Builder()
        .name("ui-density").description("Change the spacing and size of Meteor Reskin controls.")
        .defaultValue(1.0).min(0.70).sliderRange(0.70,1.45)
        .onSliderRelease().onChanged(v -> refresh()).build());
    public final Setting<Boolean> marquee = reskinSettings.add(new BoolSetting.Builder()
        .name("hover-to-reveal-text").description("Reveal clipped labels on hover.")
        .defaultValue(true).build());
    public final Setting<Boolean> reducedMotion = reskinSettings.add(new BoolSetting.Builder()
        .name("reduced-motion").description("Disable animated text movement.")
        .defaultValue(false).build());
    public final Setting<Integer> maxColumns = reskinSettings.add(new IntSetting.Builder()
        .name("maximum-columns").description("Maximum number of category columns on the Modules page.")
        .defaultValue(4).min(1).sliderRange(1,8).onChanged(v -> refresh()).build());
    public final Setting<SettingColor> surfaceColor = reskinSettings.add(new ColorSetting.Builder()
        .name("surface").description("Panel surface color and transparency.")
        .defaultValue(new SettingColor(14,18,24,242))
        .onChanged(v -> { backgroundColor.get().set(v); refresh(); }).build());
    public final Setting<SettingColor> headerSurfaceColor = reskinSettings.add(new ColorSetting.Builder()
        .name("header-surface").description("Panel header color and transparency.")
        .defaultValue(new SettingColor(24,30,39,255)).onChanged(v -> refresh()).build());
    public final Setting<SettingColor> borderColor = reskinSettings.add(new ColorSetting.Builder()
        .name("border").description("Window and separator border color.")
        .defaultValue(new SettingColor(63,84,106,255))
        .onChanged(v -> { outlineColor.get().set(v); refresh(); }).build());

    private final ReskinAppearanceTab appearanceTab = new ReskinAppearanceTab();

    public ReskinTheme() {
        super();
        try {
            Field f = GuiTheme.class.getDeclaredField("name");
            f.setAccessible(true);
            f.set(this, "Meteor Reskin");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to register Meteor Reskin", e);
        }
        settingsFactory = new ReskinSettingsFactory(this);
        paintDefaults();
        applyStylePreset(stylePreset.get());
    }

    public ReskinAppearanceTab appearanceTab() { return appearanceTab; }
    public StylePreset style() { return stylePreset.get(); }
    public String styleTitle() { return style().title; }
    public String authorTitle() { return "BY Cloudy_City"; }
    /** Explicit user action. Loading a saved style never overwrites custom colors. */
    public void chooseStyle(StylePreset preset) {
        stylePreset.set(preset);
        applyStylePreset(preset);
        cornerRadius.set(0);
        meteordevelopment.meteorclient.gui.GuiThemes.save();
        ReskinAccess.reopenCurrent(this);
    }
    /** Structure follows the viewport; type and native controls still use Meteor's effective scale.
     * This prevents a saved 0.75 GUI scale from doubling the card/column count. */
    public double composition(double value) {
        if(referenceProportions == null || !referenceProportions.get()) return scale(value);
        double frame = Math.min(logicalWidth()/1666.0, logicalHeight()/896.0);
        return value * Math.max(scale(1), frame);
    }
    public int compositionColumns(double usable, double gap, double minimum, int designMaximum) {
        int fit=Math.max(1,(int)Math.floor((usable+gap)/(minimum+gap)));
        return referenceProportions.get()?Math.min(fit,designMaximum):fit;
    }
    public double navigationHeight(){return composition(logicalWidth()<composition(1180)||style()==StylePreset.APEX_WORKSPACE?112:82);}
    public double topInset(){return sideInset()>0?composition(28):style()==StylePreset.NEON_DOCK?composition(24):navigationHeight()+composition(28);}
    public double bottomInset(){return style()==StylePreset.NEON_DOCK?navigationHeight()+composition(30):composition(34);}
    public double sideInset(){
        if((style()!=StylePreset.SLATE_SIDEBAR && style()!=StylePreset.PRISM_STUDIO)||logicalWidth()<composition(950))return 0;
        double caption=textWidth("Appearance");
        for(var tab:meteordevelopment.meteorclient.gui.tabs.Tabs.get())caption=Math.max(caption,textWidth(tab.name));
        return Math.max(composition(250),Math.min(logicalWidth()*.28,Math.ceil(caption+scale(110))));
    }
    public void saveAppearance(){meteordevelopment.meteorclient.gui.GuiThemes.save();}

    /** Restore only the appearance palette and leave Meteor's other configuration untouched. */
    public void paintDefaultColors() {
        applyStylePreset(style());
        refresh();
    }

    private void paintDefaults() {
        set(textColor.get(), 232, 238, 244, 255);
        set(textSecondaryColor.get(), 163, 176, 191, 255);
        set(titleTextColor.get(), 245, 249, 252, 255);
        set(backgroundColor.get(false, false), 14, 18, 24, 242);
        set(backgroundColor.get(false, true), 26, 34, 44, 250);
        set(backgroundColor.get(true, true), 48, 72, 98, 255);
        set(outlineColor.get(false, false), 63, 84, 106, 255);
        set(outlineColor.get(false, true), 88, 125, 171, 255);
        set(moduleBackground.get(), 34, 45, 58, 255);
        set(separatorCenter.get(), 65, 82, 104, 255);
        set(separatorEdges.get(), 39, 48, 60, 180);
        set(scrollbarColor.get(false, false), 74, 92, 116, 210);
        set(scrollbarColor.get(false, true), 98, 132, 176, 255);
    }
    private static void set(SettingColor c, int r, int g, int b, int a) { c.set(r, g, b, a); }

    /** Instant appearance presets, never a change to any module setting or game state. */
    public void applyStylePreset(StylePreset preset) {
        switch (preset) {
            case OBSIDIAN_COMMAND -> {
                applyColors(81, 224, 192, 17, 23, 25, 30, 39, 41, 69, 87, 87);
            }
            case AURORA_GLASS -> {
                applyColors(96,238,221,24,36,45,40,57,65,88,120,130);
                surfaceColor.get().a=228;
            }
            case NEON_DOCK -> {
                applyColors(238, 76, 255, 19, 12, 28, 33, 18, 48, 140, 56, 160);
            }
            case SLATE_SIDEBAR -> {
                applyColors(132, 241, 210, 18, 22, 28, 31, 38, 46, 88, 106, 109);
            }
            case APEX_WORKSPACE -> { applyColors(41,236,150,12,16,20,20,26,30,46,91,70); }
            case HORIZON_SPLIT -> { applyColors(244,177,89,25,24,25,44,38,36,116,91,63); }
            case ATLAS_PANELS -> { applyColors(74,203,255,13,23,36,25,42,61,60,99,140); }
            case PRISM_STUDIO -> { applyColors(202,144,255,27,22,42,42,33,63,104,79,139); }
        }
    }

    private void applyColors(int ar, int ag, int ab, int sr, int sg, int sb, int hr, int hg, int hb, int br, int bg, int bb) {
        accentColor.get().set(ar, ag, ab, 255);
        checkboxColor.get().set(ar, ag, ab, 255);
        sliderHandle.get().set(ar, ag, ab, 255);
        sliderLeft.get().set(Math.max(0, Math.min(255, (int) (ar * 0.72))), Math.max(0, Math.min(255, (int) (ag * 0.72))), Math.max(0, Math.min(255, (int) (ab * 0.72))), 255);
        sliderRight.get().set(Math.max(0, sr - 4), Math.max(0, sg - 3), Math.max(0, sb - 2), 255);
        textColor.get().set(232, 238, 244, 255);
        textSecondaryColor.get().set(163, 176, 191, 255);
        titleTextColor.get().set(245, 249, 252, 255);
        backgroundColor.get().set(sr, sg, sb, 242);
        backgroundColor.get(false, false).set(sr, sg, sb, 242);
        backgroundColor.get(false, true).set(hr, hg, hb, 250);
        backgroundColor.get(true, true).set(Math.min(255, hr + 18), Math.min(255, hg + 18), Math.min(255, hb + 18), 255);
        outlineColor.get(false, false).set(br, bg, bb, 255);
        outlineColor.get(false, true).set(Math.min(255, br + 26), Math.min(255, bg + 26), Math.min(255, bb + 26), 255);
        moduleBackground.get().set(hr, hg, hb, 255);
        separatorCenter.get().set(br, bg, bb, 255);
        separatorEdges.get().set(Math.max(0, br - 18), Math.max(0, bg - 18), Math.max(0, bb - 18), 180);
        scrollbarColor.get(false, false).set(br, bg, bb, 210);
        scrollbarColor.get(false, true).set(Math.min(255, br + 22), Math.min(255, bg + 22), Math.min(255, bb + 22), 255);
        surfaceColor.get().set(sr, sg, sb, 242);
        headerSurfaceColor.get().set(hr, hg, hb, 255);
        borderColor.get().set(br, bg, bb, 255);
    }

    public void refresh() {
        try {
            Object gui = MeteorClient.mc.getClass().getField("gui").get(MeteorClient.mc);
            Object screen = gui.getClass().getMethod("screen").invoke(gui);
            if (screen instanceof meteordevelopment.meteorclient.gui.WidgetScreen ws) ws.invalidate();
        } catch (ReflectiveOperationException | RuntimeException ignored) { }
    }

    @Override public void beforeRender() {
        super.beforeRender();
        if(cornerRadius.get()>4)cornerRadius.set(0); // Migrate the rejected legacy radius, retaining custom colors.
        backgroundColor.get().set(surfaceColor.get());
        placeholderColor.get().set(textSecondaryColor.get());
        outlineColor.get().set(borderColor.get());
        checkboxColor.get().set(accentColor.get());
        sliderHandle.get().set(accentColor.get());
    }
    @Override public double scale(double value) {
        double base = super.scale(1.0) * (density == null ? 1.0 : density.get());
        if (syncMinecraftScale != null && syncMinecraftScale.get()) {
            try {
                int gui = MeteorClient.mc.getWindow().getGuiScale();
                base *= Math.max(.75, Math.min(2.5, gui / 2.0));
            } catch (RuntimeException ignored) {}
        }
        try {
            double w = Utils.getWindowWidth(), h = Utils.getWindowHeight();
            double fit = Math.max(.68, Math.min(w / 460.0, h / 340.0));
            base = Math.min(base, fit);
        } catch (RuntimeException ignored) {}
        return value * base;
    }

    @Override public boolean isModulesScreen(net.minecraft.client.gui.screens.Screen screen) {
        return screen instanceof ReskinModulesScreen || super.isModulesScreen(screen);
    }
    @Override public WTopBar topBar() { return w(new ReskinTopBar()); }
    @Override public TabScreen modulesScreen() { return new ReskinModulesScreen(this); }
    @Override public WWindow window(WWidget icon, String title) { return w(new ReskinWindow(icon, title)); }
    @Override public WWidget module(Module module, String title) { return w(new ReskinModule(module, title)); }
    @Override public WLabel label(String text, boolean title, double maxWidth) {
        if (maxWidth == 0 && !text.contains("\n")) return w(new ReskinLabel(text, title));
        return super.label(text, title, maxWidth);
    }
    @Override protected WButton button(String text, GuiTexture icon) { return w(new ReskinButton(text, icon)); }
    @Override public WView view() { return w(new ReskinView()); }
    @Override public meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox checkbox(boolean checked) {
        return w(new ReskinCheckbox(checked));
    }
    @Override public <T> meteordevelopment.meteorclient.gui.widgets.input.WDropdown<T> dropdown(T[] values,T value) {
        return w(new ReskinDropdown<>(values,value));
    }
    @Override public meteordevelopment.meteorclient.gui.widgets.WTooltip tooltip(String text){return w(new ReskinTooltip(text));}
    public Color surface() { return surfaceColor.get(); }
    public Color border() { return outlineColor.get(); }
    public int logicalWidth() { return Utils.getWindowWidth(); }
    public int logicalHeight() { return Utils.getWindowHeight(); }
}
