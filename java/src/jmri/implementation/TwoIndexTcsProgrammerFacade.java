package jmri.implementation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer facade for single index multi-CV access.
 * <p>
 * Used through the String write/read/confirm interface. Accepts address
 * formats:
 * <ul>
 * <li> T2CV.11.12 <br>
 * The write operation writes 11 to the first index CV (201), 12 to the 2nd
 * index CV (202), then writes the data to CV 203 (MSB) and 204 (LSB).<br>
 * The read operation is slightly different, writing 111 (100+11) to CV201,
 * then 12 to the 2nd index CV (202), then writes 100 to CV204, then reads the
 * two values from CV203 and CV204.
 * <li> T3CV.11.12.13 <br>
 * The write operation writes 11 to the first index CV (201), the data to the
 * 2nd index CV (202), then writes 12 to CV203 and 13 to CV204.<br>
 * The read operation writes 11 to CV201, then 12 to CV203, then 13 to CV204,
 * then reads from CV202.
 * </ul>
 * All others pass through to the next facade or programmer. E.g. 123 will do a
 * write/read/confirm to 123, or some other facade can provide "normal" indexed
 * addressing.
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2013, 2016
 */
public class TwoIndexTcsProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param prog the programmer this facade is attached to
     */
    public TwoIndexTcsProgrammerFacade(Programmer prog) {
        super(prog);
    }

    // these could be constructor arguments, but until there's another decoder
    // this weird, for simplicity we leave them as constants
    static final String indexPI = "201";
    static final String indexSI = "202";
    static final String valMSB = "203";
    static final String valLSB = "204";
    static final String readStrobe = "204";  // CV that has to be written before read
    static final String format2Flag = "T2CV"; // flag to indicate this type of CV
    static final String format3Flag = "T3CV"; // flag to indicate this type of CV
    static final int readOffset = 100;

    // members for handling the programmer interface
    int _val; // remember the value being read/written for confirmative reply
    String _cv; // remember the cv number being read/written
    int valuePI;   //  value to write to PI or -1
    int valueSI;   //  value to write to SI or -1
    int valueMSB;  //  value to write to MSB or -1
    int valueLSB;  //  value to write to LSB or -1

    private void parseCV(String cv) throws IllegalArgumentException {
        valuePI = -1;
        valueSI = -1;
        if (cv.contains(".")) {
            String[] splits = cv.split("\\.");
            if (splits.length == 3 && splits[0].equals(format2Flag)) {
                valuePI = Integer.parseInt(splits[1]);
                valueSI = Integer.parseInt(splits[2]);
            } else if (splits.length == 4 && splits[0].equals(format3Flag)) {
                valuePI = Integer.parseInt(splits[1]);
                valueMSB = Integer.parseInt(splits[2]);
                valueLSB = Integer.parseInt(splits[3]);
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
        upperByte = 0;
        if (valuePI == -1) { // this is pass through
            state = ProgState.PROGRAMMING;
            prog.writeCV(_cv, val, this);
        } else {
            // write index first
            state = ProgState.DOSIFORWRITE;
            prog.writeCV(indexPI, valuePI, this);
        }
    }

    @Override
    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);
        parseCV(CV);
        upperByte = 0;
        if (valuePI == -1) {
            state = ProgState.PROGRAMMING;
            prog.readCV(_cv, this);
        } else {
            // write index first; 2nd operation depends on type
            if (valueSI == -1) {
                state = ProgState.DOMSBFORREAD;
            } else {
                state = ProgState.DOSIFORREAD;
            }
            prog.writeCV(indexPI, valuePI + readOffset, this);
        }
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by " + _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
        }
    }

    enum ProgState {

        PROGRAMMING, // doing last read/write, next reply is end
        DOSIFORREAD, // reading, write to SI next
        DOSTROBEFORREAD,// reading, write to strobe CV next
        DOMSBFORREAD, // reading, write to MSB next
        DOLSBFORREAD, // reading, write to LSB next
        DOREADFIRST, // reading, get MSB next
        FINISHREAD, // reading, read CV (LSB) next
        DOSIFORWRITE, // writing, write to SI next
        DOWRITEFIRST, // writing, write CV (MSB) next
        FINISHWRITE, // writing, write CV (LSB) next
        NOTPROGRAMMING  // idle, doing nothing, no reply expected
    }

    ProgState state = ProgState.NOTPROGRAMMING;

    int upperByte;

    /** {@inheritDoc}
     * Note this assumes that there's only one phase to the operation
     */
    @Override
    synchronized public void programmingOpReply(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value " + value + " status " + status);
        }

        if (_usingProgrammer == null) {
            log.error("No listener to notify, reset and ignore");
            state = ProgState.NOTPROGRAMMING;
            return;
        }
        
        // Complete processing later so that WOWDecoder will go through a complete power on reset and not brown out between CV read/writes
        int interval = 150;
        ActionListener taskPerformer = new ActionListener() {
            final int myValue = value;
            final int myStatus = status;
            @Override
            public void actionPerformed(ActionEvent evt) {
                processProgrammingOpReply(myValue, myStatus);
            }
        };
        Timer t = new Timer(interval, taskPerformer );
        t.setRepeats(false);
        t.start();
    }
    
    // After a Swing delay, this processes the reply
    protected void processProgrammingOpReply(int value, int status) {
        if (status != OK ) {
            // pass abort up
            log.debug("Reset and pass abort up");
            jmri.ProgListener temp = _usingProgrammer;
            _usingProgrammer = null; // done
            state = ProgState.NOTPROGRAMMING;
            temp.programmingOpReply(value, status);
            return;
        }

        switch (state) {
            case DOSIFORREAD:
                try {
                    state = ProgState.DOSTROBEFORREAD;
                    prog.writeCV(indexSI, valueSI, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing write SI for read", e);
                }
                break;
            case DOSTROBEFORREAD:
                try {
                    state = ProgState.DOREADFIRST;
                    prog.writeCV(readStrobe, readOffset, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing write strobe for read", e);
                }
                break;
            case DOREADFIRST:
                try {
                    state = ProgState.FINISHREAD;
                    prog.readCV(valMSB, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing read first", e);
                }
                break;
            case FINISHREAD:
                try {
                    state = ProgState.PROGRAMMING;
                    if (valuePI != -1 && valueSI == -1) {
                        upperByte = 0;
                        prog.readCV(indexSI, this);
                    } else {
                        upperByte = value;
                        prog.readCV(valLSB, this);
                    }
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing final read", e);
                }
                break;

            case DOMSBFORREAD:
                try {
                    state = ProgState.DOLSBFORREAD;
                    prog.writeCV(valMSB, valueMSB, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing write strobe for read", e);
                }
                break;
            case DOLSBFORREAD:
                try {
                    state = ProgState.FINISHREAD;
                    prog.writeCV(valLSB, valueLSB, this);
                } catch (jmri.ProgrammerException e) {
                    log.error("Exception doing write strobe for read", e);
                }
                break;

            case DOSIFORWRITE:
                if (valueSI != -1) {
                    // writing SI index after PI
                    try {
                        state = ProgState.DOWRITEFIRST;
                        prog.writeCV(indexSI, valueSI, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing write SI for write", e);
                    }
                } else {
                    // writing data after PI
                    try {
                        state = ProgState.DOWRITEFIRST;
                        prog.writeCV(indexSI, _val, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing write SI for write", e);
                    }
                }
                break;
            case DOWRITEFIRST:
                if (valueSI != -1) {
                    // write upper data
                    try {
                        state = ProgState.FINISHWRITE;
                        prog.writeCV(valMSB, _val / 256, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing write MSB for write", e);
                    }
                } else {
                    // write 2nd index
                    try {
                        state = ProgState.FINISHWRITE;
                        prog.writeCV(valMSB, valueMSB, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing write MSB for write", e);
                    }
                }
                break;
            case FINISHWRITE:
                if (valueSI != -1) {
                    try {
                        state = ProgState.PROGRAMMING;
                        prog.writeCV(valLSB, _val & 255, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing final write", e);
                    }
                } else {
                    try {
                        state = ProgState.PROGRAMMING;
                        prog.writeCV(valLSB, valueLSB, this);
                    } catch (jmri.ProgrammerException e) {
                        log.error("Exception doing final write", e);
                    }
                }
                break;

            case PROGRAMMING:
                // the programmingOpReply handler might send an immediate reply, so
                // clear the current listener _first_
                jmri.ProgListener temp = _usingProgrammer;
                _usingProgrammer = null; // done
                state = ProgState.NOTPROGRAMMING;
                temp.programmingOpReply(upperByte * 256 + value, status);
                break;

            default:
                log.error("Unexpected state on reply: " + state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;
                break;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TwoIndexTcsProgrammerFacade.class);

}
