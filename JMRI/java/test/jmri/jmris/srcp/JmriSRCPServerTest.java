package jmri.jmris.srcp;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPServerTest {

    @Test
    public void testCtor() {
        JmriSRCPServer a = new JmriSRCPServer();
        Assert.assertNotNull(a);
    }

    @Test
    public void testCtorwithParameter() {
        JmriSRCPServer a = new JmriSRCPServer(2048);
        Assert.assertNotNull(a);
    }

}
