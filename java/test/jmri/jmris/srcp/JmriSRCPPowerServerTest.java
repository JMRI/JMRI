package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the jmri.jmris.srcp.JmriSRCPPowerServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPPowerServerTest extends jmri.jmris.AbstractPowerServerTestBase {
        
    private StringBuilder sb = null;

    // test parsing an ON status message.
    @Test
    public void testParseOnStatus() throws Exception {
        ps.parseStatus("1234 SET 0 POWER ON\n");
        Assert.assertEquals("Parse On Status Check",jmri.InstanceManager
                      .getDefault(jmri.PowerManager.class).getPower(),
                      jmri.PowerManager.ON);
        Assert.assertTrue("status as a result of parsing on", sb.toString().endsWith("100 INFO 0 POWER ON\n\r"));
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() throws Exception {
        ps.parseStatus("1234 SET 0 POWER OFF\n");
        Assert.assertEquals("Parse OFF Status Check",jmri.InstanceManager
                      .getDefault(jmri.PowerManager.class).getPower(),
                      jmri.PowerManager.OFF);
        Assert.assertTrue("status as a result of parsing off", sb.toString().endsWith("100 INFO 0 POWER OFF\n\r"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOnSent(){
            Assert.assertTrue("status as a result of on property change", sb.toString().endsWith("100 INFO 0 POWER ON\n\r"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOffSent(){
            Assert.assertTrue("status as a result of off property change", sb.toString().endsWith("100 INFO 0 POWER OFF\n\r"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkErrorStatusSent() {
        Assert.assertTrue("sendErrorStatus check", sb.toString().endsWith("499 ERROR unspecified error\n\r"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkUnknownStatusSent() {
        Assert.assertTrue("send Unknown Status check", sb.toString().endsWith("411 ERROR unknown value\n\r"));
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugPowerManager();
        sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        ps = new JmriSRCPPowerServer(output);
    }

    @After
    public void tearDown() {
        ps.dispose();
        ps = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
