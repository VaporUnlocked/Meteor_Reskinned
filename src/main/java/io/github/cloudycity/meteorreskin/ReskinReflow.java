// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;

import java.util.*;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.*;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.input.WDoubleEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;

/** Constraint layout for native setting controls. No rescaling of text and no horizontal panning. */
final class ReskinReflow {
    private ReskinReflow(){}
    interface WidthAware { void availableWidth(double width); }
    static double measure(WWidget widget,double width){
        double limit=Math.max(1,width);
        // A native numeric editor gives its value field a minimum width of 75.
        // Clearing it makes WTextBox fall back to a nearly square one-character box.
        // Preserve it and measure the current value, but never exceed the inspector.
        if (widget instanceof WTextBox input &&
            (input.parent instanceof WDoubleEdit || input.parent instanceof WIntEdit)) {
            double natural = Math.max(widget.theme.scale(Math.max(82, widget.minWidth)),
                widget.theme.textWidth(input.get()) + 2 * input.pad() + widget.theme.scale(8));
            widget.width = Math.min(limit, Math.ceil(natural));
            return widget.height;
        }
        if(widget instanceof WDoubleEdit || widget instanceof WIntEdit){
            numeric((WContainer)widget,limit,false);return widget.height;
        }
        widget.minWidth=0;
        if(widget instanceof WidthAware aware){aware.availableWidth(limit);widget.calculateSize();widget.width=limit;return widget.height;}
        if(widget instanceof WTable table){arrangeTable(table,limit,false);return table.height;}
        if(widget instanceof WHorizontalList list){arrangeFlow(list,list.cells,limit,false);return list.height;}
        if(widget instanceof WContainer c && !(c instanceof WView) && !(c instanceof WWindow)){
            // Preserve ordinary native sections when possible; their own collapse state remains authoritative.
            for(Cell<?> cell:c.cells)measure(cell.widget(),Math.max(1,limit-cell.padLeft()-cell.padRight()));
            c.width=limit;
            if(c instanceof WVerticalList list){
                double h=0;int n=0;
                for(Cell<?> cell:c.cells)if(cell.widget().visible){if(n++>0)h+=c.theme.scale(list.spacing);h+=cell.padTop()+cell.widget().height+cell.padBottom();}
                if(!(c instanceof WSection section)||section.isExpanded())c.height=h;
            }
            return c.height;
        }
        widget.width=Math.min(widget.width,limit);
        return widget.height;
    }
    static void place(WWidget w,double x,double y,double width){
        w.x=x;w.y=y;w.width=Math.max(1,width);
        if(w instanceof WDoubleEdit || w instanceof WIntEdit){numeric((WContainer)w,width,true);return;}
        if(w instanceof WidthAware){w.calculateWidgetPositions();return;}
        if(w instanceof WTable table){arrangeTable(table,width,true);return;}
        if(w instanceof WHorizontalList list){arrangeFlow(list,list.cells,width,true);return;}
        // Let native controls update cursor offsets, dropdown anchors and internal bookkeeping.
        w.calculateWidgetPositions();
        if(w instanceof WContainer c && !(w instanceof WView) && !(w instanceof WWindow)){
            for(Cell<?> cell:c.cells){WWidget child=cell.widget();if(child.visible)place(child,child.x,child.y,Math.min(child.width,Math.max(1,x+width-child.x)));}
        }
    }
    /** Never give a slider the pixels required to read the complete numeric value. */
    private static void numeric(WContainer edit,double bound,boolean place) {
        if(edit.cells.isEmpty()){edit.width=bound;return;}
        Cell<?> valueCell=edit.cells.getFirst();WWidget value=valueCell.widget();
        measure(value,Math.max(1,bound-valueCell.padLeft()-valueCell.padRight()));
        double gap=edit.theme.scale(7), yy=0, valueW=value.width;
        double lineH=valueCell.padTop()+value.height+valueCell.padBottom();
        if(place){valueCell.x=edit.x+valueCell.padLeft();valueCell.y=edit.y+valueCell.padTop();valueCell.width=valueW;valueCell.height=value.height;value.x=valueCell.x;value.y=valueCell.y;value.calculateWidgetPositions();}
        double used=valueCell.padLeft()+valueW+valueCell.padRight();
        for(int i=1;i<edit.cells.size();i++){
            Cell<?> cell=edit.cells.get(i);WWidget w=cell.widget();if(!w.visible)continue;
            double room=bound-used-gap-cell.padLeft()-cell.padRight();
            double desired=edit.theme.scale(80);
            if(room<desired){yy+=lineH+gap;used=0;lineH=0;room=bound-cell.padLeft()-cell.padRight();}
            else used+=gap;
            w.width=Math.max(1,room);
            if(place){cell.x=edit.x+used+cell.padLeft();cell.y=edit.y+yy+cell.padTop();cell.width=w.width;cell.height=w.height;w.x=cell.x;w.y=cell.y;w.calculateWidgetPositions();}
            lineH=Math.max(lineH,cell.padTop()+w.height+cell.padBottom());used+=cell.padLeft()+w.width+cell.padRight();
        }
        edit.width=bound;edit.height=yy+lineH;
    }
    private static void arrangeTable(WTable table,double bound,boolean place){
        double yy=0,gap=table.theme.scale(table.verticalSpacing);boolean any=false;
        for(int i=0;i<=table.rowI();i++){
            List<Cell<?>> row;try{row=table.getRow(i);}catch(IndexOutOfBoundsException e){break;}
            // WTable.rowI() is a write cursor, not the number of allocated rows.
            // getRow() returns null for an empty table or a trailing row() call.
            // Neither case is malformed, and neither should crash a render frame.
            if(row == null || row.isEmpty())continue;
            if(any)yy+=gap;any=true;
            double height=flow(row,table,bound,place,table.x,table.y+yy);
            yy+=height;
        }
        table.width=bound;table.height=yy;
    }
    private static void arrangeFlow(WHorizontalList list,List<Cell<?>> cells,double bound,boolean place){
        list.width=bound;list.height=flow(cells,list,bound,place,list.x,list.y);
    }
    private static boolean stretch(Cell<?> c){
        WWidget w=c.widget();
        return c.expandCellX && (w instanceof WContainer || w instanceof meteordevelopment.meteorclient.gui.widgets.input.WSlider || w instanceof meteordevelopment.meteorclient.gui.widgets.input.WTextBox);
    }
    private static double flow(List<Cell<?>> all,WWidget owner,double bound,boolean place,double ox,double oy){
        List<Cell<?>> row=all.stream().filter(Objects::nonNull).filter(c->c.widget()!=null && c.widget().visible).toList();
        double g=owner.theme.scale(7),xx=0,yy=0,line=0;
        for(int i=0;i<row.size();i++){
            Cell<?> cell=row.get(i);WWidget w=cell.widget();double pad=cell.padLeft()+cell.padRight();
            double allowed=Math.max(1,bound-pad);
            if(stretch(cell)){
                double fixed=0;int flexible=0;
                for(int j=i+1;j<row.size();j++){
                    Cell<?> other=row.get(j);
                    if(stretch(other))flexible++;
                    else fixed+=other.widget().width+other.padLeft()+other.padRight();
                }
                double room=bound-xx-pad-fixed-g*(row.size()-i-1);
                double minimum=owner.theme.scale(80);
                if((w instanceof WDoubleEdit||w instanceof WIntEdit)&&!((WContainer)w).cells.isEmpty()){
                    var box=(WTextBox)((WContainer)w).cells.getFirst().widget();
                    minimum=Math.max(minimum,Math.ceil(Math.max(owner.theme.scale(Math.max(82,box.minWidth)),owner.theme.textWidth(box.get())+2*box.pad()+owner.theme.scale(8)))+1);
                }
                if(room>=minimum*(flexible+1))allowed=room/(flexible+1);
                else if(xx>0){yy+=line+g;xx=0;line=0;allowed=Math.max(1,bound-pad);}
            }
            measure(w,allowed);
            double ww=stretch(cell)?allowed:Math.min(allowed,w.width);
            if(xx>0&&xx+pad+ww>bound+.5){yy+=line+g;xx=0;line=0;}
            w.width=ww;
            if(place){cell.x=ox+xx+cell.padLeft();cell.y=oy+yy+cell.padTop();cell.width=ww;cell.height=w.height;place(w,cell.x,cell.y,ww);}
            line=Math.max(line,cell.padTop()+w.height+cell.padBottom());xx+=ww+pad+g;
        }
        return yy+line;
    }
}
