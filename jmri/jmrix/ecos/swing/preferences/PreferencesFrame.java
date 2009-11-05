// PreferencesFrame.java

package jmri.jmrix.ecos.swing.preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;


/**
 * Frame for ECoS preferences
 * @author	Kevin Dickerson   Copyright (C) 2009
 * @version $Revision: 1.2 $
 */
public class PreferencesFrame extends jmri.util.JmriJFrame {

    public PreferencesFrame() {
        super();
    }

    JButton sendButton;
    PreferencesPane preferencesPane;
    
    public void initComponents() throws Exception {
        // the following code sets the frame's initial state
        
    /*    sendButton = new JButton("Update");
        sendButton.setVisible(true);
        sendButton.setToolTipText("User Preferences ");*/

        preferencesPane = new PreferencesPane();
        
        setTitle("ECoS User Preferences");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(preferencesPane);
        /*getContentPane().add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });*/

		addHelpMenu("package.jmri.jmrix.ecos.swing.preferencesframe.PreferencesFrame", true);

        // pack for display
        pack();
    }

    /*public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        preferencesPane.reset();
        
        EcosMessage m = new EcosMessage("get(1, info)");
        EcosTrafficController.instance().sendEcosMessage(m, null);

	}*/

}

