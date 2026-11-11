// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.world.item.ItemStack;

/** Optional UI-only extension point. Meteor has category icons, not a universal module-icon field. */
public final class ReskinIcons {
    private static final Map<Category,ItemStack> cache=new IdentityHashMap<>();
    private static final Map<Module,Function<Module,ItemStack>> providers=new IdentityHashMap<>();
    private ReskinIcons(){}
    public static void registerModuleIcon(Module module,Function<Module,ItemStack> provider){
        if(module==null)throw new IllegalArgumentException("module must not be null");
        if(provider==null)providers.remove(module);else providers.put(module,provider);
    }
    public static void invalidateCategoryIcons(){cache.clear();}
    public static ItemStack categoryIcon(Category category){
        if(category==null)return null;
        if(cache.containsKey(category))return cache.get(category);
        ItemStack stack=null;
        try{if(category.icon!=null){stack=category.icon.get();if(stack!=null&&stack.isEmpty())stack=null;}}
        catch(RuntimeException|LinkageError e){System.err.println("[Meteor Reskin] Category icon unavailable for "+category.name+"; using initials ("+e.getClass().getSimpleName()+").");}
        cache.put(category,stack);return stack;
    }
    public static String initials(String name){
        if(name==null||name.isBlank())return "?";
        String[] words=java.util.Arrays.stream(name.strip().split("[\\s_/-]+")).filter(w->!w.isBlank()).toArray(String[]::new);
        if(words.length==0)return "?";
        String first=words[0].substring(0,words[0].offsetByCodePoints(0,1));
        if(words.length>1){String last=words[words.length-1];first+=last.substring(0,last.offsetByCodePoints(0,1));}
        else if(words[0].codePointCount(0,words[0].length())>1)first=words[0].substring(0,words[0].offsetByCodePoints(0,2));
        return first.toUpperCase(java.util.Locale.ROOT);
    }
    static void draw(GuiRenderer r,ReskinTheme t,Category category,Module module,String fallback,double x,double y,double size){
        ItemStack stack=null;
        if(t.useCategoryIcons.get()){
            if(module!=null&&providers.containsKey(module)){
                try{stack=providers.get(module).apply(module);if(stack!=null&&stack.isEmpty())stack=null;}
                catch(RuntimeException|LinkageError e){providers.remove(module);System.err.println("[Meteor Reskin] Module icon provider failed; falling back to category icon.");}
            }
            if(stack==null)stack=categoryIcon(category);
        }
        ReskinDraw.round(r,x,y,size,size,t.scale(6),t.headerSurfaceColor.get());
        if(stack!=null){
            try{r.item(stack,(int)(x+t.scale(2)),(int)(y+t.scale(2)),(float)((size-t.scale(4))/16),false);return;}
            catch(RuntimeException|LinkageError e){cache.put(category,null);}
        }
        String text=initials(fallback);double w=t.textWidth(text);
        if(w<size)r.text(text,x+(size-w)/2,y+(size-t.textHeight())/2,t.accentColor.get(),false);
    }
}
