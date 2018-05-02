package jmri.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

/**
 * Used to intercept inputs and to display a busy cursor during reads and
 * writes.
 *
 * Based in part on code from the Java Tutorial for glass panes (java.sun.com).
 *
 * Used in PaneProgFrame to control cursor operations during programming.
 *
 * @author Howard G. Penny Copyright (C) 2005
 */
public class BusyGlassPane extends JComponent {

    CBListener listener;

    public BusyGlassPane(List<JComponent> components, List<Rectangle> rectangles, Container contentPane, JFrame parent) {
        listener = new CBListener(components, rectangles, this, contentPane, parent);
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    public void dispose() {
        this.removeMouseListener(listener);
        this.removeMouseMotionListener(listener);
    }

    /**
     * Listen for all events that our components are likely to be interested in.
     * Redispatch them to the appropriate component.
     */
    static class CBListener extends MouseInputAdapter {

        JFrame parentFrame;
        List<JComponent> liveComponents;
        List<Rectangle> liveRectangles;
        BusyGlassPane glassPane;
        Container contentPane;
        boolean inDrag = false;

        public CBListener(List<JComponent> objects, List<Rectangle> rectangles,
                BusyGlassPane glassPane, Container contentPane, JFrame parent) {
            this.parentFrame = parent;
            this.liveComponents = objects;
            this.liveRectangles = rectangles;
            this.glassPane = glassPane;
            this.contentPane = contentPane;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            redispatchMouseEvent(e);
        }

        /*
         * We must forward at least the mouse drags that started
         * with mouse presses over the button.  Otherwise,
         * when the user presses the button then drags off,
         * the button isn't disarmed -- it keeps its dark
         * gray background or whatever its L&F uses to indicate
         * that the button is currently being pressed.
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            redispatchMouseEvent(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            redispatchMouseEvent(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            redispatchMouseEvent(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            redispatchMouseEvent(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            redispatchMouseEvent(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            redispatchMouseEvent(e);
            inDrag = false;
        }

        private void redispatchMouseEvent(MouseEvent e) {
            boolean inButton = false;
            Point glassPanePoint = e.getPoint();
            Component component = null;
            Container container = contentPane;
            Point containerPoint = SwingUtilities.convertPoint(glassPane,
                    glassPanePoint,
                    contentPane);
            int eventID = e.getID();

            //XXX: If the event is from a component in a popped-up menu,
            //XXX: then the container should probably be the menu's
            //XXX: JPopupMenu, and containerPoint should be adjusted
            //XXX: accordingly.
            component = SwingUtilities.getDeepestComponentAt(container,
                    containerPoint.x,
                    containerPoint.y);

            if (component == null) {
                return;
            }

            for (int i = 0; i < liveComponents.size(); i++) {
                if (component.equals(liveComponents.get(i))) {
                    inButton = true;
                    testForDrag(eventID);
                }
            }

            for (int i = 0; i < liveRectangles.size(); i++) {
                Rectangle rectangle = this.liveRectangles.get(i);
                if (rectangle != null && rectangle.contains(containerPoint)) {
                    inButton = true;
                    testForDrag(eventID);
                }
            }

            if (inButton || inDrag) {
                Point componentPoint = SwingUtilities.convertPoint(glassPane,
                        glassPanePoint,
                        component);
                parentFrame.setCursor(Cursor.getDefaultCursor());
                component.dispatchEvent(new MouseEvent(component,
                        eventID,
                        e.getWhen(),
                        e.getModifiers(),
                        componentPoint.x,
                        componentPoint.y,
                        e.getClickCount(),
                        e.isPopupTrigger()));
            } else {
                parentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }

        }

        private void testForDrag(int eventID) {
            if (eventID == MouseEvent.MOUSE_PRESSED) {
                inDrag = true;
            }
        }
    }
}
