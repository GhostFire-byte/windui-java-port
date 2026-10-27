package windui;   // <- change to your own package (or delete this line)

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.swing.JComponent;

/**
 * WindUi - a WindUI-style (Roblox UI library) window drawn with plain Java2D. No dependencies, one file.
 *
 *  - Draggable window: title bar with icon / title / subtitle and minimize, maximize, close buttons.
 *  - Sidebar with tabs (icons, optional section labels) and a user card.
 *  - Content panel with a header, smooth scrolling and module "cards" that expand with an eased animation.
 *  - Rows: switch, slider (with value pill), button, keybind, dropdown, info text.
 *  - 10 themes with WindUI's exact colours (Dark, Light, Rose, Plant, Red, Indigo, Sky, Violet, Amber, Emerald).
 *  - Hover / switch / expand animations (exponential easing, frame-rate independent).
 *
 * You own the game loop: call paint() every frame, forward mouse events, and read your own values through the
 * getters / setters you pass in. See WindUiDemo.java for a complete example.
 */
public final class WindUi {

    // =====================================================================================================================
    //  THEMES  (values copied from WindUI's theme table)
    // =====================================================================================================================
    public static final class Theme {
        public final String name;
        public final Color bg, text, placeholder, button, icon;
        public Theme(String name, String bg, String text, String placeholder, String button, String icon) {
            this.name = name; this.bg = hex(bg); this.text = hex(text); this.placeholder = hex(placeholder); this.button = hex(button); this.icon = hex(icon);
        }
    }
    public static final Theme[] THEMES = {
        new Theme("Dark",    "#101010", "#FFFFFF", "#999999", "#52525b", "#a1a1aa"),
        new Theme("Light",   "#e4e4e7", "#000000", "#777777", "#18181b", "#52525b"),
        new Theme("Rose",    "#881337", "#ffe4e6", "#fda4af", "#e11d48", "#fecdd3"),
        new Theme("Plant",   "#14532d", "#dcfce7", "#bbf7d0", "#22c55e", "#86efac"),
        new Theme("Red",     "#7f1d1d", "#ffe4e6", "#fca5a5", "#ef4444", "#fecaca"),
        new Theme("Indigo",  "#312e81", "#e0e7ff", "#a5b4fc", "#6366f1", "#c7d2fe"),
        new Theme("Sky",     "#075985", "#e0f2fe", "#7dd3fc", "#0ea5e9", "#bae6fd"),
        new Theme("Violet",  "#4c1d95", "#ede9fe", "#c4b5fd", "#8b5cf6", "#ddd6fe"),
        new Theme("Amber",   "#78350f", "#fef3c7", "#fcd34d", "#f59e0b", "#fde68a"),
        new Theme("Emerald", "#064e3b", "#d1fae5", "#6ee7b7", "#10b981", "#a7f3d0"),
    };
    private static Color hex(String s) { return new Color(Integer.parseInt(s.substring(1), 16)); }

    // =====================================================================================================================
    //  ICONS  (18x18, drawn as vectors so there are no image assets)
    // =====================================================================================================================
    public enum Icon {
        SWORD, EYE, GEAR, FRAME, BOLT, SHIELD, BAG, SLIDERS, STAR;

