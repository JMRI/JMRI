package jmri.util.swing.multipane;

import java.awt.Frame;
import java.util.HashSet;
import javax.swing.JComponent;
import jmri.util.swing.JmriAbstractAction;

/**
 * Display a JComponent in a specific paned window.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4
 */
public class PanedInterface implements jmri.util.swing.WindowInterface {

    public PanedInterface(MultiPaneWindow frame) {
        this.frame = frame;
    }

    MultiPaneWindow frame;

    @Override
    public void show(jmri.util.swing.JmriPanel child,
            JmriAbstractAction act,
            Hint hint) {

        JComponent destination;
        if (hint == Hint.EXTEND) {
            destination = frame.getLowerRight();
        } else {
            destination = frame.getUpperRight();
        }

        destination.removeAll();
        destination.add(child);
        destination.revalidate();
        frame.resetRightToPreferredSizes();

        if (act != null) {
            actions.add(act);
        }
    }

    @Override
    public void show(final jmri.util.swing.JmriPanel child,
            jmri.util.swing.JmriAbstractAction act) {

        show(child, act, Hint.DEFAULT);
    }

    HashSet<JmriAbstractAction> actions = new HashSet<JmriAbstractAction>();

    /**
     * Return the same instance for multiple requests
     */
    @Override
    public boolean multipleInstances() {
        return false;
    }

    /**
     * Dispose when associated window is complete
     */
    @Override
    public void dispose() {
        for (JmriAbstractAction a : actions) {
            a.dispose();
        }
    }

    @Override
    public Frame getFrame() {
        return (this.frame != null) ? this.frame : null;
    }
}
