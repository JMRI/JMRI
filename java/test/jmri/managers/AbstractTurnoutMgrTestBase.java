package jmri.managers;

import java.beans.PropertyChangeListener;
import jmri.Turnout;
import jmri.TurnoutManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
    @Before
    abstract public void setUp();

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
    }

    @Test
    public void testDispose() {
        if (l != null) {
            l.dispose();  // all we're really doing here is making sure the method exists
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testProvideFailure() {
        try {
            l.provideTurnout("");
        } catch (IllegalArgumentException ex) {
          jmri.util.JUnitAppender.assertErrorMessage("Invalid system name for Turnout: System name must start with \"" + l.getSystemNamePrefix() + "\".");
          throw ex;
        }
    }

    @Test
    public void testTurnoutPutGet() {
        // create
        Turnout t = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("user name correct ", t, l.getByUserName("mine"));
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testProvideName() {
        // create
        Turnout t = l.provide("" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout("" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Turnout t1 = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        Assert.assertTrue("t1 real object returned ", t1 != null);
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest1())));

        Turnout t2 = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        Assert.assertTrue("t2 real object returned ", t2 != null);
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    @Test
    public void testMisses() {
        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout("" + getNumToTest2());

        Assert.assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
    }

    @Test
    public void testRename() {
        // get turnout
        Turnout t1 = l.newTurnout(getSystemName(getNumToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Turnout t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Test
    public void testThrownText(){
         Assert.assertEquals("thrown text",Bundle.getMessage("TurnoutStateThrown"),l.getThrownText());
    }

    @Test
    public void testClosedText(){
         Assert.assertEquals("closed text",Bundle.getMessage("TurnoutStateClosed"),l.getClosedText());
    }

    @Ignore("Turnout managers doesn't support auto system names")
    @Test
    @Override
    public void testAutoSystemNames() {
    }

    /**
     * Number of turnout to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

}
