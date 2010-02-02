package jmri.jmrit.display;

import jmri.jmrit.catalog.NamedIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.ButtonGroup;
//import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
//import java.awt.Dimension;
import javax.swing.border.LineBorder;

/**
 * PositionableLabel is a JLabel that can be dragged around the
 * inside of the enclosing Container using a right-drag.
 * <P>
 * The positionable parameter is a global, set from outside.
 * The 'fixed' parameter is local, set from the popup here.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @version $Revision: 1.77 $
 */

public class PositionableLabel extends JLabel implements Positionable {

    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    static final protected int FONT_COLOR =             0x00;
    static final protected int BACKGROUND_COLOR =       0x01;
    static final protected int BORDER_COLOR =           0x02;
    static final protected int UNKOWN_FONT_COLOR =      0x03;
    static final protected int UNKOWN_BACKGROUND_COLOR =    0x04;
    static final protected int ACTIVE_FONT_COLOR =          0x05;
    static final protected int ACTIVE_BACKGROUND_COLOR =    0x06;
    static final protected int INACTIVE_FONT_COLOR =        0x07;
    static final protected int INACTIVE_BACKGROUND_COLOR =  0x09;
    static final protected int INCONSISTENT_FONT_COLOR =        0x0A;
    static final protected int INCONSISTENT_BACKGROUND_COLOR =  0x0B;

    static final protected int MIN_SIZE = 5;

    protected Editor _editor;

    protected boolean debug = false; 
    protected boolean _icon = false;
    protected boolean _text = false;
    protected boolean _control = false;
    protected NamedIcon _namedIcon;

    private String _tooltip;
    private boolean _showTooltip =true;
    private boolean _editable = true;
    private boolean _positionable = true;
    private boolean _viewCoordinates = true;
    private boolean _controlling = true;
    private boolean _hidden = false;
    private int _displayLevel;
    
    public PositionableLabel(String s, Editor editor) {
        super(s);
        if (log.isDebugEnabled()) log.debug("PositionableLabel ctor (text) "+s);
        _editor = editor;
        _text = true;
        debug = log.isDebugEnabled();
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
    }
    public PositionableLabel(NamedIcon s, Editor editor) {
        super(s);
        if (log.isDebugEnabled()) log.debug("PositionableLabel ctor (icon) "+s.getName());
        _editor = editor;
        _icon = true;
        _namedIcon = s;
        debug = log.isDebugEnabled();
        updateSize();
    }

    public final boolean isIcon() { return _icon; }
    public final boolean isText() { return _text; }
    public final boolean isControl() { return _control; }

