package jmri.jmrit.display.controlPanelEditor.shape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.jmrit.display.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
    protected int	_degrees;
    protected AffineTransform _transform;
    private NamedBeanHandle<Sensor> _controlSensor = null;
    
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
 		getShapeSize();
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

    float _sW;
    float _sH;
    void getShapeSize() {
    	float[] coord = new float[6];
    	float wMin = Float.MAX_VALUE;
    	float wMax = Float.MIN_VALUE;
    	float hMin = Float.MAX_VALUE;
    	float hMax = Float.MIN_VALUE;
    	PathIterator iter = _shape.getPathIterator(null, 5.0);	// flat
    	while (!iter.isDone()) {
    		int type = iter.currentSegment(coord);
    		if (type!=PathIterator.SEG_CLOSE) {
    	    	wMin = Math.min(wMin, coord[0]);
    	    	wMax = Math.max(wMax, coord[0]);
    	    	hMin = Math.min(hMin, coord[1]);
    	    	hMax = Math.max(hMax, coord[1]);
    		}
    		iter.next();    		    		
    	}
    	_sW = wMax-wMin;
    	_sH = hMax-hMin;
    }
    /**
     * !!! TODO fix so rotation image matches bound rectangle
     * Rotate shape 
     */
    public void rotate(int deg) {
    	_degrees = deg%360;
    	if (_degrees==0) {
    		_transform = null;
     	} else {
     		float w = _sW;
     		float h = _sH;
            double rad = _degrees*Math.PI/180.0;
            if (0<=_degrees && _degrees<90 || -360<_degrees && _degrees<=-270){
            	_transform = AffineTransform.getTranslateInstance(h*Math.sin(rad), 0.0);
            } else if (90<=_degrees && _degrees<180 || -270<_degrees && _degrees<=-180) {
            	_transform = AffineTransform.getTranslateInstance(h*Math.sin(rad)-w*Math.cos(rad), -h*Math.cos(rad));
            } else if (180<=_degrees && _degrees<270 || -180<_degrees && _degrees<=-90) {
            	_transform = AffineTransform.getTranslateInstance(-w*Math.cos(rad), -w*Math.sin(rad)-h*Math.cos(rad));
            } else {
            	_transform = AffineTransform.getTranslateInstance(0.0, -w*Math.sin(rad));
            }
            AffineTransform r = AffineTransform.getRotateInstance(rad);
            _transform.concatenate(r);          
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
        pos.makeShape();
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
    	r = new Rectangle(r.x-_lineWidth/2, r.y-_lineWidth/2, r.width+_lineWidth, r.height+_lineWidth);
    	if (_transform!=null) {
    		float[] pts = new float[8];
    		pts[0] = r.x; 
    		pts[1] = r.y; 
    		pts[2] = r.x+r.width; 
    		pts[3] = r.y; 
    		pts[4] = r.x+r.width; 
    		pts[5] = r.y+r.height; 
    		pts[6] = r.x; 
    		pts[7] = r.y+r.height;
    		_transform.transform(pts, 0, pts, 0, 4);
    		float minX = pts[0];
    		float maxX = pts[0];
    		float minY = pts[1];
    		float maxY = pts[1];
    		for (int i=2; i<pts.length; i+=2) {
    			minX = Math.min(minX, pts[i]);
    			maxX = Math.max(maxX, pts[i]);
    			minY = Math.min(minY, pts[i+1]);
    			maxY = Math.max(maxY, pts[i+1]);
    		}
        	r = new Rectangle(Math.round(minX), Math.round(minY), Math.round(maxX-minX), Math.round(maxY-minY));
    	}
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
        makeShape();
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
     
    static Logger log = LoggerFactory.getLogger(PositionableShape.class.getName());
}