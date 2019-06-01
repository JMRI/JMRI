package jmri.jmrix.powerline.swing.serialmon;

import java.util.ResourceBundle;
import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.swing.PowerlinePanelInterface;

/**
 * Swing action to create and register a MonFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008 copied from Ecos and converted
 * to Powerline
 * @author	Ken Cameron Copyright (C) 2011
 */
public class SerialMonPane extends jmri.jmrix.AbstractMonPane implements SerialListener, PowerlinePanelInterface {

   public SerialMonPane() {
        super();
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.powerline.serialmon.SerialMonFrame";
    }

    @Override
    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append(Bundle.getMessage("DefaultTag"));
        }
        x.append(": ");
        x.append(Bundle.getMessage("Title"));
        return x.toString();
    }

    @Override
    public void dispose() {
        // disconnect from the SerialTrafficController
        if (memo != null) {
            memo.getTrafficController().removeSerialListener(this);
        }
        // and unwind swing
        super.dispose();
    }

    @Override
    public void init() {
    }

    SerialSystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof SerialSystemConnectionMemo) {
            initComponents((SerialSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(SerialSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the SerialTrafficController
        memo.getTrafficController().addSerialListener(this);
    }

    @Override
    public synchronized void message(SerialMessage l) {  // receive a message and log it
        logMessage(l);
    }

    @Override
    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        logMessage(l);
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.powerline.swing.PowerlineNamedPaneAction {

        public Default() {
            super("Open Powerline Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    SerialMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(SerialSystemConnectionMemo.class));
        }

    }

}
