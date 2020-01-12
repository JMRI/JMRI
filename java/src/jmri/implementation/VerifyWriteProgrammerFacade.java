package jmri.implementation;

import javax.annotation.Nonnull;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer facade which verifies each write via a read, if possible.
 * <p>
 * If the underlying programmer (1) can read and (2) is not already doing a read verify, 
 * each write operation is followed by a readback.
 * If the value doesn't match, an error is signaled.
 * <p>
 * State Diagram for read and write operations  (click to magnify):
 * <a href="doc-files/VerifyWriteProgrammerFacade-State-Diagram.png"><img src="doc-files/VerifyWriteProgrammerFacade-State-Diagram.png" alt="UML State diagram" height="50%" width="50%"></a>
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
 
 /*
 * @startuml jmri/implementation/doc-files/VerifyWriteProgrammerFacade-State-Diagram.png
 * [*] --> NOTPROGRAMMING 
 * NOTPROGRAMMING --> READING: readCV()\n(read CV)
 * READING --> NOTPROGRAMMING: OK reply received\n(return status and value)
 * NOTPROGRAMMING --> FINISHWRITE: writeCV()\n(write CV)
 * FINISHWRITE --> FINISHREAD: OK reply & getCanRead()\n(read CV)
 * FINISHWRITE --> NOTPROGRAMMING: OK reply received && !getCanRead()\n(return OK status and value)
 * FINISHREAD --> NOTPROGRAMMING: OK reply & value matches\n(return OK status reply and value)
 * FINISHREAD --> NOTPROGRAMMING: OK reply & value not match\n(return error status reply and value)
 * READING --> NOTPROGRAMMING : Error reply received
 * FINISHWRITE --> NOTPROGRAMMING : Error reply received
 * FINISHREAD --> NOTPROGRAMMING : Error reply received
 * @enduml
*/


public class VerifyWriteProgrammerFacade extends AbstractProgrammerFacade implements ProgListener {

    /**
     * @param prog    the programmer to which this facade is attached
     */
    public VerifyWriteProgrammerFacade(Programmer prog) {
        super(prog);
    }

    // members for handling the programmer interface
    int _val;	// remember the value being read/written for confirmative reply
    String _cv;	// remember the cv number being read/written
    
    // programming interface
    @Override
    synchronized public void writeCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        _val = val;
        _cv = CV;
        useProgrammer(p);

        // write value first
        state = ProgState.FINISHWRITE;
        prog.writeCV(CV, val, this);
    }

    @Override
    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        _cv = CV;
        useProgrammer(p);

        state = ProgState.READING;
        prog.readCV(CV, this);
    }

    /**
     * This facade ensures that {@link jmri.Programmer.WriteConfirmMode#ReadAfterWrite}
     * is done, so long as it has permission to read the CV after writing.
     */
    @Override
    @Nonnull
    public WriteConfirmMode getWriteConfirmMode(String addr) {
        if ( prog.getCanRead(addr) ) {
            return WriteConfirmMode.ReadAfterWrite;
        } else {
             return prog.getWriteConfirmMode(addr);
        }
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            log.info("programmer already in use by {}", _usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
        }
    }

    /**
     * State machine for VerifyWriteProgrammerFacade  (click to magnify):
     * <a href="doc-files/VerifyWriteProgrammerFacade-State-Diagram.png"><img src="doc-files/VerifyWriteProgrammerFacade-State-Diagram.png" alt="UML State diagram" height="50%" width="50%"></a>
     */
    enum ProgState {
        /** Waiting for response to read, will end next */
        READING, 
        /** Waiting for response to write, issue verify read next */
        FINISHWRITE,
        /** Waiting for response to verify read, will end next */
        FINISHREAD,
        /** No current operation */
        NOTPROGRAMMING
    }
    ProgState state = ProgState.NOTPROGRAMMING;

    // Get notified of the result from the underlying programmer, and work
    // through the state machine for needed requests
    @Override
    public void programmingOpReply(int value, int status) {
        log.debug("notifyProgListenerEnd value {} status {} ", value, status);

        if (status != OK ) {
            // pass abort up
            jmri.ProgListener temp = _usingProgrammer;
            _usingProgrammer = null; // done
            state = ProgState.NOTPROGRAMMING;
            temp.programmingOpReply(value, status);
            return;
        }
        
        if (_usingProgrammer == null) {
            log.error("No listener to notify, reset and ignore");
            state = ProgState.NOTPROGRAMMING;
            return;
        }

        jmri.ProgListener temp = _usingProgrammer;
        
        switch (state) {
            case FINISHWRITE:
                // write completed, can we do read, and is it not already being done?
                if (prog.getCanRead(_cv) && ! prog.getWriteConfirmMode(_cv).equals(WriteConfirmMode.ReadAfterWrite) ) {
                    state = ProgState.FINISHREAD;
                    try {
                        prog.readCV(_cv, this);
                    } catch (jmri.ProgrammerException e) {
                        // pass abort up
                        _usingProgrammer = null; // done
                        state = ProgState.NOTPROGRAMMING;
                        temp.programmingOpReply(value, ProgListener.ConfirmFailed);
                        return;         
                    }
                    break;
                }
                // can't read or it's already being done
                // deliberately fall through to normal completion
                //$FALL-THROUGH$
            case READING: // done, forward the return code and data
                // the programmingOpReply handler might send an immediate reply, so
                // clear the current listener _first_
                _usingProgrammer = null; // done
                state = ProgState.NOTPROGRAMMING;
                temp.programmingOpReply(value, status);
                break;
                
            case FINISHREAD:
                _usingProgrammer = null; // done
                state = ProgState.NOTPROGRAMMING;
                // check if we got it right
                if (value == _val) {
                    // ok, reply OK
                    temp.programmingOpReply(value, status);
                } else {
                    // error, reply error
                    temp.programmingOpReply(value, ProgListener.ConfirmFailed);
                }
                break;

            default:
                log.error("Unexpected state on reply: " + state);
                // clean up as much as possible
                _usingProgrammer = null;
                state = ProgState.NOTPROGRAMMING;
                break;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(VerifyWriteProgrammerFacade.class);

}
