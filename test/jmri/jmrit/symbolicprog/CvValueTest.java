// CvValueTest.java

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

/**
 * Test CvValue class
 *
 * @author			Bob Jacobsen
 * @version         $Revision: 1.5 $
 */
public class CvValueTest extends TestCase {
    
    public void testStart() {
        new CvValue(12);
    }
    
    // can we create one and manipulate info?
    public void testCvValCreate() {
        CvValue cv = new CvValue(19);
        Assert.assertTrue(cv.number() == 19);
        cv.setValue(23);
        Assert.assertTrue(cv.getValue() == 23);
    }
    
    // check a read operation
    public void testCvValRead() {
        // initialize the system
        ProgDebugger p = new ProgDebugger();
        InstanceManager.setProgrammerManager(new DefaultProgrammerManager(p));
        
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
        
        Assert.assertTrue(i<100);
        Assert.assertTrue(cv.getValue() == 123);
        Assert.assertTrue(cv.getState() == CvValue.READ);
    }
    
    // check a confirm operation
    public void testCvValConfirmFail() {
        // initialize the system
        ProgDebugger p = new ProgDebugger();
        InstanceManager.setProgrammerManager(new DefaultProgrammerManager(p));
        p._confirmOK = false;
        
        // create the CV value
        CvValue cv = new CvValue(66);
        cv.setValue(91);
        
        cv.confirm(null);
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( cv.isBusy() && i++ < 100 )  {
            try {
                Thread.sleep(20);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i+" value="+cv.getValue()+" state="+cv.getState());
        if (i==0) log.warn("textCvValRead saw an immediate return from isBusy");
        
        Assert.assertTrue("loop passes before 100", i<100);
        Assert.assertEquals("CV value ", 91, cv.getValue());
        Assert.assertEquals("CV state ", CvValue.UNKNOWN, cv.getState());
    }
    
    // check a confirm operation
    public void testCvValConfirmPass() {
        // initialize the system
        ProgDebugger p = new ProgDebugger();
        p._confirmOK = true;
        InstanceManager.setProgrammerManager(new DefaultProgrammerManager(p));
        
        // create the CV value
        CvValue cv = new CvValue(66);
        cv.setValue(123);
        
        cv.confirm(null);
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
        
        Assert.assertTrue("loop passes before 100", i<100);
        Assert.assertEquals("CV value ", 123, cv.getValue());
        Assert.assertEquals("CV state ", CvValue.READ, cv.getState());
    }
    
    // check a write operation
    public void testCvValWrite() {
        // initialize the system
        log.debug("start testCvValWrite");
        ProgDebugger p = new ProgDebugger();
        InstanceManager.setProgrammerManager(new DefaultProgrammerManager(p));
        
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
        Assert.assertTrue(cv.getState() == CvValue.UNKNOWN);
        cv.setValue(23);
        Assert.assertTrue(cv.getState() == CvValue.EDITTED);
    }
    
    // check the initial color
    public void testInitialColor() {
        CvValue cv = new CvValue(21);
        Assert.assertEquals("initial color", CvValue.COLOR_UNKNOWN, cv.getTableEntry().getBackground());
    }
    
    // check color update for EDITTED
    public void testEdittedColor() {
        CvValue cv = new CvValue(21);
        cv.setValue(23);
        Assert.assertEquals("editted color", CvValue.COLOR_EDITTED, cv.getTableEntry().getBackground());
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
    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }
    
}
