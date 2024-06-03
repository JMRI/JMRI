package jmri.jmrit.display;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import jmri.InstanceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.display.palette.ItemPanel;
import jmri.jmrit.display.palette.TextItemPanel;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;
import jmri.util.swing.JmriMouseMotionListener;

/**
 * <a href="doc-files/Heirarchy.png"><img src="doc-files/Heirarchy.png" alt="UML class diagram for package" height="33%" width="33%"></a>
 * @author Bob Jacobsen copyright (C) 2009
 */
public class PositionableJPanel extends JPanel implements Positionable, JmriMouseListener, JmriMouseMotionListener {

    protected Editor _editor = null;

    private String _id;            // user's Id or null if no Id
    private final Set<String> _classes = new HashSet<>(); // user's classes

    private ToolTip _tooltip;
    protected boolean _showTooltip = true;
    protected boolean _editable = true;
    protected boolean _positionable = true;
    protected boolean _viewCoordinates = false;
    protected boolean _controlling = true;
    protected boolean _hidden = false;
    protected boolean _emptyHidden = false;
    protected int _displayLevel;
    private double _scale = 1.0;    // scaling factor

    JMenuItem lock = null;
    JCheckBoxMenuItem showTooltipItem = null;

    private LogixNG _logixNG;
    private String _logixNG_SystemName;

    public PositionableJPanel(Editor editor) {
        _editor = editor;
    }

