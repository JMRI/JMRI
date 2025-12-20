package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeListener;

import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

/**
 * Base for TurnoutManager tests in specific jmrix.* packages
 * <p>
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 *
 * @author Bob Jacobsen
 */
public abstract class AbstractTurnoutMgrTestBase extends AbstractProvidingManagerTestBase<TurnoutManager, Turnout> {

    // implementing classes must implement to convert integer (count) to a system name
    abstract public String getSystemName(int i);

    /**
     * Overload to load l with actual object; create scaffolds as needed
     */
    abstract public void setUp();

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
        assertNotNull( l );
    }

    @Test
    public void testDispose() {
        if (l != null) {
            l.dispose();  // all we're really doing here is making sure the method exists
        }
    }

    @Test
    public void testProvideFailure() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> l.provideTurnout(""));
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: System name must start with \"" + l.getSystemNamePrefix() + "\".");
    }

    @Test
    public void testTurnoutPutGet() {
        // create
        Turnout t = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        // check
        assertNotNull( t, "real object returned ");
        assertEquals( t, l.getByUserName("mine"), "user name correct ");
        assertEquals( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Test
    public void testProvideName() {
        // create
        Turnout t = l.provide("" + getNumToTest1());
        // check
        assertNotNull( t, "real object returned ");
        assertSame( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Test
    public void testProvideWithoutWithPrefix() throws IllegalArgumentException {
        Turnout psa = l.provide(""+getASystemNameWithNoPrefix());
        Turnout psb = l.provide(l.getSystemPrefix()+"T"+getASystemNameWithNoPrefix());
        assertSame( psa, psb, "Provide Without then With Prefix");
    }

    @Test
    public void testProvideWithWithoutPrefix() throws IllegalArgumentException {
        Turnout psa = l.provide(l.getSystemNamePrefix()+getASystemNameWithNoPrefix());
        Turnout psb = l.provide(""+getASystemNameWithNoPrefix());
        assertSame( psa, psb, "Provide With then Without Prefix");
    }

    @Test
    public void testProvideFailWithPrefix() {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> l.provide(l.getSystemPrefix()+"T"));
        assertNotNull(ex);
        JUnitAppender.assertErrorMessageStartsWith("Invalid system name for Turnout: ");

    }

    @Test
    public void testDefaultSystemName() {
        listenerResult = false;
        l.addPropertyChangeListener(new Listen());
        // create
        Turnout t = l.provideTurnout("" + getNumToTest1());
        // check
        assertNotNull( t, "real object returned ");
        assertSame( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
        assertTrue(listenerResult);
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Turnout t1 = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        assertNotNull( t1, "t1 real object returned ");
        assertSame( t1, l.getByUserName("mine"), "same by user ");
        assertSame( t1, l.getBySystemName(getSystemName(getNumToTest1())),
            "same by system ");

        Turnout t2 = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        assertNotNull( t2, "t2 real object returned ");
        // check
        assertSame( t1, t2, "same new ");
    }

    @Test
    public void testMisses() {
        // try to get nonexistant turnouts
        assertNull( l.getByUserName("foo"));
        assertNull( l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout("" + getNumToTest2());

        assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
    }

    @Test
    public void testRename() {
        // get turnout
        Turnout t1 = l.newTurnout(getSystemName(getNumToTest1()), "before");
        assertNotNull( t1, "t1 real object ");
        t1.setUserName("after");
        Turnout t2 = l.getByUserName("after");
        assertEquals( t1, t2, "same object");
        assertNull( l.getByUserName("before"), "no old object");
    }

    @Test
    public void testThrownText(){
        assertEquals( Bundle.getMessage("TurnoutStateThrown"),l.getThrownText(),
            "thrown text");
    }

    @Test
    public void testClosedText(){
        assertEquals( Bundle.getMessage("TurnoutStateClosed"), l.getClosedText(),
            "closed text");
    }

    @Test
    public void testSetAndGetOutputInterval() {
        assertEquals( 250, l.getOutputInterval(),
            "default outputInterval");
        l.getMemo().setOutputInterval(21);
        assertEquals( 21, l.getMemo().getOutputInterval(),
            "new outputInterval set get in memo");
        assertEquals( 21, l.getOutputInterval(),
            "new outputInterval via turnoutManager");
        l.setOutputInterval(50);
        assertEquals( 50, l.getOutputInterval(),
                "new outputInterval from AbstractTurnoutManager");
        assertEquals( 50, l.getMemo().getOutputInterval(),
            "new outputInterval from memo");
    }

    @Disabled("Turnout managers don't support auto system names")
    @Test
    @Override
    public void testAutoSystemNames() {
    }

    @Test
    public void testGetEntryToolTip(){
        assertNotNull( l.getEntryToolTip(), "getEntryToolTip not null");
        assertTrue( l.getEntryToolTip().length() > 5, "Entry ToolTip Contains text");
    }

    /**
     * Number of turnout to test.
     * Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     * @return a Turnout number.
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

}
