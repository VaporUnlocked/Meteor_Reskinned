// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import java.util.*;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
final class ReskinParagraph extends WWidget implements ReskinReflow.WidthAware {
    final String text;double available=300;List<String> lines=List.of();
    ReskinParagraph(String text){this.text=text;}
    public void availableWidth(double w){available=Math.max(1,w);}
    @Override protected void onCalculateSize(){width=available;lines=ReskinWrap.lines(theme,text,available,false);height=lines.size()*(theme.textHeight()+theme.scale(4))+theme.scale(8);}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){var t=(ReskinTheme)theme;double yy=y;for(String line:lines){r.text(line,x,yy,t.textSecondaryColor.get(),false);yy+=theme.textHeight()+theme.scale(4);}}
}
