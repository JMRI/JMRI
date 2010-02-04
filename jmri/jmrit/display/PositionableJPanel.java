// PositionableJPanel.java

package jmri.jmrit.display;

import java.util.List;
import java.util.ResourceBundle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * <p> </p>
 *
 * @author  Bob Jacobsen copyright (C) 2009
 * @version $Revision: 1.17 $
 */
public class PositionableJPanel extends JPanel implements Positionable, MouseListener, MouseMotionListener {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

   	protected Editor _editor = null;
    protected boolean debug = false;

    private String _tooltip;
    private boolean _showTooltip =true;
    private boolean _editable = true;
    private boolean _positionable = true;
    private boolean _viewCoordinates = false;
    private boolean _controlling = true;
    private boolean _hidden = false;
	private int _displayLevel;
    private double _scale;         // user's scaling factor

    JMenuItem lock = null;
    JCheckBoxMenuItem showTooltipItem = null;
    
    public PositionableJPanel(Editor editor) {
        _editor = editor;
        _scale = 1.0;
        debug = log.isDebugEnabled();
    }

    public void setPositionable(boolean enabled) {_positionable = enabled;}
    public boolean isPositionable() { return _positionable; }

    public void setEditable(boolean enabled) {_editable = enabled;}
    public boolean isEditable() { return _editable; }
     
    public void setViewCoordinates(boolean enabled) { _viewCoordinates = enabled; }
    public boolean getViewCoordinates() { return _viewCoordinates; }

    public void setControlling(boolean enabled) {_controlling = enabled;}
    public boolean isControlling() { return _controlling; }

    public void setHidden(boolean hide) {_hidden = hide; }
    public boolean isHidden() { return _hidden;  }
    public void showHidden() {
        if(!_hidden || _editor.isEditable()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    public void setDisplayLevel(int l) {
    	int oldDisplayLevel = _displayLevel;
    	_displayLevel = l;
    	if (oldDisplayLevel!=l){
    		log.debug("Changing label display level from "+oldDisplayLevel+" to "+_displayLevel);
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
    public void setTooltip(String tip) {
        _tooltip = tip;
    }
    public String getTooltip() {
        return _tooltip;
    }
    public void setScale(double s) {
        _scale = s;
    }
    public double getScale() {
        return _scale;
    }

    public String getNameString() {
        return getName();
    }

    // overide where used - e.g. momentary
    public void doMousePressed(MouseEvent event) {
    }
    public void doMouseReleased(MouseEvent event) {
    }

    /**
     * For over-riding in the using classes: add item specific menu choices
     */
    public void setRotateOrthogonalMenu(JPopupMenu popup){
    }
    public void setRotateMenu(JPopupMenu popup){
    }
    public void setScaleMenu(JPopupMenu popup){
    }
    public void setDisableControlMenu(JPopupMenu popup) {
    }
    public void showPopUp(JPopupMenu popup) {
    }

    JFrame _iconEditorFrame;
    IconAdder _iconEditor;
    public void setEditIconMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
    }

    /**
    *  Utility
    */
    protected boolean showIconEditorFrame(Container pos) {
        if (_iconEditorFrame != null) {
            _iconEditorFrame.setLocationRelativeTo(pos);
            _iconEditorFrame.toFront();
            _iconEditorFrame.setVisible(true);
            return true;
        }
        return false;
    }
    void edit() {
    }

    /**************** end Positionable methods **********************/

    /**
     * Removes this object from display and persistance
     */
    public void remove() {
		_editor.removeFromContents(this);
        cleanup();
        // remove from persistance by flagging inactive
        active = false;
    }

    /**
     * To be overridden if any special work needs to be done
     */
    void cleanup() {}

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }
    
    public void mousePressed(MouseEvent e) {
        _editor.mousePressed(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                            e.getX()+this.getX(), e.getY()+this.getY(), 
                                            e.getClickCount(), e.isPopupTrigger())); 
    }

    public void mouseReleased(MouseEvent e) {
        _editor.mouseReleased(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                            e.getX()+this.getX(), e.getY()+this.getY(), 
                                            e.getClickCount(), e.isPopupTrigger())); 
    }

    public void mouseClicked(MouseEvent e) {
        _editor.mouseClicked(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                            e.getX()+this.getX(), e.getY()+this.getY(), 
                                            e.getClickCount(), e.isPopupTrigger())); 
    }
    public void mouseExited(MouseEvent e) {
        _editor.mouseClicked(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                            e.getX()+this.getX(), e.getY()+this.getY(), 
                                            e.getClickCount(), e.isPopupTrigger())); 
    }
    public void mouseEntered(MouseEvent e) {
        _editor.mouseExited(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                            e.getX()+this.getX(), e.getY()+this.getY(), 
                                            e.getClickCount(), e.isPopupTrigger())); 
    }

    public void mouseMoved(MouseEvent e) {
        _editor.mouseMoved(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                            e.getX()+this.getX(), e.getY()+this.getY(), 
                                            e.getClickCount(), e.isPopupTrigger())); 
    }
    public void mouseDragged(MouseEvent e) {
        _editor.mouseDragged(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                            e.getX()+this.getX(), e.getY()+this.getY(), 
                                            e.getClickCount(), e.isPopupTrigger())); 
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableJPanel.class.getName());
}
