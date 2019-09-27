package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.IconItemPanel;
import jmri.jmrit.display.palette.ItemPanel;
import jmri.util.MathUtil;
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PositionableLabel is a JLabel that can be dragged around the inside of the
 * enclosing Container using a right-drag.
 * <p>
 * The positionable parameter is a global, set from outside. The 'fixed'
 * parameter is local, set from the popup here.
 *
 * <a href="doc-files/Heirarchy.png"><img src="doc-files/Heirarchy.png" alt="UML class diagram for package" height="33%" width="33%"></a>
 * @author Bob Jacobsen Copyright (c) 2002
 */
public class PositionableLabel extends JLabel implements Positionable {

    protected Editor _editor;

    protected boolean _icon = false;
    protected boolean _text = false;
    protected boolean _control = false;
    protected NamedIcon _namedIcon;

    protected ToolTip _tooltip;
    protected boolean _showTooltip = true;
    protected boolean _editable = true;
    protected boolean _positionable = true;
    protected boolean _viewCoordinates = true;
    protected boolean _controlling = true;
    protected boolean _hidden = false;
    protected int _displayLevel;

    protected String _unRotatedText;
    protected boolean _rotateText = false;
    private int _degrees;

    /**
     *
     * @param editor where this label is displayed
     */
    public PositionableLabel(String s, @Nonnull Editor editor) {
        super(s);
        _editor = editor;
        _text = true;
        _unRotatedText = s;
        log.debug("PositionableLabel ctor (text) {}", s);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setPopupUtility(new PositionablePopupUtil(this, this));
    }

    public PositionableLabel(@CheckForNull NamedIcon s, @Nonnull Editor editor) {
        super(s);
        _editor = editor;
        _icon = true;
        _namedIcon = s;
        log.debug("PositionableLabel ctor (icon) {}", s != null ? s.getName() : null);
        setPopupUtility(new PositionablePopupUtil(this, this));
    }

    public final boolean isIcon() {
        return _icon;
    }

    public final boolean isText() {
        return _text;
    }

    public final boolean isControl() {
        return _control;
    }

    @Override
    public @Nonnull Editor getEditor() {
        return _editor;
    }

    @Override
    public void setEditor(@Nonnull Editor ed) {
        _editor = ed;
    }

    // *************** Positionable methods *********************
    @Override
    public void setPositionable(boolean enabled) {
        _positionable = enabled;
    }

