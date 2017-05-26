package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the NamedBean interface
 *
 * @author	Bob Jacobsen Copyright (C) 2017
 */
public class NamedBeanTest extends TestCase {

    // Note: This shows that BadUserNameException doesn't (yet) have to be caught or declared
    // Eventually that will go away, and that'll be OK
    public void testNormalizePassThrough() {
        String testString = "  foo ";
        String normalForm = NamedBean.normalizeUserName(testString);
        //note: normalizeUserName now .trim()'s;
        Assert.assertEquals("foo", normalForm);
    }


    // from here down is testing infrastructure
    public NamedBeanTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NamedBeanTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NamedBeanTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
