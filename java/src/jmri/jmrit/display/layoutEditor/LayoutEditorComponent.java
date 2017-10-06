package jmri.jmrit.display.layoutEditor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import jmri.util.MathUtil;

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
        Dimension d = layoutEditor.getTargetPanel().getSize();
        return new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight());
    }

    @Override
    public int getX() {
        return (int) layoutEditor.getBounds().getX();
    }

    @Override
    public int getY() {
        return (int) layoutEditor.getBounds().getY();
    }

    @Override
    public int getWidth() {
        return (int) layoutEditor.getBounds().getWidth();
    }

    @Override
    public int getHeight() {
        return (int) layoutEditor.getBounds().getHeight();
    }

    @Override
    public Rectangle getBounds(Rectangle rv) {
        rv.setBounds(MathUtil.rectangle2DToRectangle(layoutEditor.getBounds()));
        return rv;
    }
}
