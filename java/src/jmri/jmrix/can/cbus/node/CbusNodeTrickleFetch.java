package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import java.util.TimerTask;
import jmri.util.TimerUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;


// intention is that once instance of this class active at any point, 
// managed via the CbusNodeTableModel

// if the network is quiet, ie no messages in the last XX ms, this
// will trigger a fetch for any unknown node parameters, node variables, or node event variables

public class CbusNodeTrickleFetch implements CanListener {
    
    private final CbusBasicNodeTableFetch nodeModel;
    private final TrafficController tc;
    private TimerTask trickleTimer;
    private final long trickleTimeoutValue;
    private boolean networkActive;
    
    public CbusNodeTrickleFetch(CanSystemConnectionMemo memo, CbusBasicNodeTableFetch model, long timeoutValue) {
        
        nodeModel = model;
        trickleTimeoutValue = timeoutValue;
        tc = memo.getTrafficController();
        networkActive = false;
        // start timer
        if ( timeoutValue > 0L ) {
            // connect to the CanInterface
            addTc(tc);
            startTrickleTimer();
        }
    }

    /**
     * Set up the Timer, and start it.
     */
    private void startTrickleTimer() {
        if (trickleTimer == null) {
            trickleTimer = new TimerTask(){
                    @Override
                    public void run() {
                        seeIfCanBusy();
                    }
                };
        }
        TimerUtil.schedule(trickleTimer, trickleTimeoutValue, trickleTimeoutValue);
    }
    
    private void stopTrickleTimer(){
        if (trickleTimer != null ) {
            trickleTimer.cancel();
            trickleTimer = null;
        }
    }

    private void seeIfCanBusy() {
        if ( !networkActive ) {   
            // log.debug("Quiet Network");
            nodeModel.sendNextBackgroundFetch();
        } else {
            // log.debug("Busy Network");
            networkActive = false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
        networkActive = true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
        networkActive = true;
    }
    
    public void dispose(){
        stopTrickleTimer();
        removeTc(tc);
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeTrickleFetch.class);

}
