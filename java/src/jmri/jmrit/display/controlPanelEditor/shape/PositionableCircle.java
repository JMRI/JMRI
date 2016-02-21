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
 * @author Pete cresman Copyright (c) 2012
 * @version $Revision: 1 $
 */
public class PositionableCircle extends PositionableShape {

    /**
     *
     */
    private static final long serialVersionUID = -1016948514119727789L;

    public PositionableCircle(Editor editor) {
        super(editor);
        _height = _width = 100;
    }

    public PositionableCircle(Editor editor, Shape shape) {
        super(editor, shape);
        _height = _width = 100;
    }

    @Override
    public void setHeight(int h) {
        super.setHeight(h);
        _width = _height;
    }

    public void setRadius(int r) {
        setWidth(r);
    }

    public int getRadius() {
        return _width;
    }

    /**
     * this class must be overridden by its subclasses and executed only after
     * its parameters have been set
     */
    public void makeShape() {
        setShape(new Ellipse2D.Double(0, 0, _width, _width));
    }

    public Positionable deepClone() {
        PositionableCircle pos = new PositionableCircle(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        PositionableCircle pos = (PositionableCircle) p;
        pos._width = _width;
        return super.finishClone(pos);
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("circle"));
        popup.add(new javax.swing.AbstractAction(txt) {
            /**
             *
             */
            private static final long serialVersionUID = 4014472244175717813L;

            public void actionPerformed(ActionEvent e) {
                if (_editFrame == null) {
                    _editFrame = new DrawCircle("editShape", "circle", null);
                    setEditParams();
                }
            }
        });
        return true;
    }
}
