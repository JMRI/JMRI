package jmri.jmrit.display;

import javax.swing.*;
import java.awt.event.*;
import jmri.jmrit.catalog.NamedIcon;
import jmri.*;

/**
 * TurnoutIcon provides a small icon to display a status of a turnout.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <P>
 * A click on the icon will command a state change. Specifically, it
 * will set the CommandedState to the opposite (THROWN vs CLOSED) of
 * the current KnownState.
 *
 * @author Bob Jacobsen
 * @version $Revision: 1.11 $
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
    public void setTurnout(String pUserName, String pSystemName) {
        turnout = InstanceManager.turnoutManagerInstance().
            newTurnout(pUserName, pSystemName);
        displayState(turnoutState());
        turnout.addPropertyChangeListener(this);
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

    JPopupMenu popup = null;
    JLabel ours = this;  // used for rotating icons

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
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    void displayState(int state) {
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
        try {
            if (turnout.getKnownState()==jmri.Turnout.CLOSED)
                turnout.setCommandedState(jmri.Turnout.THROWN);
            else
                turnout.setCommandedState(jmri.Turnout.CLOSED);
        } catch (jmri.JmriException reason) {
            log.warn("Exception changing turnout: "+reason);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIcon.class.getName());
}