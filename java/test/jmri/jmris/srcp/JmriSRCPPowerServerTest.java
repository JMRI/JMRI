package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


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
        assertThat(jmri.PowerManager.ON).isEqualTo(jmri.InstanceManager
                      .getDefault(jmri.PowerManager.class).getPower()).withFailMessage("Parse On Status Check");
        assertThat(sb.toString()).endsWith("100 INFO 0 POWER ON\n\r").withFailMessage("status as a result of parsing on");
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() throws Exception {
        ps.parseStatus("1234 SET 0 POWER OFF\n");
        assertThat(jmri.PowerManager.OFF).isEqualTo(jmri.InstanceManager
                      .getDefault(jmri.PowerManager.class).getPower()).withFailMessage("Parse OFF Status Check");
        assertThat(sb.toString()).endsWith("100 INFO 0 POWER OFF\n\r").withFailMessage("status as a result of parsing off");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOnSent(){
            assertThat(sb.toString()).endsWith("100 INFO 0 POWER ON\n\r").withFailMessage("status as a result of on property change");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOffSent(){
        assertThat(sb.toString()).endsWith("100 INFO 0 POWER OFF\n\r").withFailMessage("status as a result of off property change");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkErrorStatusSent() {
        assertThat(sb.toString()).endsWith("499 ERROR unspecified error\n\r").withFailMessage("sendErrorStatus check");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkUnknownStatusSent() {
        assertThat(sb.toString()).endsWith("411 ERROR unknown value\n\r").withFailMessage("send Unknown Status check");
    }

    // The minimal setup for log4J
    @BeforeEach
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

    @AfterEach
    public void tearDown() {
        ps.dispose();
        ps = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
