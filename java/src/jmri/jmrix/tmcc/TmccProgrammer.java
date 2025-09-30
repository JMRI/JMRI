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
 */
public class TmccProgrammer extends AbstractProgrammer{

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

        ret.add(TmccProgrammerManager.TMCCMODE1);
        ret.add(TmccProgrammerManager.TMCCMODE2);
        
        return ret;
        
    }


    int _val; // points to "Value" input from Simple Programmer
    int _func; // points to "SET" command for TMCC1 and TMCC2 loco ID numbers


    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {

        _val = val; // Value from Simple Programmer "Value" input
        _func = 0x00002B; // SET command for both TMCC1 and TMCC2


        // format and send the write message
        // the argument is long containing 3 bytes 

        if (getMode() == TmccProgrammerManager.TMCCMODE1 ) {
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xFE); // set the first byte/TMCC1 opcode to 0xFE
            m.putAsWord((val * 128) + _func); // set the second/third byte (address/SET command for TMCC1)
            tc.sendSerialMessage(m, null);
            
        } else {
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xF8); // set the first byte/TMCC2 opcode to 0xF8
            m.putAsWord((val * 512) + _func); // set the second/third byte (address/SET command for TMCC2)
            tc.sendSerialMessage(m, null);
            
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

}
