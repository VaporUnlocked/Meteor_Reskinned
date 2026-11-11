// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Settings;
/** Retain every stock setting type, but avoid pinning a form to its initial full text width. */
public final class ReskinSettingsFactory extends DefaultSettingsWidgetFactory {
    public ReskinSettingsFactory(GuiTheme t){super(t);}
    @Override public WWidget create(GuiTheme theme, Settings settings, String filter) {
        WWidget result=super.create(theme,settings,filter);
        result.minWidth=0;
        return result;
    }
}
