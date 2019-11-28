package jmri.implementation;

import java.beans.PropertyChangeListener;
import jmri.Light;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract Base Class for Light tests in specific jmrix packages. This is not
 * itself a test class, e.g. should not be added to a suite. Instead, this forms
 * the base for test classes, including providing some common tests.
 *
 * @author	Bob Jacobsen 2002, 2004, 2005, 2007, 2008
 */
public abstract class AbstractLightTestBase {

    // implementing classes must provide these abstract members:
    //
    @Before
    abstract public void setUp();    	// load t with actual object; create scaffolds as needed

    abstract public int numListeners();	// return number of listeners registered with the TrafficController

    abstract public void checkOnMsgSent();

    abstract public void checkOffMsgSent();

    protected Light t = null;	// holds objects under test

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
        // initial state when created must be OFF
        Assert.assertEquals("initial commanded state", Light.OFF, t.getState());
    }

    @Test
    public void testAddListener() {
        t.addPropertyChangeListener(new Listen());
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertTrue("listener invoked by setUserName", listenerResult);
        listenerResult = false;
        t.setState(Light.ON);
        Assert.assertTrue("listener invoked by setCommandedState", listenerResult);
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
    public void testDispose() {
        t.setState(Light.ON);  	// in case registration with TrafficController
        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 0, numListeners());
    }

    @Test
    public void testCommandOff() {
        t.setState(Light.OFF);
        // check
        Assert.assertEquals("state 1", jmri.Light.OFF, t.getState());
        Assert.assertEquals("state 2", "Off", t.describeState(t.getState()));
        checkOffMsgSent();
    }

    @Test
    public void testCommandOn() {
        t.setState(Light.ON);
        // check
        Assert.assertEquals("state 1", jmri.Light.ON, t.getState());
        Assert.assertEquals("state 2", "On", t.describeState(t.getState()));
        checkOnMsgSent();
    }

    @Test
    public void testGetBeanType() {
        Assert.assertEquals("bean type", t.getBeanType(), Bundle.getMessage("BeanNameLight"));
    }

    // Test the Light interface
    @Test
    public void testLight() {
        t.setState(Light.ON);
        Assert.assertEquals("Light is ON", Light.ON, t.getState());
        t.setState(Light.OFF);
        Assert.assertEquals("Light is ON", Light.OFF, t.getState());
        t.setCommandedState(Light.ON);
        Assert.assertEquals("Light is ON", Light.ON, t.getState());
        t.setCommandedState(Light.OFF);
        Assert.assertEquals("Light is ON", Light.OFF, t.getState());
        t.setState(Light.ON);
        Assert.assertEquals("Light is ON", Light.ON, t.getCommandedState());
        t.setState(Light.OFF);
        Assert.assertEquals("Light is ON", Light.OFF, t.getCommandedState());
        t.setState(Light.ON);
        Assert.assertEquals("Light is ON", Light.ON, t.getKnownState());
        t.setState(Light.OFF);
        Assert.assertEquals("Light is ON", Light.OFF, t.getKnownState());
    }

    // add a LightControl
    @Test
    public void testAddLightControls() {

        Assert.assertEquals("0 controls attached", 0, t.getLightControlList().size());
        jmri.implementation.LightControl lc = new jmri.implementation.LightControl(t);
        lc.setControlType(Light.SENSOR_CONTROL);
        t.addLightControl(lc);
        Assert.assertEquals("1 control attached", 1, t.getLightControlList().size());
        t.addLightControl(lc);
        Assert.assertEquals("1 control attached", 1, t.getLightControlList().size());
        Assert.assertEquals("control attached", lc, t.getLightControlList().get(0));
        t.addLightControl(new jmri.implementation.LightControl(t));
        Assert.assertEquals("2 controls attached", 2, t.getLightControlList().size());
        Assert.assertNotEquals("2 controls attached", t.getLightControlList().get(0),
                t.getLightControlList().get(1));
        t.clearLightControls();
        Assert.assertEquals("0 controls attached", 0, t.getLightControlList().size());

    }

}
