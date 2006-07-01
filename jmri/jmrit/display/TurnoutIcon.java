package jmri.jmrit.display;

import java.awt.event.*;

import javax.swing.*;

import jmri.*;
import jmri.jmrit.catalog.*;

/**
 * TurnoutIcon provides a small icon to display a status of a turnout.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <P>
 * A click on the icon will command a state change. Specifically, it
 * will set the CommandedState to the opposite (THROWN vs CLOSED) of
 * the current KnownState.
 *<P>
 * The default icons are for a left-handed turnout, facing point
 * for east-bound traffic.
 * @author Bob Jacobsen
 * @version $Revision: 1.22 $
 */

public class TurnoutIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public TurnoutIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif",
                            "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif"));
        displayState(turnoutState());
        icon = true;
        text = false;
    }

    // the associated Turnout object
    Turnout turnout = null;

    /**
     * Attached a named turnout to this display item
     * @param pUserName Used as a user name to lookup the turnout object
     * @param pSystemName Used as a system name to lookup the turnout object
     */
    public void setTurnout(String pSystemName, String pUserName) {
        if (InstanceManager.turnoutManagerInstance()!=null) {
            turnout = InstanceManager.turnoutManagerInstance().
                newTurnout(pSystemName, pUserName);
            if (turnout != null) {
                displayState(turnoutState());
                turnout.addPropertyChangeListener(this);
                setProperToolTip();
            } else {
                log.error("Turnout '"+pSystemName+"' not available, icon won't see changes");
            }
        } else {
            log.error("No TurnoutManager for this protocol, icon won't see changes");
        }
    }

    public Turnout getTurnout() { return turnout; }

    // display icons
    NamedIcon closed = new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif",
                                     "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif");
    NamedIcon thrown = new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif",
                                     "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif");
    NamedIcon inconsistent = new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif",
                                           "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif");
    NamedIcon unknown = new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif",
                                      "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif");

    public NamedIcon getClosedIcon() { return closed; }
    public void setClosedIcon(NamedIcon i) {
        closed = i;
        displayState(turnoutState());
    }

    public NamedIcon getThrownIcon() { return thrown; }
    public void setThrownIcon(NamedIcon i) {
        thrown = i;
        displayState(turnoutState());
    }

    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
        displayState(turnoutState());
    }

    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
        displayState(turnoutState());
    }

    protected int maxHeight() {
        return Math.max(
                Math.max( (closed!=null) ? closed.getIconHeight() : 0,
                        (thrown!=null) ? thrown.getIconHeight() : 0),
                Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                        (inconsistent!=null) ? inconsistent.getIconHeight() : 0)
            );
    }
    protected int maxWidth() {
        return Math.max(
                Math.max((closed!=null) ? closed.getIconWidth() : 0,
                        (thrown!=null) ? thrown.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
            );
    }

    /**
     * Get current state of attached turnout
     * @return A state variable from a Turnout, e.g. Turnout.CLOSED
     */
    int turnoutState() {
        if (turnout != null) return turnout.getKnownState();
        else return Turnout.UNKNOWN;
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "
                                            +e.getPropertyName()
                                            +" is now "+e.getNewValue());
	if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            displayState(now);
        }
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
        String name;
        if (turnout == null) name = "<Not connected>";
        else if (turnout.getUserName()!=null)
            name = turnout.getUserName()+" ("+turnout.getSystemName()+")";
        else
            name = turnout.getSystemName();
        return name;
    }


    /**
     * Pop-up displays the turnout name, allows you to rotate the icons
     */
    protected void showPopUp(MouseEvent e) {
        ours = this;
        if (popup==null) {
            popup = new JPopupMenu();
            popup.add(new JMenuItem(getNameString()));
            if (icon) popup.add(new AbstractAction("Rotate") {
                    public void actionPerformed(ActionEvent e) {
                        closed.setRotation(closed.getRotation()+1, ours);
                        thrown.setRotation(thrown.getRotation()+1, ours);
                        unknown.setRotation(unknown.getRotation()+1, ours);
                        inconsistent.setRotation(inconsistent.getRotation()+1, ours);
                        displayState(turnoutState());
                    }
                });

            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        } // end creation of pop-up menu

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    void displayState(int state) {
        log.debug("displayState "+state);
        updateSize();
        switch (state) {
        case Turnout.UNKNOWN:
            if (text) super.setText("<unknown>");
            if (icon) super.setIcon(unknown);
            break;
        case Turnout.CLOSED:
            if (text) super.setText("Closed");
            if (icon) super.setIcon(closed);
            break;
        case Turnout.THROWN:
            if (text) super.setText("Thrown");
            if (icon) super.setIcon(thrown);
            break;
        default:
            if (text) super.setText("<inconsistent>");
            if (icon) super.setIcon(inconsistent);
            break;
        }

        return;
    }

    /**
     * Throw the turnout when the icon is clicked
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (turnout==null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        if (turnout.getKnownState()==jmri.Turnout.CLOSED)
            turnout.setCommandedState(jmri.Turnout.THROWN);
        else
            turnout.setCommandedState(jmri.Turnout.CLOSED);
    }

    public void dispose() {
        turnout.removePropertyChangeListener(this);
        turnout = null;

        closed = null;
        thrown = null;
        inconsistent = null;
        unknown = null;

        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIcon.class.getName());
}
