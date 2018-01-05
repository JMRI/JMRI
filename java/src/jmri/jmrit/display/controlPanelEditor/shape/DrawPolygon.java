package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.display.Editor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pete Cressman Copyright (c) 2013
 */
public class DrawPolygon extends DrawFrame {

    ArrayList<Point> _vertices;
    int _curX;
    int _curY;
    private static final int NEAR = PositionableShape.SIZE;
    private boolean _editing;

    public DrawPolygon(String which, String title, PositionableShape ps) {
        super(which, title, ps);
        _vertices = new ArrayList<>();
        _editing = (ps != null);
    }

    @Override
    protected JPanel makeParamsPanel() {
        JPanel panel = super.makeParamsPanel();
        int x = getX();
        int y = getY();
        PathIterator iter = _shape.getPathIterator(null);
        float[] coord = new float[6];
        if (_editing) {
            while (!iter.isDone()) {
                iter.currentSegment(coord);
                _vertices.add(new Point(x + Math.round(coord[0]), y + Math.round(coord[1])));
                iter.next();
            }            
        }
        _shape.editing(true);
        _shape.drawHandles();

        panel.add(Box.createVerticalGlue());
        panel.add(new JLabel(Bundle.getMessage("drawInstructions2c")));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        JButton addButton = new JButton(Bundle.getMessage("ButtonAddVertex"));
        addButton.addActionListener((ActionEvent a) -> {
            addVertex(false);
        });
        p.add(addButton);
        p.add(Box.createHorizontalGlue());

        JButton deleteButton = new JButton(Bundle.getMessage("ButtonDeleteVertex"));
        deleteButton.addActionListener((ActionEvent a) -> {
            deleteVertex();
        });
        p.add(deleteButton);
        p.add(Box.createHorizontalGlue());
        panel.add(p);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // double click was made - 
    protected void makeShape(MouseEvent event, Editor ed) {
/*        Point pt = new Point(event.getX(), event.getY());
        boolean closed;           Do this later
        if (near(_vertices.get(0), pt)) {
            closed = true; // close polygon
        } else {
            closed = false;
        }*/
        Point spt = getStartPoint();
        _shape = new PositionablePolygon(ed, makePath(spt));
        if (_vertices.size() < 2) {
            closingEvent(true);
            return;
        }
        _shape.setLocation(spt);
        _shape.updateSize();
        _shape.setEditFrame(this);
        setDisplayParams();
        ed.putItem(_shape);
        _editing = true;
        _shape._editing = true;
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
        _curX = x;
        _curY = y;
        Point anchorPt = new Point(x, y);
        for (int i = 0; i < _vertices.size(); i++) {
            if (near(_vertices.get(i), anchorPt)) {
                _curX = x;
                _curY = y;
                return;
            }
        }
    }

    protected void drawShape(Graphics g) {
        if (!_editing) {
            if (_vertices.size() < 1 || !(g instanceof Graphics2D)) {
                return;
            }
            Graphics2D g2d = (Graphics2D)g;
            // disabled to satisfy P Bender
            //_lineWidth = _lineSlider.getValue();
            //BasicStroke stroke = new BasicStroke(_lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f);
            //g2d.setColor(_lineColor);
            //g2d.setStroke(stroke);
            GeneralPath path = makePath(new Point(0, 0));
            path.lineTo(_curX, _curY);
            g2d.draw(path);
        }
    }

    @Override
    protected void makeFigure(MouseEvent event, Editor ed) {
        if (!_editing) {    // creating new polygon
             Point pt = new Point(event.getX(), event.getY());
             int size = _vertices.size();
             if ( size == 0 || !near(_vertices.get(size-1), pt)) {
                 _vertices.add(pt);
             }
        }
    }

    protected boolean doHandleMove(int hitIndex, Point pt) {
        Point p = _vertices.get(hitIndex);
        p.x += pt.x;
        p.y += pt.y;
        if (_editing) {
            _shape.setShape(makePath(getStartPoint()));            
        }
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
//        if (closed) {
//            path.lineTo(_vertices.get(0).x - pt.x, _vertices.get(0).y - pt.y);            
//        }
        return path;
    }

    /**
     * "startPoint" will be the upper left corner of the figure
     * <p>
     */
    private Point getStartPoint() {
        if (_vertices.size() > 0) {
            int x = _vertices.get(0).x;
            int y = _vertices.get(0).y;
            for (int i = 1; i < _vertices.size(); i++) {
                x = Math.min(x, _vertices.get(i).x);
                y = Math.min(y, _vertices.get(i).y);
            }
            return new Point(x, y);
        } else {
            _vertices.add( new Point(_curX, _curY));
            return new Point(_curX, _curY);
        }
    }

    static private boolean near(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) < NEAR && Math.abs(p1.y - p2.y) < NEAR;
    }

    @Override
    void setDisplayWidth(int w) {
    }

    @Override
    void setDisplayHeight(int h) {
    }

    /**
     *  Add a vertex to polygon relative to selected vertex
     * @param up - if true, add after selected vertex. otherwise before selected vertex
     */
    protected void addVertex(boolean up) {
        if (_editing) {
            int hitIndex = _shape._hitIndex;
            if (hitIndex < 0) {
                if (_vertices.size() > 1) {
                    hitIndex = _vertices.size() - 1;
                } else {
                    hitIndex = 0;
                    if (_vertices.size() == 0) {
                        _vertices.add(new Point(_curX, _curY));
                    }
                }
                _shape._hitIndex = hitIndex;
                up = true;
            }
            Point r1 = _vertices.get(hitIndex);
            Point newVertex;
            if (up) {
                if (hitIndex == _vertices.size() - 1) {
                    newVertex = new Point(r1.x + 20, r1.y + 20);
                } else {
                    Point r2 = _vertices.get(hitIndex + 1);
                    newVertex = new Point((r1.x + r2.x) / 2, (r1.y + r2.y) / 2);
                }
                _shape._hitIndex++;
                hitIndex++;
            } else {
               if (hitIndex > 0) {
                    Point r2 = _vertices.get(hitIndex - 1);
                    newVertex = new Point((r1.x + r2.x) / 2, (r1.y + r2.y) / 2);
                } else {
                    newVertex = new Point(r1.x + 20, r1.y + 20);
                }
            }
            _vertices.add(hitIndex, newVertex);
            _shape.setShape(makePath(getStartPoint()));
            _shape.drawHandles();
            _shape.updateSize();
        }
    }

    protected void deleteVertex() {
        if (_editing) {
            if (_vertices.size() <= 2) {
                return;
            }
            int hitIndex = _shape._hitIndex;
            if (hitIndex < 0) {
                hitIndex = _vertices.size() - 1;
            }
            _vertices.remove(hitIndex);
            if (hitIndex > 0) {
                _shape._hitIndex--;                
            }
            _shape.setShape(makePath(getStartPoint()));
            _shape.drawHandles();
            _shape.updateSize();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DrawPolygon.class);
}
