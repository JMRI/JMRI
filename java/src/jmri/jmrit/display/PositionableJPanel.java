// PositionableJPanel.java

package jmri.jmrit.display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Container;
import javax.swing.*;

/**
 * <p> </p>
 *
 * @author  Bob Jacobsen copyright (C) 2009
 * @version $Revision$
 */
public class PositionableJPanel extends JPanel implements Positionable, MouseListener, MouseMotionListener {

   	protected Editor _editor = null;
    protected boolean debug = false;

    private ToolTip _tooltip;
    protected boolean _showTooltip =true;
    protected boolean _editable = true;
    protected boolean _positionable = true;
    protected boolean _viewCoordinates = false;
    protected boolean _controlling = true;
    protected boolean _hidden = false;
    protected int _displayLevel;
    private double _scale = 1.0;    // scaling factor

    JMenuItem lock = null;
    JCheckBoxMenuItem showTooltipItem = null;
    
    public PositionableJPanel(Editor editor) {
        _editor = editor;
        debug = log.isDebugEnabled();
    }

    public Positionable deepClone() {
        PositionableJPanel pos = new PositionableJPanel(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
    	PositionableJPanel pos = (PositionableJPanel)p;
        pos.setLocation(getX(), getY());
        pos._displayLevel = _displayLevel;
        pos._controlling = _controlling;
        pos._hidden = _hidden;
        pos._positionable = _positionable;
        pos._showTooltip =_showTooltip;        
        pos.setTooltip(getTooltip());        
        pos._editable = _editable;
        if (getPopupUtility()==null) {
            pos.setPopupUtility(null);
        } else {
            pos.setPopupUtility(getPopupUtility().clone(pos, pos.getTextComponent()));
        }
        pos.updateSize();
        return pos;
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
    public void setTooltip(ToolTip tip) {
        _tooltip = tip;
    }
    public ToolTip getTooltip() {
        return _tooltip;
    }
    public void setScale(double s) {
        _scale = s;
    }
    public double getScale() {
        return _scale;
    }
    // no subclasses support rotations (yet)
    public void rotate(int deg) {
    }
    public int getDegrees() {
        return 0;
    }
    public boolean getSaveOpaque() {
    	return isOpaque();
    }
    public JComponent getTextComponent() {
    	return this;
    }

    public String getNameString() {
        return getName();
    }

    public Editor getEditor(){
        return _editor;
    }
    public void setEditor(Editor ed) {
        _editor = ed;
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
    /**
     * For over-riding in the using classes: add item specific menu choices
     */
    public boolean setRotateOrthogonalMenu(JPopupMenu popup){
        return false;
    }
    public boolean setRotateMenu(JPopupMenu popup){
        return false;
    }
    public boolean setScaleMenu(JPopupMenu popup){
        return false;
    }
    public boolean setDisableControlMenu(JPopupMenu popup) {
        return false;
    }
    public boolean setTextEditMenu(JPopupMenu popup) {
        return false;
    }
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    JFrame _iconEditorFrame;
    IconAdder _iconEditor;
    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }
    public boolean setEditItemMenu(JPopupMenu popup) {
        return setEditIconMenu(popup);
    }

    /**
    *  Utility
    */
    protected void makeIconEditorFrame(Container pos, String name, boolean table, IconAdder editor) {
        if (editor!=null) {
            _iconEditor = editor;
        } else {
            _iconEditor = new IconAdder(name);
        }
        _iconEditorFrame = _editor.makeAddIconFrame(name, false, table, _iconEditor);
        _iconEditorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    _iconEditorFrame.dispose();
                    _iconEditorFrame = null;
                }
            });
        _iconEditorFrame.setLocationRelativeTo(pos);
        _iconEditorFrame.toFront();
        _iconEditorFrame.setVisible(true);
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
//    	transferFocus();
        _editor.mouseExited(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
                                             e.getX()+this.getX(), e.getY()+this.getY(), 
                                             e.getClickCount(), e.isPopupTrigger())); 
    }
    public void mouseEntered(MouseEvent e) {
        _editor.mouseEntered(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(), 
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

    /***************************************************************/

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
        invalidate();
        setSize(maxWidth(), maxHeight());
        if (debug) {
//            javax.swing.JTextField text = (javax.swing.JTextField)_popupUtil._textComponent;
            log.debug("updateSize: "+_popupUtil.toString()+
                      ", text: w="+getFontMetrics(_popupUtil.getFont()).stringWidth(_popupUtil.getText())+
                      "h="+getFontMetrics(_popupUtil.getFont()).getHeight());
        }
        validate();
        repaint();
    }    
    
    public int maxWidth() {
        int max = 0;
        if (_popupUtil!=null) {
            if (_popupUtil.getFixedWidth()!=0) {
                max = _popupUtil.getFixedWidth();
                max += _popupUtil.getMargin()*2;
                if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                    _popupUtil.setFixedWidth(PositionablePopupUtil.MIN_SIZE);
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            } else {
                max = getPreferredSize().width;
                /*
                if(_popupUtil._textComponent instanceof javax.swing.JTextField) {
                    javax.swing.JTextField text = (javax.swing.JTextField)_popupUtil._textComponent;
                    max = getFontMetrics(text.getFont()).stringWidth(text.getText());
                } */
                max += _popupUtil.getMargin()*2;
                if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            }
        }
        if (debug) log.debug("maxWidth= "+max+" preferred width= "+getPreferredSize().width);
        return max;
    }

    public int maxHeight() {
        int max = 0;
        if (_popupUtil!=null) {
            if (_popupUtil.getFixedHeight()!=0) {
                max = _popupUtil.getFixedHeight();
                max += _popupUtil.getMargin()*2;
                if (max < PositionablePopupUtil.MIN_SIZE) {   // don't let item disappear
                    _popupUtil.setFixedHeight(PositionablePopupUtil.MIN_SIZE);
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            } else {
                max = getPreferredSize().height;
                /*
                if(_popupUtil._textComponent!=null) {
                    max = getFontMetrics(_popupUtil._textComponent.getFont()).getHeight();
                }  */
                if (_popupUtil!=null) {
                    max += _popupUtil.getMargin()*2;
                }
                if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            }
        }
        if (debug) log.debug("maxHeight= "+max+" preferred height= "+getPreferredSize().height);
        return max;
    }
    
    public jmri.NamedBean getNamedBean() { return null; }

    static Logger log = LoggerFactory.getLogger(PositionableJPanel.class.getName());
}
