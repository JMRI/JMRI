package jmri.jmrit.display;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.Icon;

import jmri.*;

/**
 * <p>Title: TurnoutIcon provides a small icon to display a status of a turnout.</p>
 * <p>Description: </p>
 * <p>Copyright: Bob Jacobsen Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.4 $
 */

public class TurnoutIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public TurnoutIcon() {
        // super ctor call to make sure this is an icon label
        super(new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/X-red.gif")));
        displayState(turnoutState());
    }

    // what to display - at least one should be true!
    boolean showText = false;
    boolean showIcon = true;

    // the associated Turnout object
    Turnout turnout = null;

    /**
     * Attached a named turnout to this display item
     * @param name Used as a user name to lookup the turnout object
     */
    public void setTurnout(String name) {
        turnout = InstanceManager.turnoutManagerInstance().
            newTurnout(null,name);
        turnout.addPropertyChangeListener(this);
    }

    // display icons
    Icon closed = new ImageIcon(ClassLoader.getSystemResource("resources/TrackSegments/GriffenSet/Turnouts44x40/RHNORMUP.gif"));
    Icon thrown = new ImageIcon(ClassLoader.getSystemResource("resources/TrackSegments/GriffenSet/Turnouts44x40/RHREVUP.gif"));
    Icon inconsistent = new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/X-red.gif"));
    Icon unknown = new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/Question-black.gif"));

    public Icon getClosedIcon() { return closed; }
    public void setClosedIcon(Icon i) { closed = i; displayState(turnoutState()); }

    public Icon getThrownIcon() { return thrown; }
    public void setThrownIcon(Icon i) { thrown = i; displayState(turnoutState()); }

    public Icon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(Icon i) { inconsistent = i; displayState(turnoutState()); }

    public Icon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(Icon i) { unknown = i; displayState(turnoutState()); }

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
        if (turnout != null) return turnout.getCommandedState();
        else return Turnout.UNKNOWN;
    }

	// update icon as state of turnout changes
	public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e);
		if (e.getPropertyName().equals("CommandedState")) {
            int now = ((Integer) e.getNewValue()).intValue();
             displayState(now);
		}
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIcon.class.getName());
}