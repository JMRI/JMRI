package jmri.jmrix.can.cbus.swing.console;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusConsolePane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusConsolePaneTest {

    @Test
    public void testCtor() {
        CbusConsolePane pane = new CbusConsolePane();
        Assert.assertNotNull("exists", pane);
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
