// SerialAdapterTest.java

package jmri.jmrix.rps.serial;

import jmri.jmrix.rps.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.serial.SerialAdapter class.
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision: 1.1 $
 */
public class SerialAdapterTest extends TestCase {

	public void testStringParsing3() throws java.io.IOException {
        // String input = "DATA,TIME,4105,3751,1423,2835";
        String input = "4105,3751,1423,2835";
        SerialAdapter s = new SerialAdapter();
	    Reading r = s.makeReading(input);
	    Assert.assertEquals("n sample OK", 4, r.getNSample());
	    Assert.assertTrue("val 1", 0.001 > Math.abs(r.getValue(0)-4105.));
	    Assert.assertTrue("val 2", 0.001 > Math.abs(r.getValue(1)-3751.));
	    Assert.assertTrue("val 3", 0.001 > Math.abs(r.getValue(2)-1423.));
	    Assert.assertTrue("val 4", 0.001 > Math.abs(r.getValue(3)-2835.));
	}
        
	public void testStringParsing12() throws java.io.IOException {
        // String input = "DATA,TIME,1,2,3,4,5,6,7,8,9,10,11,12";
        String input = "1,2,3,4,5,6,7,8,9,10,11,12";
        SerialAdapter s = new SerialAdapter();
	    Reading r = s.makeReading(input);
	    Assert.assertEquals("n sample OK", 12, r.getNSample());
	    Assert.assertTrue("val 1", 0.001 > Math.abs(r.getValue(0)-1.));
	    Assert.assertTrue("val 2", 0.001 > Math.abs(r.getValue(1)-2.));
	    Assert.assertTrue("val 3", 0.001 > Math.abs(r.getValue(2)-3.));
	}
	// from here down is testing infrastructure

	public SerialAdapterTest(String s) {
            super(s);
	}

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {SerialAdapterTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(SerialAdapterTest.class);
            return suite;
	}

}
