package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

/**
 * PositionableCircle PositionableShapes.
 *
 * @author Pete Cressman Copyright (c) 2012
 */
public class PositionableCircle extends PositionableShape {

    public PositionableCircle(Editor editor) {
        super(editor);
    }

    public PositionableCircle(Editor editor, Shape shape) {
        super(editor, shape);
    }

    @Override
    public void setHeight(int h) {
        super.setHeight(h);
        _width = _height;
    }

    @Override
    protected Shape makeShape() {
        return new Ellipse2D.Double(0, 0, _width, _width);
    }

    @Override
    public Positionable deepClone() {
        PositionableCircle pos = new PositionableCircle(_editor);
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
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("Circle"));
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
        _editFrame = new DrawCircle("editShape", "Circle", this, getEditor(), create);
        _editFrame.setDisplayParams(this);
        return _editFrame;
    }
        
}