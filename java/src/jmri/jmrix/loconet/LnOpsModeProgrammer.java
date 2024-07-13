package jmri.jmrix.loconet;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.loconet.hexfile.HexFileFrame;
import jmri.jmrix.loconet.lnsvf2.LnSv2MessageContents;
import jmri.jmrix.loconet.uhlenbrock.LncvMessageContents;

import static jmri.jmrix.loconet.uhlenbrock.LncvMessageContents.createCvReadRequest;
import static jmri.jmrix.loconet.uhlenbrock.LncvMessageContents.createCvWriteRequest;

/**
 * Provide an Ops Mode Programmer via a wrapper that works with the LocoNet
 * SlotManager object.
 * Specific handling for message formats:
 * <ul>
 * <li>LOCONETOPSBOARD</li>
 * <li>LOCONETSV1MODE</li>
 * <li>LOCONETSV2MODE</li>
 * <li>LOCONETLNCVMODE</li>
 * <li>LOCONETBDOPSWMODE</li>
 * <li>LOCONETBD7OPSWMODE</li>
 * <li>LOCONETCSOPSWMODE</li>
 * </ul>
 * as defined in {@link LnProgrammerManager}
 *
 * Note that running a simulated LocoNet connection, {@link HexFileFrame#configure()} will substitute the
 * {@link jmri.progdebugger.ProgDebugger} for the {@link jmri.jmrix.loconet.LnOpsModeProgrammer},
 * overriding {@link #readCV(String, ProgListener)} and {@link #writeCV(String, int, ProgListener)}.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2002
 * @author B. Milhaupt, Copyright (C) 2018
 * @author Egbert Broerse, Copyright (C) 2020
 */
public class LnOpsModeProgrammer extends PropertyChangeSupport implements AddressedProgrammer, LocoNetListener {

    LocoNetSystemConnectionMemo memo;
    int mAddress;
    boolean mLongAddr;
    ProgListener p;
    boolean doingWrite;
    boolean boardOpSwWriteVal;
    private int artNum;
    private javax.swing.Timer bdOpSwAccessTimer = null;
    private javax.swing.Timer sv2AccessTimer = null;
    private javax.swing.Timer lncvAccessTimer = null;
    private boolean firstReply;

    private boolean csIsPresent;

    public LnOpsModeProgrammer(LocoNetSystemConnectionMemo memo,
            int pAddress, boolean pLongAddr) {
        this.memo = memo;
        mAddress = pAddress;
        mLongAddr = pLongAddr;
        // register to listen
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        expectCsB4();
    }
    
    private void expectCsB4() {
        csIsPresent = true; // assumption...

        if (memo.getSlotManager().getCommandStationType() ==
                LnCommandStationType.COMMAND_STATION_STANDALONE) {
            csIsPresent = false;
            return;
        }
        ConnectionConfig connection[] = {null, null, null, null};
        int i = 0;
        for (ConnectionConfig conn : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!conn.getDisabled()) {
                connection[i] = conn;
            }
            break;
        }
        
