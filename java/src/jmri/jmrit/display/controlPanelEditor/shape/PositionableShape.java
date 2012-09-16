package jmri.jmrit.display.controlPanelEditor.shape;

import jmri.jmrit.display.*;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * PositionableShape is item drawn by ava.awt.Graphics2D.
 * <P>
 * @author Pete cresman Copyright (c) 2012
 * @version $Revision: 1 $
 */

public class PositionableShape extends PositionableJComponent {

    protected Shape	_shape;
    protected Color	_lineColor = Color.black;
    protected Color	_fillColor;
    protected int	_alpha = 255;
    protected int	_lineWidth = 1;
    protected int		_degrees;
    protected AffineTransform _transform;
    
    public static java.util.ResourceBundle rbcp = ControlPanelEditor.rbcp;
    
    public PositionableShape(Editor editor) {
    	super(editor);
    	setName("Graphic");
    	setOpaque(false);
    }

    public PositionableShape(Editor editor, Shape shape) {
       	this(editor);
        _shape = shape;
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
    	if (c==null || _alpha==0) {
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

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                             RenderingHints.VALUE_RENDER_QUALITY); 
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                             RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                             RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setClip(null);
        if (_transform!=null ) {
        	g2d.transform(_transform);
        }        
        if (_fillColor!=null) {
            g2d.setColor(_fillColor);
            g2d.fill(_shape);
        }
        g2d.setColor(_fillColor);
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
    	/*
    	int w = maxWidth();
    	int h = maxHeight();
    	if (_transform !=null) {
    		Point2D.Double[] pts = new Point2D.Double[4];
    		pts[0] = new Point2D.Double(0, 0);
    		pts[1] = new Point2D.Double(w, 0);
    		pts[2] = new Point2D.Double(w, h);
    		pts[3] = new Point2D.Double(0, h);	
    		_transform.transform(pts, 0, pts, 0, 4);
        	Double minX = pts[0].x;
        	Double maxX = pts[0].x;
        	Double minY = pts[0].y;
        	Double maxY = pts[0].y;
        	for (int i=1; i<4; i++) {
        		minX = Math.min(minX, pts[i].x);
                maxX = Math.max(maxX, pts[i].x);
        		minY = Math.min(minY, pts[i].y);
                maxY = Math.max(maxY, pts[i].y);
        	}
    		w = (int)Math.ceil(maxX-minX);
    		h = (int)Math.ceil(maxY-minY);
    	}
        setSize(w, h);
        */
        setSize(maxWidth(), maxHeight());    		
    }
    
    public int maxWidth() {
    	if (_shape==null) {
    		return 0;
    	}
       	return _shape.getBounds().width;
    }
      
    public int maxHeight() {
    	if (_shape==null) {
    		return 0;
    	}
    	return _shape.getBounds().height;
    }

    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    /**
    * Rotate degrees	!!!TODO
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
        JButton doneButton = new JButton(rbcp.getString("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	editItem();
                }
        });
        panel0.add(doneButton);

        JButton cancelButton = new JButton(rbcp.getString("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	_editFrame.dispose();
                }
        });
        panel0.add(cancelButton);
        return panel0;
    }
    protected void editItem() {
        _editFrame.updateFigure(this);
        makeShape();
        updateSize();
        _editFrame.dispose();    	
        repaint();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableShape.class.getName());
}
