package jmri.implementation;

import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer facade which verifies each write via a read, if possible.
 * <p>
 * If the underlying programmer can read, each write operation is followed by a readback.
 * If the value doesn't match, an error is signaled.
 * <p>
 * State Diagram for read and write operations: <img src="doc-files/VerifyWriteProgrammerFacade-State-Diagram.png" alt="UML State diagram"><p>
 *
 * @see jmri.implementation.ProgrammerFacadeSelector
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
 
 /*
 * @startuml jmri/implementation/doc-files/VerifyWriteProgrammerFacade-State-Diagram.png
 * [*] --> NOTPROGRAMMING : Error reply received
 * [*] --> NOTPROGRAMMING 
 * NOTPROGRAMMING --> READING: readCV() (read CV)
 * READING --> NOTPROGRAMMING: OK reply received (return status and value)
 * NOTPROGRAMMING --> FINISHWRITE: writeCV() (write CV)
 * FINISHWRITE --> FINISHREAD: OK reply & getCanRead() (read CV)
 * FINISHWRITE --> NOTPROGRAMMING: OK reply received (&& !getCanRead() return OK status and value)
 * FINISHREAD --> NOTPROGRAMMING: OK reply & value matches (return OK status reply and value)
 * FINISHREAD --> NOTPROGRAMMING: OK reply & value not match (return error status reply and value)
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
    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    @Override
    synchronized public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        useProgrammer(p);

        state = ProgState.READING;
        prog.readCV(_cv, this);
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

    enum ProgState {
        READING, 
        FINISHWRITE, // doing write, issue verify read next
        FINISHREAD,  // doing verify read, will end next
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
                // write completed, can we do read, and do we have permission?
                if (prog.getCanRead(_cv)) {
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
                // can't read
                // deliberately fall through to normal completion
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

    private final static Logger log = LoggerFactory.getLogger(VerifyWriteProgrammerFacade.class.getName());

}
