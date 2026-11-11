// SPDX-License-Identifier: GPL-3.0-only
// Meteor Reskin — BY Cloudy_City. UI-only addon.
package io.github.cloudycity.meteorreskin;

import java.util.*;
import java.util.function.*;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.world.item.ItemStack;

/**
 * Shared icon resolver for all eight skins. The native Category.icon supplier is authoritative;
 * no addon integration is required. Item artwork is submitted through GuiRenderer.post(), just
 * like Meteor's WItem, so it is drawn after the enclosing mesh and inside the current scissor.
 *
 * Optional registrations are identity-keyed, not name-keyed: two addons may use the same label.
 * Register on the client thread. Suppliers must return presentation-only data and must not
 * mutate game state. Reskin does not modify the returned ItemStack or take ownership of textures.
 */
public final class ReskinIcons {
    /** Original vector symbols; no icon font dependency. */
    public enum Symbol {
        ADDON, CUBE, GRID, LIST, SEARCH, SETTINGS, PALETTE, KEYBOARD, FRIENDS, PROFILE, GUI, HUD,
        COMBAT, PLAYER, MOVEMENT, RENDER, WORLD, MISC, SATELLITE, BARITONE,
        STAR, CLOCK, POWER, SHIELD, ANCHOR, ANVIL, BED, ROBOT, ARROW, SWAP,
        ARMOR, APPLE, FISH, TOOL, POTION, CRYSTAL, TOTEM, WINGS, CAMERA, SUN,
        CHEST, TRACER, CROSSHAIR, SPEED, FEATHER, CHAT, CLOUD, BOOK, REFRESH,
        COPY, PASTE, SAVE, FOLDER, DOWNLOAD, UPLOAD, CLOSE, CHECK, INFO
    }

    public sealed interface Icon permits ItemIcon, TextureIcon, SymbolIcon {}
    public record ItemIcon(ItemStack stack) implements Icon {}
    /** GuiTexture must be packed by the owning addon using Meteor's texture packer. */
    public record TextureIcon(GuiTexture texture, boolean tintWithAccent) implements Icon {}
    public record SymbolIcon(Symbol symbol) implements Icon {}

    private static final Map<Category,Supplier<Icon>> categoryProviders = new IdentityHashMap<>();
    private static final Map<Module,Function<Module,Icon>> moduleProviders = new IdentityHashMap<>();
    private static final Map<Category,Entry> nativeCache = new IdentityHashMap<>();
    private static final Map<Category,Entry> categoryCache = new IdentityHashMap<>();
    private static final Map<Module,Entry> moduleCache = new IdentityHashMap<>();
    private static final Map<Object,Long> blockedUntil = new IdentityHashMap<>();
    private static final Set<Object> reported = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final long PRESENT_NS = 2_000_000_000L, EMPTY_NS = 1_000_000_000L,
        FAILURE_NS = 30_000_000_000L;
    // Package-private clock seam for deterministic expiry tests; production uses monotonic time.
    static LongSupplier clock = System::nanoTime;
    private static final class Entry { Icon value; long refreshAfter; }
    private ReskinIcons() {}

    public static Icon item(ItemStack item) { return new ItemIcon(item); }
    public static Icon texture(GuiTexture texture, boolean tint) { return new TextureIcon(texture,tint); }
    public static Icon symbol(Symbol symbol) { return new SymbolIcon(Objects.requireNonNull(symbol)); }

    /** An explicit registration overrides this category's ordinary Category.icon supplier. */
    public static synchronized void registerCategory(Category category, Supplier<Icon> provider) {
        Objects.requireNonNull(category,"category");
        if (provider == null) categoryProviders.remove(category); else categoryProviders.put(category,provider);
        categoryCache.remove(category); nativeCache.remove(category); reported.remove(category);
    }
    public static synchronized void registerModule(Module module, Function<Module,Icon> provider) {
        Objects.requireNonNull(module,"module");
        if (provider == null) moduleProviders.remove(module); else moduleProviders.put(module,provider);
        moduleCache.remove(module); reported.remove(module);
    }
    public static void registerCategoryIcon(Category category, Supplier<ItemStack> provider) {
        registerCategory(category,provider == null ? null : () -> item(provider.get()));
    }
    /** Source-compatible with the beta API. Pass null to remove a registration. */
    public static void registerModuleIcon(Module module, Function<Module,ItemStack> provider) {
        registerModule(module,provider == null ? null : m -> item(provider.apply(m)));
    }
    public static void registerCategorySymbol(Category category, Symbol symbol) {
        registerCategory(category,symbol == null ? null : () -> symbol(symbol));
    }
    public static void registerModuleSymbol(Module module, Symbol symbol) {
        registerModule(module,symbol == null ? null : m -> symbol(symbol));
    }
    public static synchronized void invalidateCategoryIcons() {
        nativeCache.clear(); categoryCache.clear(); blockedUntil.clear(); reported.clear();
    }
    /** Reprobe artwork without deleting registrations, user settings, or module state. */
    public static synchronized void refresh() {
        invalidateCategoryIcons(); moduleCache.clear();
    }

