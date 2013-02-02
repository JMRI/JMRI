package jmri.jmrit.display.controlPanelEditor.shape;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 * 
 */

public class ShapeDrawer  {
	
	protected ControlPanelEditor _editor;
	private boolean 	_creatingNewShape = false;
	private DrawFrame 	_drawFrame;

    public final static ResourceBundle rbcp = ControlPanelEditor.rbcp;
	
	public ShapeDrawer(ControlPanelEditor ed) {
        _editor = ed;
   }
	
	public JMenu makeMenu() {
    	JMenu drawMenu = new JMenu(rbcp.getString("drawShapes"));
    	
        JMenuItem shapeItem = new JMenuItem(rbcp.getString("drawRectangle"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    newRectangle();
                }
            });
        shapeItem = new JMenuItem(rbcp.getString("drawRoundRectangle"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    newRoundRectangle();
                }
            });
        /*
        shapeItem = new JMenuItem(rbcp.getString("drawPolygon"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    newPolygon();
                }
            });
        shapeItem = new JMenuItem(rbcp.getString("drawLine"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                	newLine();
                }
            });
            */
        shapeItem = new JMenuItem(rbcp.getString("drawCircle"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    newCircle();
                }
            });
        shapeItem = new JMenuItem(rbcp.getString("drawEllipse"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    newEllipse();
                }
            });

    	return drawMenu;
    }
    
    private void newRectangle() {
    	if (_drawFrame==null) {
        	_drawFrame = new DrawRectangle("newShape", "rectangle", this);    		
    	} else {
    		_drawFrame.toFront();
    	}
    }
	
    private void newRoundRectangle() {   	
    	if (_drawFrame==null) {
        	_drawFrame = new DrawRoundRect("newShape", "roundRect", this);    		
    	} else {
    		_drawFrame.toFront();
    	}
    }
    private void newPolygon() {   	
    }
    private void newLine() {   	
    }
    private void newCircle() {   	
    	if (_drawFrame==null) {
        	_drawFrame = new DrawCircle("newShape", "circle", this);    		
    	} else {
    		_drawFrame.toFront();
    	}
    }
    private void newEllipse() {   	
    	if (_drawFrame==null) {
        	_drawFrame = new DrawEllipse("newShape", "ellipse", this);    		
    	} else {
    		_drawFrame.toFront();
    	}
    }
    
    protected void closeDrawFrame(DrawFrame f) {
    	_drawFrame = null;   	
    }
    
    protected ControlPanelEditor getEditor() {
    	return _editor;
    }
    
    /**************************** Mouse *************************/

    ArrayList<Positionable> _saveSelectionGroup;
    /**
    * Keep selections when editing.  Restore what super nulls
    */
    public void saveSelectionGroup(ArrayList<Positionable> selectionGroup) {
    	_saveSelectionGroup = selectionGroup;
    }
    
    public boolean doMousePressed(MouseEvent event) {
    	if (_drawFrame!=null) {
            _editor.setSelectionGroup(null);
            _drawFrame.setDrawParams();
            return true;
    	}
    	return false;
    }
   
    public boolean doMouseReleased(Positionable selection, MouseEvent event) {
        if (_drawFrame!=null) {
        	_drawFrame.makeFigure();
        	_drawFrame.closingEvent();
            _editor.resetEditor();
            return true;
        }
        return false;
    }

    public boolean doMouseClicked(Positionable selection, MouseEvent event) {
        if (_drawFrame!=null) {
            return true;
        }
        return false;
    }

    /**
    * No dragging when editing
    */
    public boolean doMouseDragged(Positionable selection, MouseEvent event) {
        if (_drawFrame!=null) {
            _editor.drawSelectRect(event.getX(), event.getY());
            return true;     // no dragging when editing
        }
        return false;
    }

    static Logger log = Logger.getLogger(ShapeDrawer.class.getName());
}
