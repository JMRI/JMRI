package jmri.jmrit.decoderdefn;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;

/** 
 * DecoderDefnTest.java
 *
 * Description:	    tests for the jmrit.decoderdefn package
 * @author			Bob Jacobsen
 * @version			
 */
public class DecoderDefnTest extends TestCase {

	
	// from here down is testing infrastructure
	
	public DecoderDefnTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DecoderDefnTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.decoderdefn");
		suite.addTest(jmri.jmrit.decoderdefn.IdentifyDecoderTest.suite());
		suite.addTest(jmri.jmrit.decoderdefn.DecoderIndexFileTest.suite());
		return suite;
	}
	
}
