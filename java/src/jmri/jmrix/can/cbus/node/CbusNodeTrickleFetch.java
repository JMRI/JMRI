package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import java.util.TimerTask;
import jmri.util.TimerUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;


// intention is that once instance of this class active at any point, 
// managed via the CbusNodeTableModel

// if the network is quiet, ie no messages in the last XX ms, this
// will trigger a fetch for any unknown node parameters, node variables, or node event variables

public class CbusNodeTrickleFetch implements CanListener {
    
    private CbusNodeTableDataModel nodeModel;
    private TrafficController tc;
    private TimerTask trickleTimer;
    
    // next fetch call is double this as there should be a response from a module
    private long trickleTimeoutValue;
    
    Boolean networkActive;
    
    public CbusNodeTrickleFetch(CanSystemConnectionMemo memo, CbusNodeTableDataModel model, long timeoutValue) {
        
        nodeModel = model;
        trickleTimeoutValue = timeoutValue;
        // connect to the CanInterface
        tc = memo.getTrafficController();
        addTc(tc);
        
        networkActive = false;
        // start timer
        if ( timeoutValue > 0L ) {
            startTrickleTimer();
        } else {
            dispose();
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
     * 
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
        networkActive = true;
    }
    
    /**
     * @param m canmessage
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
        networkActive = true;
    }
    
    public void dispose(){
        stopTrickleTimer();
        tc.removeCanListener(this);
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeTrickleFetch.class);

}
