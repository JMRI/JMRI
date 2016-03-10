package jmri.jmrit.display;

//import java.awt.event.MouseListener;
//import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Howard G. Penny copyright (C) 2005
 */
public class PositionableJComponent extends JComponent implements Positionable {

    /**
     *
     */
    private static final long serialVersionUID = -4906476926163826709L;
    protected Editor _editor = null;
    protected boolean debug = false;

    private ToolTip _tooltip;
    private boolean _showTooltip = true;
    private boolean _editable = true;
    private boolean _positionable = true;
    private boolean _viewCoordinates = false;
    private boolean _controlling = true;
    private boolean _hidden = false;
    private int _displayLevel;
    private double _scale;         // user's scaling factor

    JMenuItem lock = null;
    JCheckBoxMenuItem showTooltipItem = null;

    public PositionableJComponent(Editor editor) {
        _editor = editor;
        _scale = 1.0;
        debug = log.isDebugEnabled();
    }

    @Override
    public Positionable deepClone() {
        PositionableJComponent pos = new PositionableJComponent(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableJComponent pos) {
        pos.setLocation(getX(), getY());
        pos.setDisplayLevel(getDisplayLevel());
        pos.setControlling(isControlling());
        pos.setHidden(isHidden());
        pos.setPositionable(isPositionable());
        pos.setShowTooltip(showTooltip());
        pos.setTooltip(getTooltip());
        pos.setEditable(isEditable());
        pos.updateSize();
        return pos;
    }

    public JComponent getTextComponent() {
        return this;
    }

    public void displayState() {
    }

    /**
     * *************** Positionable methods *********************
     */
    public void setPositionable(boolean enabled) {
        _positionable = enabled;
    }

    public boolean isPositionable() {
        return _positionable;
    }

    public void setEditable(boolean enabled) {
        _editable = enabled;
        showHidden();
    }

    public boolean isEditable() {
        return _editable;
    }

    public void setViewCoordinates(boolean enabled) {
        _viewCoordinates = enabled;
    }

    public boolean getViewCoordinates() {
        return _viewCoordinates;
    }

    public void setControlling(boolean enabled) {
        _controlling = enabled;
    }

    public boolean isControlling() {
        return _controlling;
    }

    public void setHidden(boolean hide) {
        _hidden = hide;
    }

    public boolean isHidden() {
        return _hidden;
    }

    public void showHidden() {
        if (!_hidden || _editor.isEditable()) {
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
        if (oldDisplayLevel != l) {
            log.debug("Changing label display level from " + oldDisplayLevel + " to " + _displayLevel);
            _editor.displayLevelChange(this);
        }
    }

    public int getDisplayLevel() {
        return _displayLevel;
    }

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

    public String getNameString() {
        return getName();
    }

    public Editor getEditor() {
        return _editor;
    }

    public void setEditor(Editor ed) {
        _editor = ed;
    }

    // overide where used - e.g. momentary
    public void doMousePressed(MouseEvent event) {
    }

    public void doMouseReleased(MouseEvent event) {
    }

    public void doMouseClicked(MouseEvent event) {
    }

    public void doMouseDragged(MouseEvent event) {
    }

    public void doMouseMoved(MouseEvent event) {
    }

    public void doMouseEntered(MouseEvent event) {
    }

    public void doMouseExited(MouseEvent event) {
    }

    public boolean storeItem() {
        return true;
    }

    public boolean doViemMenu() {
        return true;
    }

    /**
     * For over-riding in the using classes: add item specific menu choices
     */
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setRotateMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setScaleMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setDisableControlMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setTextEditMenu(JPopupMenu popup) {
        return false;
    }

    public boolean setEditItemMenu(JPopupMenu popup) {
        return false;
    }

    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }

    public PositionablePopupUtil getPopupUtility() {
        return null;
    }

    public void setPopupUtility(PositionablePopupUtil tu) {
    }

    public void updateSize() {
    }

    public int maxWidth() {
        return getWidth();
    }

    public int maxHeight() {
        return getHeight();
    }

    /**
     * ************** end Positionable methods *********************
     */
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
    void cleanup() {
    }

    boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    public jmri.NamedBean getNamedBean() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableJComponent.class.getName());
}
