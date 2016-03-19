package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

/**
 * PositionableRoundRect.
 * <P>
 * @author Pete Cressman Copyright (c) 2012
 */
public class PositionableEllipse extends PositionableRectangle {

    /**
     *
     */
    private static final long serialVersionUID = 2828662466661825613L;

    public PositionableEllipse(Editor editor) {
        super(editor);
    }

    public PositionableEllipse(Editor editor, Shape shape) {
        super(editor, shape);
    }

    /**
     * this class must be overridden by its subclasses and executed only after
     * its parameters have been set
     */
    public void makeShape() {
        setShape(new Ellipse2D.Double(0, 0, _width, _height));
    }

    @Override
    public Positionable deepClone() {
        PositionableEllipse pos = new PositionableEllipse(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableEllipse pos) {
        pos._width = _width;
        pos._height = _height;
        return super.finishClone(pos);
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("ellipse"));
        popup.add(new javax.swing.AbstractAction(txt) {
            /**
             *
             */
            private static final long serialVersionUID = -2502324392840592055L;

            public void actionPerformed(ActionEvent e) {
                if (_editFrame == null) {
                    _editFrame = new DrawEllipse("editShape", "ellipse", null);
                    setEditParams();
                }
            }
        });
        return true;
    }
}
