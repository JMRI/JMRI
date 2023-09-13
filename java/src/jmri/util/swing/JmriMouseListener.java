package jmri.util.swing;

import java.awt.event.MouseEvent;

/**
 * Replacement for {@link java.awt.event.MouseListener}.
 * This class is used to replace {@link java.awt.event.MouseEvent} with
 * {@link jmri.util.swing.JmriMouseEvent}.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public interface JmriMouseListener extends java.util.EventListener {

    /**
     * Adapt a JmriMouseListener to a MouseListener.
     * @param listener the JmriMouseListener
     * @return the MouseListener
     */
    static java.awt.event.MouseListener adapt(JmriMouseListener listener) {
        return new java.awt.event.MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                listener.mouseClicked(new JmriMouseEvent(e));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                listener.mousePressed(new JmriMouseEvent(e));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                listener.mouseReleased(new JmriMouseEvent(e));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                listener.mouseEntered(new JmriMouseEvent(e));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                listener.mouseExited(new JmriMouseEvent(e));
            }
        };
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     * @param e the event to be processed
     */
    void mouseClicked(JmriMouseEvent e);

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e the event to be processed
     */
    void mousePressed(JmriMouseEvent e);

    /**
     * Invoked when a mouse button has been released on a component.
     * @param e the event to be processed
     */
    void mouseReleased(JmriMouseEvent e);

    /**
     * Invoked when the mouse enters a component.
     * @param e the event to be processed
     */
    void mouseEntered(JmriMouseEvent e);

    /**
     * Invoked when the mouse exits a component.
     * @param e the event to be processed
     */
    void mouseExited(JmriMouseEvent e);

}
