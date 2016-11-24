/**
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests
 */
package jmri.managers;

import java.beans.PropertyChangeListener;
import jmri.Reporter;
import jmri.ReporterManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;



/**
 * Abstract Base Class for LightManager tests in specific jmrix packages. This
 * is not itself a test class, e.g. should not be added to a suite. Instead,
 * this forms the base for test classes, including providing some common tests
 *
 * @author	Bob Jacobsen 2003, 2006, 2008
 * @author      Paul Bender Copyright (C) 2016
 */
public abstract class AbstractReporterMgrTest {

    // implementing classes must provide these abstract members:
    //
    @Before
    abstract public void setUp();    	// load l with actual object; create scaffolds as needed

    abstract public String getSystemName(int i);

    protected ReporterManager l = null;	// holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        Assert.assertNotNull(l);
    }

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    @Ignore("Bad test.  Where is the USER NAME Fred set?")
    public void testReporterProvideReporter() {
        // Create
        Reporter t = l.provideReporter("" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("user name correct ", t == l.getByUserName("Fred"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));

        // Check that "providing" an already-created reporter returns the same object.
        Reporter t2 = l.provideReporter("" + getNumToTest2());
        Assert.assertTrue("provided same object ", t == t2);
    }

    @Test(expected=IllegalArgumentException.class)
    @Ignore("Not currently functional")
    public void testProvideFailure() {
        Reporter t = l.provideReporter("..");
    }

    @Test
    @Ignore("Bad test.  Depends on testReporterProvideReporter.  Must be independent or depend on BeforeClass and Before methods only.")
    public void testReporterGetBySystemName() {
        // Try a successful one -- the one that was added in testReporterProvideReporter()
        Reporter t = l.getBySystemName(getSystemName(getNumToTest1()));
        Assert.assertTrue("get retrieved existing object ", t != null);

        // Try a nonexistant one. Should return null
        t = l.getBySystemName(getSystemName(getNumToTest2()));
        Assert.assertTrue("get nonexistant object ", t == null);
    }

    @Test
    @Ignore("Bad test.  Depends on testReporterProvideReporter.  Must be independent or depend on BeforeClass and Before methods only.")
    public void testReporterGetByUserName() {
        // Try a successful one -- the one that was added in testReporterProvideReporter()
        Reporter t = l.getByUserName("Fred");
        Assert.assertTrue("get retrieved existing object ", t != null);

        // Try a nonexistant one. Should return null
        t = l.getBySystemName("Barney");
        Assert.assertTrue("get nonexistant object ", t == null);
    }

    @Test
    @Ignore("Bad test.  Depends on testReporterProvideReporter.  Must be independent or depend on BeforeClass and Before methods only.")
    public void testReporterGetByDisplayName() {
        // Try a successful one -- the one that was added in testReporterProvideReporter()
        Reporter t = l.getByDisplayName(getSystemName(getNumToTest1()));
        Assert.assertTrue("get retrieved existing object ", t != null);

        Reporter t2 = l.getByDisplayName("Fred");
        Assert.assertTrue("get retrieved existing object ", t2 == t);
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Reporter t = l.provideReporter("" + getNumToTest3());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest3())));
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Reporter t1 = l.newReporter(getSystemName(getNumToTest4()), "mine");
        Assert.assertTrue("t1 real object returned ", t1 != null);
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest4())));

        Reporter t2 = l.newReporter(getSystemName(getNumToTest4()), "mine");
        Assert.assertTrue("t2 real object returned ", t2 != null);
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    @Test
    public void testMisses() {
        // try to get nonexistant Reporters
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {
        Reporter t = l.provideReporter("" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getReporter(name.toLowerCase()));
    }

    @Test
    public void testRename() {
        // get reporter
        Reporter t1 = l.newReporter(getSystemName(getNumToTest5()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Reporter t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    /**
     * Number of light to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

    protected int getNumToTest3() {
        return 6;
    }

    protected int getNumToTest4() {
        return 5;
    }

    protected int getNumToTest5() {
        return 4;
    }
}
