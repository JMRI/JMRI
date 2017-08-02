package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;

/**
 * <P>
 * @author Pete Cressman Copyright: Copyright (c) 2012
 *
 */
public class DrawEllipse extends DrawRectangle {

    public DrawEllipse(String which, String title, ShapeDrawer parent) {
        super(which, title, parent);
    }

    /**
     * Create a new PositionableShape
     */
    @Override
    protected boolean makeFigure(MouseEvent event) {
        ControlPanelEditor ed = _parent.getEditor();
        Rectangle r = ed.getSelectRect();
        if (r != null) {
            _width = r.width;
            _height = r.height;
            Ellipse2D.Double rr = new Ellipse2D.Double(0, 0, _width, _height);
            PositionableEllipse ps = new PositionableEllipse(ed, rr);
            ps.setLocation(r.x, r.y);
            ps.updateSize();
            setDisplayParams(ps);
            ps.setEditFrame(this);
            ed.putItem(ps);            
        }
        return true;
    }

    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
    @Override
    protected void setDisplayParams(PositionableShape p) {
        super.setDisplayParams(p);
        PositionableEllipse pos = (PositionableEllipse) p;
        _width = pos.getWidth();
        _height = pos.getHeight();
    }

}