        void draw(Graphics2D g, double x, double y, Color c) {
            g.setColor(c);
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (this) {
                case SWORD:
                    g.draw(new Line2D.Double(x + 3, y + 15, x + 15, y + 3)); g.draw(new Line2D.Double(x + 3, y + 9, x + 9, y + 15));
                    g.draw(new Line2D.Double(x + 2, y + 16, x + 5, y + 13)); break;
                case EYE:
                    g.draw(new Ellipse2D.Double(x + 1, y + 4, 16, 10)); g.fill(new Ellipse2D.Double(x + 6.5, y + 6.5, 5, 5)); break;
                case GEAR:
                    g.draw(new Ellipse2D.Double(x + 5, y + 5, 8, 8));
                    for (int i = 0; i < 8; i++) {
                        double a = i * Math.PI / 4;
                        g.draw(new Line2D.Double(x + 9 + Math.cos(a) * 6, y + 9 + Math.sin(a) * 6, x + 9 + Math.cos(a) * 8, y + 9 + Math.sin(a) * 8));
                    }
                    break;
                case FRAME:
                    g.draw(new Line2D.Double(x + 2, y + 6, x + 2, y + 2)); g.draw(new Line2D.Double(x + 2, y + 2, x + 6, y + 2));
                    g.draw(new Line2D.Double(x + 12, y + 2, x + 16, y + 2)); g.draw(new Line2D.Double(x + 16, y + 2, x + 16, y + 6));
                    g.draw(new Line2D.Double(x + 2, y + 12, x + 2, y + 16)); g.draw(new Line2D.Double(x + 2, y + 16, x + 6, y + 16));
                    g.draw(new Line2D.Double(x + 16, y + 12, x + 16, y + 16)); g.draw(new Line2D.Double(x + 16, y + 16, x + 12, y + 16)); break;
                case BOLT: {
                    Path2D.Double p = new Path2D.Double();
                    p.moveTo(x + 10, y + 1); p.lineTo(x + 4, y + 10); p.lineTo(x + 9, y + 10); p.lineTo(x + 8, y + 17); p.lineTo(x + 14, y + 8); p.lineTo(x + 9, y + 8); p.closePath();
                    g.fill(p); break;
                }
                case SHIELD: {
                    Path2D.Double p = new Path2D.Double();
                    p.moveTo(x + 9, y + 2); p.lineTo(x + 15, y + 4.5); p.lineTo(x + 15, y + 9);
                    p.curveTo(x + 15, y + 13, x + 12, y + 15.5, x + 9, y + 16.5); p.curveTo(x + 6, y + 15.5, x + 3, y + 13, x + 3, y + 9);
                    p.lineTo(x + 3, y + 4.5); p.closePath(); g.draw(p); break;
                }
                case BAG:
                    g.draw(new RoundRectangle2D.Double(x + 3, y + 7, 12, 9, 3, 3)); g.draw(new Arc2D.Double(x + 6, y + 2.5, 6, 8, 0, 180, Arc2D.OPEN)); break;
                case SLIDERS:
                    for (int i = 0; i < 3; i++) {
                        double yy = y + 4 + i * 5; g.draw(new Line2D.Double(x + 2, yy, x + 16, yy));
                        g.fill(new Ellipse2D.Double(x + 4 + ((i * 5) % 9), yy - 2, 4, 4));
                    }
                    break;
                default: {
                    Path2D.Double p = new Path2D.Double();
                    for (int i = 0; i < 10; i++) {
                        double r = i % 2 == 0 ? 8 : 3.4, a = -Math.PI / 2 + i * Math.PI / 5;
                        if (i == 0) p.moveTo(x + 9 + Math.cos(a) * r, y + 9.5 + Math.sin(a) * r); else p.lineTo(x + 9 + Math.cos(a) * r, y + 9.5 + Math.sin(a) * r);
                    }
                    p.closePath(); g.draw(p);
                }
            }
        }
    }

    // =====================================================================================================================
    //  MODEL  (build it once, then keep calling paint)
    // =====================================================================================================================
    /** Marker for everything that can sit inside a card. */
    public interface Row { }

    public static final class Switch implements Row {
        final String label; final BooleanSupplier get; final Consumer<Boolean> set; double t = -1;
        Switch(String l, BooleanSupplier g, Consumer<Boolean> s) { label = l; get = g; set = s; }
    }
    public static final class SliderRow implements Row {
        final String label; final int min, max; final IntSupplier get; final IntConsumer set;
        SliderRow(String l, int mn, int mx, IntSupplier g, IntConsumer s) { label = l; min = mn; max = mx; get = g; set = s; }
    }
    public static final class ButtonRow implements Row {
        final Supplier<String> label; final Runnable run; double hover;
        ButtonRow(Supplier<String> l, Runnable r) { label = l; run = r; }
    }
    public static final class InfoRow implements Row {
        final Supplier<String> text;
        InfoRow(Supplier<String> t) { text = t; }
    }
    public static final class KeyRow implements Row {
        final String label; final Supplier<String> keyText; final BooleanSupplier waiting; final Runnable click; double hover;
        KeyRow(String l, Supplier<String> k, BooleanSupplier w, Runnable c) { label = l; keyText = k; waiting = w; click = c; }
    }
    public static final class DropdownRow implements Row {
        final String label; final List<String> options; final Supplier<String> get; final Consumer<String> set;
        boolean open; double openAnim, hover;
        DropdownRow(String l, List<String> o, Supplier<String> g, Consumer<String> s) { label = l; options = o; get = g; set = s; }
    }

    /** A module card: title, optional subtitle, optional master switch, expandable body of rows. */
    public static final class Card {
        final String title; Supplier<String> subtitle; BooleanSupplier enGet; Consumer<Boolean> enSet;
        boolean open; double openAnim, hoverAnim, enAnim = -1;
        final List<Row> rows = new ArrayList<>();
        Card(String t) { title = t; }
        public Card subtitle(Supplier<String> s) { subtitle = s; return this; }
        public Card subtitle(String s) { subtitle = () -> s; return this; }
        public Card enable(BooleanSupplier g, Consumer<Boolean> s) { enGet = g; enSet = s; return this; }
        public Card open(boolean o) { open = o; openAnim = o ? 1 : 0; return this; }
        public Card toggle(String label, BooleanSupplier g, Consumer<Boolean> s) { rows.add(new Switch(label, g, s)); return this; }
        public Card slider(String label, int min, int max, IntSupplier g, IntConsumer s) { rows.add(new SliderRow(label, min, max, g, s)); return this; }
        public Card button(String label, Runnable r) { rows.add(new ButtonRow(() -> label, r)); return this; }
        public Card button(Supplier<String> label, Runnable r) { rows.add(new ButtonRow(label, r)); return this; }
        public Card info(Supplier<String> text) { rows.add(new InfoRow(text)); return this; }
        public Card info(String text) { rows.add(new InfoRow(() -> text)); return this; }
        /** keyText = what to show on the button ("Numpad 5"); waiting = true while your code is capturing a key; click = start capturing. */
        public Card keybind(String label, Supplier<String> keyText, BooleanSupplier waiting, Runnable click) { rows.add(new KeyRow(label, keyText, waiting, click)); return this; }
        public Card dropdown(String label, List<String> options, Supplier<String> get, Consumer<String> set) { rows.add(new DropdownRow(label, options, get, set)); return this; }
    }

