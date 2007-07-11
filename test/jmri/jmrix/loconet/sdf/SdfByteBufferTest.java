// SdfByteBufferTest.java

package jmri.jmrix.loconet.sdf;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;

/**
 * Tests for the jmri.jmrix.loconet.sdf.SdfByteBuffer class.
 * @author	Bob Jacobsen  Copyright 2007
 * @version	$Revision: 1.1 $
 */
public class SdfByteBufferTest extends TestCase {


    public void testFileCtor() throws java.io.IOException {
        SdfByteBuffer b = new SdfByteBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        
        String result = b.toString();
                
        // read the golden file
        String g = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader("java/test/jmri/jmrix/loconet/sdf/test2.golden.txt"));
            String str;
            while ((str = in.readLine()) != null) {
                g+=(str+"\n");
            }
            in.close();
        } catch (IOException e) {
            log.error("exception reading golden file: "+e);
            System.out.println("exception reading golden file: "+e);
        }
        
        if (!result.equals(g)) {
            // The next lines prints the answer in case you need
            // to create a new golden file
            System.out.println("--------------------");
            System.out.println(result);
            System.out.println("--------------------");
        }
        
        Assert.assertEquals("output as string", g, result);
    }

	// from here down is testing infrastructure

	public SdfByteBufferTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SdfByteBufferTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SdfByteBufferTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SdfByteBufferTest.class.getName());

}
