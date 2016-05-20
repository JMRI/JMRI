package jmri.jmrit.display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.SecurityElement;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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
 * @version $Revision$
 * @deprecated 2.13.5, Does not work with the multi-connection correctly, believe not 
 * to work correctly before hand and that the feature is not used.
 */
 
@Deprecated
public class SecurityElementIcon extends PositionableJPanel
    implements java.beans.PropertyChangeListener, Positionable {

    JLabel rlspeed;  // speed from right to left, on the top
    JLabel dir;      // direction bits
    JLabel lrspeed;  // speed from left to right, on the bottom

    /**
     * The standard display assumes that AX is left to right (rightbound), and
     * AX is right to left. Set this false if the reverse is correct.
     */
    boolean rightboundIsAX = true;
    public void setRightBoundAX(boolean mVal) { rightboundIsAX = mVal; }
    public boolean getRightBoundAX() { return rightboundIsAX; }

    public SecurityElementIcon(Editor editor) {
        // super ctor call to make sure this is an icon label

        super(editor);

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
        setPopupUtility(null);
    }

    // the associated SecurityElement object
    SecurityElement element = null;

    public Positionable deepClone() {
        SecurityElementIcon pos = new SecurityElementIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        SecurityElementIcon pos = (SecurityElementIcon)p;
        pos.setSecurityElement(Integer.toString(element.getNumber()));
        return super.finishClone(pos);
    }

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

    public String getNameString() {
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

    static Logger log = LoggerFactory.getLogger(SecurityElementIcon.class.getName());

    /**
     * Pop-up displays the config
     */
    public boolean showPopUp(JPopupMenu popup) {

        popup.add(new JMenuItem("SE "+element.getNumber()));
        popup.add(new JSeparator(JSeparator.HORIZONTAL));
        popup.add(new AbstractAction("to: "+element.turnout) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
                                                                "Set A attachment number:",
                                                                "SE "+element.getNumber()+" A attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.attachAnum=Integer.parseInt(newVal);
                    }
                    newVal =
                        javax.swing.JOptionPane.showInputDialog(null,
                                                                "Set A attachment leg (A,B,C):",
                                                                "SE "+element.getNumber()+" A attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal.equals("A")) element.attachAleg=SecurityElement.A;
                    else if (newVal.equals("B")) element.attachAleg=SecurityElement.B;
                    else if (newVal.equals("C")) element.attachAleg=SecurityElement.C;
                    else log.warn("value needs to be A, B or C: "+newVal);
                }
            }
                  );

        if (element.attachBleg==SecurityElement.A) attach = "A";
        if (element.attachBleg==SecurityElement.B) attach = "B";
        if (element.attachBleg==SecurityElement.C) attach = "C";
        popup.add(new AbstractAction("B: "+element.attachBnum+":"+attach) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(null,
                                                                "Set B attachment number:",
                                                                "SE "+element.getNumber()+" B attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.attachBnum=Integer.parseInt(newVal);
                    }
                    newVal =
                        javax.swing.JOptionPane.showInputDialog(null,
                                                                "Set B attachment leg (A,B,C):",
                                                                "SE "+element.getNumber()+" B attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal.equals("A")) element.attachBleg=SecurityElement.A;
                    else if (newVal.equals("B")) element.attachBleg=SecurityElement.B;
                    else if (newVal.equals("C")) element.attachBleg=SecurityElement.C;
                    else log.warn("value needs to be A, B or C: "+newVal);
                }
            }
                  );

        if (element.attachCleg==SecurityElement.A) attach = "A";
        if (element.attachCleg==SecurityElement.B) attach = "B";
        if (element.attachCleg==SecurityElement.C) attach = "C";
        popup.add(new AbstractAction("C: "+element.attachCnum+":"+attach) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(null,
                                                                "Set C attachment number:",
                                                                "SE "+element.getNumber()+" C attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal!=null) {
                        element.attachCnum=Integer.parseInt(newVal);
                    }
                    newVal =
                        javax.swing.JOptionPane.showInputDialog(null,
                                                                "Set C attachment leg (A,B,C):",
                                                                "SE "+element.getNumber()+" C attachment",
                                                                javax.swing.JOptionPane.OK_CANCEL_OPTION);
                    if (newVal.equals("A")) element.attachCleg=SecurityElement.A;
                    else if (newVal.equals("B")) element.attachCleg=SecurityElement.B;
                    else if (newVal.equals("C")) element.attachCleg=SecurityElement.C;
                    else log.warn("value needs to be A, B or C: "+newVal);
                }
            }
                  );

        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        popup.add(new AbstractAction("maxAB: "+element.maxSpeedAB) {
                public void actionPerformed(ActionEvent e) {
                    String newVal =
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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
                        javax.swing.JOptionPane.showInputDialog(null,
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

        return true;
    }

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    public void dispose() {
        rlspeed = null;
        lrspeed = null;
        dir = null;
    }

    /**
     * Removes this object from display and persistance
     */
    public void remove() {
        Container parent = this.getParent();
        parent.remove(this);
        // force redisplay
        parent.validate();

        // remove from persistance
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
}
