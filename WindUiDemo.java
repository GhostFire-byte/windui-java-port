package windui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * Example of how to use WindUi.
 *   java windui.WindUiDemo            -> opens a live window you can click and drag
 *   java windui.WindUiDemo out.png    -> renders one frame to a PNG (headless)
 */
public class WindUiDemo {
    // pretend module state - in your client these are your own static fields
    static boolean aim = true, auto = false, esp = true, tracers = false, fullbright = true, freecam = false, hostile = true, passive = true;
    static int aimRange = 12, aimSmooth = 10, espRange = 64, zoom = 4;
    static String mode = "Blacklist", keyShown = "Numpad 5";
    static boolean capturing = false;

    static WindUi buildUi() {
        WindUi ui = new WindUi("Volt", "Minecraft 26.2  -  overlay");
        ui.userName(() -> "Steve", () -> "singleplayer");
        ui.onClose(() -> System.out.println("close pressed (hide the UI here)"));
        ui.onChange(() -> { /* save your settings here */ });

        WindUi.Tab combat = ui.tab("Combat", WindUi.Icon.SWORD);
        combat.card("Aim assist").subtitle(() -> "Hotkey: F6").enable(() -> aim, v -> aim = v).open(true)
            .toggle("Require line of sight", () -> true, v -> { })
            .slider("Range (blocks)", 4, 32, () -> aimRange, v -> aimRange = v)
            .slider("Speed (low = smoother)", 1, 30, () -> aimSmooth, v -> aimSmooth = v)
            .keybind("Hold to aim", () -> "Left Alt", () -> false, () -> { });
        combat.card("Auto-attack").subtitle("Hotkey: F9").enable(() -> auto, v -> auto = v)
            .toggle("Smart timing", () -> true, v -> { });

        WindUi.Tab render = ui.tab("Render", WindUi.Icon.EYE);
        render.heading("Entities");
        render.card("Entity boxes").subtitle("Hotkey: F8").enable(() -> esp, v -> esp = v).open(true)
            .slider("Range (blocks)", 8, 128, () -> espRange, v -> espRange = v)
            .toggle("Show hostile mobs", () -> hostile, v -> hostile = v)
            .toggle("Show passive mobs", () -> passive, v -> passive = v);
        render.card("Tracers").subtitle("Hotkey: Numpad 1").enable(() -> tracers, v -> tracers = v);

        WindUi.Tab automation = ui.tab("Automation", WindUi.Icon.GEAR);
        automation.card("Inventory discarder").subtitle("Hotkey: Numpad -").open(true)
            .dropdown("Mode", List.of("Blacklist", "Whitelist"), () -> mode, v -> mode = v)
            .info(() -> "Blacklist: seeds, granite, diorite")
            .button("Discard now", () -> System.out.println("discard!"))
            .keybind("Hotkey", () -> keyShown, () -> capturing, () -> capturing = !capturing);

        WindUi.Tab view = ui.tab("View", WindUi.Icon.FRAME);
        view.card("Fullbright").subtitle("Hotkey: Numpad /").enable(() -> fullbright, v -> fullbright = v);
        view.card("Zoom").subtitle("Hold C").slider("Strength (x)", 2, 20, () -> zoom, v -> zoom = v);
        view.card("Freecam").subtitle("Hotkey: Numpad *").enable(() -> freecam, v -> freecam = v);

        WindUi.Tab volt = ui.tab("Volt", WindUi.Icon.BOLT).labelBefore("OTHER");
        volt.card("Appearance").subtitle("Window theme").open(true)
            .dropdown("Theme", WindUi.themeNames(), () -> ui.theme().name, ui::setTheme)
            .button("Next theme", ui::nextTheme);
        return ui;
    }

    public static void main(String[] args) throws Exception {
        WindUi ui = buildUi();
        if (args.length > 0) {                                           // headless: render a frame to a PNG
            System.setProperty("java.awt.headless", "true");
            if (args.length > 1) ui.setTheme(args[1]);
            int W = 1100, H = 700;
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            for (int i = 0; i < 10; i++) {                                // let the animations settle
                g.setPaint(new GradientPaint(0, 0, new Color(70, 110, 60), 0, H, new Color(40, 70, 40)));   // fake "game" behind the UI
                g.fillRect(0, 0, W, H);
                ui.paint(g, W, H);
                try { Thread.sleep(30); } catch (InterruptedException ignored) { }
            }
            g.dispose();
            javax.imageio.ImageIO.write(img, "png", new java.io.File(args[0]));
            return;
        }
        final WindUi windui = ui;                                          // (a JPanel has a field called "ui" - avoid the clash)
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("WindUi demo");
            JPanel p = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(new Color(60, 95, 55)); g.fillRect(0, 0, getWidth(), getHeight());
                    windui.paint((Graphics2D) g, getWidth(), getHeight());
                }
            };
            windui.install(p);
            new javax.swing.Timer(16, e -> p.repaint()).start();
            f.setContentPane(p); f.setSize(1100, 700); f.setLocationRelativeTo(null); f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
}
