package jmri.jmrit.display.controlPanelEditor;

import jmri.jmrit.logix.OBlock;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PortalListTest {

    @Test
    public void testCTor() {
        PortalList t = new PortalList( new OBlock("OB1", "Test"), null);
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

    // private final static Logger log = LoggerFactory.getLogger(PortalListTest.class);

}
