// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;
import java.util.*;
import meteordevelopment.meteorclient.gui.GuiTheme;
/** Pixel-measured line wrapping including identifiers without spaces. */
final class ReskinWrap {
    static List<String> lines(GuiTheme t,String text,double width,boolean title){
        ArrayList<String> result=new ArrayList<>();
        for(String paragraph:(text==null?"":text).split("\\n",-1)){
            String rest=paragraph;
            while(!rest.isEmpty()){
                int end=0,lastSpace=-1;
                while(end<rest.length()){
                    int next=rest.offsetByCodePoints(end,1);
                    if(t.textWidth(rest,next,title)>Math.max(1,width)&&end>0)break;
                    if(Character.isWhitespace(rest.codePointAt(end)))lastSpace=end;
                    end=next;
                }
                if(end<rest.length()&&lastSpace>0)end=lastSpace;
                result.add(rest.substring(0,end));rest=rest.substring(end).stripLeading();
            }
            if(paragraph.isEmpty())result.add("");
        }
        return result.isEmpty()?List.of(""):result;
    }
}
