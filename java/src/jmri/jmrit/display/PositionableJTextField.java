package jmri.jmrit.display;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * A text field hosted by a positionable panel in an editor whose painting is
 * zoomed with a Graphics transform. It keeps hit testing and mouse coordinates
 * aligned with the scaled rendering.
 */
class PositionableJTextField extends JTextField {

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        PositionableJPanel panel = getPositionablePanel();
        if (panel == null || panel.getParent() == null || panel.getEditor() == null
                || panel.getEditor().getTargetPanel() == null) {
            super.repaint(tm, x, y, width, height);
            return;
        }

        Rectangle dirtyBounds = SwingUtilities.convertRectangle(this,
                new Rectangle(x, y, width, height), panel.getEditor().getTargetPanel());
        panel.getEditor().repaintTargetPanel(dirtyBounds);
    }

    private PositionableJPanel getPositionablePanel() {
        var parent = getParent();
        if (parent instanceof PositionableJPanel) {
            return (PositionableJPanel) parent;
        } else {
            return null;
        }
    }

    private Point getPanelOrigin() {
        PositionableJPanel panel = getPositionablePanel();
        if (panel == null || panel.getEditor() == null || panel.getEditor().getTargetPanel() == null
                || getParent() == null) {
            return null;
        }
        return SwingUtilities.convertPoint(this, 0, 0, panel.getEditor().getTargetPanel());
    }

    @Override
    public boolean contains(int x, int y) {
        PositionableJPanel panel = getPositionablePanel();
        Point origin = getPanelOrigin();
        if (panel == null || origin == null) {
            return super.contains(x, y);
        }
        double scale = panel.getEditor().getPaintScale();
        if (scale == 1.0) {
            return super.contains(x, y);
        }
        double unscaledX = (x + origin.x) / scale - origin.x;
        double unscaledY = (y + origin.y) / scale - origin.y;
        return unscaledX >= 0 && unscaledX < getWidth()
                && unscaledY >= 0 && unscaledY < getHeight();
    }

    @Override
    protected void processMouseEvent(MouseEvent event) {
        super.processMouseEvent(transformMouseEvent(event));
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent event) {
        super.processMouseMotionEvent(transformMouseEvent(event));
    }

    private MouseEvent transformMouseEvent(MouseEvent event) {
        PositionableJPanel panel = getPositionablePanel();
        Point origin = getPanelOrigin();
        if (panel == null || origin == null) {
            return event;
        }
        double scale = panel.getEditor().getPaintScale();
        if (scale == 1.0) {
            return event;
        }

        int x = (int) Math.round((event.getX() + origin.x) / scale - origin.x);
        int y = (int) Math.round((event.getY() + origin.y) / scale - origin.y);
        return new MouseEvent(this, event.getID(), event.getWhen(), event.getModifiersEx(), x, y,
                event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(),
                event.isPopupTrigger(), event.getButton());
    }
}
