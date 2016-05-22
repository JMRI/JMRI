// LocoMonPane.java

package jmri.jmrix.loconet.locomon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.swing.*;

/**
 * LocoNet Monitor pane displaying (and logging) LocoNet messages
 * @author	   Bob Jacobsen   Copyright (C) 2001, 2008, 2010
 * @version   $Revision$
 */
public class LocoMonPane extends jmri.jmrix.AbstractMonPane implements LocoNetListener, LnTrafficListener, LnPanelInterface {

    public LocoMonPane() {
        super();
    }

    public String getHelpTarget() { return "package.jmri.jmrix.loconet.locomon.LocoMonFrame"; }
    public String getTitle() {
        String uName = "";
        if (memo!=null) {
            uName = memo.getUserName();
            if (!"LocoNet".equals(uName)) {
                uName = uName+": ";
            } else {
                uName = "";
            }
        }
        return uName+LocoNetBundle.bundle().getString("MenuItemLocoNetMonitor");
    }
    
    /** Switch for logging mode.
     * true: enables the classic logging.
     * false: enables the logging with actual send and transmit times and
     * the direction of the message. Sent messages will have the Tx tag while
     * received messages have the Rx tag added, if raw mode is enabled. */
    private boolean useSimpleLogging = false;
    
    /** Filters which type of traffic that shall be logged. 
     *  The default is all traffic, ie. received and transmitted messages.*/
    // could be changed by adding a UI element
    // make call to memo.getLnTrafficController().changeTrafficListener if the filter was changed
    private int trafficFilter = LnTrafficListener.LN_TRAFFIC_ALL;

    public void dispose() {
        if(memo.getLnTrafficController()!=null){
        // disconnect from the LnTrafficController
            memo.getLnTrafficController().removeTrafficListener(LnTrafficListener.LN_TRAFFIC_ALL, this);
            memo.getLnTrafficController().removeLocoNetListener(~0,this);
        }
        // and unwind swing
        super.dispose();
    }

    public void init() {
    	setFixedWidthFont();
    }
    
    LocoNetSystemConnectionMemo memo;
    
    public void initContext(Object context) {
        if (context instanceof LocoNetSystemConnectionMemo ) {
            initComponents((LocoNetSystemConnectionMemo) context);
        }
    }

    public void initComponents(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the LnTrafficController
        if(memo.getLnTrafficController()==null){
            log.error("No traffic controller is available");
            return;
        }
        memo.getLnTrafficController().addTrafficListener(trafficFilter, this);
		memo.getLnTrafficController().addLocoNetListener(~0, this);
        if(memo.provides(jmri.TurnoutManager.class))
            llnmon.setLocoNetTurnoutManager((jmri.TurnoutManager)memo.get(jmri.TurnoutManager.class));
        if(memo.provides(jmri.SensorManager.class))
            llnmon.setLocoNetSensorManager((jmri.SensorManager)memo.get(jmri.SensorManager.class));
        if(memo.provides(jmri.ReporterManager.class))
            llnmon.setLocoNetReporterManager((jmri.ReporterManager)memo.get(jmri.ReporterManager.class));
    }

	@Override
	public synchronized void message(LocoNetMessage l) {  // receive a LocoNet message and log it
		
		if (!useSimpleLogging) return;
        
		// send the raw data, to display if requested
        String raw = l.toString();

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLine(llnmon.displayMessage(l), raw );
	}

    private boolean filterEcho = true;
    private LocoNetMessage lastLoggedTxMessage = null;
    
    @Override
    public synchronized void notifyXmit(Date timestamp, LocoNetMessage m) {
    	
    	if (useSimpleLogging) return;
    	
    	logMessage(timestamp, m, "Tx");
    	lastLoggedTxMessage = m;
    }
    
    @Override
    public synchronized void notifyRcv(Date timestamp, LocoNetMessage m) {
    	
    	if (useSimpleLogging) return;

    	if (filterEcho) {
    		if ((lastLoggedTxMessage != null) && (lastLoggedTxMessage.equals(m))) {     	
    			return;
    		}
    	}
    	logMessage(timestamp, m, "Rx");
    }
    
    private void logMessage(Date timestamp, LocoNetMessage l, String src) {  // receive a LocoNet message and log it
        // send the raw data, to display if requested
        String raw = src + " - " + l.toString();

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLineWithTime(timestamp, llnmon.displayMessage(l), raw );
    }

    jmri.jmrix.loconet.locomon.Llnmon llnmon = new jmri.jmrix.loconet.locomon.Llnmon();

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {
        public Default() {
            super(LocoNetBundle.bundle().getString("MenuItemLocoNetMonitor"), 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                LocoMonPane.class.getName(), 
                jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }
    
    static Logger log = LoggerFactory.getLogger(LocoMonPane.class.getName());
}
