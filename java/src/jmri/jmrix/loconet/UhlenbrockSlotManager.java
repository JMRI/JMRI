package jmri.jmrix.loconet;

import jmri.CommandStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Only change compared to standard LocoNet SlotManager is CV programming. The
 * Uhlenbrock IB-COM / Intellibox II uses some special and undocumented means
 * (OPC_IMM_PACKET).
 *
 * {@code PC -> IB: BB 7F 00 3B OPC_RQ_SL_DATA, 127(Command Station Options ), 0
 * IB -> PC: B4 3B 00 70 OPC_LONG_ACK, on OPC_RQ_SL_DATA, 0}
 *
 * {@code # start of programming session PC -> IB: E5 07 01 49 42 41 56
 * OPC_PEER_XFER, src=7, dst=9345, ?? PC -> IB: 82 7D OPC_GPOFF}
 *
 * {@code # read cv 1 R CV CV PC -> IB: ED 1F 01 49 42 71 72 01 00 00 70 00 00
 * 00 00 10 OPC_IMM_PACKET 00 00 00 00 00 00 00 00 00 00 00 00 00 00 65 IB ->
 * PC: B4 6D 01 27 OPC_LONG_ACK, on OPC_IMM_PACKET # cv 1 has value 3 VV IB ->
 * PC: E7 0E 7C 00 00 00 72 06 00 00 03 00 00 1D OPC_SL_RD_DATA, len, PT slot,}
 *
 * {@code # end off programming session PC -> IB: E5 07 01 49 42 40 57
 * OPC_PEER_XFER, src=7, dst=}
 *
 * {@code # start of programming session PC -> IB: E5 07 01 49 42 41 56
 * OPC_PEER_XFER, src=7, dst= PC -> IB: 82 7D OPC_GPOFF}
 *
 * {@code # write cv 1 W CV CV VV PC -> IB: ED 1F 01 49 42 71 71 01 00 03 70 00
 * 00 00 00 10 OPC_IMM_PACKET 00 00 00 00 00 00 00 00 00 00 00 00 00 00 65 IB
 * -> PC: B4 6D 01 27 OPC_LONG_ACK, on OPC_IMM_PACKET # cv 1 has value 3 VV IB
 * -> PC: E7 0E 7C 00 00 00 71 06 00 00 03 00 00 1E OPC_SL_RD_DATA, len, PT
 * slot,}
 *
 * {@code # end off programming session PC -> IB: E5 07 01 49 42 40 57
 * OPC_PEER_XFER, src=7, dst=}
 *
 * {@code # write 254 in cv 27 HB W CV CV VV ED 1F 01 49 42 79 71 1B 00 7E 70 00
 * 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A # HB = high bit for
 * CV value}
 *
 * {@code # response HB VV E7 0E 7C 00 00 00 71 06 02 00 7E 00 00 61}
 *
 * {@code # write 255 in cv 545 HB W CV CV VV ED 1F 01 49 42 79 71 21 02 7F 70
 * 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 33}
 *
 * {@code # read cv 393 HB R CV CV VV ED 1F 01 49 42 73 72 09 02 00 70 00 00 00
 * 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6D}
 *
 * @author Lisby Copyright (C) 2014
 */
public class UhlenbrockSlotManager extends SlotManager {

    public UhlenbrockSlotManager(LnTrafficController tc) {
        super(tc);
    }

    /**
     * Provide Uhlenbrock-specific slot implementation
     */
    @Override
    protected void loadSlots() {
        // initialize slot array
        for (int i = 0; i < NUM_SLOTS; i++) {
            _slots[i] = new UhlenbrockSlot(i);
        }
    }

    @Override
    protected boolean checkLackByte1(int Byte1) {
        //    log.info("Uhlenbrock checkLackByte1 "+Byte1);
        if ((Byte1 & 0xED) == 0x6D) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean checkLackTaskAccepted(int Byte2) {
        //    log.info("Uhlenbrock checkLackTaskAccepted "+Byte2);
        if (Byte2 == 1 // task accepted
                || Byte2 == 0x23 || Byte2 == 0x2B || Byte2 == 0x6B)// added as DCS51 fix
        {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean checkLackAcceptedBlind(int Byte2) {
        //    log.info("Uhlenbrock checkLackAcceptedBlind "+Byte2);
        if (Byte2 == 0x40 || Byte2 == 0x7F) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Look for IB-specific messages on the LocoNet, deferring all others to the
     * parent SlotManager implementation.
     *
     * @param m incoming message
     */
    @Override
    public void message(LocoNetMessage m) {

        // see if message for Intellibox-II functions F9 thru F12
        if (m.getOpCode() == LnConstants.RE_OPC_IB2_F9_F12) {
            UhlenbrockSlot slot = (UhlenbrockSlot) slot(m.getElement(1));
            slot.iB2functionMessage(m);
        }

        // see if message for Intellibox_I and -II functions F9 thru F128
        if (m.getOpCode() == LnConstants.RE_OPC_IB2_SPECIAL && m.getElement(1) == LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN) {
            UhlenbrockSlot slot = (UhlenbrockSlot) slot(m.getElement(2));
            slot.iBfunctionMessage(m);
        }

        super.message(m);
    }

    /**
     * Internal method to create the LocoNetMessage for programming on main The
     * table below contains value observed from an Intellibox II when doing
     * programming on main.
     *
     * {@literal
     * Address  CV      Value   Element Decimal Hex     Decimal Hex     Decimal Hex     4       5
     * 60       11      12      13      14      15      16      17      18      19      20      21      22      23      24      25      26      27      28      29      30 1
     * 01       2       02      1       01      ED      1F      1       49      42      71      5E      1       0       2       70      0       1       0       0       10      0       0       0       0       0       0       0       0       0       0
     * 0        0       0       0       4A 1    01      115     73      127     7F      ED      1F      1       49      42      71      5E      1       0       73      70      0       7F      0       0       10      0
     * 0        0       0       0       0       0       0       0       0       0       0       0       0       45 56   38      2       02      1       01      ED      1F      1       49      42      71      5E      38      0       2
     * 70       0       1       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       73 87   57      255     FF      1       01      ED      1F      1       49
     * 42       79      5E      57      0       7F      70      0       1       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       69 87   57      255
     * FF       127     7F      ED      1F      1       49      42      79      5E      57      0       7F      70      0       7F      0       0       10      0       0       0       0       0       0       0       0       0       0
     * 0        0       0       0       17 87   57      255     FF      255     FF      ED      1F      1       49      42      79      5E      57      0       7F      72      0       7F      0       0       10
     * 0        0       0       0       0       0       0       0       0       0       0       0       0       0       15 87   57      256     100     1       01      ED      1F      1       49      42      71      5E      57
     * 0        0       70      1       1       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       1F 87   57      256     100     127     7F      ED
     * 1F       1       49      42      71      5E      57      0       0       70      1       7F      0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       61 87
     * 57       256     100     255     FF      ED      1F      1       49      42      71      5E      57      0       0       72      1       7F      0       0       10      0       0       0       0       0       0       0
     * 0        0       0       0       0       0       0       63 87   57      513     201     1       01      ED      1F      1       49      42      71      5E      57      0       1       70      2       1       0       0
     * 10       0       0       0       0       0       0       0       0       0       0       0       0       0       0       1D 87   57      513     201     127     7F      ED      1F      1       49      42      71
     * 5E       57      0       1       70      2       7F      0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       63 87   57      513     201     255
     * FF       ED      1F      1       49      42      71      5E      57      0       1       72      2       7F      0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0
     * 61 87    57      1024    400     1       01      ED      1F      1       49      42      71      5E      57      0       0       70      4       1       0       0       10      0       0       0       0       0
     * 0        0       0       0       0       0       0       0       0       1A 87   57      1024    400     127     7F      ED      1F      1       49      42      71      5E      57      0       0       70
     * 4        7F      0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       64 87   57      1024    400     255     FF      ED      1F      1
     * 49       42      71      5E      57      0       0       72      4       7F      0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       66 120  78
     * 127      7F      127     7F      ED      1F      1       49      42      71      5E      78      0       7F      70      0       7F      0       0       10      0       0       0       0       0       0       0       0
     * 0        0       0       0       0       0       30 120  78      255     FF      127     7F      ED      1F      1       49      42      79      5E      78      0       7F      70      0       7F      0
     * 0        10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       38 120  78      255     FF      128     80      ED      1F      1       49      42      79
     * 5E       78      0       7F      72      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       45 120  78      255     FF      254
     * FE       ED      1F      1       49      42      79      5E      78      0       7F      72      0       7E      0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0
     * 3B 120   78      255     FF      255     FF      ED      1F      1       49      42      79      5E      78      0       7F      72      0       7F      0       0       10      0       0       0
     * 0        0       0       0       0       0       0       0       0       0       0       3A 127  7F      3       03      1       01      ED      1F      1       49      42      71      5E      7F      0       3       70      0
     * 1        0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       35 127  7F      3       03      127     7F      ED      1F      1       49      42
     * 71       5E      7F      0       3       70      0       7F      0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       4B 127  7F      3       03
     * 128      80      ED      1F      1       49      42      71      5E      7F      0       3       72      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0
     * 0        36 255  FF      3       03      1       01      ED      1F      1       49      42      73      5E      7F      0       3       70      0       1       0       0       10      0       0       0       0       0
     * 0        0       0       0       0       0       0       0       0       37 255     FF      255     FF      127     7F      ED      1F      1       49      42      7B      5E      7F      0       7F      70
     * 0        7F      0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       3D 255  FF      255     FF      128     80      ED      1F      1
     * 49       42      7B      5E      7F      0       7F      72      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       40 256  100
     * 2        02      1       01      ED      1F      1       49      42      71      5E      0       1       2       70      0       1       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0
     * 0        0       4A 256  100     255     FF      255     FF      ED      1F      1       49      42      79      5E      0       1       7F      72      0       7F      0       0       10      0
     * 0        0       0       0       0       0       0       0       0       0       0       0       0       43 1000 3E8       3       03      1       01      ED      1F      1       49      42      73      5E      68      3
     * 3        70      0       1       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       23
     * }
     * <p>
     * Element 0: OPC_IMM_PACKET Element 1: Always 1F Element 2: Always 01
     * Element 3: Always 49 Element 4: Always 42 Element 5: Basic value 71. Bit
     * 1 (value 02) equals bit 7 in address. Bit 3 (value 08) equals bit 7 in CV
     * number. Element 6: 5E=Write on main, 71=Write on PT, 72=Read on PT
     * Element 7: LOPSA, i.e. Bit 0-6 of address Element 8: Bit 8-14 of address.
     * This is not equal to HOPSA, since bit 7 is stored in element 5. Element
     * 9: Bit 0-6 of CV number. Element 10: Basic value 70. Bit 1 (value 02)
     * equals bit 7 in CV value. Element 11: Bit 8-11 of CV number. Element 12:
     * Bit 0-6 in CV value. Element 15: Always 10 Element 30: Checksum
     *
     */
    protected LocoNetMessage progOnMainMessage(int hopsa, int lopsa, int val, int cvnum) {
        LocoNetMessage m = new LocoNetMessage(0x1F);
        m.setOpCode(LnConstants.OPC_IMM_PACKET);
        m.setElement(1, 0x1F);
        m.setElement(2, 0x01);
        m.setElement(3, 0x49);
        m.setElement(4, 0x42);
        m.setElement(5, 0x71 | (hopsa & 0x01) << 1 | (cvnum & 0x80) >> 4);
        m.setElement(6, 0x5E);
        m.setElement(7, lopsa);
        m.setElement(8, hopsa / 2);
        m.setElement(9, cvnum & 0x7F);
        m.setElement(10, 0x70 | ((val & 0x80) >> 6));
        m.setElement(11, cvnum / 256);
        m.setElement(12, val & 0x7F);
        m.setElement(15, 0x10);
        return m;
    }

    /**
     * Internal method to create the LocoNetMessage for programming on
     * programming track. The table below contains value observed from an
     * Intellibox II when doing programming on programming track.
     *
     * {@literal
     * Operation        CV      Value   Byte # Decimal  Hex     Decimal Hex     0       1       2       3       4       5       6       7       8       9       10
     * 11       12      13      14      15      16      17      18      19      20      21      22      23      24      25      26      27      28      29      30 Read 1       01      ED
     * 1F       1       49      42      71      72      1       0       0       70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       65 Write
     * 1        01      3       03      ED      1F      1       49      42      71      71      1       0       3       70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0
     * 0        0       65 Write        27      1B      254     FE      ED      1F      1       49      42      79      71      1B      0       7E      70      0       0       0       0       10      0       0       0
     * 0        0       0       0       0       0       0       0       0       0       0       0A Write        545     221     255     FF      ED      1F      1       49      42      79      71      21      2       7F
     * 70       0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       33 Read 393     189     ED      1F      1       49      42
     * 73       72      9       1       0       70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       6D Write        n
     * 01       ED      1F      1       49      42      73      71      7F      0       1       70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0
     * 1B Write 255     FF      127     7F      ED      1F      1       49      42      73      71      7F      0       7F      70      0       0       0       0       10      0       0       0       0
     * 0        0       0       0       0       0       0       0       0       0       65 Write        255     FF      255     FF      ED      1F      1       49      42      7B      71      7F      0       7F      70
     * 0        0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       6D Write        256     100     1       01      ED      1F      1       49
     * 42       71      71      0       1       1       70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       67 Write        256     100
     * 127      7F      ED      1F      1       49      42      71      71      0       1       7F      70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0
     * 0        19 Write        256     100     255     FF      ED      1F      1       49      42      79      71      0       1       7F      70      0       0       0       0       10      0       0       0
     * 0        0       0       0       0       0       0       0       0       0       0       11 Write        513     201     1       01      ED      1F      1       49      42      71      71      1       2       1       70
     * 0        0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       65 Write        513     201     127     7F      ED      1F      1       49
     * 42       71      71      1       2       7F      70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       1B Write        513
     * 201      255     FF      ED      1F      1       49      42      79      71      1       2       7F      70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0
     * 0        0       0       13 Write        1024    400     1       01      ED      1F      1       49      42      71      71      0       4       1       70      0       0       0       0       10      0       0
     * 0        0       0       0       0       0       0       0       0       0       0       0       62 Write        1024    400     127     7F      ED      1F      1       49      42      71      71      0       4
     * 7F       70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       1C Write        1024    400     255     FF      ED
     * 1F       1       49      42      79      71      0       4       7F      70      0       0       0       0       10      0       0       0       0       0       0       0       0       0       0       0       0       0       0       14
     * }
     * <p>
     * Element 0: OPC_IMM_PACKET Element 1: Always 1F Element 2: Always 01
     * Element 3: Always 49 Element 4: Always 42 Element 5: Basic value 71. Bit
     * 1 (value 02) equals bit 7 in CV number. Bit 3 (value 08) equals bit 7 in
     * CV value. Element 6: 5E=Write on main, 71=Write on PT, 72=Read on PT
     * Element 7: Bit 0-6 of CV number Element 8: Bit 8-11 of CV number. Element
     * 9: Bit 0-6 of CV value. Element 10: Always 70. Element 15: Always 10
     * Element 30: Checksum
     *
     */
    protected LocoNetMessage progOnProgrammingTrackMessage(int element6, int val, int cvnum) {
        //       log.error("About to create read/write CV command for IB-COM.", new Excception());
        LocoNetMessage m = new LocoNetMessage(0x1F);
//log.info("--------SENDING OPC_IMM_PACKET TO IB-COM PROGRAMMING TRACK. pcmd=" + pcmd + "val="+val+" cvnum="+cvnum+" write="+write);
        m.setOpCode(LnConstants.OPC_IMM_PACKET);
        m.setElement(1, 0x1F);
        m.setElement(2, 0x01);
        m.setElement(3, 0x49);
        m.setElement(4, 0x42);
        m.setElement(5, 0x71 | (val & 0x80) >> 4 | (cvnum & 0x80) >> 6);
        m.setElement(6, element6);
        m.setElement(7, cvnum & 0x7F);
        m.setElement(8, cvnum / 256);
        m.setElement(9, val & 0x7F);
        m.setElement(10, 0x70);
        m.setElement(15, 0x10);
        return m;
    }

    /*
     * Internal method to create the LocoNetMessage for programmer task start
     */
    @Override
    protected LocoNetMessage progTaskStart(int pcmd, int val, int cvnum, boolean write) {
        switch (pcmd) {
            case 0x67:
                return progOnMainMessage(hopsa, lopsa, val, cvnum);  // write on main

            case 0x63:
                return progOnProgrammingTrackMessage(0x6F, val, cvnum); // write on PT in PageMode
            case 0x6B:
                return progOnProgrammingTrackMessage(0x71, val, cvnum); // write on PT in DirectByteMode
            case 0x53:
                return progOnProgrammingTrackMessage(0x6D, val, cvnum); // write on PT in RegisterMode or AddressMode

            case 0x23:
                return progOnProgrammingTrackMessage(0x6E, 0, cvnum); // read on PT in PageMode
            case 0x2B:
                return progOnProgrammingTrackMessage(0x72, 0, cvnum); // read on PT in DirectByteMode
            case 0x13:
                return progOnProgrammingTrackMessage(0x6C, 0, cvnum); // read on PT in RegisterMoode or AddressMode
            default:
                log.warn("Unhandled programming type: {}", pcmd);
                break;
        }
        // We are probably being asked to read CV on main track, which is not suppoorted by IB. So get out of programming mode.
        return stopIBComPT();
    }

    /**
     * Internal method to create the LocoNetMessage for enabling programming
     * track in IB-COM / Intellibox II Note: This method is specific to
     * Uhlenbrock
     */
    protected LocoNetMessage startIBComPT() {
        //       log.error("About to initiate programming track for IB-COM.", new Exception());

        LocoNetMessage m = new LocoNetMessage(7);
//log.info("--------startIBComPT");
        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement(1, 0x07);
        m.setElement(2, 0x01);
        m.setElement(3, 0x49);
        m.setElement(4, 0x42);
        m.setElement(5, 0x41);
        return m;
    }

    /**
     * Internal method to create the LocoNetMessage for disabling programming
     * track in IB-COM / Intellibox II Note: This method is currently not used
     */
    protected LocoNetMessage stopIBComPT() {
        //       log.error("About to stop using programming track for IB-COM.", new Exception());

        LocoNetMessage m = new LocoNetMessage(7);

        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement(1, 0x07);
        m.setElement(2, 0x01);
        m.setElement(3, 0x49);
        m.setElement(4, 0x42);
        m.setElement(5, 0x40);
        return m;
    }

    // internal method to remember who's using the programmer
    // Note: Overridden in order to also call the startIBComPT method
    @Override
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        super.useProgrammer(p);
        tc.sendLocoNetMessage(startIBComPT());
    }

    // A couple of seconds after the last programming command, power is meant to be turned on.
    // However, for the Uhlenbrock IB-COM / Intellibox II, the command station is taken out of programming mode instead.
    @Override
    synchronized protected void doEndOfProgramming() {
        log.debug("Uhlenbrock doEndOfProgramming");
        tc.sendLocoNetMessage(stopIBComPT());
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(UhlenbrockSlotManager.class);

}
