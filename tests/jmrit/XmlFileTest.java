/** 
 * XmlFileTest.java
 *
 * Description:	
 * @author			Bob Jacobsen  Copyright 2001
 * @version			
 */

package jmri.jmrit;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.jdom.*;
import org.jdom.output.*;

public class XmlFileTest extends TestCase {

	public void testCheckFile() {
		// XmlFile is abstract, so can't check ctor directly; use local class
		XmlFile x = new XmlFile() {
		};
		
		Assert.assertTrue("existing file ", x.checkFile("xml/"));
		Assert.assertTrue("non-existing file ", !x.checkFile("dummy file not expected to exist"));
	}
	

	// from here down is testing infrastructure
	
	public XmlFileTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {XmlFileTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XmlFileTest.class);
		return suite;
	}
	
	// static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XmlFileTest.class.getName());

}
