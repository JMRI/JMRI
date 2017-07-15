package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.display.layoutEditor.LayoutBlock;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LayoutBlockThroughPathsTableModelTest {

    @Test
    public void testCTor() {
        LayoutBlock  b = new LayoutBlock("test","test");
        LayoutBlockThroughPathsTableModel t = new LayoutBlockThroughPathsTableModel(false,b);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutBlockThroughPathsTableModelTest.class.getName());

}
