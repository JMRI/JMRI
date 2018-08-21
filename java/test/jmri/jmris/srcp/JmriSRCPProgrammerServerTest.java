package jmri.jmris.srcp;

import org.junit.*;
import org.junit.Test;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPProgrammerServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPProgrammerServerTest{

    @Test
    public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        JmriSRCPProgrammerServer a = new JmriSRCPProgrammerServer(output);
        Assert.assertNotNull(a);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
