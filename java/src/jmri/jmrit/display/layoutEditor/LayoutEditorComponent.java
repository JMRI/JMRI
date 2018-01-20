package jmri.jmrit.display.layoutEditor;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import jmri.util.MathUtil;

/*
 * This is an intermediate component used to put the Layout Editor
 * into the component layers hierarchy so that objects can be drawn
 * in front of or behind layout editor objects.
 *
 * @author George Warner Copyright (c) 2017-2018
*/

class LayoutEditorComponent extends JComponent {

    private final LayoutEditor layoutEditor;

    protected LayoutEditorComponent(@Nonnull final LayoutEditor LayoutEditor) {
        super();
        this.layoutEditor = LayoutEditor;
    }

    // not actually used anywhere
    protected LayoutEditor getLayoutEditor() {
        return layoutEditor;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        if (g instanceof Graphics2D) {
            layoutEditor.draw((Graphics2D) g);
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public Rectangle getBounds() {
        JComponent targetPanel = layoutEditor.getTargetPanel();
        Rectangle2D targetBounds = targetPanel.getBounds();
        Container parent = getParent();
        if (parent != null) {   // if there is a parent...
            Rectangle2D parentBounds = parent.getBounds();

            // convert our origin to parent coordinates
            Point2D origin = new Point2D.Double(
                    targetBounds.getX() - parentBounds.getX(),
                    targetBounds.getY() - parentBounds.getY());

            return new Rectangle((int) origin.getX(), (int) origin.getY(),
                    (int) targetBounds.getWidth(), (int) targetBounds.getHeight());
        } else {
            return MathUtil.rectangle2DToRectangle(targetBounds);
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public Rectangle getBounds(Rectangle rv) {
        rv.setBounds(getBounds());
        return rv;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public int getX() {
        Rectangle bounds = getBounds();
        return (int) bounds.getX();
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public int getY() {
        Rectangle bounds = getBounds();
        return (int) bounds.getY();
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        Rectangle bounds = getBounds();
        return (int) bounds.getWidth();
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        Rectangle bounds = getBounds();
        return (int) bounds.getHeight();
    }

    //initialize logging
    //private transient final static Logger log = LoggerFactory.getLogger(
    //        LayoutEditorComponent.class
    //);

}
