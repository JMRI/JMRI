package jmri.jmrix.can.cbus.swing.eventtable;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of CbusEventTablePane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusEventTablePaneTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new CbusEventTablePane();
        title = "Event Table";
        helpTarget = "package.jmri.jmrix.can.cbus.CbusEventTablePane";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
