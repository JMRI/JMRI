package jmri.jmrix.tmcc;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;


/**
 * Implements the jmri.Programmer interface via commands for the TMCC
 * control interface.
 *
 * Made from the EasyDCC programmer
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2025
 * with edits/additions by
 * @author Timothy Jump Copyright (C) 2025
 */
public class TmccProgrammer extends AbstractProgrammer {

    public TmccProgrammer(TmccSystemConnectionMemo memo) {
        tc = memo.getTrafficController();
    }

    protected SerialTrafficController tc = null;

    /** 
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();

        ret.add(TmccProgrammerManager.TMCCMODE1_ENGID);
        ret.add(TmccProgrammerManager.TMCCMODE2_ENGID);
        
        ret.add(TmccProgrammerManager.TMCCMODE1_TRKID);
        ret.add(TmccProgrammerManager.TMCCMODE2_TRKID);

        ret.add(TmccProgrammerManager.TMCCMODE1_SWID);
        ret.add(TmccProgrammerManager.TMCCMODE1_ACCID);

        return ret;

    }


    int _cv; // points to "CV" input from Simple Programmer
    int _val; // points to "Value" input from Simple Programmer
    int _func; // points to "SET" command for TMCC1 and TMCC2 loco and track ID numbers


    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);

        _cv = CV;  // Value from Simple Programmer "CV" input
        _val = val; // Value from Simple Programmer "Value" input
        _func = 0x00002B; // SET command for both TMCC1 and TMCC2

        // validate CV == 1 for TMCC loco ID programming
        // validate ID#/address for TMCC is between 1-98
        // format and send the TMCC loco ID write message
        // note: the argument is long containing 3 bytes

        if (CV == 1) {
            
            if (val > 0 && val < 99) {

                if (getMode() == TmccProgrammerManager.TMCCMODE1_ENGID) {
                    SerialMessage m = new SerialMessage();
                    m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                    m.putAsWord((val * 128) + _func); // set the second/third byte (address/SET command for TMCC1)
                    tc.sendSerialMessage(m, null);
                }

                if  (getMode() == TmccProgrammerManager.TMCCMODE2_ENGID) {
                    SerialMessage m = new SerialMessage();
                    m.setOpCode(0xF8); // set the first byte/TMCC2 opcode to 0xF8 for ENGID
                    m.putAsWord(((val * 512) + 256) + _func); // set the second/third byte (address/SET command for TMCC2)
                    tc.sendSerialMessage(m, null);
                }
                
                if (getMode() == TmccProgrammerManager.TMCCMODE1_TRKID) {
                    if (val < 10) {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                        m.putAsWord(((val * 128) + 51200)+ _func); // set the second/third byte (address/SET command for TMCC1)
                        tc.sendSerialMessage(m, null);

                    } else {
                        SerialMessage m = new SerialMessage();
                        m.setOpCode(0x00);
                        m.putAsWord(00000);
                        tc.sendSerialMessage(m, null);
                        log.warn("Address Must be Between 1-9 for TMCC1_TRK");
                    }
                }

                if  (getMode() == TmccProgrammerManager.TMCCMODE2_TRKID) {
                    SerialMessage m = new SerialMessage();
                    m.setOpCode(0xF9); // set the first byte/TMCC2 opcode to 0xF9 for TRKID
                    m.putAsWord(((val * 512) + 256) + _func); // set the second/third byte (address/SET command for TMCC2)
                    tc.sendSerialMessage(m, null);
                }

                if  (getMode() == TmccProgrammerManager.TMCCMODE1_SWID) {
                    SerialMessage m = new SerialMessage();
                    m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                    m.putAsWord(((val * 128) + 16384) + _func); // set the second/third byte (address/SET command for TMCC2)
                    tc.sendSerialMessage(m, null);
                }
                
                if  (getMode() == TmccProgrammerManager.TMCCMODE1_ACCID) {
                    SerialMessage m = new SerialMessage();
                    m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
                    m.putAsWord(((val * 128) + 32768) + _func); // set the second/third byte (address/SET command for TMCC2)
                    tc.sendSerialMessage(m, null);
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
            m.putAsWord(00001);
            tc.sendSerialMessage(m, null);
            log.warn("CV Must Equal 1 for Programming TMCC Loco/Engine, Switch, Accessory ID#s");
        }

        // End the "writing..." process in SimpleProgrammer
        notifyProgListenerEnd(p, _val, jmri.ProgListener.OK);
 
    }


    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, jmri.ProgListener p) throws jmri.ProgrammerException {
    }


    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized protected void timeout() {
    }
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TmccProgrammer.class);

}
