package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import jmri.util.MenuScroller;
import jmri.util.swing.JmriColorChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles text attributes for Positionables. Font, size, style and
 * color. Margin size and color, Border size and color, Fixed sizes.
 * Justification.
 * <p>
 * moved from PositionableLabel
 *
 * @author Pete Cressman copyright (C) 2010
 */
public class PositionablePopupUtil {

    protected JComponent _textComponent;    // closest ancestor for JLabel and JTextField
    protected int _textType;                // JComponent does not have text, used for casting
    protected Positionable _parent;
    protected PositionablePopupUtil _self;
    protected PositionablePropertiesUtil _propertiesUtil;

    private final Color defaultBorderColor;
    private boolean _suppressRecentColor = false;

    protected final int LABEL = 1;
    protected final int TEXTFIELD = 2;
    protected final int JCOMPONENT = 3;

    public PositionablePopupUtil(Positionable parent, JComponent textComp) {
        _parent = parent;
        if (textComp instanceof JLabel) {
            _textType = LABEL;
        } else if (textComp instanceof JTextField) {
            _textType = TEXTFIELD;
        } else {
            _textType = JCOMPONENT;
        }
        _textComponent = textComp;
        _self = this;

        defaultBorderColor = _parent.getBackground();
        _propertiesUtil = new PositionablePropertiesUtil(_parent);
    }

    public PositionablePopupUtil clone(Positionable parent, JComponent textComp) {
        PositionablePopupUtil util = new PositionablePopupUtil(parent, textComp);
        util.setJustification(getJustification());
        util.setHorizontalAlignment(getJustification());
        util.setFixedWidth(getFixedWidth());
        util.setFixedHeight(getFixedHeight());
        util.setMargin(getMargin());
        util.setBorderSize(getBorderSize());
        util.setBorderColor(getBorderColor());
        util.setFont(getFont().deriveFont(getFontStyle()));
        util.setFontSize(getFontSize());
        util.setFontStyle(getFontStyle());
        util.setOrientation(getOrientation());
        util.setBackgroundColor(getBackground());
        util.setForeground(getForeground());
        util.setHasBackground(hasBackground());     // must do this AFTER setBackgroundColor
        return util;
    }

    @Override
    public String toString() {
        return _parent.getNameString() + ": fixedWidth= " + fixedWidth + ", fixedHeight= " + fixedHeight
                + ", margin= " + margin + ", borderSize= " + borderSize;
    }

    /**
     * *************************************************************************************
     */
    static final public int FONT_COLOR = 0x00;
    static final public int BACKGROUND_COLOR = 0x01;
    static final public int BORDER_COLOR = 0x02;
    static final public int MIN_SIZE = 5;

    private int fixedWidth = 0;
    private int fixedHeight = 0;
    private int margin = 0;
    private int borderSize = 0;
    private Color borderColor = null;
    private Border borderMargin = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    private Border outlineBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    private boolean _hasBackground;      // Should background be painted or clear

    JMenuItem italic = null;
    JMenuItem bold = null;

    public void propertyUtil(JPopupMenu popup) {
        JMenuItem edit = new JMenuItem(Bundle.getMessage("MenuItemProperties") + "...");
        edit.addActionListener((ActionEvent e) -> {
            _propertiesUtil.display();
        });
        popup.add(edit);
    }

    public void setFixedTextMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditFixed"));
        JMenuItem jmi;
        if (getFixedWidth() == 0) {
            jmi = edit.add("Width = Auto");
        } else {
            jmi = edit.add("Width = " + _parent.maxWidth());
        }
        jmi.setEnabled(false);

        if (getFixedHeight() == 0) {
            jmi = edit.add("Height = Auto");
        } else {
            jmi = edit.add("Height = " + _parent.maxHeight());
        }
        jmi.setEnabled(false);

        edit.add(CoordinateEdit.getFixedSizeEditAction(_parent));

