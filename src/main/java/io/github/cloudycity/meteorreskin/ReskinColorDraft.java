// SPDX-License-Identifier: GPL-3.0-only
package io.github.cloudycity.meteorreskin;

import java.util.Locale;

/** Independent editable RGBA value. An invalid edit never changes the previous color. */
public final class ReskinColorDraft {
    private int r,g,b,a;
    private float hue;
    public ReskinColorDraft(int r,int g,int b,int a) { set(r,g,b,a); }
    private static int clamp(int n) { return Math.max(0,Math.min(255,n)); }
    public void set(int r,int g,int b,int a) {
        this.r=clamp(r);this.g=clamp(g);this.b=clamp(b);this.a=clamp(a);
        float[] hsv=java.awt.Color.RGBtoHSB(this.r,this.g,this.b,null);
        if(hsv[1]>0 && hsv[2]>0)hue=hsv[0];
    }
    public int r(){return r;} public int g(){return g;} public int b(){return b;} public int a(){return a;}
    public String hex(){return String.format(Locale.ROOT,"#%02X%02X%02X%02X",r,g,b,a);}
    public float[] hsv(){float[] v=java.awt.Color.RGBtoHSB(r,g,b,null);v[0]=hue;return v;}
    public void hsv(float h,float s,float v){
        hue=Math.max(0,Math.min(1,h));
        int rgb=java.awt.Color.HSBtoRGB(hue,Math.max(0,Math.min(1,s)),Math.max(0,Math.min(1,v)));
        r=rgb>>>16&255;g=rgb>>>8&255;b=rgb&255;
    }
    public boolean parseHex(String value){
        if(value==null)return false;
        String h=value.strip();if(h.startsWith("#"))h=h.substring(1);
        if(!h.matches("(?i)([0-9a-f]{6}|[0-9a-f]{8})"))return false;
        int rr=Integer.parseInt(h.substring(0,2),16),gg=Integer.parseInt(h.substring(2,4),16),bb=Integer.parseInt(h.substring(4,6),16);
        int aa=h.length()==8?Integer.parseInt(h.substring(6,8),16):a;
        set(rr,gg,bb,aa);return true;
    }
    public boolean channel(int channel,String value){
        if(value==null||!value.strip().matches("[0-9]{1,3}"))return false;
        int n=Integer.parseInt(value.strip());if(n>255)return false;
        switch(channel){case 0->set(n,g,b,a);case 1->set(r,n,b,a);case 2->set(r,g,n,a);case 3->set(r,g,b,n);default->throw new IllegalArgumentException("RGBA channel");}
        return true;
    }
}