    public static final class Tab {
        final String name; final Icon icon; String labelBefore; double selAnim, hoverAnim;
        final List<Object> items = new ArrayList<>();           // Card or String (section heading)
        Tab(String n, Icon i) { name = n; icon = i; }
        public Tab labelBefore(String label) { labelBefore = label; return this; }
        public Card card(String title) { Card c = new Card(title); items.add(c); return c; }
        public Tab heading(String text) { items.add(text); return this; }
    }

    // =====================================================================================================================
    //  STATE
    // =====================================================================================================================
    private final List<Tab> tabs = new ArrayList<>();
    private Tab current;
    private String title, subtitle;
    private Supplier<String> userName = () -> "Player", userSub = () -> "";
    private Runnable onClose = () -> { }, onChange = () -> { };
    private Theme theme = THEMES[0];
    private String fontName = "Segoe UI";
    private double scale = 1.0;
    private boolean dimBackground = true, maximized, minimized, dragging;
    private int wx = -1, wy = -1, dragDX, dragDY;
    private double scroll, scrollTarget, maxScroll, dt = 0.016;
    private long lastNs = System.nanoTime();
    private int mx = -1000, my = -1000;
    private final Rectangle windowRect = new Rectangle();

    private static final class Hit {
        Rectangle r, clip; Consumer<Point> press; Consumer<Point> drag; Runnable release;
    }
    private final List<Hit> hits = new ArrayList<>();
    private Hit active;

    private static final int TOP = 60, SIDE = 196;

    public WindUi(String title, String subtitle) { this.title = title; this.subtitle = subtitle; }

    // ------------------------------------------------------------------------ configuration
    public Tab tab(String name, Icon icon) { Tab t = new Tab(name, icon); tabs.add(t); if (current == null) current = t; return t; }
    public WindUi userName(Supplier<String> name, Supplier<String> sub) { userName = name; userSub = sub; return this; }
    public WindUi onClose(Runnable r) { onClose = r; return this; }
    /** Called after any value changed (switch, dropdown, slider released, theme) - a good place to save your settings. */
    public WindUi onChange(Runnable r) { onChange = r; return this; }
    public WindUi font(String family) { fontName = family; return this; }
    public WindUi scale(double s) { scale = s; return this; }
    public WindUi dimBackground(boolean b) { dimBackground = b; return this; }
    public Theme theme() { return theme; }
    public void setTheme(String name) { for (Theme t : THEMES) if (t.name.equalsIgnoreCase(name)) { theme = t; onChange.run(); } }
    public void nextTheme() { theme = THEMES[(Arrays.asList(THEMES).indexOf(theme) + 1) % THEMES.length]; onChange.run(); }
    public static List<String> themeNames() { List<String> l = new ArrayList<>(); for (Theme t : THEMES) l.add(t.name); return l; }

    /** Window position, tab, theme and expanded cards, for your settings file. */
    public void saveState(Map<String, String> out) {
        out.put("windui.theme", theme.name); out.put("windui.x", String.valueOf(wx)); out.put("windui.y", String.valueOf(wy));
        out.put("windui.tab", current == null ? "" : current.name);
        for (Tab t : tabs) for (Object it : t.items) if (it instanceof Card c) out.put("windui.open." + t.name + "." + c.title, String.valueOf(c.open));
    }
    public void loadState(Map<String, String> in) {
        try {
            if (in.containsKey("windui.theme")) setThemeQuiet(in.get("windui.theme"));
            wx = Integer.parseInt(in.getOrDefault("windui.x", "-1")); wy = Integer.parseInt(in.getOrDefault("windui.y", "-1"));
            for (Tab t : tabs) {
                if (t.name.equals(in.get("windui.tab"))) current = t;
                for (Object it : t.items) if (it instanceof Card c && in.containsKey("windui.open." + t.name + "." + c.title)) c.open(Boolean.parseBoolean(in.get("windui.open." + t.name + "." + c.title)));
            }
        } catch (RuntimeException ignored) { }
    }
    private void setThemeQuiet(String name) { for (Theme t : THEMES) if (t.name.equalsIgnoreCase(name)) theme = t; }

