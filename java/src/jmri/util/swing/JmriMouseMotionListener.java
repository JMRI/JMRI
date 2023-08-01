package jmri.util.swing;

import java.awt.event.MouseEvent;

/**
 * Replacement for {@link java.awt.event.MouseMotionListener}.
 * This class is used to replace {@link java.awt.event.MouseEvent} with
 * {@link jmri.util.swing.JmriMouseEvent}.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public interface JmriMouseMotionListener extends java.util.EventListener {

    /**
     * Adapt a JmriMouseMotionListener to a MouseMotionListener.
     * @param listener the JmriMouseListener
     * @return the MouseListener
     */
    static java.awt.event.MouseMotionListener adapt(JmriMouseMotionListener listener) {
        return new java.awt.event.MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                listener.mouseDragged(new JmriMouseEvent(e));
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                listener.mouseMoved(new JmriMouseEvent(e));
            }
        };
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  {@code MOUSE_DRAGGED} events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p>
     * Due to platform-dependent Drag&amp;Drop implementations,
     * {@code MOUSE_DRAGGED} events may not be delivered during a native
     * Drag&amp;Drop operation.
     * @param e the event to be processed
     */
    void mouseDragged(JmriMouseEvent e);

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     * @param e the event to be processed
     */
    void mouseMoved(JmriMouseEvent e);

}
