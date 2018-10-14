package jmri.jmrix.sprog.update;

import javax.swing.JOptionPane;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display the firmware version of the attached SPROG hardware.
 *
 * @author	Andrew Crosland Copyright (C) 2008
 */
public class SprogVersionFrame extends jmri.util.JmriJFrame implements SprogVersionListener {

    private SprogSystemConnectionMemo _memo = null;

    public SprogVersionFrame(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void initComponents() {
        setTitle(Bundle.getMessage("SprogVersionTitle"));

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.sprog.update.SprogVersionFrame", true);

        // Start the query
        SprogVersionQuery query = _memo.getSprogVersionQuery();
        query.requestVersion(this);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void notifyVersion(SprogVersion v) {
        log.debug("Version {} notified", v.toString());
        JOptionPane.showMessageDialog(null, Bundle.getMessage("SprogVersionDialogString", v.toString()),
                Bundle.getMessage("SprogVersionTitle"), JOptionPane.INFORMATION_MESSAGE);
        setVisible(false);
        dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogVersionFrame.class);
}
