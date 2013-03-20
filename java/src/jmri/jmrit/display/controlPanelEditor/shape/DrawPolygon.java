package jmri.jmrit.display.controlPanelEditor.shape;

import org.slf4j.Logger;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import java.util.ArrayList;

//import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.Editor;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2013
 * @version $Revision: 1 $
 * 
 */

public class DrawPolygon extends DrawFrame{
	
	ArrayList <Point> 		_vertex;
	ArrayList <Rectangle>	_hitVertex;
	int 	_curX;
	int 	_curY;
	int		_curVertexIdx = -1;
	boolean _editing;
	
	public DrawPolygon(String which, String title, ShapeDrawer parent) {
		super(which, title, parent);
		_vertex = new ArrayList <Point>();
		_editing = false;
	}
	
	public DrawPolygon(Editor ed, String title, PositionableShape ps) {
		super("editShape", title, null);
		_vertex = new ArrayList <Point>();
		_hitVertex = new ArrayList <Rectangle>();
		_editing = true;
		_pShape = ps;
		Point loc = ps.getLocation();
    	PathIterator iter = ps.getPathIterator(null);
        float[] coord = new float[6];
    	while (!iter.isDone()) {
     		/*int type = */iter.currentSegment(coord);
       		Point pt = new Point(Math.round(coord[0])+loc.x, Math.round(coord[1])+loc.y);
			_vertex.add(pt);
			_hitVertex.add(new Rectangle(pt.x-NEAR, pt.y-NEAR, 2*NEAR, 2*NEAR));
			iter.next();
    	}
		ps.setVisible(false);
    	((jmri.jmrit.display.controlPanelEditor.ControlPanelEditor)ed).setDrawFrame(this);
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
	
	protected boolean dragTo(int x, int y) {
		if (_editing) {
			_curX = x;
			_curY = y;
			return true;
		}
		return false;
	}
	
	protected void anchorPoint(int x, int y) {
		Point anchorPt = new Point(x, y);
		for (int i=0; i<_vertex.size(); i++) {
			if (near(_vertex.get(i), anchorPt)) {
				_curVertexIdx = i; 
				_curX = x;
				_curY = y;
				return;
			}
		}
		_curVertexIdx = -1;
	}
	
	protected void drawShape(Graphics g) {
    	if (_editing) {
    		editShape(g);
    	} else {
    		makeNewShape(g);
    	}
	}
	
	private void makeNewShape(Graphics g) {
		if (_vertex.size()==0) {
			return;
		}
        Graphics2D g2d = (Graphics2D)g;
        _lineWidth = _lineSlider.getValue();
        BasicStroke stroke = new BasicStroke(_lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f);
        g2d.setColor(_lineColor);
        g2d.setStroke(stroke);
        GeneralPath path = makePath(new Point(0,0));
        path.lineTo(_curX, _curY);
        g2d.draw(path);
	}
	
	private void editShape(Graphics g) {
		if (_vertex.size()==0) {
			return;
		}
		Point pt = new Point(_curX, _curY);
		if (_curVertexIdx>=0) {
			_vertex.remove(_curVertexIdx);
			_hitVertex.remove(_curVertexIdx);
			_vertex.add(_curVertexIdx, pt);
			_hitVertex.add(new Rectangle(_curX-NEAR, _curY-NEAR, 2*NEAR, 2*NEAR));
		}		
        Graphics2D g2d = (Graphics2D)g;
        BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f);
        g2d.setColor(_lineColor);
        g2d.setStroke(stroke);
//		Point loc = _pShape.getLocation();
//		Point spt = new Point(-loc.x, -loc.y);
        GeneralPath path = makePath(new Point(0,0));
    	for (int i=0; i<_hitVertex.size(); i++) {
    		Rectangle r = _hitVertex.get(i);
            g2d.drawRect(r.x, r.y, r.width, r.height);
    	}
        g2d.draw(path);
//    	g2d.translate(loc.x, loc.y);
	}
	
    /**
     * Create a new PositionableShape 
     */
    protected boolean makeFigure() {
    	if (_editing) {
    		return editFigure();
    	} else {
    		return makeNewFigure();
    	}
    }
    
    private boolean makeNewFigure() {
    	Editor ed = _parent.getEditor();
    	Point p = new Point(ed.getLastX(), ed.getLastY());
    	if (hitPolygonVertex(p)) {
    		if (near(_vertex.get(0), p)) {
    	    	_vertex.add(p);	// close polygon    			
    		}
    		Point spt = getStartPoint();
        	PositionablePolygon ps = new PositionablePolygon(ed, makePath(spt));
        	ps.setLocation(spt);
        	ps.setDisplayLevel(Editor.MARKERS);
    		setPositionableParams(ps);
            ps.updateSize();
        	ed.putItem(ps);
    		return true;
    	}
    	_vertex.add(p);
    	return false;
	}
    
    private boolean editFigure() {
    	Editor ed = _parent.getEditor();
    	Point p = new Point(ed.getLastX(), ed.getLastY());
    	if (hitPolygonVertex(p)) {
    		
    	}
    	return false;
    }

    /**
     * @param editing
     * @return
     * When polygon is not editing, setLocation will position the start point
     */
    private GeneralPath makePath(Point pt) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, _vertex.size()+1);
        path.moveTo(_vertex.get(0).x-pt.x, _vertex.get(0).y-pt.y);      	
        for (int i=1; i<_vertex.size(); i++) {
        	path.lineTo(_vertex.get(i).x-pt.x, _vertex.get(i).y-pt.y);
        }
    	return path;
    }
    
    private Point getStartPoint() {
    	int x = _vertex.get(0).x;
    	int y = _vertex.get(0).y;
        for (int i=1; i<_vertex.size(); i++) {
        	x = Math.min(x, _vertex.get(i).x);
        	y = Math.min(y, _vertex.get(i).y);
        }
    	Point p = new Point(x, y);
    	return p;
    }
    
    private boolean hitPolygonVertex(Point p) {
    	for (int i=0; i<_vertex.size(); i++) {
    		if (near(_vertex.get(i), p)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private static int NEAR = 4;
    private boolean near(Point p1, Point p2) {
    	if (Math.abs(p1.x-p2.x)< NEAR && Math.abs(p1.y-p2.y)<NEAR) {
    		return true;
    	}
    	return false;
    }

    /**
     * Set parameters on a new or updated PositionableShape
     *
    protected void setPositionableParams(PositionableShape p) {
    	super.setPositionableParams(p);
    	PositionablePolygon pos = (PositionablePolygon)p;
	}

    /**
     * Set parameters on the popup that will edit the PositionableShape
     *
    protected void setDisplayParams(PositionableShape p) {
    	super.setDisplayParams(p);
    	PositionablePolygon pos = (PositionablePolygon)p;
    }

    /**
     * Editing is done.  Update the existing PositionableShape
     */
    protected void updateFigure(PositionableShape p) {
    	PositionablePolygon pos = (PositionablePolygon)p;
		Point spt = getStartPoint();
		pos.setShape(makePath(spt));
		setPositionableParams(pos);
    }
   
    static Logger log = LoggerFactory.getLogger(DrawPolygon.class.getName());
}
