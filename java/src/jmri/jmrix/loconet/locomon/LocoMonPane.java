// LocoMonPane.java
package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.LocoNetBundle;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnPanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocoNet Monitor pane displaying (and logging) LocoNet messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008, 2010
 * @version $Revision$
 */
public class LocoMonPane extends jmri.jmrix.AbstractMonPane implements LocoNetListener, LnPanelInterface {

    /**
     *
     */
    private static final long serialVersionUID = 6661496696289363944L;

    public LocoMonPane() {
        super();
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.locomon.LocoMonFrame";
    }

    public String getTitle() {
        String uName = "";
        if (memo != null) {
            uName = memo.getUserName();
            if (!"LocoNet".equals(uName)) {
                uName = uName + ": ";
            } else {
                uName = "";
            }
        }
        return uName + LocoNetBundle.bundle().getString("MenuItemLocoNetMonitor");
    }

    public void dispose() {
        if (memo!= null && memo.getLnTrafficController() != null) {
            // disconnect from the LnTrafficController
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
        // and unwind swing
        super.dispose();
    }

    public void init() {
    }

    LocoNetSystemConnectionMemo memo;

    public void initContext(Object context) {
        if (context instanceof LocoNetSystemConnectionMemo) {
            initComponents((LocoNetSystemConnectionMemo) context);
        }
    }

    public void initComponents(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the LnTrafficController
        if (memo.getLnTrafficController() == null) {
            log.error("No traffic controller is available");
            return;
        }
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        if (memo.provides(jmri.TurnoutManager.class)) {
            llnmon.setLocoNetTurnoutManager((jmri.TurnoutManager) memo.get(jmri.TurnoutManager.class));
        }
        if (memo.provides(jmri.SensorManager.class)) {
            llnmon.setLocoNetSensorManager((jmri.SensorManager) memo.get(jmri.SensorManager.class));
        }
        if (memo.provides(jmri.ReporterManager.class)) {
            llnmon.setLocoNetReporterManager((jmri.ReporterManager) memo.get(jmri.ReporterManager.class));
        }
    }

    public synchronized void message(LocoNetMessage l) {  // receive a LocoNet message and log it
        // send the raw data, to display if requested
        String raw = l.toString();

        // display the decoded data
        // we use Llnmon to format, expect it to provide consistent \n after each line
        nextLine(llnmon.displayMessage(l), raw);
    }

    jmri.jmrix.loconet.locomon.Llnmon llnmon = new jmri.jmrix.loconet.locomon.Llnmon();

    /** 
     * Get hex opcode for filtering
     */
    @Override
    protected String getOpCodeForFilter(String raw) {
        //note: Loconet raw is formatted like "BB 01 00 45", so extract the correct bytes from it (BB) for comparison
        if (raw != null && raw.length() >= 2) {
            return raw.substring(0,2);
        } else return null;
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = -3893647635865243864L;

        public Default() {
            super(LocoNetBundle.bundle().getString("MenuItemLocoNetMonitor"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    LocoMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocoMonPane.class.getName());
}