    @Override
    public Positionable deepClone() {
        PositionableJPanel pos = new PositionableJPanel(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableJPanel pos) {
        pos.setLocation(getX(), getY());
        pos._displayLevel = _displayLevel;
        pos._controlling = _controlling;
        pos._hidden = _hidden;
        pos._positionable = _positionable;
        pos._showTooltip = _showTooltip;
        pos.setToolTip(getToolTip());
        pos._editable = _editable;
        if (getPopupUtility() == null) {
            pos.setPopupUtility(null);
        } else {
            pos.setPopupUtility(getPopupUtility().clone(pos, pos.getTextComponent()));
        }
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
    public JComponent getTextComponent() {
        return this;
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_PositionableJPanel");
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

    public boolean setEditTextItemMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("SetTextSizeColor")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editTextItem();
            }
        });
        return true;
    }

    TextItemPanel _itemPanel;

    protected void editTextItem() {
        _paletteFrame = makePaletteFrame(Bundle.getMessage("SetTextSizeColor"));
        _itemPanel = new TextItemPanel(_paletteFrame, "Text");
        ActionListener updateAction = (ActionEvent a) -> updateTextItem();
        _itemPanel.init(updateAction, this);
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    protected void updateTextItem() {
        PositionablePopupUtil util = _itemPanel.getPositionablePopupUtil();
        _itemPanel.setAttributes(this);
        if (_editor._selectionGroup != null) {
            _editor.setSelectionsAttributes(util, this);
        } else {
            _editor.setAttributes(util, this);
        }
        finishItemUpdate(_paletteFrame, _itemPanel);
    }

    public jmri.jmrit.display.DisplayFrame _paletteFrame;

    // ********** Methods for Item Popups in Control Panel editor *******************
    /**
     * Create a palette window.
     *
     * @param title the name of the palette
     * @return DisplayFrame for palette item
     */
    public DisplayFrame makePaletteFrame(String title) {
        jmri.jmrit.display.palette.ItemPalette.loadIcons();

        return new DisplayFrame(title, _editor);
    }

    public void initPaletteFrame(DisplayFrame paletteFrame, @Nonnull ItemPanel itemPanel) {
        Dimension dim = itemPanel.getPreferredSize();
        JScrollPane sp = new JScrollPane(itemPanel);
        dim = new Dimension(dim.width + 25, dim.height + 25);
        sp.setPreferredSize(dim);
        paletteFrame.add(sp);
        paletteFrame.pack();
        jmri.InstanceManager.getDefault(jmri.util.PlaceWindow.class).nextTo(_editor, this, paletteFrame);
        paletteFrame.setVisible(true);
    }

    public void finishItemUpdate(DisplayFrame paletteFrame, @Nonnull ItemPanel itemPanel) {
        itemPanel.closeDialogs();
        paletteFrame.dispose();
        invalidate();
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

    /**
     * For over-riding in the using classes: add item specific menu choices
     */
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
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    JFrame _iconEditorFrame;
    IconAdder _iconEditor;

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        return setEditIconMenu(popup);
    }

    protected void makeIconEditorFrame(Container pos, String name, boolean table, IconAdder editor) {
        if (editor != null) {
            _iconEditor = editor;
        } else {
            _iconEditor = new IconAdder(name);
        }
        _iconEditorFrame = _editor.makeAddIconFrame(name, false, table, _iconEditor);
        _iconEditorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
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
     * To be overridden if any special work needs to be done
     */
    void cleanup() {
    }

    boolean active = true;

    /**
     * @return true if this object is still displayed, and should be stored;
     *         false otherwise
     */
    public boolean isActive() {
        return active;
    }

    @Override
    public void mousePressed(JmriMouseEvent e) {
        _editor.mousePressed(new JmriMouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseReleased(JmriMouseEvent e) {
        _editor.mouseReleased(new JmriMouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseClicked(JmriMouseEvent e) {
        _editor.mouseClicked(new JmriMouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseExited(JmriMouseEvent e) {
//     transferFocus();
        _editor.mouseExited(new JmriMouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseEntered(JmriMouseEvent e) {
        _editor.mouseEntered(new JmriMouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseMoved(JmriMouseEvent e) {
        _editor.mouseMoved(new JmriMouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseDragged(JmriMouseEvent e) {
        _editor.mouseDragged(new JmriMouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    /**
     * ************************************************************
     */
    PositionablePopupUtil _popupUtil;

    @Override
    public void setPopupUtility(PositionablePopupUtil tu) {
        _popupUtil = tu;
    }

    @Override
    public PositionablePopupUtil getPopupUtility() {
        return _popupUtil;
    }

    /**
     * Update the AWT and Swing size information due to change in internal
     * state, e.g. if one or more of the icons that might be displayed is
     * changed
     */
    @Override
    public void updateSize() {
        invalidate();
        setSize(maxWidth(), maxHeight());
        if (log.isTraceEnabled()) {
            // the following fails when run on Jenkins under Xvfb with an NPE in non-JMRI code
            log.trace("updateSize: {}, text: w={} h={}",
                    _popupUtil.toString(),
                    getFontMetrics(_popupUtil.getFont()).stringWidth(_popupUtil.getText()),
                    getFontMetrics(_popupUtil.getFont()).getHeight());
        }
        validate();
        repaint();
    }

    @Override
    public int maxWidth() {
        int max = 0;
        if (_popupUtil != null) {
            if (_popupUtil.getFixedWidth() != 0) {
                max = _popupUtil.getFixedWidth();
                max += _popupUtil.getMargin() * 2;
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
                max += _popupUtil.getMargin() * 2;
                if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            }
        }
        log.debug("maxWidth= {} preferred width= {}", max, getPreferredSize().width);
        return max;
    }

    @Override
    public int maxHeight() {
        int max = 0;
        if (_popupUtil != null) {
            if (_popupUtil.getFixedHeight() != 0) {
                max = _popupUtil.getFixedHeight();
                max += _popupUtil.getMargin() * 2;
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
                if (_popupUtil != null) {
                    max += _popupUtil.getMargin() * 2;
                }
                if (max < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                    max = PositionablePopupUtil.MIN_SIZE;
                }
            }
        }
        log.debug("maxHeight= {} preferred width= {}", max, getPreferredSize().height);
        return max;
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

    private final static Logger log = LoggerFactory.getLogger(PositionableJPanel.class);
}
