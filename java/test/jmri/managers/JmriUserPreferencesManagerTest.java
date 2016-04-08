package jmri.managers;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.managers.JmriUserPreferencesManagerTest class.
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class JmriUserPreferencesManagerTest extends TestCase {

    public void testSetGet() {
        JmriUserPreferencesManager d = new TestJmriUserPreferencesManager();

        Assert.assertTrue(!d.getSimplePreferenceState("one"));

        d.setSimplePreferenceState("one", true);
        Assert.assertTrue(d.getSimplePreferenceState("one"));
        Assert.assertTrue(!d.getSimplePreferenceState("two"));

        d.setSimplePreferenceState("one", false);
        Assert.assertTrue(!d.getSimplePreferenceState("one"));
        Assert.assertTrue(!d.getSimplePreferenceState("two"));

    }

    // from here down is testing infrastructure
    public JmriUserPreferencesManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JmriUserPreferencesManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JmriUserPreferencesManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        System.setProperty("org.jmri.Apps.configFilename", "jmriconfig2.xml");
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    private static class TestJmriUserPreferencesManager extends JmriUserPreferencesManager {

        @Override
        protected void showMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember, int type) {
            // Uncomment to force failure if wanting to verify that showMessage does not get called.
            //org.slf4j.LoggerFactory.getLogger(TestUserPreferencesManager.class).error("showMessage called.", new Exception());
        }
    };
}
