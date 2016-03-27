package jmri.util.swing;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author	Bob Jacobsen Copyright 2014
 */
public class JCBHandleTest extends TestCase {

    public void testToStringReal() {
        JCBHandle<DummyObject> a = new JCBHandle<DummyObject>(new DummyObject());
        Assert.assertEquals("dummy output", a.toString());
    }

    public void testToStringEmpty() {
        JCBHandle<DummyObject> a = new JCBHandle<DummyObject>("no object");
        Assert.assertEquals("no object", a.toString());
    }

    class DummyObject {

        public String toString() {
            return "dummy output";
        }
    }

    // from here down is testing infrastructure
    public JCBHandleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JCBHandleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JCBHandleTest.class);

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
