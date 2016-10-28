package jmri.jmrix.loconet;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the LocoNet
 * SlotManager object.
 *
 * @see jmri.Programmer
 * @author	Bob Jacobsen Copyright (C) 2002
 */
public class LnOpsModeProgrammer implements AddressedProgrammer, LocoNetListener {

    SlotManager mSlotMgr;
    LocoNetSystemConnectionMemo memo;
    int mAddress;
    boolean mLongAddr;
    ProgListener p;
    boolean doingWrite;
    
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
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        mSlotMgr.writeCVOpsMode(CV, val, p, mAddress, mLongAddr);
    }

    @Override
    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        mSlotMgr.readCVOpsMode(CV, p, mAddress, mLongAddr);
    }

    @Override
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        this.p = null;
        // Check mode
        if (getMode().equals(LnProgrammerManager.LOCONETSV1MODE)) {
            this.p = p;
            doingWrite = true;
            // SV1 mode
            log.debug("write CV \"{}\" to {} addr:{}", CV, val, mAddress);

            // make message
            int locoIOAddress = mAddress;
            int locoIOSubAddress = ((mAddress+256)/256)&0x7F;
            LocoNetMessage m = jmri.jmrix.loconet.locoio.LocoIO.writeCV(locoIOAddress, locoIOSubAddress, decodeCvNum(CV), val);
            // force version 1 tag
            m.setElement(4, 0x01);

            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);            
        } else if (getMode().equals(LnProgrammerManager.LOCONETSV2MODE)) {
            this.p = p;
            // SV2 mode
            log.debug("write CV \"{}\" to {} addr:{}", CV, val, mAddress);
            // make message
            LocoNetMessage m = new LocoNetMessage(16);
            loadSV2MessageFormat(m, mAddress, decodeCvNum(CV), val);
            m.setElement(3, 0x01); // 1 byte write
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);
        } else {
            // DCC ops mode
            writeCV(Integer.parseInt(CV), val, p);
        }
    }

    @Override
    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        this.p = null;
        // Check mode
        if (getMode().equals(LnProgrammerManager.LOCONETSV1MODE)) {
            this.p = p;
            doingWrite = false;
            // SV1 mode
            log.debug("read CV \"{}\" addr:{}", CV, mAddress);
            
            // make message
            int locoIOAddress = mAddress&0xFF;
            int locoIOSubAddress = ((mAddress+256)/256)&0x7F;
            LocoNetMessage m = jmri.jmrix.loconet.locoio.LocoIO.readCV(locoIOAddress, locoIOSubAddress, decodeCvNum(CV));
            // force version 1 tag
            m.setElement(4, 0x01);
                        
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);            
        } else if (getMode().equals(LnProgrammerManager.LOCONETSV2MODE)) {
            this.p = p;
            // SV2 mode
            log.debug("read CV \"{}\" addr:{}", CV, mAddress, mAddress);
            // make message
            LocoNetMessage m = new LocoNetMessage(16);
            loadSV2MessageFormat(m, mAddress, decodeCvNum(CV), 0);
            m.setElement(3, 0x02); // 1 byte read
            log.debug("  Message {}", m);
            memo.getLnTrafficController().sendLocoNetMessage(m);            
        } else {
            // DCC ops mode
            readCV(Integer.parseInt(CV), p);
        }
    }

    @Override
    @SuppressWarnings("deprecation") // parent Programmer method deprecated, will remove at same time
    public final void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        confirmCV(""+CV, val, p);
    }

    @Override
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        this.p = null;
        // Check mode
        if (getMode().equals(LnProgrammerManager.LOCONETSV2MODE)) {
            // SV2 mode
            log.error("confirm CV \"{}\" addr:{} in SV2 mode not implemented", CV, mAddress);
            p.programmingOpReply(0, ProgListener.UnknownError);
        } else {
            // DCC ops mode
            confirmCV(Integer.parseInt(CV), val, p);
        }
    }

    public void message(LocoNetMessage m) {
        // see if reply to LNSV 1 or LNSV2 request
        if ((m.getElement( 0) & 0xFF) != 0xE5) return;
        if ((m.getElement( 1) & 0xFF) != 0x10) return;

        log.debug("reply {}",m);
        if (getMode().equals(LnProgrammerManager.LOCONETSV1MODE)) {
            if ((m.getElement( 4) & 0xFF) != 0x01) return; // format 1
            if ((m.getElement( 5) & 0x70) != 0x00) return; // 5
        
            // check for src address (?) moved to 0x50
            // this might not be the right way to tell....
            if ((m.getElement(3) & 0x7F) != 0x50) return; 
            
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
            if ((m.getElement(3) != 0x41) && (m.getElement(3) != 0x42)) return; // need a "Write One Reply", or a "Read One Reply"
            if ((m.getElement( 4) & 0xFF) != 0x02) return; // format 2
            if ((m.getElement( 5) & 0x70) != 0x10) return; // need SVX1 high nibble = 1
            if ((m.getElement(10) & 0x70) != 0x10) return; // need SVX2 high nibble = 1

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
    protected ProgrammingMode mode = DefaultProgrammerManager.OPSBYTEMODE;

    @Override
    public final void setMode(ProgrammingMode m) {
        if (getSupportedModes().contains(m)) {
            mode = m;
            notifyPropertyChange("Mode", mode, m);
        } else {
            throw new IllegalArgumentException("Invalid requested mode: " + m);
        }
    }

    public final ProgrammingMode getMode() {
        return mode;
    }

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(DefaultProgrammerManager.OPSBYTEMODE);
        ret.add(LnProgrammerManager.LOCONETSV1MODE);
        ret.add(LnProgrammerManager.LOCONETSV2MODE);
        return ret;
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
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

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

    public String decodeErrorCode(int i) {
        return mSlotMgr.decodeErrorCode(i);
    }

    public boolean getLongAddress() {
        return mLongAddr;
    }

    public int getAddressNumber() {
        return mAddress;
    }

    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LnOpsModeProgrammer.class.getName());

}