        popup.add(edit);
    }

    public void setTextMarginMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditMargin"));
        if ((fixedHeight == 0) || (fixedWidth == 0)) {
            JMenuItem jmi = edit.add("Margin = " + getMargin());
            jmi.setEnabled(false);
            edit.add(CoordinateEdit.getMarginEditAction(_parent));
        }
        popup.add(edit);
    }

    public void setBackgroundMenu(JPopupMenu popup) {
        JMenuItem edit = new JMenuItem(Bundle.getMessage("FontBackgroundColor"));
        edit.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(_textComponent,
                                 Bundle.getMessage("FontBackgroundColor"),
                                 getBackground());
            if (desiredColor!=null ) {
               setBackgroundColor(desiredColor);
           }
        });

        popup.add(edit);

    }

    public void setTextBorderMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditBorder"));
        JMenuItem jmi = edit.add("Border Size = " + borderSize);
        jmi.setEnabled(false);
        edit.add(CoordinateEdit.getBorderEditAction(_parent));
        JMenuItem colorMenu = new JMenuItem(Bundle.getMessage("BorderColorMenu"));
        colorMenu.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(_textComponent,
                                 Bundle.getMessage("BorderColorMenu"),
                                 defaultBorderColor);
            if (desiredColor!=null ) {
               setBorderColor(desiredColor);
           }
        });
        edit.add(colorMenu);
        popup.add(edit);
    }

    public void setTextFontMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditFont"));
        edit.add(makeFontMenu());
        edit.add(makeFontSizeMenu());
        edit.add(makeFontStyleMenu());
        JMenuItem colorMenu = new JMenuItem(Bundle.getMessage("FontColor"));
        colorMenu.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(_textComponent,
                                 Bundle.getMessage("FontColor"),
                                 _textComponent.getForeground());
            if (desiredColor!=null ) {
               _textComponent.setForeground(desiredColor);
           }
        });
        edit.add(colorMenu);
        popup.add(edit);
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int m) {
        margin = m;
        if (_parent.isOpaque()) {
            borderMargin = new LineBorder(getBackground(), m);
        } else {
            borderMargin = BorderFactory.createEmptyBorder(m, m, m, m);
        }
        if (_showBorder) {
            _parent.setBorder(new CompoundBorder(outlineBorder, borderMargin));
        }
        _parent.updateSize();
    }

    public int getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(int w) {
        fixedWidth = w;
        if (log.isDebugEnabled()) {
            log.debug("setFixedWidth()=" + getFixedWidth());
        }
        _parent.updateSize();
    }

    public int getFixedHeight() {
        return fixedHeight;
    }

    public void setFixedHeight(int h) {
        fixedHeight = h;
        if (log.isDebugEnabled()) {
            log.debug("setFixedHeight()=" + getFixedHeight());
        }
        _parent.updateSize();
    }

    public void setFixedSize(int w, int h) {
        fixedWidth = w;
        fixedHeight = h;
        if (log.isDebugEnabled()) {
            log.debug("setFixedSize()=" + "(" + getFixedWidth() + "," + getFixedHeight() + ")");
        }
        _parent.updateSize();
    }

    public void setBorderSize(int border) {
        borderSize = border;

        if (borderColor != null) {
            outlineBorder = new LineBorder(borderColor, borderSize);
            _parent.setBorder(new CompoundBorder(outlineBorder, borderMargin));
            //setHorizontalAlignment(CENTRE);
        }
        _parent.updateSize();
    }

    private boolean _showBorder = true;

    public void setBorder(boolean set) {
        _showBorder = set;
        if (set) {
            if (borderColor != null ) {
                outlineBorder = new LineBorder(borderColor, borderSize);
                _parent.setBorder(new CompoundBorder(outlineBorder, borderMargin));
            }
        } else {
            _parent.setBorder(null);
        }
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderColor(Color border) {
        borderColor = border;
        if (borderColor != null && _showBorder) {
            outlineBorder = new LineBorder(borderColor, borderSize);
            _parent.setBorder(new CompoundBorder(outlineBorder, borderMargin));
        }
        if (!_suppressRecentColor) {
            JmriColorChooser.addRecentColor(border);
        }
    }

    public Color getBorderColor() {
        if (borderColor == null) {
            borderColor = _parent.getBackground();
        }
        return borderColor;
    }

    public void setForeground(Color c) {
        _textComponent.setForeground(c);
        _parent.updateSize();
        if (!_suppressRecentColor) {
            JmriColorChooser.addRecentColor(c);
        }
    }

    public Color getForeground() {
        return _textComponent.getForeground();
    }

    public void setBackgroundColor(Color color) {
        if (color == null || color.getAlpha() == 0) {
            setHasBackground(false);
            _textComponent.setBackground(color); // retain the passed color
                                                 // which may not be null
        } else {
            setHasBackground(true);
            _textComponent.setBackground(color);
            _parent.setBackground(color);
            if (!_suppressRecentColor) {
                JmriColorChooser.addRecentColor(color);
            }
        }
        if (hasBackground()) {
            setMargin(margin);  //This rebuilds margin and sets it colour.
        }
        _parent.updateSize();
    }
    
    public void setSuppressRecentColor(boolean b) {
        _suppressRecentColor = b;
    }

    public void setHasBackground(boolean set) {
        _hasBackground = set;
        if (_textComponent instanceof PositionableJPanel) {
            _textComponent.setOpaque(_hasBackground);
        }
        if (!_hasBackground) {
            _parent.setOpaque(false);
            _textComponent.setOpaque(false);
        }
    }

    public boolean hasBackground() {
        return _hasBackground;
    }

    public Color getBackground() {
        Color c = _textComponent.getBackground();
        if (c==null) {
            c = Color.WHITE;
        }
        if (!_hasBackground) {
            // make sure the alpha value is set to 0
            c = jmri.util.ColorUtil.setAlpha(c,0);
        }
        return c;
    }

    protected JMenu makeFontMenu() {
        JMenu fontMenu = new JMenu("Font"); // create font menu
        //fontMenu.setMnemonic('n'); // set mnemonic to n

        // get the current font family name
        String defaultFontFamilyName = _textComponent.getFont().getFamily();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String fontFamilyNames[] = ge.getAvailableFontFamilyNames();

        // create radiobutton menu items for font names
        ButtonGroup fontButtonGroup = new ButtonGroup(); // manages font names

        // create Font radio button menu items
        for (String fontFamilyName : fontFamilyNames) {
            // create its menu item
            JCheckBoxMenuItem fontMenuItem = new JCheckBoxMenuItem(fontFamilyName);
            Font menuFont = fontMenuItem.getFont();
            menuFont = new Font(fontFamilyName, menuFont.getStyle(), menuFont.getSize());
            fontMenuItem.setFont(menuFont);

            // set its action listener
            fontMenuItem.addActionListener((ActionEvent e) -> {
                Font oldFont = _textComponent.getFont();
                Font newFont = new Font(fontFamilyName, oldFont.getStyle(), oldFont.getSize());
                if (!oldFont.equals(newFont)) {
                    setFont(newFont);
                }
            });

            // add to button group
            fontButtonGroup.add(fontMenuItem);
            // set (de)selected
            fontMenuItem.setSelected(defaultFontFamilyName.equals(fontFamilyName));
            // add to font menu
            fontMenu.add(fontMenuItem);
        }

        MenuScroller.setScrollerFor(fontMenu, 36);

        return fontMenu;
    }

    protected JMenu makeFontSizeMenu() {
        JMenu sizeMenu = new JMenu("Font Size");
        ButtonGroup buttonGrp = new ButtonGroup();
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 6);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 8);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 10);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 11);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 12);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 14);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 16);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 18);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 20);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 24);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 28);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 32);
        addFontSizeMenuEntry(sizeMenu, buttonGrp, 36);
        return sizeMenu;
    }

    void addFontSizeMenuEntry(JMenu menu, ButtonGroup fontButtonGroup, final int size) {
        JRadioButtonMenuItem r = new JRadioButtonMenuItem("" + size);
        r.addActionListener((ActionEvent e) -> {
            setFontSize(size);
        });
        fontButtonGroup.add(r);
        r.setSelected(_textComponent.getFont().getSize() == size);
        menu.add(r);
    }

    public void setFont(Font font) {
        Font oldFont = _textComponent.getFont();
        Font newFont = new Font(font.getFamily(), oldFont.getStyle(), oldFont.getSize());
        if (!oldFont.equals(newFont)) {
            _textComponent.setFont(newFont);
            _parent.updateSize();
            _parent.getEditor().setAttributes(_self, _parent);
        }
    }

    public Font getFont() {
        return _textComponent.getFont();
    }

    public void setFontSize(float newSize) {
        _textComponent.setFont(_textComponent.getFont().deriveFont(newSize));
        _parent.updateSize();
        ///_parent.getEditor().setAttributes(_self, _parent);
    }

    public int getFontSize() {
        return _textComponent.getFont().getSize();
    }

    void setItalic() {
        log.debug("When style item selected italic state is {}", italic.isSelected());
        if (italic.isSelected()) {
            setFontStyle(Font.ITALIC, 0);
        } else {
            setFontStyle(0, Font.ITALIC);
        }
    }

    void setBold() {
        log.debug("When style item selected bold state is {}", bold.isSelected());
        if (bold.isSelected()) {
            setFontStyle(Font.BOLD, 0);
        } else {
            setFontStyle(0, Font.BOLD);
        }
    }

    protected JMenu makeFontStyleMenu() {
        JMenu styleMenu = new JMenu(Bundle.getMessage("FontStyle"));
        styleMenu.add(italic = newStyleMenuItem(new AbstractAction(Bundle.getMessage("Italic")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled()) { // Avoid action lookup unless needed
                    log.debug("When style item selected {} italic state is {}", getValue(NAME), italic.isSelected());
                }
                if (italic.isSelected()) {
                    setFontStyle(Font.ITALIC, 0);
                } else {
                    setFontStyle(0, Font.ITALIC);
                }
            }
        }, Font.ITALIC));

        styleMenu.add(bold = newStyleMenuItem(new AbstractAction(Bundle.getMessage("Bold")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled()) { // Avoid action lookup unless needed
                    log.debug("When style item selected {} bold state is {}",
                            getValue(NAME), bold.isSelected());
                }
                if (bold.isSelected()) {
                    setFontStyle(Font.BOLD, 0);
                } else {
                    setFontStyle(0, Font.BOLD);
                }
            }
        }, Font.BOLD));
        return styleMenu;
    }

    public void setFontStyle(int style) {
        _textComponent.setFont(_textComponent.getFont().deriveFont(style));
        _parent.updateSize();
    }

    public void setFontStyle(int addStyle, int dropStyle) {
        int styleValue = (getFontStyle() & ~dropStyle) | addStyle;
        log.debug("setFontStyle: addStyle={}, dropStyle={}, net styleValue is {}", addStyle, dropStyle, styleValue);
        if (bold != null) {
            bold.setSelected((styleValue & Font.BOLD) != 0);
        }
        if (italic != null) {
            italic.setSelected((styleValue & Font.ITALIC) != 0);
        }
        _textComponent.setFont(_textComponent.getFont().deriveFont(styleValue));

        //setSize(getPreferredSize().width, getPreferredSize().height);
        _parent.updateSize();
    }

    public int getFontStyle() {
        return _textComponent.getFont().getStyle();
    }

    protected JMenuItem newStyleMenuItem(AbstractAction a, int mask) {
        // next two lines needed because JCheckBoxMenuItem(AbstractAction) not in 1.1.8
        JCheckBoxMenuItem c = new JCheckBoxMenuItem((String) a.getValue(AbstractAction.NAME));
        c.addActionListener(a);
        if (log.isDebugEnabled()) { // Avoid action lookup unless needed
            log.debug("When creating style item {} mask was {} state was {}",
                     a.getValue(AbstractAction.NAME), mask, getFontStyle());
        }
        if ((mask & getFontStyle()) == mask) {
            c.setSelected(true);
        }
        return c;
    }

    public void copyItem(JPopupMenu popup) {
        JMenuItem edit = new JMenuItem("Copy");
        edit.addActionListener((ActionEvent e) -> {
            _parent.getEditor().copyItem(_parent);
        });
        popup.add(edit);
    }

    /*
     * ************* Justification ***********************
     */
    public void setTextJustificationMenu(JPopupMenu popup) {
        JMenu justMenu = new JMenu(Bundle.getMessage("Justification"));
        addJustificationMenuEntry(justMenu, LEFT);
        addJustificationMenuEntry(justMenu, RIGHT);
        addJustificationMenuEntry(justMenu, CENTRE);
        popup.add(justMenu);
    }

    static public final int LEFT = 0x00;
    static public final int RIGHT = 0x02;
    static public final int CENTRE = 0x04;

    private int justification = CENTRE; //Default is always Centre

    public void setJustification(int just) {
        log.debug("setJustification: justification={}", just);
        justification = just;
        setHorizontalAlignment(justification);
        _parent.updateSize();
    }

    public void setJustification(String just) {
        log.debug("setJustification: justification ={}", just);
        switch (just) {
            case "right":
                justification = RIGHT;
                break;
            case "center":
            case "centre":
                // allow US or UK spellings
                justification = CENTRE;
                break;
            default:
                justification = LEFT;
                break;
        }
        setHorizontalAlignment(justification);
        _parent.updateSize();
    }

    public int getJustification() {
        log.debug("getJustification: justification ={}", justification);
        return justification;
    }

    void addJustificationMenuEntry(JMenu menu, final int just) {
        ButtonGroup justButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        switch (just) {
            case RIGHT:
                r = new JRadioButtonMenuItem(Bundle.getMessage("right"));
                break;
            case CENTRE:
                r = new JRadioButtonMenuItem(Bundle.getMessage("center"));
                break;
            case LEFT:
            default:
                r = new JRadioButtonMenuItem(Bundle.getMessage("left"));
        }
        r.addActionListener((ActionEvent e) -> {
            setJustification(just);
        } //final int justification = just;
        );
        justButtonGroup.add(r);
        if (justification == just) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
    }

    public void setHorizontalAlignment(int alignment) {
        if (_textType == LABEL) {
            switch (alignment) {
                case LEFT:
                    ((JLabel) _textComponent).setHorizontalAlignment(JLabel.LEFT);
                    break;
                case RIGHT:
                    ((JLabel) _textComponent).setHorizontalAlignment(JLabel.RIGHT);
                    break;
                case CENTRE:
                    ((JLabel) _textComponent).setHorizontalAlignment(JLabel.CENTER);
                    break;
                default:
                    ((JLabel) _textComponent).setHorizontalAlignment(JLabel.CENTER);
            }
        } else if (_textType == TEXTFIELD) {
            switch (alignment) {
                case LEFT:
                    ((JTextField) _textComponent).setHorizontalAlignment(JTextField.LEFT);
                    break;
                case RIGHT:
                    ((JTextField) _textComponent).setHorizontalAlignment(JTextField.RIGHT);
                    break;
                case CENTRE:
                default:
                    ((JTextField) _textComponent).setHorizontalAlignment(JTextField.CENTER);
            }
        }
    }

    public String getText() {
        if (_textType == LABEL) {
            return ((JLabel) _textComponent).getText();
        } else if (_textType == TEXTFIELD) {
            return ((JTextField) _textComponent).getText();
        }
        return null;
    }

    public final static int HORIZONTAL = 0x00;
    public final static int VERTICAL_UP = 0x01;
    public final static int VERTICAL_DOWN = 0x02;

    private int orientation = HORIZONTAL;

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int ori) {
        orientation = ori;
        _parent.updateSize();
    }

    public void setOrientation(String ori) {
        switch (ori) {
            case "vertical_up":
                setOrientation(VERTICAL_UP);
                break;
            case "vertical_down":
                setOrientation(VERTICAL_DOWN);
                break;
            default:
                setOrientation(HORIZONTAL);
                break;
        }
    }

    public void setTextOrientationMenu(JPopupMenu popup) {
        JMenu oriMenu = new JMenu(Bundle.getMessage("Orientation"));
        addOrientationMenuEntry(oriMenu, HORIZONTAL);
        addOrientationMenuEntry(oriMenu, VERTICAL_UP);
        addOrientationMenuEntry(oriMenu, VERTICAL_DOWN);
        popup.add(oriMenu);
    }

    void addOrientationMenuEntry(JMenu menu, final int ori) {
        ButtonGroup justButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        switch (ori) {
            default:
            case HORIZONTAL:
                r = new JRadioButtonMenuItem("Horizontal");
                break;
            case VERTICAL_UP:
                r = new JRadioButtonMenuItem("Vertical Up");
                break;
            case VERTICAL_DOWN:
                r = new JRadioButtonMenuItem("Vertical Down");
                break;
        }
        r.addActionListener((ActionEvent e) -> {
            setOrientation(ori);
        });
        justButtonGroup.add(r);
        if (orientation == ori) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
    }

    ArrayList<JMenuItem> editAdditionalMenu = new ArrayList<>(0);
    ArrayList<JMenuItem> viewAdditionalMenu = new ArrayList<>(0);

    /**
     * Add a menu item to be displayed when the popup menu is called for when in
     * edit mode.
     *
     * @param menu the item to add
     */
    public void addEditPopUpMenu(JMenuItem menu) {
        if (!editAdditionalMenu.contains(menu)) {
            editAdditionalMenu.add(menu);
        }
    }

    /**
     * Add a menu item to be displayed when the popup menu is called for when in
     * view mode.
     *
     * @param menu menu item or submenu to add
     */
    public void addViewPopUpMenu(JMenuItem menu) {
        if (!viewAdditionalMenu.contains(menu)) {
            viewAdditionalMenu.add(menu);
        }
    }

    /**
     * Add the menu items to the edit popup menu
     *
     * @param popup the menu to add items to
     */
    public void setAdditionalEditPopUpMenu(JPopupMenu popup) {
        if (editAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        editAdditionalMenu.forEach((mi) -> {
            popup.add(mi);
        });
    }

    /**
     * Add the menu items to the view popup menu.
     *
     * @param popup the menu to add items to
     */
    public void setAdditionalViewPopUpMenu(JPopupMenu popup) {
        if (viewAdditionalMenu.isEmpty()) {
            return;
        }
        viewAdditionalMenu.forEach((mi) -> {
            popup.add(mi);
        });
    }

    private final static Logger log = LoggerFactory.getLogger(PositionablePopupUtil.class);
}
