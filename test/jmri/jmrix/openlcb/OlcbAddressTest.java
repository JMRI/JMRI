// OlcbAddressTest.java

package jmri.jmrix.openlcb;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanMessage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbAddress class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 * @version     $Revision: 1.1 $
 */
public class OlcbAddressTest extends TestCase {

    public void testCbusAddressOK() {
        // +/- form
        assertTrue(new OlcbAddress("+001").check());
        assertTrue(new OlcbAddress("-001").check());
        
        // hex form
        assertTrue(new OlcbAddress("x0ABC").check());
        assertTrue(new OlcbAddress("x0abc").check());
        assertTrue(new OlcbAddress("xa1b2c3").check());
        assertTrue(new OlcbAddress("x123456789ABCDEF0").check());
        
        // n0e0 form
        assertTrue(new OlcbAddress("+n1e2").check());
        assertTrue(new OlcbAddress("+n01e002").check());
        assertTrue(new OlcbAddress("+1e2").check());
        assertTrue(new OlcbAddress("-n1e2").check());
        assertTrue(new OlcbAddress("-n01e002").check());
        assertTrue(new OlcbAddress("-1e2").check());
        assertTrue(new OlcbAddress("n1e2").check());
        assertTrue(new OlcbAddress("n01e002").check());
        assertTrue(new OlcbAddress("1e2").check());
        assertTrue(new OlcbAddress("+n12e34").check());
        assertTrue(new OlcbAddress("+n12e35").check());
        
    }

    public void testCbusAddressNotOK() {
        assertTrue(!new OlcbAddress("+0A1").check());
        assertTrue(!new OlcbAddress("- 001").check());
        assertTrue(!new OlcbAddress("ABC").check());

        assertTrue(!new OlcbAddress("xABC").check());    // odd number of digits     
        assertTrue(!new OlcbAddress("xprs0").check());

        assertTrue(!new OlcbAddress("+n1e").check());
        assertTrue(!new OlcbAddress("+ne1").check());
        assertTrue(!new OlcbAddress("+e1").check());
        assertTrue(!new OlcbAddress("+n1").check());

        // multiple address not OK
        assertTrue(!new OlcbAddress("+1;+1;+1").check());
    }

    public void testCbusIdParseMatchReply() {
        assertTrue(new OlcbAddress("+12").match(
                new CanReply(
                    new int[]{OlcbConstants.CBUS_ACON,0x00,0x00,0x00,12}
        )));
        assertTrue(new OlcbAddress("-12").match(
                new CanReply(
                    new int[]{OlcbConstants.CBUS_ACOF,0x00,0x00,0x00,12}
        )));
        assertTrue(new OlcbAddress("x123456789ABCDEF0").match(
                new CanReply(
                    new int[]{0x12,0x34,0x56,0x78,
                              0x9A,0xBC,0xDE,0xF0}
        )));
    }
    
    public void testCbusIdParseMatchMessage() {
        assertTrue(new OlcbAddress("+12").match(
                new CanMessage(
                    new int[]{OlcbConstants.CBUS_ACON,0x00,0x00,0x00,12}
        )));
        assertTrue(new OlcbAddress("-12").match(
                new CanMessage(
                    new int[]{OlcbConstants.CBUS_ACOF,0x00,0x00,0x00,12}
        )));
        assertTrue(new OlcbAddress("x123456789ABCDEF0").match(
                new CanMessage(
                    new int[]{0x12,0x34,0x56,0x78,
                              0x9A,0xBC,0xDE,0xF0}
        )));
    }
    
    public void testNEformMatch() {
        assertTrue(new OlcbAddress("+n12e34").match(
                new CanMessage(
                    new int[]{OlcbConstants.CBUS_ACON,0x00,12,0x00,34}
        )));

        assertTrue(new OlcbAddress("+12e34").match(
                new CanMessage(
                    new int[]{OlcbConstants.CBUS_ACON,0x00,12,0x00,34}
        )));

        assertTrue(new OlcbAddress("12e34").match(
                new CanMessage(
                    new int[]{OlcbConstants.CBUS_ACON,0x00,12,0x00,34}
        )));

        assertTrue(new OlcbAddress("n12e34").match(
                new CanMessage(
                    new int[]{OlcbConstants.CBUS_ACON,0x00,12,0x00,34}
        )));

        assertTrue(new OlcbAddress("-n12e34").match(
                new CanMessage(
                    new int[]{OlcbConstants.CBUS_ACOF,0x00,12,0x00,34}
        )));
    }
    
