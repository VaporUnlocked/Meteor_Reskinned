// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import net.minecraft.client.gui.screens.Screen;

/** A presentation-only appearance tab; not registered as a gameplay module. */
public final class ReskinAppearanceTab extends Tab {
    public ReskinAppearanceTab() { super("Appearance"); }
    @Override public TabScreen createScreen(GuiTheme theme) { return new ReskinAppearanceScreen(theme,this); }
    @Override public boolean isScreen(Screen screen) { return screen instanceof ReskinAppearanceScreen; }
}
