package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

/**
 * @author Pete Cressman Copyright (c) 2012
 */
public class PositionableEllipse extends PositionableRectangle {

    public PositionableEllipse(Editor editor) {
        super(editor);
    }

    public PositionableEllipse(Editor editor, Shape shape) {
        super(editor, shape);
    }

    @Override
    protected Shape makeShape() {
        return new Ellipse2D.Double(0, 0, _width, _height);
    }

    @Override
    public Positionable deepClone() {
        PositionableEllipse pos = new PositionableEllipse(_editor);
        return finishClone(pos);
    }

    /*    protected Positionable finishClone(PositionableShape pos) {
        pos._width = _width;
        pos._height = _height;
        return super.finishClone(pos);
    }*/
    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("Ellipse"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeEditFrame(false);
            }
        });
        return true;
    }

    @Override
    protected DrawFrame makeEditFrame(boolean create) {
        _editFrame = new DrawEllipse("editShape", "Ellipse", this, getEditor(), create);
        _editFrame.setDisplayParams(this);
        return _editFrame;
    }

}