    public void testCbusIdNotParse() {
        assertTrue(!new OlcbAddress("-12").match(
                new CanReply(
                    new int[]{OlcbConstants.CBUS_ACON,0x00,0x00,0x00,12}
        )));
        assertTrue(!new OlcbAddress("-268").match(
                new CanReply(
                    new int[]{OlcbConstants.CBUS_ACOF,0x00,0x00,0x00,12}
        )));
    }
    
    public void testPlusMinus() {
        assertTrue( (new OlcbAddress("+001")).equals(new OlcbAddress("+001")));        
        assertTrue( (new OlcbAddress("+001")).equals(new OlcbAddress("x9000000001")));
        assertTrue( (new OlcbAddress("-200003")).equals(new OlcbAddress("x9100020003")));
        
    }

    public void testEqualsOK() {
        assertTrue( (new OlcbAddress("+001")).equals(new OlcbAddress("+001")));
        assertTrue( (new OlcbAddress("+001")).equals(new OlcbAddress("x9000000001")));
    }
    
    public void testSplitCheckOK() {
        assertTrue(new OlcbAddress("+001").checkSplit());
        assertTrue(new OlcbAddress("-001").checkSplit());
        assertTrue(new OlcbAddress("x0ABC").checkSplit());        
        assertTrue(new OlcbAddress("x0abc").checkSplit());        
        assertTrue(new OlcbAddress("xa1b2c3").checkSplit());        
        assertTrue(new OlcbAddress("x123456789ABCDEF0").checkSplit());        

        assertTrue(new OlcbAddress("+001;+001").checkSplit());
        assertTrue(new OlcbAddress("-001;+001").checkSplit());
        assertTrue(new OlcbAddress("x0ABC;+001").checkSplit());        
        assertTrue(new OlcbAddress("x0abc;+001").checkSplit());        
        assertTrue(new OlcbAddress("xa1b2c3;+001").checkSplit());        
        assertTrue(new OlcbAddress("x123456789ABCDEF0;+001").checkSplit());        
    }
    
    public void testMultiTermSplitCheckOK() {
        assertTrue(new OlcbAddress("+1;+1;+1").checkSplit());
        assertTrue(new OlcbAddress("+1;+1").checkSplit());
        assertTrue(new OlcbAddress("+n12e34;+1").checkSplit());
        assertTrue(new OlcbAddress("+1;x1234").checkSplit());
        assertTrue(new OlcbAddress("+1;n12e34").checkSplit());
        assertTrue(new OlcbAddress("+1;+n12e34").checkSplit());
        assertTrue(new OlcbAddress("+n12e34;+n12e35").checkSplit());
    }
        
    public void testSplitCheckNotOK() {
        assertTrue(!new OlcbAddress("+0A1").check());
        assertTrue(!new OlcbAddress("- 001").check());
        assertTrue(!new OlcbAddress("ABC").check());        
        assertTrue(!new OlcbAddress("xprs0").check());        

        assertTrue(!new OlcbAddress("+1;;+1").checkSplit());
        assertTrue(!new OlcbAddress("+001;").checkSplit());
        assertTrue(!new OlcbAddress("-001;").checkSplit());
        assertTrue(!new OlcbAddress("-001;;").checkSplit());
        assertTrue(!new OlcbAddress("xABC;").checkSplit());
        assertTrue(!new OlcbAddress("xabc;").checkSplit());
        assertTrue(!new OlcbAddress("xa1b2c3;").checkSplit());
        assertTrue(!new OlcbAddress("x123456789ABCDEF0;").checkSplit());   

        assertTrue(!new OlcbAddress("+001;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("-001;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("xABC;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("xabc;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("xa1b2c3;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("x123456789ABCDEF0;xprs0").checkSplit());
    }

    public void testSplit() {
        OlcbAddress a;
        OlcbAddress[] v;
        
        a = new OlcbAddress("+001");
        v = a.split();
        assertTrue(v.length==1);
        assertTrue(new OlcbAddress("+001").equals(v[0]));
        
        a = new OlcbAddress("+001;-2");
        v = a.split();
        assertTrue(v.length==2);
        assertTrue(new OlcbAddress("+001").equals(v[0]));
        assertTrue(new OlcbAddress("-2").equals(v[1]));
        
    }

    // from here down is testing infrastructure

    public OlcbAddressTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
    	String[] testCaseName = {OlcbAddressTest.class.getName()};
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbAddressTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbAddressTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
