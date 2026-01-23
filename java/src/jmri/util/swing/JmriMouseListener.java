package jmri.util.swing;

import java.awt.Component;
import java.awt.event.MouseEvent;

import jmri.util.SystemType;

/**
 * Replacement for {@link java.awt.event.MouseListener}.
 * This class is used to replace {@link java.awt.event.MouseEvent} with
 * {@link jmri.util.swing.JmriMouseEvent}.
 *
 * This adds system-type specific behavior for macOS. Since Java 11-21 (at least) on macOS
 * omits calling mouseClicked if the cursor has moved even a tiny bit while the mouse
 * is down, this replaces that behavior with a small dead zone on macOS only.
 *
 * @author Daniel Bergqvist (C) 2022
 * @author Bob Jacobsen (C) 2026
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
                if (! SystemType.isMacOSX()) { // macOS handles clicks in mouseReleased
                    listener.mouseClicked(new JmriMouseEvent(e));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
                lastClickCount = e.getClickCount();
                listener.mousePressed(new JmriMouseEvent(e));
            }

            static final int DEADBAND2 = 16;  // 4 pixels, with drag theshold of 5
            
            @Override
            public void mouseReleased(MouseEvent e) {
                listener.mouseReleased(new JmriMouseEvent(e));
                
                if (SystemType.isMacOSX()) {
                    if (Math.pow(e.getY()-lastY,2)+Math.pow(e.getX()-lastX,2) <= DEADBAND2) {
                    
                        Component source = null;
                        if (e.getSource() instanceof Component) { 
                            source = (Component) e.getSource();
                        }
                        listener.mouseClicked(new JmriMouseEvent(
                            source, e.getID(), e.getWhen(), e.getModifiersEx(),
                            e.getX(), e.getY(), lastClickCount, e.isPopupTrigger(),
                            e.getButton()
                        ));
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                listener.mouseEntered(new JmriMouseEvent(e));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                listener.mouseExited(new JmriMouseEvent(e));
            }
            
            int lastX, lastY, lastClickCount;
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
