// PositionableJComponent.java

package jmri.jmrit.display;

import javax.swing.JComponent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import jmri.util.JmriJFrame;

/**
 * <p> </p>
 *
 * @author  Howard G. Penny copyright (C) 2005
 * @version $Revision: 1.1 $
 */
abstract class PositionableJComponent extends JComponent
                        implements MouseMotionListener, MouseListener,
                                    Positionable {
    JmriJFrame _parentFrame;

    public PositionableJComponent(JmriJFrame parentFrame) {
        _parentFrame = parentFrame;
        debug = log.isDebugEnabled();
        setProperToolTip();
        connect();
    }

    /**
     * For over-riding in the using classes:
     */
    public void setProperToolTip() { }

    /**
     * Connect listeners
     */
    void connect() {
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    void disconnect() {
        removeMouseMotionListener(this);
        removeMouseListener(this);
    }

    private Integer displayLevel;
    public void setDisplayLevel(Integer l) { displayLevel = l; }
    public void setDisplayLevel(int l) { setDisplayLevel(new Integer(l)); }
    public Integer getDisplayLevel() { return displayLevel; }

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
            if (!getPositionable()) return;
            // update object postion by how far dragged
            int xObj = getX()+(e.getX()-xClick);
            int yObj = getY()+(e.getY()-yClick);
            this.setLocation(xObj, yObj);
            // and show!
            this.repaint();
        }
    }

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    protected JPopupMenu popup = null;
    protected JComponent ours;

    /**
     * For over-riding in the using classes: add item specific menu choices
     */
    protected void addToPopup() { }

    private void showPopUp(MouseEvent e) {
//        if (!getPopupEnabled()) return;  // We need to distinguish between popup and editable
        if (!getEditable()) return;
        if (popup == null) {
            popup = new JPopupMenu();
            popup.add(lock = newLockMenuItem(new AbstractAction("Lock Position") {
                public void actionPerformed(ActionEvent e) {
                    if (lock.isSelected()) {
                        setPositionable(false);
                    } else {
                        setPositionable(true);
                    }
                }
            }));
            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });
            this.addToPopup();
        }
        if (getPositionable()) {
            lock.setSelected(false);
        } else {
            lock.setSelected(true);
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    public JMenuItem newLockMenuItem(AbstractAction a) {
      JCheckBoxMenuItem k = new JCheckBoxMenuItem((String)a.getValue(a.NAME));
      k.addActionListener(a);
      if (!getPositionable()) k.setSelected(true);
      return k;
    }

    JMenuItem italic = null;
    JMenuItem bold = null;
    JMenuItem lock = null;
    boolean debug = false;

    public void setPositionable(boolean enabled) {positionable = enabled;}
    public boolean getPositionable() { return positionable; }
    private boolean positionable = true;

    public void setEditable(boolean enabled) {editable = enabled;}
    public boolean getEditable() { return editable; }
    private boolean editable = true;

    public void setControlling(boolean enabled) {controlling = enabled;}
    public boolean getControlling() { return controlling; }
    private boolean controlling = true;

//    public void setPopupEnabled(boolean enabled) {popupEnabled = enabled;}
//    public boolean getPopupEnabled() { return popupEnabled; }
//    private boolean popupEnabled = true;

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    public void dispose() {
        if (popup != null) popup.removeAll();
        popup = null;
        ours = null;
        disconnect();
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
        // cleanup before "this" is removed
        cleanup();
        Point p = this.getLocation();
        int w = this.getWidth();
        int h = this.getHeight();
        Container parent = this.getParent();
        parent.remove(this);
        // force redisplay
        parent.validate();
        parent.repaint((int)p.getX(),(int)p.getY(),w,h);

        // remove from persistance by flagging inactive
        active = false;
    }

    /**
     * To be overridden if any special work needs to be done
     */
    void cleanup() {}

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PositionableJComponent.class.getName());
}
