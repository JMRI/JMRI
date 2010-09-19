// MonitorFrame.java

package jmri.jmrix.openlcb.swing.monitor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;

/**
 * Frame displaying (and logging) OpenLCB (CAN) frames
 *
 * @author	    Bob Jacobsen   Copyright (C) 2009, 2010
 * @version         $Revision: 1.1 $
 */

public class MonitorFrame extends jmri.jmrix.AbstractMonFrame implements CanListener {

    public MonitorFrame() {
        super();
    }

    protected String title() { return "OpenLCB Monitor"; }

    protected void init() {
        TrafficController.instance().addCanListener(this);
    }

    public void dispose() {
        TrafficController.instance().removeCanListener(this);
        super.dispose();
    }

    public synchronized void message(CanMessage l) {  // receive a message and log it
        if (log.isDebugEnabled()) log.debug("Message: "+l.toString());
        String formatted = "("+Integer.toHexString(l.getHeader())
                            + (l.isExtended() ? " ext)" : ")");
        for (int i = 0; i < l.getNumDataElements(); i++)
            formatted += " "+jmri.util.StringUtil.twoHexFromInt(l.getElement(i));
        nextLine("M: "+formatted+"\n", l.toString());
    }

    public synchronized void reply(CanReply l) {  // receive a reply and log it
        if (log.isDebugEnabled()) log.debug("Reply: "+l.toString());
        String formatted = "("+Integer.toHexString(l.getHeader())
                            + (l.isExtended() ? " ext)" : ")");
        for (int i = 0; i < l.getNumDataElements(); i++)
            formatted += " "+jmri.util.StringUtil.twoHexFromInt(l.getElement(i));
        nextLine("R: "+formatted+"\n", l.toString());
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonitorFrame.class.getName());

}
