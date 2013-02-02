// MonitorPane.java

package jmri.jmrix.can.swing.monitor;

import org.apache.log4j.Logger;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanPanelInterface;

/**
 * Frame displaying (and logging) CAN frames
 *
 * @author	    Bob Jacobsen   Copyright (C) 2009
 * @version         $Revision: 17977 $
 */

public class MonitorPane extends jmri.jmrix.AbstractMonPane implements CanListener, CanPanelInterface {

    public MonitorPane(){
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
        try{
            initComponents();
        } catch (Exception e){
            log.error(e.toString());
        }
    }

    public String getTitle() {
        return "CAN Monitor";
    }
    
    public void init() {}

    public synchronized void message(CanMessage l) {  // receive a message and log it
        if (log.isDebugEnabled()) log.debug("Message: "+l.toString());
        StringBuffer buf = new StringBuffer();
        //String formatted = "("+Integer.toHexString(l.getHeader())
        //                    + (l.isExtended() ? " ext)" : ")");
        buf.append("("+Integer.toHexString(l.getHeader())
                            + (l.isExtended() ? " ext)" : ")"));
        for (int i = 0; i < l.getNumDataElements(); i++)
            //formatted += " "+jmri.util.StringUtil.twoHexFromInt(l.getElement(i));
            buf.append(" "+jmri.util.StringUtil.twoHexFromInt(l.getElement(i)));
        nextLine("M: "+buf.toString()+"\n", l.toString());
    }

    public synchronized void reply(CanReply l) {  // receive a reply and log it
        if (log.isDebugEnabled()) log.debug("Reply: "+l.toString());
        StringBuffer buf = new StringBuffer();
        //String formatted = "("+Integer.toHexString(l.getHeader())
        //                    + (l.isExtended() ? " ext)" : ")");
        buf.append("("+Integer.toHexString(l.getHeader())
                            + (l.isExtended() ? " ext)" : ")"));
        for (int i = 0; i < l.getNumDataElements(); i++)
            //formatted += " "+jmri.util.StringUtil.twoHexFromInt(l.getElement(i));
            buf.append(" "+jmri.util.StringUtil.twoHexFromInt(l.getElement(i)));
        nextLine("R: "+buf.toString()+"\n", l.toString());
    }
    
    public void dispose() {
        // disconnect from the LnTrafficController
        memo.getTrafficController().removeCanListener(this);
        // and unwind swing
        super.dispose();
    }
    
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {
        public Default() {
            super("CAN Monitor", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                MonitorPane.class.getName(), 
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    
    static Logger log = Logger.getLogger(MonitorPane.class.getName());

}
