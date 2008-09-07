// StatusFrame.java

package jmri.jmrix.ecos.swing.statusframe;

import jmri.util.*;
import jmri.jmrix.ecos.*;
import java.awt.*;
import javax.swing.*;


/**
 * Frame for ECoS status
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version $Revision: 1.2 $
 */
public class StatusFrame extends jmri.util.JmriJFrame {

    public StatusFrame() {
        super();
    }

    JButton sendButton;
    StatusPane statusPane;
    
    public void initComponents() throws Exception {
        // the following code sets the frame's initial state
        
        sendButton = new JButton("Update");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Request status update from ECoS");

        statusPane = new StatusPane();
        
        setTitle("ECoS Info");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(statusPane);
        getContentPane().add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

		addHelpMenu("package.jmri.jmrix.ecos.swing.statusframe.StatusFrame", true);

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        statusPane.reset();
        
        EcosMessage m = new EcosMessage("get(1, info)");
        EcosTrafficController.instance().sendEcosMessage(m, null);

	}

}

