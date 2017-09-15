package jmri.jmrix.mrc;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MrcInterfaceScaffold.java
 *
 * Description:	Test scaffold implementation of MrcInterface
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2006
  *
 * Use an object of this type as a MrcTrafficController in tests
 */
public class MrcInterfaceScaffold extends MrcTrafficController {

    public MrcInterfaceScaffold() {
    }

    // override some MrcTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record Mrc messages sent, provide access for making sure they are OK
     */
    public Vector<MrcMessage> outbound = new Vector<MrcMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendMrcMessage(MrcMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("sendMrcMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
    }

    /**
     * Avoid error message, normal in parent
     */
    protected void connectionWarn() {
    }

    /**
     * Avoid error message, normal in parent
     */
    protected void portWarn(Exception e) {
    }

    public void receiveLoop() {
    }


    /**
     * Is there a backlog of information for the outbound link? This includes
     * both in the program (e.g. the outbound queue) and in the command station
     * interface (e.g. flow control from the port)
     *
     * @return true if busy, false if nothing waiting to send
     */
    @Override
    public boolean isXmtBusy(){
       return false;
    }



    private final static Logger log = LoggerFactory.getLogger(MrcInterfaceScaffold.class);

}



