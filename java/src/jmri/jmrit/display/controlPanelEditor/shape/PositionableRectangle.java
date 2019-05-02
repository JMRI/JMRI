package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

/**
 * @author Pete cresman Copyright (c) 2012
 */
public class PositionableRectangle extends PositionableShape {

    public PositionableRectangle(Editor editor) {
        super(editor);
    }

    public PositionableRectangle(Editor editor, Shape shape) {
        super(editor, shape);
    }

    @Override
    protected Shape makeShape() {
        return new Rectangle2D.Double(0, 0, _width, _height);
    }

    @Override
    public Positionable deepClone() {
        PositionableRectangle pos = new PositionableRectangle(_editor);
        return finishClone(pos);
    }

    @Override
    protected Positionable finishClone(PositionableShape pos) {
        pos._width = _width;
        pos._height = _height;
        return super.finishClone(pos);
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("Rectangle"));
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
        _editFrame = new DrawRectangle("editShape", "Rectangle", this, getEditor(), create);
        _editFrame.setDisplayParams(this);
        return _editFrame;
    }
        
}