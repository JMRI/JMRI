package jmri.util.swing;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

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

        @Override
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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JCBHandleTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
