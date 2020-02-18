package jmri.jmris.srcp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPTimeServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPTimeServerTest extends jmri.jmris.AbstractTimeServerTestBase {

    private StringBuilder sb = null;

    /**
     * {@inhertDoc} 
     */
    @Override
    public void confirmErrorStatusSent(){
       // send Error status doesn't send anything, should it?
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void confirmStatusSent(){
       // send status doesn't send anything, should it?
    }

    @Test
    public void sendRate() throws java.io.IOException {
       a.sendRate();
       assertThat(sb.toString()).endsWith("101 INFO 0 TIME 1 1\n\r").withFailMessage("Rate Sent");
    }

    @Test
    public void sendTime() throws java.io.IOException {
       a.sendTime();
       assertThat(sb.toString()).matches(".* 100 INFO 0 TIME .* .{1,2} .{1,2} .{1,2}\n\r").withFailMessage("time sent");
    }

    @BeforeEach
    @Override
    public void setUp(){
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        a = new JmriSRCPTimeServer(output);
    }

    @AfterEach
    @Override
    public void tearDown(){
        a.dispose();
        a = null;
        sb = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();
    }

}
