package jmri.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

import javax.swing.Box;
import javax.swing.JFrame;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Pete Cressman 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class PlaceWindowTest {

    private final int tolerance = 5;

    @Test
    public void testCTor() {
        PlaceWindow t = new PlaceWindow();
        assertThat(t).withFailMessage("exiists").isNotNull();
    }

    @Test
    public void testNextTo() {
        JFrame w1 = createFrameWitnDimentionsAtPoint(400, 400, 200, 200);
        JFrame w2 = createFrameWitnDimentionsAtPoint(200,200,100,100);

        PlaceWindow pw = new PlaceWindow();
        Point pt = pw.nextTo(w1,  null,  w2);        // w2 to the right of w1
        // weaken assertion for test environments with different screen environment
        assertThat(pt.x).isGreaterThanOrEqualTo(600);
        assertThat(pt.y).isBetween(300- tolerance,300+ tolerance);

        w1.setLocation(300, 200);
        pt = pw.nextTo(w1,  null,  w2);        // w2 to the left of w1
        // weaken assertion for different screen sizes
        assertThat(pt.x).isLessThanOrEqualTo(100);
        assertThat(pt.y).isBetween(300- tolerance,300+ tolerance);

        w1.dispose();
        w2.dispose();
    }

    private JFrame createFrameWitnDimentionsAtPoint(int width, int height, int x, int y) {
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
        JFrame w1 = createFrameWitnDimentionsAtPoint(rect.width - 20, rect.height - 60, 10, 50);
        JFrame w2 = createFrameWitnDimentionsAtPoint(200, 200, 100, 100);
        PlaceWindow pw = new PlaceWindow();

        Point pt = pw.nextTo(w1,  null,  w2);
        // w2 position should be centered within a pixel and overlap from above
        //System.out.println(Math.abs((rect.width/2-100) - pt.x));
        assertThat(Math.abs(((rect.width/2)-100) - pt.x))
                .withFailMessage("pt.x at screen center")
                .isLessThanOrEqualTo(tolerance);
        assertThat(pt.y).withFailMessage("pt.y at screen top").isEqualTo(0);
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
