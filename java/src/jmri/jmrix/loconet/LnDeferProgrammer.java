package jmri.jmrix.loconet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Programmer implementation for Programmer that uses a SlotManager (which is also an AbstractProgrammer)
 * that might be provided later. This is done by connecting through a LocoNetSystemConnectionMemo.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class LnDeferProgrammer implements Programmer {

    public LnDeferProgrammer(@Nonnull LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
    }
    
    LocoNetSystemConnectionMemo memo;
    
    
    /** {@inheritDoc} */
    @Override
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            m.writeCV(CV, val, p);
        } else {
            log.warn("writeCV called without a SlotManager");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            m.readCV(CV, p);
        } else {
            log.warn("readCV called without a SlotManager");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            m.confirmCV(CV, val, p);
        } else {
            log.warn("confirmCV called without a SlotManager");
        }
    }

    @Override
    @Nonnull public List<ProgrammingMode> getSupportedModes() {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            return m.getSupportedModes();
        } else {
            log.warn("getSupportedModes() called without a SlotManager");
            return new ArrayList<ProgrammingMode>(); // empty
        }
    }

    @Override
    public void setMode(ProgrammingMode p) {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            m.setMode(p);
        } else {
            log.warn("setMode() called without a SlotManager");
        }
    }

    @Override
    public ProgrammingMode getMode() {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            return m.getMode();
        } else {
            log.warn("getMode() called without a SlotManager");
            return ProgrammingMode.ADDRESSMODE; // being cautious
        }
    }

    @Override
    public boolean getCanRead() {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            return m.getCanRead();
        } else {
            log.warn("getCanRead() called without a SlotManager");
            return true; // being cautious
        }
    }
        
    @Override
    public boolean getCanRead(String addr) {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            return m.getCanRead(addr);
        } else {
            log.warn("getCanRead(String) called without a SlotManager");
            return true; // being cautious
        }
    }
        
    @Override
    public boolean getCanWrite() {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            return m.getCanWrite();
        } else {
            log.warn("getCanWrite() called without a SlotManager");
            return true; // being cautious
        }
    }
        
    @Override
    public boolean getCanWrite(String addr) {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            return m.getCanWrite(addr);
        } else {
            log.warn("getCanWrite(String) called without a SlotManager");
            return true; // being cautious
        }
    }
 
    @Override
    @Nonnull
    public Programmer.WriteConfirmMode getWriteConfirmMode(String addr) {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            return m.getWriteConfirmMode(addr);
        } else {
            log.warn("getWriteConfirmMode() called without a SlotManager");
            return Programmer.WriteConfirmMode.DecoderReply; // being cautious
        }
    }
        

    @Override
    public void notifyProgListenerEnd(ProgListener p, int value, int status) {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            m.notifyProgListenerEnd(p, value, status);
        } else {
            log.warn("notifyProgListenerEnd called without a SlotManager");
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener p) {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            m.addPropertyChangeListener(p);
        } else {
            log.warn("addPropertyChangeListener called without a SlotManager");
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener p) {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            m.removePropertyChangeListener(p);
        } else {
            log.warn("removePropertyChangeListener called without a SlotManager");
        }
    }

    @Override
    public String decodeErrorCode(int i) {
        SlotManager m = memo.getSlotManager();
        if (m!=null) {
            return m.decodeErrorCode(i);
        } else {
            log.warn("decodeErrorCode called without a SlotManager");
            return "<unknown>"; // being cautious
        }
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SlotManager.class);

}
