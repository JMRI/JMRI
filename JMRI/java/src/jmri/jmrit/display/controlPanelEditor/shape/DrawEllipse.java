package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import jmri.jmrit.display.Editor;

/**
 * @author Pete Cressman Copyright (c) 2012
 */
public class DrawEllipse extends DrawRectangle {

    public DrawEllipse(String which, String title, PositionableShape ps) {
        super(which, title, ps);
    }

    @Override
    protected void makeFigure(MouseEvent event, Editor ed) {
        Rectangle r = ed.getSelectRect();
        if (r != null) {
            _width = r.width;
            _height = r.height;
            Ellipse2D.Double rr = new Ellipse2D.Double(0, 0, _width, _height);
            _shape = new PositionableEllipse(ed, rr);
            _shape.setLocation(r.x, r.y);
            _shape.updateSize();
            _shape.setEditFrame(this);
            setDisplayParams();
            ed.putItem(_shape);
        }
    }

    /**
     * Set parameters on the contextual menu that will edit the
     * PositionableShape.
     *
     * @param p the shape to be edited
     *
    @Override
    protected void setDisplayParams(PositionableShape p) {
        super.setDisplayParams(p);
        _width = p.getWidth();
        _height = p.getHeight();
    }
*/
}
