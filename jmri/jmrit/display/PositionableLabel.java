package jmri.jmrit.display;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import jmri.*;
import jmri.jmrit.catalog.NamedIcon;

/**
 * PositionableLabel is a JLabel that can be dragged around the
 * inside of the enclosing Container using a right-drag.
 * @author Bob Jacobsen Copyright (c) 2002
 * @version $Revision: 1.13 $
 */

public class PositionableLabel extends JLabel
                        implements MouseMotionListener, MouseListener,
                                    Positionable {

    public PositionableLabel(String s) {
        super(s);
        text = true;
        debug = log.isDebugEnabled();
        setProperToolTip();
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
        setToolTipText("Alt-click to see menu, drag with meta key to move");
    }

    public boolean isIcon() { return icon; }
    boolean icon = false;
    public boolean isText() { return text; }
    boolean text = false;

    NamedIcon namedIcon = null;

    /**
     * Connect listeners
     */
    void connect() {
        addMouseMotionListener(this);
        addMouseListener(this);
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

    // cursor location reference for this move (relative to object)
    int xClick = 0;
    int yClick = 0;

    public void mousePressed(MouseEvent e) {
        // remember where we are
        xClick = e.getX();
        yClick = e.getY();
        // if (debug) log.debug("Pressed: "+where(e));
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        // if (debug) log.debug("Release: "+where(e));
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (debug) log.debug("Clicked: "+where(e));
        if (debug && e.isMetaDown()) log.debug("meta down");
        if (debug && e.isAltDown()) log.debug(" alt down");
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
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
        //if (debug) log.debug("Moved:   "+where(e));
    }
    public void mouseDragged(MouseEvent e) {
        if (e.isMetaDown()) {
            //if (debug) log.debug("Dragged: "+where(e));
            // update object postion by how far dragged
            int xObj = getX()+(e.getX()-xClick);
            int yObj = getY()+(e.getY()-yClick);
            this.setLocation(xObj, yObj);
            // and show!
            this.repaint();
        }
    }

    JPopupMenu popup = null;
    JLabel ours;
    /**
     * For over-riding in the using classes: only provides icon rotation
     */
    protected void showPopUp(MouseEvent e) {
        ours = this;
        if (icon && popup == null ) {
            popup = new JPopupMenu();
            popup.add(new AbstractAction("Rotate") {
                public void actionPerformed(ActionEvent e) {
                    namedIcon.setRotation(namedIcon.getRotation()+1, ours);
                    setIcon(namedIcon);
                }
            });
            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });
        } else if (text && popup == null) {
            popup = new JPopupMenu();
            JMenu sizeMenu = new JMenu("Font size");
            fontButtonGroup = new ButtonGroup();
            addFontMenuEntry(sizeMenu, 6);
            addFontMenuEntry(sizeMenu, 8);
            addFontMenuEntry(sizeMenu, 10);
            addFontMenuEntry(sizeMenu, 12);
            addFontMenuEntry(sizeMenu, 14);
            addFontMenuEntry(sizeMenu, 16);
            addFontMenuEntry(sizeMenu, 20);
            addFontMenuEntry(sizeMenu, 24);
            addFontMenuEntry(sizeMenu, 28);
            addFontMenuEntry(sizeMenu, 32);
            addFontMenuEntry(sizeMenu, 36);
            popup.add(sizeMenu);

            JMenu styleMenu = new JMenu("Font style");
            styleMenu.add(italic = newStyleMenuItem(new AbstractAction("Italic") {
                public void actionPerformed(ActionEvent e) {
                    if (bold.isSelected()) setFontStyle(Font.ITALIC, 0);
                    else setFontStyle(0, Font.ITALIC);
                }
              }, Font.ITALIC));

            styleMenu.add(bold = newStyleMenuItem(new AbstractAction("Bold") {
                public void actionPerformed(ActionEvent e) {
                    if (bold.isSelected()) setFontStyle(Font.BOLD, 0);
                    else setFontStyle(0, Font.BOLD);
                }
              }, Font.BOLD));
            popup.add(styleMenu);

            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        } else log.warn("showPopUp when neither text nor icon true");
        // show the result
        if (popup != null) popup.show(e.getComponent(), e.getX(), e.getY());
    }

    void addFontMenuEntry(JMenu menu, final int size) {
        AbstractAction a = new AbstractAction(""+size) {
            final float desiredSize = size+0.f;
            public void actionPerformed(ActionEvent e) { setFontSize(desiredSize); }
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(a);
        fontButtonGroup.add(r);
        if (getFont().getSize() == size) r.setSelected(true);
        else r.setSelected(false);
        menu.add(r);
    }

    public void setFontSize(float newSize) {
        setFont(getFont().deriveFont(newSize));
        setSize(getPreferredSize().width, getPreferredSize().height);
    }

    public JMenuItem newStyleMenuItem(AbstractAction a, int mask) {
        JCheckBoxMenuItem c = new JCheckBoxMenuItem(a);
        if ( (mask & getFont().getStyle()) == mask ) c.setSelected(true);
        return c;
    }

    JMenuItem italic = null;
    JMenuItem bold = null;
    ButtonGroup fontButtonGroup = null;

    public void setFontStyle(int addStyle, int dropStyle) {
        int styleValue = (getFont().getStyle() & ~dropStyle) | addStyle;
        if (bold != null) bold.setSelected( (styleValue & Font.BOLD) != 0);
        if (italic != null) italic.setSelected( (styleValue & Font.ITALIC) != 0);
        setFont(getFont().deriveFont(styleValue));
        setSize(getPreferredSize().width, getPreferredSize().height);
    }

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    boolean debug = false;

    public void setPositionable(boolean enabled) {positionable = enabled;}
    public boolean getPositionable() { return positionable; }
    private boolean positionable = true;

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) popup.removeAll();
        fontButtonGroup =null;
        popup = null;
        italic = null;
        bold = null;
        ours = null;
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
        Container parent = this.getParent();
        parent.remove(this);
        // force redisplay
        parent.validate();

        // remove from persistance
        active = false;
    }

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PositionableLabel.class.getName());

}