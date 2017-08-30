package jmri.managers;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the jmri.managers.DefaultUserMessagePreferencesTest class.
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class DefaultUserMessagePreferencesTest extends TestCase {

    public void testSetGet() {
        DefaultUserMessagePreferences d = new DefaultUserMessagePreferences() {
            @Override
            public void displayRememberMsg() {
            }
        };
        jmri.util.JUnitAppender.assertWarnMessage("Won't protect preferences at shutdown without registered ShutDownManager");

        Assert.assertTrue(!d.getSimplePreferenceState("one"));

        d.setSimplePreferenceState("one", true);
        Assert.assertTrue(d.getSimplePreferenceState("one"));
        Assert.assertTrue(!d.getSimplePreferenceState("two"));

        d.setSimplePreferenceState("one", false);
        Assert.assertTrue(!d.getSimplePreferenceState("one"));
        Assert.assertTrue(!d.getSimplePreferenceState("two"));

    }

    // from here down is testing infrastructure
    public DefaultUserMessagePreferencesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DefaultUserMessagePreferencesTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultUserMessagePreferencesTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
        System.setProperty("org.jmri.Apps.configFilename", "jmriconfig2.xml");
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