    private static Object payload(Icon icon) {
        return switch (icon) {
            case ItemIcon i -> i.stack();
            case TextureIcon i -> i.texture();
            case SymbolIcon i -> i.symbol();
            case null -> null;
        };
    }
    private static boolean usable(Icon icon, long now) {
        Object key=payload(icon);
        if (key == null) return false;
        Long until=blockedUntil.get(key);
        if (until != null) {
            if (now < until) return false;
            blockedUntil.remove(key);
        }
        return !(icon instanceof ItemIcon i) || !i.stack().isEmpty();
    }
    private static void report(Object owner,String label,Throwable failure) {
        if (reported.add(owner)) System.err.println("[Meteor Reskin] Icon fallback for " + label
            + " (" + failure.getClass().getSimpleName() + "). Retry is automatic; see Appearance > Icons.");
    }
    private static <T> Icon cached(Map<T,Entry> map,T owner,String label,Supplier<Icon> provider) {
        Entry e=map.computeIfAbsent(owner,k -> new Entry()); long now=clock.getAsLong();
        try {
            if (e.refreshAfter==0 || now >= e.refreshAfter) {
                Icon candidate=provider.get(); e.value=usable(candidate,now)?candidate:null;
                e.refreshAfter=now+(e.value==null?EMPTY_NS:PRESENT_NS);
            }
            return usable(e.value,now)?e.value:null;
        } catch (RuntimeException | LinkageError failure) {
            e.value=null; e.refreshAfter=now+FAILURE_NS; report(owner,label,failure); return null;
        }
    }
    private static Icon nativeCategory(Category category) {
        if (category==null) return null;
        return cached(nativeCache,category,category.name,
            () -> category.icon==null?null:item(category.icon.get()));
    }
    private static Icon providedCategory(Category category) {
        if (category==null) return null;
        Supplier<Icon> override=categoryProviders.get(category);
        Icon icon=override==null?null:cached(categoryCache,category,category.name,override);
        return icon!=null?icon:nativeCategory(category);
    }
    static synchronized String categorySource(ReskinTheme theme,Category category) {
        if(!theme.useCategoryIcons.get())return "Symbol mode";
        Icon icon=providedCategory(category);
        if(icon instanceof ItemIcon)return "Provided item artwork";
        if(icon instanceof TextureIcon)return "Registered texture";
        if(icon instanceof SymbolIcon)return "Registered symbol";
        return "Fallback symbol / no artwork";
    }
    /** Compatibility accessor: returns a valid provided category item, otherwise null. */
    public static synchronized ItemStack categoryIcon(Category category) {
        Icon icon=providedCategory(category); return icon instanceof ItemIcon i?i.stack():null;
    }
    static synchronized Icon resolve(ReskinTheme theme,Category category,Module module) {
        if (theme.useCategoryIcons.get()) {
            Function<Module,Icon> provider=module==null?null:moduleProviders.get(module);
            Icon supplied=provider==null?null:cached(moduleCache,module,module.title,() -> provider.apply(module));
            if (supplied!=null) return supplied;
        }
        if (module!=null && theme.semanticModuleIcons.get() && isCoreModule(module)) {
            Symbol symbol=moduleSymbol(module.name); if (symbol!=null) return symbol(symbol);
        }
        if (theme.useCategoryIcons.get()) {
            Icon supplied=providedCategory(category); if (supplied!=null) return supplied;
        }
        return symbol(categorySymbol(category==null?"":category.name));
    }
    private static boolean isCoreModule(Module module) {
        // Never guess at private third-party icon fields or use a built-in module name to override an addon.
        return module.getClass().getName().startsWith("meteordevelopment.meteorclient.systems.modules.");
    }
    static Symbol categorySymbol(String name) {
        String n=canonical(name);
        return switch(n) {
            case "combat" -> Symbol.COMBAT; case "player" -> Symbol.PLAYER;
            case "movement" -> Symbol.MOVEMENT; case "render" -> Symbol.RENDER;
            case "world" -> Symbol.WORLD; case "misc" -> Symbol.MISC;
            case "allmodules" -> Symbol.GRID; default -> Symbol.ADDON;
        };
    }
    static String canonical(String name) {
        return name==null?"":name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","");
    }
    /** Exact native module identifiers only. No gameplay behavior is inferred or changed. */
    static Symbol moduleSymbol(String name) {
        return switch(canonical(name)) {
            case "anchoraura", "anchor" -> Symbol.ANCHOR;
            case "antianvil", "autoanvil" -> Symbol.ANVIL;
            case "antibed", "bedaura" -> Symbol.BED;
            case "antibot" -> Symbol.ROBOT;
            case "arrowdodge", "bowaimbot", "bowspam" -> Symbol.ARROW;
            case "attributeswap", "chestswap", "swaphands" -> Symbol.SWAP;
            case "autoarmor" -> Symbol.ARMOR;
            case "autoeat", "autogap" -> Symbol.APPLE;
            case "autofish" -> Symbol.FISH;
            case "autotool", "infinityminer", "nuker", "veinminer" -> Symbol.TOOL;
            case "autoexp", "automend", "potionsaver", "autobrewer" -> Symbol.POTION;
            case "crystalaura", "autocrystal", "crystalplace" -> Symbol.CRYSTAL;
            case "autototem", "totempop" -> Symbol.TOTEM;
            case "elytrafly", "elytraboost", "flight" -> Symbol.WINGS;
            case "cameratweaks", "freecam", "freelook", "zoom" -> Symbol.CAMERA;
            case "fullbright", "ambience", "timechanger", "weatherchanger" -> Symbol.SUN;
            case "storageesp", "echestfarmer", "inventorytweaks" -> Symbol.CHEST;
            case "tracers", "breadcrumbs", "waypoints", "trajectories" -> Symbol.TRACER;
            case "killaura", "hitboxes", "entityowner", "nametags" -> Symbol.CROSSHAIR;
            case "speed", "sprint", "timer" -> Symbol.SPEED;
            case "nofall", "antivoid" -> Symbol.FEATHER;
            case "betterchat", "chattweaks", "notifier", "messages" -> Symbol.CHAT;
            case "blur" -> Symbol.CLOUD;
            case "bettertooltips", "bettertab", "bookbot" -> Symbol.BOOK;
            case "autorespawn", "autoreconnect" -> Symbol.REFRESH;
            case "autolog", "autoleave" -> Symbol.POWER;
            default -> null;
        };
    }

