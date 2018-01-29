package jmri.jmrix.loconet;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the LocoNet
 * SlotManager object.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2002
 * @author B. Milhaupt, Copyright (C) 2018
 */
public class LnOpsModeProgrammer implements AddressedProgrammer, LocoNetListener {

    SlotManager mSlotMgr;
    LocoNetSystemConnectionMemo memo;
    int mAddress;
    boolean mLongAddr;
    ProgListener p;
    boolean doingWrite;
    boolean boardOpSwWriteVal;
    private javax.swing.Timer bdOpSwAccessTimer = null;


    public LnOpsModeProgrammer(SlotManager pSlotMgr,
            LocoNetSystemConnectionMemo memo,
            int pAddress, boolean pLongAddr) {
        mSlotMgr = pSlotMgr;
        this.memo = memo;
        mAddress = pAddress;
        mLongAddr = pLongAddr;
        // register to listen
        memo.getLnTrafficController().addLocoNetListener(~0, this);
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    @Override
    @Deprecated
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMgr.writeCVOpsMode(CV, val, p, mAddress, mLongAddr);
    }

    @Override
    @Deprecated
    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        mSlotMgr.readCVOpsMode(CV, p, mAddress, mLongAddr);
    }

    @Override
    public void writeCV(String CV, int val, ProgListener pL) throws ProgrammerException {
        p = null;
        // Check mode
        LocoNetMessage m;
        if (getMode().equals(LnProgrammerManager.LOCONETCSOPSWMODE)) {
            mSlotMgr.setMode(LnProgrammerManager.LOCONETCSOPSWMODE);
            mSlotMgr.writeCV(CV, val, pL); // deal with this via service-mode programmer
        } else if (getMode().equals(LnProgrammerManager.LOCONETBDOPSWMODE)) {
            /**
             * CV format is e.g. "113.12" where the first part defines the
             * typeword for the specific board type and the second is the specific bit number
             * Known values:
             * <UL>
             * <LI>0x70 112 - PM4
             * <LI>0x71 113 - BDL16
             * <LI>0x72 114 - SE8
             * <LI>0x73 115 - DS64
             * </ul>
             */
            if (bdOpSwAccessTimer == null) {
                initiializeBdOpsAccessTimer();
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

            // save a copy of the written value for use during reply
            boardOpSwWriteVal = ((val & 0x01) == 1);

            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);

            bdOpSwAccessTimer.start();
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
            p = pL;
            // SV2 mode
            log.debug("write CV \"{}\" to {} addr:{}", CV, val, mAddress);
            // make message
            m = new LocoNetMessage(16);
            loadSV2MessageFormat(m, mAddress, decodeCvNum(CV), val);
            m.setElement(3, 0x01); // 1 byte write
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);
        } else {
            // DCC ops mode
            writeCV(Integer.parseInt(CV), val, pL);
        }
    }

    @Override
    public void readCV(String CV, ProgListener pL) throws ProgrammerException {
        this.p = null;
        // Check mode
        String[] parts;
        LocoNetMessage m;
        if (getMode().equals(LnProgrammerManager.LOCONETCSOPSWMODE)) {
            mSlotMgr.setMode(LnProgrammerManager.LOCONETCSOPSWMODE);
            mSlotMgr.readCV(CV, pL); // deal with this via service-mode programmer
        } else if (getMode().equals(LnProgrammerManager.LOCONETBDOPSWMODE)) {
            /**
             * CV format is e.g. "113.12" where the first part defines the
             * typeword for the specific board type and the second is the specific bit number
             * Known values:
             * <UL>
             * <LI>0x70 112 - PM4
             * <LI>0x71 113 - BDL16
             * <LI>0x72 114 - SE8
             * <LI>0x73 115 - DS64
             * </ul>
             */
            if (bdOpSwAccessTimer == null) {
                initiializeBdOpsAccessTimer();
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
            bdOpSwAccessTimer.start();

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
            p = pL;
            // SV2 mode
            log.debug("read CV \"{}\" addr:{}", CV, mAddress, mAddress);
            // make message
            m = new LocoNetMessage(16);
            loadSV2MessageFormat(m, mAddress, decodeCvNum(CV), 0);
            m.setElement(3, 0x02); // 1 byte read
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);
        } else {
            // DCC ops mode
            readCV(Integer.parseInt(CV), pL);
        }
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation") // parent Programmer method deprecated, will remove at same time
    public final void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        confirmCV(""+CV, val, p);
    }

    @Override
    public void confirmCV(String CV, int val, ProgListener pL) throws ProgrammerException {
        p = null;
        // Check mode
        if (getMode().equals(LnProgrammerManager.LOCONETCSOPSWMODE)) {
            mSlotMgr.setMode(LnProgrammerManager.LOCONETCSOPSWMODE);
            mSlotMgr.readCV(CV, pL); // deal with this via service-mode programmer
        } else if (getMode().equals(LnProgrammerManager.LOCONETBDOPSWMODE)) {
            readCV(CV, pL);
        }
        else if (getMode().equals(LnProgrammerManager.LOCONETSV2MODE)) {
            // SV2 mode
            log.warn("confirm CV \"{}\" addr:{} in SV2 mode not implemented", CV, mAddress);
            pL.programmingOpReply(0, ProgListener.UnknownError);
        } else {
            // DCC ops mode
            mSlotMgr.confirmCVOpsMode(CV, val, pL, mAddress, mLongAddr);
        }
    }

    @Override
    public void message(LocoNetMessage m) {

        log.debug("LocoNet message received: {}",m);
        if (getMode().equals(LnProgrammerManager.LOCONETBDOPSWMODE)) {

            // are we reading? If not, ignore
            if (p == null) {
                log.warn("received board-program reply message with no reply object: {}", m);
                return;
            }

            // check for right type, unit
            if (m.getOpCode() != 0xb4
                    || ((m.getElement(1) != 0x00) && (m.getElement(1) != 0x50))) {
                return;
            }
            // got a message that is LONG_ACK reply to an BdOpsSw access
            bdOpSwAccessTimer.stop();    // kill the timeout timer

            // LACK with 0x00 or 0x50 in byte 1; assume its to us.

            if (doingWrite) {

                int code = ProgListener.OK;
                int val = boardOpSwWriteVal?1:0;

                ProgListener temp = p;
                p = null;
                temp.programmingOpReply(val, code);

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
            temp.programmingOpReply(val, code);


        } else if (getMode().equals(LnProgrammerManager.LOCONETSV1MODE)) {
            // see if reply to LNSV 1 or LNSV2 request
            if (((m.getElement( 0) & 0xFF) != 0xE5) ||
                    ((m.getElement( 1) & 0xFF) != 0x10) ||
                    ((m.getElement( 4) & 0xFF) != 0x01) || // format 1
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
                log.warn("received SV reply message with no reply object: {}", m);
                return;
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
                temp.programmingOpReply(val, code);
            }
        } else if (getMode().equals(LnProgrammerManager.LOCONETSV2MODE)) {
            // see if reply to LNSV 1 or LNSV2 request
            if (((m.getElement( 0) & 0xFF) != 0xE5) ||
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
                return;
            } else {
                log.debug("returning SV programming reply: {}", m);
                int code = ProgListener.OK;
                int val = (m.getElement(11)&0x7F)|(((m.getElement(10)&0x01) != 0x00)? 0x80:0x00);

                ProgListener temp = p;
                p = null;
                temp.programmingOpReply(val, code);
            }
        }
    }

    int decodeCvNum(String CV) {
        try {
            return Integer.valueOf(CV).intValue();
        } catch (java.lang.NumberFormatException e) {
            return 0;
        }
    }

    void loadSV2MessageFormat(LocoNetMessage m, int mAddress, int cvAddr, int data) {
        m.setElement(0, 0xE5);
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

    @Override
    public final void setMode(ProgrammingMode m) {
        if (getSupportedModes().contains(m)) {
            mode = m;
            notifyPropertyChange("Mode", mode, m); // NOI18N
        } else {
            throw new IllegalArgumentException("Invalid requested mode: " + m); // NOI18N
        }
    }

    @Override
    public final ProgrammingMode getMode() {
        return mode;
    }

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>(4);
        ret.add(ProgrammingMode.OPSBYTEMODE);
        ret.add(LnProgrammerManager.LOCONETSV1MODE);
        ret.add(LnProgrammerManager.LOCONETSV2MODE);
        ret.add(LnProgrammerManager.LOCONETBDOPSWMODE);
        ret.add(LnProgrammerManager.LOCONETCSOPSWMODE);
        return ret;
    }

    /**
     * Confirmation mode by programming mode; not that this doesn't
     * yet know whether BDL168 hardware is present to allow DecoderReply
     * to function; that should be a preference eventually.  See also DCS240...
     *
     * @param addr CV address ignored, as there's no variance with this in LocoNet
     * @return Depends on programming mode
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
     * Provide a {@link java.beans.PropertyChangeSupport} helper.
     */
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param listener The PropertyChangeListener to be added
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void notifyPropertyChange(String key, Object oldValue, Object value) {
        propertyChangeSupport.firePropertyChange(key, oldValue, value);
    }

    /**
     * Can this ops-mode programmer read back values? Yes, if transponding
     * hardware is present, but we don't check that here.
     *
     * @return always true
     */
    @Override
    public boolean getCanRead() {
        return true;
    }

    @Override
    public boolean getCanRead(String addr) {
        return getCanRead();
    }

    @Override
    public boolean getCanWrite() {
        return true;
    }

    @Override
    public boolean getCanWrite(String addr) {
        return getCanWrite() && Integer.parseInt(addr) <= 1024;
    }

    @Override
    public String decodeErrorCode(int i) {
        return mSlotMgr.decodeErrorCode(i);
    }

    @Override
    public boolean getLongAddress() {
        return mLongAddr;
    }

    @Override
    public int getAddressNumber() {
        return mAddress;
    }

    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    void initiializeBdOpsAccessTimer() {
        if (bdOpSwAccessTimer == null) {
            bdOpSwAccessTimer = new javax.swing.Timer(1000, (ActionEvent e) -> {
                ProgListener temp = p;
                p = null;
                temp.programmingOpReply(0, ProgListener.FailedTimeout);
            });
        bdOpSwAccessTimer.setInitialDelay(1000);
        bdOpSwAccessTimer.setRepeats(false);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LnOpsModeProgrammer.class);

}
