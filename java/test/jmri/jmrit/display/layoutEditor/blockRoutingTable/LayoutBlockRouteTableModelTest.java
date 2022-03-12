package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutBlockRouteTableModelTest.class);
}
