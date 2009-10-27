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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import java.awt.Dimension;
import javax.swing.border.LineBorder;
import java.util.ResourceBundle;

/**
 * PositionableLabel is a JLabel that can be dragged around the
 * inside of the enclosing Container using a right-drag.
 * <P>
 * The positionable parameter is a global, set from outside.
 * The 'fixed' parameter is local, set from the popup here.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @version $Revision: 1.65 $
 */

public class PositionableLabel extends JLabel
                        implements MouseMotionListener, MouseListener,
                                    Positionable {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    
    public PositionableLabel(String s) {
        super(s);
        text = true;
        debug = log.isDebugEnabled();
        setProperToolTip();
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        connect();
    }
    public PositionableLabel(NamedIcon s) {
        super(s);
        icon = true;
        namedIcon = s;
        updateSize();
        debug = log.isDebugEnabled();
        setProperToolTip();
        connect();
    }

    public void setProperToolTip() {
        //setToolTipText(rb.getString("IconToolTip"));
		int system = jmri.util.SystemType.getType();
		if (system==jmri.util.SystemType.MACOSX) {
			setToolTipText(rb.getString("ToolTipGenericMac"));
		}
		else if (system==jmri.util.SystemType.WINDOWS) {
			setToolTipText(rb.getString("ToolTipGenericWin"));
		}
		else {
			setToolTipText(rb.getString("ToolTipGeneric"));
		}
    }

    public boolean isIcon() { return icon; }
    protected boolean icon = false;
    public boolean isText() { return text; }
    protected boolean text = false;

    NamedIcon namedIcon = null;

    /**
     * Connect listeners
     */
    void connect() {
        addMouseMotionListener(this);
        addMouseListener(this);
    }
    public void connect(LayoutEditor panel) {
		layoutPanel = panel;
    }

   	LayoutEditor layoutPanel = null;
    /**
     * Set panel (called from Layout Editor)
     */
    protected void setPanel(LayoutEditor panel) {
		layoutPanel = panel;
    }

    public LayoutEditor getLayoutPanel(){
        return layoutPanel;
    }
    
    PanelEditor panelEditor = null;
    /**
     * Set panel (called from Panel Editor)
     * @param panel
     */
    protected void setPanel(PanelEditor panel){
    	panelEditor = panel;
    }

    public PanelEditor getPanelEditor(){
        return panelEditor;
    }

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
    
    private boolean hidden = false;

    public void setHidden(boolean boo){
        hidden=boo;
    }
    

    public boolean getHidden(){
        return hidden;
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
        fixedWidth=width;
        fixedHeight=height;
        if ((width!=0) && (height!=0)){
            setSize(fixedWidth, fixedHeight);
        } else if ((width!=0) && (height==0)){
            setSize(fixedWidth, maxHeight());
        } else if ((width==0) && (height!=0)){
            setSize(maxWidth(), fixedHeight);
        } else
            setSize(maxWidth(), maxHeight());
    }

    public void setBackground(Color color){
        if (text){
            setOpaque(true);
            super.setBackground(color);
            updateSize();
        }
    }

    /**
     * Update the AWT and Swing size information due to change in internal
     * state, e.g. if one or more of the icons that might be displayed
     * is changed
     */
    protected void updateSize(){
        setSize(maxWidth(), maxHeight());
    }

    protected int maxWidth(){
        if ((fixedWidth==0) && (margin==0)){
            if(text)
                return ((javax.swing.JLabel)this).getMaximumSize().width;
            else
                return namedIcon.getIconWidth(); // defer to superclass
        }else if ((fixedWidth==0) && (margin!=0)){
            if(text)
                return ((javax.swing.JLabel)this).getMaximumSize().width+(margin*2);
            else
                return namedIcon.getIconWidth()+(margin*2);
        }else if ((fixedWidth!=0) && (margin!=0)){
            return fixedWidth-(margin*2);
        }
        return fixedWidth;
    }
    protected int maxHeight(){
        if ((fixedHeight==0) && (margin==0)){
            if(text)
                return ((javax.swing.JLabel)this).getMaximumSize().height;
            else
                return namedIcon.getIconHeight(); // defer to superclass
        }else if ((fixedHeight==0) && (margin!=0)){
            if (text)
                return ((javax.swing.JLabel)this).getMaximumSize().height+(margin*2);
            else
                return namedIcon.getIconHeight()+(margin*2);
        } else if ((fixedHeight!=0) && (margin!=0)){
            return fixedHeight-(margin*2);
        }
        return fixedHeight;
    }

    private Integer displayLevel;
    public void setDisplayLevel(Integer l) {
    	Integer oldDisplayLevel = displayLevel;
    	displayLevel = l;
    	if (oldDisplayLevel!=null && oldDisplayLevel!=l && panelEditor!=null){
    		log.debug("Changing label display level");
    		panelEditor.setDisplayLevel(this);
    	}
    }
    public void setDisplayLevel(int l) { setDisplayLevel(new Integer(l)); }
    public Integer getDisplayLevel() { return displayLevel; }
    
	public boolean isBackground() { return (displayLevel.intValue() == LayoutEditor.BKG.intValue());
    }

    // cursor location reference for this move (relative to object)
    int xClick = 0;
    int yClick = 0;

    public void mousePressed(MouseEvent e) {
		// if using LayoutEditor, let LayoutEditor handle the mouse pressed event
		if (layoutPanel!=null) {
			layoutPanel.handleMousePressed(e,this.getX(),this.getY());
			return;
		}
        _saveFixed = getFixed();    // may get teporarily fixed to move icon underneath this one
        if (panelEditor!=null) {
            if (!e.isMetaDown()) {
                panelEditor.doMousePressed(getX()+e.getX(), getY()+e.getY(), true);
            }
            else if (e.isShiftDown()) {
                panelEditor.doMousePressedShift(getX()+e.getX(), getY()+e.getY());
                setFixed(true);  // hold this icon temporarily while moving what is below
            }
        }
        // remember where we are
        xClick = e.getX();
        yClick = e.getY();
        // if (debug) log.debug("mousePressed: "+where(e));
        if (e.isPopupTrigger()) {
            //if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
		// if using LayoutEditor, let LayoutEditor handle the mouse released event
		if (layoutPanel!=null) {
			layoutPanel.handleMouseReleased(e,getX(),getY());
			return;
		}
        boolean wasDragging = false;
        if (panelEditor!=null) {
             List <JComponent> list = panelEditor.getSelections();
             log.debug("mouseReleased "+(list!=null && list.contains(this)));
             wasDragging = panelEditor.doMouseReleased(getX()+e.getX(), getY()+e.getY(), 
                                                   (list!=null && list.contains(this)) );
        }
        setFixed(_saveFixed);  // restore (if needed)
        // if (debug) log.debug("mouseReleased: "+where(e));
        if (e.isPopupTrigger() && !wasDragging) {
            showPopUp(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
		if (layoutPanel!=null) {
			layoutPanel.handleMouseClicked(e, this.getX(), this.getY());
			return;
		}
        if (panelEditor!=null) {
             List <JComponent> list = panelEditor.getSelections();
             log.debug("mouseClicked "+(list!=null && list.contains(this)));
             panelEditor.doMouseClicked(getX()+e.getX(), getY()+e.getY(), 
                                                   (list!=null && list.contains(this)) );
        } else if (debug) log.debug("mouseClicked: "+where(e));
        if (debug && e.isMetaDown()) log.debug("meta down");
        if (debug && e.isAltDown()) log.debug(" alt down");
        if (e.isPopupTrigger()) {
            //if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }

    public void mouseExited(MouseEvent e) {
        // if (debug) log.debug("Exited:  "+where(e));
    }
    public void mouseEntered(MouseEvent e) {
        // if (debug) log.debug("Entered: "+where(e));
    }

    public void mouseMoved(MouseEvent e) {
		if (layoutPanel!=null) layoutPanel.setLoc((int)((getX()+e.getX())/layoutPanel.getZoomScale()),
							(int)((getY()+e.getY())/layoutPanel.getZoomScale())); 
        //if (debug) log.debug("Moved:   "+where(e));
    }
    public void mouseDragged(MouseEvent e) {
		// if using LayoutEditor, let LayoutEditor handle the mouse dragged event
		if (layoutPanel!=null) {
			layoutPanel.handleMouseDragged(e,getX(),getY());
			return;
		}
        if (panelEditor!=null) {
            if (!e.isMetaDown()) {
                panelEditor.doMouseDragged(getX()+e.getX(), getY()+e.getY(), true);
            }
            else {
                if (!getPositionable()) return;
                panelEditor.doMouseDragged(getX()+e.getX(), getY()+e.getY(), false);
                List <JComponent> list = panelEditor.getSelections();
                int deltaX = e.getX() - xClick;
                int deltaY = e.getY() - yClick;
                if ((list==null) || !list.contains(this)) {
                    moveLabel(deltaX, deltaY);
                    panelEditor.doMousePressed(getX()+e.getX(), getY()+e.getY(), false);
                } else if (!getFixed()) {
                    for (int i=0; i<list.size(); i++){
                        JComponent comp = list.get(i);
                        if (comp instanceof PositionableLabel) {
                            ((PositionableLabel)comp).moveLabel(deltaX, deltaY);
                        } else if (comp instanceof PositionableJPanel) {
                            ((PositionableJPanel)comp).movePanel(deltaX, deltaY);
                        }
                    }
                    panelEditor.moveSelectRect(deltaX, deltaY);
                } else if (e.isShiftDown()) {
                    deltaX = e.getX() - xClick;
                    deltaY = e.getY() - yClick;
                    for (int i=0; i<list.size(); i++){
                        JComponent comp = list.get(i);
                        if (comp instanceof PositionableLabel) {
                            ((PositionableLabel)comp).moveLabel(deltaX, deltaY);
                        } else if (comp instanceof PositionableJPanel) {
                            ((PositionableJPanel)comp).movePanel(deltaX, deltaY);
                        }
                    }
                    xClick = e.getX();
                    yClick = e.getY();
                    this.repaint();
                }
            }
        }
    }

    // update object postion by how far dragged
    void moveLabel(int deltaX, int deltaY) {
        if (getPositionable() && !getFixed()) {
            int xObj = getX() + deltaX;
            int yObj = getY() + deltaY;
            // don't allow negative placement, icon can become unreachable
            if (xObj < 0) xObj = 0;
            if (yObj < 0) yObj = 0;
            this.setLocation(xObj, yObj);
            // and show!
            this.repaint();
        }
    }

    protected JPopupMenu popup = null;
    protected JLabel ours;
    /**
     * For over-riding in the using classes: only provides icon rotation
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        if (icon) {
            popup = new JPopupMenu();                                    
             
            popup.add(new AbstractAction(rb.getString("Rotate")) {
                public void actionPerformed(ActionEvent e) {
                    namedIcon.setRotation(namedIcon.getRotation()+1, ours);
                    updateSize();
                    setIcon(namedIcon);
                    // bug fix, must repaint icons that have same width and height
                    repaint();
                }
            });
            checkLocationEditable(popup, getText());
            
            addFixedItem(popup);
            addShowTooltipItem(popup);
            
            popup.add(new AbstractAction(rb.getString("EditPlainIcon")) {
                    public void actionPerformed(ActionEvent e) {
                        edit();
                    }
                });

            popup.add(setHiddenMenu());
            popup.add(new AbstractAction(rb.getString("Remove")) {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });
        } else if (text) {
            popup = new JPopupMenu();
            
            checkLocationEditable(popup, getText());

            if (fixedWidth==0)
                popup.add("Width= Auto");
            else
                popup.add("Width= " + this.maxWidth());

            if (fixedHeight==0)
                popup.add("Height= Auto");
            else
                popup.add("Height= " + this.maxHeight());

            if((fixedHeight==0)||(fixedWidth==0))
                popup.add("Margin= " + this.getMargin());
            if (hidden) popup.add(rb.getString("Hidden"));
            else popup.add(rb.getString("NotHidden"));
            
            popup.addSeparator();

			if (layoutPanel!=null){
	            popup.add(new AbstractAction("Set x & y") {
					public void actionPerformed(ActionEvent e) {
						String name = getText();
						displayCoordinateEdit(name);
					}
				});
		}
			popup.add(new AbstractAction(rb.getString("SetFixedSize")) {
				public void actionPerformed(ActionEvent e) {
					String name = getText();
					fixedSizeEdit(name);
				}
			});
            if((fixedHeight==0)||(fixedWidth==0)){
                popup.add(new AbstractAction(rb.getString("SetMarginSize")) {
                    public void actionPerformed(ActionEvent e) {
                        String name = getText();
                        marginSizeEdit(name);
                    }
                });
            }
            //popup.add(makeFontSizeMenu());

            //popup.add(makeFontStyleMenu());

            //popup.add(makeFontColorMenu());

            popup.add(makeBackgroundFontColorMenu());

            popup.add(textBorderMenu(getText()));
            popup.add(setHiddenMenu());
            addFixedItem(popup);
            addShowTooltipItem(popup);
            
            addTextEditEntry(popup, true);
            popup.add(new AbstractAction(rb.getString("Remove")) {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        } else if (!text && !icon)
            log.warn("showPopUp when neither text nor icon true");
        // show the result
        if (popup != null) popup.show(e.getComponent(), e.getX(), e.getY());
    }

    ButtonGroup hiddenButtonGroup;
    
    JMenu setHiddenMenu(){
        JMenu hiddenMenu = new JMenu("Hide");
        
        hiddenButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r = new JRadioButtonMenuItem("Hide");
        r.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { setHidden(true); }
        });
        hiddenButtonGroup.add(r);
        if (getHidden()) r.setSelected(true);
        else r.setSelected(false);
        hiddenMenu.add(r);
        
        r = new JRadioButtonMenuItem("Visible");
        r.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { setHidden(false); }
        });
        hiddenButtonGroup.add(r);
        if (!getHidden()) r.setSelected(true);
        else r.setSelected(false);
        hiddenMenu.add(r);
        
        return hiddenMenu;
	}

    JFrame _editorFrame;
    IconAdder _editor;
    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder();
        NamedIcon icon = new NamedIcon(namedIcon);
        _editor.setIcon(0, "plainIcon", icon);
        makeAddIconFrame("EditIcon", "addIconToPanel", 
                                     "pressAdd", _editor);
        _editor.makeIconPanel();

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editIcon();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _editor.addCatalog();
                    _editorFrame.pack();
                }
        };
        _editor.complete(addIconAction, changeIconAction, false, true);

    }

    void editIcon() {
        String url = _editor.getIcon("plainIcon").getURL();
        namedIcon = NamedIcon.getIconByName(url);
        setIcon(namedIcon);
        updateSize();
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
        invalidate();
    }

    protected JMenu makeFontSizeMenu() {
        JMenu sizeMenu = new JMenu("Font Size");
        fontButtonGroup = new ButtonGroup();
        addFontMenuEntry(sizeMenu, 6);
        addFontMenuEntry(sizeMenu, 8);
        addFontMenuEntry(sizeMenu, 10);
        addFontMenuEntry(sizeMenu, 11);
        addFontMenuEntry(sizeMenu, 12);
        addFontMenuEntry(sizeMenu, 14);
        addFontMenuEntry(sizeMenu, 16);
        addFontMenuEntry(sizeMenu, 20);
        addFontMenuEntry(sizeMenu, 24);
        addFontMenuEntry(sizeMenu, 28);
        addFontMenuEntry(sizeMenu, 32);
        addFontMenuEntry(sizeMenu, 36);
        return sizeMenu;
    }
    
    JMenu textBorderMenu(final String name) {
        JMenu borderMenu = new JMenu("Border Menu");
        borderMenu.add("Border Size= " + borderSize);
        borderMenu.addSeparator();
        borderMenu.add(new AbstractAction(rb.getString("SetBorderSize")) {
				public void actionPerformed(ActionEvent e) {
					displayBorderEdit(name);
				}
			});

        borderMenu.add(makeBorderColorMenu());
        return borderMenu;
    }

    void addFontMenuEntry(JMenu menu, final int size) {
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
        italic = new JCheckBoxMenuItem(rb.getString("Italic"));
        italic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                italic.setSelected(!italic.isSelected());
                setItalic();
            }
        });
        styleMenu.add(italic);
        bold = new JCheckBoxMenuItem(rb.getString("Bold"));
        bold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bold.setSelected(!bold.isSelected());
                setBold();
            }
        });
        setFontStyle(0, 0);
        styleMenu.add(bold);
        return styleMenu;     
    }

    /*JMenu makeFontStyleMenu() {
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
    
    public JMenuItem newStyleMenuItem(AbstractAction a, int mask) {
        // next two lines needed because JCheckBoxMenuItem(AbstractAction) not in 1.1.8
        JCheckBoxMenuItem c = new JCheckBoxMenuItem((String)a.getValue(AbstractAction.NAME));
        c.addActionListener(a);
        if (log.isDebugEnabled()) log.debug("When creating style item "+((String)a.getValue(AbstractAction.NAME))
                                            +" mask was "+mask+" state was "+getFont().getStyle());
        if ( (mask & getFont().getStyle()) == mask ) c.setSelected(true);
        return c;
    }*/

    protected void checkLocationEditable(JPopupMenu popup,  String name) {
		if (layoutPanel!=null){
			popup.add("x= " + this.getX());
			popup.add("y= " + this.getY());
	}
			
		if (getViewCoordinates()) {
            if (icon) {
                /*
                popup.add(new AbstractAction(rb.getString("shear")) {
                        public void actionPerformed(ActionEvent e) {
                            tranform(e, BOX_TYPE_SHEAR);
                        }
                    });
                */
                popup.add(new AbstractAction(rb.getString("rotate")) {
                        public void actionPerformed(ActionEvent e) {
                            tranform(e, BOX_TYPE_ROTATE);
                        }
                    });
                popup.add(new AbstractAction(rb.getString("scale")) {
                        public void actionPerformed(ActionEvent e) {
                            tranform(e, BOX_TYPE_SCALE);
                        }
                    });
            }
			popup.add("x= " + this.getX());
			popup.add("y= " + this.getY());
			popup.add("level= " + this.getDisplayLevel().intValue());
        }
        if (!_saveFixed) {  // this is user's setting
            if (panelEditor!=null){
                List <JComponent> list = panelEditor.getSelections();
                if (list!=null) {
                    if (list.contains(this)) {
                        popup.add(new AbstractAction(rb.getString("AlignX")) {
                            public void actionPerformed(ActionEvent e) {
                                alignGroup(true);
                            }
                        });
                        popup.add(new AbstractAction(rb.getString("AlignY")) {
                            public void actionPerformed(ActionEvent e) {
                                alignGroup(false);
                            }
                        });
                    }
                }
            }
        }
			popup.add(new PopupAction(name));
    }



    protected void alignGroup(boolean alignX) {
        List <JComponent> list = panelEditor.getSelections();
        int sum = 0;
        int cnt = 0;
        for (int i=0; i<list.size(); i++) {
            JComponent comp = list.get(i);
            if (comp instanceof PositionableLabel) {
                if (((PositionableLabel)comp).getFixed() ) continue;
            }else if (comp instanceof PositionableJPanel) {
                if (!((PositionableJPanel)comp).getPositionable()) continue;
            }
            if (alignX) {
                sum += comp.getX();
            } else {
                sum += comp.getY();
            }
            cnt++;
        }
        int ave = Math.round((float)sum/cnt);
        for (int i=0; i<list.size(); i++) {
            JComponent comp = list.get(i);
            if (comp instanceof PositionableLabel) {
                if (((PositionableLabel)comp).getFixed() ) continue;
            }else if (comp instanceof PositionableJPanel) {
                if (!((PositionableJPanel)comp).getPositionable()) continue;
            }
            if (alignX) {
                comp.setLocation(ave, comp.getY());
            } else {
                comp.setLocation(comp.getX(), ave);
            }
        }
    }

    class PopupAction extends AbstractAction {
        String name;
        PopupAction(String n) {
            super(rb.getString("SetLocation"));
            name = n;
        }
        public void actionPerformed(ActionEvent e) {
            displayCoordinateEdit(name);
        }
    }

    public void displayBorderEdit(String name){
    	if (log.isDebugEnabled())
			log.debug("make Border Edit menu");
        TextBorderSizeEdit f = new TextBorderSizeEdit();
		f.addHelpMenu("package.jmri.jmrit.display.TextBorderSizeEdit", true);
		try {
			f.initComponents(this, name);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);

    }

    public void fixedSizeEdit(String name) {
		if (log.isDebugEnabled())
			log.debug("make new coordinate menu");
        FixedSizeEdit f = new FixedSizeEdit();

		f.addHelpMenu("package.jmri.jmrit.display.fixedSizeEdit", true);
		try {
			f.initComponents(this, name);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

    public void marginSizeEdit(String name) {
		if (log.isDebugEnabled())
			log.debug("make new coordinate menu");
         TextMarginSizeEdit f = new TextMarginSizeEdit();
		
		f.addHelpMenu("package.jmri.jmrit.display.marginSizeEdit", true);
		try {
			f.initComponents(this, name);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

    public void displayCoordinateEdit(String name) {
		if (log.isDebugEnabled())
			log.debug("make new coordinate menu");
        CoordinateEdit f = new CoordinateEdit();
        f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
		f.initComponents(this, name);
		f.setVisible(true);	
	}
    
    /*protected JMenu makeFontColorMenu() {
        JMenu colorMenu = new JMenu(rb.getString("FontColor"));
        colorButtonGroup = new ButtonGroup();
        addColorMenuEntry(colorMenu, rb.getString("Black"), Color.black);
        addColorMenuEntry(colorMenu, rb.getString("DarkGray"),Color.darkGray);
        addColorMenuEntry(colorMenu, rb.getString("Gray"),Color.gray);
        addColorMenuEntry(colorMenu, rb.getString("LightGray"),Color.lightGray);
        addColorMenuEntry(colorMenu, rb.getString("White"),Color.white);
        addColorMenuEntry(colorMenu, rb.getString("Red"),Color.red);
        addColorMenuEntry(colorMenu, rb.getString("Orange"),Color.orange);
        addColorMenuEntry(colorMenu, rb.getString("Yellow"),Color.yellow);
        addColorMenuEntry(colorMenu, rb.getString("Green"),Color.green);
        addColorMenuEntry(colorMenu, rb.getString("Blue"),Color.blue);
        addColorMenuEntry(colorMenu, rb.getString("Magenta"),Color.magenta);
        return colorMenu;
    }*/

    protected JMenu makeBorderColorMenu() {
        JMenu colorMenu = new JMenu(rb.getString("ColorMenu"));
        colorBorderButtonGroup = new ButtonGroup();
        addColorMenuEntry(colorMenu, rb.getString("Black"), Color.black, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("DarkGray"),Color.darkGray, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("Gray"),Color.gray, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("LightGray"),Color.lightGray, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("White"),Color.white, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("Red"),Color.red, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("Orange"),Color.orange, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("Yellow"),Color.yellow, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("Green"),Color.green, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("Blue"),Color.blue, 0x02);
        addColorMenuEntry(colorMenu, rb.getString("Magenta"),Color.magenta, 0x02);
        return colorMenu;
    }

    protected JMenu makeFontColorMenu() {
        JMenu colorMenu = new JMenu(rb.getString("FontColor"));
        colorButtonGroup = new ButtonGroup();
        addColorMenuEntry(colorMenu, rb.getString("Black"), Color.black, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("DarkGray"),Color.darkGray, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("Gray"),Color.gray, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("LightGray"),Color.lightGray, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("White"),Color.white, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("Red"),Color.red, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("Orange"),Color.orange, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("Yellow"),Color.yellow, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("Green"),Color.green, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("Blue"),Color.blue, 0x00);
        addColorMenuEntry(colorMenu, rb.getString("Magenta"),Color.magenta, 0x00);
        return colorMenu;
    }

    protected JMenu makeBackgroundFontColorMenu() {
        JMenu colorMenu = new JMenu(rb.getString("FontBackgroundColor"));
        colorBackButtonGroup = new ButtonGroup();
        addColorMenuEntry(colorMenu, rb.getString("Black"), Color.black, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("DarkGray"),Color.darkGray, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("Gray"),Color.gray, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("LightGray"),Color.lightGray, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("White"),Color.white, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("Red"),Color.red, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("Orange"),Color.orange, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("Yellow"),Color.yellow, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("Green"),Color.green, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("Blue"),Color.blue, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("Magenta"),Color.magenta, 0x01);
        addColorMenuEntry(colorMenu, rb.getString("Clear"), null, 0x01);
        return colorMenu;
    }

    void addColorMenuEntry(JMenu menu, final String name, final Color color, final int foreground) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            final Color desiredColor = color;
            public void actionPerformed(ActionEvent e) {
                if (foreground==0x00) setForeground(desiredColor);
                else if (foreground==0x01){
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
                        setBackground(desiredColor);
                }
                else setBorderColor(desiredColor);
            }
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        if (foreground==0x00){
            colorButtonGroup.add(r);
            if (getForeground().getRGB() == color.getRGB())  r.setSelected(true);
            else r.setSelected(false);
        }
        else if (foreground==0x01){
            colorBackButtonGroup.add(r);
            if (color==null){
                if (!isOpaque())
                    r.setSelected(true);
                else
                    r.setSelected(false);
            } else if (getBackground().getRGB() == color.getRGB()) r.setSelected(true);
            else r.setSelected(false);
        }
        else{
            colorBorderButtonGroup.add(r);
            if( getBorderColor()!=null)
                if (getBorderColor().getRGB() == color.getRGB())  r.setSelected(true);
            else r.setSelected(false);
        }
        //if (getForeground().getRGB() == color.getRGB())  r.setSelected(true);
        //else r.setSelected(false);
        menu.add(r);
    }
        
    /*void addColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            final Color desiredColor = color;
            public void actionPerformed(ActionEvent e) { setForeground(desiredColor); }
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        colorButtonGroup.add(r);
        if (getForeground().getRGB() == color.getRGB())  r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }*/

    JCheckBoxMenuItem showTooltipItem = null;
    void addShowTooltipItem(JPopupMenu popup) {
        showTooltipItem = new JCheckBoxMenuItem(rb.getString("Tooltip"));
        showTooltipItem.setSelected(getShowTooltip());
        popup.add(showTooltipItem);
        showTooltipItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setShowTooltip(showTooltipItem.isSelected());
            }
        });
    }
        
    JCheckBoxMenuItem showFixedItem = null;
    void addFixedItem(JPopupMenu popup) {
        showFixedItem = new JCheckBoxMenuItem(rb.getString("Fixed"));
        showFixedItem.setSelected(getFixed());
        popup.add(showFixedItem);
        showFixedItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doSetFixed(showFixedItem.isSelected());
            }
        });
    }
        
    JCheckBoxMenuItem disableItem = null;
    void addDisableMenuEntry(JPopupMenu popup) {
        disableItem = new JCheckBoxMenuItem(rb.getString("Disable"));
        disableItem.setSelected(getForceControlOff());
        popup.add(disableItem);
        disableItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setForceControlOff(disableItem.isSelected());
            }
        });
    }

    static private final int BOX_TYPE_TEXT = 1;
