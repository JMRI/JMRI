package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import jmri.jmrit.display.Editor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * @author Pete Cressman Copyright: Copyright (c) 2013
 *
 */
public class DrawPolygon extends DrawFrame {

    /**
     *
     */
    private static final long serialVersionUID = -8879310189293502065L;
    ArrayList<Point> _vertices;
    int _curX;
    int _curY;
    int _curVertexIdx = -1;
    private static final int NEAR = PositionableShape.SIZE;
    PositionablePolygon _pShape;

    public DrawPolygon(String which, String title, ShapeDrawer parent) {
        super(which, title, parent);
        _vertices = new ArrayList<Point>();
    }

    public DrawPolygon(Editor ed, String title, PositionablePolygon ps) {
        super("editShape", title, null);
        _vertices = new ArrayList<Point>();
        _pShape = ps;
        _pShape.editing(true);
        _parent = ((jmri.jmrit.display.controlPanelEditor.ControlPanelEditor) ed).getShapeDrawer();
        _parent.setDrawFrame(this);
        int x = getX();
        int y = getY();
        PathIterator iter = ps.getPathIterator(null);
        float[] coord = new float[6];
        while (!iter.isDone()) {
            iter.currentSegment(coord);
            _vertices.add(new Point(x + Math.round(coord[0]), y + Math.round(coord[1])));
            iter.next();
        }
        _pShape.drawHandles();
    }

    @Override
    protected void closingEvent() {
        if (_pShape != null) {
            _parent.setDrawFrame(null);
            _pShape.editing(false);
        }
        super.closingEvent();
        repaint();
    }

    /*
     * Rubber Band line
     * @see jmri.jmrit.display.controlPanelEditor.shape.DrawFrame#drawLine(int, int)
     */
    protected void moveTo(int x, int y) {
        if (!_editing) {
            _curX = x;
            _curY = y;
        }
    }

    protected void anchorPoint(int x, int y) {
        _curVertexIdx = -1;
        Point anchorPt = new Point(x, y);
        for (int i = 0; i < _vertices.size(); i++) {
            if (near(_vertices.get(i), anchorPt)) {
                _curVertexIdx = i;
                _curX = x;
                _curY = y;
                return;
            }
        }
    }

    protected void drawShape(Graphics g) {
        if (!_editing) {
            if (_vertices.size() == 0) {
                return;
            }
            Graphics2D g2d = (Graphics2D) g;
            _lineWidth = _lineSlider.getValue();
            BasicStroke stroke = new BasicStroke(_lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f);
            g2d.setColor(_lineColor);
            g2d.setStroke(stroke);
            GeneralPath path = makePath(new Point(0, 0));
            path.lineTo(_curX, _curY);
            g2d.draw(path);
        }
    }

    /*	@Override
     public void paint(Graphics g) {
     super.paint(g);
     if (_editing) {
     int hitIndex = _pShape.getHitIndex();
     if (hitIndex>=0) {
     super.paint(g);
     Graphics2D g2d = (Graphics2D)g;
     _lineWidth = _lineSlider.getValue();
     BasicStroke stroke = new BasicStroke(_lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f);
     g2d.setColor(_lineColor);
     g2d.setStroke(stroke);
     GeneralPath path = new GeneralPath();
     if (hitIndex==0) {
     Point p0 = _vertices.get(1);
     path.moveTo(p0.x, p0.y);
     path.lineTo(_curX, _curY);			
     } else if (hitIndex==_vertices.size()-1) {
     Point p0 = _vertices.get(hitIndex-1);
     path.moveTo(p0.x, p0.y);
     path.lineTo(_curX, _curY);						
     } else {
     Point p0 = _vertices.get(hitIndex-1);
     Point p1 = _vertices.get(hitIndex+1);
     path.moveTo(p0.x, p0.y);
     path.lineTo(_curX, _curY);			
     path.lineTo(p1.x, p1.y);						
     }
     g2d.draw(path);    		    			
     }
     }
     }		
     /**
     * Create a new PositionableShape 
     */
    @Override
    protected boolean makeFigure(MouseEvent event) {
        if (_editing) {
            int hitIndex = _pShape.getHitIndex();
            if (hitIndex >= 0) {
                Point pt;
                try {
                    pt = _pShape.getInversePoint(event.getX(), event.getY());
                } catch (java.awt.geom.NoninvertibleTransformException nte) {
                    log.error("Can't locate Hit Rectangles " + nte.getMessage());
                    return false;
                }
                _vertices.remove(hitIndex);
                _vertices.add(hitIndex, pt);
                _pShape.setShape(makePath(getStartPoint()));

            }
            return false;
        } else {
            Point p = new Point(event.getX(), event.getY());
            if (hitPolygonVertex(p)) {
                if (near(_vertices.get(0), p)) {
                    _vertices.add(p);	// close polygon    			
                }
                Editor ed = _parent.getEditor();
                Point spt = getStartPoint();
                PositionablePolygon ps = new PositionablePolygon(ed, makePath(spt));
                ps.setLocation(spt);
                ps.setDisplayLevel(Editor.MARKERS);
                setPositionableParams(ps);
                ps.updateSize();
                ed.putItem(ps);
                return true;
            }
            _vertices.add(p);
            return false;
        }
    }

