package jmri.jmrix.can.cbus.swing;

import jmri.jmrix.can.cbus.CbusFilterType;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019,2020
 */
public class CbusFilterPanelTest {

    @Test
    public void testCTorNode() {
        CbusFilterPanel t = new CbusFilterPanel(null,1);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorFilter() {
        CbusFilterPanel t = new CbusFilterPanel(null,CbusFilterType.CFIN);
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(CbusEventFilterTest.class);

}
