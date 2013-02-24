package jmri.jmrit.display.controlPanelEditor.shape;

import org.apache.log4j.Logger;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

import java.awt.geom.GeneralPath;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JPopupMenu;

/**
 * PositionableRoundRect.
 * <P>
 * @author Pete cresman Copyright (c) 2013
 * @version $Revision: 1 $
 */

public class PositionablePolygon extends PositionableShape {

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
        	PositionableShape ps;
                public void actionPerformed(ActionEvent e) {
                	_editFrame = new DrawPolygon(getEditor(), "polygon", ps);
                	setEditParams();               	
                }
                javax.swing.AbstractAction init(PositionableShape p) {
                	ps = p;
                	return this;
                }
            }.init(this));
        return true;
    }

    static Logger log = Logger.getLogger(PositionablePolygon.class.getName());
}
