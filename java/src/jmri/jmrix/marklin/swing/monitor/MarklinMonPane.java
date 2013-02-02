/**
 * MarklinMonPane.java
 *
 * Description:		Swing action to create and register a
 *       			MonFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version
 */

package jmri.jmrix.marklin.swing.monitor;

import org.apache.log4j.Logger;
import jmri.jmrix.marklin.*;
import jmri.jmrix.marklin.swing.*;


public class MarklinMonPane extends jmri.jmrix.AbstractMonPane implements MarklinListener, MarklinPanelInterface{

    public MarklinMonPane() {
        super();
    }
    
    public String getHelpTarget() { return null; }
    public String getTitle() { 
        if (memo!=null){
            return memo.getUserName () + " Command Monitor";
        }
        return "CS2 Command Monitor"; 
    }

    public void dispose() {
        // disconnect from the LnTrafficController
        memo.getTrafficController().removeMarklinListener(this);
        // and unwind swing
        super.dispose();
    }
    
    public void init() {}
    
    MarklinSystemConnectionMemo memo;
    
    public void initContext(Object context) {
        if (context instanceof MarklinSystemConnectionMemo ) {
            initComponents((MarklinSystemConnectionMemo) context);
        }
    }
    
    public void initComponents(MarklinSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the MarklinTrafficController
        memo.getTrafficController().addMarklinListener(this);
    }

    public synchronized void message(MarklinMessage l) {  // receive a message and log it
        if (l.isBinary())
          	nextLine("binary cmd: "+l.toString()+"\n", null);
        else
            nextLine("cmd: \""+l.toString()+"\"\n", null);
	}
    
	public synchronized void reply(MarklinReply l) {  // receive a reply message and log it
	    String raw = "";
	    for (int i=0;i<l.getNumDataElements(); i++) {
	        if (i>0) raw+=" ";
            raw = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i)&0xFF, raw);
        }
	        
	    if (l.isUnsolicited()) {    
            nextLine("msg: \""+MarklinMon.displayReply(l)+"\"\n", raw);
        } else {
            nextLine("rep: \""+MarklinMon.displayReply(l)+"\"\n", raw);
        }
	}
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.marklin.swing.MarklinNamedPaneAction {
        public Default() {
            super("CS2 Command Monitor", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                MarklinMonPane.class.getName(), 
                jmri.InstanceManager.getDefault(MarklinSystemConnectionMemo.class));
        }
    }

	static Logger log = Logger.getLogger(MarklinMonPane.class.getName());

}


/* @(#)MonAction.java */
