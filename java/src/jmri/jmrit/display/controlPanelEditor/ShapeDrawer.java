package jmri.jmrit.display.controlPanelEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jmri.jmrit.display.Positionable;

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
	
    protected JMenu makeMenu() {
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
        	_drawFrame = new DrawRectangle("New Rectangle", this);    		
    	} else {
    		_drawFrame.toFront();
    	}
    }
	
    private void newRoundRectangle() {   	
    }
    private void newPolygon() {   	
    }
    private void newLine() {   	
    }
    private void newCircle() {   	
    }
    private void newEllipse() {   	
    }
    
    protected void closeDrawFrame(DrawFrame f) {
    	_drawFrame = null;   	
    }
    
    /**************************** Mouse *************************/

    ArrayList<Positionable> _saveSelectionGroup;
    /**
    * Keep selections when editing.  Restore what super nulls
    */
    protected void saveSelectionGroup(ArrayList<Positionable> selectionGroup) {
    	_saveSelectionGroup = selectionGroup;
    }
    
    protected void doMousePressed(MouseEvent event) {
    	if (_drawFrame!=null) {
            _editor.setSelectionGroup(null);
    	}
    }
   
    public boolean doMouseReleased(Positionable selection, MouseEvent event) {
        if (_drawFrame!=null) {
        	return true;
        }
        return false;
    }

    protected boolean doMouseClicked(Positionable selection, MouseEvent event) {
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
        if (selection instanceof PositionableShape) {
            return false;		// OK to drag portal icon
        } else {
        	return true;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ShapeDrawer.class.getName());
}