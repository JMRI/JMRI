package jmri.jmrit.display.controlPanelEditor;

import jmri.jmrit.display.*;
//import java.awt.Dimension;

//import java.awt.Container;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.util.ResourceBundle;

//import javax.swing.AbstractAction;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 * PositionableShape is item drawn by ava.awt.Graphics2D.
 * <P>
 * The positionable parameter is a global, set from outside.
 * The 'fixed' parameter is local, set from the popup here.
 *
 * @author Pete cresman Copyright (c) 2012
 * @version $Revision: 1 $
 */

public class PositionableShape extends JComponent implements Positionable {

    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    protected Editor _editor;

    protected boolean _control = false;
    protected ToolTip _tooltip;
    protected boolean _showTooltip =true;
    protected boolean _editable = true;
    protected boolean _positionable = true;
    protected boolean _viewCoordinates = true;
    protected boolean _controlling = true;
    protected boolean _hidden = false;
    protected int 	_displayLevel;
    
    private Shape	_shape;
    
    public PositionableShape(Editor editor) {
        _editor = editor;    	
    }

    public PositionableShape(Editor editor, Shape shape) {
        _editor = editor;
        _shape = shape;
    }
    public final boolean isControl() { return _control; }

    public Editor getEditor(){
        return _editor;
    }

    public void setEditor(Editor ed) {
        _editor = ed;
    }
    
    /***************** Positionable methods **********************/

    public void setPositionable(boolean enabled) { _positionable = enabled; }
    public final boolean isPositionable() { return _positionable; }
    
    public void setEditable(boolean enabled) {
        _editable = enabled;
        showHidden();
    }
    public boolean isEditable() { return _editable; }

    public void setViewCoordinates(boolean enabled) { _viewCoordinates = enabled; }
    public boolean getViewCoordinates() { return _viewCoordinates; }

    public void setControlling(boolean enabled) {_controlling = enabled;}
    public boolean isControlling() { return _controlling; }

    public void setHidden(boolean hide) {
        _hidden = hide;
        showHidden();
    }
    public boolean isHidden() { return _hidden;  }
    public void showHidden() {
        if(!_hidden || _editor.isEditable()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
    /**
    * Delayed setDisplayLevel for DnD
    */
    public void setLevel(int l) {
    	_displayLevel = l;
    }
    public void setDisplayLevel(int l) {
    	int oldDisplayLevel = _displayLevel;
    	_displayLevel = l;
    	if (oldDisplayLevel!=l){
    		_editor.displayLevelChange(this);
    	}
    }
    public int getDisplayLevel() { return _displayLevel; }
    
    public void setShowTooltip(boolean set) {
        _showTooltip = set;
    }
    public boolean showTooltip() {
        return _showTooltip;
    }
    public void setTooltip(ToolTip tip) {
        _tooltip = tip;
    }
    public ToolTip getTooltip() {
        return _tooltip;
    }

    public String getNameString() {
        return "Graphic";
    }

    public Positionable deepClone() {
        PositionableShape pos = new PositionableShape(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        PositionableShape pos = (PositionableShape)p;
        pos._control = _control;
        pos.setLocation(getX(), getY());
        pos._displayLevel = _displayLevel;
        pos._controlling = _controlling;
        pos._hidden = _hidden;
        pos._positionable = _positionable;
        pos._showTooltip =_showTooltip;        
        pos.setTooltip(getTooltip());        
        pos._editable = _editable;
//        pos._shape = !!!todo
        if (getPopupUtility()==null) {
            pos.setPopupUtility(null);
        } else {
            pos.setPopupUtility(getPopupUtility().clone(pos));
        }
        pos.setOpaque(isOpaque());
        pos._saveOpaque = _saveOpaque;
        return pos;
    }

    // overide where used - e.g. momentary
    public void doMousePressed(MouseEvent event) {}
    public void doMouseReleased(MouseEvent event) {}
    public void doMouseClicked(MouseEvent event) {}
    public void doMouseDragged(MouseEvent event) {}
    public void doMouseMoved(MouseEvent event) {}
    public void doMouseEntered(MouseEvent event) {}
    public void doMouseExited(MouseEvent event) {}

    public boolean storeItem() {
        return true;
    }
    public boolean doViemMenu() {
        return true;
    }
    
    /**************** end Positionable methods **********************/
    /****************************************************************/

    PositionablePopupUtil _popupUtil;
    public void setPopupUtility(PositionablePopupUtil tu) {
        _popupUtil = tu;
    }
    public PositionablePopupUtil getPopupUtility() {
        return _popupUtil;
    }

    /**
     * Update the AWT and Swing size information due to change in internal
     * state, e.g. if one or more of the icons that might be displayed
     * is changed
     */
    public void updateSize() {
        setSize(maxWidth(), maxHeight());
    }
    
    public int maxWidth() {
    	return getBounds().width;
    }
    
    
    public int maxHeight() {
    	return getBounds().height;
    }
    
	public boolean isBackground() { return (_displayLevel == Editor.BKG);
    }

    /******* Methods to add menu items to popup ********/

    /**
    *  Call to a Positionable that has unique requirements
    * - e.g. RpsPositionIcon, SecurityElementIcon
    */
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    public boolean setEditIconMenu(JPopupMenu popup) {
    	return false;
    }
    public boolean setDisableControlMenu(JPopupMenu popup) {
    	return false;
    }
    public boolean setEditItemMenu(JPopupMenu popup) {
        return setEditIconMenu(popup);
    }
    public boolean setTextEditMenu(JPopupMenu popup) {
        return false;
    }
    
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {
    	return false;
    }

    /**
    * Rotate degrees
    * return true if popup is set
    */
    public boolean setRotateMenu(JPopupMenu popup) {
        if (_displayLevel > Editor.BKG) {
            popup.add(CoordinateEdit.getRotateEditAction(this));
            return true;
        }
        return false;
    }

    /**
    * Scale percentage
    * return true if popup is set
    */
    public boolean setScaleMenu(JPopupMenu popup) {
        if (_displayLevel > Editor.BKG) {
            popup.add(CoordinateEdit.getScaleEditAction(this));
            return true;
        }
        return false;
    }


    public void setScale(double s) {
    	//!!!todo
    }
    public double getScale() {
        return 1.0;
    }

    private boolean _saveOpaque;
    public void saveOpaque(boolean set) {
    	_saveOpaque =set;
    }
    public boolean getSaveOpaque() {
    	return _saveOpaque;
   }
    
    public void rotate(int deg) {
    	//!!!todo
    }

    public int getDegrees() {
        return 0;
    }
    
    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    public void dispose() {
    }

    /**
     * Removes this object from display and persistance
     */
    public void remove() {
		_editor.removeFromContents(this);
        // remove from persistance by flagging inactive
        active = false;
        dispose();
    }

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }


    public int getHeight() {
      return getSize().height;
    }

    public int getWidth() {
      return getSize().width;
    }

    /**
    * Provides a generic method to return the bean associated with the Positionable
    */
    public jmri.NamedBean getNamedBean() { return null; }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableShape.class.getName());

}
