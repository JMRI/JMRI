/**
 * MrcMonPane.java
 *
 * Description:		Swing action to create and register a
 *       			MonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version		$Revision: 22942 $
 * @author		kcameron Copyright (C) 2011
 * 	copied from SerialMonPane.java
 * @author		Daniel Boudreau Copyright (C) 2012
 *  added human readable format
 */

package jmri.jmrix.mrc.swing.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.mrc.*;
import jmri.jmrix.mrc.swing.*;

import java.util.Date;

import javax.swing.JCheckBox;


public class MrcMonPanel extends jmri.jmrix.AbstractMonPane implements MrcTrafficListener, MrcPanelInterface{

	private static final long serialVersionUID = 6106790197336170372L;

	public MrcMonPanel() {
        super();
    }
    
public String getHelpTarget() { return "package.jmri.jmrix.mrc.swing.monitor.MrcMonPanel"; }//IN18N

    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("MRC_"); //IN18N
    	}
		x.append(": "); //IN18N
    	x.append("Command Monitor"); //IN18N
        return x.toString(); 
    }

    @Override
    public void dispose() {
        if(memo.getMrcTrafficController()!=null){
            memo.getMrcTrafficController().removeTrafficListener(trafficFilter, this);
        }
        // and unwind swing
        super.dispose();
    }
    
    @Override
    public void init() {}
    
    MrcSystemConnectionMemo memo;
    
    @Override
    public void initContext(Object context) {
        if (context instanceof MrcSystemConnectionMemo ) {
            initComponents((MrcSystemConnectionMemo) context);
        }
    }
    
    JCheckBox includePoll = new JCheckBox("Include Poll Messages"); //IN18N
    
    private int trafficFilter = MrcInterface.ALL;
    
    @Override
    public void initComponents(MrcSystemConnectionMemo memo) {
        this.memo = memo;
        add(includePoll);
        // connect to the LnTrafficController
        if(memo.getMrcTrafficController()==null){
            log.error("No traffic controller is available"); //IN18N
            return;
        }
        memo.getMrcTrafficController().addTrafficListener(trafficFilter, this);
    }
    
    MrcMessage previousPollMessage;
    Date previousTimeStamp;
    
    @Override
    public synchronized void notifyXmit(Date timestamp, MrcMessage m) {
    	if(!includePoll.isSelected() && (m.getMessageClass() & MrcInterface.POLL) == MrcInterface.POLL){
            return;
        }
    	
    	logMessage(timestamp, m, "Tx:");
    }
    
    public synchronized void notifyFailedXmit(Date timestamp, MrcMessage m) {
    	
    	logMessage(timestamp, m, "FAILED:");
    }
    
    @Override
    public synchronized void notifyRcv(Date timestamp, MrcMessage m) {
        
        String prefix = "Rx:"; //IN18N
        if(!includePoll.isSelected() && (m.getMessageClass() & MrcInterface.POLL) == MrcInterface.POLL && m.getElement(1)==0x01){
            //Do not show poll messages
            previousPollMessage = m;
            return;
        } else if (previousPollMessage!=null) {
            if((m.getMessageClass() & MrcInterface.POLL) == MrcInterface.POLL){
                previousPollMessage = null;
                return;
            }
            prefix = "Rx: From Cab - " + Integer.toString(previousPollMessage.getElement(0)); //IN18N
            previousPollMessage = null;
        }
    	logMessage(timestamp, m, prefix);
    }
    
    private void logMessage(Date timestamp, MrcMessage m, String src) {  // receive a Mrc message and log it
        String raw = "";
        for (int i=0;i<m.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(m.getElement(i)&0xFF, raw);
        }

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLineWithTime(timestamp, src + " " + m.toString() + "\n", raw );
    }
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.mrc.swing.MrcNamedPaneAction {

		private static final long serialVersionUID = -7644336249246783644L;

		public Default() {
            super("Mrc Command Monitor", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                MrcMonPanel.class.getName(), 
                jmri.InstanceManager.getDefault(MrcSystemConnectionMemo.class)); //IN18N
        }
    }

	static Logger log = LoggerFactory.getLogger(MrcMonPanel.class.getName());

}


/* @(#)MonAction.java */
