package jmri.jmrit.display.layoutEditor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LayoutEditorComponent extends JComponent {

    private final LayoutEditor layoutEditor;

    protected LayoutEditorComponent(@Nonnull final LayoutEditor LayoutEditor) {
        super();
        this.layoutEditor = LayoutEditor;
    }

    // (not actually used anywhere…)
    protected LayoutEditor getLayoutEditor() {
        return layoutEditor;
    }

    @Override
    public void paint(Graphics g) {
        if (g instanceof Graphics2D) {
            layoutEditor.draw((Graphics2D) g);
        }
    }

    @Override
    public Rectangle getBounds() {
        return MathUtil.rectangle2DToRectangle(layoutEditor.getTargetPanel().getBounds());
    }

    @Override
    public Rectangle getBounds(Rectangle rv) {
        rv.setBounds(getBounds());
        return rv;
    }

    @Override
    public int getX() {
        return (int) layoutEditor.getTargetPanel().getX();
    }

    @Override
    public int getY() {
        return (int) layoutEditor.getTargetPanel().getY();
    }

    @Override
    public int getWidth() {
        return (int) layoutEditor.getTargetPanel().getWidth();
    }

    @Override
    public int getHeight() {
        return (int) layoutEditor.getTargetPanel().getHeight();
    }

    //initialize logging
    private transient final static Logger log = LoggerFactory.getLogger(
            LayoutEditorComponent.class
    );
}
