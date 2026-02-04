package jmri.jmrix.sprog.update;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.util.swing.JmriJOptionPane;

/**
 * Display the firmware version of the attached SPROG hardware.
 *
 * @author Andrew Crosland Copyright (C) 2008
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
    public synchronized void notifyVersion(SprogVersion v) {
        log.debug("Version {} notified", v.toString());
        JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("SprogVersionDialogString", v.toString()),
                Bundle.getMessage("SprogVersionTitle"), JmriJOptionPane.INFORMATION_MESSAGE);
        setVisible(false);
        dispose();
    }

    /**
     * Removes SprogVersionListener.
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        _memo.getSprogVersionQuery().removeSprogVersionListener(this);
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SprogVersionFrame.class);
}
