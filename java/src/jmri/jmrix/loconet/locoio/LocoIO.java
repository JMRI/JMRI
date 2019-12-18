package jmri.jmrix.loconet.locoio;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 * Manage the communication to/from a LocoIO board.
 * 
 * Uses the LOCONETSV1MODE programming mode.
 *
 * Uses LnProgrammer LOCOIO_PEER_CODE_SV_VER1 message format, comparable to DecoderPro LOCONETSV1MODE
 * Currently (4.11.6) this tool does not work with the HDL LocoIO rev 3 and newer boards,
 * with risk of breaking the stored config.
 *
 * @see jmri.jmrix.loconet.LnOpsModeProgrammer#message(LocoNetMessage)
 *
 * Programming SV's
 * <p>
 * The SV's in a LocoIO hardware module can be programmed using LocoNet OPC_PEER_XFER messages.
 * <p>
 * Commands for setting SV's:
 * <p>
 * PC to LocoIO LocoNet message (OPC_PEER_XFER)
 * <pre><code>
 * Code LOCOIO_SV_READ _or_ LOCOIO_SV_WRITE ----
 * 0xE5 OPC_PEER_XFER
 * OPC_PEER_XFER 0x10 Message length
 * SRCL 0x50 0x50 // low address byte of LocoBuffer
 * DSTL LocoIO low address
 * DSTH 0x01 0x01 // Fixed LocoIO high address PXCT1
 * D1 LOCOIO_SV_READ _or_ LOCOIO_SV_WRITE // Read/Write command
 * D2 SV number
 * D3 0x00 0x00
 * D4 0x00 Data to Write PXCT2
 * D5 LocoIO Sub-address
 * D6 0x00 0x00
 * D7 0x00 0x00
 * D8 0x00 0x00
 * CHK Checksum Checksum
 * </code></pre>
 *
 * LocoIO to PC reply message (OPC_PEER_XFER)
 * <pre><code>
 * Code LOCOIO_SV_READ _or_ LOCOIO_SV_WRITE ----
 * 0xE5 OPC_PEER_XFER
 * 0x10 Message length
 * SRCL LocoIO low address
 * DSTL 0x50 0x50 // low address byte of LocoBuffer
 * DSTH 0x01 0x01 // high address byte of LocoBuffer
 * PXCT1 MSB LocoIO version // High order bit of LocoIO version
 * D1 LOCOIO_SV_READ _or_ LOCOIO_SV_WRITE // Original Command
 * D2 SV number requested
 * D3 LSBs LocoIO version // Lower 7 bits of LocoIO version
 * D4 0x00 0x00 PXCT2 MSB Requested Data // High order bit of requested data
 * D5 LocoIO Sub-address
 * D6 Requested Data 0x00
 * D7 Requested Data + 1 0x00
 * D8 Requested Data + 2 Written Data
 * CHK Checksum Checksum
 * </code></pre>
 *
 * @author John Plocher 2006, 2007
 */
public class LocoIO {

    public static final int LOCOIO_SV_WRITE = 0x01;
    public static final int LOCOIO_SV_READ = 0x02;
    public static final int LOCOIO_BROADCAST_ADDRESS = 0x1000; // LocoIO broadcast

    public static final int LOCOIO_PEER_CODE_7BIT_ADDRS = 0x00;
    public static final int LOCOIO_PEER_CODE_ANSI_TEXT = 0x00; // not used
    public static final int LOCOIO_PEER_CODE_SV_VER1 = 0x08;
    public static final int LOCOIO_PEER_CODE_SV_VER2 = 0x09; // not used

    /**
     * Create a new instance of LocoIO.
     */
    public LocoIO() {
    }

    public static int SENSOR_ADR(int a1, int a2) {
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1;
    }

    /**
     * Compose a LocoNet message from the given ingredients for reading
     * the value of one specific SV from a given LocoIO.
     *
     * @param locoIOAddress base address of the LocoIO board to read from
     * @param locoIOSubAddress subAddress of the LocoIO board
     * @param cv the SV index to query
     * @return complete message to send
     */
    public static LocoNetMessage readCV(int locoIOAddress, int locoIOSubAddress, int cv) {
        int[] contents = {LOCOIO_SV_READ, cv, 0, 0, locoIOSubAddress, 0, 0, 0};

        return LocoNetMessage.makePeerXfr(
                0x1050, // B'cast locobuffer address
                locoIOAddress,
                contents, // CV and SubAddr to read
                LOCOIO_PEER_CODE_SV_VER1
        );
    }

    /**
     * Compose a LocoNet message from the given ingredients for reading
     * the value of one specific SV from a given LocoIO.
     *
     * @param locoIOAddress base address of the LocoIO board to read from
     * @param locoIOSubAddress subAddress of the LocoIO board
     * @param cv the SV index to change
     * @param data the new value to store in the board's SV
     * @return complete message to send
     */
    public static LocoNetMessage writeCV(int locoIOAddress, int locoIOSubAddress, int cv, int data) {
        int[] contents = {LOCOIO_SV_WRITE, cv, 0, data, locoIOSubAddress, 0, 0, 0};

        return LocoNetMessage.makePeerXfr(
                0x1050, // B'cast locobuffer address
                locoIOAddress,
                contents, // CV and SubAddr to read
                LOCOIO_PEER_CODE_SV_VER1
        );
    }

    /**
     * Compose and send a message out onto LocoNet changing the LocoIO hardware board
     * address of all connected LocoIO boards.
     * <p>
     * User is warned beforehand that this is a broadcast type operation in
     * {@link jmri.jmrix.loconet.locoio.LocoIOPanel#cautionAddrSet()}
     *
     * @param address the new base address of the LocoIO board to change
     * @param subAddress the new subAddress of the board
     * @param ln the TrafficController to use for sending the message
     */
    public static void programLocoIOAddress(int address, int subAddress, LnTrafficController ln) {
        LocoNetMessage msg;
        msg = LocoIO.writeCV(0x0100, 0, 1, address & 0xFF);
        ln.sendLocoNetMessage(msg);
        if (subAddress != 0) {
            msg = LocoIO.writeCV(0x0100, 0, 2, subAddress);
            ln.sendLocoNetMessage(msg);
        }
    }

    /**
     * Send out a probe of all connected LocoIO units on a given LocoNet connection.
     *
     * @param ln the TrafficController to use for sending the message
     */
    public static void probeLocoIOs(LnTrafficController ln) {
        LocoNetMessage msg;
        msg = LocoIO.readCV(0x0100, 0, 2);
        ln.sendLocoNetMessage(msg);
    }

}
