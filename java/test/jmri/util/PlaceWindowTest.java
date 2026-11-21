package jmri.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.Box;
import javax.swing.JFrame;

import static org.assertj.core.api.Assertions.assertThat;

import org.mockito.Mockito;

/**
 *
 * @author Pete Cressman 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class PlaceWindowTest {

    private static final int TOLERANCE = 5;

    @Test
    public void testCTor() {
        PlaceWindow t = new PlaceWindow();
        assertThat(t).withFailMessage("exiists").isNotNull();
    }

    @Test
    public void testNextTo() {
        JFrame w1 = createFrameWithDimentionsAtPoint(400, 400, 200, 200);
        JFrame w2 = createFrameWithDimentionsAtPoint(200,200,100,100);

        PlaceWindow pw = new PlaceWindow();
        Point pt = pw.nextTo(w1,  null,  w2);        // w2 to the right of w1
        // weaken assertion for test environments with different screen environment
        assertThat(pt.x).isGreaterThanOrEqualTo(600);
        assertThat(pt.y).isBetween(300- TOLERANCE,300+ TOLERANCE);

        w1.setLocation(300, 200);
        pt = pw.nextTo(w1,  null,  w2);        // w2 to the left of w1
        // weaken assertion for different screen sizes
        assertThat(pt.x).isLessThanOrEqualTo(100);
        assertThat(pt.y).isBetween(300- TOLERANCE,300+ TOLERANCE);

        JUnitUtil.dispose(w1);
        JUnitUtil.dispose(w2);
    }

    private JFrame createFrameWithDimentionsAtPoint(int width, int height, int x, int y) {
        JFrame w1 = new JFrame();
        w1.getContentPane().add(Box.createRigidArea(new Dimension(width, height)));
        w1.setLocation(x, y);
        w1.pack();
        w1.setVisible(true);
        return w1;
    }

    @Test
    public void testNextToBig() {
        java.awt.Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        JFrame w1 = createFrameWithDimentionsAtPoint(rect.width - 20, rect.height - 60, 10, 50);
        JFrame w2 = createFrameWithDimentionsAtPoint(200, 200, 100, 100);
        PlaceWindow pw = new PlaceWindow();

        Point pt = pw.nextTo(w1,  null,  w2);
        // w2 position should be centered within a pixel and overlap from above
        //System.out.println(Math.abs((rect.width/2-100) - pt.x));
        assertThat(Math.abs(((rect.width/2)-100) - pt.x))
                .withFailMessage("pt.x at screen center")
                .isLessThanOrEqualTo(TOLERANCE);
        assertThat(pt.y).withFailMessage("pt.y at screen top").isEqualTo(0);

        JUnitUtil.dispose(w1);
        JUnitUtil.dispose(w2);
    }

    @Test
    public void testScreenSizeBeyondLastScreen() {
        int screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
        PlaceWindow pw = new PlaceWindow();
        assertThat(pw.getScreenSize(screens - 1).width).withFailMessage("last screen has a width").isGreaterThan(0);
        assertThat(pw.getScreenSize(screens)).withFailMessage("no screen beyond the last").isEqualTo(new Dimension(0, 0));
        assertThat(pw.getScreenSize(-1)).withFailMessage("no screen before the first").isEqualTo(new Dimension(0, 0));
    }

    @Test
    public void testAnotherInstanceLeavesScreenSizesAlone() {
        PlaceWindow pw = new PlaceWindow();
        Dimension first = pw.getScreenSize(0);
        new PlaceWindow();
        assertThat(pw.getScreenSize(0)).withFailMessage("same screen size").isEqualTo(first);
    }

    /**
     * Screens as the system arranges them, for {@link PlaceWindow}.
     */
    private static GraphicsEnvironment screens(Rectangle... bounds) {
        GraphicsEnvironment env = Mockito.mock(GraphicsEnvironment.class);
        GraphicsDevice[] devices = new GraphicsDevice[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            GraphicsConfiguration gc = Mockito.mock(GraphicsConfiguration.class);
            Mockito.when(gc.getBounds()).thenReturn(bounds[i]);
            devices[i] = Mockito.mock(GraphicsDevice.class);
            Mockito.when(devices[i].getDefaultConfiguration()).thenReturn(gc);
            Mockito.when(devices[i].getIDstring()).thenReturn("screen " + i);
        }
        Mockito.when(env.getScreenDevices()).thenReturn(devices);
        return env;
    }

    /**
     * A window at a place, of a size, as far as {@link PlaceWindow} looks.
     */
    private static Window window(int x, int y, int width, int height) {
        Window w = Mockito.mock(Window.class);
        Mockito.when(w.getLocation()).thenReturn(new Point(x, y));
        Mockito.when(w.getSize()).thenReturn(new Dimension(width, height));
        Mockito.when(w.getWidth()).thenReturn(width);
        Mockito.when(w.getPreferredSize()).thenReturn(new Dimension(width, height));
        return w;
    }

    private static boolean onScreen(Rectangle screen, Point p, Dimension size) {
        return screen.contains(new Rectangle(p, size));
    }

    /**
     * Daniel's screens: one 1200 wide and 1900 high, then one 1900 wide and
     * 1200 high. Together 3100 x 1900, but below 1200 on the right is no
     * screen. Windows near the bottom of the tall screen, or on the wide
     * one, are kept on their own screen.
     */
    @Test
    public void testScreensOfDifferentShapes() {
        Rectangle tall = new Rectangle(0, 0, 1200, 1900);
        Rectangle wide = new Rectangle(1200, 0, 1900, 1200);
        PlaceWindow pw = new PlaceWindow(screens(tall, wide));

        // a parent low on the tall screen, at its right edge: to the right would be off it, and below 1200 nothing
        Window parent = window(900, 1500, 300, 300);
        Window target = window(0, 0, 400, 300);
        Point p = pw.nextTo(parent, null, target);
        assertThat(onScreen(tall, p, target.getSize())).withFailMessage("on the tall screen: " + p).isTrue();
        assertThat(p.x + 400).withFailMessage("beside the parent, to its left").isLessThanOrEqualTo(900);

        // a parent at the bottom of the wide screen, filling it to the right: the target stays on that screen
        parent = window(2500, 900, 600, 300);
        p = pw.nextTo(parent, null, target);
        assertThat(onScreen(wide, p, target.getSize())).withFailMessage("on the wide screen: " + p).isTrue();
        Mockito.verify(target).setLocation(p);

        // placed above a parent on the wide screen, the target is centred over it, not pushed to the screen's left edge
        parent = window(1300, 800, 1700, 400);
        p = pw.nextTo(parent, null, window(0, 0, 400, 300));
        assertThat(p).isEqualTo(new Point(1300 + (1700 - 400) / 2, 800 - 300));
    }

    /**
     * A screen to the left of the main one has negative x; one above it,
     * negative y.
     */
    @Test
    public void testScreensLeftOfAndAboveTheMainOne() {
        Rectangle main = new Rectangle(0, 0, 1920, 1080);
        Rectangle left = new Rectangle(-1280, 100, 1280, 1024);
        Rectangle above = new Rectangle(0, -1080, 1920, 1080);
        PlaceWindow pw = new PlaceWindow(screens(main, left, above));

        assertThat(pw.getScreenNum(window(-1000, 300, 200, 200))).isEqualTo(1);
        assertThat(pw.getScreenNum(window(500, -500, 200, 200))).isEqualTo(2);
        assertThat(pw.getScreenNum(window(500, 500, 200, 200))).isEqualTo(0);
        assertThat(pw.getScreenNum(window(-1000, 50, 200, 200)))
                .withFailMessage("above the left screen, on no screen: the nearest").isEqualTo(1);
        assertThat(pw.getScreenBounds(1)).isEqualTo(left);
        assertThat(pw.getScreenSize(1)).isEqualTo(new Dimension(1280, 1024));

        // a parent filling the left screen: the target goes on the left screen, not the main one
        Window target = window(0, 0, 400, 300);
        Point p = pw.nextTo(window(-1280, 100, 1280, 1024), null, target);
        assertThat(onScreen(left, p, target.getSize())).withFailMessage("on the left screen: " + p).isTrue();

        // a parent at the top of the screen above: kept on that screen
        p = pw.nextTo(window(0, -1080, 1920, 900), null, target);
        assertThat(onScreen(above, p, target.getSize())).withFailMessage("on the screen above: " + p).isTrue();
    }

    @Test
    public void testTargetBiggerThanTheScreenKeepsItsCornerOnIt() {
        Rectangle only = new Rectangle(0, 0, 800, 600);
        PlaceWindow pw = new PlaceWindow(screens(only));
        Point p = pw.nextTo(window(100, 100, 600, 400), null, window(0, 0, 1000, 700));
        assertThat(p).isEqualTo(new Point(0, 0));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
