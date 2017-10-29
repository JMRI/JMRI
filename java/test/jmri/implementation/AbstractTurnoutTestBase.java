package jmri.implementation;

import java.beans.PropertyChangeListener;
import jmri.Turnout;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract base class for Turnout tests in specific jmrix. packages
 *
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests
 * @author	Bob Jacobsen
 */
public abstract class AbstractTurnoutTestBase {

    // implementing classes must provide these abstract members:
    //
    @Before
    abstract public void setUp();    	// load t with actual object; create scaffolds as needed

    abstract public int numListeners();	// return number of listeners registered with the TrafficController

    abstract public void checkThrownMsgSent() throws InterruptedException;

    abstract public void checkClosedMsgSent() throws InterruptedException;

    protected Turnout t = null;	// holds objects under test

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
        // initial commanded state when created must be UNKNOWN
        Assert.assertEquals("initial commanded state 1", Turnout.UNKNOWN, t.getCommandedState());
        // initial known state when created must be UNKNOWN
        Assert.assertEquals("initial known state", Turnout.UNKNOWN, t.getKnownState());
        Assert.assertEquals("initial commanded state 2", Turnout.UNKNOWN, t.getState());
    }

    @Test
    public void testAddListener() {
        t.addPropertyChangeListener(new Listen());
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertTrue("listener invoked by setUserName", listenerResult);
        listenerResult = false;
        t.setCommandedState(Turnout.CLOSED);
        Assert.assertTrue("listener invoked by setCommandedState", listenerResult);
    }

    @Test
    public void testRemoveListener() {
        Listen ln = new Listen();
        t.addPropertyChangeListener(ln);
        t.removePropertyChangeListener(ln);
        listenerResult = false;
        t.setUserName("user id");
        Assert.assertTrue("listener should not have heard message after removeListener",
                !listenerResult);
    }

    @Test
    public void testDispose() {
        t.setCommandedState(Turnout.CLOSED);  	// in case registration with TrafficController
        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 0, numListeners());
    }

    @Test
    public void testCommandClosed() throws InterruptedException {
        t.setCommandedState(Turnout.CLOSED);
        // check
        Assert.assertEquals("commanded state 1", Turnout.CLOSED, t.getCommandedState());
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.CLOSED, t.getState());
        Assert.assertEquals("commanded state 3", "Closed", t.describeState(t.getState()));
        checkClosedMsgSent();
    }

    @Test
    public void testCommandThrown() throws InterruptedException {
        t.setCommandedState(Turnout.THROWN);
        // check
        Assert.assertEquals("commanded state 1", Turnout.THROWN, t.getCommandedState());
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.THROWN, t.getState());
        Assert.assertEquals("commanded state 3", "Thrown", t.describeState(t.getState()));
        checkThrownMsgSent();
    }

    @Test
    public void testGetAndSetInverted(){
        Assume.assumeTrue(t.canInvert());  // skip test if can't invert.
        Assert.assertFalse("Default Inverted State",t.getInverted());
        t.setInverted(true);
        Assert.assertTrue("set Inverted",t.getInverted());
    }

    @Test
    public void testInvertedCommandClosed() throws InterruptedException {
        Assume.assumeTrue(t.canInvert());  // skip test if can't invert.
        t.setInverted(true);
        t.setCommandedState(Turnout.CLOSED);
        // check
        Assert.assertEquals("commanded state 1", Turnout.CLOSED, t.getCommandedState());
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.CLOSED, t.getState());
        Assert.assertEquals("commanded state 3", "Closed", t.describeState(t.getState()));
        checkThrownMsgSent();
    }

    @Test
    public void testInvertedCommandThrown() throws InterruptedException {
        Assume.assumeTrue(t.canInvert());  // skip test if can't invert.
        t.setInverted(true);
        t.setCommandedState(Turnout.THROWN);
        // check
        Assert.assertEquals("commanded state 1", Turnout.THROWN, t.getCommandedState());
        ((AbstractTurnout)t).setKnownStateToCommanded();
        Assert.assertEquals("commanded state 2", Turnout.THROWN, t.getState());
        Assert.assertEquals("commanded state 3", "Thrown", t.describeState(t.getState()));
        checkClosedMsgSent();
    }

}
