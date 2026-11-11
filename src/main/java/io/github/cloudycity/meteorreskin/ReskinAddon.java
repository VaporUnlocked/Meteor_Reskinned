// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.PostInit;
import java.io.File;
import java.io.IOException;

/** Pure presentation addon: no modules, commands, keybinds, or gameplay hooks. */
public final class ReskinAddon extends MeteorAddon {
    private static final String THEME_NAME = "Meteor Reskin";
    private static final String ACTIVATION_MARKER = "meteor-reskin-v1-activated.flag";

    @Override
    public void onInitialize() {
        GuiThemes.add(new ReskinTheme());
        System.out.println("[Meteor Reskin] " + ReskinBuild.VERSION + " registered UI theme; waiting for Meteor GUI configuration to load.");
    }

    @PostInit
    public static void activateAfterGuiConfiguration() {
        try {
            File flag = new File(MeteorClient.FOLDER, ACTIVATION_MARKER);
            boolean shouldActivate = !flag.isFile();
            if (shouldActivate) {
                GuiThemes.select(THEME_NAME);
                if (!(GuiThemes.get() instanceof ReskinTheme)) {
                    System.err.println("[Meteor Reskin] Auto-selection failed: theme did not become active.");
                    return;
                }
                GuiThemes.save();
                File folder = flag.getParentFile();
                if (folder != null && !folder.exists() && !folder.mkdirs()) {
                    System.err.println("[Meteor Reskin] Unable to create activation marker folder: " + folder);
                } else if (!flag.createNewFile()) {
                    System.err.println("[Meteor Reskin] Unable to create activation marker: " + flag);
                }
                System.out.println("[Meteor Reskin] Theme activated after Meteor GUI configuration was restored.");
            } else {
                System.out.println("[Meteor Reskin] Theme registered. Current theme: " + (GuiThemes.get() == null ? "none" : GuiThemes.get().name));
            }
        } catch (IOException | SecurityException e) {
            System.err.println("[Meteor Reskin] Could not persist first-run theme selection: " + e);
        }
    }

    @Override public String getPackage() { return "io.github.cloudycity.meteorreskin"; }
    
}
