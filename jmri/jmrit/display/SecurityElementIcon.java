package jmri.jmrit.display;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.*;
import jmri.jmrix.loconet.SecurityElement;

/**
 * SecurityElementIcon provides a small icon to display a status of a SecurityElement.
 * <p>
 * Unfortunately, this cannot inherit from PositionableLabel, as it displays
 * only text or icon.  So instead we inherit from JPanel and
 * explicitly add the code for Positionable
 *
 * @author Bob Jacobsen Copyright 2002
 * @version $Revision: 1.1 $
 */

public class SecurityElementIcon extends JPanel
            implements java.beans.PropertyChangeListener,
            MouseListener, MouseMotionListener {

    JLabel rlspeed;  // speed from right to left, on the top
    JLabel dir;      // direction bits
    JLabel lrspeed;  // speed from left to right, on the bottom

    boolean debug;

    /**
     * The standard display assumes that AX is left to right (rightbound), and
     * AX is right to left. Set this false if the reverse is correct.
     */
    boolean rightboundIsAX = true;
    public void setRightBoundAX(boolean mVal) { rightboundIsAX = mVal; }
    public boolean getRightBoundAX() { return rightboundIsAX; }

    public SecurityElementIcon() {
        // super ctor call to make sure this is an icon label

        super();

        debug = log.isDebugEnabled();

        // show the starting state
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(rlspeed = new JLabel("????"));
        add(dir =     new JLabel("<??>"));
        add(lrspeed = new JLabel("????"));

        // set the labels to a smaller font
        float newsize = lrspeed.getFont().getSize() * 0.8f;
        rlspeed.setFont(lrspeed.getFont().deriveFont(newsize));
        lrspeed.setFont(lrspeed.getFont().deriveFont(newsize));
        dir.setFont(lrspeed.getFont().deriveFont(newsize));

        // and make movable
        connect();
    }

    // the associated SecurityElement object
    SecurityElement element = null;

    public SecurityElement getSecurityElement() { return element; }

    /**
     * Attached a numbered element to this display item
     * @param name Used as a number to lookup the sensor object
     */
    public void setSecurityElement(String name) {
        element = jmri.jmrix.loconet.LnSecurityElementManager.instance()
                        .getSecurityElement(name);
        element.addPropertyChangeListener(this);
    }

	// update as state of turnout changes
	public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (rightboundIsAX) {
            lrspeed.setText(String.valueOf(element.currentSpeedAX));
            rlspeed.setText(String.valueOf(element.currentSpeedXA));
        } else {
            rlspeed.setText(String.valueOf(element.currentSpeedAX));
            lrspeed.setText(String.valueOf(element.currentSpeedXA));
        }

        if (getRightBoundAX()) {
            String direction;
            if ((element.currentDirection&SecurityElement.XA)!=0)
                direction = "<-";
            else
                direction =" -";
            if ((element.currentDirection&SecurityElement.AX)!=0)
                dir.setText(direction+">");
            else
                dir.setText(direction+" ");
        } else { // leftbound AX
            String direction;
            if ((element.currentDirection&SecurityElement.AX)!=0)
                direction = "<-";
            else
                direction =" -";
            if ((element.currentDirection&SecurityElement.XA)!=0)
                dir.setText(direction+">");
            else
                dir.setText(direction+" ");
        }
	}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SecurityElementIcon.class.getName());

    // below here is copied from PositionableLabel
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
        if (debug && e.isPopupTrigger()) log.debug("Released was pop up trigger");
    }
    public void mouseClicked(MouseEvent e) {
        if (debug) log.debug("Clicked: "+where(e));
        if (debug && e.isPopupTrigger()) log.debug("Clicked was pop up trigger");
        if (debug && e.isMetaDown()) log.debug("meta down");
        if (debug && e.isAltDown()) log.debug(" alt down");
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
    /**
     * Pop-up displays the config
     */
    protected void showPopUp(MouseEvent e) {
        if (popup==null) {
            popup = new JPopupMenu();
            popup.add(new JMenuItem("SE "+element.getNumber()));
            String mode = "?? "+element.mLogic;
            if (element.mLogic==SecurityElement.ABS) mode="ABS";
            else if (element.mLogic==SecurityElement.APB) mode="APB";
            else if (element.mLogic==SecurityElement.HEADBLOCK) mode="headblock";
            popup.add(new JMenuItem("mode: "+mode));
            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            popup.add(new JMenuItem("to: "+element.turnout));
            popup.add(new JMenuItem("ds: "+element.dsSensor));
            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            popup.add(new JMenuItem("A: "+element.attachAnum+":"+element.attachAleg));
            popup.add(new JMenuItem("B: "+element.attachBnum+":"+element.attachBleg));
            popup.add(new JMenuItem("C: "+element.attachCnum+":"+element.attachCleg));
            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            popup.add(new JMenuItem("maxAB: "+element.maxSpeedAB));
            popup.add(new JMenuItem("maxBA: "+element.maxSpeedBA));
            popup.add(new JMenuItem("maxAC: "+element.maxSpeedAC));
            popup.add(new JMenuItem("maxCA: "+element.maxSpeedCA));
            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            popup.add(new JMenuItem("brakeAB:"+element.maxBrakingAB));
            popup.add(new JMenuItem("brakeBA:"+element.maxBrakingBA));
            popup.add(new JMenuItem("brakeAC:"+element.maxBrakingAC));
            popup.add(new JMenuItem("brakeCA:"+element.maxBrakingCA));
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

}