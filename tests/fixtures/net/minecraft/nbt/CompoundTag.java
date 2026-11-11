package net.minecraft.nbt;
import java.util.*;public class CompoundTag implements Tag {
 public final Map<String,Object> data=new LinkedHashMap<>();
 public Tag put(String k,Tag v){return (Tag)data.put(k,v);}public void putString(String k,String v){data.put(k,v);}public void putBoolean(String k,boolean v){data.put(k,v);}public void putInt(String k,int v){data.put(k,v);}public void putDouble(String k,double v){data.put(k,v);}
 public String getStringOr(String k,String d){return data.getOrDefault(k,d).toString();}public boolean getBooleanOr(String k,boolean d){return (Boolean)data.getOrDefault(k,d);}public int getIntOr(String k,int d){return ((Number)data.getOrDefault(k,d)).intValue();}public double getDoubleOr(String k,double d){return ((Number)data.getOrDefault(k,d)).doubleValue();}
 public Optional<String> getString(String k){return Optional.ofNullable((String)data.get(k));}public Optional<Integer> getInt(String k){return Optional.ofNullable((Integer)data.get(k));}
 public CompoundTag getCompoundOrEmpty(String k){return (CompoundTag)data.getOrDefault(k,new CompoundTag());}public Optional<CompoundTag> getCompound(String k){return Optional.ofNullable((CompoundTag)data.get(k));}public ListTag getListOrEmpty(String k){return (ListTag)data.getOrDefault(k,new ListTag());}public boolean contains(String k){return data.containsKey(k);}public boolean isEmpty(){return data.isEmpty();}
}
