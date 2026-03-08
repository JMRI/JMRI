package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeListener;

import jmri.*;

import org.junit.jupiter.api.*;

/**
 * Abstract Base Class for Light tests in specific jmrix packages. This is not
 * itself a test class, e.g. should not be added to a suite. Instead, this forms
 * the base for test classes, including providing some common tests.
 *
 * @author Bob Jacobsen 2002, 2004, 2005, 2007, 2008
 */
public abstract class AbstractLightTestBase {

    // implementing classes must provide these abstract members:
    //
    abstract public void setUp();       // load t with actual object; create scaffolds as needed

    abstract public int numListeners(); // return number of listeners registered with the TrafficController

    abstract public void checkOnMsgSent();

    abstract public void checkOffMsgSent();

    protected Light t = null; // holds objects under test

    private boolean listenerResult = false;

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
        // initial state when created must be OFF
        assertEquals(Light.OFF, t.getState(), "initial commanded state");
    }

    @Test
    public void testAddListener() {
        t.addPropertyChangeListener(new Listen());
        listenerResult = false;
        t.setUserName("user id");
        assertTrue(listenerResult, "listener invoked by setUserName");
        listenerResult = false;
        t.setState(Light.ON);
        assertTrue(listenerResult, "listener invoked by setCommandedState");
    }

    @Test
    public void testRemoveListener() {
        Listen ln = new Listen();
        t.addPropertyChangeListener(ln);
        t.removePropertyChangeListener(ln);
        listenerResult = false;
        t.setUserName("user id");
        assertFalse(listenerResult,
            "listener should not have heard message after removeListener");
    }

    @Test
    public void testDispose() {
        t.setState(Light.ON); // in case registration with TrafficController
                              // is deferred to after first use
        t.dispose();
        assertEquals(0, numListeners(), "controller listeners remaining");
    }

    @Test
    public void testRemoveListenerOnDispose() {
        assertEquals(0, t.getNumPropertyChangeListeners(), "starts 0 listeners");
        t.addPropertyChangeListener(new Listen());
        assertEquals(1, t.getNumPropertyChangeListeners(), "controller listener added");
        t.dispose();
        assertTrue(t.getNumPropertyChangeListeners() < 1, "controller listeners remaining < 1");
    }

    @Test
    public void testCommandOff() {
        t.setState(Light.OFF);
        // check
        assertEquals(jmri.Light.OFF, t.getState(), "state 1");
        assertEquals("Off", t.describeState(t.getState()), "state 2");
        checkOffMsgSent();
    }

    @Test
    public void testCommandOn() {
        t.setState(Light.ON);
        // check
        assertEquals(Light.ON, t.getState(), "state 1");
        assertEquals("On", t.describeState(t.getState()), "state 2");
        checkOnMsgSent();
    }

    @Test
    public void testGetBeanType() {
        assertEquals(Bundle.getMessage("BeanNameLight"), t.getBeanType(), "bean type");
    }

    // Test the Light interface
    @Test
    public void testLight() {
        t.setState(Light.ON);
        assertEquals(Light.ON, t.getState(), "Light is ON");
        t.setState(Light.OFF);
        assertEquals(Light.OFF, t.getState(), "Light is ON");
        t.setCommandedState(Light.ON);
        assertEquals(Light.ON, t.getState(), "Light is ON");
        t.setCommandedState(Light.OFF);
        assertEquals(Light.OFF, t.getState(), "Light is ON");
        t.setState(Light.ON);
        assertEquals(Light.ON, t.getCommandedState(), "Light is ON");
        t.setState(Light.OFF);
        assertEquals(Light.OFF, t.getCommandedState(), "Light is ON");
        t.setState(Light.ON);
        assertEquals(Light.ON, t.getKnownState(), "Light is ON");
        t.setState(Light.OFF);
        assertEquals(Light.OFF, t.getKnownState(), "Light is ON");
    }

    // add a LightControl
    @Test
    public void testAddLightControls() {
        assertEquals(0, t.getLightControlList().size(), "0 controls attached");
        LightControl lc = new jmri.implementation.DefaultLightControl(t);
        lc.setControlType(Light.SENSOR_CONTROL);
        t.addLightControl(lc);
        assertEquals(1, t.getLightControlList().size(), "1 control attached");
        t.addLightControl(lc);
        assertEquals(1, t.getLightControlList().size(), "1 control attached");
        assertEquals(lc, t.getLightControlList().get(0), "control attached");
        t.addLightControl(new jmri.implementation.DefaultLightControl(t));
        assertEquals(2, t.getLightControlList().size(), "2 controls attached");
        assertNotEquals(t.getLightControlList().get(0),
                t.getLightControlList().get(1), "2 controls attached");
        t.clearLightControls();
        assertEquals(0, t.getLightControlList().size(), "0 controls attached");
    }

}
