// PocketTesterTest.java

package jmri.jmrix.pricom.pockettester;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.pricom.pockettester package.
 * @author      Bob Jacobsen  Copyright 2005
 * @version   $Revision: 1.2 $
 */
public class PocketTesterTest extends TestCase {

    // from here down is testing infrastructure

    public PocketTesterTest(String s) {
        super(s);
    }

    // convenient strings
    static String version = "PRICOM Design DCC Pocket Tester - Version 1.4\n";
    
    static String idlePacket = "Decoder Idle Packet\n";
    
    static String speed0003A = "ADR=0003 CMD=Speed    STP=128   DIR=Fwd SPD=S124\n";
    static String speed0003B = "ADR=0003 CMD=Speed    STP=128   DIR=Fwd SPD=S000\n";
    
    static String speed0123A = "ADR=0123 CMD=Speed    STP=128   DIR=Fwd SPD=S124\n";
    static String speed0123B = "ADR=0123 CMD=Speed    STP=128   DIR=Fwd SPD=S000\n";
    
    static String speed012A  = "ADR= 012 CMD=Speed    STP=128   DIR=Fwd SPD=S124\n";
    static String speed012B  = "ADR= 012 CMD=Speed    STP=128   DIR=Fwd SPD=S000\n";
    
    static String acc0222A   = "ADR=0222 CMD=Accessry VAL=Thrown/R(OFF) ACT=OFF\n";
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PocketTesterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.pricom.pockettester.PocketTesterTest");
        suite.addTest(jmri.jmrix.pricom.pockettester.DataSourceTest.suite());
        suite.addTest(jmri.jmrix.pricom.pockettester.PacketDataModelTest.suite());
        suite.addTest(jmri.jmrix.pricom.pockettester.PacketTableFrameTest.suite());
        suite.addTest(jmri.jmrix.pricom.pockettester.MonitorFrameTest.suite());
        return suite;
    }

}
