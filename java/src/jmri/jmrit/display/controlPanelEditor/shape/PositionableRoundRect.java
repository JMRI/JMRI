package jmri.jmrit.display.controlPanelEditor.shape;

import org.apache.log4j.Logger;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.Shape;
import javax.swing.JPopupMenu;

/**
 * PositionableRoundRect adds corner radii to PositionableShapes.
 * <P>
 * @author Pete cresman Copyright (c) 2012
 * @version $Revision: 1 $
 */

public class PositionableRoundRect extends PositionableRectangle {

    protected int	_radius = 10;
    
    public PositionableRoundRect(Editor editor) {
    	super(editor);
    }

    public PositionableRoundRect(Editor editor, Shape shape) {
       	super(editor, shape);
    }

    public void setCornerRadius(int r) {
    	_radius = r;
    }
	public int getCornerRadius() {
		return _radius;
	}
    /**
     * this class must be overridden by its subclasses and executed
     *  only after its parameters have been set
     */
    public void makeShape() {  	
    	setShape(new RoundRectangle2D.Double(0, 0, _width, _height, _radius, _radius));
    }

    public Positionable deepClone() {
    	PositionableRoundRect pos = new PositionableRoundRect(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
    	PositionableRoundRect pos = (PositionableRoundRect)p;
        pos._radius = _radius;
        return super.finishClone(pos);
    }
    
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("roundRect"));
        popup.add(new javax.swing.AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                	_editFrame = new DrawRoundRect("editShape", "rectangle", null);
                	setEditParams();               	
                }
            });
        return true;
    }

    static Logger log = Logger.getLogger(PositionableRoundRect.class.getName());
}
