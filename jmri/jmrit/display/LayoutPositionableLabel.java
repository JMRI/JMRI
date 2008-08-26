package jmri.jmrit.display;

import jmri.jmrit.catalog.NamedIcon;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.util.JmriJFrame;

import java.util.ResourceBundle;

/**
 * LayoutPositionableLabel is a JLabel that can be dragged around the
 * inside of the Layout Editor panel using a right-drag.
 * <P>
 * This module is derived from PositionalLabel.java by 
 *   Bob Jacobsen Copyright (c) 2002, Revision 1.30 
 * <P>
 * A name change was needed to work around the hard dependence on PanelEditor
 *   in PositionaleLabelXml.java, without the possibility of compromising any
 *   existing PanelEditor panels. The two routines have diverged as new features
 *	 were added to each.
 * <P>
 * The positionable parameter is a global, set from outside.
 * The 'fixed' parameter is local, set from the popup here.
 * <P>
 * Since Layout Editor does not currently use turnout icons, tristate code is 
 * included here, but commented out.
 *
 * @author Dave Duchamp Copyright (c) 2007, 2008
 * @version $Revision: 1.8 $
 */

public class LayoutPositionableLabel extends JLabel
					implements MouseMotionListener, MouseListener,Positionable {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");

    public LayoutPositionableLabel(String s) {
        super(s);
        text = true;
        debug = log.isDebugEnabled();
        setProperToolTip();
    }
    public LayoutPositionableLabel(NamedIcon s) {
        super(s);
        icon = true;
        namedIcon = s;
        updateSize();
        debug = log.isDebugEnabled();
        setProperToolTip();
    }

    public void setProperToolTip() {
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
    boolean icon = false;
    public boolean isText() { return text; }
    boolean text = false;

    NamedIcon namedIcon = null;
	protected LayoutEditor layoutPanel = null;

    /**
     * Connect listeners - called from Layout Editor
     */
    public void connect(LayoutEditor panel) {
        addMouseMotionListener(this);
        addMouseListener(this);
		layoutPanel = panel;
    }

    /**
     * Set panel (called from Layout Editor)
     */
    public void setPanel(LayoutEditor panel) {
		layoutPanel = panel;
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
        return namedIcon.getIconWidth();
    }
    protected int maxHeight(){
        return namedIcon.getIconHeight();
    }

    private Integer displayLevel;
    public void setDisplayLevel(Integer l) { displayLevel = l; }
    public void setDisplayLevel(int l) { setDisplayLevel(new Integer(l)); }
    public Integer getDisplayLevel() { return displayLevel; }
	public boolean isBackground() { return (displayLevel.intValue() == LayoutEditor.BKG.intValue());}

    // cursor location reference for this move (relative to object)
    int xClick = 0;
    int yClick = 0;

    public void mousePressed(MouseEvent e) {
		// allow Layout Editor to handle the mouse pressed event
		layoutPanel.handleMousePressed(e, this.getX(), this.getY());
    }

    public void mouseReleased(MouseEvent e) {
		// allow Layout Editor to handle the mouse released event
		layoutPanel.handleMouseReleased(e, this.getX(), this.getY());
    }

    public void mouseClicked(MouseEvent e) {
        if (debug) log.debug("Clicked: "+where(e));
        if (debug && e.isMetaDown()) log.debug("meta down");
        if (debug && e.isAltDown()) log.debug(" alt down");
    }
    public void mouseExited(MouseEvent e) {
        // if (debug) log.debug("Exited:  "+where(e));
    }
    public void mouseEntered(MouseEvent e) {
        // if (debug) log.debug("Entered: "+where(e));
    }

    public void mouseMoved(MouseEvent e) {
		// update coordinates in Layout Editor tool bar
		layoutPanel.setLoc((int)((getX()+e.getX())/layoutPanel.getZoomScale()),
							(int)((getY()+e.getY())/layoutPanel.getZoomScale())); 
    }
    public void mouseDragged(MouseEvent e) {
		// allow Layout Editor to handle the mouse dragged event
		layoutPanel.handleMouseDragged(e, this.getX(), this.getY());
    }

    JPopupMenu popup = null;
    JLabel ours;
    /**
     * For over-riding in the using classes: only provides icon rotation
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        if (icon) {
            popup = new JPopupMenu();
			popup.add("x= " + this.getX());
			popup.add("y= " + this.getY());
			popup.add(new AbstractAction("Set x & y") {
				public void actionPerformed(ActionEvent e) {
					String name = getText();
					displayCoordinateEdit(name);
				}
			});
			if (!isBackground()) {
				popup.add(new AbstractAction(rb.getString("Rotate")) {
					public void actionPerformed(ActionEvent e) {
						namedIcon.setRotation(namedIcon.getRotation()+1, ours);
						updateSize();
						setIcon(namedIcon);
					}
				});
				addFixedItem(popup);
				addShowTooltipItem(popup);
			}            
            popup.add(new AbstractAction(rb.getString("Remove")) {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });
        } else if (text) {
            popup = new JPopupMenu();
			popup.add("x= " + this.getX());
			popup.add("y= " + this.getY());
			popup.add(new AbstractAction("Set x & y") {
				public void actionPerformed(ActionEvent e) {
					String name = getText();
					displayCoordinateEdit(name);
				}
			});
            popup.add(makeFontSizeMenu());

            popup.add(makeFontStyleMenu());

            popup.add(makeFontColorMenu());

            addFixedItem(popup);
            addShowTooltipItem(popup);
            
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

    JMenu makeFontSizeMenu() {
        JMenu sizeMenu = new JMenu(rb.getString("FontSize"));
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
        setSize(getPreferredSize().width, getPreferredSize().height);
    }

    JMenu makeFontStyleMenu() {
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
    
    public void displayCoordinateEdit(String name) {
		if (log.isDebugEnabled())
			log.debug("make new coordinate menu");
		LayoutCoordinateEdit f = new LayoutCoordinateEdit();
		f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
		try {
			f.initComponents(this, name);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
    
    JMenu makeFontColorMenu() {
        JMenu colorMenu = new JMenu(rb.getString("FontColor"));
        colorButtonGroup = new ButtonGroup();
        addColorMenuEntry(colorMenu, rb.getString("Black"), Color.black);
        addColorMenuEntry(colorMenu, rb.getString("DarkGray"),Color.darkGray);
        addColorMenuEntry(colorMenu, rb.getString("Gray"),Color.gray);
        addColorMenuEntry(colorMenu, rb.getString("LightGray"),Color.lightGray);
        addColorMenuEntry(colorMenu, rb.getString("White"),Color.white);
        addColorMenuEntry(colorMenu, rb.getString("Red"),Color.red);
        addColorMenuEntry(colorMenu, rb.getString("Pink"),Color.pink);
        addColorMenuEntry(colorMenu, rb.getString("Orange"),Color.orange);
        addColorMenuEntry(colorMenu, rb.getString("Yellow"),Color.yellow);
        addColorMenuEntry(colorMenu, rb.getString("Green"),Color.green);
        addColorMenuEntry(colorMenu, rb.getString("Blue"),Color.blue);
        addColorMenuEntry(colorMenu, rb.getString("Magenta"),Color.magenta);
        addColorMenuEntry(colorMenu, rb.getString("Cyan"),Color.cyan);
        return colorMenu;
    }
        
    void addColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            final String desiredName = name;
            final Color desiredColor = color;
            public void actionPerformed(ActionEvent e) { setForeground(desiredColor); }
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);
        colorButtonGroup.add(r);
        if (getForeground().getRGB() == color.getRGB())  r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }

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
                setFixed(showFixedItem.isSelected());
            }
        });
    }
        
    JCheckBoxMenuItem disableItem = null;
    void addDisableMenuEntry(JPopupMenu popup) {
        disableItem = new JCheckBoxMenuItem(rb.getString("Disabled"));
        disableItem.setSelected(getForceControlOff());
        popup.add(disableItem);
        disableItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setForceControlOff(disableItem.isSelected());
            }
        });
    }
    
//    JCheckBoxMenuItem tristateItem = null;
//    void addTristateEntry(JPopupMenu popup) {
//    	tristateItem = new JCheckBoxMenuItem(rb.getString("Tristate"));
//    	tristateItem.setSelected(getTristate());
//        popup.add(tristateItem);
//        tristateItem.addActionListener(new ActionListener(){
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//                setTristate(tristateItem.isSelected());
//            }
//        });
//    }
        
    public JMenuItem newStyleMenuItem(AbstractAction a, int mask) {
        // next two lines needed because JCheckBoxMenuItem(AbstractAction) not in 1.1.8
        JCheckBoxMenuItem c = new JCheckBoxMenuItem((String)a.getValue(AbstractAction.NAME));
        c.addActionListener(a);
        if (log.isDebugEnabled()) log.debug("When creating style item "+((String)a.getValue(AbstractAction.NAME))
                                            +" mask was "+mask+" state was "+getFont().getStyle());
        if ( (mask & getFont().getStyle()) == mask ) c.setSelected(true);
        return c;
    }

    JMenuItem italic = null;
    JMenuItem bold = null;
    ButtonGroup fontButtonGroup = null;
    ButtonGroup colorButtonGroup = null;

    public void setFontStyle(int addStyle, int dropStyle) {
        int styleValue = (getFont().getStyle() & ~dropStyle) | addStyle;
        if (bold != null) bold.setSelected( (styleValue & Font.BOLD) != 0);
        if (italic != null) italic.setSelected( (styleValue & Font.ITALIC) != 0);
        setFont(jmri.util.FontUtil.deriveFont(getFont(),styleValue));

        setSize(getPreferredSize().width, getPreferredSize().height);
    }

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    boolean debug = false;

    public void setPositionable(boolean enabled) { positionable = enabled; }
    public boolean getPositionable() { return positionable; }
    private boolean positionable = true;
    
// The three items below are not used with Layout Editor, but are present for 
//		compatibality with the Positionable interface.
    public void setViewCoordinates(boolean enabled) { viewCoordinates = enabled; }
    public boolean getViewCoordinates() { return viewCoordinates; }
    private boolean viewCoordinates = true;

    public void setEditable(boolean enabled) {editable = enabled;}
    public boolean getEditable() { return editable; }
    private boolean editable = true;

    public void setFixed(boolean enabled) {
        fixed = enabled;
        if (showFixedItem!=null) showFixedItem.setSelected(getFixed());
    }
    public boolean getFixed() { return fixed; }
    private boolean fixed = false;

    public void setControlling(boolean enabled) {controlling = enabled;}
    public boolean getControlling() { return controlling; }
    private boolean controlling = true;

    public void setForceControlOff(boolean set) {
        forceControlOff = set;
        if (disableItem!=null) disableItem.setSelected(getForceControlOff());
    }
    public boolean getForceControlOff() { return forceControlOff; }
    private boolean forceControlOff = false;
    
//    public void setTristate(boolean set) {
//    	tristate = set;
//    }
    
//    public boolean getTristate() { return tristate; }	
//    private boolean tristate = false;

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

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) popup.removeAll();
        fontButtonGroup = null;
        colorButtonGroup = null;
        popup = null;
        italic = null;
        bold = null;
        ours = null;
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
		if (layoutPanel!=null) layoutPanel.removeObject((Object)this);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutPositionableLabel.class.getName());

}
