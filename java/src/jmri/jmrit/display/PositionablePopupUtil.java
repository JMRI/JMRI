// PositionablePopupUtil.java

package jmri.jmrit.display;

import java.util.ArrayList;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;

import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * <p>This class handles text attributes for Positionables. 
 * Font size, style and color.  
 * Margin size and color, 
 * Border size and color, 
 * Fixed sizes. 
 * Justification.
 * </p>
 * @author  Pete Cressman copyright (C) 2010
 * @version $I $
 */
public class PositionablePopupUtil {

    private boolean debug = false;
    protected JComponent _textComponent;    // closest ancestor for JLabel and JTextField
    protected int _textType;                // JComponent does not have text, used for casting
    protected Positionable _parent;
    protected PositionablePropertiesUtil _propertiesUtil;

    private Color defaultForeground;
    private Color defaultBackground;
    private Color defaultBorderColor;

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
        debug = log.isDebugEnabled();
        defaultForeground = _textComponent.getForeground();
        defaultBackground = _textComponent.getBackground();
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
        util.setFont(util.getFont().deriveFont(getFontStyle()));
        util.setFontSize(getFontSize());
        util.setOrientation(getOrientation());
        util.setBackgroundColor(getBackground());
        util.setForeground(getForeground());
        return util;
    }

    public String toString() {
        return _parent.getNameString()+": fixedWidth= "+fixedWidth+", fixedHeight= "+fixedHeight+
                 ", margin= "+margin+", borderSize= "+borderSize; 
    }
    
    /****************************************************************************************/

    static final public int FONT_COLOR =             0x00;
    static final public int BACKGROUND_COLOR =       0x01;
    static final public int BORDER_COLOR =           0x02;
    static final public int MIN_SIZE = 5;

    private int fixedWidth=0;
    private int fixedHeight=0;
    private int margin=0;
    private int borderSize=0;
    private Color borderColor=null;
    private Border borderMargin = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    private Border outlineBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);

    JMenuItem italic = null;
    JMenuItem bold = null;

    public void propertyUtil(JPopupMenu popup){
        JMenuItem edit = new JMenuItem("Properties");
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _propertiesUtil.display(); }
        });
        popup.add(edit);
    }

    public void setFixedTextMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditFixed"));
        if (getFixedWidth()==0)
            edit.add("Width= Auto");
        else
            edit.add("Width= " + _parent.maxWidth());

        if (getFixedHeight()==0)
            edit.add("Height= Auto");
        else
            edit.add("Height= " + _parent.maxHeight());

        edit.add(CoordinateEdit.getFixedSizeEditAction(_parent));
        popup.add(edit);
    }

    public void setTextMarginMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditMargin"));
        if((fixedHeight==0)||(fixedWidth==0)) {
            edit.add("Margin= " + getMargin());
            edit.add(CoordinateEdit.getMarginEditAction(_parent));
        }
        popup.add(edit);
    }
    
    public void setBackgroundMenu(JPopupMenu popup){
        JMenu edit = new JMenu(Bundle.getMessage("FontBackgroundColor"));
        makeColorMenu(edit, BACKGROUND_COLOR);
        popup.add(edit);
    
    }

    public void setTextBorderMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditBorder"));
        edit.add("Border Size= " + borderSize);
        edit.add(CoordinateEdit.getBorderEditAction(_parent));
        JMenu colorMenu = new JMenu(Bundle.getMessage("BorderColorMenu"));
        makeColorMenu(colorMenu, BORDER_COLOR);
        edit.add(colorMenu);
        popup.add(edit);
    }

    public void setTextFontMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditFont"));
        edit.add(makeFontSizeMenu());
        edit.add(makeFontStyleMenu());
        JMenu colorMenu = new JMenu(Bundle.getMessage("FontColor"));
        makeColorMenu(colorMenu, PositionablePopupUtil.FONT_COLOR);
        edit.add(colorMenu);
        popup.add(edit);
    }

    public int getMargin() {
        return margin;
    }
    public void setMargin(int m) {
        margin = m;
        if (_parent.isOpaque()){
            borderMargin = new LineBorder(getBackground(),m);
            //_parent.setBorder(new LineBorder(setBackground(), m));
            
        } else{
            borderMargin = BorderFactory.createEmptyBorder(m, m, m, m);
            //_parent.setBorder(BorderFactory.createEmptyBorder(m, m, m, m));
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
        if (log.isDebugEnabled()) log.debug("setFixedWidth()="+getFixedWidth());
        _parent.updateSize();
    }
    public int getFixedHeight() {
        return fixedHeight;
    }
    public void setFixedHeight(int h) {
        fixedHeight = h;
        if (log.isDebugEnabled()) log.debug("setFixedHeight()="+getFixedHeight());
        _parent.updateSize();
    }
    public void setFixedSize(int w, int h) {
        fixedWidth = w;
        fixedHeight = h;
        if (log.isDebugEnabled()) log.debug("setFixedSize()="+"("+getFixedWidth()+","+getFixedHeight()+")");
        _parent.updateSize();
    }

    public void setBorderSize(int border){
        borderSize = border;
        
        if(borderColor!=null){
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
            if(borderColor!=null){
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

    public void setBorderColor(Color border){
        borderColor = border;
        if(borderColor!=null && _showBorder){
            outlineBorder = new LineBorder(borderColor, borderSize);
            _parent.setBorder(new CompoundBorder(outlineBorder, borderMargin));
        }
    }

    public Color getBorderColor(){
        if (borderColor==null) {
            borderColor = _parent.getBackground();
        }
        return borderColor;
    }

    public void setForeground(Color c) {
        _textComponent.setForeground(c);
        _parent.updateSize();
    }

    public Color getForeground() {
        return _textComponent.getForeground();
    }

    public void setBackgroundColor(Color color){
        if(color==null){
            _textComponent.setOpaque(false);
            _parent.setOpaque(false);
        }
        else {
            _textComponent.setOpaque(true);
            _textComponent.setBackground(color);
            _parent.setOpaque(true);
            _parent.setBackground(color);
        }
        setMargin(margin);  //This rebuilds margin and sets it colour.
        _parent.updateSize();
    }

    public Color getBackground() {
    	return _textComponent.getBackground();
    }

    protected JMenu makeFontSizeMenu() {
        JMenu sizeMenu = new JMenu("Font Size");
        ButtonGroup buttonGrp = new ButtonGroup();
        addFontMenuEntry(sizeMenu, buttonGrp, 6);
        addFontMenuEntry(sizeMenu, buttonGrp, 8);
        addFontMenuEntry(sizeMenu, buttonGrp, 10);
        addFontMenuEntry(sizeMenu, buttonGrp, 11);
        addFontMenuEntry(sizeMenu, buttonGrp, 12);
        addFontMenuEntry(sizeMenu, buttonGrp, 14);
        addFontMenuEntry(sizeMenu, buttonGrp, 16);
        addFontMenuEntry(sizeMenu, buttonGrp, 20);
        addFontMenuEntry(sizeMenu, buttonGrp, 24);
        addFontMenuEntry(sizeMenu, buttonGrp, 28);
        addFontMenuEntry(sizeMenu, buttonGrp, 32);
        addFontMenuEntry(sizeMenu, buttonGrp, 36);
        return sizeMenu;
    }
    
    void addFontMenuEntry(JMenu menu, ButtonGroup fontButtonGroup, final int size) {
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(""+size);
        r.addActionListener(new ActionListener() {
            final float desiredSize = size+0.f;
            public void actionPerformed(ActionEvent e) {
                setFontSize(desiredSize);
            }
        });
        fontButtonGroup.add(r);
        if (_textComponent.getFont().getSize() == size) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }

    public void setFont(Font font) {
        _textComponent.setFont(font);
        _parent.updateSize();
    }

    public Font getFont() {
        return _textComponent.getFont();
    }

    public void setFontSize(float newSize) {
        _textComponent.setFont(jmri.util.FontUtil.deriveFont(_textComponent.getFont(), newSize));
        //setSize(getPreferredSize().width, getPreferredSize().height);
        _parent.updateSize();
    }

    public int getFontSize() {
        return _textComponent.getFont().getSize();
    }

    void setItalic() {
        if (debug)
            log.debug("When style item selected italic state is "+italic.isSelected());
        if (italic.isSelected()) setFontStyle(Font.ITALIC, 0);
        else setFontStyle(0, Font.ITALIC);
    }
    void setBold() {
        if (debug)
            log.debug("When style item selected bold state is "+bold.isSelected());
        if (bold.isSelected()) setFontStyle(Font.BOLD, 0);
        else setFontStyle(0, Font.BOLD);
    }

    protected JMenu makeFontStyleMenu() {
        JMenu styleMenu = new JMenu(Bundle.getMessage("FontStyle"));
        styleMenu.add(italic = newStyleMenuItem(new AbstractAction(Bundle.getMessage("Italic")) {
            public void actionPerformed(ActionEvent e) {
                if (debug)
                    log.debug("When style item selected "+((String)getValue(NAME))
                                +" italic state is "+italic.isSelected());
                if (italic.isSelected()) setFontStyle(Font.ITALIC, 0);
                else setFontStyle(0, Font.ITALIC);
            }
          }, Font.ITALIC));

        styleMenu.add(bold = newStyleMenuItem(new AbstractAction(Bundle.getMessage("Bold")) {
            public void actionPerformed(ActionEvent e) {
                if (debug)
                    log.debug("When style item selected "+((String)getValue(NAME))
                                +" bold state is "+bold.isSelected());
                if (bold.isSelected()) setFontStyle(Font.BOLD, 0);
                else setFontStyle(0, Font.BOLD);
            }
          }, Font.BOLD));
         return styleMenu;
    }

    public void setFontStyle(int style) {
        _textComponent.setFont(jmri.util.FontUtil.deriveFont(_textComponent.getFont(),style));
		_parent.updateSize();
    }

    public void setFontStyle(int addStyle, int dropStyle) {
        int styleValue = (_textComponent.getFont().getStyle() & ~dropStyle) | addStyle;
        if (debug)
            log.debug("setFontStyle: addStyle="+addStyle+", dropStyle= "+dropStyle
                        +", net styleValue is "+styleValue);
        if (bold != null) bold.setSelected( (styleValue & Font.BOLD) != 0);
        if (italic != null) italic.setSelected( (styleValue & Font.ITALIC) != 0);
        _textComponent.setFont(jmri.util.FontUtil.deriveFont(_textComponent.getFont(),styleValue));

        //setSize(getPreferredSize().width, getPreferredSize().height);
		_parent.updateSize();
    }

    public int getFontStyle() {
        return _textComponent.getFont().getStyle();
    }
    
    protected JMenuItem newStyleMenuItem(AbstractAction a, int mask) {
        // next two lines needed because JCheckBoxMenuItem(AbstractAction) not in 1.1.8
        JCheckBoxMenuItem c = new JCheckBoxMenuItem((String)a.getValue(AbstractAction.NAME));
        c.addActionListener(a);
        if (debug) log.debug("When creating style item "+((String)a.getValue(AbstractAction.NAME))
                                            +" mask was "+mask+" state was "+_textComponent.getFont().getStyle());
        if ( (mask & _textComponent.getFont().getStyle()) == mask ) c.setSelected(true);
        return c;
    }

    protected void makeColorMenu(JMenu colorMenu, int type) {
        ButtonGroup buttonGrp = new ButtonGroup();
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Black"), Color.black, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("DarkGray"),Color.darkGray, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Gray"),Color.gray, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("LightGray"),Color.lightGray, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("White"),Color.white, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Red"),Color.red, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Orange"),Color.orange, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Yellow"),Color.yellow, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Green"),Color.green, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Blue"),Color.blue, type);
        addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Magenta"),Color.magenta, type);
        if (type == BACKGROUND_COLOR){
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Clear"), null, type);
        }
    }

    protected void addColorMenuEntry(JMenu menu, ButtonGroup colorButtonGroup,
                           final String name, Color color, final int colorType) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            Color desiredColor;
            public void actionPerformed(ActionEvent e) {
                switch (colorType){
                    case FONT_COLOR : 
                        _textComponent.setForeground(desiredColor); 
                        break;
                    case BACKGROUND_COLOR : 
                        if(desiredColor==null){
                            _textComponent.setOpaque(false);
                            _parent.setOpaque(false);
                            //We need to force a redisplay when going to clear as the area
                            //doesn't always go transparent on the first click.
                            Point p = _parent.getLocation();
                            int w = _parent.getWidth();
                            int h = _parent.getHeight();
                            Container parent = _parent.getParent();
                            // force redisplay
                            setMargin(margin);  //This rebuilds margin and clears it colour.
                            parent.validate();
                            parent.repaint(p.x,p.y,w,h);
                        }
                        else
                            setBackgroundColor(desiredColor);
                        break;
                    case BORDER_COLOR : 
                        setBorderColor(desiredColor); 
                        break;
                }
            }
            ActionListener init (Color c) {
                desiredColor = c;
                return this;
            }
        }.init(color);
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);

        if (debug) log.debug("setColorButton: colorType="+colorType);
        switch (colorType) {
            case FONT_COLOR:
                if (color==null) { color = defaultForeground; }
                setColorButton(_textComponent.getForeground(), color, r);
                break;
            case BACKGROUND_COLOR:
                if (color==null) { color = defaultBackground; }
                setColorButton(_textComponent.getBackground(), color, r);
                break;
            case BORDER_COLOR:
                if (color==null) { color = defaultBorderColor; }
                setColorButton(getBorderColor(), color, r);
        }
        colorButtonGroup.add(r);
        menu.add(r);
    }
                
    protected void setColorButton(Color color, Color buttonColor, JRadioButtonMenuItem r) {
        if (debug)
            log.debug("setColorButton: color="+color+" (RGB= "+(color==null?"":color.getRGB())+
                      ") buttonColor= "+buttonColor+" (RGB= "+(buttonColor==null?"":buttonColor.getRGB())+")");
        if (buttonColor!=null) {
            if (color!=null && buttonColor.getRGB() == color.getRGB()) {
                 r.setSelected(true);
            } else r.setSelected(false);
        } else {
            if (color==null)  r.setSelected(true);
            else  r.setSelected(false);
        }
    }

    public void copyItem(JPopupMenu popup){
        JMenuItem edit = new JMenuItem("Copy");
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _parent.getEditor().copyItem(_parent); }
        });
        popup.add(edit);
    }
    /*************** Justification ************************/

    public void setTextJustificationMenu(JPopupMenu popup) {
        JMenu justMenu = new JMenu("Justification");
        addJustificationMenuEntry(justMenu, LEFT);
        addJustificationMenuEntry(justMenu, RIGHT);
        addJustificationMenuEntry(justMenu, CENTRE);
        popup.add(justMenu);
    }

    static public final int LEFT   = 0x00;
    static public final int RIGHT  = 0x02;
    static public final int CENTRE = 0x04;
    
    private int justification=CENTRE; //Default is always Centre
    
    public void setJustification(int just){
        log.debug("setJustification: justification="+just);
        justification=just;
        setHorizontalAlignment(justification);
        _parent.updateSize();
    }
        
    public void setJustification(String just){
        log.debug("setJustification: justification="+just);
        if (just.equals("right"))
            justification=RIGHT;
        else if (just.equals("centre"))
            justification=CENTRE;
        else
            justification=LEFT;
        setHorizontalAlignment(justification);
        _parent.updateSize();
    }
    
    public int getJustification(){
        log.debug("getJustification: justification="+justification);
        return justification;
    }
    
    void addJustificationMenuEntry(JMenu menu, final int just) {
        ButtonGroup justButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        switch(just){
            case LEFT :     r = new JRadioButtonMenuItem("LEFT");
                            break;
            case RIGHT:     r = new JRadioButtonMenuItem("RIGHT");
                            break;
            case CENTRE:    r = new JRadioButtonMenuItem("CENTRE");
                            break;
            default :       r = new JRadioButtonMenuItem("LEFT");
        }
        r.addActionListener(new ActionListener() {
            //final int justification = just;
            public void actionPerformed(ActionEvent e) { setJustification(just); }
        });
        justButtonGroup.add(r);
        if (justification == just) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }
    
    public void setHorizontalAlignment(int alignment) {
        if (_textType == LABEL) {
            switch (alignment){
                case LEFT :     ((JLabel)_textComponent).setHorizontalAlignment(JLabel.LEFT);
                                break;
                case RIGHT :    ((JLabel)_textComponent).setHorizontalAlignment(JLabel.RIGHT);
                                break;
                case CENTRE :   ((JLabel)_textComponent).setHorizontalAlignment(JLabel.CENTER);
                                break;
                default     :   ((JLabel)_textComponent).setHorizontalAlignment(JLabel.CENTER);
            }
        } else if (_textType == TEXTFIELD) {
            switch (alignment){
                case LEFT :     ((JTextField)_textComponent).setHorizontalAlignment(JTextField.LEFT);
                                break;
                case RIGHT :    ((JTextField)_textComponent).setHorizontalAlignment(JTextField.RIGHT);
                                break;
                case CENTRE :   ((JTextField)_textComponent).setHorizontalAlignment(JTextField.CENTER);
                                break;
                default     :   ((JTextField)_textComponent).setHorizontalAlignment(JTextField.CENTER);
            }
        }
    }

    public String getText() {
        if (_textType == LABEL) {
            return ((JLabel)_textComponent).getText();
        } else if (_textType == TEXTFIELD) {
            return ((JTextField)_textComponent).getText();
        }
        return null;
    }

    public final static int HORIZONTAL = 0x00;
    public final static int VERTICAL_UP =0x01;
    public final static int VERTICAL_DOWN =0x02;
    
    private int orientation = HORIZONTAL;
    
    public int getOrientation(){
        return orientation;
    }
    
    public void setOrientation(int ori){
        orientation=ori;
        _parent.updateSize();
    }
    
    public void setOrientation(String ori){
        if(ori.equals("vertical_up"))
            setOrientation(VERTICAL_UP);
        else if(ori.equals("vertical_down"))
            setOrientation(VERTICAL_DOWN);
        else
            setOrientation(HORIZONTAL);
    }
    
    public void setTextOrientationMenu(JPopupMenu popup) {
        JMenu oriMenu = new JMenu("Orientation");
        addOrientationMenuEntry(oriMenu, HORIZONTAL);
        addOrientationMenuEntry(oriMenu, VERTICAL_UP);
        addOrientationMenuEntry(oriMenu, VERTICAL_DOWN);
        popup.add(oriMenu);
    }
    
    void addOrientationMenuEntry(JMenu menu, final int ori) {
        ButtonGroup justButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        switch(ori){
            case HORIZONTAL :     r = new JRadioButtonMenuItem("Horizontal");
                            break;
            case VERTICAL_UP:     r = new JRadioButtonMenuItem("Vertical Up");
                            break;
            case VERTICAL_DOWN:    r = new JRadioButtonMenuItem("Vertical Down");
                            break;
            default :       r = new JRadioButtonMenuItem("Horizontal");
        }
        r.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { setOrientation(ori); }
        });
        justButtonGroup.add(r);
        if (orientation == ori) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }
    
    ArrayList<JMenuItem> editAdditionalMenu = new ArrayList<JMenuItem>(0);
    ArrayList<JMenuItem> viewAdditionalMenu = new ArrayList<JMenuItem>(0);
    
    /**
    *  Add a menu item to be displayed when the popup menu is called for
    *  when in edit mode.
    */
        public void addEditPopUpMenu(JMenuItem menu){
        if(!editAdditionalMenu.contains(menu)){
            editAdditionalMenu.add(menu);
        }
    }

    /**
    *  Add a menu item to be displayed when the popup menu is called for
    *  when in view mode.
    */    
    public void addViewPopUpMenu(JMenuItem menu){
        if(!viewAdditionalMenu.contains(menu)){
            viewAdditionalMenu.add(menu);
        }
    }
    
    /**
    *  Add the menu items to the edit popup menu
    */
    public void setAdditionalEditPopUpMenu(JPopupMenu popup){
        if(editAdditionalMenu.isEmpty())
            return;
        popup.addSeparator();
        for(JMenuItem mi:editAdditionalMenu){
            popup.add(mi);
        }
    }
    
    /**
    *  Add the menu items to the view popup menu
    */
    public void setAdditionalViewPopUpMenu(JPopupMenu popup){
        if(viewAdditionalMenu.isEmpty())
            return;
        for(JMenuItem mi:viewAdditionalMenu){
            popup.add(mi);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionablePopupUtil.class.getName());
}
