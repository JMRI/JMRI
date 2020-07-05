package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import jmri.jmrit.display.Editor;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * @author Pete Cressman Copyright (c) 2012
 */
@API(status = MAINTAINED)
public class DrawEllipse extends DrawRectangle {

    public DrawEllipse(String which, String title, PositionableShape ps, Editor ed, boolean create) {
        super(which, title, ps, ed, create);
    }

    @Override
    protected PositionableShape makeFigure(Rectangle r, Editor ed) {
        if (r != null) {
            _width = r.width;
            _height = r.height;
            Ellipse2D.Double rr = new Ellipse2D.Double(0, 0, _width, _height);
            _shape = new PositionableEllipse(ed, rr);
        }
        return _shape;
    }
}
