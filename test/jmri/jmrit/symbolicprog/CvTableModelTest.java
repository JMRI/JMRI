/** 
 * CvTableModelTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.symbolicprog;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

public class CvTableModelTest extends TestCase {

	public void testStart() {
		new CvTableModel(new JLabel());
	}
	
	
	// from here down is testing infrastructure
	
	public CvTableModelTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {CvTableModelTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(CvTableModelTest.class);
		return suite;
	}
	
	// static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CvTableModelTest.class.getName());

}
