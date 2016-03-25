// LlnmonTest.java
package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.LocoNetMessage;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.locomon.Llnmon class.
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2007
 * @author      B. Milhaupt  Copyright (C) 2015
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

    public void testSVProgrammingProtocolV1() {
        LocoNetMessage l;
        Llnmon f = new Llnmon();

        l = new LocoNetMessage(new int[]{0xE5, 0x10, 0x50, 0x53, 0x01, 0x00, 0x02, 0x03, 0x00, 0x00, 0x10, 0x01, 0x00, 0x00, 0x00, 0x18});
        assertEquals(" read SV 3", "LocoBuffer => LocoIO@53/1 Query SV3.\n", f.displayMessage(l));
    }
    
    public void testSVProgrammingProtocolV2() {
        LocoNetMessage l;
        Llnmon f = new Llnmon();

        l = new LocoNetMessage(new int[]{0xE5, 0x10, 0x01, 0x02, 0x02, 0x10, 0x23, 0x00, 0x03, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertEquals(" SV2 test 1", "(SV Format 2) Read single SV request to destination address 35 initiated by agent 1:\n\tRead request for SV3\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xE5, 0x10, 0x01, 0x48, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x49});
        assertEquals(" SV2 test 2", "(SV Format 2) Reply from destination address 513 to Identify device request initiated by agent 1:\n\tDevice characteristics are manufacturer 3, developer number 4, product 1,541, serial number 2,055\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xE5, 0x10, 0x01, 0x01, 0x02, 0x12, 0x40, 0x20, 0x10, 0x08, 0x10, 0x04, 0x02, 0x01, 0x7F, 0x0A});
        assertEquals(" SV2 test 3","(SV Format 2) Write single SV request to destination address 41,024 initiated by agent 1:\n\tChange SV2,064 to 4\n"
                , f.displayMessage(l));
        

        
        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x08});
        assertEquals(" SV test 4", "(SV Format 2) Write single SV request to destination address 0 initiated by agent 1:\n\tChange SV0 to 0\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x02, 0x10, 0x01, 0x02, 0x04, 0x08, 0x10, 0x10, 0x20, 0x40, 0x7F, 0x08});
        assertEquals(" SV test 5", "(SV Format 2) Write single SV request to destination address 513 initiated by agent 1:\n\tChange SV2,052 to 16\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x02, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x03});
        assertEquals(" SV test 6", "(SV Format 2) Read single SV request to destination address 513 initiated by agent 1:\n\tRead request for SV1,027\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x03, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x02});
        assertEquals(" SV test 7", "(SV Format 2) Write single SV (masked) request to destination address 513 initiated by agent 1:\n\tchange SV1,027 to 5, applying write mask 6\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x01, 0x02, 0x04, 0x08, 0x10, 0x10, 0x20, 0x40, 0x7F, 0x0C});
        assertEquals(" SV test 8", "(SV Format 2) Write four request to destination address 513 initiated by agent 1:\n\twrite SVs 2,052 thru 2,055(?) with values 16, 32, 64, and 127\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x02, 0x04, 0x08, 0x10, 0x10, 0x20, 0x40, 0x7F, 0x01, 0x0C});
        assertEquals(" SV test 9", "(SV Format 2) Write four request to destination address 1,026 initiated by agent 1:\n\twrite SVs 4,104 thru 4,107(?) with values 32, 64, 127, and 1\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x04, 0x08, 0x10, 0x20, 0x10, 0x40, 0x7F, 0x01, 0x02, 0x0C});
        assertEquals(" SV test 10", "(SV Format 2) Write four request to destination address 2,052 initiated by agent 1:\n\twrite SVs 8,208 thru 8,211(?) with values 64, 127, 1, and 2\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x08, 0x10, 0x20, 0x40, 0x10, 0x7F, 0x01, 0x02, 0x04, 0x0C});
        assertEquals(" SV test 11", "(SV Format 2) Write four request to destination address 4,104 initiated by agent 1:\n\twrite SVs 16,416 thru 16,419(?) with values 127, 1, 2, and 4\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x10, 0x20, 0x40, 0x7F, 0x10, 0x01, 0x02, 0x04, 0x08, 0x0C});
        assertEquals(" SV test 12", "(SV Format 2) Write four request to destination address 8,208 initiated by agent 1:\n\twrite SVs 32,576 thru 32,579(?) with values 1, 2, 4, and 8\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x20, 0x40, 0x7F, 0x01, 0x10, 0x02, 0x04, 0x08, 0x10, 0x0C});
        assertEquals(" SV test 13", "(SV Format 2) Write four request to destination address 16,416 initiated by agent 1:\n\twrite SVs 383 thru 386(?) with values 2, 4, 8, and 16\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x40, 0x7F, 0x01, 0x02, 0x10, 0x04, 0x08, 0x10, 0x20, 0x0C});
        assertEquals(" SV test 14", "(SV Format 2) Write four request to destination address 32,576 initiated by agent 1:\n\twrite SVs 513 thru 516(?) with values 4, 8, 16, and 32\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x11, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x0D});
        assertEquals(" SV test 15", "(SV Format 2) Write four request to destination address 128 initiated by agent 1:\n\twrite SVs 0 thru 3(?) with values 0, 0, 0, and 0\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x12, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x0E});
        assertEquals(" SV test 16", "(SV Format 2) Write four request to destination address 32,768 initiated by agent 1:\n\twrite SVs 0 thru 3(?) with values 0, 0, 0, and 0\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x14, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x08});
        assertEquals(" SV test 17", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n\twrite SVs 128 thru 131(?) with values 0, 0, 0, and 0\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x18, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x04});
        assertEquals(" SV test 18", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n\twrite SVs 32,768 thru 32,771(?) with values 0, 0, 0, and 0\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x11, 0x00, 0x00, 0x00, 0x00, 0x0D});
        assertEquals(" SV test 19", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n\twrite SVs 0 thru 3(?) with values 128, 0, 0, and 0\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x12, 0x00, 0x00, 0x00, 0x00, 0x0E});
        assertEquals(" SV test 20", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n\twrite SVs 0 thru 3(?) with values 0, 128, 0, and 0\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x14, 0x00, 0x00, 0x00, 0x00, 0x08});
        assertEquals(" SV test 21", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n\twrite SVs 0 thru 3(?) with values 0, 0, 128, and 0\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x04});
        assertEquals(" SV test 22", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n\twrite SVs 0 thru 3(?) with values 0, 0, 0, and 128\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x06, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x07});
        assertEquals(" SV test 23", "(SV Format 2) Read four SVs request to destination address 513 initiated by agent 1:\n\tread SVs 1,027 thru 1,030(?)\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x07, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x06});
        assertEquals(" SV test 24", "(SV Format 2) Discover all devices request initiated by agent 1\n"
                , f.displayMessage(l));
        
        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x08, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x09});
        assertEquals(" SV test 25", "(SV Format 2) Identify Device request initiated by agent 1 to destination address 513\n"
                , f.displayMessage(l));
        
        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x09, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x08});
        assertEquals(" SV test 26", "(SV Format 2) Change address request initiated by agent 1:\n\tChange address of device with manufacturer 3, developer number 4, product 1,541, and serial number 2,055 so that it responds as destination address 513\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x0A, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x0B});
        assertEquals(" SV test 27", "Peer to Peer transfer: SRC=0x1, DSTL=0xa, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x0F, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x0E});
        assertEquals(" SV test 28", "(SV Format 2) Reconfigure request initiated by agent 1 to destination address 513\n"
                , f.displayMessage(l));
        
        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x41, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x40});
        assertEquals(" SV test 29", "(SV Format 2) Reply from destination address 513 for Write single SV request initiated by agent 1:\n\tSV1,027 current value is 5\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x42, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x43});
        assertEquals(" SV test 30", "(SV Format 2) Reply from destination address 513 for Read single SV request initiated by agent 1:\n\tSV1,027 current value is 5\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x43, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x42});
        assertEquals(" SV test 31", "(SV Format 2) Reply from destination address 513 for Write single SV (masked) request initiated by agent 1:\n\tSV1,027 written with mask 6; SV1,027 current value is 5\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x45, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x44});
        assertEquals(" SV test 32", "(SV Format 2) Reply from destination address 513 to Write four request initiated by agent 1:\n\tSVs 1,027 thru 1,030(?) current values are 5, 6, 7, and 8\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x46, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x47});
        assertEquals(" SV test 33", "(SV Format 2) Reply from destination address 513 to Read four request initiated by agent 1:\n\tSVs 1,027 thru 1,030(?) current values are 5, 6, 7, and 8\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x47, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x46});
        assertEquals(" SV test 34", "(SV Format 2) Reply from destination address 513 to Discover devices request initiated by agent 1:\n\tDevice characteristics are manufacturer 3, developer number 4, product 1,541, serial number 2,055\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x48, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x49});
        assertEquals(" SV test 35", "(SV Format 2) Reply from destination address 513 to Identify device request initiated by agent 1:\n\tDevice characteristics are manufacturer 3, developer number 4, product 1,541, serial number 2,055\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x49, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x48});
        assertEquals(" SV test 36", "(SV Format 2) Reply to Change address request initiated by agent 1:\n\tDevice with manufacturer 3, developer number 4, product 1,541, and serial number 2,055 is now using destination address 513\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x4A, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x4B});
        assertEquals(" SV test 37", "Peer to Peer transfer: SRC=0x1, DSTL=0x4a, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x4F, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x4E});
        assertEquals(" SV test 38", "(SV Format 2) Reply from destination address 513 to Reconfigure request initiated by agent 1:\n\tDevice characteristics are manufacturer 3, developer number 4, product 1,541, serial number 2,055\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x00, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x01});
        assertEquals(" SV test 39", "Peer to Peer transfer: SRC=0x1, DSTL=0x0, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x11, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x10});
        assertEquals(" SV test 40", "Peer to Peer transfer: SRC=0x1, DSTL=0x11, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x21, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x20});
        assertEquals(" SV test 41", "Peer to Peer transfer: SRC=0x1, DSTL=0x21, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x31, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x30});
        assertEquals(" SV test 42", "Peer to Peer transfer: SRC=0x1, DSTL=0x31, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x40, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x41});
        assertEquals(" SV test 43", "Peer to Peer transfer: SRC=0x1, DSTL=0x40, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x51, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x50});
        assertEquals(" SV test 44", "Peer to Peer transfer: SRC=0x1, DSTL=0x51, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x61, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x60});
        assertEquals(" SV test 45", "Peer to Peer transfer: SRC=0x1, DSTL=0x61, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x71, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x70});
        assertEquals(" SV test 46", "Peer to Peer transfer: SRC=0x1, DSTL=0x71, DSTH=0x2, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x02, 0x00, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x10});
        assertEquals(" SV test 47", "Peer to Peer transfer: SRC=0x1, DSTL=0x1, DSTH=0x2, PXCT1=0x0, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x00, 0x05, 0x06, 0x07, 0x08, 0x10});
        assertEquals(" SV test 48", "Peer to Peer transfer: SRC=0x1, DSTL=0x1, DSTH=0x2, PXCT1=0x10, PXCT2=0x0\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x01, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x03});
        assertEquals(" SV test 49", "Peer to Peer transfer: SRC=0x1, DSTL=0x1, DSTH=0x1, PXCT1=0x10, PXCT2=0x10\n\tData [0x1 0x2 0x3 0x4,0x5 0x6 0x7 0x8]\n"
                , f.displayMessage(l));

        
        
        
        
        
        
        
        
        
    }
    
    public void testLissy1() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x60, 0x01, 0x42, 0x35, 0x05});
        Llnmon f = new Llnmon();

        assertEquals("Lissy message 1", "Lissy 1 IR Report: Loco 8501 moving south\n", f.displayMessage(l));
    }

    public void testLissy2() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x40, 0x01, 0x42, 0x35, 0x25});
        Llnmon f = new Llnmon();

        assertEquals("Lissy message 2", "Lissy 1 IR Report: Loco 8501 moving north\n", f.displayMessage(l));
    }

    public void testLACK() {
        Llnmon f = new Llnmon();

        LocoNetMessage l = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x23, 0x07});
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
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
