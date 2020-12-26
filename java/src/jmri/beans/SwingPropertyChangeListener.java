package jmri.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;

/**
 * If constructed with {@code SwingPropertyChangeListener(listener, true)} this
 * subclass of {@link PropertyChangeListener} ensures listener is only ever
 * notified on the <i>Event Dispatch Thread</i>.
 *
 * @author Randall Wood Copyright 2020
 */
public class SwingPropertyChangeListener implements PropertyChangeListener {

    private final PropertyChangeListener listener;
    private final boolean notifyOnEDT;

    /**
     * Create a SwingPropertyChangeListener with an associated listener that
     * notifies the associated listener on the EDT.
     *
     * @param listener the listener that {@link PropertyChangeEvent}s will be
     *                 passed to
     */
    public SwingPropertyChangeListener(PropertyChangeListener listener) {
        this(listener, true);
    }

    /**
     * Create a SwingPropertyChangeListener with an associated listener.
     *
     * @param listener    the listener that {@link PropertyChangeEvent}s will be
     *                    passed to
     * @param notifyOnEDT true to notify listener on the EDT; false to notify
     *                    listener on current thread
     */
    public SwingPropertyChangeListener(PropertyChangeListener listener, boolean notifyOnEDT) {
        this.listener = listener;
        this.notifyOnEDT = notifyOnEDT;
    }

    /**
     * {@inheritDoc}
     * 
     * This implementation calls the listener's implementation on the EDT if
     * {@link #isNotifyOnEDT()} is true.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!notifyOnEDT || SwingUtilities.isEventDispatchThread()) {
            listener.propertyChange(evt);
        } else {
            SwingUtilities.invokeLater(() -> listener.propertyChange(evt));
        }
    }

    public boolean isNotifyOnEDT() {
        return notifyOnEDT;
    }
}
