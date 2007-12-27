// DataSourceTest.java

package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.DataSource class.
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision: 1.2 $
 */
public class DataSourceTest extends TestCase {

	public void testStringParsing3() throws java.io.IOException {
        // String input = "DATA,TIME,4105,3751,1423,2835";
        String input = "4105,3751,1423,2835";
        DataSource s = new DataSource();
        s.polledAddress = 4321;
	    Reading r = s.makeReading(input);
	    Assert.assertEquals("n sample OK", 4, r.getNSample());
	    Assert.assertTrue("val 1", 0.001 > Math.abs(r.getValue(0)-4105.));
	    Assert.assertTrue("val 2", 0.001 > Math.abs(r.getValue(1)-3751.));
	    Assert.assertTrue("val 3", 0.001 > Math.abs(r.getValue(2)-1423.));
	    Assert.assertTrue("val 4", 0.001 > Math.abs(r.getValue(3)-2835.));
	    Assert.assertEquals("ID ok", 4321, r.getID());
	}
        
	public void testStringParsing12() throws java.io.IOException {
        // String input = "DATA,TIME,1,2,3,4,5,6,7,8,9,10,11,12";
        String input = "1,2,3,4,5,6,7,8,9,10,11,12";
        DataSource s = new DataSource();
        s.polledAddress = 1234;
	    Reading r = s.makeReading(input);
	    Assert.assertEquals("ID ok", 1234, r.getID());
	    Assert.assertEquals("n sample OK", 12, r.getNSample());
	    Assert.assertTrue("val 1", 0.001 > Math.abs(r.getValue(0)-1.));
	    Assert.assertTrue("val 2", 0.001 > Math.abs(r.getValue(1)-2.));
	    Assert.assertTrue("val 3", 0.001 > Math.abs(r.getValue(2)-3.));
	    Assert.assertTrue("val 12", 0.001 > Math.abs(r.getValue(11)-12.));
	}
	// from here down is testing infrastructure

	public DataSourceTest(String s) {
            super(s);
	}

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {DataSourceTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(DataSourceTest.class);
            return suite;
	}

}
