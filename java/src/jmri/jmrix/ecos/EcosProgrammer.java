package jmri.jmrix.ecos;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import jmri.jmrix.ecos.utilities.GetEcosObjectNumber;

/**
 * Implements the jmri.Programmer interface via commands for the ECoS
 * programmer. This provides a service mode programmer.
 *
 * @author Karl Johan Lisby Copyright (C) 2015 and 2018
 */
public class EcosProgrammer extends AbstractProgrammer implements EcosListener {

    public EcosProgrammer(EcosTrafficController etc) {
        tc = etc;
    }

    EcosTrafficController tc;
    int ecosObject = 5;
    String readCommand  = "mode[readdccdirect]";
    String writeCommand = "mode[writedccdirect]";
    
    /** 
     * {@inheritDoc}
     *
     * @return list of programming modes implemented for ECoS
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.DIRECTBYTEMODE);
        return ret;
    }

    // members for handling the programmer interface
    int progState = 0;
    static final int NOTPROGRAMMING = 0; // is notProgramming
    static final int MODESENT = 1;       // waiting reply to command to go into programming mode
    static final int COMMANDSENT = 2;    // read/write command sent, waiting reply
    boolean _progRead = false;
    int _val; // remember the value being read/written for confirmative reply
    int _cv; // remember the cv being read/written

    // programming interface

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("writeCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = false;
        // set commandPending state
        progState = MODESENT;
        _val = val;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // format and send message to go to program mode
        // ECOS is in program mode by default but we need to subscribe to events
        EcosMessage m;
        m = new EcosMessage("request("+ecosObject+",view)");
        tc.sendEcosMessage(m, this);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("readCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = true;
        // set commandPending state
        // set commandPending state
        progState = MODESENT;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // format and send message to go to program mode
        // ECOS is in program mode by default but we need to subscribe to events
        EcosMessage m;
        m = new EcosMessage("request("+ecosObject+",view)");
        tc.sendEcosMessage(m, this);
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
            return;
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void message(EcosMessage m) {
        log.info("message: "+m);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void reply(EcosReply reply) {
        log.info("reply: "+reply);
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) {
                log.debug("reply in NOTPROGRAMMING state");
            }
            return;
        } else if (progState == MODESENT) {
            log.debug("reply in MODESENT state");
            // see if reply is the acknowledge of requesting view of events; if not, wait
            if (reply.match("<REPLY request("+ecosObject+",view)>") == -1) {
                return;
            }
            if (reply.match("<END 0 (OK)>") == -1) {
                return;
            }
            // here ready to send the read/write command
            progState = COMMANDSENT;
            // send the command for reading or writing CV
            try {
                startLongTimer();
                EcosMessage m;
                if (_progRead) {
                    // read was in progress - send read command
                    m = new EcosMessage("set("+ecosObject+","+readCommand+",cv["+_cv+"])");
                } else {
                    // write was in progress - send write command
                    m = new EcosMessage("set("+ecosObject+","+writeCommand+",cv["+_cv+","+_val+"])");
                }
                tc.sendEcosMessage(m, this);
            } catch (Exception e) {
                // program op failed, go straight to end
                log.error("program operation failed, exception " + e);
                progState = NOTPROGRAMMING;
                EcosMessage m;
                m = new EcosMessage("release("+ecosObject+",view)");
                tc.sendEcosMessage(m, this);
                notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
                return;
            }
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in COMMANDSENT state");
            }
            // The real reply comes in an event; if this is not that event, wait
            if (reply.match("<EVENT "+ecosObject+">") == -1) {
                return;
            }
            // operation done, capture result, then leave programming mode
            progState = NOTPROGRAMMING;
            stopTimer();
            EcosMessage m;
            m = new EcosMessage("release("+ecosObject+",view)");
            tc.sendEcosMessage(m, this);
            // check for errors
            if (reply.match("error") >= 0 || reply.match(",ok]") == -1) {
                log.debug("ERROR during programming " + reply);
                // ECOS is not very informative about the precise nature of errors.
                // We might guess that there is no loco present
                notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
                return;
            }
            // Get the CV value from the reply if reading
            if (_progRead) {
                // read was in progress - get return value
                _val = GetEcosObjectNumber.getEcosObjectNumber(reply.toString(),",",",ok]");
                log.debug("read CV "+_cv+" value: "+_val);
            }
            
            // if this was a read, we cached the value earlier.  If its a
            // write, we're to return the original write value
            notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            
        } else {
            log.debug("reply in un-decoded state");
        }
    }

    /** 
     * {@inheritDoc}
     *
     * Internal routine to handle a timeout.
     */
    @Override
    synchronized protected void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) {
                log.debug("timeout!");
            }
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            EcosMessage m;
            m = new EcosMessage("release("+ecosObject+",view)");
            tc.sendEcosMessage(m, this);
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    /**
     * Internal method to notify of the final result.
     */
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value " + value + " status " + status);
        }
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        notifyProgListenerEnd(temp,value,status);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EcosProgrammer.class);

}
