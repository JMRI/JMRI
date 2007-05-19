// NceMacroGenFrame.java


package jmri.jmrix.nce.macro;

import jmri.jmrix.nce.*;
import java.awt.*;
import javax.swing.*;

/**
 * Frame for user input of Nce macros
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @author Dan Boudreau 	Copyright (C) 2007
 * @version $Revision: 1.1 $
 **/
public class NceMacroGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.nce.NceListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public NceMacroGenFrame() {
    }

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state

        jLabel1.setText("Macro: ");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Execute NCE macro");

        packetTextField.setText("");
		packetTextField.setToolTipText("Enter macro 1 to 255");
		packetTextField.setMaximumSize(new Dimension(packetTextField
				.getMaximumSize().width, packetTextField.getPreferredSize().height));

        
        setTitle("Execute NCE macro");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);
 
        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {

		// Send Macro

		NceMessage m = createMacroCmd(packetTextField.getText());
		if (m == null) {
			JOptionPane.showMessageDialog(NceMacroGenFrame.this,
					"Enter 1 to 255", "NCE Macro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		NceTrafficController.instance().sendNceMessage(m, this);

	}

    public void  message(NceMessage m) {}  // ignore replies
    public void  reply(NceReply r) {} // ignore replies

    NceMessage createMacroCmd(String s) {
		// gather bytes in result
		if (s.length() == 0 | s.length() > 3)
			return null; // max three char, macro 1 - 255
		int macroNum = 0;
		try {
			macroNum = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}

		if (macroNum < 1 | macroNum > 255)
			return null;

		NceMessage m = new NceMessage(2);
		m.setElement(0, 0x9C); 		// Macro cmd
		m.setElement(1, macroNum);	// Macro #
		m.setBinary(true);
		m.setReplyLen(1);
		return m;
	}
}

