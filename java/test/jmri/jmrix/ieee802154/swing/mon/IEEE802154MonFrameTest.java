package jmri.jmrix.ieee802154.swing.mon;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of IEEE802154MonFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IEEE802154MonFrameTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IEEE802154MonFrame action = new IEEE802154MonFrame(new IEEE802154SystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        InstanceManager.setDefault(IEEE802154SystemConnectionMemo.class, new IEEE802154SystemConnectionMemo());
        IEEE802154MonFrame action = new IEEE802154MonFrame();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
