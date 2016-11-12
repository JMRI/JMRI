package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

/**
 * PositionableCircle PositionableShapes.
 * <P>
 * @author Pete Cressman Copyright (c) 2012
 */
public class PositionableCircle extends PositionableShape {

    public PositionableCircle(Editor editor) {
        super(editor);
        makeShape();
    }

    public PositionableCircle(Editor editor, Shape shape) {
        super(editor, shape);
    }

    public void setHeight(int h) {
        super.setHeight(h);
        _width = _height;
    }

    /**
     * this class must be overridden by its subclasses and executed only after
     * its parameters have been set
     */
    public void makeShape() {
        setShape(new Ellipse2D.Double(0, 0, _width, _width));
    }

    @Override
    public Positionable deepClone() {
        PositionableCircle pos = new PositionableCircle(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableShape pos) {
        pos._width = _width;
        pos._height = _height;
        return super.finishClone(pos);
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("Circle"));
        popup.add(new javax.swing.AbstractAction(txt) {
            public void actionPerformed(ActionEvent e) {
                if (_editFrame == null) {
                    _editFrame = new DrawCircle("editShape", "Circle", null);
                    setEditParams();
                }
            }
        });
        return true;
    }
}
