/** 
 * CvValueTest.java
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

import jmri.*;
import jmri.progdebugger.*;

public class CvValueTest extends TestCase {

	public void testStart() {
		new CvValue(12);
	}
	
	// can we create one and manipulate info?
	public void testCvValCreate() {
		CvValue cv = new CvValue(19);
		assert(cv.number() == 19);
		cv.setValue(23);
		assert(cv.getValue() == 23);
	}

	// check a read operation
	public void testCvValRead() {
		// initialize the system
		ProgDebugger p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		// create the CV value
		CvValue cv = new CvValue(91);
		cv.read(null);
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( cv.isBusy() && i++ < 100 )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+cv.getValue()+" state="+cv.getState());
		if (i==0) log.warn("textCvValRead saw an immediate return from isBusy");

		assert(i<100);
		assert(cv.getValue() == 123);
		assert(cv.getState() == CvValue.READ);
	}
	
	// check a write operation
	public void testCvValWrite() {
		// initialize the system
		ProgDebugger p = new ProgDebugger();
		InstanceManager.setProgrammer(p);
		
		// create the CV value
		CvValue cv = new CvValue(91);
		cv.setValue(12);
		cv.write(null); 
		// wait for reply (normally, done by callback; will check that later)
		int i = 0;
		while ( cv.isBusy() && i++ < 100 )  {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
		if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+cv.getValue()+" state="+cv.getState());
		if (i==0) log.warn("textCvValWrite saw an immediate return from isBusy");

		Assert.assertTrue("iterations ", i<100);
		Assert.assertEquals("cv value ", 12, cv.getValue());
		Assert.assertEquals("cv state ", CvValue.STORED, cv.getState());
		Assert.assertEquals("last value written ", 12, p.lastWrite());
	}
	
	// check the state diagram
	public void testCvValStates() {
		CvValue cv = new CvValue(21);
		assert(cv.getState() == CvValue.UNKNOWN);
		cv.setValue(23);
		assert(cv.getState() == CvValue.EDITTED);
	}
		

	// from here down is testing infrastructure
	
	public CvValueTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {CvValueTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(CvValueTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CvValue.class.getName());

}