    /** Kept for integrations compiled against the beta API; missing artwork now uses a visible symbol. */
    public static String initials(String name) {
        if(name==null||name.isBlank())return "?";
        String[] words=Arrays.stream(name.strip().split("[\\s_/-]+")).filter(w->!w.isBlank()).toArray(String[]::new);
        if(words.length==0)return "?";
        String first=words[0].substring(0,words[0].offsetByCodePoints(0,1));
        if(words.length>1){String last=words[words.length-1];first+=last.substring(0,last.offsetByCodePoints(0,1));}
        else if(words[0].codePointCount(0,words[0].length())>1)first=words[0].substring(0,words[0].offsetByCodePoints(0,2));
        return first.toUpperCase(Locale.ROOT);
    }
    private static synchronized void failed(Icon icon,Object owner,String label,Throwable failure) {
        long until=clock.getAsLong()+FAILURE_NS;
        Object key=payload(icon); if(key!=null)blockedUntil.put(key,until);
        // Quarantine the producer too: a supplier allocating a new ItemStack must not defeat
        // the cooldown merely by returning a new identity on its next normal cache refresh.
        for(Map<?,Entry> cache:List.of(nativeCache,categoryCache,moduleCache))
            for(Entry entry:cache.values())if(entry.value==icon){entry.value=null;entry.refreshAfter=until;}
        report(owner==null?icon:owner,label,failure);
    }
    static void draw(GuiRenderer r,ReskinTheme t,Category category,Module module,String label,double x,double y,double size) {
        if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(size)||size<=0)return;
        Icon icon=resolve(t,category,module);
        double inset=Math.min(size*.10,Math.max(1,t.scale(2))),edge=Math.max(.01,size-2*inset);
        double ix=x+inset,iy=y+inset;
        Object owner=module==null?category:module;
        if(icon instanceof ItemIcon item){
            final int px=(int)Math.round(ix),py=(int)Math.round(iy);
            final float scale=(float)(edge/16.0);
            // NOT absolutePost: it would lose the scrolling panel's scissor and draw over other rows.
            r.post(() -> {
                try { if(!item.stack().isEmpty()) r.item(item.stack(),px,py,scale,false); }
                catch(RuntimeException|LinkageError failure){failed(icon,owner,label,failure);}
            });
            return;
        }
        if(icon instanceof TextureIcon texture){
            try { r.quad(ix,iy,edge,edge,texture.texture(),texture.tintWithAccent()?t.accentColor.get():new Color(255,255,255,255)); return; }
            catch(RuntimeException|LinkageError failure){failed(icon,owner,label,failure);}
        }
        Symbol symbol=icon instanceof SymbolIcon s?s.symbol():Symbol.ADDON;
        ReskinGlyph.draw(r,symbol.name().toLowerCase(Locale.ROOT),ix,iy,edge,t.textColor.get());
    }
}
