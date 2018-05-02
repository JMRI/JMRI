package jmri.jmrix.rps.csvinput;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.rps.csvinput package.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class CsvTest extends TestCase {

    public void testCreateReader() throws java.io.IOException {
        Object o = new com.csvreader.CsvReader("java/test/jmri/jmrix/rps/csvinput/testdata.csv");
        Assert.assertNotNull("exists", o);
    }

    public void testReading() throws java.io.IOException {
        com.csvreader.CsvReader o = new com.csvreader.CsvReader("java/test/jmri/jmrix/rps/csvinput/testdata.csv");
        Assert.assertTrue("read 1st line", o.readRecord());
        Assert.assertEquals("1st line column count", 4, o.getColumnCount());

        Assert.assertEquals("1st line datum 1", "1", o.get(0));
        Assert.assertEquals("1st line datum 2", "2", o.get(1));
        Assert.assertEquals("1st line datum 3", "3", o.get(2));
        Assert.assertEquals("1st line datum 4", "4", o.get(3));

        Assert.assertTrue("read 2nd line", o.readRecord());

        Assert.assertEquals("2nd line datum 1", "4", o.get(0));
        Assert.assertEquals("2nd line datum 2", "3", o.get(1));
        Assert.assertEquals("2nd line datum 3", "2", o.get(2));
        Assert.assertEquals("2nd line datum 4", "1", o.get(3));

        Assert.assertTrue("can't read 3rd line", !o.readRecord());
    }

    // from here down is testing infrastructure
    public CsvTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CsvTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(CsvTest.class);
        return suite;
    }

}
