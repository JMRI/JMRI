package jmri.util.docbook;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.docbook.RevHistory class.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class RevHistoryTest extends TestCase {

    public void testCtor() {
        new RevHistory();
    }

    public void testAdd2() {
        RevHistory r = new RevHistory();
        r.addRevision("one");
        r.addRevision("two");

        Assert.assertEquals(2, r.list.size());

        Assert.assertEquals(1, r.list.get(0).revnumber);
        Assert.assertEquals(2, r.list.get(1).revnumber);

        Assert.assertEquals("one", r.list.get(0).revremark);
        Assert.assertEquals("two", r.list.get(1).revremark);
    }

    public void testToString() {
        RevHistory r2 = new RevHistory();
        r2.addRevision(2, "date 2", "initials 2", "remark 2");
        r2.addRevision(3, "date 3", "initials 3", "remark 3");

        String result = r2.toString(" ");
        String expected = " 2, date 2, initials 2, remark 2\n"
                + " 3, date 3, initials 3, remark 3\n";

        Assert.assertEquals(expected, result);
    }

    // from here down is testing infrastructure
    public RevHistoryTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RevHistoryTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RevHistoryTest.class);
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
