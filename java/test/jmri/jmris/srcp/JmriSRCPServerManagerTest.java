package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


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
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
