// GlassPane.java

package jmri.util;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import com.sun.java.util.collections.List;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;

/**
 * Used to intercept inputs and to display a busy
 * cursor during reads and writes.
 *
 * @author  Howard G. Penny   Copyright (C) 2005
 * @version $Revision: 1.1 $
 */
public class GlassPane extends JComponent {

    CBListener listener;

    public GlassPane(List components, Container contentPane, PaneProgFrame parent) {
        listener = new CBListener(components, this, contentPane, parent);
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    public void dispose() {
        this.removeMouseListener(listener);
        this.removeMouseMotionListener(listener);
    }
}

/**
 * Listen for all events that our components are likely to be
 * interested in.  Redispatch them to the appropriate component.
 */
class CBListener extends MouseInputAdapter {
    PaneProgFrame parentFrame;
    List liveComponents;
    GlassPane glassPane;
    Container contentPane;
    boolean inDrag = false;

    public CBListener(List objects,
                      GlassPane glassPane, Container contentPane, PaneProgFrame parent) {
        this.parentFrame = parent;
        this.liveComponents = objects;
        this.glassPane = glassPane;
        this.contentPane = contentPane;
    }

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
    public void mouseDragged(MouseEvent e) {
        redispatchMouseEvent(e);
    }

    public void mouseClicked(MouseEvent e) {
        redispatchMouseEvent(e);
    }

    public void mouseEntered(MouseEvent e) {
        redispatchMouseEvent(e);
    }

    public void mouseExited(MouseEvent e) {
        redispatchMouseEvent(e);
    }

    public void mousePressed(MouseEvent e) {
        redispatchMouseEvent(e);
    }

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

        if (inButton || (inButton && inDrag)) {
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
            parentFrame.setCursor(Cursor.WAIT_CURSOR);
        }

    }

    private void testForDrag(int eventID) {
        if (eventID == MouseEvent.MOUSE_PRESSED) {
            inDrag = true;
        }
    }
}
