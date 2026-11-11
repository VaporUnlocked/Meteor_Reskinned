# In-game acceptance checklist — not yet completed here

Use a backup copy of a Prism instance. Install exactly one Reskin JAR, retain Meteor 26.3-17 and the instance's Fabric dependencies, and run Java 25.

1. Open each layout from Appearance. Confirm the title-screen addon credit and menu header author. Restart after customizing colors and confirm persistence.
2. Open every registered tab, including addon tabs. Test navigation arrows at the smallest supported window and the bottom/side bars in Neon/Slate.
3. Test all category pages with the complete addon set, with category icons enabled and disabled. Check long names and a category with no icon.
4. In every layout, inspect a module without toggling it, use its switch once, favorite it, right-click it and open the full native settings editor. Verify existing custom addon controls.
5. Exercise booleans, numbers, sliders, enums, colors, lists, files, vectors, keybinds, conditional settings and custom setting factories. Test nested scrolling and dropdowns near each edge.
6. Resize while the GUI is open; switch windowed/fullscreen and GUI Scale values including Auto. Confirm pointer hitboxes and clipping agree with rendered controls.
7. Open Panels on Config, GUI, HUD, Friends, Macros, Profiles and addon tabs. Change its categories and close it without triggering the underlying page.
8. Inspect the game log for errors. Record actual native-font, texture and GPU behavior; do not substitute headless fixture screenshots as proof of these checks.
