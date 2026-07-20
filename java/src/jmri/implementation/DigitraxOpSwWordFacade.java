package jmri.implementation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammerFacade;
import jmri.util.ThreadingUtil;

/**
 * Programmer facade for single index multi-CV access.
 * <p>
 * Used through the String write/read/confirm interface. Accepts one address
 * format in LOCONETBDOPSWMODE mode only:
 * <ul>
 * <li> 115.25.16<br>
 * The 115 is the device type (115 is a DS64); 25 is the first OpSw for 
 * setting the offset address; 16 is the address to be written.
 * This will write/read 32 bits from the associated Op Switch word.
 * </ul>
 * All others pass through to the next facade or programmer. E.g. 123 will do a
 * write/read/confirm to 123, or some other facade can provide "normal" indexed
 * addressing.
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2013, 2016, 2026
 */
public class DigitraxOpSwWordFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param prog the programmer this facade is attached to
     */
    public DigitraxOpSwWordFacade(Programmer prog) {
        super(prog);
    }

    String _cv;  // the CV being processed
    int _val;    // value to be written or being read
    ProgState state = ProgState.NOTPROGRAMMING;
    
    int accumulator;   // when accumulating a value during read
    String typeValue;  // device type value
    int pageAddress;   // if > 0, this is the OpSw to start writing the page number, 
                          // else this is an unpaged access
    int pageNumber;    // the page number to be written to the address
    
    // members for handling the programmer interface
    class OpSwCommand {
        OpSwCommand(String cv, int value, boolean read) {
            this.cv = cv;
            this.value = value;
            this.read = read;
        }
        String cv;
        int value;
        boolean read;  // write if false
        int bit; // 0-31 number to shift the returned bit
    }
    
    Deque<OpSwCommand> commands = new ArrayDeque<OpSwCommand>();

    private void parseCV(String cv) throws IllegalArgumentException {
        pageAddress = -1;
        
        if (cv.contains(".")) {
            String[] splits = cv.split("\\.");
            if (splits.length == 3) {
                typeValue   = splits[0];
                pageAddress = Integer.parseInt(splits[1]);
                pageNumber  = Integer.parseInt(splits[2]);
            } else {
                _cv = cv;  // this is a pass through operation
            }
        } else {
            _cv = cv;
        }
    }

    // typical typeValue value is "155"
    List<OpSwCommand> addressToCommands(String typeValue, int address) {
        var result = new ArrayList<OpSwCommand>();
        for (int i = 0; i <= 7; i++) {
            int bit = (address>>i)&0x01;
            String cv = typeValue+"."+Integer.toString(i+pageAddress);
            result.add(new OpSwCommand(cv, bit, false));
        }
        return result;
    }

    int offsetConstant = 33-0;  // OpsSwitch number for LSB of data value
                                // could also be pageAddress+8

    OpSwCommand oneBit(String typeValue, int swNum, int data, boolean read) {
        int bitNum = swNum-33; // is this a constant?
        int bit = (data>>bitNum)&0x01;
        String cv = typeValue+"."+Integer.toString(swNum);
        var retval = new OpSwCommand(cv, bit, read);
        retval.bit = bitNum;
        return retval;
    }
    
    // programming interface
    @Override
    synchronized public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("writeCV with {}", p);
        _val = val;
        useProgrammer(p);
        pageAddress= -1;
        parseCV(CV);
        if (pageAddress == -1) { // this is pass through
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else {
            commands = new ArrayDeque<OpSwCommand>();
            
            commands.addAll(addressToCommands(typeValue, pageNumber));
            
            for (int i = 46; i <= 48; i++) commands.add(oneBit(typeValue, i, val, false));
            
            for (int i = 33; i <= 45; i++) commands.add(oneBit(typeValue, i, val, false));
            
            for (int i = 62; i <= 64; i++) commands.add(oneBit(typeValue, i, val, false));

            for (int i = 49; i <= 61; i++) commands.add(oneBit(typeValue, i, val, false));

            state = ProgState.SENDWRITESEQUENCE;
            prog.writeCV(commands.getFirst().cv, commands.getFirst().value, this);
        }
    }

    @Override
    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
       readCV(CV, p, 0);
    }

    int accumulation;  // accumulate the read result
    @Override
    synchronized public void readCV(String CV, jmri.ProgListener p, int val) throws jmri.ProgrammerException {
        log.debug("readCV with {}", p);
        useProgrammer(p);
        pageAddress= -1;
        accumulation = 0;
        parseCV(CV);
        if (pageAddress == -1) {
            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this, val);
        } else {
            commands = new ArrayDeque<OpSwCommand>();
            
            commands.addAll(addressToCommands(typeValue, pageNumber));
            
            for (int i = 46; i <= 48; i++) commands.add(oneBit(typeValue, i, val, true));
            
            for (int i = 33; i <= 45; i++) commands.add(oneBit(typeValue, i, val, true));
            
            for (int i = 62; i <= 64; i++) commands.add(oneBit(typeValue, i, val, true));

            for (int i = 49; i <= 61; i++) commands.add(oneBit(typeValue, i, val, true));

            state = ProgState.SENDREADSEQUENCE;
            prog.writeCV(commands.getFirst().cv, commands.getFirst().value, this); // first address bit written
        }
    }

    @Override
    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        pageAddress= -1;
        parseCV(CV);
        if (pageAddress == -1) {
            state = ProgState.PROGRAMMING;
            prog.confirmCV(_cv, val, this);
        } else {
            commands = new ArrayDeque<OpSwCommand>();
            
            commands.addAll(addressToCommands(typeValue, pageNumber));
            
            for (int i = 46; i <= 48; i++) commands.add(oneBit(typeValue, i, val, true));
            
            for (int i = 33; i <= 45; i++) commands.add(oneBit(typeValue, i, val, true));
            
            for (int i = 62; i <= 64; i++) commands.add(oneBit(typeValue, i, val, true));

            for (int i = 49; i <= 61; i++) commands.add(oneBit(typeValue, i, val, true));

            state = ProgState.SENDWRITESEQUENCE;
            prog.confirmCV(commands.getFirst().cv, commands.getFirst().value, this);
        }
    }
    
    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("useProgrammer for {} with {}", p, _usingProgrammer, new Exception("traceback"));
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {        
            log.debug("programmer already in use by {}", _usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
        }
    }

    enum ProgState {

        PROGRAMMING, // doing last read/write, next reply is end; typically a single operation
        NOTPROGRAMMING,  // idle
        SENDWRITESEQUENCE,
        SENDREADSEQUENCE
    }

    public static int delayInterval = 5; // public static so can be changed in a script
    
    /** {@inheritDoc}
     * Note this assumes that there's only one phase to the operation
     */
    @Override
    synchronized public void programmingOpReply(int value, int status) {
        log.debug("notifyProgListenerEnd value {} status {}", value, status);

        if (_usingProgrammer == null) {
            log.error("No listener to notify, reset and ignore");
            state = ProgState.NOTPROGRAMMING;
            return;
        }
        
        // Complete processing later 
        final int myValue = value;
        final int myStatus = status;
        ThreadingUtil.runOnGUIDelayed(() -> {
            processProgrammingOpReply(myValue, myStatus);
        }, delayInterval);
    }
    
    // After a delay, this processes the reply on the Layout Thread
    protected void processProgrammingOpReply(int value, int status) {
        if (status != OK) {
            log.debug("Reset and pass abort up");
            jmri.ProgListener temp = _usingProgrammer;
            _usingProgrammer = null; // done
            state = ProgState.NOTPROGRAMMING;
            commands.clear();
            temp.programmingOpReply(value, status);
            return;
        }

        switch (state) {
            case PROGRAMMING:
                // the programmingOpReply handler might send an immediate reply, so
                // clear the current listener _first_
                jmri.ProgListener temp = _usingProgrammer;
                _usingProgrammer = null; // done
                state = ProgState.NOTPROGRAMMING;
                commands.clear();  // just in case
                temp.programmingOpReply(value, status);
                break;

            case SENDWRITESEQUENCE:
                // drop last-sent item
                var last = commands.pollFirst();
                // and send next
                var next = commands.peekFirst();
                if (next == null) {
                    // operation is done
                    // the programmingOpReply handler might send an immediate reply, so
                    // clear the current listener _first_
                    temp = _usingProgrammer;
                    _usingProgrammer = null; // done
                    state = ProgState.NOTPROGRAMMING;
                    commands.clear();  // just in case
                    log.debug("_usingProgrammer: {}", _usingProgrammer);
                    temp.programmingOpReply(accumulation, status);
                    return;
                    
                } else {
                    // send that one
                    try {
                        prog.writeCV(next.cv, next.value, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing write OpSw", e);
                    }   
                }
                return;
      
            case SENDREADSEQUENCE:
                // drop last-sent item
                last = commands.pollFirst();
                // if last was a read, accumulate value
                if (last.read) {
                    accumulation = accumulation | ((value&0x01)<<last.bit);
                }
                // and send next
                next = commands.peekFirst();
                if (next == null) {
                    // operation is done
                    // the programmingOpReply handler might send an immediate reply, so
                    // clear the current listener _first_
                    temp = _usingProgrammer;
                    _usingProgrammer = null; // done
                    state = ProgState.NOTPROGRAMMING;
                    commands.clear();  // just in case
                    log.debug("_usingProgrammer: {}", _usingProgrammer);
                    temp.programmingOpReply(accumulation, status);
                    return;
                    
                } else {
                    // send that one
                    try {
                        if (next.read) {
                            prog.readCV(next.cv, this, next.value);
                        } else {
                            prog.writeCV(next.cv, next.value, this);
                        }
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing write OpSw", e);
                    }   
                }
                return;
        
            default:
                log.error("Unexpected state on reply: {}", state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;
                commands.clear();
                return;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitraxOpSwWordFacade.class);

}
