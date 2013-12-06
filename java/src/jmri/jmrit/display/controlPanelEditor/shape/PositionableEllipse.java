package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;
import java.awt.event.ActionEvent;

import java.awt.geom.Ellipse2D;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PositionableRoundRect.
 * <P>
 * @author Pete cresman Copyright (c) 2012
 * @version $Revision: 1 $
 */

public class PositionableEllipse extends PositionableRectangle {
    public PositionableEllipse(Editor editor) {
    	super(editor);
    }

    public PositionableEllipse(Editor editor, Shape shape) {
       	super(editor, shape);
    }
    /**
     * this class must be overridden by its subclasses and executed
     *  only after its parameters have been set
     */
    public void makeShape() {  	
    	setShape(new Ellipse2D.Double(0, 0, _width, _height));
    }

    public Positionable deepClone() {
    	PositionableEllipse pos = new PositionableEllipse(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
    	PositionableEllipse pos = (PositionableEllipse)p;
        pos._width = _width;
        pos._height = _height;
        return super.finishClone(pos);
    }
    
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("ellipse"));
        popup.add(new javax.swing.AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                	_editFrame = new DrawEllipse("editShape", "ellipse", null);
                	setEditParams();               	
                }
            });
        return true;
    }

    static Logger log = LoggerFactory.getLogger(PositionableEllipse.class.getName());
}
