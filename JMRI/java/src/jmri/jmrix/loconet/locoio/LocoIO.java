/*
 * LocoIO.java
 *
 * Manage the communication to/from a LocoIO board
 */
package jmri.jmrix.loconet.locoio;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 * Programming SV's
 *
 * The SV's in LocoIO can be programmed using Loconet OPC_PEER_XFER messages.
 * Commands for setting SV's
 *
 * PC to LocoIO loconet message (OPC_PEER_XFER)
 *
 * Code LOCOIO_SV_READ LOCOIO_SV_WRITE ---- -------------- --------------- 0xE5
 * OPC_PEER_XFER OPC_PEER_XFER 0x10 Message length Message length SRCL 0x50 0x50
 * // low address byte of Locobuffer DSTL LocoIO low address LocoIO low address
 * DSTH 0x01 0x01 // Fixed LocoIO high address PXCT1 D1 LOCOIO_SV_READ
 * LOCOIO_SV_WRITE // Read/Write command D2 SV number SV number D3 0x00 0x00 D4
 * 0x00 Data to Write PXCT2 D5 LocoIO Sub-address LocoIO Sub-address D6 0x00
 * 0x00 D7 0x00 0x00 D8 0x00 0x00 CHK Checksum Checksum
 *
 *
 * LocoIO to PC reply message (OPC_PEER_XFER)
 *
 * Code LOCOIO_SV_READ LOCOIO_SV_WRITE ---- -------------- --------------- 0xE5
 * OPC_PEER_XFER OPC_PEER_XFER 0x10 Message length Message length SRCL LocoIO
 * low address LocoIO low address DSTL 0x50 0x50 // low address byte of
 * Locobuffer DSTH 0x01 0x01 // high address byte of Locobuffer PXCT1 MSB LocoIO
 * version MSB LocoIO version // High order bit of LocoIO version D1
 * LOCOIO_SV_READ LOCOIO_SV_WRITE // Original Command D2 SV number requested SV
 * number requested D3 LSBs LocoIO version LSBs LocoIO version // Lower 7 bits
 * of LocoIO version D4 0x00 0x00 PXCT2 MSB Requested Data MSB Requested Data //
 * High order bit of requested data D5 LocoIO Sub-address LocoIO Sub-address D6
 * Requested Data 0x00 D7 Requested Data + 1 0x00 D8 Requested Data + 2 Written
 * Data CHK Checksum Checksum
 *
 *
 * @author plocher
 */
public class LocoIO {

    public static final int LOCOIO_SV_WRITE = 0x01;
    public static final int LOCOIO_SV_READ = 0x02;
    public static final int LOCOIO_BROADCAST_ADDRESS = 0x1000; // LocoIO broadcast

    public static final int LOCOIO_PEER_CODE_7BIT_ADDRS = 0x00;
    public static final int LOCOIO_PEER_CODE_ANSI_TEXT = 0x00;
    public static final int LOCOIO_PEER_CODE_SV_VER1 = 0x08;
    public static final int LOCOIO_PEER_CODE_SV_VER2 = 0x09;

    /**
     * Creates a new instance of LocoIO
     */
    public LocoIO() {
    }

    public static int SENSOR_ADR(int a1, int a2) {
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1;
    }

    public static LocoNetMessage readCV(int locoIOAddress, int locoIOSubAddress, int cv) {
        int[] contents = {LOCOIO_SV_READ, cv, 0, 0, locoIOSubAddress, 0, 0, 0};

        return LocoNetMessage.makePeerXfr(
                0x1050, // B'cast locobuffer address
                locoIOAddress,
                contents, // CV and SubAddr to read
                LOCOIO_PEER_CODE_SV_VER1
        );

    }

    public static LocoNetMessage writeCV(int locoIOAddress, int locoIOSubAddress, int cv, int data) {
        int[] contents = {LOCOIO_SV_WRITE, cv, 0, data, locoIOSubAddress, 0, 0, 0};

        return LocoNetMessage.makePeerXfr(
                0x1050, // B'cast locobuffer address
                locoIOAddress,
                contents, // CV and SubAddr to read
                LOCOIO_PEER_CODE_SV_VER1
        );
    }

    public static void programLocoIOAddress(int address, int subAddress, LnTrafficController ln) {
        LocoNetMessage msg;
        msg = LocoIO.writeCV(0x0100, 0, 1, address & 0xFF);
        ln.sendLocoNetMessage(msg);
        if (subAddress != 0) {
            msg = LocoIO.writeCV(0x0100, 0, 2, subAddress);
            ln.sendLocoNetMessage(msg);
        }
    }

    public static void probeLocoIOs(LnTrafficController ln) {
        LocoNetMessage msg;
        msg = LocoIO.readCV(0x0100, 0, 2);
        ln.sendLocoNetMessage(msg);
    }
}
