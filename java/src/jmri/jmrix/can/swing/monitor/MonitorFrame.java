// MonitorFrame.java

package jmri.jmrix.can.swing.monitor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;

/**
 * Frame displaying (and logging) CAN frames
 *
 * @author	    Bob Jacobsen   Copyright (C) 2009
 * @version         $Revision$
 */

public class MonitorFrame extends jmri.jmrix.AbstractMonFrame implements CanListener {

    public MonitorFrame() {
        super();
    }

    protected String title() { return "CAN Monitor"; }

    protected void init() {
        TrafficController.instance().addCanListener(this);
    }

    public void dispose() {
        TrafficController.instance().removeCanListener(this);
        super.dispose();
    }

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
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonitorFrame.class.getName());

}
