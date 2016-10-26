package jmri.jmrix.can.cbus.swing.eventtable;

import apps.tests.Log4JFixture;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of CbusEventTablePane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusEventTablePaneTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusEventTablePane pane = new CbusEventTablePane();
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
