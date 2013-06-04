package jmri.jmrit.display.controlPanelEditor.shape;

import org.slf4j.Logger;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
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
	
	ArrayList <Point> 		_vertices;
	int 	_curX;
	int 	_curY;
	int		_curVertexIdx = -1;
	boolean _editing;
    private static final int NEAR = PositionableShape.SIZE;
	PositionablePolygon _pShape;
    private int _lastX;
    private int _lastY;
	
	public DrawPolygon(String which, String title, ShapeDrawer parent) {
		super(which, title, parent);
		_vertices = new ArrayList <Point>();
		_editing = false;
	}
	
	public DrawPolygon(Editor ed, String title, PositionablePolygon ps) {
		super("editShape", title, null);
		_vertices = new ArrayList <Point>();
		_editing = true;
		_pShape = ps;
    	_pShape.editing(true);
		int x = getX();
		int y = getY();
    	PathIterator iter = ps.getPathIterator(null);
        float[] coord = new float[6];
    	while (!iter.isDone()) {
     		iter.currentSegment(coord);
       		_vertices.add(new Point(x+Math.round(coord[0]), y+Math.round(coord[1])));
       		iter.next();
    	}
    	_pShape.drawHandles();
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
		Point anchorPt = new Point(x, y);
		for (int i=0; i<_vertices.size(); i++) {
			if (near(_vertices.get(i), anchorPt)) {
				_curVertexIdx = i; 
				_curX = x;
				_curY = y;
				return;
			}
		}
		_curVertexIdx = -1;
	}
	
	protected void drawShape(Graphics g) {
    	if (!_editing) {
    		if (_vertices.size()==0) {
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
	}
/*	
    public void paint(Graphics g) {
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
    protected boolean makeFigure(MouseEvent event) {
    	if (_editing) {
    		int hitIndex = _pShape.getHitIndex();
    		if (hitIndex>=0) {
        		Point pt;
        		try {
            		pt = _pShape.getInversePoint(event.getX(), event.getY());
        		} catch (java.awt.geom.NoninvertibleTransformException nte) {
      	   			 log.error("Can't locate Hit Rectangles "+nte.getMessage());
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
//    	_curX = p.x + pt.x;
//    	_curY = p.y + pt.y;
    	p.x += pt.x;
    	p.y += pt.y;
        _pShape.setShape(makePath(getStartPoint()));
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
     * @param pt is "startPoint" the upper left corner of the figure 
     * @return
      */
    private GeneralPath makePath(Point pt) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, _vertices.size()+1);
        path.moveTo(_vertices.get(0).x-pt.x, _vertices.get(0).y-pt.y);      	
        for (int i=1; i<_vertices.size(); i++) {
        	path.lineTo(_vertices.get(i).x-pt.x, _vertices.get(i).y-pt.y);
        }
    	return path;
    }

    /**
     * "startPoint" will be the upper left corner of the figure
     * @return
     */
    private Point getStartPoint() {
    	int x = _vertices.get(0).x;
    	int y = _vertices.get(0).y;
        for (int i=1; i<_vertices.size(); i++) {
        	x = Math.min(x, _vertices.get(i).x);
        	y = Math.min(y, _vertices.get(i).y);
        }
    	Point p = new Point(x, y);
    	return p;
    }
    
    private boolean hitPolygonVertex(Point p) {
    	for (int i=0; i<_vertices.size(); i++) {
    		if (near(_vertices.get(i), p)) {
    			return true;
    		}
    	}
    	return false;
    }
    
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
     * Done editing. Set parameters on the popup that will edit the PositionableShape
     *
    protected void setDisplayParams(PositionableShape p) {
    	super.setDisplayParams(p);
    	_editing = false;
    }

    /**
     * Editing is done.  Update the existing PositionableShape
     */
    protected void updateFigure(PositionableShape p) {
    	PositionablePolygon pos = (PositionablePolygon)p;
    	_editing = false;
    	_pShape.editing(false);
//		Point spt = getStartPoint();
//		pos.setShape(makePath(spt));
//		setPositionableParams(pos);
    }
   
    static Logger log = LoggerFactory.getLogger(DrawPolygon.class.getName());
}
