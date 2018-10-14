package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerManager class 
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class JmriSRCPServerManagerTest {

    @Test
    public void testGetInstance() {
        JmriSRCPServerManager a = JmriSRCPServerManager.getInstance();
        Assert.assertNotNull(a);
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
