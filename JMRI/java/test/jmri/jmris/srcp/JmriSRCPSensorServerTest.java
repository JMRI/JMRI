package jmri.jmris.srcp;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPSensorServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPSensorServerTest{

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
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        JmriSRCPSensorServer a = new JmriSRCPSensorServer(input, output);
        Assert.assertNotNull(a);
    }

}
