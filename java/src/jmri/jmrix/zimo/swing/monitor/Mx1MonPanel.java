/**
 * Mx1MonPane.java
 *
 * Description:	Swing action to create and register a MonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision: 22942 $
 * @author	kcameron Copyright (C) 2011 copied from SerialMonPane.java
 * @author	Daniel Boudreau Copyright (C) 2012 added human readable format
 */
package jmri.jmrix.zimo.swing.monitor;

import java.util.Date;
import javax.swing.JCheckBox;
import jmri.jmrix.zimo.Mx1Listener;
import jmri.jmrix.zimo.Mx1Message;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
import jmri.jmrix.zimo.swing.Mx1PanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mx1MonPanel extends jmri.jmrix.AbstractMonPane implements Mx1Listener, Mx1PanelInterface {

    private static final long serialVersionUID = 6106790197336170372L;

    public Mx1MonPanel() {
        super();
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.zimo.swing.monitor.Mx1MonPanel";
    }//IN18N

    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("Mx1_"); //IN18N
        }
        x.append(": "); //IN18N
        x.append("Command Monitor"); //IN18N
        return x.toString();
    }

    @Override
    public void dispose() {
        if (memo.getMx1TrafficController() != null) {
            memo.getMx1TrafficController().removeMx1Listener(~0, this);
        }
        // and unwind swing
        super.dispose();
    }

    @Override
    public void init() {
    }

    Mx1SystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof Mx1SystemConnectionMemo) {
            initComponents((Mx1SystemConnectionMemo) context);
        }
    }

    JCheckBox includePoll = new JCheckBox("Include Poll Messages"); //IN18N

    @Override
    public void initComponents(Mx1SystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the LnTrafficController
        if (memo.getMx1TrafficController() == null) {
            log.error("No traffic controller is available"); //IN18N
            return;
        }
        memo.getMx1TrafficController().addMx1Listener(~0, this);
    }

    Date previousTimeStamp;

    public synchronized void notifyXmit(Date timestamp, Mx1Message m) {
        logMessage(timestamp, m, "Tx:");
    }

    public synchronized void notifyFailedXmit(Date timestamp, Mx1Message m) {

        logMessage(timestamp, m, "FAILED:");
    }

    public synchronized void notifyRcv(Date timestamp, Mx1Message m) {

        String prefix = "Rx:"; //IN18N
        logMessage(timestamp, m, prefix);
    }

    public synchronized void message(Mx1Message l) {  // receive a MX-1 message and log it
        // display the raw data if requested
        String raw = "packet: ";
        if (rawCheckBox.isSelected()) {
            int len = l.getNumDataElements();
            for (int i = 0; i < len; i++) {
                raw += Integer.toHexString(l.getElement(i)) + " ";
            }
        }

        // display the decoded data
        nextLine(l.getStringMsg() + "\n", raw);
    }

    private void logMessage(Date timestamp, Mx1Message m, String src) {  // receive a Mrc message and log it
        String raw = "";
        for (int i = 0; i < m.getNumDataElements(); i++) {
            if (i > 0) {
                raw += " ";
            }
            raw = jmri.util.StringUtil.appendTwoHexFromInt(m.getElement(i) & 0xFF, raw);
        }

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLineWithTime(timestamp, src + " " + m.toString() + "\n", raw);
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.zimo.swing.Mx1NamedPaneAction {

        private static final long serialVersionUID = -7644336249246783644L;

        public Default() {
            super("Mx1 Command Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    Mx1MonPanel.class.getName(),
                    jmri.InstanceManager.getDefault(Mx1SystemConnectionMemo.class)); //IN18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1MonPanel.class.getName());

}


/* @(#)MonAction.java */