    public Editor getEditor(){
        return _editor;
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
    public void setDisplayLevel(int l) {
    	int oldDisplayLevel = _displayLevel;
    	_displayLevel = l;
    	if (oldDisplayLevel!=l){
    		if (log.isDebugEnabled()) log.debug("Changing label display level from "+oldDisplayLevel+" to "+_displayLevel);
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

    public String getNameString() {
        if (_icon) return "Icon";
        else if (_text) return "Text";
        else return "None!";
    }

    // overide where used - e.g. momentary
    public void doMousePressed(MouseEvent event) {
    }
    public void doMouseReleased(MouseEvent event) {
    }

    /**************** end Positionable methods **********************/
    
    /**
     * This deals with the formating of the JLabel and text boxes.
     *
     */
    private int borderSize=0;

    public void setBorderSize(int border){
        borderSize = border;
        if(borderColor!=null){
            setBorder(new LineBorder(borderColor, borderSize));
            setSize(maxWidth(), maxHeight());
            this.setHorizontalAlignment(JLabel.CENTER);
        }
    }

    public int getBorderSize(){
        return borderSize;
    }

    private Color borderColor=null;

    public void setBorderColor(Color border){
        borderColor = border;
        if(borderSize!=0){
            setBorder(new LineBorder(borderColor, borderSize));
            setSize(maxWidth(), maxHeight());
            this.setHorizontalAlignment(JLabel.CENTER);
        }
    }

    public Color getBorderColor(){
        return borderColor;
    }

    private int margin=0;

    public void setMargin(int mar){
        margin = mar;
        this.setHorizontalAlignment(JLabel.CENTER);
        updateSize();
    }

    public int getMargin(){
        return margin;
    }
    
    private int fixedWidth=0;
    private int fixedHeight=0;

    public int getFixedWidth(){
        return fixedWidth;
    }

    public int getFixedHeight(){
        return fixedHeight;
    }

    public void setFixedSize(int width, int height){
        //System.out.println("fixed width " + width + ", " + height);
        fixedWidth=width;
        fixedHeight=height;
        updateSize();
    }

    public void setBackgroundColor(Color color){
        if (_text){
            // We consider that if the value passed is null, then the background is clear.
            if(color==null){
                setOpaque(false);
            }
            else {
                setOpaque(true);
                setBackground(color);
            }
        }
    }

    /**
     * Update the AWT and Swing size information due to change in internal
     * state, e.g. if one or more of the icons that might be displayed
     * is changed
     */
    protected void updateSize(){
        setSize(maxWidth(), maxHeight());
        if (_icon && _text) {
            //we have a combined icon/text therefore the icon is central to the text.
            setIconTextGap (-(_namedIcon.getIconWidth()+maxWidth())/2);
        }
    }
    
    public int maxWidth(){
        int max = 0;
        if (fixedWidth!=0) {
            max = fixedWidth;
            if (margin!=0) {
                max -= margin*2;
            }
            if (max < MIN_SIZE) {  // don't let item disappear
                fixedWidth += MIN_SIZE-max;
                max = MIN_SIZE;
            }
        } else {
            if(_icon) {
                max = _namedIcon.getIconWidth();
            }
            if(_text) {
                max = Math.max(getMaximumSize().width, max);
            }
            if (margin!=0) {
                max += margin*2;
            }
        }
        if (debug) log.debug("maxWidth= "+max+" preferred width= "+getPreferredSize().width);
        return max;
    }

    public int maxHeight(){
        int max = 0;
        if (fixedHeight!=0) {
            max = fixedHeight;
            if (margin!=0) {
                max -= margin*2;
            }
            if (max < MIN_SIZE) {   // don't let item disappear
                fixedHeight += MIN_SIZE-max;
                max = MIN_SIZE;
            }
        } else {
            if(_icon) {
                max = _namedIcon.getIconHeight();
            }
            if(_text) {
                max = Math.max(getMaximumSize().height, max);
            }
            if (margin!=0) {
                max += margin*2;
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
    public void showPopUp(JPopupMenu popup) {}

    /**
    * Rotate othogonally
    */
    public void setRotateOrthogonalMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(rb.getString("Rotate")) {
            public void actionPerformed(ActionEvent e) {
                rotateOrthogonal();
            }
        });
    }
    protected void rotateOrthogonal() {
        _namedIcon.setRotation(_namedIcon.getRotation()+1, this);
        setIcon(_namedIcon);
        updateSize();
        repaint();
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

    protected boolean showIconEditorFrame(Container pos) {
        if (_iconEditorFrame != null) {
            _iconEditorFrame.setLocationRelativeTo(pos);
            _iconEditorFrame.toFront();
            _iconEditorFrame.setVisible(true);
            return true;
        }
        return false;
    }

    protected void edit() {
        if (_iconEditorFrame != null) {
            _iconEditorFrame.setLocationRelativeTo(null);
            _iconEditorFrame.toFront();
            return;
        }
        _iconEditor = new IconAdder();
        NamedIcon icon = new NamedIcon(_namedIcon);
        _iconEditor.setIcon(0, "plainIcon", icon);
        _iconEditorFrame = makeAddIconFrame("EditIcon", "addIconToPanel", 
                                     "pressAdd", _iconEditor, this);
        _iconEditor.makeIconPanel();

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editIcon();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _iconEditor.addCatalog();
                    _iconEditorFrame.pack();
                }
        };
        _iconEditor.complete(addIconAction, changeIconAction, false, true);

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

    public void setRotateMenu(JPopupMenu popup) {
        popup.add(CoordinateEdit.getRotateEditAction(this));
    }

    public void setScaleMenu(JPopupMenu popup) {
        popup.add(CoordinateEdit.getScaleEditAction(this));
    }

    public void setFixedTextMenu(JPopupMenu popup) {
        if (fixedWidth==0)
            popup.add("Width= Auto");
        else
            popup.add("Width= " + this.maxWidth());

        if (fixedHeight==0)
            popup.add("Height= Auto");
        else
            popup.add("Height= " + this.maxHeight());

        popup.add(CoordinateEdit.getFixedSizeEditAction(this));
    }

    public void setTextMarginMenu(JPopupMenu popup) {
        if((fixedHeight==0)||(fixedWidth==0)) {
            popup.add("Margin= " + this.getMargin());
            popup.add(CoordinateEdit.getMarginEditAction(this));
        }
    }

    public void setTextBorderMenu(JPopupMenu popup) {
        popup.add("Border Size= " + borderSize);
        popup.add(CoordinateEdit.getBorderEditAction(this));
        JMenu colorMenu = new JMenu(rb.getString("BorderColorMenu"));
        makeColorMenu(colorMenu, BORDER_COLOR);
        popup.add(colorMenu);
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
            public void actionPerformed(ActionEvent e) { setFontSize(desiredSize); }
        });
        fontButtonGroup.add(r);
        if (getFont().getSize() == size) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }

    public void setFontSize(float newSize) {
        setFont(jmri.util.FontUtil.deriveFont(getFont(), newSize));
        //setSize(getPreferredSize().width, getPreferredSize().height);
        updateSize();
    }

    void setItalic() {
        if (log.isDebugEnabled())
            log.debug("When style item selected italic state is "+italic.isSelected());
        if (italic.isSelected()) setFontStyle(Font.ITALIC, 0);
        else setFontStyle(0, Font.ITALIC);
    }
    void setBold() {
        if (log.isDebugEnabled())
            log.debug("When style item selected bold state is "+bold.isSelected());
        if (bold.isSelected()) setFontStyle(Font.BOLD, 0);
        else setFontStyle(0, Font.BOLD);
    }

    protected JMenu makeFontStyleMenu() {
        JMenu styleMenu = new JMenu(rb.getString("FontStyle"));
        styleMenu.add(italic = newStyleMenuItem(new AbstractAction(rb.getString("Italic")) {
            public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled())
                    log.debug("When style item selected "+((String)getValue(NAME))
                                +" italic state is "+italic.isSelected());
                if (italic.isSelected()) setFontStyle(Font.ITALIC, 0);
                else setFontStyle(0, Font.ITALIC);
            }
          }, Font.ITALIC));

        styleMenu.add(bold = newStyleMenuItem(new AbstractAction(rb.getString("Bold")) {
            public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled())
                    log.debug("When style item selected "+((String)getValue(NAME))
                                +" bold state is "+bold.isSelected());
                if (bold.isSelected()) setFontStyle(Font.BOLD, 0);
                else setFontStyle(0, Font.BOLD);
            }
          }, Font.BOLD));
         return styleMenu;
    }
    
    protected JMenuItem newStyleMenuItem(AbstractAction a, int mask) {
        // next two lines needed because JCheckBoxMenuItem(AbstractAction) not in 1.1.8
        JCheckBoxMenuItem c = new JCheckBoxMenuItem((String)a.getValue(AbstractAction.NAME));
        c.addActionListener(a);
        if (log.isDebugEnabled()) log.debug("When creating style item "+((String)a.getValue(AbstractAction.NAME))
                                            +" mask was "+mask+" state was "+getFont().getStyle());
        if ( (mask & getFont().getStyle()) == mask ) c.setSelected(true);
        return c;
    }

    public void setBackgroundFontColorMenu(JPopupMenu popup) {
        JMenu colorMenu = new JMenu(rb.getString("FontBackgroundColor"));
        makeColorMenu(colorMenu, BACKGROUND_COLOR);
        popup.add(colorMenu);
    }

    public void makeColorMenu(JMenu colorMenu, int type) {
        ButtonGroup buttonGrp = new ButtonGroup();
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Black"), Color.black, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("DarkGray"),Color.darkGray, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Gray"),Color.gray, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("LightGray"),Color.lightGray, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("White"),Color.white, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Red"),Color.red, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Orange"),Color.orange, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Yellow"),Color.yellow, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Green"),Color.green, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Blue"),Color.blue, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Magenta"),Color.magenta, type);
        addColorMenuEntry(colorMenu, buttonGrp, rb.getString("Clear"), null, type);
    }

    void addColorMenuEntry(JMenu menu, ButtonGroup colorButtonGroup,
                           final String name, final Color color, final int colorType) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            final Color desiredColor = color;
            public void actionPerformed(ActionEvent e) {
                switch (colorType){
                    case FONT_COLOR : 
                        setForeground(desiredColor); 
                        break;
                    case BACKGROUND_COLOR : 
                        if(color==null){
                            setOpaque(false);
                            //We need to force a redisplay when going to clear as the area
                            //doesn't always go transparent on the first click.
                            Point p = getLocation();
                            int w = getWidth();
                            int h = getHeight();
                            Container parent = getParent();
                            // force redisplay
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
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);

        switch (colorType) {
            case FONT_COLOR:
                setColorButton(getForeground(), color, r);
                break;
            case BACKGROUND_COLOR:
                setColorButton(getBackground(), color, r);
                break;
            case BORDER_COLOR:
                setColorButton(getBorderColor(), color, r);
        }
        colorButtonGroup.add(r);
        menu.add(r);
    }
                
    protected void setColorButton(Color buttonColor, Color color, JRadioButtonMenuItem r) {
        if (buttonColor!=null){
            if (color!=null && buttonColor.getRGB() == color.getRGB()) {
                 r.setSelected(true);
            } else r.setSelected(false);
        } else {
            if (color==null)  r.setSelected(true);
            else  r.setSelected(false);
        }
    }

    JCheckBoxMenuItem disableItem = null;
    public void setDisableControlMenu(JPopupMenu popup) {
        disableItem = new JCheckBoxMenuItem(rb.getString("Disable"));
        disableItem.setSelected(getForceControlOff());
        popup.add(disableItem);
        disableItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setForceControlOff(disableItem.isSelected());
            }
        });
    }

    void scale(int s) {
        _namedIcon.scale(s, this);
        setIcon(_namedIcon);
        updateSize();
    }

    void rotate(int deg) {
        _namedIcon.rotate(deg, this);
        setIcon(_namedIcon);
        updateSize();
    }
    
    JCheckBoxMenuItem tristateItem = null;
    void addTristateEntry(JPopupMenu popup) {
    	tristateItem = new JCheckBoxMenuItem(rb.getString("Tristate"));
    	tristateItem.setSelected(getTristate());
        popup.add(tristateItem);
        tristateItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setTristate(tristateItem.isSelected());
            }
        });
    }

    public void setTextFontMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(rb.getString("EditFont"));
        edit.add(makeFontSizeMenu());
        edit.add(makeFontStyleMenu());
        JMenu colorMenu = new JMenu(rb.getString("FontColor"));
        makeColorMenu(colorMenu, FONT_COLOR);
        edit.add(colorMenu);
        popup.add(edit);
    }

    public void setTextEditMenu(JPopupMenu popup) {
        setTextEditMenu(popup, "EditText");
    }
    public void setTextEditMenu(JPopupMenu popup, String menuTitle) {
        popup.add(CoordinateEdit.getTextEditAction(this, menuTitle));
    }

    public void setTextJustificationMenu(JPopupMenu popup) {
        JMenu justMenu = new JMenu("Justification");
        addJustificationMenuEntry(justMenu, LEFT);
        addJustificationMenuEntry(justMenu, RIGHT);
        addJustificationMenuEntry(justMenu, CENTRE);
        popup.add(justMenu);
    }

    private int originalX=0;
    private int originalY=0;
    
    static final int LEFT   = 0x00;
    static final int RIGHT  = 0x02;
    static final int CENTRE = 0x04;
    
    private int justification=LEFT; //Default is always left    
    
    public void setJustification(int just){
        justification=just;
        setJustification();
    }
        
    public void setJustification(String just){
        if (just.equals("right"))
            justification=RIGHT;
        else if (just.equals("centre"))
            justification=CENTRE;
        else
            justification=LEFT;
        setJustification();
    }
    
    private void setJustification(){
        if (getFixedWidth()==0){
            switch (justification){
                case RIGHT :    setOriginalLocation(this.getX()+this.maxWidth(), this.getY());
                                break;
                case CENTRE :   setOriginalLocation(this.getX()+(this.maxWidth()/2), this.getY());
                                break;
            }
            this.setHorizontalAlignment(JLabel.CENTER);
            updateSize();
        }
        else{
            switch (justification){
                case LEFT :     this.setHorizontalAlignment(JLabel.LEFT);
                                break;
                case RIGHT :    this.setHorizontalAlignment(JLabel.RIGHT);
                                break;
                case CENTRE :   this.setHorizontalAlignment(JLabel.CENTER);
                                break;
                default     :   this.setHorizontalAlignment(JLabel.CENTER);
            }
        }
    }
    
    public void setOriginalLocation(int x, int y){
        originalX=x;
        originalY=y;
        updateSize();
    }
    
    public int getJustification(){
        return justification;
    }
    
    public int getOriginalX(){
        return originalX;
    }
    
    public int getOriginalY(){
        return originalY;
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
    
    JCheckBoxMenuItem lock = null;
    JCheckBoxMenuItem showTooltipItem = null;
    JMenuItem italic = null;
    JMenuItem bold = null;

    public void setFontStyle(int addStyle, int dropStyle) {
        int styleValue = (getFont().getStyle() & ~dropStyle) | addStyle;
        if (log.isDebugEnabled())
            log.debug("setFontStyle: addStyle="+addStyle+", dropStyle= "+dropStyle
                        +", net styleValue is "+styleValue);
        if (bold != null) bold.setSelected( (styleValue & Font.BOLD) != 0);
        if (italic != null) italic.setSelected( (styleValue & Font.ITALIC) != 0);
        setFont(jmri.util.FontUtil.deriveFont(getFont(),styleValue));

        //setSize(getPreferredSize().width, getPreferredSize().height);
		updateSize();
    }

    public void setForceControlOff(boolean set) {
        forceControlOff = set;
        if (disableItem!=null) disableItem.setSelected(getForceControlOff());
    }
    public boolean getForceControlOff() { return forceControlOff; }
    private boolean forceControlOff = false;
    
    public void setTristate(boolean set) {
    	tristate = set;
    }    
    public boolean getTristate() { return tristate; }
    private boolean tristate = false;

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    public void dispose() {
        italic = null;
        bold = null;
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

    public static JFrame makeAddIconFrame(String title, String select1, String select2, 
                                IconAdder editor, Container pos) {
        JFrame frame = new JFrame(rb.getString(title));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (select1 != null) p.add(new JLabel(rb.getString(select1)));
        if (select2 != null) p.add(new JLabel(rb.getString(select2)));
        frame.getContentPane().add(p,BorderLayout.NORTH);
        if (editor != null) {
            frame.getContentPane().add(editor);
            editor.setParent(frame);
        }

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                JFrame frame;
				public void windowClosing(java.awt.event.WindowEvent e) {
                    if (frame!=null) {
                        frame.dispose();
                        frame = null;
                    }
                }
                java.awt.event.WindowAdapter init(JFrame f) {
                    frame = f;
                    return this;
                }
            }.init(frame));
        frame.setLocationRelativeTo(pos);
        frame.setVisible(true);
        frame.pack();
        return frame;
    }
    

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableLabel.class.getName());

}
