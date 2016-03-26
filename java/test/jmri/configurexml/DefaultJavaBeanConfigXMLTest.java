package jmri.configurexml;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Checks of java bean storage.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class DefaultJavaBeanConfigXMLTest extends TestCase {

    public void testStoreBean() {
        DefaultJavaBeanConfigXML x = new DefaultJavaBeanConfigXML();
        x.store(new TestBean1());
    }

    public void testTestBean() {
        TestBean1 tb1 = new TestBean1();
        Assert.assertTrue(tb1.equals(tb1));

        TestBean1 tb2 = new TestBean1();
        Assert.assertTrue(tb1.equals(tb2));

        tb2.setA("foo");
        tb1.setA("bar");
        Assert.assertFalse(tb1.equals(tb2));

        TestBean1 tb3 = new TestBean1();
        TestBean1 tb4 = new TestBean1();
        tb3.setB(77);
        tb4.setB(78);
        Assert.assertFalse(tb3.equals(tb4));
    }

    public void testLoadBeanDefault() throws Exception {
        DefaultJavaBeanConfigXML x = new DefaultJavaBeanConfigXML();
        TestBean1 start = new TestBean1();

        TestBean1 end = (TestBean1) x.unpack(x.store(start));

        Assert.assertTrue(start.equals(end));
    }

    public void testLoadBeanValue() throws Exception {
        DefaultJavaBeanConfigXML x = new DefaultJavaBeanConfigXML();
        TestBean1 start = new TestBean1();
        start.setA("foo");
        start.setB(88);

        TestBean1 end = (TestBean1) x.unpack(x.store(start));

        Assert.assertTrue(start.equals(end));
    }

    // from here down is testing infrastructure
    public DefaultJavaBeanConfigXMLTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DefaultJavaBeanConfigXMLTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultJavaBeanConfigXMLTest.class);
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
