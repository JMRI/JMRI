package jmri.jmrit.display.controlPanelEditor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logix.OBlock;
/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PortalListTest {

    @Test
    public void testCTor() {
        PortalList t = new PortalList( new OBlock("OB1", "Test"));
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

    private final static Logger log = LoggerFactory.getLogger(PortalListTest.class.getName());

}
