package jmri.implementation;

import java.beans.PropertyChangeListener;
import jmri.JmriException;
import jmri.Sensor;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Eventually: Abstract Base Class for Sensor tests in specific jmrix packages. This is not
 * itself a test class, e.g. should not be added to a suite. Instead, this forms
 * the base for test classes, including providing some common tests.
 *
 * @author	Bob Jacobsen 2016 from AbstractLightTest
 */
public /*abstract*/ class AbstractSensorTest extends TestCase {

    // implementing classes must provide these these methods

    // return number of listeners registered with the TrafficController
    /*abstract*/ public int numListeners() {return 0;}	

    /*abstract*/ public void checkOnMsgSent() {}

    /*abstract*/ public void checkOffMsgSent() {}

    // load t with actual object; create scaffolds as needed
    /*abstract*/ protected void setUp() {
        t = new AbstractSensor("Foo", "Bar"){
                public void requestUpdateFromLayout(){}
        };
    }
    
    protected AbstractSensor t = null;	// holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    public void testCreate() {
        // initial state when created must be UNKNOWN
        Assert.assertEquals("initial state", Sensor.UNKNOWN, t.getState());
    }

    public void testAddListener() throws JmriException {
        t.addPropertyChangeListener(new Listen());
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertTrue("listener invoked by setUserName", listenerResult);
        listenerResult = false;
        t.setState(Sensor.ACTIVE);
        Assert.assertTrue("listener invoked by setState", listenerResult);
    }

    public void testRemoveListener() {
        Listen ln = new Listen();
        t.addPropertyChangeListener(ln);
        t.removePropertyChangeListener(ln);
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertTrue("listener should not have heard message after removeListner",
                !listenerResult);
    }

    public void testDispose() throws JmriException {
        t.setState(Sensor.ACTIVE);  	// in case registration with TrafficController is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 0, numListeners());
    }

    public void testCommandInactive() throws JmriException {
        t.setState(Sensor.INACTIVE);
        // check
        Assert.assertEquals("state", Sensor.INACTIVE, t.getState());
        checkOffMsgSent();
    }

    public void testCommandActive() throws JmriException {
        t.setState(Sensor.ACTIVE);
        // check
        Assert.assertEquals("commanded state", Sensor.ACTIVE, t.getState());
        checkOnMsgSent();
    }

    public void testDebounceSettings() throws JmriException {
        t.setSensorDebounceGoingActiveTimer(81L);
        Assert.assertEquals("timer", 81L, t.getSensorDebounceGoingActiveTimer());
        
        t.setSensorDebounceGoingInActiveTimer(31L);
        Assert.assertEquals("timer", 31L, t.getSensorDebounceGoingInActiveTimer());
        
        Assert.assertEquals("initial default", false, t.useDefaultTimerSettings());
        t.useDefaultTimerSettings(true);
        Assert.assertEquals("initial default", true, t.useDefaultTimerSettings());
                
    }

    public void testDebounce() throws JmriException {
        t.setSensorDebounceGoingActiveTimer(81L);
        Assert.assertEquals("timer", 81L, t.getSensorDebounceGoingActiveTimer());
        
        t.setSensorDebounceGoingInActiveTimer(31L);
        Assert.assertEquals("timer", 31L, t.getSensorDebounceGoingInActiveTimer());
                
        Assert.assertEquals("initial state", Sensor.UNKNOWN, t.getState());
        t.setOwnState(Sensor.ACTIVE); // next is considered to run immediately, before debounce
        Assert.assertEquals("post-set state", Sensor.UNKNOWN, t.getState());
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == Sensor.ACTIVE;}, "Sensor.ACTIVE set");

        Assert.assertEquals("2nd state", Sensor.ACTIVE, t.getState());
        t.setOwnState(Sensor.INACTIVE); // next is considered to run immediately, before debounce
        Assert.assertEquals("post-set state", Sensor.ACTIVE, t.getState());
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == Sensor.INACTIVE;}, "Sensor.INACTIVE set");
    }
    
    // from here down is testing infrastructure

    public AbstractSensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AbstractSensorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractSensorTest.class);
        return suite;
    }

}
