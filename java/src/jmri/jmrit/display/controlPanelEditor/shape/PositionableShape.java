package jmri.jmrit.display.controlPanelEditor.shape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.jmrit.display.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
//import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * PositionableShape is item drawn by java.awt.Graphics2D.
 * <P>
 * @author Pete cresman Copyright (c) 2012
 * @version $Revision: 1 $
 */

public class PositionableShape extends PositionableJComponent 
					implements java.beans.PropertyChangeListener {

    private Shape	_shape;
    protected Color	_lineColor = Color.black;
    protected Color	_fillColor;
    protected int	_alpha = 255;
    protected int	_lineWidth = 1;
    private int	_degrees;
    protected AffineTransform _transform;
    private NamedBeanHandle<Sensor> _controlSensor = null;
    // GUI resizing params
    private Rectangle[] _handles;
    protected int _hitIndex = -1;
    protected int _lastX;
    protected int _lastY;
    // params for shape's bounding box
    protected int _width;
    protected int _height;
	static final int TOP	=0;
	static final int RIGHT	=1;
	static final int BOTTOM	=2;
	static final int LEFT	=3;    
    static final int SIZE = 10;
    
    public PositionableShape(Editor editor) {
    	super(editor);
    	setName("Graphic");
    }

    public PositionableShape(Editor editor, Shape shape) {
       	this(editor);
        _shape = shape;
    }
    
    public PathIterator getPathIterator(AffineTransform at) {
    	return _shape.getPathIterator(at);
    }
    
    protected void setShape(Shape s) {
    	_shape = s;
    }
    protected Shape getShape() {
    	return _shape;
    }
    public AffineTransform getTransform() {
    	return _transform;
    }
    public void setWidth(int w) {
    	_width = w;
    }
    public void setHeight(int h) {
    	_height = h;
    }
    @Override
    public int getHeight() {
      return _height;
    }

    @Override
    public int getWidth() {
      return _width;
    }

    /**
     * this class must be overridden by its subclasses and executed
     *  only after its parameters have been set
     */
    public void makeShape() {  	
    }

    public void setLineColor(Color c) {
    	if (c==null) {
    		c = Color.black;
    	}
    	_lineColor = c;
    }
    public Color getLineColor() {
    	return _lineColor;
    }

    public void setFillColor(Color c) {
    	if (c==null) {
    		_fillColor = null;
    	} else {
        	_fillColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), _alpha);    		
    	}
    }
    public Color getFillColor( ) {
    	return _fillColor;
    }
    
    public void setAlpha(int a) {
    	_alpha = a;
    }
    public int getAlpha() {
    	return _alpha;
    }

    public void setLineWidth(int w) {
    	_lineWidth = w;
    }
    public int getLineWidth() {
    	return _lineWidth;
    }

    /**
     * Rotate shape 
     */
    public void rotate(int deg) {
    	_degrees = deg%360;
    	if (_degrees==0) {
    		_transform = null;
     	} else {
            double rad = _degrees*Math.PI/180.0;
            _transform = new AffineTransform();
            _transform.setToRotation(rad, _width/2, _height/2);
    	}
    	updateSize();
    }

    public void paint(Graphics g) {
//		if (log.isDebugEnabled())
//			log.debug("PositionalShape Paint: " +this.getClass().getName()+" Hidden= "+isHidden());        
    	if (!getEditor().isEditable() && isHidden()) {
    		return;
    	}
        Graphics2D g2d = (Graphics2D)g;
        /*
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                             RenderingHints.VALUE_RENDER_QUALITY); 
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                             RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                             RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        */
        g2d.setClip(null);
        if (_transform!=null ) {
        	g2d.transform(_transform);
        }        
        if (_fillColor!=null) {
            g2d.setColor(_fillColor);
            g2d.fill(_shape);
        }
        BasicStroke stroke = new BasicStroke(_lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f);
        g2d.setColor(_lineColor);
        g2d.setStroke(stroke);
        g2d.draw(_shape);
        paintHandles(g2d);
    }
    
    protected void paintHandles(Graphics2D g2d) {
        if (_handles!=null) {
            g2d.setColor(Editor.HIGHLIGHT_COLOR);
            g2d.setStroke(new java.awt.BasicStroke(2.0f));
            Rectangle r = getBounds();
            r.x=0;
            r.y=0;
       		g2d.draw(r);
        	for (int i=0; i<_handles.length; i++) {
        		g2d.setColor(Color.RED);
        		g2d.fill(_handles[i]);
                g2d.setColor(Editor.HIGHLIGHT_COLOR);
           		g2d.draw(_handles[i]);
        	}
        }
    }

    public Positionable deepClone() {
        PositionableShape pos = new PositionableShape(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        PositionableShape pos = (PositionableShape)p;
        pos._alpha = _alpha; 
        pos._lineWidth = _lineWidth; 
        pos.setFillColor(_fillColor); 
        pos._lineColor = new Color(_lineColor.getRed(), _lineColor.getGreen(), _lineColor.getBlue());
//        pos.makeShape();
        pos.updateSize();
        return super.finishClone(pos);
    }
    
    public Dimension getSize(Dimension rv) {
    	return new Dimension(maxWidth(), maxHeight());	
    }
    
    public void updateSize() {
    	Rectangle r;
    	if (_shape!=null) {
        	r = _shape.getBounds();    		
    	} else {
        	r = super.getBounds();    		
    	}
    	_width = r.width;
    	_height = r.height;
        setSize(r.width, r.height);
    }
    
    public int maxWidth() {
       	return getSize().width;
    }
      
    public int maxHeight() {
    	return getSize().height;
    }

    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    /**
    * return true if popup is set
    */
    public boolean setRotateMenu(JPopupMenu popup) {
        if (getDisplayLevel() > Editor.BKG) {
            popup.add(CoordinateEdit.getRotateEditAction(this));
            return true;
        }
        return false;
    }

    public boolean setScaleMenu(JPopupMenu popup) {
        return false;
    }
    
    public int getDegrees() {
        return _degrees;
    }
    
    DrawFrame _editFrame;    
    public boolean setEditItemMenu(JPopupMenu popup) {
    	return false;
    }

    protected void setEditParams() {
        _editFrame.setDisplayParams(this);
        java.awt.Container contentPane = _editFrame.getContentPane();
        contentPane.add(_editFrame.makeParamsPanel());
        contentPane.add(makeDoneButtonPanel());
        _editFrame.pack();    	
    }
    
    protected JPanel makeDoneButtonPanel() {
        JPanel panel0 = new JPanel();
        panel0.setLayout(new FlowLayout());
        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	editItem();
                }
        });
        panel0.add(doneButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _editFrame.closingEvent();
                }
        });
        panel0.add(cancelButton);
        return panel0;
    }
    protected void editItem() {
        _editFrame.updateFigure(this);
//        makeShape();
        removeHandles();
        updateSize();
        _editFrame.closingEvent();    	
        repaint();
    }
    
	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getNameString() + " property " + evt.getPropertyName() + " is now "
					+ evt.getNewValue()+" from "+evt.getSource().getClass().getName());
        
        if (evt.getPropertyName().equals("KnownState")) {
            setHidden(((Integer)evt.getNewValue()).intValue()==Sensor.INACTIVE);
            setEditable(true);
            Rectangle bd = getBounds();
//            repaint(0, 0, 0, bd.width+2*_lineWidth, bd.height+2*_lineWidth);
            repaint(0, -_lineWidth, -_lineWidth, bd.width+2*_lineWidth, bd.height+2*_lineWidth);
        }
	}

    /**
     * Attach a named sensor to shape
     * @param pName Used as a system/user name to lookup the sensor object
     */
	public void setControlSensor(String pName) {
        if (pName==null || pName.trim().length()==0) {
            setControlSensorHandle(null);
            return;
        }
        if (InstanceManager.sensorManagerInstance()!=null) {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
            if (sensor != null) {
           	 setControlSensorHandle(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));                
            } else {
                log.error("PositionalShape Control Sensor '"+pName+"' not available, shape won't see changes");
            }
        } else {
            log.error("No SensorManager for this protocol, block icons won't see changes");
        }
    }
    public void setControlSensorHandle(NamedBeanHandle<Sensor> senHandle) {
        if (_controlSensor != null) {
       	 getControlSensor().removePropertyChangeListener(this);
        }
        _controlSensor = senHandle;
        if (_controlSensor != null) {
            Sensor sensor = getControlSensor();
            sensor.addPropertyChangeListener(this, _controlSensor.getName(), "PositionalShape");
            setHidden(sensor.getKnownState()==Sensor.INACTIVE);
        } 
    }
    public Sensor getControlSensor() {
        if (_controlSensor==null) {
            return null;
        }
        return _controlSensor.getBean(); 
    }    
    public NamedBeanHandle <Sensor> getControlSensorHandle() { return _controlSensor; }

    public void dispose() {
        if (_controlSensor != null) {
       	 getControlSensor().removePropertyChangeListener(this);
        }
        _controlSensor = null;
    }
    
    protected void removeHandles() {
      	 _handles = null;
       	 invalidate();
    }
    
    protected void drawHandles() {
    	_handles = new Rectangle[4];
    	_handles[TOP] = new Rectangle(_width/2-SIZE/2, 0, SIZE, SIZE);
    	_handles[RIGHT] = new Rectangle(_width-SIZE,_height/2-SIZE/2, SIZE, SIZE);
    	_handles[BOTTOM] = new Rectangle(_width/2-SIZE/2, _height-SIZE, SIZE, SIZE);
    	_handles[LEFT] = new Rectangle(0, _height/2-SIZE/2, SIZE, SIZE);
    }
    
    protected Point getInversePoint(int x, int y) throws java.awt.geom.NoninvertibleTransformException  {
 	   	 if (_transform!=null) {
 	   		 java.awt.geom.AffineTransform t = _transform.createInverse();
 	   		 float[] pt = new float[2];
 	   		 pt[0]=x;
 	   		 pt[1]=y;
 	   		 t.transform(pt, 0, pt, 0, 1);
 	   		 return new Point(Math.round(pt[0]), Math.round(pt[1]));
 	   	 }
 	   	 return new Point(x, y);
   }
   
    
    public void doMousePressed(MouseEvent event) {
    	_hitIndex=-1;
    	if (_handles!=null) {   		
      	   	 _lastX = event.getX();
       	   	 _lastY = event.getY();
       	   	 int x = _lastX-getX();
       	   	 int y = _lastY- getY();
       	   	 Point pt;
       	   	 try {
           	   	 pt = getInversePoint(x, y);       	   		 
       	   	 } catch (java.awt.geom.NoninvertibleTransformException nte) {
   	   			 log.error("Can't locate Hit Rectangles "+nte.getMessage());
   	   			 return;
       	   	 }
       	   	 for (int i=0; i<_handles.length; i++) {
       	   		 if (_handles[i].contains(pt.x, pt.y)) {
       	   			 _hitIndex=i;
       	   		 }       	   		 
       	   	 }
    	}
    }
   
    protected boolean doHandleMove(MouseEvent event) {
    	if (_hitIndex>=0) {
            int deltaX = event.getX() - _lastX;
            int deltaY = event.getY() - _lastY;
        	switch (_hitIndex) {
    			case TOP:
    				if (_height-deltaY > SIZE) {
    					setHeight(_height-deltaY);
        				_editor.moveItem(this, 0, deltaY);    					
    				} else {
    					setHeight(SIZE);
        				_editor.moveItem(this, 0, _height-SIZE);    					
    				}
    				break;
        		case RIGHT:
        			setWidth(Math.max(SIZE, _width+deltaX));
        			break;
        		case BOTTOM:
        			setHeight(Math.max(SIZE, _height+deltaY));
        			break;
        		case LEFT:
        			if (_width-deltaX > SIZE) {
        				setWidth(Math.max(SIZE, _width-deltaX));
        				_editor.moveItem(this, deltaX, 0);        				
        			} else {
        				setWidth(SIZE);
        				_editor.moveItem(this, _width-SIZE, 0);        				
        			}
        			break;
        	}
            makeShape();
            updateSize();
            drawHandles();
            repaint();
            _lastX = event.getX();
            _lastY = event.getY();   	 
            return true;
    	}
    	return false;
    }
    
    static Logger log = LoggerFactory.getLogger(PositionableShape.class.getName());
}