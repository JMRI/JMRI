package jmri.jmrit.display.controlPanelEditor.shape;

import org.apache.log4j.Logger;
import jmri.jmrit.display.*;

import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.Shape;
import javax.swing.JPopupMenu;

/**
 * PositionableCircle  PositionableShapes.
 * <P>
 * @author Pete cresman Copyright (c) 2012
 * @version $Revision: 1 $
 */

public class PositionableCircle extends PositionableShape {

    protected int	_radius = 100;
    
    public PositionableCircle(Editor editor) {
    	super(editor);
    }

    public PositionableCircle(Editor editor, Shape shape) {
       	super(editor, shape);
    }

    public void setRadius(int r) {
    	_radius = r;
    }
    public int getRadius() {
    	return _radius;
    }
    /**
     * this class must be overridden by its subclasses and executed
     *  only after its parameters have been set
     */
    public void makeShape() {  	
		setShape(new Ellipse2D.Double(0, 0, _radius, _radius));
    }

    public Positionable deepClone() {
    	PositionableCircle pos = new PositionableCircle(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
    	PositionableCircle pos = (PositionableCircle)p;
        pos._radius = _radius;
        return super.finishClone(pos);
    }
    
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("circle"));
        popup.add(new javax.swing.AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                	_editFrame = new DrawCircle("editShape", "circle", null);
                	setEditParams();               	
                }
            });
        return true;
    }
    /*
    protected void editItem() {
        _editFrame.updateFigure(this);
        updateSize();
        _editFrame.dispose();
        repaint();
    }*/

    static Logger log = Logger.getLogger(PositionableCircle.class.getName());
}
