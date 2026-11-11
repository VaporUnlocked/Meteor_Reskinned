package meteordevelopment.meteorclient.gui.renderer;

import java.awt.*;import java.awt.geom.*;import java.awt.image.BufferedImage;import java.awt.font.FontRenderContext;import java.io.File;import javax.imageio.ImageIO;import java.util.ArrayList;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.utils.render.color.Color;

/** Headless draw-submission adapter. Produces software test renders, not Minecraft screenshots. */
public class GuiRenderer {
 public GuiTheme theme;public int clipDepth,itemCalls,drawCalls;public boolean capture;
 public BufferedImage image;private Graphics2D g;private final java.util.Stack<Shape> clips=new java.util.Stack<>();private final ArrayList<Runnable> post=new ArrayList<>();
 private static final FontRenderContext FRC=new FontRenderContext(null,true,true);
 public static double measure(String s,double size){return new Font("SansSerif",Font.PLAIN,15).deriveFont((float)size).getStringBounds(s,FRC).getWidth();}
 public GuiRenderer(){this(false);}public GuiRenderer(boolean capture){this.capture=capture;if(capture){image=new BufferedImage(meteordevelopment.meteorclient.utils.Utils.width,meteordevelopment.meteorclient.utils.Utils.height,BufferedImage.TYPE_INT_ARGB);g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);g.setColor(new java.awt.Color(7,11,18));g.fillRect(0,0,image.getWidth(),image.getHeight());}}
 private static void finite(double... n){for(double x:n)if(!Double.isFinite(x))throw new AssertionError("non-finite drawing coordinate");}
 private java.awt.Color color(Color c){return new java.awt.Color(Math.clamp(c.r,0,255),Math.clamp(c.g,0,255),Math.clamp(c.b,0,255),Math.clamp(c.a,0,255));}
 public void quad(double x,double y,double w,double h,Color c){finite(x,y,w,h);if(w<0||h<0)throw new AssertionError("negative quad "+w+" "+h);drawCalls++;if(capture){g.setColor(color(c));g.fill(new Rectangle2D.Double(x,y,w,h));}}
 public void quad(double x,double y,double w,double h,Color a,Color b){quad(x,y,w,h,a);}public void quad(double x,double y,double w,double h,Color a,Color b,Color c,Color d){quad(x,y,w,h,a);}
 public void quad(double x,double y,double w,double h,GuiTexture texture,Color c){quad(x,y,w,h,c);}public void quad(WWidget w,Color c){quad(w.x,w.y,w.width,w.height,c);}public void fill(WWidget w,Color c){quad(w,c);}
 public void triangle(double a,double b,double c,double d,double e,double f,Color color){finite(a,b,c,d,e,f);drawCalls++;if(capture){Path2D p=new Path2D.Double();p.moveTo(a,b);p.lineTo(c,d);p.lineTo(e,f);p.closePath();g.setColor(color(color));g.fill(p);}}
 public void text(String text,double x,double y,Color c,boolean title){finite(x,y);drawCalls++;if(capture){float size=(float)((theme==null?1:theme.scale(1))*15*(title?1.25:1));g.setFont(new Font("SansSerif",Font.PLAIN,15).deriveFont(size));g.setColor(color(c));g.drawString(text,(float)x,(float)(y+size*.80));}}
 public void scissorStart(double x,double y,double w,double h){finite(x,y,w,h);if(w<0||h<0)throw new AssertionError("negative clip");clipDepth++;if(capture){clips.push(g.getClip());g.clip(new Rectangle2D.Double(x,y,w,h));}}
 public void scissorEnd(){if(--clipDepth<0)throw new AssertionError("unbalanced clip");if(capture)g.setClip(clips.pop());}
 public void tooltip(String s){}public void rotatedQuad(double x,double y,double w,double h,double rot,GuiTexture t,Color c){quad(x,y,w,h,c);}
 public void item(net.minecraft.world.item.ItemStack stack,int x,int y,float size,boolean overlay){itemCalls++;quad(x,y,16*size,16*size,new Color(113,164,140));}
 public void absolutePost(Runnable r){post.add(r);}public void finish(){for(Runnable r:new ArrayList<>(post))r.run();post.clear();if(clipDepth!=0)throw new AssertionError("clip stack not balanced: "+clipDepth);}
 public void save(String file)throws Exception{finish();if(capture){g.setClip(null);g.setFont(new Font("SansSerif",Font.PLAIN,11));g.setColor(java.awt.Color.GRAY);g.drawString("SOFTWARE WIDGET TEST RENDER - NOT A MINECRAFT SCREENSHOT",8,image.getHeight()-3);ImageIO.write(image,"png",new File(file));g.dispose();}}
}
