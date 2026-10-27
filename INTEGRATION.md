# WindUi - integration guide

`WindUi.java` is one file with no dependencies (plain Java2D, Java 17+; tested compile-clean with `-Xlint:all`).
Change or delete the `package windui;` line to match your project. `WindUiDemo.java` is a complete example
(`java windui.WindUiDemo` opens a live window, `java windui.WindUiDemo out.png [Theme]` renders a PNG).

## 1. Build the UI once

```java
WindUi ui = new WindUi("Volt", "Minecraft 26.2");
ui.userName(() -> playerName, () -> "singleplayer");
ui.onClose(() -> closeMenu());            // the X button - you decide what "close" means
ui.onChange(() -> saveSettings());        // switch / dropdown / slider-release / theme changed

WindUi.Tab combat = ui.tab("Combat", WindUi.Icon.SWORD);
combat.card("Aim assist")
      .subtitle(() -> "Hotkey: " + keyName("aim"))          // optional second line
      .enable(() -> aimOn, v -> aimOn = v)                    // optional master switch on the card
      .toggle("Require line of sight", () -> aimLos, v -> aimLos = v)
      .slider("Range (blocks)", 4, 32, () -> (int) aimRange, v -> aimRange = v)
      .dropdown("Mode", List.of("A", "B"), () -> mode, v -> mode = v)
      .keybind("Hold to aim", () -> keyName("aimhold"), () -> capturingId == "aimhold", () -> startCapture("aimhold"))
      .button("Reset", () -> reset())
      .info(() -> "any live text");
```
Icons: `SWORD EYE GEAR FRAME BOLT SHIELD BAG SLIDERS STAR`. `tab.labelBefore("OTHER")` adds a sidebar section label,
`tab.heading("Entities")` adds a heading between cards. Every value is read through your getter each frame, so hotkeys that
change a setting are reflected automatically.

## 2. Draw it every frame (inside your overlay's paint)

```java
if (menuOpen) ui.paint(g2d, width, height);   // call it ONCE per frame - it paints a dim layer and animates by frame time
```
`ui.dimBackground(false)` removes the dim layer. `ui.scale(1.25)` scales everything (high-DPI); mouse coordinates are scaled for you.

## 3. Forward mouse events (same coordinate space you paint in)

```java
boolean used = ui.mousePressed(x, y);      // true = the UI consumed it (so don't pass it on)
ui.mouseDragged(x, y);  ui.mouseReleased(x, y);  ui.mouseMoved(x, y);
ui.mouseWheel(x, y, e.getWheelRotation());
```
Or, for a Swing component: `ui.install(component);` wires all of the above and repaints.
On a click-through overlay (WS_EX_TRANSPARENT) remember to make the window clickable while the UI is open.

## 4. Key capture (rebinding)

The UI doesn't read the keyboard. A `keybind` row only shows `keyText` (or "press a key..." while `waiting` is true) and
calls `click` when pressed - start your own capture there, set the new key, and the row updates by itself.

## 5. Persistence (optional)

```java
Map<String,String> m = new HashMap<>(); ui.saveState(m);   // window position, tab, theme, which cards are open
ui.loadState(m);                                           // call after building the UI
```

## Look & feel notes
- Colours are WindUI's own 10 themes (`WindUi.THEMES`, `ui.nextTheme()`, `ui.setTheme("Sky")`).
- Font defaults to "Segoe UI" (Windows); `ui.font("Inter")` if you ship/install another one.
- Everything animates with exponential easing (switches, hovers, card expand, dropdowns, scrolling).
