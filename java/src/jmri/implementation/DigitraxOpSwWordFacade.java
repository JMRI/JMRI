package jmri.implementation;

import java.util.ArrayDeque;
import java.util.Deque;

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
    int typeValue;     // device type value
    int pageAddress;   // if > 0, this is the page address, else this is an unpaged access
    int pageNumber;    // the page number to be written to the address
    
    // members for handling the programmer interface
    class OpSwCommand {
        String cv;
        String value;
        boolean read;  // write if false
    }
    
    Deque<OpSwCommand> commands = new ArrayDeque<OpSwCommand>();

    private void parseCV(String cv) throws IllegalArgumentException {
        pageAddress = -1;
        
        if (cv.contains(".")) {
            String[] splits = cv.split("\\.");
            if (splits.length == 3) {
                typeValue   = Integer.parseInt(splits[0]);
                pageAddress = Integer.parseInt(splits[1]);
                pageNumber  = Integer.parseInt(splits[2]);
            } else {
                _cv = cv;  // this is a pass through operation
            }
        } else {
            _cv = cv;
        }
    }

    // programming interface
    @Override
    synchronized public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        _val = val;
        useProgrammer(p);
        parseCV(CV);
        if (pageAddress == -1) { // this is pass through
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else {
            // write a complete series
            // ???????
        }
    }

    @Override
    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
       readCV(CV, p, 0);
    }

    @Override
    synchronized public void readCV(String CV, jmri.ProgListener p, int startVal) throws jmri.ProgrammerException {
        useProgrammer(p);
        parseCV(CV);
        if (pageAddress == -1) {
            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this, startVal);
        } else {
            // write a complete series
            // ???????
        }
    }

    @Override
    synchronized public void confirmCV(String CV, int startVal, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        parseCV(CV);
        if (pageAddress == -1) {
            state = ProgState.PROGRAMMING;
            prog.confirmCV(_cv, startVal, this);
        } else {
            // write a complete series
            // ???????
        }
    }
    
    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by {}", _usingProgrammer);
            }
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

    public static int delayInterval = 10; // public static so can be changed in a script
    
    /** {@inheritDoc}
     * Note this assumes that there's only one phase to the operation
     */
    @Override
    synchronized public void programmingOpReply(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value {} status {}", value, status);
        }

        if (_usingProgrammer == null) {
            log.error("No listener to notify, reset and ignore");
            state = ProgState.NOTPROGRAMMING;
            return;
        }
        
        // Complete processing later 
        final int myValue = value;
        final int myStatus = status;
        ThreadingUtil.runOnLayoutDelayed(() -> {
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
                temp.programmingOpReply(value, status);
                commands.clear();
                break;

            case SENDWRITESEQUENCE:
            
            
            case SENDREADSEQUENCE:
            
            
            default:
                log.error("Unexpected state on reply: {}", state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;
                commands.clear();
                break;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitraxOpSwWordFacade.class);

}
