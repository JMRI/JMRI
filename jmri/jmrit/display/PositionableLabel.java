package jmri.jmrit.display;

import javax.swing.*;
import java.awt.event.*;

import jmri.*;
import jmri.jmrit.catalog.NamedIcon;

/**
 * <p>Title: PositionableLabel is a JLabel that can be dragged around the
 * inside of the enclosing Container using a right-drag.</p>
 * <p>Description: </p>
 * <p>Copyright: Bob Jacobsen Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.8 $
 */

public class PositionableLabel extends JLabel
                        implements MouseMotionListener, MouseListener,
                                    Positionable {

    public PositionableLabel(String s) {
        super(s);
        text = true;
        debug = log.isDebugEnabled();
        setToolTipText("Alt-click to see menu, drag with meta key to move");
        connect();
    }
    public PositionableLabel(NamedIcon s) {
        super(s);
        icon = true;
        namedIcon = s;
        debug = log.isDebugEnabled();
        setToolTipText("Alt-click to see menu, drag with meta key to move");
        connect();
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

    // cursor location reference for this move (relative to object)
    int xClick = 0;
    int yClick = 0;

    public void mousePressed(MouseEvent e) {
        // remember where we are
        xClick = e.getX();
        yClick = e.getY();
        if (debug) log.debug("Pressed: "+where(e));
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (debug) log.debug("Release: "+where(e));
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
        if (debug) log.debug("Exited:  "+where(e));
    }
    public void mouseEntered(MouseEvent e) {
        if (debug) log.debug("Entered: "+where(e));
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
        if (icon) {
            popup = new JPopupMenu();
            popup.add(new AbstractAction("Rotate") {
                public void actionPerformed(ActionEvent e) {
                    namedIcon.setRotation(namedIcon.getRotation()+1, ours);
                    setIcon(namedIcon);
                    ours.setSize(ours.getPreferredSize().width, ours.getPreferredSize().height);
                }
            }
            );
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    boolean debug = false;

    public void setPositionable(boolean enabled) {positionable = enabled;}
    public boolean getPositionable() { return positionable; }
    private boolean positionable = true;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PositionableLabel.class.getName());

}