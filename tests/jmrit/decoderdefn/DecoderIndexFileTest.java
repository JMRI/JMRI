/** 
 * DecoderIndexFileTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.decoderdefn;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.jdom.*;
import org.jdom.output.*;

public class DecoderIndexFileTest extends TestCase {

	public void testStart() {
		new DecoderIndexFile();
	}
	
	
	// from here down is testing infrastructure
	
	public DecoderIndexFileTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DecoderIndexFileTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(DecoderIndexFileTest.class);
		return suite;
	}
	
	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderIndexFileTest.class.getName());

}