    protected boolean doHandleMove(int hitIndex, Point pt) {
        Point p = _vertices.get(hitIndex);
        p.x += pt.x;
        p.y += pt.y;
        _pShape.setShape(makePath(getStartPoint()));
        return false;
    }

    /**
     * @param pt is "startPoint" the upper left corner of the figure
     */
    private GeneralPath makePath(Point pt) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, _vertices.size() + 1);
        path.moveTo(_vertices.get(0).x - pt.x, _vertices.get(0).y - pt.y);
        for (int i = 1; i < _vertices.size(); i++) {
            path.lineTo(_vertices.get(i).x - pt.x, _vertices.get(i).y - pt.y);
        }
        return path;
    }

    /**
     * "startPoint" will be the upper left corner of the figure
     *
     */
    private Point getStartPoint() {
        int x = _vertices.get(0).x;
        int y = _vertices.get(0).y;
        for (int i = 1; i < _vertices.size(); i++) {
            x = Math.min(x, _vertices.get(i).x);
            y = Math.min(y, _vertices.get(i).y);
        }
        Point p = new Point(x, y);
        return p;
    }

    private boolean hitPolygonVertex(Point p) {
        for (int i = 0; i < _vertices.size(); i++) {
            if (near(_vertices.get(i), p)) {
                return true;
            }
        }
        return false;
    }

    static private boolean near(Point p1, Point p2) {
        if (Math.abs(p1.x - p2.x) < NEAR && Math.abs(p1.y - p2.y) < NEAR) {
            return true;
        }
        return false;
    }

    /**
     * Editing is done. Update the existing PositionableShape
     */
    @Override
    protected void updateFigure(PositionableShape p) {
        PositionablePolygon pos = (PositionablePolygon) p;
        _editing = false;
        _pShape.editing(false);
        setPositionableParams(pos);
    }

    protected void addVertex(boolean up) {
        if (_editing) {
            int hitIndex = _pShape.getHitIndex();
            Point r1 = _vertices.get(hitIndex);
            Point newVertex;
            if (up) {
                if (hitIndex == _vertices.size() - 1) {
                    newVertex = new Point(r1.x + 20, r1.y + 20);
                } else if (hitIndex >= 0) {
                    Point r2 = _vertices.get(hitIndex + 1);
                    newVertex = new Point((r1.x + r2.x) / 2, (r1.y + r2.y) / 2);
                } else {
                    return;
                }
                _pShape._hitIndex++;
            } else {
                if (hitIndex > 0) {
                    Point r2 = _vertices.get(hitIndex - 1);
                    newVertex = new Point((r1.x + r2.x) / 2, (r1.y + r2.y) / 2);
                } else if (hitIndex == 0) {
                    newVertex = new Point(r1.x + 20, r1.y + 20);
                } else {
                    return;
                }
            }
            _vertices.add(_pShape.getHitIndex(), newVertex);
            _pShape.setShape(makePath(getStartPoint()));
            _pShape.drawHandles();
        }
    }

    protected void deleteVertex() {
        if (_editing) {
            int hitIndex = _pShape.getHitIndex();
            if (hitIndex < 0) {
                return;
            }
            _vertices.remove(hitIndex);
            _pShape._hitIndex--;
            _pShape.setShape(makePath(getStartPoint()));
            _pShape.drawHandles();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DrawPolygon.class.getName());
}
