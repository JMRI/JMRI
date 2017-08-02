package jmri.jmris.srcp;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPTimeServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPTimeServerTest extends jmri.jmris.AbstractTimeServerTestBase {

    @Before
    @Override
    public void setUp(){
        jmri.util.JUnitUtil.resetInstanceManager();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        a = new JmriSRCPTimeServer(output);
    }

    @After
    @Override
    public void tearDown(){
       a = null;
       jmri.util.JUnitUtil.resetInstanceManager();
    }

}
