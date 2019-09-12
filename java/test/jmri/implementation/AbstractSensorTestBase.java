package jmri.implementation;

import java.beans.PropertyChangeListener;
import jmri.JmriException;
import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;


/**
 * Abstract Base Class for Sensor tests in specific jmrix packages. This is not
 * itself a test class, e.g. should not be added to a suite. Instead, this forms
 * the base for test classes, including providing some common tests.
 *
 * @author	Bob Jacobsen 2016 from AbstractLightTestBase (which was called AbstractLightTest at the time)
 * @author  Paul Bender Copyright (C) 2018
*/
public abstract class AbstractSensorTestBase {

    // implementing classes must provide these these methods

    // return number of listeners registered with the TrafficController
    abstract public int numListeners();

    abstract public void checkOnMsgSent();

    abstract public void checkOffMsgSent();

    abstract public void checkStatusRequestMsgSent();

    // implementing classes must provide this abstract member:
    @Before
    abstract public void setUp(); // load t with actual object; create scaffolds as needed

    protected AbstractSensor t = null;	// holds object under test; set by setUp()

    static protected boolean listenerResult = false;

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
        Assert.assertEquals("initial state 1", Sensor.UNKNOWN, t.getState());
        Assert.assertEquals("initial state 2", "Unknown", t.describeState(t.getState()));
    }

    @Test
    public void testAddListener() throws JmriException {
        t.addPropertyChangeListener(new Listen());
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertTrue("listener invoked by setUserName", listenerResult);
        listenerResult = false;
        t.setState(Sensor.ACTIVE);
        Assert.assertTrue("listener invoked by setState", listenerResult);
    }

    @Test
    public void testRemoveListener() {
        Listen ln = new Listen();
        t.addPropertyChangeListener(ln);
        t.removePropertyChangeListener(ln);
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertFalse("listener should not have heard message after removeListener", 
                listenerResult);
    }

    @Test
    public void testDispose() throws JmriException {
        t.setState(Sensor.ACTIVE);  	// in case registration with TrafficController is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 0, numListeners());
    }

    @Test
    public void testCommandInactive() throws JmriException {
        t.setState(Sensor.INACTIVE);
        // check
        Assert.assertEquals("state 1", Sensor.INACTIVE, t.getState());
        Assert.assertEquals("state 2", "Inactive", t.describeState(t.getState()));
    }

    @Test
    public void testCommandActive() throws JmriException {
        t.setState(Sensor.ACTIVE);
        // check
        Assert.assertEquals("state 1", Sensor.ACTIVE, t.getState());
        Assert.assertEquals("state 2", "Active", t.describeState(t.getState()));
    }

    @Test
    public void testInvertAfterInactive() throws JmriException {
        Assume.assumeTrue(t.canInvert());
        t.setState(Sensor.INACTIVE);
	t.setInverted(true);
        // check
        Assert.assertEquals("state 1", Sensor.ACTIVE, t.getState());
        Assert.assertEquals("state 2", "Active", t.describeState(t.getState()));
    }

    @Test
    public void testInvertAfterActive() throws JmriException {
        Assume.assumeTrue(t.canInvert());
        t.setState(Sensor.ACTIVE);
        t.setInverted(true);
        // check
        Assert.assertEquals("state 1", Sensor.INACTIVE, t.getState());
        Assert.assertEquals("state 2", "Inactive", t.describeState(t.getState()));
    }

    @Test
    public void testDebounceSettings() throws JmriException {
        t.setSensorDebounceGoingActiveTimer(81L);
        Assert.assertEquals("timer", 81L, t.getSensorDebounceGoingActiveTimer());

        t.setSensorDebounceGoingInActiveTimer(31L);
        Assert.assertEquals("timer", 31L, t.getSensorDebounceGoingInActiveTimer());

        Assert.assertEquals("initial default", false, t.getUseDefaultTimerSettings());
        t.setUseDefaultTimerSettings(true);
        Assert.assertEquals("initial default", true, t.getUseDefaultTimerSettings());
    }

    @Test
    public void testDebounce() throws JmriException {
        t.setSensorDebounceGoingActiveTimer(81L);
        Assert.assertEquals("timer", 81L, t.getSensorDebounceGoingActiveTimer());

        t.setSensorDebounceGoingInActiveTimer(31L);
        Assert.assertEquals("timer", 31L, t.getSensorDebounceGoingInActiveTimer());

        Assert.assertEquals("initial state", Sensor.UNKNOWN, t.getState());
        t.setOwnState(Sensor.ACTIVE); // next is considered to run immediately, before debounce
        Assert.assertEquals("post-set state", Sensor.UNKNOWN, t.getState());
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == t.getRawState();}, "raw state = state");
        Assert.assertEquals("2nd state", Sensor.ACTIVE, t.getState());

        t.setOwnState(Sensor.INACTIVE); // next is considered to run immediately, before debounce
        Assert.assertEquals("post-set state", Sensor.ACTIVE, t.getState());
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == t.getRawState();}, "raw state = state");
        Assert.assertEquals("Final state", Sensor.INACTIVE, t.getState());
    }

    @Test
    public void testGetPullResistance(){
        // default is off, override this test if this is supported.
        Assert.assertEquals("Pull Direction", jmri.Sensor.PullResistance.PULL_OFF, t.getPullResistance());
    }

    @Test
    public void testGetBeanType(){
        Assert.assertEquals("bean type", t.getBeanType(), Bundle.getMessage("BeanNameSensor"));
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
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == Sensor.ON;}, "state = ON");
        Assert.assertTrue("Sensor is ON", t.getState() == Sensor.ON);
        t.setState(Sensor.OFF);
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == Sensor.OFF;}, "state = OFF");
        Assert.assertTrue("Sensor is ON", t.getState() == Sensor.OFF);
        t.setCommandedState(Sensor.ON);
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == Sensor.ON;}, "state = ON");
        Assert.assertTrue("Sensor is ON", t.getState() == Sensor.ON);
        t.setCommandedState(Sensor.OFF);
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == Sensor.OFF;}, "state = OFF");
        Assert.assertTrue("Sensor is ON", t.getState() == Sensor.OFF);
        t.setState(Sensor.ON);
        jmri.util.JUnitUtil.waitFor(()->{return t.getCommandedState() == Sensor.ON;}, "commanded state = ON");
        Assert.assertTrue("Sensor is ON", t.getCommandedState() == Sensor.ON);
        t.setState(Sensor.OFF);
        jmri.util.JUnitUtil.waitFor(()->{return t.getCommandedState() == Sensor.OFF;}, "commanded state = OFF");
        Assert.assertTrue("Sensor is ON", t.getCommandedState() == Sensor.OFF);
    }

    //dispose of t.
    @After
    abstract public void tearDown();

}
