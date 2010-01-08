// UserMessagePreferencesFrame.java

package jmri.jmrit.beantable.usermessagepreferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;


/**
 * Frame for ECoS preferences
 * @author	Kevin Dickerson   Copyright (C) 2009
 * @version $Revision: 1.1 $
 */
public class UserMessagePreferencesFrame extends jmri.util.JmriJFrame {

    public UserMessagePreferencesFrame() {
        super();
    }

    JButton sendButton;
    
    UserMessagePreferencesPane userMessagePreferencesPane;
    
    public void initComponents() throws Exception {
        // the following code sets the frame's initial state
        
        userMessagePreferencesPane = new UserMessagePreferencesPane();
        
        setTitle("User Message Preferences");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(userMessagePreferencesPane);

		addHelpMenu("package.jmri.jmrit.beantable.userMessagePreferences.UserMessagePreferencesFrame", true);

        pack();
    }

}

