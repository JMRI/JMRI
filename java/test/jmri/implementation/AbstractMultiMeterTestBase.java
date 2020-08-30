package jmri.implementation;

import java.beans.PropertyChangeListener;

import jmri.JmriException;
import jmri.Meter;
import jmri.MeterGroup;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Abstract Base Class for MultiMeter tests in specific jmrix packages. This is 
 * not itself a test class, e.g. should not be added to a suite. Instead, this
 * forms the base for test classes, including providing some common tests.
 *
 * @author Bob Jacobsen 2002, 2004, 2005, 2007, 2008
 * @author  Paul Bender Copyright (C) 2017
 */
public abstract class AbstractMultiMeterTestBase {

    @BeforeEach
    abstract public void setUp(); // load mm with actual object; create scaffolds as needed

    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown(){
        mm.dispose();
        jmri.util.JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        jmri.util.JUnitUtil.tearDown();
    }

    protected MeterGroup mm = null; // holds objects under test

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
    public void testUpdateAndGetCurrent() throws JmriException{
        Assume.assumeNotNull(mm.getMeterByName(MeterGroup.CurrentMeter));
        mm.getMeterByName(MeterGroup.CurrentMeter).getMeter().setCommandedAnalogValue(0.5f);
        Assert.assertEquals("current after set",0.5f,mm.getMeterByName(MeterGroup.CurrentMeter).getMeter().getKnownAnalogValue(),0.0001);
    }

    @Test
    public void testUpdateAndGetVoltage() throws JmriException{
        Assume.assumeNotNull(mm.getMeterByName(MeterGroup.VoltageMeter));
        mm.getMeterByName(MeterGroup.VoltageMeter).getMeter().setCommandedAnalogValue(0.5f);
        Assert.assertEquals("voltage after set",0.5f,mm.getMeterByName(MeterGroup.VoltageMeter).getMeter().getKnownAnalogValue(),0.0001);
    }

    @Test
    public void testAddListener() throws JmriException {
        Listen ln = new Listen();
        
        MeterGroup.MeterInfo meterInfo = mm.getMeterByName(MeterGroup.CurrentMeter);
        if (meterInfo != null) {
            Meter meter = meterInfo.getMeter();
            meter.addPropertyChangeListener(ln);
            meter.setCommandedAnalogValue(0.5f);
            jmri.util.JUnitUtil.waitFor(()->{ return ln.eventSeen(); } );
            Assert.assertTrue("listener invoked by setCurrent", ln.eventSeen());
        }
        
        meterInfo = mm.getMeterByName(MeterGroup.VoltageMeter);
        if (meterInfo != null) {
            Meter meter = meterInfo.getMeter();
            meter.addPropertyChangeListener(ln);
            meter.setCommandedAnalogValue(0.5f);
            jmri.util.JUnitUtil.waitFor(()->{ return ln.eventSeen(); } );
            Assert.assertTrue("listener invoked by setVoltage", ln.eventSeen());
        }
    }

    @Test
    public void testRemoveListener() throws JmriException {
        Listen ln = new Listen();
        
        MeterGroup.MeterInfo meterInfo = mm.getMeterByName(MeterGroup.CurrentMeter);
        if (meterInfo != null) {
            Meter meter = meterInfo.getMeter();
            meter.addPropertyChangeListener(ln);
            meter.removePropertyChangeListener(ln);
            meter.setCommandedAnalogValue(0.5f);
            // this should just timeout;
            Assert.assertFalse(jmri.util.JUnitUtil.waitFor(()->{ return ln.eventSeen(); } ));
            Assert.assertFalse("listener invoked by setCurrent, but not listening", ln.eventSeen());
        }
        
        meterInfo = mm.getMeterByName(MeterGroup.VoltageMeter);
        if (meterInfo != null) {
            Meter meter = meterInfo.getMeter();
            meter.addPropertyChangeListener(ln);
            meter.removePropertyChangeListener(ln);
            meter.setCommandedAnalogValue(0.5f);
            // this should just timeout;
            Assert.assertFalse(jmri.util.JUnitUtil.waitFor(()->{ return ln.eventSeen(); } ));
            Assert.assertFalse("listener invoked by setCurrent, but not listening", ln.eventSeen());
        }
    }

}