        if ((csIsPresent == true) && (connection[0] != null)) {
            if (connection[0].name().equalsIgnoreCase("LocoNet Simulator")) {
                csIsPresent = false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeCV(String CV, int val, ProgListener pL) throws ProgrammerException {
        if (p != null) {
            log.error("Will try to null an existing programmer!");
        }
        p = null;
        // Check mode
        LocoNetMessage m;
        if (getMode().equals(LnProgrammerManager.LOCONETCSOPSWMODE)) {
            memo.getSlotManager().setMode(LnProgrammerManager.LOCONETCSOPSWMODE);
            memo.getSlotManager().writeCV(CV, val, pL); // deal with this via service-mode programmer
        } else if (getMode().equals(LnProgrammerManager.LOCONETBDOPSWMODE)) {
            /*
             * CV format is e.g. "113.12" where the first part defines the
             * typeword for the specific board type and the second is the specific bit number
             * Known values:
             * <ul>
             * <li>0x70 112 - PM4
             * <li>0x71 113 - BDL16
             * <li>0x72 114 - SE8
             * <li>0x73 115 - DS64
             * </ul>
             */
            if (bdOpSwAccessTimer == null) {
                initializeBdOpsAccessTimer();
            }
            p = pL;
            doingWrite = true;
            // Board programming mode
            log.debug("write CV \"{}\" to {} addr:{}", CV, val, mAddress);
            String[] parts = CV.split("\\.");
            int typeWord = Integer.parseInt(parts[0]);
            int state = Integer.parseInt(parts[parts.length>1 ? 1 : 0]);

            // make message
            m = new LocoNetMessage(6);
            m.setOpCode(LnConstants.OPC_MULTI_SENSE);
            int element = 0x72;
            if ((mAddress & 0x80) != 0) {
                element |= 1;
            }
            m.setElement(1, element);
            m.setElement(2, (mAddress-1) & 0x7F);
            m.setElement(3, typeWord);
            int loc = (state - 1) / 8;
            int bit = (state - 1) - loc * 8;
            m.setElement(4, loc * 16 + bit * 2  + (val&0x01));

            // save a copy of the written value low bit for use during reply
            boardOpSwWriteVal = ((val & 0x01) == 1);

            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);
            bdOpSwAccessTimer.restart();

        } else if (getMode().equals(LnProgrammerManager.LOCONETBD7OPSWMODE)) {
            /*
             * Normal CV format for Digitrax 7th-gen Accy devices
             */
            if (bdOpSwAccessTimer == null) {
                initializeBdOpsAccessTimer();
            }
            p = pL;
            doingWrite = true;
            // Board programming mode
            log.debug("write CV \"{}\" to {} addr:{}", CV, val, mAddress);

            // get prefix if any
            String[] parts = CV.split("\\.");
            int offset = 0;
            int cv = 0;
            switch (parts.length) {
                case 1: // plain CV number
                    cv = Integer.parseInt(parts[0])-1;
                    break;
                case 2:  //  offset.CV format
                    offset = Integer.parseInt(parts[0]);
                    cv = Integer.parseInt(parts[1])-1;
                    break;
                default:
                    log.error("unexpected number of parts in CV {}", CV);
            }

            int address6th = ((mAddress-1+offset) >> 2) & 0x3F;
            int upper3 = ~(((mAddress-1+offset) >> 8) & 0x07);
            int lower2 = (mAddress-1+offset) & 0x03;
            int address7th = ((upper3 << 4) & 0x70) | (lower2 << 1) | 0x08;

            // make message - send immediate packet with custom content
            m = new LocoNetMessage(11);
            m.setOpCode(0xED);
            m.setElement(1, 0x0B);
            m.setElement(2, 0x7F);
            m.setElement(3, 0x54);
            m.setElement(4, 0x07 | (((val >> 7) & 0x01)<<4));
            m.setElement(5, address6th);
            m.setElement(6, address7th);
            m.setElement(7, 0x6C | ((cv >> 7) & 0x03));
            m.setElement(8, cv&0x7F);  // CV number
            m.setElement(9, val&0x7F); // Data

            // save a copy of the written value low bit for use during reply
            boardOpSwWriteVal = ((val & 0x01) == 1);

            log.debug("  Message {}", m);
            firstReply = true;
            memo.getLnTrafficController().sendLocoNetMessage(m);
            bdOpSwAccessTimer.restart();

        } else if (getMode().equals(LnProgrammerManager.LOCONETSV1MODE)) {
            p = pL;
            doingWrite = true;
            // SV1 mode
            log.debug("write CV \"{}\" to {} addr:{}", CV, val, mAddress);

            // make message
            int locoIOAddress = mAddress;
            int locoIOSubAddress = ((mAddress+256)/256)&0x7F;
            m = jmri.jmrix.loconet.locoio.LocoIO.writeCV(locoIOAddress, locoIOSubAddress, decodeCvNum(CV), val);
            // force version 1 tag
            m.setElement(4, 0x01);
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);

        } else if (getMode().equals(LnProgrammerManager.LOCONETSV2MODE)) {
            if (sv2AccessTimer == null) {
                initializeSV2AccessTimer();
            }
            p = pL;
            // SV2 mode
            log.debug("write CV \"{}\" to {} addr:{}", CV, val, mAddress);
            // make message
            m = new LocoNetMessage(16);
            loadSV2MessageFormat(m, mAddress, decodeCvNum(CV), val);
            m.setElement(3, 0x01); // 1 byte write
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);
            sv2AccessTimer.restart();
        } else if (getMode().equals(LnProgrammerManager.LOCONETLNCVMODE)) {
            if (lncvAccessTimer == null) {
                initializeLncvAccessTimer();
            }
            /*
             * CV format is e.g. "5033.12" where the first part defines the
             * article number (type/module class) for the board and the second is the specific bit number.
             * Modules without their own art. no. use 65535 (broadcast mode).
             */
            // LNCV Module programming mode
            String[] parts = CV.split("\\.");
            if (parts.length > 1) {
                artNum = Integer.parseInt(parts[0]); // stored for comparison
            }
            int cvNum = Integer.parseInt(parts[parts.length > 1 ? 1 : 0]);
            p = pL;
            doingWrite = true;
            // LNCV mode
            log.debug("write CV \"{}\" to {} addr:{} (art. {})", cvNum, val, mAddress, artNum);
            // make message
            m = createCvWriteRequest(artNum, cvNum, val);
            // module must be in Programming mode (handled by LNCV tool), note that mAddress is not included in LNCV Write message
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);
            lncvAccessTimer.restart();
        } else {
            // LOCONETOPSBOARD decoder i.e. getMode().equals(LnProgrammerManager.LOCONETOPSBOARD)
            // and the remaining case of DCC ops mode
            memo.getSlotManager().setAcceptAnyLACK();
            memo.getSlotManager().writeCVOpsMode(CV, val, pL, mAddress, mLongAddr);
        }
    }

    /**
     * {@inheritDoc}
     * @param CV the CV to read, could be a composite string that is split in this method te pass eg. the module type
     * @param pL  the listener that will be notified of the read
     */
    @Override
    public void readCV(String CV, ProgListener pL) throws ProgrammerException {
        if (this.p != null) {
            log.error("Will try to null an existing programmer!");
        }
        this.p = null;
        // Check mode
        String[] parts;
        LocoNetMessage m;
        if (getMode().equals(LnProgrammerManager.LOCONETCSOPSWMODE)) {
            memo.getSlotManager().setMode(LnProgrammerManager.LOCONETCSOPSWMODE);
            memo.getSlotManager().readCV(CV, pL); // deal with this via service-mode programmer
        } else if (getMode().equals(LnProgrammerManager.LOCONETBDOPSWMODE)) {
            /*
             * CV format is e.g. "113.12" where the first part defines the
             * typeword for the specific board type and the second is the specific bit number
             * Known values:
             * <ul>
             * <li>0x70 112 - PM4
             * <li>0x71 113 - BDL16
             * <li>0x72 114 - SE8
             * <li>0x73 115 - DS64
             * </ul>
             */
            if (bdOpSwAccessTimer == null) {
                initializeBdOpsAccessTimer();
            }
            p = pL;
            doingWrite = false;
            // Board programming mode
            log.debug("read CV \"{}\" addr:{}", CV, mAddress);
            parts = CV.split("\\.");
            int typeWord = Integer.parseInt(parts[0]);
            int state = Integer.parseInt(parts[parts.length>1 ? 1 : 0]);

            // make message
            m = new LocoNetMessage(6);
            m.setOpCode(LnConstants.OPC_MULTI_SENSE);
            int element = 0x62;
            if ((mAddress & 0x80) != 0) {
                element |= 1;
            }
            m.setElement(1, element);
            m.setElement(2, (mAddress-1) & 0x7F);
            m.setElement(3, typeWord);
            int loc = (state - 1) / 8;
            int bit = (state - 1) - loc * 8;
            m.setElement(4, loc * 16 + bit * 2);

            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);
            bdOpSwAccessTimer.restart();

        } else if (getMode().equals(LnProgrammerManager.LOCONETBD7OPSWMODE)) {
            /*
             * Normal CV format
             */
            if (bdOpSwAccessTimer == null) {
                initializeBdOpsAccessTimer();
            }
            p = pL;
            doingWrite = false;
            // Board programming mode
            log.debug("read CV \"{}\" addr:{}", CV, mAddress);

            // get prefix if any
            parts = CV.split("\\.");
            int offset = 0;
            int cv = 0;
            switch (parts.length) {
                case 1: // plain CV number
                    cv = Integer.parseInt(parts[0])-1;
                    break;
                case 2:  //  offset.CV format
                    offset = Integer.parseInt(parts[0]);
                    cv = Integer.parseInt(parts[1])-1;
                    break;
                default:
                    log.error("unexpected number of parts in CV {}", CV);
            }

            int address6th = ((mAddress-1+offset) >> 2) & 0x3F;
            int upper3 = ~(((mAddress-1+offset) >> 8) & 0x07);
            int lower2 = (mAddress-1+offset) & 0x03;
            int address7th = ((upper3 << 4) & 0x70) | (lower2 << 1) | 0x08;

            // make message - send immediate packet with custom content
            m = new LocoNetMessage(11);
            m.setOpCode(0xED);
            m.setElement(1, 0x0B);
            m.setElement(2, 0x7F);
            m.setElement(3, 0x54);
            m.setElement(4, 0x07);
            m.setElement(5, address6th);
            m.setElement(6, address7th);
            m.setElement(7, 0x64 | ((cv >> 7) & 0x03));
            m.setElement(8, cv&0x7F);  // CV number
            m.setElement(9, 0);

            log.debug("  Message {}", m);
            firstReply = true;
            memo.getLnTrafficController().sendLocoNetMessage(m);
            bdOpSwAccessTimer.restart();
        } else if (getMode().equals(LnProgrammerManager.LOCONETSV1MODE)) {
            p = pL;
            doingWrite = false;
            // SV1 mode
            log.debug("read CV \"{}\" addr:{}", CV, mAddress);
            // make message
            int locoIOAddress = mAddress&0xFF;
            int locoIOSubAddress = ((mAddress+256)/256)&0x7F;
            m = jmri.jmrix.loconet.locoio.LocoIO.readCV(locoIOAddress, locoIOSubAddress, decodeCvNum(CV));
            // force version 1 tag
            m.setElement(4, 0x01);
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);

        } else if (getMode().equals(LnProgrammerManager.LOCONETSV2MODE)) {
            if (sv2AccessTimer == null) {
                initializeSV2AccessTimer();
            }
            p = pL;
            // SV2 mode
            log.debug("read CV \"{}\" addr:{}", CV, mAddress);
            // make message
            m = new LocoNetMessage(16);
            loadSV2MessageFormat(m, mAddress, decodeCvNum(CV), 0);
            m.setElement(3, 0x02); // 1 byte read
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);
            sv2AccessTimer.restart();
        } else if (getMode().equals(LnProgrammerManager.LOCONETLNCVMODE)) {
            if (lncvAccessTimer == null) {
                initializeLncvAccessTimer();
            }
            /*
             * CV format passed by SymbolicProg is formed "5033.12", where the first part defines the
             * article number (type/module class) for the board and the second is the specific bit number.
             * Modules without their own art. no. use 65535 (broadcast mode), so cannot use decoder definition.
             */
            parts = CV.split("\\.");
            if (parts.length > 1) {
                artNum = Integer.parseInt(parts[0]); // stored for comparison
            }
            int cvNum = Integer.parseInt(parts[parts.length > 1 ? 1 : 0]);
            doingWrite = false;
            // numberformat "113.12" is simply consumed by ProgDebugger (HexFile sim connection)
            p = pL;
            // LNCV mode
            log.debug("read LNCV \"{}\" addr:{}", CV, mAddress);
            // make message
            m = createCvReadRequest(artNum, mAddress, cvNum); // module must be in Programming mode (is handled by LNCV tool)
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);
            lncvAccessTimer.restart();
        } else if (getMode().equals(LnProgrammerManager.LOCONETOPSBOARD)) {
            // LOCONETOPSBOARD decoder
            log.trace("LOCONETOPSBOARD start operation");
            memo.getSlotManager().setAcceptAnyLACK();
            memo.getSlotManager().readCVOpsMode(CV, pL, mAddress, mLongAddr);
        } else {
            // DCC ops mode
            memo.getSlotManager().readCVOpsMode(CV, pL, mAddress, mLongAddr);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirmCV(String CV, int val, ProgListener pL) throws ProgrammerException {
        if (this.p != null) {
            log.error("Will try to null an existing programmer!");
        }
        p = null;
        // Check mode
        if (getMode().equals(LnProgrammerManager.LOCONETCSOPSWMODE)) {
            memo.getSlotManager().setMode(LnProgrammerManager.LOCONETCSOPSWMODE);
            memo.getSlotManager().readCV(CV, pL); // deal with this via service-mode programmer
        } else if (getMode().equals(LnProgrammerManager.LOCONETBDOPSWMODE)) {
            readCV(CV, pL);
        } else if (getMode().equals(LnProgrammerManager.LOCONETBD7OPSWMODE)) {
            readCV(CV, pL);
        } else if (getMode().equals(LnProgrammerManager.LOCONETSV2MODE)) {
            // SV2 mode
            log.warn("confirm CV \"{}\" addr:{} in SV2 mode not implemented", CV, mAddress);
            notifyProgListenerEnd(pL, 0, ProgListener.UnknownError);
        } else if (getMode().equals(LnProgrammerManager.LOCONETLNCVMODE)) {
            // LNCV (Uhlenbrock) mode
            log.warn("confirm CV \"{}\" addr:{} in LNCV mode not (yet) implemented", CV, mAddress);
            readCV(CV, pL);
            //notifyProgListenerEnd(pL, 0, ProgListener.UnknownError);
        } else if (getMode().equals(LnProgrammerManager.LOCONETOPSBOARD)) {
            // LOCONETOPSBOARD decoder
            memo.getSlotManager().setAcceptAnyLACK();
            memo.getSlotManager().confirmCVOpsMode(CV, val, pL, mAddress, mLongAddr);
        } else {
            // DCC ops mode
            memo.getSlotManager().confirmCVOpsMode(CV, val, pL, mAddress, mLongAddr);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(LocoNetMessage m) {
        if (getMode().equals(LnProgrammerManager.LOCONETBDOPSWMODE)) {
            // are we programming? If not, ignore
            if (p == null) {
                log.warn("received board-program reply message with no reply object: {}", m);
                return;
            }
            // check for right type, unit
            if (m.getOpCode() != LnConstants.OPC_LONG_ACK
                    || ((m.getElement(1) != 0x00) && (m.getElement(1) != 0x50))) {
                return;
            }
            // got a message that is LONG_ACK reply to an BdOpsSw access
            bdOpSwAccessTimer.stop();    // kill the timeout timer
            // LACK with 0x00 or 0x50 in byte 1; assume it's to us
            if (doingWrite) {
                int code = ProgListener.OK;
                int val = (boardOpSwWriteVal ? 1 : 0);
                ProgListener temp = p;
                p = null;
                notifyProgListenerEnd(temp, val, code);
                return;
            }

            int val = 0;
            if ((m.getElement(2) & 0x20) != 0) {
                val = 1;
            }

            // successful read if LACK return status is not 0x7F
            int code = ProgListener.OK;
            if ((m.getElement(2) == 0x7f)) {
                code = ProgListener.UnknownError;
            }

            ProgListener temp = p;
            p = null;
            notifyProgListenerEnd(temp, val, code);

        } else if (getMode().equals(LnProgrammerManager.LOCONETBD7OPSWMODE)) {
            // Deal with Digitrax 7th-gen Accessory board CV accesses
            // Are we programming? If not, ignore
            if (p == null) {
                log.warn("7th-gen Accessory board Ops programmer received reply message with no reply object: {}", m);
                return;
            }
            // check for right type, unit
            // ignore if not Long_ack
            if ((m.getOpCode() != LnConstants.OPC_LONG_ACK)){
                return;
            }
            if (!((m.getElement(1) == 0x6E) ||
                   ( m.getElement(1) == 0x6D))) {
                // ignore if not Long_ack or either of two appropriate
                // Long-ack response types
                log.debug("Ignoring OPC_LONG_ACK with <LOPC> {}, <ACK1> {}.",
                        Integer.toHexString(m.getElement(1)),
                        Integer.toHexString(m.getElement(2))
                        );
                return;
            }

            // is a OPC_LONG_ACK og 0x6D or 0x6E.

            if (firstReply && csIsPresent) {
                firstReply = false;
                log.debug("Ignoring first OPC_LONG_ACK from 7th gen access. access from cs.");
                return;
            }

            // got a message that is LONG_ACK reply to a 7th-gen BdOpsSw access
            bdOpSwAccessTimer.stop();    // kill the timeout timer
            int code = ProgListener.UnknownError;
            int val = 0;

            // LACK with 0x6E in byte 1; assume it's to us
            if (doingWrite
                    && m.getElement(1) == 0x6D
                    && (m.getElement(2) == 0x55 || m.getElement(2) == 0x5A)) {
                code = ProgListener.OK;
                val = (boardOpSwWriteVal ? 1 : 0);
            } else {
                code = ProgListener.OK;
                val = m.getElement(2) + ((m.getElement(1) & 1) << 7);
            }

            scheduleReplyForLater(val, code);

        } else if (getMode().equals(LnProgrammerManager.LOCONETSV1MODE)) {
            // see if reply to LNSV 1 or LNSV2 request
            if ((m.getOpCode() != LnConstants.OPC_PEER_XFER) ||
                    (m.getElement( 1) != 0x10) ||
                    (m.getElement( 4) != 0x01) || // format 1
                    ((m.getElement( 5) & 0x70) != 0x00)) {
                return;
            }

            // check for src address (?) moved to 0x50
            // this might not be the right way to tell....
            if ((m.getElement(3) & 0x7F) != 0x50) {
                return;
            }

            // more checks needed? E.g. addresses?

            // Mode 1 return data comes back in
            // byte index 12, with the MSB in 0x01 of byte index 10
            //

            // check pending activity
            if (p == null) {
                log.debug("received SV reply message with no reply object: {}", m);
            } else {
                log.debug("returning SV programming reply: {}", m);
                int code = ProgListener.OK;
                int val;
                if (doingWrite) {
                    val = m.getPeerXfrData()[7];
                } else {
                    val = m.getPeerXfrData()[5];
                }
                ProgListener temp = p;
                p = null;
                notifyProgListenerEnd(temp, val, code);
            }
        } else if (getMode().equals(LnProgrammerManager.LOCONETSV2MODE)) {
            // see if reply to LNSV 1 or LNSV2 request
            if (((m.getOpCode() & 0xFF) != LnConstants.OPC_PEER_XFER) ||
                    ((m.getElement( 1) & 0xFF) != 0x10) ||
                    ((m.getElement( 3) != 0x41) && (m.getElement(3) != 0x42)) || // need a "Write One Reply", or a "Read One Reply"
                    ((m.getElement( 4) & 0xFF) != 0x02) || // format 2)
                    ((m.getElement( 5) & 0x70) != 0x10) || // need SVX1 high nibble = 1
                    ((m.getElement(10) & 0x70) != 0x10) // need SVX2 high nibble = 1
                    ) {
                return;
            }
            // more checks needed? E.g. addresses?

            // return reply
            if (p == null) {
                log.error("received SV reply message with no reply object: {}", m);
            } else {
                log.debug("returning SV programming reply: {}", m);

                sv2AccessTimer.stop();    // kill the timeout timer

                int code = ProgListener.OK;
                int val = (m.getElement(11)&0x7F)|(((m.getElement(10)&0x01) != 0x00)? 0x80:0x00);

                ProgListener temp = p;
                p = null;
                notifyProgListenerEnd(temp, val, code);
            }
        } else if (getMode().equals(LnProgrammerManager.LOCONETLNCVMODE)) {
            // see if reply to LNCV request
            // (compare this part to that in LNCV Tool jmri.jmrix.loconet.swing.lncvprog.LncvProgPane.message)
            // is it a LACK write confirmation response from module?
            int code;
            if ((m.getOpCode() == LnConstants.OPC_LONG_ACK) &&
                    (m.getElement(1) == 0x6D) && doingWrite) { // elem 1 = OPC (matches 0xED), elem 2 = ack1
                // convert Uhlenbrock LNCV error codes to ProgListener codes, TODO extend that list to match?
                switch (m.getElement(2)) {
                    case 0x7f:
                        code = ProgListener.OK;
                        break;
                    case 2:
                    case 3:
                        code = ProgListener.NotImplemented;
                        break;
                    case 1:
                    default:
                        code = ProgListener.UnknownError;
                }
                if (lncvAccessTimer != null) {
                    lncvAccessTimer.stop(); // kill the timeout timer
                }
                // LACK with 0x00 or 0x50 in byte 1; assume it's to us.
                ProgListener temp = p;
                p = null;
                notifyProgListenerEnd(temp, 0, code);
            }
            if ((LncvMessageContents.extractMessageType(m) == LncvMessageContents.LncvCommand.LNCV_READ_REPLY) ||
            (LncvMessageContents.extractMessageType(m) == LncvMessageContents.LncvCommand.LNCV_READ_REPLY2)) {
                // it's an LNCV ReadReply message, decode contents
                LncvMessageContents contents = new LncvMessageContents(m);
                int artReturned = contents.getLncvArticleNum();
                int valReturned = contents.getCvValue();
                code = ProgListener.OK;
                // forward write reply
                if (artReturned != artNum) { // it's not for us?
                    //code = ProgListener.ConfirmFailed;
                    log.warn("LNCV read reply received for article {}, expected article {}", artReturned, artNum);
                }
                if (lncvAccessTimer != null) {
                    lncvAccessTimer.stop(); // kill the timeout timer
                }
                ProgListener temp = p;
                p = null;
                notifyProgListenerEnd(temp, valReturned, code);
            }
        }
    }

    private void scheduleReplyForLater(int val, int code) {
        int _val = val;
        int _code = code;

        jmri.util.TimerUtil.scheduleOnLayoutThread(new java.util.TimerTask() {
            @Override
            public void run() {
                log.debug("Passing result from 7g Accy Ops access.");
                ProgListener tempProgListener = p;
                p = null;
                notifyProgListenerEnd(tempProgListener, _val, _code);
            }
        }, 50);
    }

    int decodeCvNum(String CV) {
        try {
            return Integer.parseInt(CV);
        } catch (java.lang.NumberFormatException e) {
            return 0;
        }
    }

    /** Fill in an SV2 format LocoNet message from parameters provided.
     * Compare to SV2 message handler in {@link LnSv2MessageContents#createSv2Message(int, int, int, int, int, int, int, int)}
     *
     * @param m         Base LocoNet message to fill
     * @param mAddress  Destination board address
     * @param cvAddr    Dest. board CV number
     * @param data      Value to put into CV
     */
    void loadSV2MessageFormat(LocoNetMessage m, int mAddress, int cvAddr, int data) {
        m.setElement(0, LnConstants.OPC_PEER_XFER);
        m.setElement(1, 0x10);
        m.setElement(2, 0x01);
        // 3 SV_CMD to be filled in later
        m.setElement(4, 0x02);
        // 5 will come back to SVX1
        m.setElement(6, mAddress&0xFF);
        m.setElement(7, (mAddress>>8)&0xFF);
        m.setElement(8, cvAddr&0xFF);
        m.setElement(9, (cvAddr/256)&0xFF);

        // set SVX1
        int svx1 = 0x10
                    |((m.getElement(6)&0x80) != 0 ? 0x01 : 0)  // DST_L
                    |((m.getElement(7)&0x80) != 0 ? 0x02 : 0)  // DST_L
                    |((m.getElement(8)&0x80) != 0 ? 0x04 : 0)  // DST_L
                    |((m.getElement(9)&0x80) != 0 ? 0x08 : 0); // SV_ADRH
        m.setElement(5, svx1);
        m.setElement(6, m.getElement(6)&0x7F);
        m.setElement(7, m.getElement(7)&0x7F);
        m.setElement(8, m.getElement(8)&0x7F);
        m.setElement(9, m.getElement(9)&0x7F);

        // 10 will come back to SVX2
        m.setElement(11, data&0xFF);
        m.setElement(12, (data>>8)&0xFF);
        m.setElement(13, (data>>16)&0xFF);
        m.setElement(14, (data>>24)&0xFF);

        // set SVX2
        int svx2 = 0x10
                    |((m.getElement(11)&0x80) != 0 ? 0x01 : 0)
                    |((m.getElement(12)&0x80) != 0 ? 0x02 : 0)
                    |((m.getElement(13)&0x80) != 0 ? 0x04 : 0)
                    |((m.getElement(14)&0x80) != 0 ? 0x08 : 0);
        m.setElement(10, svx2);
        m.setElement(11, m.getElement(11)&0x7F);
        m.setElement(12, m.getElement(12)&0x7F);
        m.setElement(13, m.getElement(13)&0x7F);
        m.setElement(14, m.getElement(14)&0x7F);
    }

    // handle mode
    protected ProgrammingMode mode = ProgrammingMode.OPSBYTEMODE;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMode(ProgrammingMode m) {
        if (getSupportedModes().contains(m)) {
            mode = m;
            firePropertyChange("Mode", mode, m); // NOI18N
        } else {
            throw new IllegalArgumentException("Invalid requested mode: " + m); // NOI18N
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ProgrammingMode getMode() {
        return mode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>(4);
        ret.add(ProgrammingMode.OPSBYTEMODE);
        ret.add(LnProgrammerManager.LOCONETBD7OPSWMODE);
        ret.add(LnProgrammerManager.LOCONETOPSBOARD);
        ret.add(LnProgrammerManager.LOCONETSV1MODE);
        ret.add(LnProgrammerManager.LOCONETSV2MODE);
        ret.add(LnProgrammerManager.LOCONETLNCVMODE);
        ret.add(LnProgrammerManager.LOCONETBDOPSWMODE);
        ret.add(LnProgrammerManager.LOCONETCSOPSWMODE);
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * Confirmation mode by programming mode; not that this doesn't
     * yet know whether BDL168 hardware is present to allow DecoderReply
     * to function; that should be a preference eventually. See also DCS240...
     *
     * @param addr CV address ignored, as there's no variance with this in LocoNet
     * @return depends on programming mode
     */
    @Nonnull
    @Override
    public Programmer.WriteConfirmMode getWriteConfirmMode(String addr) {
        if (getMode().equals(ProgrammingMode.OPSBYTEMODE)) {
            return WriteConfirmMode.NotVerified;
        }
        return WriteConfirmMode.DecoderReply;
    }

    /**
     * {@inheritDoc}
     *
     * Can this ops-mode programmer read back values? Yes, if transponding
     * hardware is present and regular ops mode, or if in any other mode.
     *
     * @return always true
     */
    @Override
    public boolean getCanRead() {
        if (getMode().equals(ProgrammingMode.OPSBYTEMODE)) return memo.getSlotManager().getTranspondingAvailable(); // only way can be false
        return true;
     }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCanRead(String addr) {
        return getCanRead();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCanWrite() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCanWrite(String addr) {
        return getCanWrite() && Integer.parseInt(addr) <= 1024;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String decodeErrorCode(int i) {
        return memo.getSlotManager().decodeErrorCode(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLongAddress() {
        return mLongAddr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAddressNumber() {
        return mAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    void initializeBdOpsAccessTimer() {
        if (bdOpSwAccessTimer == null) {
            bdOpSwAccessTimer = new javax.swing.Timer(1000, (ActionEvent e) -> {
                ProgListener temp = p;
                p = null;
                notifyProgListenerEnd(temp, 0, ProgListener.FailedTimeout);
            });
        bdOpSwAccessTimer.setInitialDelay(1000);
        bdOpSwAccessTimer.setRepeats(false);
        }
    }

    void initializeSV2AccessTimer() {
       if (sv2AccessTimer == null) {
            sv2AccessTimer = new javax.swing.Timer(1000, (ActionEvent e) -> {
                ProgListener temp = p;
                p = null;
                notifyProgListenerEnd(temp, 0, ProgListener.FailedTimeout);
            });
        sv2AccessTimer.setInitialDelay(1000);
        sv2AccessTimer.setRepeats(false);
        }
    }

    void initializeLncvAccessTimer() {
        if (lncvAccessTimer == null) {
            lncvAccessTimer = new javax.swing.Timer(1000, (ActionEvent e) -> {
                ProgListener temp = p;
                p = null;
                notifyProgListenerEnd(temp, 0, ProgListener.FailedTimeout);
            });
            lncvAccessTimer.setInitialDelay(1000);
            lncvAccessTimer.setRepeats(false);
        }
    }
    
    @Override
    public void dispose() {
        memo.getLnTrafficController().removeLocoNetListener(~0, this);
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnOpsModeProgrammer.class);

}
