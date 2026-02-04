package jmri.jmrix.tmcc;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Provide an Ops Mode Programmer via a wrapper that works with the
 * TMCC control interface
 * <p>
 * Functionally, this just creates packets to send via the Command Station.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2002, 2025
 * with edits/additions by
 * @author Timothy Jump Copyright (C) 2025
 */
public class TmccOpsModeProgrammer extends TmccProgrammer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;

    public TmccOpsModeProgrammer(int pAddress, boolean pLongAddr, TmccSystemConnectionMemo memo) {
        super(memo);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }


    /** 
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();

        ret.add(TmccProgrammerManager.TMCCMODE1_ENGFEATURE);
        ret.add(TmccProgrammerManager.TMCCMODE2_ENGFEATURE);

        return ret;
    }


    int _cv; // points to "CV" input from Simple Programmer
    int _val; // points to "Value" input from Simple Programmer



    /** 
     * {@inheritDoc}
     *
     * Forward a write request to an ops-mode write operation.
     */
    @Override
    public synchronized void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("write CV={} val={}", CV, val);


        _cv = CV; // Value from Simple Programmer "CV" input
        _val = val; // Value from Simple Programmer "Value" input



        // validate CV == 2 for TMCC loco Feature programming
        // validate ID#/address for TMCC is between 1-98
        // validate Feature Type for TMCC
        // format and send the TMCC loco Feature write message
        // note: the argument is long containing 3 bytes 

        if (CV == 2) {
            
            if (mAddress > 0 && mAddress < 99) {

                // TMCC2 Feature Types
                if  (getMode() == TmccProgrammerManager.TMCCMODE2_ENGFEATURE) {

                    if (val == 0) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xF8); // set the first byte/TMCC2 opcode to 0xF8
                        m.putAsWord(((mAddress * 512) + 256) + 16); // set the second/third byte (address/numeric for TMCC2 val = 0)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 1) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xF8); // set the first byte/TMCC2 opcode to 0xF8
                        m.putAsWord(((mAddress * 512) + 256) + 17); // set the second/third byte (address/numeric for TMCC2 val = 1)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 2) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xF8); // set the first byte/TMCC2 opcode to 0xF8
                        m.putAsWord(((mAddress * 512) + 256) + 18); // set the second/third byte (address/numeric for TMCC2 val = 2)
                        tc.sendSerialMessage(m, null);

                    } else {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0x00);
                        m.putAsWord(00004);
                        tc.sendSerialMessage(m, null);
                        log.warn("Value Entered is Not a TMCC2 Feature Type");
                    }

                }


                // TMCC1 Feature Types
                if (getMode() == TmccProgrammerManager.TMCCMODE1_ENGFEATURE) {         

                    if (val == 4) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 20); // set the second/third byte (address/numeric for TMCC1 val = 4)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 5) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 21); // set the second/third byte (address/numeric for TMCC1 val = 5)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 6) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 22); // set the second/third byte (address/numeric for TMCC1 val = 6)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 8) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 24); // set the second/third byte (address/numeric for TMCC1 val = 8)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 34) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 39); // set the second/third byte (address/numeric for TMCC1 val = 34)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 36) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 41); // set the second/third byte (address/numeric for TMCC1 val = 36)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 74) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 43); // set the second/third byte (address/numeric for TMCC1 val = 74)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 75) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 44); // set the second/third byte (address/numeric for TMCC1 val = 75)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 76) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 45); // set the second/third byte (address/numeric for TMCC1 val = 76)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 740) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 59); // set the second/third byte (address/numeric for TMCC1 val = 740)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 750) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 60); // set the second/third byte (address/numeric for TMCC1 val = 750)
                        tc.sendSerialMessage(m, null);

                    } else if (val == 760) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord((mAddress * 128) + 61); // set the second/third byte (address/numeric for TMCC1 val = 760)
                        tc.sendSerialMessage(m, null);

                    } else {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0x00);
                        m.putAsWord(00003);
                        tc.sendSerialMessage(m, null);
                        log.warn("Value Entered is Not a TMCC1 Feature Type");
                    }

                }

            } else {
                SerialMessage m = new SerialMessage();
                m.setOpCode(0x00);
                m.putAsWord(00000);
                tc.sendSerialMessage(m, null);
                log.warn("Address Must be Between 1-98 for TMCC");
            }

        } else {
            SerialMessage m = new SerialMessage();
            m.setOpCode(0x00);
            m.putAsWord(00002);
            tc.sendSerialMessage(m, null);
            log.warn("CV Must Equal 2 for Programming TMCC Feature Type");

        }

        // End the "writing..." process in SimpleProgrammer
        notifyProgListenerEnd(p, _val, jmri.ProgListener.OK);
 
    }


    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("read CV={}", CV);
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        log.debug("confirm CV={}", CV);
        log.error("confirmCV not available in this protocol");
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     *
     * Can this ops-mode programmer read back values? For now, no, but maybe
     * later.
     *
     * @return always false for now
     */
    @Override
    public boolean getCanRead() {
        return false;
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

    // initialize logging
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TmccOpsModeProgrammer.class);


}
