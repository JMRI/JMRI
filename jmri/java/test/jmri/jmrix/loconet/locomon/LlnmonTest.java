// LlnmonTest.java

package jmri.jmrix.loconet.locomon;

import org.apache.log4j.Logger;
import jmri.jmrix.loconet.LocoNetMessage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.locomon.Llnmon class.
 * @author	    Bob Jacobsen Copyright (C) 2002, 2007
 * @version         $Revision$
 */
public class LlnmonTest extends TestCase {

    public void testTransponding() {
        LocoNetMessage l;
        Llnmon f = new Llnmon();
        
        l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x20, 0x08, 0x20, 0x26});
        assertEquals("out A", "Transponder address 1056 (long) absent at 161 () (BDL16x Board 11 RX4 zone A).\n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x20, 0x08, 0x20, 0x04});
        assertEquals(" in A", "Transponder address 1056 (long) present at 161 () (BDL16x Board 11 RX4 zone A).\n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x22, 0x08, 0x20, 0x24});
        assertEquals(" in B", "Transponder address 1056 (long) present at 163 () (BDL16x Board 11 RX4 zone B).\n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x24, 0x08, 0x20, 0x04});
        assertEquals(" in C", "Transponder address 1056 (long) present at 165 () (BDL16x Board 11 RX4 zone C).\n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x26, 0x08, 0x20, 0x04});
        assertEquals(" in D", "Transponder address 1056 (long) present at 167 () (BDL16x Board 11 RX4 zone D).\n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x28, 0x08, 0x20, 0x04});
        assertEquals(" in E", "Transponder address 1056 (long) present at 169 () (BDL16x Board 11 RX4 zone E).\n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2A, 0x08, 0x20, 0x04});
        assertEquals(" in F", "Transponder address 1056 (long) present at 171 () (BDL16x Board 11 RX4 zone F).\n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2C, 0x08, 0x20, 0x04});
        assertEquals(" in G", "Transponder address 1056 (long) present at 173 () (BDL16x Board 11 RX4 zone G).\n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2E, 0x08, 0x20, 0x04});
        assertEquals(" in H", "Transponder address 1056 (long) present at 175 () (BDL16x Board 11 RX4 zone H).\n", f.displayMessage(l));
    }

    public void testLissy1() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4,0x08,0x00,0x60,0x01,0x42,0x35,0x05});
        Llnmon f = new Llnmon();
        
        assertEquals("Lissy message 1", "Lissy 1 IR Report: Loco 8501 moving south\n", f.displayMessage(l));
    }

    public void testLissy2() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4,0x08,0x00,0x40,0x01,0x42,0x35,0x25});
        Llnmon f = new Llnmon();
        
        assertEquals("Lissy message 2", "Lissy 1 IR Report: Loco 8501 moving north\n", f.displayMessage(l));
    }

    public void testLACK() {
        Llnmon f = new Llnmon();
        
        LocoNetMessage l = new LocoNetMessage(new int[]{0xB4,0x6F, 0x23, 0x07});
        assertEquals("LACK 23", "LONG_ACK: DCS51 programming reply, thought to mean OK.\n", f.displayMessage(l));
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

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(LlnmonTest.class.getName());

}
