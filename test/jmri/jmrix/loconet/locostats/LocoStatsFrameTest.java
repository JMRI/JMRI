package jmri.jmrix.loconet.locostats;

import junit.framework.*;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LnConstants;

/**
 * Tests for the LocoStatsFrame class
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 * @version     $Revision: 1.1 $
 */
public class LocoStatsFrameTest extends TestCase {


    public void testDefaultFormat() {
        LocoStatsFrame f = new LocoStatsFrame(){
            public void requestUpdate() {  // replace actual transmit
                updatePending = true;
            }
            void report(String m) {}  // suppress messages
        };
        f.setTitle("Default LocoStats Window");
        f.setVisible(true);
    }
    
    public void testLocoBufferFormat() {
        LocoStatsFrame f = new LocoStatsFrame(){
            public void requestUpdate() {  // replace actual transmit
                updatePending = true;
            }
            void report(String m) {}  // suppress messages
        };
        f.setTitle("LocoBuffer Stats Window");
        f.setLocation(0, 150);
        f.setVisible(true);
        f.requestUpdate();
        f.message(new LocoNetMessage(
            new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x50, 0x50, 0x01, 0x0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        ));
    }
    
    public void testPR2Format() {
        LocoStatsFrame f = new LocoStatsFrame(){
            public void requestUpdate() {  // replace actual transmit
                updatePending = true;
            }
            void report(String m) {}  // suppress messages
        };
        f.setTitle("PR2 Stats Window");
        f.setLocation(0, 300);
        f.setVisible(true);
        f.requestUpdate();
        f.message(new LocoNetMessage(
            new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01, 
                        0x00, 1, 2, 0, 4, 
                        0x00, 5, 6, 0, 0, 
                      0}
        ));
    }
    
    public void testMS100Format() {
        LocoStatsFrame f = new LocoStatsFrame(){
            public void requestUpdate() {  // replace actual transmit
                updatePending = true;
            }
            void report(String m) {}  // suppress messages
        };
        f.setTitle("MS100 Stats Window");
        f.setLocation(0, 450);
        f.setVisible(true);
        f.requestUpdate();
        f.message(new LocoNetMessage(
            new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01, 
                        0x00, 1, 2, 0x20, 4, 
                        0x00, 5, 6, 0, 0, 
                      0}
        ));
    }
    
    // from here down is testing infrastructure

    public LocoStatsFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoStatsFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoStatsFrameTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoStatsFrameTest.class.getName());
}
