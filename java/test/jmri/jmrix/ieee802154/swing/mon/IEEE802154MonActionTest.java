package jmri.jmrix.ieee802154.swing.mon;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of IEEE802154MonAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IEEE802154MonActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IEEE802154MonAction action = new IEEE802154MonAction("IEEE 802.15.4 test Action", new IEEE802154SystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IEEE802154MonAction action = new IEEE802154MonAction( new IEEE802154SystemConnectionMemo());
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
