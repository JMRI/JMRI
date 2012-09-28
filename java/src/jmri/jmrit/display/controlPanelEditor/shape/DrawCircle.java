package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import javax.swing.*;

import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;


/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 * 
 */

public class DrawCircle extends DrawFrame {
	
	JTextField	_radiusText;
    int 		_radius;			// corner radius
	
	public DrawCircle(String which, String title, ShapeDrawer parent) {
		super(which, title, parent);
		_radius = 100;
	}
	
    /**
     * Create a new PositionableShape 
     */
	protected JPanel makeParamsPanel() {
	   JPanel panel = super.makeParamsPanel();
	   
       JPanel p = new JPanel();
	   p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
       p.add(new JLabel(rbcp.getString("circle")));
       JPanel pp = new JPanel();
       _radiusText = new JTextField(6);
       _radiusText.addKeyListener(new KeyListener() {
           public void keyTyped(KeyEvent E) { }
           public void keyPressed(KeyEvent E){ }
           public void keyReleased(KeyEvent E) { 
             JTextField tmp = (JTextField) E.getSource();
             String t = tmp.getText();
             if (t!=null && t.length()>0) {
                 _radius = Integer.parseInt(t);        	 
             }
           }
       	});
	   _radiusText.setText(Integer.toString(_radius));
       _radiusText.setHorizontalAlignment(JTextField.RIGHT);
       pp.add(_radiusText);
       pp.add(new JLabel(rbcp.getString("circleRadius")));
       p.add(pp);
       panel.add(p);
       panel.add(Box.createVerticalStrut(STRUT_SIZE));
       return panel;
	}
	
	protected void makeFigure() {
		ControlPanelEditor ed = _parent.getEditor();
		Rectangle r = ed.getSelectRect();
		if (r==null) {
			return;
		}
		_radius = Math.max(r.width, r.height);
		Ellipse2D.Double rr = new Ellipse2D.Double(0, 0, _radius, _radius);
		PositionableCircle ps = new PositionableCircle(ed, rr);
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
       ((PositionableCircle) p).setRadius(_radius);    		
	}

    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
    protected void setDisplayParams(PositionableShape p) {
    	super.setDisplayParams(p);
    	PositionableCircle pos = (PositionableCircle)p;
    	_radius = pos.getRadius();
    }

    /**
     * Editing is done.  Update the existing PositionableShape
     */
    protected void updateFigure(PositionableShape p) {
    	PositionableCircle pos = (PositionableCircle)p;
		setPositionableParams(pos);
//		pos.makeShape();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DrawCircle.class.getName());
}