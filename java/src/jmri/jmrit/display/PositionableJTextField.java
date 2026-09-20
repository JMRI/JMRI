package jmri.jmrit.display;

import java.awt.*;
import java.awt.event.*;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import javax.swing.*;

/**
 * This is a base class implementation for 
 * {@link PositionableJPanel} subclasses that contain
 * a single {@link JTextField} for their implementation.
 * <p>
 * @author Bob Jacobsen   Copyright (C) 2026
 * @since 5.17.4
 */
 
public class PositionableJTextField extends PositionableJPanel {

    public PositionableJTextField(Editor editor) {
        super(editor);
    }

    // provide a customized JTextField for subclass use
    protected final JTextField _textBox = new RepositioningJTextField(); // local class
    protected int _nCols;

    // reimplement positioning to use the local coordinates in the enclosed JTextField

    @OverridingMethodsMustInvokeSuper // final to require this
    @Override
    protected final void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        // Scale the rendering context
        g2d.scale(getScale(), getScale());
        super.paintComponent(g2d);
        g2d.dispose();
    }

    @OverridingMethodsMustInvokeSuper // final to require this
    @Override
    public final void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(translateMouseEvent(e));
    }

    @OverridingMethodsMustInvokeSuper // final to require this
    @Override
    public final void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(translateMouseEvent(e));
    }

    @OverridingMethodsMustInvokeSuper // final to require this
    @Override
    public final boolean contains(int x, int y) {
        double scale = getScale();
        int logicalX = (int) Math.round(x / scale);
        int logicalY = (int) Math.round(y / scale);
        
        boolean retval = logicalX >= 0 && logicalX < getWidth() && logicalY >= 0 && logicalY < getHeight();
        return retval;
    }

    @OverridingMethodsMustInvokeSuper // final to require this
    final MouseEvent translateMouseEvent(MouseEvent e) {
        double scale = getScale();
        int scaledX = (int) Math.round(e.getX() / scale);
        int scaledY = (int) Math.round(e.getY() / scale);
        return new MouseEvent(
            (Component)e.getSource(), 
            e.getID(),
            e.getWhen(),
            e.getModifiersEx(),
            scaledX, scaledY,
            e.getClickCount(),
            e.isPopupTrigger(),
            e.getButton()
        );
    }

    int originalX, originalY;
    
    @OverridingMethodsMustInvokeSuper // final to require this
    @Override 
    public final int getX() { return originalX; }
    
    @OverridingMethodsMustInvokeSuper // final to require this
    @Override 
    public final int getY() { return originalY; }

    @OverridingMethodsMustInvokeSuper // final to require this
    @Override
    public final void setLocation(int x, int y) {
        originalX = x;
        originalY = y;
        double scale = getScale();
        super.setLocation((int)Math.round(scale*x), (int)Math.round(scale*y));
    }
    
    @OverridingMethodsMustInvokeSuper // final to require this
    @Override
    public final void setLocation(Point p) {
        this.setLocation(p.x, p.y);
    }

    @OverridingMethodsMustInvokeSuper // final to require this
    @Override
    public final Point getLocation() {
        return new Point(originalX, originalY);
    }

    @OverridingMethodsMustInvokeSuper // final to require this
    public final int getNumColumns() {
        return _nCols;
    }

    @OverridingMethodsMustInvokeSuper // final to require this
    public final void setNumColumns(int nCols) {
        _textBox.setColumns(nCols);
        _nCols = nCols;
    }

    @OverridingMethodsMustInvokeSuper // final to require this
    @Override
    public final JComponent getTextComponent() {
        return _textBox;
    }

    // extend JTextField to handle scaling 
    class RepositioningJTextField extends JTextField {
        // Painting is already inside the scalled MemoryInputIcon, so 
        // doesn't need to be scaled again

        // rescale the pointer location of mouse events
        @Override
        public void processMouseEvent(MouseEvent e) {
            // translateMouseEvent is obtained from the enclosing class
            super.processMouseEvent(translateMouseEvent(e));
        }
        @Override
        public void processMouseMotionEvent(MouseEvent e) {
            // translateMouseEvent is obtained from the enclosing class
            super.processMouseMotionEvent(translateMouseEvent(e));
        }
        
        // rescale detection of the bounds of the field
        @Override
        public boolean contains(int x, int y) {
            double scale = getScale();
            int logicalX = (int) Math.round(x / scale);
            int logicalY = (int) Math.round(y / scale);
            
            boolean retval = logicalX >= 0 && logicalX < getWidth() && logicalY >= 0 && logicalY < getHeight();
            return retval;
        }
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionableJTextField.class);

}
