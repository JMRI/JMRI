package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

/**
 * PositionableRoundRect adds corner radii to PositionableShapes.
 *
 * @author Pete cresman Copyright (c) 2012
 */
public class PositionableRoundRect extends PositionableRectangle {

    protected int _radius = 40;

    public PositionableRoundRect(Editor editor) {
        super(editor);
    }

    public PositionableRoundRect(Editor editor, Shape shape) {
        super(editor, shape);
    }

    public void setCornerRadius(int r) {
        _radius = r;
        invalidateShape();
    }

    public int getCornerRadius() {
        return _radius;
    }

    @Override
    protected Shape makeShape() {
        return new RoundRectangle2D.Double(0, 0, _width, _height, _radius, _radius);
    }

    @Override
    public Positionable deepClone() {
        PositionableRoundRect pos = new PositionableRoundRect(_editor);
        pos._radius = _radius;
        return finishClone(pos);
    }

    @Override
    protected Positionable finishClone(PositionableShape pos) {
        return super.finishClone(pos);
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("roundRect"));
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
        _editFrame = new DrawRoundRect("editShape", "roundRect", this, getEditor(), create);
        _editFrame.setDisplayParams(this);
        return _editFrame;
    }
}