    @Override
    public final boolean isPositionable() {
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
        if (_hidden != hide) {
            _hidden = hide;
            showHidden();
        }
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

    /**
     * Delayed setDisplayLevel for DnD.
     *
     * @param l the level to set
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
    @Nonnull
    public  String getNameString() {
        if (_icon && _displayLevel > Editor.BKG) {
            return "Icon";
        } else if (_text) {
            return "Text Label";
        } else {
            return "Background";
        }
    }

    /**
     * When text is rotated or in an icon mode, the return of getText() may be
     * null or some other value
     *
     * @return original defining text set by user
     */
    public String getUnRotatedText() {
        return _unRotatedText;
    }

    public void setUnRotatedText(String s) {
        _unRotatedText = s;
    }

    @Override
    @Nonnull
    public Positionable deepClone() {
        PositionableLabel pos;
        if (_icon) {
            NamedIcon icon = new NamedIcon((NamedIcon) getIcon());
            pos = new PositionableLabel(icon, _editor);
        } else {
            pos = new PositionableLabel(getText(), _editor);
        }
        return finishClone(pos);
    }

    protected @Nonnull Positionable finishClone(@Nonnull PositionableLabel pos) {
        pos._text = _text;
        pos._icon = _icon;
        pos._control = _control;
//        pos._rotateText = _rotateText;
        pos._unRotatedText = _unRotatedText;
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
        pos.setOpaque(isOpaque());
        if (_namedIcon != null) {
            pos._namedIcon = cloneIcon(_namedIcon, pos);
            pos.setIcon(pos._namedIcon);
            pos.rotate(_degrees);  //this will change text in icon with a new _namedIcon.
        }
        pos.updateSize();
        return pos;
    }

    @Override
    public @Nonnull JComponent getTextComponent() {
        return this;
    }

    public static @Nonnull NamedIcon cloneIcon(NamedIcon icon, PositionableLabel pos) {
        if (icon.getURL() != null) {
            return new NamedIcon(icon, pos);
        } else {
            NamedIcon clone = new NamedIcon(icon.getImage());
            clone.scale(icon.getScale(), pos);
            clone.rotate(icon.getDegrees(), pos);
            return clone;
        }
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

    /*
     * ************** end Positionable methods *********************
     */
    /**
     * *************************************************************
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
        int width = maxWidth();
        int height = maxHeight();
        log.trace("updateSize() w= {}, h= {} _namedIcon= {}", width, height, _namedIcon);

        setSize(width, height);
        if (_namedIcon != null && _text) {
            //we have a combined icon/text therefore the icon is central to the text.
            setHorizontalTextPosition(CENTER);
        }
    }

    @Override
    public int maxWidth() {
        if (_rotateText && _namedIcon != null) {
            return _namedIcon.getIconWidth();
        }
        if (_popupUtil == null) {
            return maxWidthTrue();
        }

        switch (_popupUtil.getOrientation()) {
            case PositionablePopupUtil.VERTICAL_DOWN:
            case PositionablePopupUtil.VERTICAL_UP:
                return maxHeightTrue();
            default:
                return maxWidthTrue();
        }
    }

    @Override
    public int maxHeight() {
        if (_rotateText && _namedIcon != null) {
            return _namedIcon.getIconHeight();
        }
        if (_popupUtil == null) {
            return maxHeightTrue();
        }
        switch (_popupUtil.getOrientation()) {
            case PositionablePopupUtil.VERTICAL_DOWN:
            case PositionablePopupUtil.VERTICAL_UP:
                return maxWidthTrue();
            default:
                return maxHeightTrue();
        }
    }

    public int maxWidthTrue() {
        int result = 0;
        if (_popupUtil != null && _popupUtil.getFixedWidth() != 0) {
            result = _popupUtil.getFixedWidth();
            result += _popupUtil.getBorderSize() * 2;
            if (result < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                _popupUtil.setFixedWidth(PositionablePopupUtil.MIN_SIZE);
                result = PositionablePopupUtil.MIN_SIZE;
            }
        } else {
            if (_text && getText() != null) {
                if (getText().trim().length() == 0) {
                    // show width of 1 blank character
                    if (getFont() != null) {
                        result = getFontMetrics(getFont()).stringWidth("0");
                    }
                } else {
                    result = getFontMetrics(getFont()).stringWidth(getText());
                }
            }
            if (_icon && _namedIcon != null) {
                result = Math.max(_namedIcon.getIconWidth(), result);
            }
            if (_text && _popupUtil != null) {
                result += _popupUtil.getMargin() * 2;
                result += _popupUtil.getBorderSize() * 2;
            }
            if (result < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                result = PositionablePopupUtil.MIN_SIZE;
            }
        }
        if (log.isTraceEnabled()) { // avoid AWT size computation
            log.trace("maxWidth= {} preferred width= {}", result, getPreferredSize().width);
        }
        return result;
    }

    public int maxHeightTrue() {
        int result = 0;
        if (_popupUtil != null && _popupUtil.getFixedHeight() != 0) {
            result = _popupUtil.getFixedHeight();
            result += _popupUtil.getBorderSize() * 2;
            if (result < PositionablePopupUtil.MIN_SIZE) {   // don't let item disappear
                _popupUtil.setFixedHeight(PositionablePopupUtil.MIN_SIZE);
            }
        } else {
            //if(_text) {
            if (_text && getText() != null && getFont() != null) {
                result = getFontMetrics(getFont()).getHeight();
            }
            if (_icon && _namedIcon != null) {
                result = Math.max(_namedIcon.getIconHeight(), result);
            }
            if (_text && _popupUtil != null) {
                result += _popupUtil.getMargin() * 2;
                result += _popupUtil.getBorderSize() * 2;
            }
            if (result < PositionablePopupUtil.MIN_SIZE) {  // don't let item disappear
                result = PositionablePopupUtil.MIN_SIZE;
            }
        }
        if (log.isTraceEnabled()) { // avoid AWT size computation
            log.trace("maxHeight= {} preferred height= {}", result, getPreferredSize().height);
        }
        return result;
    }

    public boolean isBackground() {
        return (_displayLevel == Editor.BKG);
    }

    public boolean isRotated() {
        return _rotateText;
    }

    public void updateIcon(NamedIcon s) {
        _namedIcon = s;
        super.setIcon(_namedIcon);
        updateSize();
        repaint();
    }

    /*
     * ***** Methods to add menu items to popup *******
     */

    /**
     * Call to a Positionable that has unique requirements - e.g.
     * RpsPositionIcon, SecurityElementIcon
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    /**
     * Rotate othogonally return true if popup is set
     */
    @Override
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {

        if (isIcon() && (_displayLevel > Editor.BKG) && (_namedIcon != null)) {
            popup.add(new AbstractAction(Bundle.getMessage("RotateOrthoSign",
                    (_namedIcon.getRotation() * 90))) { // Bundle property includes degree symbol
                @Override
                public void actionPerformed(ActionEvent e) {
                    rotateOrthogonal();
                }
            });
            return true;
        }
        return false;
    }

    protected void rotateOrthogonal() {
        _namedIcon.setRotation(_namedIcon.getRotation() + 1, this);
        super.setIcon(_namedIcon);
        updateSize();
        repaint();
    }

/*    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        return setEditIconMenu(popup);
    }*/

    /*
     * ********** Methods for Item Popups in Panel editor ************************
     */
    JFrame _iconEditorFrame;
    IconAdder _iconEditor;

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        if (_icon && !_text) {
            String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Icon"));
            popup.add(new AbstractAction(txt) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
            return true;
        }
        return false;
    }

    /**
     * For item popups in Panel Editor.
     *
     * @param pos    the container
     * @param name   the name
     * @param table  true if creating a table; false otherwise
     * @param editor the associated editor
     */
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

    protected void edit() {
        makeIconEditorFrame(this, "Icon", false, null);
        NamedIcon icon = new NamedIcon(_namedIcon);
        _iconEditor.setIcon(0, "plainIcon", icon);
        _iconEditor.makeIconPanel(false);

        ActionListener addIconAction = (ActionEvent a) -> {
            editIcon();
        };
        _iconEditor.complete(addIconAction, true, false, true);

    }

    protected void editIcon() {
        String url = _iconEditor.getIcon("plainIcon").getURL();
        _namedIcon = NamedIcon.getIconByName(url);
        super.setIcon(_namedIcon);
        updateSize();
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
        repaint();
    }

    public jmri.jmrit.display.DisplayFrame _paletteFrame;

    //
    // ********** Methods for Item Popups in Control Panel editor *******************
    //
    /**
     * Create a palette window.
     *
     * @param title the name of the palette
     * @return DisplayFrame for palette item
     */
    public DisplayFrame makePaletteFrame(String title) {
        jmri.jmrit.display.palette.ItemPalette.loadIcons(_editor);

        DisplayFrame paletteFrame = new DisplayFrame(title, false, false);
//        paletteFrame.setLocationRelativeTo(this);
//        paletteFrame.toFront();
        return paletteFrame;
    }

    public void initPaletteFrame(DisplayFrame paletteFrame, @Nonnull ItemPanel itemPanel) {
        Dimension dim = itemPanel.getPreferredSize();
        JScrollPane sp = new JScrollPane(itemPanel);
        dim = new Dimension(dim.width + 25, dim.height + 25);
        sp.setPreferredSize(dim);
        paletteFrame.add(sp);
        paletteFrame.pack();
        paletteFrame.setLocation(jmri.util.PlaceWindow.nextTo(_editor, this, paletteFrame));
        paletteFrame.setVisible(true);
    }

    public void finishItemUpdate(DisplayFrame paletteFrame, @Nonnull ItemPanel itemPanel) {
        itemPanel.closeDialogs();
        paletteFrame.dispose();
        invalidate();
    }

    @Override
    public boolean setEditItemMenu(@Nonnull JPopupMenu popup) {
        if (!_icon) {
            return false;
        }
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Icon"));
        popup.add(new AbstractAction(txt) {

            @Override
            public void actionPerformed(ActionEvent e) {
                editIconItem();
            }
        });
        return true;
    }

    IconItemPanel _iconItemPanel;

    protected void editIconItem() {
        _paletteFrame = makePaletteFrame(
                java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameTurnout")));
        _iconItemPanel = new IconItemPanel(_paletteFrame, "Icon", _editor); // NOI18N
        ActionListener updateAction = (ActionEvent a) -> {
                updateIconItem();
         };
        _iconItemPanel.init(updateAction);
        initPaletteFrame(_paletteFrame, _iconItemPanel);
    }

    private void updateIconItem() {
        NamedIcon icon = _iconItemPanel.getIcon();
        if (icon != null) {
            String url = icon.getURL();
            setIcon(NamedIcon.getIconByName(url));
            updateSize();
        }
        _paletteFrame.dispose();
        _paletteFrame = null;
        _iconItemPanel = null;
        invalidate();
    }
/* future use to replace editor.setTextAttributes
    public boolean setEditTextMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("TextAttributes"), Bundle.getMessage("Text"));
        popup.add(new AbstractAction(txt) {

            @Override
            public void actionPerformed(ActionEvent e) {
                editTextItem();
            }
        });
        return true;
    }

    TextItemPanel _textItemPanel;
    
    protected void editTextItem() {
        makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("TextAttributes"), Bundle.getMessage("BeanNameTurnout")));
        _textItemPanel = new TextItemPanel(_paletteFrame, "Text", _editor); // NOI18N
        ActionListener updateAction = (ActionEvent a) -> {
                updateTextItem();
         };
        _textItemPanel.init(updateAction, this);
        initPaletteFrame(_textItemPanel);
    }

    private void updateTextItem() {
        _textItemPanel.updateAttributes(this);
        updateSize();
        _paletteFrame.dispose();
        _paletteFrame = null;
        _iconItemPanel = null;
        invalidate();
    }*/

    /**
     * Rotate degrees return true if popup is set.
     */
    @Override
    public boolean setRotateMenu(@Nonnull JPopupMenu popup) {
        if (_displayLevel > Editor.BKG) {
             popup.add(CoordinateEdit.getRotateEditAction(this));
        }
        return false;
    }

    /**
     * Scale percentage form display.
     *
     * @return true if popup is set
     */
    @Override
    public boolean setScaleMenu(@Nonnull JPopupMenu popup) {
        if (isIcon() && _displayLevel > Editor.BKG) {
            popup.add(CoordinateEdit.getScaleEditAction(this));
            return true;
        }
        return false;
    }

    @Override
    public boolean setTextEditMenu(@Nonnull JPopupMenu popup) {
        if (isText()) {
            popup.add(CoordinateEdit.getTextEditAction(this, "EditText"));
            return true;
        }
        return false;
    }

    JCheckBoxMenuItem disableItem = null;

    @Override
    public boolean setDisableControlMenu(@Nonnull JPopupMenu popup) {
        if (_control) {
            disableItem = new JCheckBoxMenuItem(Bundle.getMessage("Disable"));
            disableItem.setSelected(!_controlling);
            popup.add(disableItem);
            disableItem.addActionListener((java.awt.event.ActionEvent e) -> {
                setControlling(!disableItem.isSelected());
            });
            return true;
        }
        return false;
    }

    @Override
    public void setScale(double s) {
        if (_namedIcon != null) {
            _namedIcon.scale(s, this);
            super.setIcon(_namedIcon);
            updateSize();
            repaint();
        }
    }

    @Override
    public double getScale() {
        if (_namedIcon == null) {
            return 1.0;
        }
        return ((NamedIcon) getIcon()).getScale();
    }

    public void setIcon(NamedIcon icon) {
        _namedIcon = icon;
        super.setIcon(icon);
    }

    @Override
    public void rotate(int deg) {
        if (log.isDebugEnabled()) {
            log.debug("rotate({}) with _rotateText {}, _text {}, _icon {}", deg, _rotateText, _text, _icon);
        }
        _degrees = deg;

        if ((deg != 0) && (_popupUtil.getOrientation() != PositionablePopupUtil.HORIZONTAL)) {
            _popupUtil.setOrientation(PositionablePopupUtil.HORIZONTAL);
        }

        if (_rotateText || deg == 0) {
            if (deg == 0) {             // restore unrotated whatever
                _rotateText = false;
                if (_text) {
                    if (log.isDebugEnabled()) {
                        log.debug("   super.setText(\"{}\");", _unRotatedText);
                    }
                    super.setText(_unRotatedText);
                    if (_popupUtil != null) {
                        setOpaque(_popupUtil.hasBackground());
                        _popupUtil.setBorder(true);
                    }
                    if (_namedIcon != null) {
                        String url = _namedIcon.getURL();
                        if (url == null) {
                            if (_text & _icon) {    // create new text over icon
                                _namedIcon = makeTextOverlaidIcon(_unRotatedText, _namedIcon);
                                _namedIcon.rotate(deg, this);
                            } else if (_text) {
                                _namedIcon = null;
                            }
                        } else {
                            _namedIcon = new NamedIcon(url, url);
                        }
                    }
                    super.setIcon(_namedIcon);
                } else {
                    if (_namedIcon != null) {
                        _namedIcon.rotate(deg, this);
                    }
                    super.setIcon(_namedIcon);
                }
            } else {
                if (_text & _icon) {    // update text over icon
                    _namedIcon = makeTextOverlaidIcon(_unRotatedText, _namedIcon);
                } else if (_text) {     // update text only icon image
                    _namedIcon = makeTextIcon(_unRotatedText);
                }
                _namedIcon.rotate(deg, this);
                super.setIcon(_namedIcon);
                setOpaque(false);   // rotations cannot be opaque
            }
        } else {  // first time text or icon is rotated from horizontal
            if (_text && _icon) {   // text overlays icon  e.g. LocoIcon
                _namedIcon = makeTextOverlaidIcon(_unRotatedText, _namedIcon);
                super.setText(null);
                _rotateText = true;
                setOpaque(false);
            } else if (_text) {
                _namedIcon = makeTextIcon(_unRotatedText);
                super.setText(null);
                _rotateText = true;
                setOpaque(false);
            }
            if (_popupUtil != null) {
                _popupUtil.setBorder(false);
            }
            _namedIcon.rotate(deg, this);
            super.setIcon(_namedIcon);
        }
        updateSize();
        repaint();
    }   // rotate

    /**
     * Create an image of icon with overlaid text.
     *
     * @param text the text to overlay
     * @param ic   the icon containing the image
     * @return the icon overlaying text on ic
     */
    protected NamedIcon makeTextOverlaidIcon(String text, @Nonnull NamedIcon ic) {
        String url = ic.getURL();
        if (url == null) {
            return null;
        }
        NamedIcon icon = new NamedIcon(url, url);

        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();

        int textWidth = getFontMetrics(getFont()).stringWidth(text);
        int textHeight = getFontMetrics(getFont()).getHeight();

        int width = Math.max(textWidth, iconWidth);
        int height = Math.max(textHeight, iconHeight);

        int hOffset = Math.max((textWidth - iconWidth) / 2, 0);
        int vOffset = Math.max((textHeight - iconHeight) / 2, 0);

        if (_popupUtil != null) {
            if (_popupUtil.getFixedWidth() != 0) {
                switch (_popupUtil.getJustification()) {
                    case PositionablePopupUtil.LEFT:
                        hOffset = _popupUtil.getBorderSize();
                        break;
                    case PositionablePopupUtil.RIGHT:
                        hOffset = _popupUtil.getFixedWidth() - width;
                        hOffset += _popupUtil.getBorderSize();
                        break;
                    default:
                        hOffset = Math.max((_popupUtil.getFixedWidth() - width) / 2, 0);
                        hOffset += _popupUtil.getBorderSize();
                        break;
                }
                width = _popupUtil.getFixedWidth() + 2 * _popupUtil.getBorderSize();
            } else {
                width += 2 * (_popupUtil.getMargin() + _popupUtil.getBorderSize());
                hOffset += _popupUtil.getMargin() + _popupUtil.getBorderSize();
            }
            if (_popupUtil.getFixedHeight() != 0) {
                vOffset = Math.max(vOffset + (_popupUtil.getFixedHeight() - height) / 2, 0);
                vOffset += _popupUtil.getBorderSize();
                height = _popupUtil.getFixedHeight() + 2 * _popupUtil.getBorderSize();
            } else {
                height += 2 * (_popupUtil.getMargin() + _popupUtil.getBorderSize());
                vOffset += _popupUtil.getMargin() + _popupUtil.getBorderSize();
            }
        }

        BufferedImage bufIm = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufIm.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   // Turned off due to poor performance, see Issue #3850 and PR #3855 for background
//                 RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        if (_popupUtil != null) {
            if (_popupUtil.hasBackground()) {
                g2d.setColor(_popupUtil.getBackground());
                g2d.fillRect(0, 0, width, height);
            }
            if (_popupUtil.getBorderSize() != 0) {
                g2d.setColor(_popupUtil.getBorderColor());
                g2d.setStroke(new java.awt.BasicStroke(2 * _popupUtil.getBorderSize()));
                g2d.drawRect(0, 0, width, height);
            }
        }

        g2d.drawImage(icon.getImage(), AffineTransform.getTranslateInstance(hOffset, vOffset + 1), this);

        if (false) {    //TODO: dead-strip this; the string is now drawn in paintComponent
            g2d.setFont(getFont());
            hOffset = Math.max((width - textWidth) / 2, 0);
            vOffset = Math.max((height - textHeight) / 2, 0) + getFontMetrics(getFont()).getAscent();
            g2d.setColor(getForeground());
            g2d.drawString(text, hOffset, vOffset);
        }

        icon = new NamedIcon(bufIm);
        g2d.dispose();
        icon.setURL(url);
        return icon;
    }

    /**
     * Create a text image whose bit map can be rotated.
     */
    private NamedIcon makeTextIcon(String text) {
        if (text == null || text.equals("")) {
            text = " ";
        }
        int width = getFontMetrics(getFont()).stringWidth(text);
        int height = getFontMetrics(getFont()).getHeight();
        // int hOffset = 0;  // variable has no effect, see Issue #5662
        // int vOffset = getFontMetrics(getFont()).getAscent();
        if (_popupUtil != null) {
            if (_popupUtil.getFixedWidth() != 0) {
                switch (_popupUtil.getJustification()) {
                    case PositionablePopupUtil.LEFT:
                        // hOffset = _popupUtil.getBorderSize(); // variable has no effect, see Issue #5662
                        break;
                    case PositionablePopupUtil.RIGHT:
                        // hOffset = _popupUtil.getFixedWidth() - width; // variable has no effect, see Issue #5662
                        // hOffset += _popupUtil.getBorderSize(); // variable has no effect, see Issue #5662
                        break;
                    default:
                        // hOffset = Math.max((_popupUtil.getFixedWidth() - width) / 2, 0); // variable has no effect, see Issue #5662
                        // hOffset += _popupUtil.getBorderSize(); // variable has no effect, see Issue #5662
                        break;
                }
                width = _popupUtil.getFixedWidth() + 2 * _popupUtil.getBorderSize();
            } else {
                width += 2 * (_popupUtil.getMargin() + _popupUtil.getBorderSize());
                // hOffset += _popupUtil.getMargin() + _popupUtil.getBorderSize(); // variable has no effect, see Issue #5662
            }
            if (_popupUtil.getFixedHeight() != 0) {
                // vOffset = Math.max(vOffset + (_popupUtil.getFixedHeight() - height) / 2, 0);
                // vOffset += _popupUtil.getBorderSize();
                height = _popupUtil.getFixedHeight() + 2 * _popupUtil.getBorderSize();
            } else {
                height += 2 * (_popupUtil.getMargin() + _popupUtil.getBorderSize());
                // vOffset += _popupUtil.getMargin() + _popupUtil.getBorderSize();
            }
        }

        BufferedImage bufIm = new BufferedImage(width + 2, height + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufIm.createGraphics();

        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.clearRect(0, 0, bufIm.getWidth(), bufIm.getHeight());

        g2d.setFont(getFont());
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   // Turned off due to poor performance, see Issue #3850 and PR #3855 for background
//                 RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        if (_popupUtil != null) {
            if (_popupUtil.hasBackground()) {
                g2d.setColor(_popupUtil.getBackground());
                g2d.fillRect(0, 0, width, height);
            }
            if (_popupUtil.getBorderSize() != 0) {
                g2d.setColor(_popupUtil.getBorderColor());
                g2d.setStroke(new java.awt.BasicStroke(2 * _popupUtil.getBorderSize()));
                g2d.drawRect(0, 0, width, height);
            }
        }

        NamedIcon icon = new NamedIcon(bufIm);
        g2d.dispose();
        return icon;
    }

    public void setDegrees(int deg) {
        _degrees = deg;
    }

    @Override
    public int getDegrees() {
        return _degrees;
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    public void dispose() {
    }

    /**
     * Removes this object from display and persistance
     */
    @Override
    public void remove() {
        if (_editor.removeFromContents(this)) {
            // Modified to support conditional delete for NX sensors
            // remove from persistance by flagging inactive
            active = false;
            dispose();
        }
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

    protected void setSuperText(String text) {
        _unRotatedText = text;
        super.setText(text);
    }

    @Override
    public void setText(String text) {
        _unRotatedText = text;
        _text = (text != null && text.length() > 0);  // when "" is entered for text, and a font has been specified, the descender distance moves the position
        if (/*_rotateText &&*/!isIcon() && _namedIcon != null) {
            log.debug("setText calls rotate({})", _degrees);
            rotate(_degrees);  //this will change text label as a icon with a new _namedIcon.
        } else {
            log.debug("setText calls super.setText()");
            super.setText(text);
        }
    }

    private boolean needsRotate;

    @Override
    public Dimension getSize() {
        if (!needsRotate) {
            return super.getSize();
        }

        Dimension size = super.getSize();
        if (_popupUtil == null) {
            return super.getSize();
        }
        switch (_popupUtil.getOrientation()) {
            case PositionablePopupUtil.VERTICAL_DOWN:
            case PositionablePopupUtil.VERTICAL_UP:
                if (_degrees != 0) {
                    rotate(0);
                }
                return new Dimension(size.height, size.width); // flip dimension
            default:
                return super.getSize();
        }
    }

    @Override
    public int getHeight() {
        return getSize().height;
    }

    @Override
    public int getWidth() {
        return getSize().width;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (_popupUtil == null) {
            super.paintComponent(g);
        } else {
            Graphics2D g2d = (Graphics2D) g.create();

            // set antialiasing hint for macOS and Windows
            // note: antialiasing has performance problems on some variants of Linux (Raspberry pi)
            if (SystemType.isMacOSX() || SystemType.isWindows()) {
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//                 g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   // Turned off due to poor performance, see Issue #3850 and PR #3855 for background
//                         RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }

            switch (_popupUtil.getOrientation()) {
                case PositionablePopupUtil.VERTICAL_UP:
                    g2d.translate(0, getSize().getHeight());
                    g2d.transform(AffineTransform.getQuadrantRotateInstance(-1));
                    break;
                case PositionablePopupUtil.VERTICAL_DOWN:
                    g2d.transform(AffineTransform.getQuadrantRotateInstance(1));
                    g2d.translate(0, -getSize().getWidth());
                    break;
                case 0: 
                    // routine value (not initialized) for no change
                    break;
                default:
                    // unexpected orientation value
                    jmri.util.Log4JUtil.warnOnce(log, "Unexpected orientation = {}", _popupUtil.getOrientation());
                    break;
            }

            needsRotate = true;
            super.paintComponent(g2d);
            needsRotate = false;

            if (_popupUtil.getOrientation() == PositionablePopupUtil.HORIZONTAL) {
                if ((_unRotatedText != null) && (_degrees != 0)) {
                    double angleRAD = Math.toRadians(_degrees);

                    int iconWidth = getWidth();
                    int iconHeight = getHeight();

                    int textWidth = getFontMetrics(getFont()).stringWidth(_unRotatedText);
                    int textHeight = getFontMetrics(getFont()).getHeight();

                    Point2D textSizeRotated = MathUtil.rotateRAD(textWidth, textHeight, angleRAD);
                    int textWidthRotated = (int) textSizeRotated.getX();
                    int textHeightRotated = (int) textSizeRotated.getY();

                    int width = Math.max(textWidthRotated, iconWidth);
                    int height = Math.max(textHeightRotated, iconHeight);

                    int iconOffsetX = width / 2;
                    int iconOffsetY = height / 2;

                    g2d.transform(AffineTransform.getRotateInstance(angleRAD, iconOffsetX, iconOffsetY));

                    int hOffset = iconOffsetX - (textWidth / 2);
                    //int vOffset = iconOffsetY + ((textHeight - getFontMetrics(getFont()).getAscent()) / 2);
                    int vOffset = iconOffsetY + (textHeight / 4);   // why 4? Don't know, it just looks better

                    g2d.setFont(getFont());
                    g2d.setColor(getForeground());
                    g2d.drawString(_unRotatedText, hOffset, vOffset);
                }
            }
        }
    }   // paintComponent

    /**
     * Provide a generic method to return the bean associated with the
     * Positionable.
     */
    @Override
    public jmri.NamedBean getNamedBean() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableLabel.class);

}
