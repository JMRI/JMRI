package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

/**
 * PositionableRoundRect adds corner radii to PositionableShapes.
 * <P>
 * @author Pete cresman Copyright (c) 2012
 */
public class PositionableRoundRect extends PositionableRectangle {

    /**
     *
     */
    private static final long serialVersionUID = 8833172771959196146L;
    protected int _radius = 10;

    public PositionableRoundRect(Editor editor) {
        super(editor);
    }

    public PositionableRoundRect(Editor editor, Shape shape) {
        super(editor, shape);
    }

    public void setCornerRadius(int r) {
        _radius = r;
    }

    public int getCornerRadius() {
        return _radius;
    }

    /**
     * this class must be overridden by its subclasses and executed only after
     * its parameters have been set
     */
    public void makeShape() {
        setShape(new RoundRectangle2D.Double(0, 0, _width, _height, _radius, _radius));
    }

    @Override
    public Positionable deepClone() {
        PositionableRoundRect pos = new PositionableRoundRect(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableRoundRect pos) {
        pos._radius = _radius;
        return super.finishClone(pos);
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("roundRect"));
        popup.add(new javax.swing.AbstractAction(txt) {
            /**
             *
             */
            private static final long serialVersionUID = -1360281223318437388L;

            public void actionPerformed(ActionEvent e) {
                if (_editFrame == null) {
                    _editFrame = new DrawRoundRect("editShape", "roundRect", null);
                    setEditParams();
                }
            }
        });
        return true;
    }
}