//    static private final int BOX_TYPE_SHEAR = 2;
    static private final int BOX_TYPE_SCALE = 3;
    static private final int BOX_TYPE_ROTATE = 4;

    void tranform(ActionEvent e, int type) {
        if (log.isDebugEnabled())
            log.debug("tranform action: cmd="+e.getActionCommand());
        String title = null;
        switch (type) {
            case BOX_TYPE_TEXT:
                title = "ChangeText";
                break;
/*
            case BOX_TYPE_SHEAR:
                title = "shear";
                break;
*/
            case BOX_TYPE_SCALE:
                title = "scale";
                break;
            case BOX_TYPE_ROTATE:
                title = "rotate";
                break;
        }
        makeTextBoxFrame(title, "pressAdd", type);
    }

    JTextField _textBox;
//    JTextField _textBox2;
    void makeTextBoxFrame(String title, String select, int type) {
        _editorFrame = new JFrame(rb.getString("Edit"));
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(5,5));
        p.add(new JLabel(rb.getString(title)), BorderLayout.NORTH);
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p.add(p2, BorderLayout.CENTER);
        _textBox = new JTextField();
        if (type==BOX_TYPE_TEXT) {
            _textBox.setText(getText());
        }
        p2.add(_textBox);
/*
        if (type==BOX_TYPE_SHEAR) {
            _textBox2 = new JTextField();
            p2.add(_textBox2);
        }
*/
        JButton button = new JButton(rb.getString("Done"));
        button.addActionListener(new ActionListener() {
                int type;
            public void actionPerformed(ActionEvent a) {
                switch (type) {
                    case BOX_TYPE_TEXT:
                        setText(_textBox.getText());
                        setIconTextGap (-(getWidth()+getPreferredSize().width)/2);
                        setSize(getPreferredSize().width, getPreferredSize().height);
                        break;
/*                    case BOX_TYPE_SHEAR:
                        int x = 0;
                        try {
                            x = Integer.parseInt(_textBox.getText());
                        } catch (NumberFormatException nfe) { break; }
                        int y = 0;
                        try {
                            y = Integer.parseInt(_textBox2.getText());
                        } catch (NumberFormatException nfe) { break; }
                        namedIcon.shear(x, y);
                        setIcon(namedIcon);
                        updateSize();
                        break;
*/
                    case BOX_TYPE_SCALE:
                        int scale = 0;
                        try {
                            scale = Integer.parseInt(_textBox.getText());
                        } catch (NumberFormatException nfe) { break; }
                        scale(scale);
                        break;
                    case BOX_TYPE_ROTATE:
                        int deg = 0;
                        try {
                            deg = Integer.parseInt(_textBox.getText());
                        } catch (NumberFormatException nfe) { break; }
                        rotate(deg);
                        break;
                }
                _editorFrame.dispose();
                _editorFrame = null;
                repaint();
            }
            ActionListener init(int t) {
                type = t;
                return this;
            }
        }.init(type));
        p.add(button, BorderLayout.SOUTH);
        _editorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
                    _editorFrame.dispose();
                    _editorFrame = null;
                }
            });
        _editorFrame.getContentPane().add(p);
        _editorFrame.setLocationRelativeTo(this);
        _editorFrame.setVisible(true);
        _editorFrame.pack();
    }

    void scale(int s) {
        namedIcon.scale(s, this);
        setIcon(namedIcon);
        updateSize();
    }

    void rotate(int deg) {
        namedIcon.rotate(deg, this);
        setIcon(namedIcon);
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

    void addTextEditEntry(JPopupMenu popup, boolean change) {
        JMenu edit = new JMenu(rb.getString("EditText"));
        popup.add(edit);
        edit.add(makeFontSizeMenu());
        edit.add(makeFontStyleMenu());
        edit.add(makeFontColorMenu());
        if (change){
            edit.add(new AbstractAction(rb.getString("ChangeText")) {
                    public void actionPerformed(ActionEvent e) {
                        changeText(e);
                    }
                });
        }
    }

    void changeText(ActionEvent e) {
        if (log.isDebugEnabled())
            log.debug("changeText action: cmd="+e.getActionCommand()+", source= "+e.getSource()
                        +", etc. "+e.toString());
        //makeAddIconFrame("EditText", null, "pressAdd", null);
        tranform(e, BOX_TYPE_TEXT);
    }
    
    JCheckBoxMenuItem italic = null;
    JCheckBoxMenuItem bold = null;
    ButtonGroup fontButtonGroup = null;
    ButtonGroup colorButtonGroup = null;
    ButtonGroup colorBackButtonGroup = null;
    ButtonGroup colorBorderButtonGroup = null;

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

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    boolean debug = false;

    public void setPositionable(boolean enabled) { positionable = enabled; }
    public boolean getPositionable() { return positionable; }
    private boolean positionable = true;
    
    public void setViewCoordinates(boolean enabled) { viewCoordinates = enabled; }
    public boolean getViewCoordinates() { return viewCoordinates; }
    private boolean viewCoordinates = false;

    //public void setEditable(boolean enabled) {editable = enabled;}
    public void setEditable(boolean enabled) {
        editable = enabled;
        if((hidden) && (editable))
            setVisible(true);
        else if ((hidden) && (!editable))
            setVisible(false);
        }
    public boolean getEditable() { return editable; }
    private boolean editable = true;

    public void setFixed(boolean enabled) {
        fixed = enabled;
        if (showFixedItem!=null) showFixedItem.setSelected(getFixed());
    }

    protected void doSetFixed(boolean enabled) {
        if (layoutPanel!=null) {
	        fixed = enabled;
	        if (showFixedItem!=null) showFixedItem.setSelected(getFixed());
			return;
		}
		if (panelEditor!=null) {
            List <JComponent> list = panelEditor.getSelections();
            if (list!=null && list.contains(this)) {
                for (int i=0; i<list.size(); i++) {
                    JComponent comp = list.get(i);
                    if (comp instanceof PositionableLabel) {
                        ((PositionableLabel)comp).setFixed(enabled);
                    } else if (comp instanceof PositionableJPanel) {
                        ((PositionableJPanel)comp).setPositionable(!enabled);
                    }
                }
            } else { setFixed(enabled); }
        }
    }
    public boolean getFixed() { return fixed; }
    private boolean fixed = false;
    private boolean _saveFixed = false;

    public void setControlling(boolean enabled) {controlling = enabled;}
    public boolean getControlling() { return controlling; }
    private boolean controlling = true;

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

    public void setShowTooltip(boolean set) {
        if (set)
            setProperToolTip();
        else
            setToolTipText(null);
        showTooltip = set;
        if (showTooltipItem!=null) showTooltipItem.setSelected(getShowTooltip());
    }
    public boolean getShowTooltip() { return showTooltip; }
    private boolean showTooltip = true;

    public String getNameString() {
        if (icon) return "(Icon)";
        else if (text) return "(Text)";
        else return "None!";
    }

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    public void dispose() {
        if (popup != null) popup.removeAll();
        fontButtonGroup = null;
        colorButtonGroup = null;
        colorBackButtonGroup = null;
        colorBorderButtonGroup = null;
        popup = null;
        italic = null;
        bold = null;
        ours = null;
    }

    public void updateLevel(){
        if (layoutPanel!=null) layoutPanel.removeObject(this);
        Container parent = this.getParent();
        parent.remove(this);
        parent.validate();
        if (layoutPanel!=null) layoutPanel.putLabel(this);
    }
    
    /**
     * Removes this object from display and persistance
     */
    public void remove() {
		if (layoutPanel!=null) layoutPanel.removeObject(this);
        if (panelEditor != null) panelEditor.remove(this);
        Point p = this.getLocation();
        int w = this.getWidth();
        int h = this.getHeight();
        Container parent = this.getParent();
        parent.remove(this);
        // force redisplay
        parent.validate();
        parent.repaint(p.x,p.y,w,h);

        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    void makeAddIconFrame(String title, String select1, String select2, 
                                IconAdder editor) {
        _editorFrame = new JFrame(rb.getString(title));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (select1 != null) p.add(new JLabel(rb.getString(select1)));
        if (select2 != null) p.add(new JLabel(rb.getString(select2)));
        _editorFrame.getContentPane().add(p,BorderLayout.NORTH);
        if (editor != null) {
            _editorFrame.getContentPane().add(editor);
            editor.setParent(_editorFrame);
        }

        _editorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
                    _editorFrame.dispose();
                    _editorFrame = null;
                }
            });
        _editorFrame.setLocationRelativeTo(this);
        _editorFrame.setVisible(true);
        _editorFrame.pack();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableLabel.class.getName());

}
