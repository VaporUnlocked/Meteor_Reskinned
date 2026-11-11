// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;

import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/** A staged hex editor: valid partial typing cannot accidentally commit the six-digit prefix of RGBA. */
final class ReskinColorField extends WContainer implements ReskinReflow.WidthAware {
    final String title;final Setting<SettingColor> setting;WTextBox input;WButton apply,pick;
    double bound=400;boolean pending,invalid,updating;
    ReskinColorField(String title,Setting<SettingColor> setting){this.title=title;this.setting=setting;}
    String hex(){var c=setting.get();return new ReskinColorDraft(c.r,c.g,c.b,c.a).hex();}
    @Override public void init(){
        input=add(theme.textBox(hex())).widget();input.action=()->{if(!updating){pending=true;invalid=false;}};
        apply=add(theme.button("Apply")).widget();apply.action=this::commit;
        pick=add(theme.button("Picker")).widget();pick.action=()->ReskinAccess.later(()->{
            var screen=new ReskinColorScreen((ReskinTheme)theme,setting);
            screen.onClosed(()->{pending=false;invalid=false;sync();});ReskinAccess.open(screen);
        });
    }
    void sync(){if(updating)return;updating=true;try{input.set(hex());}finally{updating=false;}}
    boolean commit(){
        var c=setting.get();var draft=new ReskinColorDraft(c.r,c.g,c.b,c.a);
        if(!draft.parseHex(input.get())){invalid=true;return false;}
        SettingColor replacement=new SettingColor(draft.r(),draft.g(),draft.b(),draft.a());replacement.rainbow=c.rainbow;
        if(!setting.set(replacement)){invalid=true;return false;}
        pending=false;invalid=false;sync();((ReskinTheme)theme).saveAppearance();return true;
    }
    @Override public void availableWidth(double width){bound=width;}
    @Override protected void onCalculateSize(){width=bound;height=theme.scale(116);}
    @Override protected void onCalculateWidgetPositions(){
        double p=theme.scale(14),xx=x+p,inner=Math.max(1,width-2*p),yy=y+theme.scale(39);
        locate(input,xx+theme.scale(33),yy,Math.max(1,inner-theme.scale(33)),theme.scale(29));
        double gap=theme.scale(9),w=(inner-gap)/2;locate(pick,xx,yy+theme.scale(37),w,theme.scale(28));locate(apply,xx+w+gap,yy+theme.scale(37),w,theme.scale(28));
    }
    void locate(meteordevelopment.meteorclient.gui.widgets.WWidget w,double x,double y,double width,double height){w.x=x;w.y=y;w.width=width;w.height=height;w.calculateWidgetPositions();}
    @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
        var t=(ReskinTheme)theme;if(!pending&&!input.isFocused()&&!input.get().equals(hex()))sync();
        ReskinDraw.panel(r,t,x,y,width,height,false);ReskinDraw.text(r,t,title+(invalid?" · invalid hex":pending?" · unapplied":""),x+t.scale(14),y+t.scale(13),width-t.scale(28),invalid?new meteordevelopment.meteorclient.utils.render.color.Color(255,150,140):t.textColor.get(),false);
        ReskinDraw.round(r,x+t.scale(14),y+t.scale(39),t.scale(26),t.scale(29),t.scale(5),setting.get());
    }
}
