package jmri.jmrix.sprog.update;

import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;

/**
 * Get the firmware version of the attached SPROG hardware
 *
 * @author	Andrew Crosland Copyright (C) 2008
 */
public class SprogVersionFrame extends jmri.util.JmriJFrame implements SprogVersionListener {

    private SprogSystemConnectionMemo _memo = null;

    public SprogVersionFrame(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    synchronized public void initComponents() throws Exception {
        setTitle("SPROG Version");

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.sprog.update.SprogVersionFrame", true);

        // Start the query
        SprogVersionQuery query = _memo.getSprogVersionQuery();
        query.requestVersion(this);
    }

    synchronized public void notifyVersion(SprogVersion v) {
        if (log.isDebugEnabled()) {
            log.debug("Version " + v.toString() + " notified");
        }
        JOptionPane.showMessageDialog(null, v.toString(),
                "SPROG Version", JOptionPane.INFORMATION_MESSAGE);
        setVisible(false);
        dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogVersionFrame.class.getName());
}