    // =====================================================================================================================
    //  INPUT  (coordinates are in the same space you paint in; return value = "the UI used this event")
    // =====================================================================================================================
    public boolean mousePressed(int x, int y) {
        Point p = new Point((int) (x / scale), (int) (y / scale));
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.r.contains(p) || (h.clip != null && !h.clip.contains(p))) continue;
            if (h.press != null) h.press.accept(p);
            active = (h.drag != null || h.release != null) ? h : null;
            return true;
        }
        return false;
    }
    public void mouseDragged(int x, int y) {
        mx = (int) (x / scale); my = (int) (y / scale);
        if (dragging) { wx = mx - dragDX; wy = my - dragDY; }
        else if (active != null && active.drag != null) active.drag.accept(new Point(mx, my));
    }
    public void mouseReleased(int x, int y) {
        dragging = false;
        if (active != null && active.release != null) active.release.run();
        active = null;
    }
    public void mouseMoved(int x, int y) { mx = (int) (x / scale); my = (int) (y / scale); }
    /** rotation: +1 / -1 per wheel notch (MouseWheelEvent.getWheelRotation()). */
    public boolean mouseWheel(int x, int y, int rotation) {
        if (!windowRect.contains((int) (x / scale), (int) (y / scale))) return false;
        scrollTarget += rotation * 70;
        return true;
    }
    /** Convenience: wires paint-independent mouse listeners to a Swing component and repaints it. */
    public void install(JComponent c) {
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { WindUi.this.mousePressed(e.getX(), e.getY()); c.repaint(); }
            @Override public void mouseReleased(MouseEvent e) { WindUi.this.mouseReleased(e.getX(), e.getY()); c.repaint(); }
            @Override public void mouseDragged(MouseEvent e) { WindUi.this.mouseDragged(e.getX(), e.getY()); c.repaint(); }
            @Override public void mouseMoved(MouseEvent e) { WindUi.this.mouseMoved(e.getX(), e.getY()); c.repaint(); }
            @Override public void mouseWheelMoved(MouseWheelEvent e) { WindUi.this.mouseWheel(e.getX(), e.getY(), e.getWheelRotation()); c.repaint(); }
        };
        c.addMouseListener(ma); c.addMouseMotionListener(ma); c.addMouseWheelListener(ma);
    }

    // =====================================================================================================================
    //  DRAWING HELPERS
    // =====================================================================================================================
    private Font font(int style, int size) { return new Font(fontName, style, size); }
    private static Color a(Color c, int alpha) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha))); }
    private static Color lerp(Color x, Color y, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color((int) (x.getRed() + (y.getRed() - x.getRed()) * t), (int) (x.getGreen() + (y.getGreen() - x.getGreen()) * t),
                         (int) (x.getBlue() + (y.getBlue() - x.getBlue()) * t), (int) (x.getAlpha() + (y.getAlpha() - x.getAlpha()) * t));
    }
    private double ease(double cur, double target, double speed) { return cur + (target - cur) * (1 - Math.exp(-dt * speed)); }
    private static void rr(Graphics2D g, double x, double y, double w, double h, double arc) { g.fill(new RoundRectangle2D.Double(x, y, w, h, arc, arc)); }
    private static void rro(Graphics2D g, double x, double y, double w, double h, double arc) { g.draw(new RoundRectangle2D.Double(x + .5, y + .5, w - 1, h - 1, arc, arc)); }
    private boolean hover(Rectangle r, Rectangle clip) { return r.contains(mx, my) && (clip == null || clip.contains(mx, my)); }
    private static String fit(Graphics2D g, String s, int maxW) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= maxW) return s;
        while (s.length() > 1 && fm.stringWidth(s + "...") > maxW) s = s.substring(0, s.length() - 1);
        return s + "...";
    }
    private Hit hit(Rectangle r, Rectangle clip, Consumer<Point> press) { Hit h = new Hit(); h.r = new Rectangle(r); h.clip = clip; h.press = press; hits.add(h); return h; }
    private void changed() { onChange.run(); }

    private void drawSwitch(Graphics2D g, int x, int y, int w, int h, double t) {
        Theme T = theme;
        g.setColor(lerp(a(T.text, 18), T.button, t)); rr(g, x, y, w, h, h);                          // fill fades from a faint tint to the button colour
        g.setColor(a(T.text, (int) (12 + 22 * t))); g.setStroke(new BasicStroke(1f)); rro(g, x, y, w, h, h);
        int k = h - 8; double kx = x + 4 + t * (w - 8 - k);                                          // knob slides
        g.setColor(a(Color.BLACK, 40)); g.fill(new Ellipse2D.Double(kx, y + 5, k, k));
        g.setColor(Color.WHITE); g.fill(new Ellipse2D.Double(kx, y + 4, k, k));
    }

    private void chevron(Graphics2D g, int cx, int cy, double open) {
        g.setColor(theme.icon); g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double ang = open * Math.PI / 2;                                                              // rotates from ">" to "v"
        AffineTransform old = g.getTransform();
        g.rotate(ang, cx, cy);
        Path2D.Double p = new Path2D.Double(); p.moveTo(cx - 2, cy - 4); p.lineTo(cx + 2, cy); p.lineTo(cx - 2, cy + 4);
        g.draw(p);
        g.setTransform(old);
    }

    // =====================================================================================================================
    //  PAINT
    // =====================================================================================================================
    public void paint(Graphics2D g0, int w, int h) {
        if (tabs.isEmpty()) return;
        Graphics2D g = (Graphics2D) g0.create();
        try {
            long ns = System.nanoTime(); dt = Math.min(0.1, (ns - lastNs) / 1e9); lastNs = ns;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.scale(scale, scale);
            int W = (int) (w / scale), H = (int) (h / scale);
            hits.clear();
            Theme T = theme;
            if (dimBackground) { g.setColor(new Color(0, 0, 0, 90)); g.fillRect(0, 0, W, H); }

            int ww, wh, x, y;
            if (maximized) { x = 30; y = 30; ww = W - 60; wh = H - 60; }
            else {
                ww = Math.min(800, W - 40); wh = Math.min(560, H - 40);
                if (wx < 0) { wx = (W - ww) / 2; wy = (H - wh) / 2; }
                wx = Math.max(0, Math.min(wx, W - 140)); wy = Math.max(0, Math.min(wy, H - 70));
                x = wx; y = wy;
            }
            if (minimized) wh = TOP;
            windowRect.setBounds(x, y, ww, wh);

            for (int i = 9; i >= 1; i--) { g.setColor(new Color(0, 0, 0, 8)); rr(g, x - i, y - i + 4, ww + 2 * i, wh + 2 * i, 34 + i); }     // soft shadow
            g.setColor(a(T.bg, 250)); rr(g, x, y, ww, wh, 32);                                                                                // corner radius 16
            g.setColor(a(T.text, 24)); g.setStroke(new BasicStroke(1f)); rro(g, x, y, ww, wh, 32);
            hit(windowRect, null, p -> { });                                                                                                   // swallow clicks inside the window

            // ------------------------------------------------ title bar
            g.setColor(a(T.text, 20)); rr(g, x + 18, y + 13, 34, 34, 20);
            Icon.BOLT.draw(g, x + 26, y + 21, T.text);
            g.setFont(font(Font.BOLD, 17)); g.setColor(T.text); g.drawString(title, x + 64, y + 28);
            g.setFont(font(Font.PLAIN, 12)); g.setColor(T.icon); g.drawString(subtitle, x + 64, y + 46);
            windowButton(g, new Rectangle(x + ww - 52, y + 13, 34, 34), "close", () -> { onClose.run(); });
            windowButton(g, new Rectangle(x + ww - 92, y + 13, 34, 34), "max", () -> { maximized = !maximized; scrollTarget = 0; });
            windowButton(g, new Rectangle(x + ww - 132, y + 13, 34, 34), "min", () -> minimized = !minimized);
            Hit drag = hit(new Rectangle(x, y, ww - 140, TOP), null, p -> { if (!maximized) { dragging = true; dragDX = p.x - wx; dragDY = p.y - wy; } });
            drag.release = () -> { dragging = false; changed(); };
            if (minimized) return;

            // ------------------------------------------------ sidebar
            int sx = x + 12, sy = y + TOP + 4, sw = SIDE - 12;
            boolean firstTab = true;
            for (Tab t : tabs) {
                String label = t.labelBefore != null ? t.labelBefore : (firstTab ? "MODULES" : null);
                if (label != null) {
                    if (!firstTab) sy += 6;
                    g.setFont(font(Font.PLAIN, 12)); g.setColor(T.placeholder); g.drawString(label, sx + 8, sy + 12); sy += 24;
                }
                firstTab = false;
                Rectangle tr = new Rectangle(sx, sy, sw, 38);
                boolean sel = t == current;
                t.hoverAnim = ease(t.hoverAnim, hover(tr, null) ? 1 : 0, 22); t.selAnim = ease(t.selAnim, sel ? 1 : 0, 20);
                g.setColor(a(T.text, (int) (t.selAnim * 20 + t.hoverAnim * 9))); rr(g, tr.x, tr.y, tr.width, tr.height, 24);
                g.setColor(a(T.text, (int) (t.selAnim * 34))); g.setStroke(new BasicStroke(1f)); rro(g, tr.x, tr.y, tr.width, tr.height, 24);
                t.icon.draw(g, tr.x + 14, tr.y + 10, lerp(T.icon, T.text, t.selAnim));
                g.setFont(font(sel ? Font.BOLD : Font.PLAIN, 14)); g.setColor(lerp(T.icon, T.text, Math.max(t.selAnim, t.hoverAnim * 0.6)));
                g.drawString(t.name, tr.x + 44, tr.y + 24);
                hit(tr, null, p -> { current = t; scroll = 0; scrollTarget = 0; });
                sy += 42;
            }
            if (wh > 330) {                                                                                                               // user card
                int uy = y + wh - 12 - 56;
                g.setColor(a(T.text, 13)); rr(g, sx, uy, sw, 56, 24);
                g.setColor(a(T.text, 20)); g.setStroke(new BasicStroke(1f)); rro(g, sx, uy, sw, 56, 24);
                g.setColor(a(T.text, 36)); g.fill(new Ellipse2D.Double(sx + 10, uy + 10, 36, 36));
                String name = userName.get(); if (name == null || name.isEmpty()) name = "?";
                g.setFont(font(Font.BOLD, 16)); g.setColor(T.text);
                String ini = name.substring(0, 1).toUpperCase();
                g.drawString(ini, sx + 28 - g.getFontMetrics().stringWidth(ini) / 2, uy + 34);
                g.setFont(font(Font.BOLD, 14)); g.drawString(fit(g, name, sw - 64), sx + 54, uy + 25);
                g.setFont(font(Font.PLAIN, 12)); g.setColor(T.icon); g.drawString(fit(g, userSub.get(), sw - 64), sx + 54, uy + 42);
            }

            // ------------------------------------------------ content panel
            int cx = x + SIDE + 6, cy = y + TOP, cw = ww - SIDE - 6 - 12, ch = wh - TOP - 12;
            g.setColor(a(T.text, 13)); rr(g, cx, cy, cw, ch, 24);
            g.setColor(a(T.text, 20)); g.setStroke(new BasicStroke(1f)); rro(g, cx, cy, cw, ch, 24);
            current.icon.draw(g, cx + 20, cy + 19, T.icon);
            g.setFont(font(Font.BOLD, 19)); g.setColor(T.text); g.drawString(current.name, cx + 50, cy + 35);
            int nCards = 0; for (Object it : current.items) if (it instanceof Card) nCards++;
            g.setFont(font(Font.PLAIN, 12)); g.setColor(T.icon);
            String cnt = nCards + (nCards == 1 ? " module" : " modules");
            g.drawString(cnt, cx + cw - 20 - g.getFontMetrics().stringWidth(cnt), cy + 33);
            g.setColor(a(T.text, 26)); g.drawLine(cx, cy + 56, cx + cw, cy + 56);

            Rectangle clip = new Rectangle(cx + 1, cy + 58, cw - 2, ch - 60);
            Shape oldClip = g.getClip(); g.setClip(clip);
            scroll += (scrollTarget - scroll) * (1 - Math.exp(-dt * 18));                                                                    // smooth wheel scrolling
            int yy = clip.y + 12 - (int) Math.round(scroll), top = yy;
            for (Object it : current.items) {
                if (it instanceof Card c) yy += drawCard(g, c, cx + 14, yy, cw - 28 - 8, clip) + 10;
                else {
                    g.setFont(font(Font.BOLD, 15)); g.setColor(T.text); g.drawString((String) it, cx + 18, yy + 16); yy += 28;
                }
            }
            int contentH = yy - top;
            maxScroll = Math.max(0, contentH - (clip.height - 24));
            scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll)); scroll = Math.max(0, Math.min(scroll, maxScroll));
            if (maxScroll > 0) {
                int th = Math.max(36, (int) (clip.height * (double) clip.height / (contentH + 24)));
                int ty = clip.y + (int) ((clip.height - th) * (scroll / maxScroll));
                g.setColor(a(T.text, 55)); rr(g, cx + cw - 8, ty, 4, th, 4);
            }
            g.setClip(oldClip);
        } finally { g.dispose(); }
    }

    private void windowButton(Graphics2D g, Rectangle r, String kind, Runnable onClick) {
        double hv = hover(r, null) ? 1 : 0;
        g.setColor(a(theme.text, (int) (hv * 30))); rr(g, r.x, r.y, r.width, r.height, 20);
        g.setColor(theme.icon); g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = r.x + r.width / 2, cy = r.y + r.height / 2;
        switch (kind) {
            case "min": g.drawLine(cx - 6, cy, cx + 6, cy); break;
            case "max": g.draw(new RoundRectangle2D.Double(cx - 6, cy - 6, 12, 12, 4, 4)); break;
            default: g.drawLine(cx - 5, cy - 5, cx + 5, cy + 5); g.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
        }
        hit(r, null, p -> onClick.run());
    }

    private int rowHeight(Row r) {
        if (r instanceof Switch) return 34;
        if (r instanceof SliderRow) return 50;
        if (r instanceof ButtonRow) return 38;
        if (r instanceof InfoRow) return 24;
        if (r instanceof DropdownRow d) return 44 + (int) Math.round(d.openAnim * (d.options.size() * 30 + 10));
        return 38;                                                                                  // KeyRow
    }

    /** One module card; the body height is animated (eased) between 0 and its full height. */
    private int drawCard(Graphics2D g, Card c, int x, int yy, int w, Rectangle clip) {
        Theme T = theme;
        String sub = c.subtitle == null ? null : c.subtitle.get();
        int hdr = sub != null && !sub.isEmpty() ? 58 : 46;
        c.openAnim = ease(c.openAnim, c.open ? 1 : 0, 14);
        int bodyFull = 16; for (Row r : c.rows) bodyFull += rowHeight(r);
        int body = c.openAnim > 0.004 ? (int) Math.round(bodyFull * c.openAnim) : 0;
        int total = hdr + body;
        Rectangle hdrR = new Rectangle(x, yy, w, hdr);
        c.hoverAnim = ease(c.hoverAnim, hover(hdrR, clip) ? 1 : 0, 22);
        g.setColor(a(T.text, (int) (13 + 14 * c.hoverAnim))); rr(g, x, yy, w, total, 24);                    // card = text colour at 5%, 10% on hover
        g.setColor(a(T.text, 20)); g.setStroke(new BasicStroke(1f)); rro(g, x, yy, w, total, 24);

        boolean hasEn = c.enGet != null, on = hasEn && c.enGet.getAsBoolean();
        g.setFont(font(Font.BOLD, 15)); g.setColor(!hasEn || on ? T.text : a(T.text, 175));
        g.drawString(fit(g, c.title, w - 110), x + 18, yy + (sub != null && !sub.isEmpty() ? 26 : 29));
        if (sub != null && !sub.isEmpty()) { g.setFont(font(Font.PLAIN, 12)); g.setColor(T.icon); g.drawString(fit(g, sub, w - 110), x + 18, yy + 44); }
        chevron(g, x + w - 22, yy + hdr / 2, c.openAnim);
        hit(hdrR, clip, p -> { c.open = !c.open; changed(); });
        if (hasEn) {
            if (c.enAnim < 0) c.enAnim = on ? 1 : 0;
            c.enAnim = ease(c.enAnim, on ? 1 : 0, 18);
            int sx = x + w - 40 - 46, sy = yy + (hdr - 26) / 2;
            drawSwitch(g, sx, sy, 46, 26, c.enAnim);
            hit(new Rectangle(sx - 4, sy - 4, 54, 34), clip, p -> { c.enSet.accept(!c.enGet.getAsBoolean()); changed(); });
        }
        if (body > 0) {
            Shape old = g.getClip();
            Rectangle bodyClip = new Rectangle(x, yy + hdr, w, body).intersection(clip);
            g.clipRect(bodyClip.x, bodyClip.y, bodyClip.width, bodyClip.height);
            g.setColor(a(T.text, 22)); g.drawLine(x + 12, yy + hdr, x + w - 12, yy + hdr);
            int iy = yy + hdr + 8, mark = hits.size();
            for (Row r : c.rows) iy += drawRow(g, r, x, iy, w, bodyClip);
            if (c.openAnim < 0.9) while (hits.size() > mark) hits.remove(hits.size() - 1);              // no clicking while it is still opening
            g.setClip(old);
        }
        return total;
    }

    private int drawRow(Graphics2D g, Row row, int cx, int y, int cw, Rectangle clip) {
        Theme T = theme;
        int ix = cx + 16, iw = cw - 32;
        if (row instanceof Switch s) {
            boolean on = s.get.getAsBoolean();
            if (s.t < 0) s.t = on ? 1 : 0;
            s.t = ease(s.t, on ? 1 : 0, 18);
            g.setFont(font(Font.PLAIN, 13)); g.setColor(T.text); g.drawString(fit(g, s.label, iw - 54), ix, y + 22);
            drawSwitch(g, ix + iw - 38, y + 5, 38, 24, s.t);
            hit(new Rectangle(cx + 6, y, cw - 12, 34), clip, p -> { s.set.accept(!s.get.getAsBoolean()); changed(); });
            return 34;
        }
        if (row instanceof SliderRow s) {
            int v = s.get.getAsInt(), x0 = ix + 2, x1 = ix + iw - 2, ty = y + 38;
            double frac = Math.max(0, Math.min(1, (v - s.min) / (double) (s.max - s.min)));
            int kx = x0 + (int) ((x1 - x0) * frac);
            g.setFont(font(Font.PLAIN, 13)); g.setColor(T.text); g.drawString(fit(g, s.label, iw - 70), ix, y + 18);
            String vs = String.valueOf(v);                                                                                          // value pill (WindUI's textbox)
            g.setFont(font(Font.BOLD, 12));
            int pw = Math.max(40, g.getFontMetrics().stringWidth(vs) + 18);
            g.setColor(a(T.text, 13)); rr(g, ix + iw - pw, y + 3, pw, 22, 16);
            g.setColor(a(T.text, 36)); g.setStroke(new BasicStroke(1f)); rro(g, ix + iw - pw, y + 3, pw, 22, 16);
            g.setColor(T.text); g.drawString(vs, ix + iw - pw + (pw - g.getFontMetrics().stringWidth(vs)) / 2, y + 18);
            g.setColor(a(T.text, 24)); rr(g, x0, ty - 2, x1 - x0, 4, 4);                                                            // 4 px track
            g.setColor(a(T.button, 235)); rr(g, x0, ty - 2, Math.max(4, kx - x0), 4, 4);
            g.setColor(a(Color.BLACK, 40)); g.fill(new Ellipse2D.Double(kx - 6.5, ty - 5.5, 13, 13));
            g.setColor(Color.WHITE); g.fill(new Ellipse2D.Double(kx - 6.5, ty - 6.5, 13, 13));                                      // 13 px knob
            Consumer<Point> setFromX = p -> {
                double f = Math.max(0, Math.min(1, (p.x - x0) / (double) (x1 - x0)));
                s.set.accept(s.min + (int) Math.round(f * (s.max - s.min)));
            };
            Hit h = hit(new Rectangle(x0 - 8, y + 26, x1 - x0 + 16, 24), clip, setFromX);
            h.drag = setFromX; h.release = this::changed;
            return 50;
        }
        if (row instanceof ButtonRow b) {
            Rectangle r = new Rectangle(ix, y + 3, iw, 32);
            b.hover = ease(b.hover, hover(r, clip) ? 1 : 0, 22);
            g.setColor(a(T.text, (int) (13 + 16 * b.hover))); rr(g, r.x, r.y, r.width, r.height, 20);
            g.setColor(a(T.text, 38)); g.setStroke(new BasicStroke(1f)); rro(g, r.x, r.y, r.width, r.height, 20);
            g.setFont(font(Font.BOLD, 13)); g.setColor(T.text);
            String t = fit(g, b.label.get(), r.width - 16);
            g.drawString(t, r.x + (r.width - g.getFontMetrics().stringWidth(t)) / 2, r.y + 21);
            hit(r, clip, p -> { b.run.run(); changed(); });
            return 38;
        }
        if (row instanceof InfoRow i) {
            g.setFont(font(Font.PLAIN, 12)); g.setColor(T.icon); g.drawString(fit(g, i.text.get(), iw), ix, y + 16);
            return 24;
        }
        if (row instanceof KeyRow k) {
            boolean wait = k.waiting.getAsBoolean();
            g.setFont(font(Font.PLAIN, 13)); g.setColor(T.text); g.drawString(fit(g, k.label, iw - 130), ix, y + 23);
            Rectangle r = new Rectangle(ix + iw - 120, y + 5, 120, 28);
            k.hover = ease(k.hover, hover(r, clip) ? 1 : 0, 22);
            g.setColor(wait ? a(T.button, 255) : a(T.text, (int) (13 + 14 * k.hover))); rr(g, r.x, r.y, r.width, r.height, 20);
            g.setColor(a(T.text, wait ? 70 : 38)); g.setStroke(new BasicStroke(1f)); rro(g, r.x, r.y, r.width, r.height, 20);
            g.setFont(font(Font.BOLD, 12)); g.setColor(wait ? Color.WHITE : T.text);
            String t = wait ? "press a key..." : k.keyText.get();
            g.drawString(t, r.x + (r.width - g.getFontMetrics().stringWidth(t)) / 2, r.y + 18);
            hit(r, clip, p -> k.click.run());
            return 38;
        }
        if (row instanceof DropdownRow d) {
            d.openAnim = ease(d.openAnim, d.open ? 1 : 0, 16);
            if (!d.open && d.openAnim < 0.004) d.openAnim = 0;
            g.setFont(font(Font.PLAIN, 13)); g.setColor(T.text); g.drawString(fit(g, d.label, iw - 170), ix, y + 24);
            Rectangle r = new Rectangle(ix + iw - 160, y + 5, 160, 30);
            d.hover = ease(d.hover, hover(r, clip) ? 1 : 0, 22);
            g.setColor(a(T.text, (int) (13 + 14 * d.hover))); rr(g, r.x, r.y, r.width, r.height, 20);
            g.setColor(a(T.text, 38)); g.setStroke(new BasicStroke(1f)); rro(g, r.x, r.y, r.width, r.height, 20);
            g.setFont(font(Font.PLAIN, 13)); g.setColor(T.text); g.drawString(fit(g, d.get.get(), r.width - 44), r.x + 14, r.y + 20);
            chevron(g, r.x + r.width - 18, r.y + 15, d.openAnim);
            hit(r, clip, p -> d.open = !d.open);
            if (d.openAnim > 0.004) {
                int listH = (int) Math.round((d.options.size() * 30 + 10) * d.openAnim);
                Shape old = g.getClip();
                g.clipRect(Math.max(r.x - 4, clip.x), Math.max(y + 40, clip.y), r.width + 8, Math.min(listH + 2, clip.y + clip.height - Math.max(y + 40, clip.y)));
                g.setColor(a(T.text, 13)); rr(g, r.x, y + 40, r.width, d.options.size() * 30 + 10, 20);
                g.setColor(a(T.text, 38)); g.setStroke(new BasicStroke(1f)); rro(g, r.x, y + 40, r.width, d.options.size() * 30 + 10, 20);
                int oy = y + 45; String cur = d.get.get();
                for (String o : d.options) {
                    Rectangle or = new Rectangle(r.x + 4, oy, r.width - 8, 28);
                    boolean sel = o.equals(cur), hov = hover(or, clip);
                    if (sel || hov) { g.setColor(a(T.text, sel ? 24 : 14)); rr(g, or.x, or.y, or.width, or.height, 16); }
                    g.setFont(font(sel ? Font.BOLD : Font.PLAIN, 13)); g.setColor(T.text); g.drawString(fit(g, o, or.width - 24), or.x + 12, or.y + 19);
                    if (d.openAnim > 0.9) hit(or, clip, p -> { d.set.accept(o); d.open = false; changed(); });
                    oy += 30;
                }
                g.setClip(old);
            }
            return rowHeight(d);
        }
        return 0;
    }
}
