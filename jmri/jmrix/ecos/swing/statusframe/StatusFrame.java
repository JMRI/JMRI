// StatusFrame.java

package jmri.jmrix.ecos.swing.statusframe;

import jmri.jmrix.ecos.*;
import javax.swing.*;


/**
 * Frame for ECoS status
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version $Revision: 1.5 $
 * @deprecated 2.11.3
 */
@Deprecated
public class StatusFrame extends jmri.util.JmriJFrame {

    public StatusFrame(EcosSystemConnectionMemo memo) {
        super();
        adaptermemo = memo;
    }
    
    EcosSystemConnectionMemo adaptermemo;

    JButton sendButton;
    StatusPane statusPane;
    
    public void initComponents(EcosSystemConnectionMemo memo) throws Exception {
        adaptermemo = memo;
        // the following code sets the frame's initial state
        
        sendButton = new JButton("Update");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Request status update from ECoS");

        statusPane = new StatusPane(adaptermemo.getTrafficController());
        
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
        adaptermemo.getTrafficController().sendEcosMessage(m, null);

	}

    public void setSystemConnectionMemo(EcosSystemConnectionMemo memo){
        adaptermemo=memo;
    }

}

