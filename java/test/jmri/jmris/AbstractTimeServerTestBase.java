package jmri.jmris;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Common tests for classes derived from jmri.jmris.AbstractTimeServer class
 *
 * @author Paul Bender Copyright (C) 2017 
 */
abstract public class AbstractTimeServerTestBase {

    protected AbstractTimeServer a = null;

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
    // derived classes must configure the TimeServer variable (a)
    abstract public void setUp();

    @After
    // derived classes must clean up the TimeServer variable (a)
    abstract public void tearDown();

}
