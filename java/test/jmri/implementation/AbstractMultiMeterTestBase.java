package jmri.implementation;

import java.beans.PropertyChangeListener;
import jmri.MultiMeter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract Base Class for MultiMeter tests in specific jmrix packages. This is 
 * not itself a test class, e.g. should not be added to a suite. Instead, this
 * forms the base for test classes, including providing some common tests.
 *
 * @author	Bob Jacobsen 2002, 2004, 2005, 2007, 2008
 * @author  Paul Bender Copyright (C) 2017	
 */
public abstract class AbstractMultiMeterTestBase {

    @Before
    abstract public void setUp();    	// load mm with actual object; create scaffolds as needed

    @After
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown(){
        mm.dispose();
        jmri.util.JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        jmri.util.JUnitUtil.tearDown();
    }

    protected MultiMeter mm = null;	// holds objects under test

    protected class Listen implements PropertyChangeListener {
    
        private boolean listenerResult = false;

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }

        public boolean eventSeen(){
           return listenerResult;
        }
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        Assert.assertNotNull("MultiMeter Created", mm );
    }

    @Test
    public void testUpdateAndGetCurrent(){
        Assume.assumeTrue(mm.hasCurrent());
        mm.setCurrent(0.5f);
        Assert.assertEquals("current after set",0.5f,mm.getCurrent(),0.0001);
    }

    @Test
    public void testUpdateAndGetVoltage(){
        Assume.assumeTrue(mm.hasVoltage());
        mm.setVoltage(0.5f);
        Assert.assertEquals("current after set",0.5f,mm.getVoltage(),0.0001);
    }

    @Test
    public void testAddListener() {
        Listen ln = new Listen();
        mm.addPropertyChangeListener(ln);
        mm.setCurrent(0.5f);
        jmri.util.JUnitUtil.waitFor(()->{ return ln.eventSeen(); } );
        Assert.assertTrue("listener invoked by setCurrent", ln.eventSeen());
    }

    @Test
    public void testRemoveListener() {
        Listen ln = new Listen();
        mm.addPropertyChangeListener(ln);
        mm.removePropertyChangeListener(ln);
        mm.setCurrent(0.5f);
        // this should just timeout;
        Assert.assertFalse(jmri.util.JUnitUtil.waitFor(()->{ return ln.eventSeen(); } ));
        Assert.assertFalse("listener invoked by setCurrent, but not listening", ln.eventSeen());
    }

}
