package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Create a new throttle.
 *
 * @author			Glen Oberhauser
 * @version     $Revision: 1.2 $
 */
public class EditThrottlePreferencesAction extends AbstractAction {

    /**
     * Constructor
     * @param s Name for the action.
     */
    public EditThrottlePreferencesAction(String s) {
        super(s);
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e)
	{
		javax.swing.JOptionPane.showMessageDialog(null, "Coming Soon!");
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EditThrottlePreferencesAction.class.getName());

}