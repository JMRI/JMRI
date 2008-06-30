package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;


/**
 * Create a new throttle.
 *
 * @author			Glen Oberhauser
 * @version     $Revision: 1.4 $
 */
public class EditThrottlePreferencesAction extends AbstractAction {
	
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.throttle.ThrottleBundle");

    /**
     * Constructor
     * @param s Name for the action.
     */
    public EditThrottlePreferencesAction(String s) {
        super(s);

    // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            setEnabled(false);
        }

    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e)
	{
		javax.swing.JOptionPane.showMessageDialog(null, rb.getString("ComingSoon"));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EditThrottlePreferencesAction.class.getName());

}
