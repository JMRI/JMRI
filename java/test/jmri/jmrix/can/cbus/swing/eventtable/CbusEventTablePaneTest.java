package jmri.jmrix.can.cbus.swing.eventtable;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusEventTablePane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusEventTablePaneTest {

    @Test
    public void testCtor() {
        CbusEventTablePane pane = new CbusEventTablePane();
        Assert.assertNotNull("exists", pane);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
