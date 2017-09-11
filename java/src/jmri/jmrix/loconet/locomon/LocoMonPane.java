package jmri.jmrix.loconet.locomon;

import jmri.InstanceManager;
import jmri.ReporterManager;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnPanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocoNet Monitor pane displaying (and logging) LocoNet messages
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010
 */
public class LocoMonPane extends jmri.jmrix.AbstractMonPane implements LocoNetListener, LnPanelInterface {

    public LocoMonPane() {
        super();
        // provide a default Llnmon instance - this should be replaced with the
        // correct one later, but is needed for Unit Testing
        this.llnmon = new Llnmon(InstanceManager.getDefault(TurnoutManager.class),
                InstanceManager.getDefault(SensorManager.class),
                InstanceManager.getDefault(ReporterManager.class));
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.locomon.LocoMonFrame"; // NOI18N
    }

    @Override
    public String getTitle() {
        String uName = "";
        if (memo != null) {
            uName = memo.getUserName();
            if (!"LocoNet".equals(uName)) { // NOI18N
                uName = uName + ": ";
            } else {
                uName = "";
            }
        }
        return uName + Bundle.getMessage("MenuItemLocoNetMonitor");
    }

    @Override
    public void dispose() {
        if (memo != null && memo.getLnTrafficController() != null) {
            // disconnect from the LnTrafficController
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
        // and unwind swing
        super.dispose();
    }

    @Override
    public void init() {
    }

    LocoNetSystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof LocoNetSystemConnectionMemo) {
            initComponents((LocoNetSystemConnectionMemo) context);
        }
    }

    @Override
    public synchronized void initComponents(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the LnTrafficController
        if (memo.getLnTrafficController() == null) {
            log.error("No traffic controller is available");
            return;
        }
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        this.llnmon = new Llnmon(memo);
    }

    @Override
    public synchronized void message(LocoNetMessage l) {  // receive a LocoNet message and log it
        // send the raw data, to display if requested
        String raw = l.toString();
        //format the message text, expect it to provide consistent \n after each line
        String formatted = llnmon.displayMessage(l);

        // display the formatted data in the monitor pane
        nextLine(formatted, raw);

        //include loconet monitoring in session.log if TRACE enabled
        log.trace(formatted.substring(0, formatted.length() - 1));

    }

    Llnmon llnmon;

    /**
     * Get hex opcode for filtering.
     *
     * @param raw byte sequence
     * @return the first byte pair
     */
    @Override
    protected String getOpCodeForFilter(String raw) {
        //note: Loconet raw is formatted like "BB 01 00 45", so extract the correct bytes from it (BB) for comparison
        if (raw != null && raw.length() >= 2) {
            return raw.substring(0, 2);
        } else {
            return null;
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemLocoNetMonitor"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    LocoMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocoMonPane.class);
}
