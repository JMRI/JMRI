package jmri.managers;

import java.beans.PropertyChangeListener;

import jmri.Light;
import jmri.LightManager;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Abstract Base Class for LightManager tests in specific jmrix packages.
 * <p>
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests
 *
 * @author Bob Jacobsen 2003, 2006, 2008
 * @author  Paul Bender Copyright (C) 2016
 */
public abstract class AbstractLightMgrTestBase extends AbstractProvidingManagerTestBase<LightManager, Light> {

    // implementing classes must provide these abstract members:
    //
    abstract public void setUp(); // load t with actual object; create scaffolds as needed

    abstract public String getSystemName(int i);

    private boolean listenerResult = false;

    private class Listen implements PropertyChangeListener {

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        l.addPropertyChangeListener(new Listen());
        Light t = l.provide("" + getSystemName(getNumToTest1()));
        Assertions.assertNotNull( t, "real object returned ");
        Assertions.assertTrue( listenerResult, "listener triggered");
    }

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testLightPutGet() {
        // create
        Light t = l.newLight(getSystemName(getNumToTest1()), "mine");
        // check
        Assertions.assertNotNull( t, "real object returned ");
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testProvideName() {
        // create
        Light t = l.provide("" + getNumToTest1());
        // check
        Assertions.assertNotNull( t, "real object returned ");
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Light t = l.provideLight("" + getNumToTest1());
        // check
        Assertions.assertNotNull( t, "real object returned ");
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testProvideFailure() {
        Throwable throwable = Assert.assertThrows(IllegalArgumentException.class, () -> l.provideLight(""));
        Assertions.assertNotNull(throwable.getMessage(), "message exists in exception");
        jmri.util.JUnitAppender.assertErrorMessage("Invalid system name for Light: System name must start with \"" + l.getSystemNamePrefix() + "\".");
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Light t1 = l.newLight(getSystemName(getNumToTest1()), "mine");
        Assertions.assertNotNull( t1, "t1 real object returned ");
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest1())));

        Light t2 = l.newLight(getSystemName(getNumToTest1()), "mine");
        Assertions.assertNotNull( t2, "t2 real object returned ");
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    @Test
    public void testMisses() {
        // try to get nonexistant lights
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {
        Light t = l.provideLight(getSystemName(getNumToTest2()));
        String name = t.getSystemName();
        Assert.assertNull(l.getLight(name.toLowerCase()));
    }

    @Test
    public void testRename() {
        // get light
        Light t1 = l.newLight(getSystemName(getNumToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Light t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Test
    public void testGetEntryToolTip(){
        Assert.assertNotNull("getEntryToolTip not null", l.getEntryToolTip());
        Assert.assertTrue("Entry ToolTip Contains text",(l.getEntryToolTip().length()>5));
    }

    /**
     * Number of light to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     * @return 9 by default.
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }
}
