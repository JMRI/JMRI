package jmri.jmrix.can.cbus.swing;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.TestTrafficController;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of CbusMenu
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusMenuTest {


    private TrafficController tc = null;
    private CanSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        CbusMenu action = new CbusMenu(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new TestTrafficController();
        m = new CanSystemConnectionMemo();
        m.setSystemPrefix("ABC");

    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
        tc = null;
    }
}
