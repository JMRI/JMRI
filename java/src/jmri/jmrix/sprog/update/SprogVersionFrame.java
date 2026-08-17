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
    private volatile boolean disposed = false;

    public SprogVersionFrame(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("SprogVersionTitle"));

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.sprog.update.SprogVersionFrame", true);

        // Start the query
        SprogVersionQuery query = _memo.getSprogVersionQuery();
        query.requestVersion(this);
    }

    /**
     * Handle the version reply. May be called on any thread (serial event
     * thread or Swing timer thread), so dispatch to the EDT to show the dialog
     * and dispose the frame. Not synchronized — dispatching via
     * runOnGUIEventually ensures neither the calling thread nor the EDT blocks
     * waiting for a lock held by the other.
     * {@inheritDoc}
     */
    @Override
    public void notifyVersion(SprogVersion v) {
        log.debug("Version {} notified", v.toString());
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            if (!disposed) {
                JmriJOptionPane.showMessageDialog(SprogVersionFrame.this,
                        Bundle.getMessage("SprogVersionDialogString", v.toString()),
                        Bundle.getMessage("SprogVersionTitle"), JmriJOptionPane.INFORMATION_MESSAGE);
                setVisible(false);
                dispose();
            }
        });
    }

    /**
     * Removes SprogVersionListener.
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        disposed = true;
        _memo.getSprogVersionQuery().removeSprogVersionListener(this);
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SprogVersionFrame.class);
}
