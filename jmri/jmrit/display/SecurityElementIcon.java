package jmri.jmrit.display;

import jmri.jmrix.loconet.SecurityElement;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * An icon to display a status of a SecurityElement.
 * <p>
 * Unfortunately, this cannot inherit from PositionableLabel, as that displays
 * only text or icon.  So instead we inherit from JPanel and
 * explicitly add the code for Positionable.
 * <P>
 * This also has LocoNet-specific code, so perhaps should be in the
 * jmrix.loconet package.  See also {@link jmri.jmrit.display.configurexml.SecurityElementIconXml}
 * if you move this.
 *
 * @author Bob Jacobsen Copyright 2002
 * @version $Revision: 1.18 $
 */

public class SecurityElementIcon extends JPanel
    implements java.beans.PropertyChangeListener,
               MouseListener, MouseMotionListener, Positionable {

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
        Font f = jmri.util.FontUtil.deriveFont(lrspeed.getFont(), newsize);
        rlspeed.setFont(f);
        lrspeed.setFont(f);
        dir.setFont(f);

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
        setProperToolTip();
    }

    void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
        String name;
        if (element == null) name = "<Not connected>";
        else name = "SE"+element.getNumber();
        return name;
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

    JPopupMenu popup = null;

    /**
     * Pop-up displays the config
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        popup = new JPopupMenu();
        popup.add(new JMenuItem("SE "+element.getNumber()));
        popup.add(new JSeparator(JSeparator.HORIZONTAL));
        popup.add(new AbstractAction("to: "+element.turnout) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set turnout number:",
                                                                "SE "+element.getNumber()+" turnout number",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.turnout=Integer.parseInt(newVal);
                    }
                }
            }
                  );

        popup.add(new AbstractAction("ds: "+element.dsSensor) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set detection section number:",
                                                                "SE "+element.getNumber()+" detection section",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.dsSensor=Integer.parseInt(newVal);
                    }
                }
            }
                  );

        popup.add(new AbstractAction("aux: "+element.auxInput) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set aux input number:",
                                                                "SE "+element.getNumber()+" aux input",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.auxInput=Integer.parseInt(newVal);
                    }
                }
            }
                  );

        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        String attach = "?";
        if (element.attachAleg==SecurityElement.A) attach = "A";
        if (element.attachAleg==SecurityElement.B) attach = "B";
        if (element.attachAleg==SecurityElement.C) attach = "C";
        popup.add(new AbstractAction("A: "+element.attachAnum+":"+attach) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set A attachment number:",
                                                                "SE "+element.getNumber()+" A attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.attachAnum=Integer.parseInt(newVal);
                    }
                    newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set A attachment leg (A,B,C):",
                                                                "SE "+element.getNumber()+" A attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal.equals("A")) element.attachAleg=SecurityElement.A;
                    else if (newVal.equals("B")) element.attachAleg=SecurityElement.B;
                    else if (newVal.equals("C")) element.attachAleg=SecurityElement.C;
                    else if (newVal!=null) log.warn("value needs to be A, B or C: "+newVal);
                }
            }
                  );

        if (element.attachBleg==SecurityElement.A) attach = "A";
        if (element.attachBleg==SecurityElement.B) attach = "B";
        if (element.attachBleg==SecurityElement.C) attach = "C";
        popup.add(new AbstractAction("B: "+element.attachBnum+":"+attach) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set B attachment number:",
                                                                "SE "+element.getNumber()+" B attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.attachBnum=Integer.parseInt(newVal);
                    }
                    newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set B attachment leg (A,B,C):",
                                                                "SE "+element.getNumber()+" B attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal.equals("A")) element.attachBleg=SecurityElement.A;
                    else if (newVal.equals("B")) element.attachBleg=SecurityElement.B;
                    else if (newVal.equals("C")) element.attachBleg=SecurityElement.C;
                    else if (newVal!=null) log.warn("value needs to be A, B or C: "+newVal);
                }
            }
                  );

        if (element.attachCleg==SecurityElement.A) attach = "A";
        if (element.attachCleg==SecurityElement.B) attach = "B";
        if (element.attachCleg==SecurityElement.C) attach = "C";
        popup.add(new AbstractAction("C: "+element.attachCnum+":"+attach) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set C attachment number:",
                                                                "SE "+element.getNumber()+" C attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.attachCnum=Integer.parseInt(newVal);
                    }
                    newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set C attachment leg (A,B,C):",
                                                                "SE "+element.getNumber()+" C attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal.equals("A")) element.attachCleg=SecurityElement.A;
                    else if (newVal.equals("B")) element.attachCleg=SecurityElement.B;
                    else if (newVal.equals("C")) element.attachCleg=SecurityElement.C;
                    else if (newVal!=null) log.warn("value needs to be A, B or C: "+newVal);
                }
            }
                  );

        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        popup.add(new AbstractAction("maxAB: "+element.maxSpeedAB) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set max A->B speed:",
                                                                "Max AB speed in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.maxSpeedAB=Integer.parseInt(newVal);
                    }
                }
            }
                  );
        popup.add(new AbstractAction("maxBA: "+element.maxSpeedBA) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set max B->A speed:",
                                                                "Max BA speed in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.maxSpeedBA=Integer.parseInt(newVal);
                    }
                }
            }
                  );
        popup.add(new AbstractAction("maxAC: "+element.maxSpeedAC) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set max A->C speed:",
                                                                "Max AC speed in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.maxSpeedAC=Integer.parseInt(newVal);
                    }
                }
            }
                  );
        popup.add(new AbstractAction("maxCA: "+element.maxSpeedCA) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set max C->A speed:",
                                                                "Max CA speed in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.maxSpeedCA=Integer.parseInt(newVal);
                    }
                }
            }
                  );

        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        popup.add(new AbstractAction("brakeAB: "+element.maxBrakingAB) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set A->B braking:",
                                                                "AB braking in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.maxBrakingAB=Integer.parseInt(newVal);
                    }
                }
            }
                  );
        popup.add(new AbstractAction("brakeBA: "+element.maxBrakingBA) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set B->A braking:",
                                                                "BA braking in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.maxBrakingBA=Integer.parseInt(newVal);
                    }
                }
            }
                  );
        popup.add(new AbstractAction("brakeAC: "+element.maxBrakingAC) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set A->C braking:",
                                                                "AC braking in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.maxBrakingAC=Integer.parseInt(newVal);
                    }
                }
            }
                  );
        popup.add(new AbstractAction("brakeCA: "+element.maxBrakingCA) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Set C->A braking:",
                                                                "CA braking in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.maxBrakingCA=Integer.parseInt(newVal);
                    }
                }
            }
                  );

        popup.add(new AbstractAction("onAXreserve: "+element.onAXReservation) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "On AX reservation (0 none, 1 stop opposite, 2 stop unreserved):",
                                                                "On AX reservation in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.onAXReservation=Integer.parseInt(newVal);
                    }
                }
            }
                  );
        popup.add(new AbstractAction("onXAreserve: "+element.onAXReservation) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "On XA reservation (0 none, 1 stop opposite, 2 stop unreserved):",
                                                                "On XA reservation in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.onXAReservation=Integer.parseInt(newVal);
                    }
                }
            }
                  );

        popup.add(new AbstractAction("makeAreserve: "+element.makeAReservation) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Make A reservation (0 no, 1 yes):",
                                                                "Make A reservation in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.makeAReservation=(Integer.parseInt(newVal)==1);
                    }
                }
            }
                  );
        popup.add(new AbstractAction("makeBreserve: "+element.makeBReservation) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Make B reservation (0 no, 1 yes):",
                                                                "Make B reservation in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.makeBReservation=(Integer.parseInt(newVal)==1);
                    }
                }
            }
                  );
        popup.add(new AbstractAction("makeCreserve: "+element.makeCReservation) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(popup,
                                                                "Make C reservation (0 no, 1 yes):",
                                                                "Make C reservation in SE "+element.getNumber(),
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.makeCReservation=(Integer.parseInt(newVal)==1);
                    }
                }
            }
                  );

        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        // show some debugging state
        popup.add(new JLabel(" Occupancies: "+element.showOccupancy()));
        popup.add(new JLabel(" Speed input: "+element.showInputSpeeds()));
        popup.add(new JLabel(" Reservations: "+element.showReservations()));

        // and include remove at the bottom
        popup.add(new JSeparator(JSeparator.HORIZONTAL));
        popup.add(new AbstractAction("Remove") {
            public void actionPerformed(ActionEvent e) {
                remove();
                dispose();
            }
        });

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) popup.removeAll();
        popup = null;
        rlspeed = null;
        lrspeed = null;
        dir = null;
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

    public void setPositionable(boolean enabled) {positionable = enabled;}
    public boolean getPositionable() { return positionable; }
    private boolean positionable = true;

    public void setEditable(boolean enabled) {editable = enabled;}
    public boolean getEditable() { return editable; }
    private boolean editable = true;

    public void setControlling(boolean enabled) {controlling = enabled;}
    public boolean getControlling() { return controlling; }
    private boolean controlling = true;

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }
}
