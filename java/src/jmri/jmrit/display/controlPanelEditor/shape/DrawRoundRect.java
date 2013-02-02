package jmri.jmrit.display.controlPanelEditor.shape;

import org.apache.log4j.Logger;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;

import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;


/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 * 
 */

public class DrawRoundRect extends DrawRectangle {
	
    JTextField	_radiusText;
    int 		_radius;			// corner radius
	
	public DrawRoundRect(String which, String title, ShapeDrawer parent) {
		super(which, title, parent);
		_radius = 40;
	}
	
	protected JPanel makeParamsPanel() {
	   JPanel panel = super.makeParamsPanel();
       JPanel p = new JPanel();
       p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
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
       pp.add(new JLabel(rbcp.getString("cornerRadius")));
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
		RoundRectangle2D.Double rr = new RoundRectangle2D.Double(0, 0, r.width, r.height, _radius, _radius);
		PositionableRoundRect ps = new PositionableRoundRect(ed, rr);
		ps.setLocation(r.x, r.y);
    	ps.setDisplayLevel(ControlPanelEditor.MARKERS);
	   	setPositionableParams(ps);
	   	ps.updateSize();
	   	ed.putItem(ps);
	}
   
   
	protected void setPositionableParams(PositionableShape p) {
		super.setPositionableParams(p);
	       ((PositionableRoundRect) p).setCornerRadius(_radius);    		
	}
	
    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
    protected void setDisplayParams(PositionableShape p) {
    	super.setDisplayParams(p);
    	PositionableRoundRect pos = (PositionableRoundRect)p;
    	_radius = pos.getCornerRadius();
    }

	/**
	 * Editing is done.  Update the existing PositionableShape
	 */
	protected void updateFigure(PositionableShape p) {
	   PositionableRoundRect pos = (PositionableRoundRect)p;
	   setPositionableParams(pos);
//	   pos.makeShape();
	}

    static Logger log = Logger.getLogger(DrawRoundRect.class.getName());
}
