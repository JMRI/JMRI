package jmri.jmrit.display;

import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.ItemPalette;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

/**
 * PositionableLabel is a JLabel that can be dragged around the
 * inside of the enclosing Container using a right-drag.
 * <P>
 * The positionable parameter is a global, set from outside.
 * The 'fixed' parameter is local, set from the popup here.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @version $Revision$
 */

public class PositionableLabel extends JLabel implements Positionable {

    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    protected Editor _editor;

    private boolean debug = false; 
    protected boolean _icon = false;
    protected boolean _text = false;
    protected boolean _control = false;
    protected NamedIcon _namedIcon;

    protected ToolTip _tooltip;
    protected boolean _showTooltip =true;
    protected boolean _editable = true;
    protected boolean _positionable = true;
    protected boolean _viewCoordinates = true;
    protected boolean _controlling = true;
    protected boolean _hidden = false;
    protected int _displayLevel;
    
    public PositionableLabel(String s, Editor editor) {
        super(s);
        _editor = editor;
        _text = true;
        debug = log.isDebugEnabled();
        if (debug) log.debug("PositionableLabel ctor (text) "+s);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setPopupUtility(new PositionablePopupUtil(this, this));
    }
    public PositionableLabel(NamedIcon s, Editor editor) {
        super(s);
        _editor = editor;
        _icon = true;
        _namedIcon = s;
        debug = log.isDebugEnabled();
        if (debug) log.debug("PositionableLabel ctor (icon) "+s.getName());
        setPopupUtility(new PositionablePopupUtil(this, this));
    }

