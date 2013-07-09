package jmri.jmrit.display.controlPanelEditor.shape;

import org.slf4j.Logger;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPopupMenu;
import org.slf4j.LoggerFactory;

/**
 * PositionableRoundRect.
 * <P>
 * @author Pete cresman Copyright (c) 2013
 * @version $Revision: 1 $
 */

public class PositionablePolygon extends PositionableShape {
	
//	ArrayList <Point> 		_vertices;
	ArrayList<Rectangle>	_vertexHandles;
	boolean _editing = false;

    public PositionablePolygon(Editor editor) {
    	super(editor);
    }

    public PositionablePolygon(Editor editor, Shape shape) {
       	super(editor, shape);
    }

    public Positionable deepClone() {
    	PositionablePolygon pos = new PositionablePolygon(_editor);
        return finishClone(pos);
    }
    
    public Positionable finishClone(Positionable pg) {
    	PositionablePolygon pos = (PositionablePolygon)pg;
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        path.append(getPathIterator(null), false);
        /*
    	PathIterator iter = _shape.getPathIterator(null);
        float[] coord = new float[6];
    	while (!iter.isDone()) {
    		int type = iter.currentSegment(coord);
    		switch (type) {
    		case PathIterator.SEG_MOVETO:
    			path.moveTo(coord[0], coord[1]);
    			break;
    		case PathIterator.SEG_LINETO:
    			path.lineTo(coord[0], coord[1]);
    			break;
    		case PathIterator.SEG_QUADTO:
    			path.quadTo(coord[0], coord[1], coord[2], coord[3]);
    			break;
    		case PathIterator.SEG_CUBICTO:
    			path.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[53]);
    			break;
    		case PathIterator.SEG_CLOSE:
    			path.closePath();
    			break;
    		}
    	}
    	*/
    	pos.setShape(path);
        return super.finishClone(pos);
    }
    
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("polygon"));
        popup.add(new javax.swing.AbstractAction(txt) {
        	PositionablePolygon ps;
                public void actionPerformed(ActionEvent e) {
                	_editFrame = new DrawPolygon(getEditor(), "polygon", ps);
                	setEditParams();               	
                }
                javax.swing.AbstractAction init(PositionablePolygon p) {
                	ps = p;
                	return this;
                }
            }.init(this));
        return true;
    }
    /*
	protected boolean dragTo(int x, int y) {
		if (_editing) {
			_curX = x;
			_curY = y;
			return true;
		}
		return false;
	}
*/
    protected void editing(boolean edit) {
    	_editing = edit;
    }
    
    protected int getHitIndex() {
    	return _hitIndex;
    }

    @Override
    protected void removeHandles() {
    	_vertexHandles = null;
      	super.removeHandles();    		
   }
   
    protected void drawHandles() {
    	if (_editing) {
    		_vertexHandles = new ArrayList<Rectangle>();
        	PathIterator iter = getPathIterator(null);
            float[] coord = new float[6];
        	while (!iter.isDone()) {
         		iter.currentSegment(coord);
           		int x = Math.round(coord[0]);
           		int y = Math.round(coord[1]);
           		_vertexHandles.add(new Rectangle(x-SIZE/2, y-SIZE/2, SIZE, SIZE));
    			iter.next();
        	}   	
    	} else {
    		super.drawHandles();
    	}
    }
    
    @Override
    public void doMousePressed(MouseEvent event) {
    	_hitIndex=-1;
    	if (!_editor.isEditable()) {
    		return;
    	}
    	if (_editing) {
        	if (_vertexHandles!=null) {   		
         	   	 _lastX = event.getX();
          	   	 _lastY = event.getY();
          	   	 int x = _lastX-getX();//-SIZE/2;
          	   	 int y = _lastY- getY();//-SIZE/2;
           	   	 Point pt;
           	   	 try {
               	   	 pt = getInversePoint(x, y);       	   		 
           	   	 } catch (java.awt.geom.NoninvertibleTransformException nte) {
       	   			 log.error("Can't locate Hit Rectangles "+nte.getMessage());
       	   			 return;
           	   	 }
          	   	 for (int i=0; i<_vertexHandles.size(); i++) {
          	   		 if (_vertexHandles.get(i).contains(pt.x, pt.y)) {
          	   			 _hitIndex=i;
          	   		 }       	   		 
          	   	 }
        	}
    	} else {
    		super.doMousePressed(event);
    	}
    }
    
    @Override
    protected boolean doHandleMove(MouseEvent event) {
    	if (_hitIndex>=0 && _editor.isEditable()) {
        	if (_editing) {
        		Point pt = new Point(event.getX()-_lastX, event.getY()-_lastY);
/*        		try {
            		pt = getInversePoint(event.getX()-_lastX, event.getY()-_lastY);
        		} catch (java.awt.geom.NoninvertibleTransformException nte) {
      	   			 log.error("Can't locate Hit Rectangles "+nte.getMessage());
      	   			 return false;
        		}*/
        		Rectangle rect = _vertexHandles.get(_hitIndex);
        		rect.x += pt.x;
        		rect.y += pt.y;
        		if (_editFrame!=null) {
        			((DrawPolygon)_editFrame).doHandleMove(_hitIndex, pt);
        		}
    	        _lastX = event.getX();
    	        _lastY = event.getY();
        	} else {
    	        float deltaX = event.getX() - _lastX;
    	        float deltaY = event.getY() - _lastY;
                float width = _width;
                float height = _height;
                if (_height<SIZE || _width<SIZE) {
                	log.error("Bad size _width= "+_width+", _height= "+_height);
                }
                GeneralPath path = null;
            	switch (_hitIndex) {
        			case TOP:
        				if (height-deltaY > SIZE) {
        					path = scale(1, (height-deltaY)/height);
            				_editor.moveItem(this, 0, (int)deltaY);    					
        				} else {
        					path = scale(1, SIZE/height);
            				_editor.moveItem(this, 0, _height-SIZE);    					
        				}
        				break;
            		case RIGHT:
            			path = scale(Math.max(SIZE/width, (width+deltaX)/width), 1);
            			break;
            		case BOTTOM:
            			path = scale(1, Math.max(SIZE/height, (height+deltaY)/height));
            			break;
            		case LEFT:
            			if (_width-deltaX > SIZE) {
        					path = scale((width-deltaX)/width, 1);
            				_editor.moveItem(this, (int)deltaX, 0);        				
            			} else {
        					path = scale(SIZE/width, 1);
            				_editor.moveItem(this, _width-SIZE, 0);        				
            			}
            			break;
            	}
            	if (path!=null) {
            		setShape(path);
            	}
        	}
            drawHandles();
            repaint();
            updateSize();
            _lastX = event.getX();
            _lastY = event.getY();   	 
        	return true;
    	}
        return false;
    }
    
    private GeneralPath scale(float ratioX, float ratioY) {
//    	log.info("scale("+ratioX+" , "+ratioY+")");
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
    	PathIterator iter = getPathIterator(null);
        float[] coord = new float[6];
    	while (!iter.isDone()) {
    		int type = iter.currentSegment(coord);
    		switch (type) {
    		case PathIterator.SEG_MOVETO:
    			path.moveTo(coord[0]*ratioX, coord[1]*ratioY);
    			break;
    		case PathIterator.SEG_LINETO:
    			path.lineTo(coord[0]*ratioX, coord[1]*ratioY);
    			break;
    		case PathIterator.SEG_QUADTO:
    			path.quadTo(coord[0], coord[1], coord[2], coord[3]);
    			break;
    		case PathIterator.SEG_CUBICTO:
    			path.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[53]);
    			break;
    		case PathIterator.SEG_CLOSE:
    			path.closePath();
    			break;
    		}
//    		log.debug("type= "+type+"  x= "+coord[0]+", y= "+ coord[1]);
    		iter.next();
    	}
    	return path;
    }
    
    
    @Override
    protected void paintHandles(Graphics2D g2d) {
    	if (_editing) {
            if (_vertexHandles!=null) {
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                Iterator<Rectangle> iter = _vertexHandles.iterator();
                while(iter.hasNext()) {
                	Rectangle rect = iter.next();
            		g2d.setColor(Color.BLUE);
            		g2d.fill(rect);
                    g2d.setColor(Editor.HIGHLIGHT_COLOR);
               		g2d.draw(rect);
                }
            }    		
    	} else {
        	super.paintHandles(g2d);    		
    	}
    }

    static Logger log = LoggerFactory.getLogger(PositionablePolygon.class.getName());
}
