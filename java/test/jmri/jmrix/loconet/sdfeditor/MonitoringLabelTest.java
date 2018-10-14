package jmri.jmrix.loconet.sdfeditor;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.sdfeditor.MonitoringLabel class.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class MonitoringLabelTest {

    @Test
    public void testShowPane() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MonitoringLabel p = new MonitoringLabel();
        java.beans.PropertyChangeEvent e
                = new java.beans.PropertyChangeEvent(this, "Event", "old content", "new content");
        JFrame f = new JFrame();
        f.getContentPane().add(p);
        f.setVisible(true);
        p.propertyChange(e);

        Assert.assertEquals("check content", "new content", p.getText());
        f.dispose();
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
