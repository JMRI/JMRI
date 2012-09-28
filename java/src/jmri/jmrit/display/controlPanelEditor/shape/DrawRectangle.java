package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 * 
 */

public class DrawRectangle extends DrawFrame{
	
	int _width;
	int _height;
	JTextField _widthText;
	JTextField _heightText;
	
	public DrawRectangle(String which, String title, ShapeDrawer parent) {
		super(which, title, parent);
        _lineWidth = 3;
	}
    
    protected JPanel makeParamsPanel() {
    	JPanel panel = super.makeParamsPanel(); 	   
        JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		JPanel pp = new JPanel();
        _widthText = new JTextField(6);
        _widthText.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent E) { }
            public void keyPressed(KeyEvent E){ }
            public void keyReleased(KeyEvent E) { 
              JTextField tmp = (JTextField) E.getSource();
              String t = tmp.getText();
              if (t!=null && t.length()>0) {
            	  _width = Integer.parseInt(t);        	 
              }
            }
        	});
    	_widthText.setText(Integer.toString(_width));
        _widthText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_widthText);
        pp.add(new JLabel(rbcp.getString("width")));
        p.add(pp);
        p.add(Box.createHorizontalStrut(STRUT_SIZE));

        pp = new JPanel();
        _heightText = new JTextField(6);
        _heightText.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent E) { }
            public void keyPressed(KeyEvent E){ }
            public void keyReleased(KeyEvent E) { 
              JTextField tmp = (JTextField) E.getSource();
              String t = tmp.getText();
              if (t!=null && t.length()>0) {
            	  _height = Integer.parseInt(t);        	 
              }
            }
        	});
    	_heightText.setText(Integer.toString(_height));
        _heightText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_heightText);
        pp.add(new JLabel(rbcp.getString("height")));
        p.add(pp);
        
        panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    /**
     * Create a new PositionableShape 
     */
    protected void makeFigure() {
    	ControlPanelEditor ed = _parent.getEditor();
    	Rectangle r = ed.getSelectRect();
    	if (r==null) {
    		return;
    	}
    	_width = r.width;
    	_height = r.height;
    	Rectangle2D.Double rr = new Rectangle2D.Double(0, 0, _width, _height);
    	PositionableRectangle ps = new PositionableRectangle(ed, rr);
    	ps.setLocation(r.x, r.y);
    	ps.setDisplayLevel(ControlPanelEditor.MARKERS);
		setPositionableParams(ps);
        ps.updateSize();
    	ed.putItem(ps);
	}

    /**
     * Set parameters on a new or updated PositionableShape
     */
    protected void setPositionableParams(PositionableShape p) {
    	super.setPositionableParams(p);
    	PositionableRectangle pos = (PositionableRectangle)p;
    	pos.setWidth(_width);
    	pos.setHeight(_height);
	}

    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
    protected void setDisplayParams(PositionableShape p) {
    	super.setDisplayParams(p);
    	PositionableRectangle pos = (PositionableRectangle)p;
    	_width = pos.getWidth();
    	_height = pos.getHeight();   	
    }

    /**
     * Editing is done.  Update the existing PositionableShape
     */
    protected void updateFigure(PositionableShape p) {
    	PositionableRectangle pos = (PositionableRectangle)p;
		setPositionableParams(pos);
//		pos.makeShape();
    }
   
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DrawRectangle.class.getName());
}