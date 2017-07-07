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
public class JmriSRCPTimeServerTest {

    private JmriSRCPTimeServer a = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(a);
    }

    @Test
    public void addAndRemoveListener(){
       jmri.Timebase t = jmri.InstanceManager.getDefault(jmri.Timebase.class);
       int n = t.getMinuteChangeListeners().length;
       a.listenToTimebase(true);
       Assert.assertEquals("added listener",n+1,t.getMinuteChangeListeners().length);
       a.listenToTimebase(false);
       // per the jmri.jmrit.simpleclock.SimpleTimebase class, remove is not 
       // implemented, so the following check doesn't work.
       //Assert.assertEquals("removed listener",n,t.getMinuteChangeListeners().length);
    }

    @Before
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
    public void tearDown(){
       a = null;
       jmri.util.JUnitUtil.resetInstanceManager();
    }

}
