// MonitorPane.java

package jmri.jmrix.openlcb.swing.monitor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.swing.CanPanelInterface;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

/**
 * Frame displaying (and logging) OpenLCB (CAN) frames
 *
 * @author	    Bob Jacobsen   Copyright (C) 2009, 2010
 * @version         $Revision: 17977 $
 */

public class MonitorPane extends jmri.jmrix.AbstractMonPane implements CanListener, CanPanelInterface {

    public MonitorPane() {
        super();
    }

    CanSystemConnectionMemo memo;
    
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo ) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }
    
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        memo.getTrafficController().addCanListener(this);
    }
    
    public String getTitle() {
        return "OpenLCB Monitor";
    }


    protected void init() {
    }

    public void dispose() {
       memo.getTrafficController().removeCanListener(this);
        super.dispose();
    }

    public synchronized void message(CanMessage l) {  // receive a message and log it
        if (log.isDebugEnabled()) log.debug("Message: "+l.toString());
        StringBuilder formatted = new StringBuilder("M: ");
        formatted.append(l.isExtended() ? "[" : "(");
        formatted.append(Integer.toHexString(l.getHeader()));
        formatted.append((l.isExtended() ? "]" : ")"));
        for (int i = 0; i < l.getNumDataElements(); i++) {
            formatted.append(" ");
            formatted.append(jmri.util.StringUtil.twoHexFromInt(l.getElement(i)));
        }
        formatted.append("\n");
        nextLine(new String(formatted), l.toString());
    }

    public synchronized void reply(CanReply l) {  // receive a reply and log it
        if (log.isDebugEnabled()) log.debug("Reply: "+l.toString());
        StringBuilder formatted = new StringBuilder("R: ");
        formatted.append(l.isExtended() ? "[" : "(");
        formatted.append(Integer.toHexString(l.getHeader()));
        formatted.append((l.isExtended() ? "]" : ")"));
        for (int i = 0; i < l.getNumDataElements(); i++) {
            formatted.append(" ");
            formatted.append(jmri.util.StringUtil.twoHexFromInt(l.getElement(i)));
        }
        formatted.append("\n");
        nextLine(new String(formatted), l.toString());
    }
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {
        public Default() {
            super("Openlcb Monitor", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                MonitorPane.class.getName(), 
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonitorPane.class.getName());

}
