package jmri.jmrit.display;

import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jmri.InstanceManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.swing.JmriMouseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <a href="doc-files/Heirarchy.png"><img src="doc-files/Heirarchy.png" alt="UML class diagram for package" height="33%" width="33%"></a>
 * @author Howard G. Penny copyright (C) 2005
 */
public class PositionableJComponent extends JComponent implements Positionable {

    protected Editor _editor = null;

    private String _id;            // user's Id or null if no Id
    private final Set<String> _classes = new HashSet<>(); // user's classes

    private ToolTip _tooltip;
    private boolean _showTooltip = true;
    private boolean _editable = true;
    private boolean _positionable = true;
    private boolean _viewCoordinates = false;
    private boolean _controlling = true;
    private boolean _hidden = false;
    private boolean _emptyHidden = false;
    private int _displayLevel;
    private double _scale;         // user's scaling factor

    JMenuItem lock = null;
    JCheckBoxMenuItem showTooltipItem = null;

    private LogixNG _logixNG;
    private String _logixNG_SystemName;

    public PositionableJComponent(Editor editor) {
        _editor = editor;
        _scale = 1.0;
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
        pos.setShowToolTip(showToolTip());
        pos.setToolTip(getToolTip());
        pos.setEditable(isEditable());
        pos.updateSize();
        return pos;
    }

    /** {@inheritDoc} */
    @Override
    public void setId(String id) throws Positionable.DuplicateIdException {
        if (Objects.equals(this._id, id)) return;
        _editor.positionalIdChange(this, id);
        this._id = id;
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return _id;
    }

    /** {@inheritDoc} */
    @Override
    public void addClass(String className) {
        _editor.positionalAddClass(this, className);
        _classes.add(className);
    }

    /** {@inheritDoc} */
    @Override
    public void removeClass(String className) {
        _editor.positionalRemoveClass(this, className);
        _classes.remove(className);
    }

    /** {@inheritDoc} */
    @Override
    public void removeAllClasses() {
        for (String className : _classes) {
            _editor.positionalRemoveClass(this, className);
        }
        _classes.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getClasses() {
        return java.util.Collections.unmodifiableSet(_classes);
    }

    @Override
    public JComponent getTextComponent() {
        return this;
    }

    public void displayState() {
    }

    //
    // *************** Positionable methods *********************
    //
    @Override
    public void setPositionable(boolean enabled) {
        _positionable = enabled;
    }

    @Override
    public boolean isPositionable() {
        return _positionable;
    }

    @Override
    public void setEditable(boolean enabled) {
        _editable = enabled;
        showHidden();
    }

    @Override
    public boolean isEditable() {
        return _editable;
    }

    @Override
    public void setViewCoordinates(boolean enabled) {
        _viewCoordinates = enabled;
    }

    @Override
    public boolean getViewCoordinates() {
        return _viewCoordinates;
    }

    @Override
    public void setControlling(boolean enabled) {
        _controlling = enabled;
    }

    @Override
    public boolean isControlling() {
        return _controlling;
    }

    @Override
    public void setHidden(boolean hide) {
        _hidden = hide;
    }

    @Override
    public boolean isHidden() {
        return _hidden;
    }

    @Override
    public void showHidden() {
        if (!_hidden || _editor.isEditable()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    @Override
    public void setEmptyHidden(boolean hide) {
        _emptyHidden = hide;
    }

    @Override
    public boolean isEmptyHidden() {
        return _emptyHidden;
    }

    @Override
    public void setValueEditDisabled(boolean isDisabled) {
    }

    @Override
    public boolean isValueEditDisabled() {
        return false;
    }

    /**
     * Delayed setDisplayLevel for DnD.
     *
     * @param l the new level
     */
    public void setLevel(int l) {
        _displayLevel = l;
    }

    @Override
    public void setDisplayLevel(int l) {
        int oldDisplayLevel = _displayLevel;
        _displayLevel = l;
        if (oldDisplayLevel != l) {
            log.debug("Changing label display level from {} to {}", oldDisplayLevel, _displayLevel);
            _editor.displayLevelChange(this);
        }
    }

    @Override
    public int getDisplayLevel() {
        return _displayLevel;
    }

    @Override
    public void setShowToolTip(boolean set) {
        _showTooltip = set;
    }

    @Override
    public boolean showToolTip() {
        return _showTooltip;
    }

    @Override
    public void setToolTip(ToolTip tip) {
        _tooltip = tip;
    }

    @Override
    public ToolTip getToolTip() {
        return _tooltip;
    }

    @Override
    public void setScale(double s) {
        _scale = s;
    }

    @Override
    public double getScale() {
        return _scale;
    }

    // no subclasses support rotations (yet)
    @Override
    public void rotate(int deg) {
    }

    @Override
    public int getDegrees() {
        return 0;
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_PositionableJComponent");
    }

    @Override
    public String getNameString() {
        return getName();
    }

    @Override
    public Editor getEditor() {
        return _editor;
    }

    @Override
    public void setEditor(Editor ed) {
        _editor = ed;
    }

    // overide where used - e.g. momentary
    @Override
    public void doMousePressed(JmriMouseEvent event) {
    }

    @Override
    public void doMouseReleased(JmriMouseEvent event) {
    }

    @Override
    public void doMouseClicked(JmriMouseEvent event) {
    }

    @Override
    public void doMouseDragged(JmriMouseEvent event) {
    }

    @Override
    public void doMouseMoved(JmriMouseEvent event) {
    }

    @Override
    public void doMouseEntered(JmriMouseEvent event) {
    }

    @Override
    public void doMouseExited(JmriMouseEvent event) {
    }

    @Override
    public boolean storeItem() {
        return true;
    }

    @Override
    public boolean doViemMenu() {
        return true;
    }

    @Override
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setRotateMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setScaleMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setDisableControlMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public PositionablePopupUtil getPopupUtility() {
        return null;
    }

    @Override
    public void setPopupUtility(PositionablePopupUtil tu) {
    }

    @Override
    public void updateSize() {
    }

    @Override
    public int maxWidth() {
        return getWidth();
    }

    @Override
    public int maxHeight() {
        return getHeight();
    }

    /*
     ************** end Positionable methods *********************
     */
    /**
     * Removes this object from display and persistance
     */
    @Override
    public void remove() {
        _editor.removeFromContents(this);
        cleanup();
        // remove from persistance by flagging inactive
        active = false;
    }

    /**
     * To be overridden if any special work needs to be done.
     */
    void cleanup() {
    }

    boolean active = true;

    /**
     * Check if the component is still displayed, and should be stored.
     *
     * @return true if active; false otherwise
     */
    public boolean isActive() {
        return active;
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG getLogixNG() {
        return _logixNG;
    }

    /** {@inheritDoc} */
    @Override
    public void setLogixNG(LogixNG logixNG) {
        this._logixNG = logixNG;
    }

    /** {@inheritDoc} */
    @Override
    public void setLogixNG_SystemName(String systemName) {
        this._logixNG_SystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setupLogixNG() {
        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .getBySystemName(_logixNG_SystemName);
        if (_logixNG == null) {
            throw new RuntimeException(String.format(
                    "LogixNG %s is not found for positional %s in panel %s",
                    _logixNG_SystemName, getNameString(), getEditor().getName()));
        }
        _logixNG.setInlineLogixNG(this);
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableJComponent.class);
}
