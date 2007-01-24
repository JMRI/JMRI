// LlnmonTest.java

package jmri.jmrix.loconet.locomon;

import apps.tests.Log4JFixture;

import jmri.jmrix.loconet.LocoNetMessage;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.StringUtil;

/**
 * Tests for the jmri.jmrix.loconet.locomon.Llnmon class.
 * @author	    Bob Jacobsen Copyright (C) 2002, 2007
 * @version         $Revision: 1.1 $
 */
public class LlnmonTest extends TestCase {

    public void testLissy1() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4,0x08,0x00,0x60,0x01,0x42,0x35,0x05});
        Llnmon f = new Llnmon();
        
        assertEquals("Lissy message 1", "Lissy 1: Loco 8501 moving south", f.displayMessage(l));
    }

    public void testLissy2() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4,0x08,0x00,0x40,0x01,0x42,0x35,0x25});
        Llnmon f = new Llnmon();
        
        assertEquals("Lissy message 2", "Lissy 1: Loco 8501 moving north", f.displayMessage(l));
    }

    // from here down is testing infrastructure

    public LlnmonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LlnmonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LlnmonTest.class);
        return suite;
    }

    Log4JFixture log4jfixtureInst = new Log4JFixture(this);

    protected void setUp() {
    	log4jfixtureInst.setUp();
    }

    protected void tearDown() {
    	log4jfixtureInst.tearDown();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LlnmonTest.class.getName());

}
