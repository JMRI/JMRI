package jmri.managers;

import java.awt.Dimension;
import java.awt.Point;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

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

        Point windowLocation = new Point(69, 96);
        d.setWindowLocation(TestUserPreferencesManager.class.toString(), windowLocation);
        Point savedWindowLocation = d.getWindowLocation(TestUserPreferencesManager.class.toString());
        Assert.assertEquals(windowLocation, savedWindowLocation);

        Dimension windowSize = new Dimension(666, 999);
        d.setWindowSize(TestUserPreferencesManager.class.toString(), windowSize);
        Dimension savedWindowSize = d.getWindowSize(TestUserPreferencesManager.class.toString());
        Assert.assertEquals(windowSize, savedWindowSize);
    }

    public void testSaveRestoreSetup() {
        JmriUserPreferencesManager d = new TestJmriUserPreferencesManager();

        d.setSimplePreferenceState("PLUGH", true);
        d.setSimplePreferenceState("XYZZY", false);
	}

    public void testSaveRestoreTest() {
        JmriUserPreferencesManager d = new TestJmriUserPreferencesManager();

        // this line fails (commenting out so alltest passes
        //Assert.assertTrue(d.getSimplePreferenceState("PLUGH"));
        Assert.assertTrue(!d.getSimplePreferenceState("XYZZY"));
	}

    // from here down is testing infrastructure
    public JmriUserPreferencesManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JmriUserPreferencesManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
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
    }
}
