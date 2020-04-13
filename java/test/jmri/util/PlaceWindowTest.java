package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

import javax.swing.Box;
import javax.swing.JFrame;
/**
 *
 * @author Pete Cressman 2020   
 */
public class PlaceWindowTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PlaceWindow t = new PlaceWindow();
        Assert.assertNotNull("exists",t);
    }

    @Test
//    @Ignore("Placement point dependent on GraphicsEnvironment and test screen size")
    public void testNextTo() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame w1 = new JFrame();
        w1.getContentPane().add(Box.createRigidArea(new Dimension(400, 400)));
        w1.setLocation(200, 200);
        w1.pack();
        w1.setVisible(true);
        
        JFrame w2 = new JFrame();
        w2.getContentPane().add(Box.createRigidArea(new Dimension(200, 200)));
        w2.setLocation(100, 100);
        w2.pack();
        w2.setVisible(true);

        PlaceWindow pw = new PlaceWindow();
        Point pt = pw.nextTo(w1,  null,  w2);        // w2 to the right of w1
        // weaken assertion for test environments with different screen environment
        Assert.assertTrue("pt.x=", pt.x >= 600);
        Assert.assertEquals("pt.y=", 300, pt.y);
        
        w1.setLocation(300, 200);
        pt = pw.nextTo(w1,  null,  w2);        // w2 to the left of w1
        // weaken assertion for different screen sizes
        Assert.assertTrue("pt.x=", pt.x <= 100);
        Assert.assertEquals("pt.y=", 300, pt.y);
        
        w1.dispose();
        w2.dispose();
    }
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
