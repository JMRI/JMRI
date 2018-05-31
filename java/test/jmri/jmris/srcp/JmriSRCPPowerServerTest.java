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
public class JmriSRCPPowerServerTest{

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

        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        Assert.assertNotNull(a);
    }

    // test sending an error message.
    @Test
    public void testSendErrorStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        try {
            a.sendErrorStatus();
            Assert.assertTrue("sendErrorStatus check", sb.toString().endsWith("499 ERROR unspecified error\n\r"));
        } catch (java.io.IOException ioe) {
            Assert.fail("Exception sending Error Status");
        }
    }

    // test sending an On Status.
    @Test
    public void testSendOnStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        try {
            a.sendStatus(jmri.PowerManager.ON);
            Assert.assertTrue("On Status check", sb.toString().endsWith("100 INFO 0 POWER ON\n\r"));
        } catch (java.io.IOException ioe) {
            Assert.fail("Exception sending On Status");
        }
    }

    // test sending an OFF Status.
    @Test
    public void testSendOffStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        try {
            a.sendStatus(jmri.PowerManager.OFF);
            Assert.assertTrue("send Off Status check", sb.toString().endsWith("100 INFO 0 POWER OFF\n\r"));
        } catch (java.io.IOException ioe) {
            Assert.fail("Exception sending Off Status");
        }
    }

    // test sending an Unknown Status.
    @Test
    public void testSendUnknownStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        try {
            a.sendStatus(-1);
            Assert.assertTrue("send Unknown Status check", sb.toString().endsWith("411 ERROR unknown value\n\r"));
        } catch (java.io.IOException ioe) {
            Assert.fail("Exception sending Off Status");
        }
    }

    // test parsing an ON status message.
    @Test
    public void testParseOnStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        try {
            a.parseStatus("1234 SET 0 POWER ON\n");
            Assert.assertEquals("Parse On Status Check",
                    jmri.InstanceManager
                            .getDefault(jmri.PowerManager.class).getPower(),
                    jmri.PowerManager.ON);
            Assert.assertTrue("status as a result of parsing on", sb.toString().endsWith("100 INFO 0 POWER ON\n\r"));
        } catch (jmri.JmriException jmrie) {
            Assert.fail("Exception retrieving Status");
        }
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        try {
            a.parseStatus("1234 SET 0 POWER OFF\n");
            Assert.assertEquals("Parse OFF Status Check",
                    jmri.InstanceManager
                            .getDefault(jmri.PowerManager.class).getPower(),
                    jmri.PowerManager.OFF);
            Assert.assertTrue("status as a result of parsing off", sb.toString().endsWith("100 INFO 0 POWER OFF\n\r"));
        } catch (jmri.JmriException jmrie) {
            Assert.fail("Exception retrieving Status");
        }
    }

    // test the property change sequence for an ON property change.
    @Test
    public void testPropertyChagneOnStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        try {
            jmri.InstanceManager.getDefault(jmri.PowerManager.class)
                            .setPower(jmri.PowerManager.ON);
            Assert.assertTrue("status as a result of on property change", sb.toString().endsWith("100 INFO 0 POWER ON\n\r"));
        } catch (jmri.JmriException je){
            Assert.fail("Exception setting Status");
        }
    }

    // test the property change sequence for an OFF property change.
    @Test
    public void testPropertyChangeOffStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        try {
            jmri.InstanceManager.getDefault(jmri.PowerManager.class).setPower(jmri.PowerManager.OFF);
            Assert.assertTrue("status as a result of off property change", sb.toString().endsWith("100 INFO 0 POWER OFF\n\r"));
        } catch (jmri.JmriException je){
            Assert.fail("Exception setting Status");
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugPowerManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
