package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

/**
 * PositionableRoundRect.
 * <P>
 * @author Pete cresman Copyright (c) 2012
 */
public class PositionableRectangle extends PositionableShape {

    public PositionableRectangle(Editor editor) {
        super(editor);
        makeShape();
    }

    public PositionableRectangle(Editor editor, Shape shape) {
        super(editor, shape);
    }

    /**
     * Make shape with new parameters
     */
    public void makeShape() {
        setShape(new Rectangle2D.Double(0, 0, _width, _height));
    }

    public Positionable deepClone() {
        PositionableRectangle pos = new PositionableRectangle(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableShape pos) {
        pos._width = _width;
        pos._height = _height;
        return super.finishClone(pos);
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("Rectangle"));
        popup.add(new javax.swing.AbstractAction(txt) {
            public void actionPerformed(ActionEvent e) {
                if (_editFrame == null) {
                    _editFrame = new DrawRectangle("editShape", "Rectangle", null);
                    setEditParams();
                }
            }
        });
        return true;
    }
}
