package jmri.jmrit.display;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <a href="doc-files/Heirarchy.png"><img src="doc-files/Heirarchy.png" alt="UML class diagram for package" height="33%" width="33%"></a>
 * @author Bob Jacobsen copyright (C) 2009
 */
public class PositionableJPanel extends JPanel implements Positionable, MouseListener, MouseMotionListener {

    protected Editor _editor = null;

    private ToolTip _tooltip;
    protected boolean _showTooltip = true;
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

    public void setLevel(int l) {
        _displayLevel = l;
    }

    @Override
    public void setDisplayLevel(int l) {
        int oldDisplayLevel = _displayLevel;
        _displayLevel = l;
        if (oldDisplayLevel != l) {
            log.debug("Changing label display level from " + oldDisplayLevel + " to " + _displayLevel);
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
    public void doMousePressed(MouseEvent event) {
    }

    @Override
    public void doMouseReleased(MouseEvent event) {
    }

    @Override
    public void doMouseClicked(MouseEvent event) {
    }

    @Override
    public void doMouseDragged(MouseEvent event) {
    }

    @Override
    public void doMouseMoved(MouseEvent event) {
    }

    @Override
    public void doMouseEntered(MouseEvent event) {
    }

    @Override
    public void doMouseExited(MouseEvent event) {
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

    /**
     * ************** end Positionable methods *********************
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
    public void mousePressed(MouseEvent e) {
        _editor.mousePressed(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        _editor.mouseReleased(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        _editor.mouseClicked(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseExited(MouseEvent e) {
//     transferFocus();
        _editor.mouseExited(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        _editor.mouseEntered(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        _editor.mouseMoved(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
                e.getX() + this.getX(), e.getY() + this.getY(),
                e.getClickCount(), e.isPopupTrigger()));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        _editor.mouseDragged(new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiersEx(),
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

    private final static Logger log = LoggerFactory.getLogger(PositionableJPanel.class);
}
