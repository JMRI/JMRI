
package jmri.jmrix.loconet.locobuffer;

import junit.framework.*;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LnConstants;

/**
 * Tests for the LocoBufferStatsFrame class
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version     $Revision: 1.2 $
 */
public class LocoBufferStatsFrameTest extends TestCase {


    public void testDefaultFormat() {
        LocoBufferStatsFrame f = new LocoBufferStatsFrame(){
            public void requestUpdate() {  // replace actual transmit
                updatePending = true;
            }
            void report(String m) {}  // suppress messages
        };
        f.setTitle("Default LocoBuffer Stats Window");
        f.setVisible(true);
    }
    
    public void testLocoBufferFormat() {
        LocoBufferStatsFrame f = new LocoBufferStatsFrame(){
            public void requestUpdate() {  // replace actual transmit
                updatePending = true;
            }
            void report(String m) {}  // suppress messages
        };
        f.setTitle("LocoBuffer Stats Window");
        f.setVisible(true);
        f.requestUpdate();
        f.message(new LocoNetMessage(
            new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x50, 0x50, 0x01, 0x0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        ));
    }
    
    public void testPR2Format() {
        LocoBufferStatsFrame f = new LocoBufferStatsFrame(){
            public void requestUpdate() {  // replace actual transmit
                updatePending = true;
            }
            void report(String m) {}  // suppress messages
        };
        f.setTitle("PR2 Stats Window");
        f.setVisible(true);
        f.requestUpdate();
        f.message(new LocoNetMessage(
            new int[]{LnConstants.OPC_PEER_XFER, 0x10, 0x22, 0x22, 0x01, 0x0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        ));
    }
    
    // from here down is testing infrastructure

    public LocoBufferStatsFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoBufferStatsFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoBufferStatsFrameTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoBufferStatsFrameTest.class.getName());
}
