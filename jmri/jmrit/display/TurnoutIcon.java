package jmri.jmrit.display;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import jmri.Turnout;

/**
 * <p>Title: TurnoutIcon provides a small icon to display a status of a turnout.</p>
 * <p>Description: </p>
 * <p>Copyright: Bob Jacobsen Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version 1.0
 */

public class TurnoutIcon extends JLabel implements java.beans.PropertyChangeListener {

    public TurnoutIcon() {
        super(new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/X-red.gif")));
    }

    ImageIcon closed = new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/Checkmark-green.gif"));
    ImageIcon thrown = new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/Twiddle-yellow.gif"));
    ImageIcon inconsistent = new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/X-red.gif"));
    ImageIcon unknown = new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/Question-black.gif"));

	// update icon as state of turnout changes
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (e.getPropertyName().equals("CommandedState")) {
			int now = ((Integer) e.getNewValue()).intValue();
			switch (now) {
				case Turnout.UNKNOWN:
					super.setText("<unknown>");
                    super.setIcon(unknown);
					return;
				case Turnout.CLOSED:
					super.setText("Closed");
                    super.setIcon(closed);
					return;
				case Turnout.THROWN:
					super.setText("Thrown");
                    super.setIcon(thrown);
					return;
				default:
					super.setText("<inconsistent>");
                    super.setIcon(inconsistent);
					return;
			}
		}
	}

}