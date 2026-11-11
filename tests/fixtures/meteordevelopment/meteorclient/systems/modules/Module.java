package meteordevelopment.meteorclient.systems.modules;
import meteordevelopment.meteorclient.settings.*;
public class Module {public final Category category;public final String title,name,description;public final Settings settings=new Settings();public boolean favorite,active;public int toggles;
 public Module(Category c,String name){category=c;this.title=name;this.name=name.toLowerCase();description="Existing module configuration supplied by Meteor or an installed addon.";settings.getDefaultGroup().add(new BoolSetting.Builder().name("show-details").defaultValue(true).build());settings.getDefaultGroup().add(new DoubleSetting.Builder().name("sample-value").defaultValue(50.0).build());}
 public boolean isActive(){return active;}public void toggle(){active=!active;toggles++;}
}
