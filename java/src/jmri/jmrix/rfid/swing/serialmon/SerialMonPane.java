package jmri.jmrix.rfid.swing.serialmon;

import jmri.InstanceManager;
import jmri.jmrix.rfid.RfidListener;
import jmri.jmrix.rfid.RfidMessage;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.swing.RfidNamedPaneAction;
import jmri.jmrix.rfid.swing.RfidPanelInterface;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 * Swing action to create and register a MonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class SerialMonPane extends jmri.jmrix.AbstractMonPane implements RfidListener, RfidPanelInterface {

    public SerialMonPane() {
        super();
    }

    @Override
    public String getHelpTarget() {
        return null;
    }

    @Override
    public String getTitle() {
        return "RFID Device Command Monitor";
    }

    @Override
    public void dispose() {
        // disconnect from the RfidTrafficController
        memo.getTrafficController().removeRfidListener(this);
        // and unwind swing
        super.dispose();
    }

    @Override
    public void init() {
    }

    RfidSystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof RfidSystemConnectionMemo) {
            initComponents((RfidSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(RfidSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the RfidTrafficController
        memo.getTrafficController().addRfidListener(this);
    }

    @Override
    public synchronized void message(RfidMessage l) {  // receive a message and log it
        if (l.isBinary()) {
            nextLine("binary cmd: " + l.toMonitorString() + "\n", null);
        } else {
            nextLine("cmd: \"" + l.toMonitorString() + "\"\n", null);
        }
    }

    @Override
    public synchronized void reply(RfidReply l) {  // receive a reply message and log it
        String raw = "";
        for (int i = 0; i < l.getNumDataElements(); i++) {
            if (i > 0) {
                raw += " ";
            }
            raw = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i) & 0xFF, raw);
        }

        if (l.isUnsolicited()) {
            nextLine("msg: \"" + l.toMonitorString() + "\"\n", raw);
        } else {
            nextLine("rep: \"" + l.toMonitorString() + "\"\n", raw);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends RfidNamedPaneAction {

        public Default() {
            super("RFID Device Command Monitor",
                    new JmriJFrameInterface(),
                    SerialMonPane.class.getName(),
                    InstanceManager.getDefault(RfidSystemConnectionMemo.class));
        }
    }

}
