// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;

import java.util.*;
import meteordevelopment.meteorclient.gui.*;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.*;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.settings.*;

/** Retains native/custom factory callbacks with compact inline simple controls and vertically reflowed complex editors. */
public final class ReskinSettingsFactory extends DefaultSettingsWidgetFactory {
    public ReskinSettingsFactory(GuiTheme theme){super(theme);}
    @Override public WWidget create(GuiTheme theme,Settings settings,String filter){
        Form form=new Form(settings,filter==null?"":filter);form.theme=theme;return form;
    }
    final class Form extends WContainer implements ReskinReflow.WidthAware {
        final Settings settings;final String filter;double bound=480;
        Form(Settings settings,String filter){this.settings=settings;this.filter=filter;}
        @Override public void availableWidth(double width){bound=Math.max(1,width);}
        @Override public void init(){
            for(SettingGroup group:settings){
                List<Setting<?>> matches=new ArrayList<>();for(Setting<?> s:group)if(ReskinSearch.matches(filter,s.title,s.name))matches.add(s);
                if(matches.isEmpty())continue;
                add(new GroupHeader(group)).top();
                for(Setting<?> s:matches)add(new Row(group,s)).top().expandX();
            }
        }
        @Override protected void onCalculateSize(){
            width=bound;height=0;double gap=theme.scale(3);
            for(var c:cells){WWidget w=c.widget();if(w instanceof Row row){w.visible=row.group.sectionExpanded&&row.setting.isVisible();row.setting.lastWasVisible=row.setting.isVisible();}
                if(!w.visible)continue;ReskinReflow.measure(w,bound);height+=w.height+gap;
            }
        }
        @Override public void calculateWidgetPositions(){x=Math.round(x);y=Math.round(y);onCalculateWidgetPositions();}
        @Override protected void onCalculateWidgetPositions(){double yy=y;for(var c:cells)if(c.widget().visible){WWidget w=c.widget();c.x=x;c.y=yy;c.width=width;c.height=w.height;ReskinReflow.place(w,x,yy,width);yy+=w.height+theme.scale(3);}}
        /** Hidden rows keep old coordinates. Native WContainer.render stops at the first
         * child below the screen; that shortcut is invalid for a visibility-filtered form.
         * Render only live rows, without assuming stale hidden rows remain Y-sorted. */
        @Override public boolean render(GuiRenderer renderer,double mouseX,double mouseY,double delta) {
            if(!visible)return true;
            WView viewport=getView();
            for(var cell:cells){
                WWidget child=cell.widget();
                if(!child.visible)continue;
                if(viewport!=null && !viewport.isWidgetInView(child))continue;
                if(child.y+child.height<=0 || child.y>=meteordevelopment.meteorclient.utils.Utils.getWindowHeight())continue;
                if(viewport!=null && !viewport.mouseOver)child.mouseOver=false;
                child.render(renderer,mouseX,mouseY,delta);
            }
            return false;
        }
        @Override protected boolean propagateEvents(WWidget w){return w.visible;}
    }
    final class GroupHeader extends WPressable {
        final SettingGroup group;final ReskinText text=new ReskinText();GroupHeader(SettingGroup g){group=g;}
        @Override protected void onCalculateSize(){width=theme.scale(180);height=theme.scale(34);}
        @Override protected void onPressed(int button){if(button==1){group.sectionExpanded=!group.sectionExpanded;invalidate();}}
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){var t=(ReskinTheme)theme;ReskinDraw.text(r,t,group.sectionExpanded?"−":"+",x+3,y+theme.scale(8),theme.scale(20),t.accentColor.get(),false);text.draw(r,t,group.name,x+theme.scale(26),y+theme.scale(8),Math.max(1,width-theme.scale(30)),t.textHeight(),false,t.titleTextColor.get(),mouseOver,dt);}
    }
    final class Row extends WContainer implements ReskinReflow.WidthAware {
        final SettingGroup group;final Setting<?> setting;WTable controls;
        double bound=480,titleH,controlsWidth,labelWidth,descriptionH;
        boolean inline;List<String> lines=List.of(),descriptionLines=List.of();
        Row(SettingGroup group,Setting<?> setting){this.group=group;this.setting=setting;tooltip=setting.description;}
        @Override public void availableWidth(double width){bound=Math.max(1,width);}
        @Override public void init(){
            controls=add(theme.table()).top().widget();
            if(setting instanceof ColorSetting color){
                var pick=controls.add(theme.button("Edit color")).expandX().widget();pick.action=()->ReskinAccess.later(()->ReskinAccess.open(new ReskinColorScreen((ReskinTheme)theme,color)));
                var reset=controls.add(theme.button("Reset")).widget();reset.action=color::reset;
            }else{
                Factory factory=getFactory(setting.getClass());
                if(factory!=null)factory.create(controls,setting);
                else controls.add(theme.label("Custom editor available in Full settings"));
            }
        }
        @Override protected void onCalculateSize(){
            width=bound;double pad=theme.scale(10),inner=Math.max(1,width-2*pad),gap=theme.scale(16);
            // Booleans and enums belong beside their labels, not in giant full-width boxes.
            double naturalControls=controls.cells.stream().filter(c->c.widget().visible).mapToDouble(c->c.widget().width+c.padLeft()+c.padRight()+theme.scale(7)).sum()+2;
            controlsWidth=Math.min(inner*.48,Math.max(theme.scale(setting instanceof BoolSetting?110:150),naturalControls));
            inline=(setting instanceof BoolSetting || setting instanceof EnumSetting<?>)&&inner-controlsWidth-gap>=theme.scale(150);
            labelWidth=inline?inner-controlsWidth-gap:inner;
            lines=ReskinWrap.lines(theme,setting.title,labelWidth,false);
            titleH=lines.size()*(theme.textHeight()+theme.scale(3));
            descriptionLines=List.of();
            if(setting.description!=null&&!setting.description.isBlank()&&width>theme.scale(300)){
                var ds=ReskinWrap.lines(theme,setting.description,labelWidth,false);
                descriptionLines=ds.size()>2?ds.subList(0,2):ds;
            }
            descriptionH=descriptionLines.isEmpty()?0:theme.scale(6)+descriptionLines.size()*(theme.textHeight()+theme.scale(2));
            ReskinReflow.measure(controls,inline?controlsWidth:inner);
            height=pad+(inline?Math.max(titleH+descriptionH,controls.height):titleH+descriptionH+theme.scale(8)+controls.height)+pad;
        }
        @Override public void calculateWidgetPositions(){x=Math.round(x);y=Math.round(y);onCalculateWidgetPositions();}
        @Override protected void onCalculateWidgetPositions(){
            double pad=theme.scale(10),inner=Math.max(1,width-2*pad);
            ReskinReflow.place(controls,inline?x+width-pad-controlsWidth:x+pad,inline?y+pad:y+pad+titleH+descriptionH+theme.scale(8),inline?controlsWidth:inner);
        }
        @Override protected void onRender(GuiRenderer r,double mx,double my,double dt){
            var t=(ReskinTheme)theme;
            if(mouseOver)r.quad(x,y,width,height,ReskinDraw.alpha(t.headerSurfaceColor.get(),95));
            r.quad(x+t.scale(10),y+height-1,Math.max(1,width-t.scale(20)),1,ReskinDraw.alpha(t.border(),95));
            double yy=y+t.scale(10);for(String line:lines){r.text(line,x+t.scale(10),yy,t.titleTextColor.get(),false);yy+=t.textHeight()+t.scale(3);}
            yy+=t.scale(6);for(String line:descriptionLines){ReskinDraw.text(r,t,line,x+t.scale(10),yy,labelWidth,t.textSecondaryColor.get(),false);yy+=t.textHeight()+t.scale(2);}
        }
    }
}
