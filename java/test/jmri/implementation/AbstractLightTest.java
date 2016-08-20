package jmri.implementation;

import java.beans.PropertyChangeListener;
import jmri.Light;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Abstract Base Class for Light tests in specific jmrix packages. This is not
 * itself a test class, e.g. should not be added to a suite. Instead, this forms
 * the base for test classes, including providing some common tests.
 *
 * @author	Bob Jacobsen 2002, 2004, 2005, 2007, 2008
 * @version	$Revision$
 */
public abstract class AbstractLightTest extends TestCase {

    // implementing classes must provide these abstract members:
    //
    abstract protected void setUp();    	// load t with actual object; create scaffolds as needed

    abstract public int numListeners();	// return number of listeners registered with the TrafficController

    abstract public void checkOnMsgSent();

    abstract public void checkOffMsgSent();

    public AbstractLightTest(String s) {
        super(s);
    }

    protected Light t = null;	// holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    public void testCreate() {
        // initial state when created must be OFF
        Assert.assertEquals("initial commanded state", Light.OFF, t.getState());
    }

    public void testAddListener() {
        t.addPropertyChangeListener(new Listen());
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertTrue("listener invoked by setUserName", listenerResult);
        listenerResult = false;
        t.setState(Light.ON);
        Assert.assertTrue("listener invoked by setCommandedState", listenerResult);
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

    public void testDispose() {
        t.setState(Light.ON);  	// in case registration with TrafficController 
        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 0, numListeners());
    }

    public void testCommandOff() {
        t.setState(Light.OFF);
        // check
        Assert.assertEquals("commanded state", jmri.Light.OFF, t.getState());
        checkOffMsgSent();
    }

    public void testCommandOn() {
        t.setState(Light.ON);
        // check
        Assert.assertEquals("commanded state", jmri.Light.ON, t.getState());
        checkOnMsgSent();
    }

}
