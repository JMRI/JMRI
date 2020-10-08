package jmri.managers;

import java.beans.PropertyChangeListener;

import jmri.JmriException;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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
    @BeforeEach
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

    @Test
    public void testProvideFailure() {
        Assert.assertThrows(IllegalArgumentException.class, () -> l.provideTurnout(""));
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: System name must start with \"" + l.getSystemNamePrefix() + "\".");
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
    public void testProvideWithoutWithPrefix() throws IllegalArgumentException {
        Turnout psa = l.provide(""+getASystemNameWithNoPrefix());
        Turnout psb = l.provide(l.getSystemPrefix()+"T"+getASystemNameWithNoPrefix());
        Assert.assertTrue("Provide Without then With Prefix", psa == psb);
    }

    @Test
    public void testProvideWithWithoutPrefix() throws IllegalArgumentException {
        Turnout psa = l.provide(l.getSystemNamePrefix()+getASystemNameWithNoPrefix());
        Turnout psb = l.provide(""+getASystemNameWithNoPrefix());
        Assert.assertTrue("Provide With then Without Prefix", psa == psb);
    }
    
    @Test
    public void testProvideFailWithPrefix() throws IllegalArgumentException {
        
        Assert.assertThrows(IllegalArgumentException.class, () -> l.provide(l.getSystemPrefix()+"T"));
        JUnitAppender.assertErrorMessageStartsWith("Invalid system name for Turnout: ");
        
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
         Assert.assertEquals("thrown text", Bundle.getMessage("TurnoutStateThrown"),l.getThrownText());
    }

    @Test
    public void testClosedText(){
         Assert.assertEquals("closed text", Bundle.getMessage("TurnoutStateClosed"), l.getClosedText());
    }

    @Test
    public void testSetAndGetOutputInterval() {
        Assert.assertEquals("default outputInterval", 250, l.getOutputInterval());
        l.getMemo().setOutputInterval(21);
        Assert.assertEquals("new outputInterval in memo", 21, l.getMemo().getOutputInterval()); // set & get in memo
        Assert.assertEquals("new outputInterval via manager", 21, l.getOutputInterval()); // get via turnoutManager
        l.setOutputInterval(50);
        Assert.assertEquals("new outputInterval from manager", 50, l.getOutputInterval()); // interval stored in AbstractTurnoutManager
        Assert.assertEquals("new outputInterval from manager", 50, l.getMemo().getOutputInterval()); // get from memo
    }

    @Disabled("Turnout managers don't support auto system names")
    @Test
    @Override
    public void testAutoSystemNames() {
    }
    
    @Test
    public void testGetEntryToolTip(){
        Assert.assertNotNull("getEntryToolTip not null", l.getEntryToolTip());
        Assert.assertTrue("Entry ToolTip Contains text",(l.getEntryToolTip().length()>5));
    }
    
    @Test
    public void testGetNextValidAddress() throws JmriException {
        
        if (!l.allowMultipleAdditions(l.getSystemNamePrefix())){
            return;
        }
        
        Assert.assertNotNull("next valid before OK", l.getNextValidAddress(getASystemNameWithNoPrefix(), l.getSystemPrefix(),false));
    
        Assert.assertNotEquals("requesting ignore existing does not return same", 
                l.getNextValidAddress(getASystemNameWithNoPrefix(), l.getSystemPrefix(),true),
                l.getNextValidAddress(getASystemNameWithNoPrefix(), l.getSystemPrefix(),false));
        
        Turnout t =  l.provideTurnout(getASystemNameWithNoPrefix());
        Assert.assertNotNull("exists", t);
        
        String nextValidAddr = l.getNextValidAddress(getASystemNameWithNoPrefix(), l.getSystemPrefix(),false);
        Turnout nextValid =  l.provideTurnout(nextValidAddr);
        Assert.assertNotNull("exists", nextValid);
        Assert.assertNotEquals(nextValid, t);
        
    }
    
    @Test
    public void testIncorrectGetNextValidAddress() {
        if (!l.allowMultipleAdditions(l.getSystemNamePrefix())){
            return;
        }
        boolean contains = Assert.assertThrows(JmriException.class,
            ()->{
                l.getNextValidAddress("NOTANINCREMENTABLEADDRESS", l.getSystemPrefix(),false);
            }).getMessage().contains("NOTANINCREMENTABLEADDRESS");
        Assert.assertTrue("Exception contained incorrect address", contains);
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
