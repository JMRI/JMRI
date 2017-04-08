package jmri.jmrix.lenz.swing.lv102;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * LV102FrameTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.lv102.LV102Frame class
 *
 * @author	Paul Bender
 */
public class LV102FrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        LV102Frame f = new LV102Frame();
        Assert.assertNotNull(f);
    }

    @Test
    public void testCloseButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // an LV102 Internal Frame
        LV102Frame f = new LV102Frame(Bundle.getMessage("LV102Config"));
        f.setVisible(true);
        LV102FrameScaffold operator = new LV102FrameScaffold();
        operator.pushCloseButton();
    }



    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
