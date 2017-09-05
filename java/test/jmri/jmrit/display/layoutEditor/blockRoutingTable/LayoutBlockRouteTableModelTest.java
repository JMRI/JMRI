package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LayoutBlockRouteTableModelTest {

    @Test
    public void testCTor() {
        LayoutBlock  b = new LayoutBlock("test","test");
        LayoutBlockRouteTableModel t = new LayoutBlockRouteTableModel(false,b);
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
    // private final static Logger log = LoggerFactory.getLogger(LayoutBlockRouteTableModelTest.class);
}
