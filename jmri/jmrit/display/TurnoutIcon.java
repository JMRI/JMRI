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
 * @version $Revision: 1.16 $
 */

public class TurnoutIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public TurnoutIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif",
                            "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif"));
        displayState(turnoutState());
    }

    // what to display - at least one should be true!
    boolean showText = false;
    boolean showIcon = true;

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
            displayState(turnoutState());
            turnout.addPropertyChangeListener(this);
        } else {
            log.error("No turnout manager, can't register for updates. Is there a layout connection?");
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
    public void setClosedIcon(NamedIcon i) { closed = i; displayState(turnoutState()); }

    public NamedIcon getThrownIcon() { return thrown; }
    public void setThrownIcon(NamedIcon i) { thrown = i; displayState(turnoutState()); }

    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) { inconsistent = i; displayState(turnoutState()); }

    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) { unknown = i; displayState(turnoutState()); }

    public int getHeight() {
        return Math.max(
                        Math.max(closed.getIconHeight(), thrown.getIconHeight()),
                        Math.max(inconsistent.getIconHeight(), unknown.getIconHeight())
                        );
    }

    public int getWidth() {
        return Math.max(
                        Math.max(closed.getIconWidth(), thrown.getIconWidth()),
                        Math.max(inconsistent.getIconWidth(), unknown.getIconWidth())
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

    /**
     * Pop-up displays the turnout name, allows you to rotate the icons
     */
    protected void showPopUp(MouseEvent e) {
        if (popup==null) {
            popup = new JPopupMenu();
            String name;
            if (turnout.getUserName()!=null)
                name = turnout.getUserName()+" ("+turnout.getSystemName()+")";
            else
                name = turnout.getSystemName();
            popup.add(new JMenuItem(name));
            if (showIcon) popup.add(new AbstractAction("Rotate") {
                    public void actionPerformed(ActionEvent e) {
                        closed.setRotation(closed.getRotation()+1, ours);
                        thrown.setRotation(thrown.getRotation()+1, ours);
                        unknown.setRotation(unknown.getRotation()+1, ours);
                        inconsistent.setRotation(inconsistent.getRotation()+1, ours);
                        displayState(turnoutState());
                        ours.setSize(ours.getPreferredSize().width, ours.getPreferredSize().height);
                    }
                });
        }

        popup.add(new AbstractAction("Remove") {
            public void actionPerformed(ActionEvent e) {
                remove();
                dispose();
            }
        });

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    void displayState(int state) {
        log.debug("displayState "+state);
        switch (state) {
        case Turnout.UNKNOWN:
            if (showText) super.setText("<unknown>");
            if (showIcon) super.setIcon(unknown);
            return;
        case Turnout.CLOSED:
            if (showText) super.setText("Closed");
            if (showIcon) super.setIcon(closed);
            return;
        case Turnout.THROWN:
            if (showText) super.setText("Thrown");
            if (showIcon) super.setIcon(thrown);
            return;
        default:
            if (showText) super.setText("<inconsistent>");
            if (showIcon) super.setIcon(inconsistent);
            return;
        }
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
