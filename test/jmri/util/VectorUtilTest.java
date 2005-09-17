// VectorUtilTest.java

package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;
import com.sun.java.util.collections.Comparable;

/**
 * Tests for the jmri.util.VectorUtil class.
 * @author	Bob Jacobsen  Copyright 2005
 * @version	$Revision: 1.1 $
 */
public class VectorUtilTest extends TestCase {

    public void testSort1() {
        String input[] = new String[]{ "A", "B", "C" };
        String output[] = new String[]{ "A", "B", "C" };
        Vector v = makeVector(input);
        VectorUtil.sort(v);
        compareResult(v, output);
    }

    public void testSort2() {
        String input[] = new String[]{ "A", "b", "C" };
        String output[] = new String[]{ "A", "C", "b" };
        Vector v = makeVector(input);
        VectorUtil.sort(v);
        compareResult(v, output);
    }

    public void testSort3() {
        String input[] = new String[]{ "B", "C", "A" };
        String output[] = new String[]{ "A", "B", "C" };
        Vector v = makeVector(input);
        VectorUtil.sort(v);
        compareResult(v, output);
    }

    public void testSort4() {
        String input[] = new String[]{ "c", "b", "a" };
        String output[] = new String[]{ "a", "b", "c" };
        Vector v = makeVector(input);
        VectorUtil.sort(v);
        compareResult(v, output);
    }

    public void testSort5() {
        String input[] = new String[]{ "A", "c", "b" };
        String output[] = new String[]{ "A", "b", "c" };
        Vector v = makeVector(input);
        VectorUtil.sort(v);
        compareResult(v, output);
    }

    public void testSort6() {
        String input[] = new String[]{ "A", "A", "b" };
        String output[] = new String[]{ "A", "A", "b" };
        Vector v = makeVector(input);
        VectorUtil.sort(v);
        compareResult(v, output);
    }

    
	// from here down is testing infrastructure

    private void compareResult(Vector result, String[] output) {
        Assert.assertEquals("length", output.length, result.size());
        for (int i=0; i<output.length; i++) {
            Assert.assertEquals("element "+i, output[i], result.elementAt(i));
        }
    }
            
    private Vector makeVector(String[] input) {
        Vector result = new Vector();
        for (int i=0; i<input.length; i++) {
            result.addElement(input[i]);
        }
        return result;
    }
    
	public VectorUtilTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {VectorUtilTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(VectorUtilTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VectorUtilTest.class.getName());

}
