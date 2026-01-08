package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.beans.PropertyChangeListener;

import jmri.JmriException;
import jmri.Sensor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;


/**
 * Abstract Base Class for Sensor tests in specific jmrix packages. This is not
 * itself a test class, e.g. should not be added to a suite. Instead, this forms
 * the base for test classes, including providing some common tests.
 *
 * @author Bob Jacobsen 2016 from AbstractLightTestBase (which was called AbstractLightTest at the time)
 * @author  Paul Bender Copyright (C) 2018
*/
public abstract class AbstractSensorTestBase {

    // implementing classes must provide these these methods

    // return number of listeners registered with the TrafficController
    abstract public int numListeners();

    abstract public void checkActiveMsgSent();

    abstract public void checkInactiveMsgSent();

    abstract public void checkStatusRequestMsgSent();

    // implementing classes must provide this abstract member:
    abstract public void setUp(); // load t with actual object; create scaffolds as needed

    protected AbstractSensor t = null; // holds object under test; set by setUp()

    protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        // initial state when created must be UNKNOWN
        assertEquals( Sensor.UNKNOWN, t.getState(), "initial state 1 unknown");
        assertEquals( "Unknown", t.describeState(t.getState()), "initial state 2");
    }

    @Test
    public void testAddListener() throws JmriException {
        t.addPropertyChangeListener(new Listen());
        listenerResult = false;
        t.setUserName("user id");
        assertTrue( listenerResult, "listener invoked by setUserName");
        listenerResult = false;
        t.setState(Sensor.ACTIVE);
        assertTrue( listenerResult, "listener invoked by setState");
    }

    @Test
    public void testRemoveListener() {
        Listen ln = new Listen();
        t.addPropertyChangeListener(ln);
        t.removePropertyChangeListener(ln);
        listenerResult = false;
        t.setUserName("user id");
        assertFalse( listenerResult,
            "listener should not have heard message after removeListener");
    }

    @Test
    public void testDispose() throws JmriException {
        t.setState(Sensor.ACTIVE); // in case registration with TrafficController is deferred to after first use
        t.dispose();
        assertEquals( 0, numListeners(), "controller listeners remaining");
    }
    
    @Test
    public void testRemoveListenerOnDispose() {
        assertEquals( 0, t.getNumPropertyChangeListeners(), "starts 0 listeners");
        t.addPropertyChangeListener(new Listen());
        assertEquals( 1, t.getNumPropertyChangeListeners(), "controller listener added");
        t.dispose();
        assertTrue( t.getNumPropertyChangeListeners() < 1, "controller listeners remaining < 1");
    }

    @Test
    public void testCommandInactive() throws JmriException {
        t.setState(Sensor.INACTIVE);
        // check
        assertEquals( Sensor.INACTIVE, t.getState(), "state 1");
        assertEquals( "Inactive", t.describeState(t.getState()), "state 2");
    }

    @Test
    public void testCommandActive() throws JmriException {
        t.setState(Sensor.ACTIVE);
        // check
        assertEquals( Sensor.ACTIVE, t.getState(), "state 1");
        assertEquals( "Active", t.describeState(t.getState()), "state 2");
    }

    @Test
    public void testCommandSentActive() throws JmriException {
        t.setState(Sensor.ACTIVE);
        assertEquals( Sensor.ACTIVE, t.getState(), "Sensor goes active");
        checkActiveMsgSent();
    }
    
    @Test
    public void testCommandSentInactive() throws JmriException {
        t.setState(Sensor.INACTIVE);
        assertEquals( Sensor.INACTIVE, t.getState(), "sensor goes inactive");
        checkInactiveMsgSent();
    }
    
    @Test
    public void testInvertAfterInactive() throws JmriException {
        assumeTrue(t.canInvert(), "Sensor does not invert");
        t.setState(Sensor.INACTIVE);
        t.setInverted(true);
        // check
        assertEquals( Sensor.ACTIVE, t.getState(), "state 1");
        assertEquals( "Active", t.describeState(t.getState()), "state 2");
    }

    @Test
    public void testInvertAfterActive() throws JmriException {
        assumeTrue(t.canInvert(), "Sensor does not invert");
        t.setState(Sensor.ACTIVE);
        t.setInverted(true);
        // check
        assertEquals( Sensor.INACTIVE, t.getState(), "state 1");
        assertEquals( "Inactive", t.describeState(t.getState()), "state 2");
    }

    @Test
    public void testDebounceSettings() throws JmriException {
        t.setSensorDebounceGoingActiveTimer(81L);
        assertEquals( 81L, t.getSensorDebounceGoingActiveTimer(), "timer");

        t.setSensorDebounceGoingInActiveTimer(31L);
        assertEquals( 31L, t.getSensorDebounceGoingInActiveTimer(), "timer");

        assertFalse( t.getUseDefaultTimerSettings(), "initial default");
        t.setUseDefaultTimerSettings(true);
        assertTrue( t.getUseDefaultTimerSettings(), "initial default");
    }

    @Test
    public void testDebounce() throws JmriException {
        t.setSensorDebounceGoingActiveTimer(81L);
        assertEquals( 81L, t.getSensorDebounceGoingActiveTimer(), "timer");

        t.setSensorDebounceGoingInActiveTimer(31L);
        assertEquals( 31L, t.getSensorDebounceGoingInActiveTimer(), "timer");

        assertEquals( Sensor.UNKNOWN, t.getState(), "initial state");
        t.setOwnState(Sensor.ACTIVE); // next is considered to run immediately, before debounce
        assertEquals( Sensor.UNKNOWN, t.getState(), "post-set state");
        JUnitUtil.waitFor(()-> t.getState() == t.getRawState(), "raw state = state");
        assertEquals( Sensor.ACTIVE, t.getState(), "2nd state");

        t.setOwnState(Sensor.INACTIVE); // next is considered to run immediately, before debounce
        assertEquals( Sensor.ACTIVE, t.getState(), "post-set state");
        JUnitUtil.waitFor(()-> t.getState() == t.getRawState(), "raw state = state");
        assertEquals( Sensor.INACTIVE, t.getState(), "Final state");

        disposeAndWaitForDebounceThread(t);

    }

    private void disposeAndWaitForDebounceThread(AbstractSensor t) {
        Thread debounce = t.thr;
        t.dispose();
        if ( debounce !=null ) {
            JUnitUtil.waitFor( ()-> { return !debounce.isAlive(); }, debounce.getName() + " did not close");
        }
    }

    @Test
    public void testGetPullResistance(){
        // default is off, override this test if this is supported.
        assertEquals( Sensor.PullResistance.PULL_OFF, t.getPullResistance(), "Pull Direction");
    }

    @Test
    public void testGetBeanType(){
        assertEquals( t.getBeanType(), Bundle.getMessage("BeanNameSensor"), "bean type");
    }

    // Test outgoing status request
    @Test
    public void testSensorStatusRequest() {
        t.requestUpdateFromLayout();
        // check that the correct message was sent
        checkStatusRequestMsgSent();
    }

    // Test the Sensor interface
    @Test
    public void testSensor() throws JmriException {
        t.setState(Sensor.ON);
        JUnitUtil.waitFor( ()-> t.getState() == Sensor.ON, "state = ON");
        assertEquals( Sensor.ON, t.getState(), "Sensor is ON");
        t.setState(Sensor.OFF);
        JUnitUtil.waitFor( ()-> t.getState() == Sensor.OFF, "state = OFF");
        assertEquals( Sensor.OFF, t.getState(), "Sensor is OFF");
        t.setCommandedState(Sensor.ON);
        JUnitUtil.waitFor( ()-> t.getState() == Sensor.ON, "state = ON");
        assertEquals( Sensor.ON, t.getState(), "Sensor is ON");
        t.setCommandedState(Sensor.OFF);
        JUnitUtil.waitFor( ()-> t.getState() == Sensor.OFF, "state = OFF");
        assertEquals( Sensor.OFF, t.getState(), "Sensor is OFF");
        t.setState(Sensor.ON);
        JUnitUtil.waitFor( ()-> t.getCommandedState() == Sensor.ON, "commanded state = ON");
        assertEquals( Sensor.ON, t.getCommandedState(), "Sensor is ON");
        t.setState(Sensor.OFF);
        JUnitUtil.waitFor( ()-> t.getCommandedState() == Sensor.OFF, "commanded state = OFF");
        assertEquals( Sensor.OFF, t.getCommandedState(), "Sensor is ON");
    }
    
    @Test
    public void testSensorSetKnownState() throws JmriException {

        // Assert.assertEquals("ACTIVE", t.describeState(Sensor.ACTIVE), t.describeState(t.getState()));
        
        t.setKnownState(Sensor.ACTIVE);
        assertEquals( Sensor.ACTIVE, t.getState(), "ACTIVE");

        t.setKnownState(Sensor.INACTIVE);
        assertEquals( Sensor.INACTIVE, t.getState(), "INACTIVE");

        t.setKnownState(Sensor.UNKNOWN);
        assertEquals( Sensor.UNKNOWN, t.getState(), "UNKNOWN");

        // Reset known state to something normal
        t.setKnownState(Sensor.ACTIVE);
        assertEquals( Sensor.ACTIVE, t.getState(), "ACTIVE");

        t.setKnownState(Sensor.INCONSISTENT);
        assertEquals( Sensor.INCONSISTENT, t.getState(), "INCONSISTENT");
    }

    //dispose of t.
    abstract public void tearDown();

}