    public final boolean isIcon() { return _icon; }
    public final boolean isText() { return _text; }
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
    		if (debug) log.debug("Changing label display level from "+oldDisplayLevel+" to "+_displayLevel);
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
        if (_icon && _displayLevel > Editor.BKG) return "Icon";
        else if (_text) return "Text Label";
        else return "Background";
    }

    public Positionable deepClone() {
        PositionableLabel pos;
        if (_icon) {
            NamedIcon icon = new NamedIcon((NamedIcon)getIcon());
            pos = new PositionableLabel(icon, _editor);
        } else {
            pos = new PositionableLabel(getText(), _editor);
        }
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        PositionableLabel pos = (PositionableLabel)p;
        pos._text = _text;
        pos._icon = _icon;
        pos.setText(getText());
        pos.setLocation(getX(), getY());
        pos.setDisplayLevel(getDisplayLevel());
        pos.setControlling(isControlling());
        pos.setHidden(isHidden());
        pos.setPositionable(isPositionable());
        pos.setShowTooltip(showTooltip());        
        pos.setTooltip(getTooltip());        
        pos.setEditable(isEditable());
        if (getPopupUtility()==null) {
            pos.setPopupUtility(null);
        } else {
            pos.setPopupUtility(getPopupUtility().clone(pos));
        }
        if (getIcon()!=null) {
            pos.setIcon(cloneIcon((NamedIcon)getIcon(), pos));
            pos.updateSize();
        }
        return pos;
    }

    protected NamedIcon cloneIcon(NamedIcon icon, PositionableLabel pos) {
        NamedIcon clone = new NamedIcon(icon, pos);
        return clone;
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
        if (debug) {
            log.debug("updateSize() w= "+maxWidth()+", h= "+maxHeight()+" _namedIcon= "+_namedIcon);
        }
        setSize(maxWidth(), maxHeight());
        if ( _namedIcon!=null && _text) {
            //we have a combined icon/text therefore the icon is central to the text.
            setHorizontalTextPosition(CENTER);
        }
    }
    
    public int maxWidth() {
        int max = 0;
        if (_popupUtil!=null && _popupUtil.getFixedWidth()!=0) {
            max = _popupUtil.getFixedWidth();
            max += _popupUtil.getBorderSize()*2;
            if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                _popupUtil.setFixedWidth(PositionablePopupUtil.MIN_SIZE);
                max = PositionablePopupUtil.MIN_SIZE;
            }
        } else {
            if(_text && getText()!=null) {
                if (getText().trim().length()==0) {
                    // show width of 1 blank character
                    if (getFont()!=null) {
                        max = getFontMetrics(getFont()).stringWidth("0");
                    }
                } else {
                    max = getFontMetrics(getFont()).stringWidth(getText());
                }
            }
            if(_icon && _namedIcon!=null) {
                max = Math.max(_namedIcon.getIconWidth(), max);
            }
            if (_popupUtil!=null) {
                max += _popupUtil.getMargin()*2;
                max += _popupUtil.getBorderSize()*2;
            }
            if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                max = PositionablePopupUtil.MIN_SIZE;
            }
        }
        if (debug) log.debug("maxWidth= "+max+" preferred width= "+getPreferredSize().width);
        return max;
    }

    public int maxHeight() {
        int max = 0;
        if (_popupUtil!=null && _popupUtil.getFixedHeight()!=0) {
            max = _popupUtil.getFixedHeight();
            max += _popupUtil.getBorderSize()*2;
            if (max < PositionablePopupUtil.MIN_SIZE) {   // don't let item disappear
                _popupUtil.setFixedHeight(PositionablePopupUtil.MIN_SIZE);
            }
        } else {
            //if(_text) {
            if(_text && getText()!=null && getFont()!=null) {
                max = getFontMetrics(getFont()).getHeight();
            }
            if(_icon && _namedIcon!=null) {
                max = Math.max(_namedIcon.getIconHeight(), max);
            }
            if (_popupUtil!=null) {
                max += _popupUtil.getMargin()*2;
                max += _popupUtil.getBorderSize()*2;
            }
            if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                max = PositionablePopupUtil.MIN_SIZE;
            }
        }
        if (debug) log.debug("maxHeight= "+max+" preferred height= "+getPreferredSize().height);
        return max;
    }

	public boolean isBackground() { return (_displayLevel == Editor.BKG);
    }

    public void updateIcon(NamedIcon s){
        _namedIcon = s;
        setIcon(_namedIcon);
        updateSize();
    }

    /******* Methods to add menu items to popup ********/

    /**
    *  Call to a Positionable that has unique requirements
    * - e.g. RpsPositionIcon, SecurityElementIcon
    */
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    /**
    * Rotate othogonally
    * return true if popup is set
    */
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {

        if (isIcon() && _displayLevel > Editor.BKG) {
            popup.add(new AbstractAction(rb.getString("Rotate")) {
                public void actionPerformed(ActionEvent e) {
                    rotateOrthogonal();
                }
            });
            return true;
        }
        return false;
    }
    protected void rotateOrthogonal() {
        _namedIcon.setRotation(_namedIcon.getRotation()+1, this);
        setIcon(_namedIcon);
        updateSize();
        repaint();
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        return setEditIconMenu(popup);
    }

    JFrame _iconEditorFrame;
    IconAdder _iconEditor;
    public boolean setEditIconMenu(JPopupMenu popup) {
        if (_icon && !_text) {
            String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("Icon"));
            popup.add(new AbstractAction(txt) {
                    public void actionPerformed(ActionEvent e) {
                        edit();
                    }
                });
            return true;
        }
        return false;
    }

    jmri.util.JmriJFrame _paletteFrame;

    protected void makePalettteFrame(String title) {
    	jmri.jmrit.display.palette.ItemPalette.loadIcons();

        _paletteFrame = new jmri.util.JmriJFrame(title, false, false);
        _paletteFrame.setLocationRelativeTo(this);
        _paletteFrame.toFront();
        _paletteFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            Editor editor;
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (ImageIndexEditor.checkImageIndex(editor)) {
                	ItemPalette.storeIcons();   // write maps to tree
                }
            }
            java.awt.event.WindowAdapter init(Editor ed) {
                editor = ed;
                return this;
            }
        }.init(_editor));
    }

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

    protected void edit() {
        makeIconEditorFrame(this, "Icon", false, null);
        NamedIcon icon = new NamedIcon(_namedIcon);
        _iconEditor.setIcon(0, "plainIcon", icon);
        _iconEditor.makeIconPanel(false);

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editIcon();
            }
        };
        _iconEditor.complete(addIconAction, true, false, true);

    }

    protected void editIcon() {
        String url = _iconEditor.getIcon("plainIcon").getURL();
        _namedIcon = NamedIcon.getIconByName(url);
        setIcon(_namedIcon);
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    /**
    * Rotate degrees
    * return true if popup is set
    */
    public boolean setRotateMenu(JPopupMenu popup) {
        if (isIcon() && _displayLevel > Editor.BKG) {
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
        if (isIcon() && _displayLevel > Editor.BKG) {
            popup.add(CoordinateEdit.getScaleEditAction(this));
            return true;
        }
        return false;
    }


    public boolean setTextEditMenu(JPopupMenu popup) {
        if (isText()) {
            popup.add(CoordinateEdit.getTextEditAction(this, "EditText"));
            return true;
        }
        return false;
    }
    
    JCheckBoxMenuItem disableItem = null;
    public boolean setDisableControlMenu(JPopupMenu popup) {
        if (_control) {
            disableItem = new JCheckBoxMenuItem(rb.getString("Disable"));
            disableItem.setSelected(!_controlling);
            popup.add(disableItem);
            disableItem.addActionListener(new ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setControlling(!disableItem.isSelected());
                }
            });
            return true;
        }
        return false;
    }

    public void setScale(double s) {
        if (_namedIcon!=null) {
            _namedIcon.scale(s, this);
            setIcon(_namedIcon);
            updateSize();
        }
    }
    public double getScale() {
        if (_namedIcon==null) {
            return 1.0;
        }
        return ((NamedIcon)getIcon()).getScale();
    }

    public void rotate(int deg) {
        if (_namedIcon!=null) {
            _namedIcon.rotate(deg, this);
            setIcon(_namedIcon);
            updateSize();
        }
    }
    public int getDegrees() {
        if (_namedIcon==null) {
            return 0;
        }
        return ((NamedIcon)getIcon()).getDegrees();
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
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableLabel.class.getName());

}
