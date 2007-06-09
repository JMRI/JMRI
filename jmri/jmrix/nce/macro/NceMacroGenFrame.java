// NceMacroGenFrame.java


package jmri.jmrix.nce.macro;

import jmri.jmrix.nce.*;
import java.awt.*;
import javax.swing.*;

/**
 * Frame for user input of Nce macros
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @author Dan Boudreau 	Copyright (C) 2007
 * @version $Revision: 1.4 $
 **/

public class NceMacroGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.nce.NceListener {

	private static final int REPLY_LEN = 1;
	
	// member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JLabel macroText = new javax.swing.JLabel();
    javax.swing.JLabel macroReply = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(4);

    public NceMacroGenFrame() {
    }

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state

        jLabel1.setText("  Macro: ");
        jLabel1.setVisible(true);
        
        macroText.setText("  Reply: "); 
        macroText.setVisible(true);
        
        macroReply.setText("unknown"); 
        macroReply.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Execute NCE macro");

        packetTextField.setText("");
		packetTextField.setToolTipText("Enter macro 1 to 255");
		packetTextField.setMaximumSize(new Dimension(packetTextField
				.getMaximumSize().width, packetTextField.getPreferredSize().height));

        
        setTitle("Execute NCE macro");
        getContentPane().setLayout(new GridLayout(4,2));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(macroText);
        getContentPane().add(macroReply);
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
			macroReply.setText("error");
			JOptionPane.showMessageDialog(NceMacroGenFrame.this,
					"Enter 1 to 255", "NCE Macro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		macroReply.setText("waiting");
		NceTrafficController.instance().sendNceMessage(m, this);

	}

    public void  message(NceMessage m) {}  // ignore replies
    public void reply(NceReply r) {
		if (r.getNumDataElements() == REPLY_LEN) {

			int recChar = r.getElement(0);
			if (recChar == '!')
				macroReply.setText("okay");
			if (recChar == '0')
				macroReply.setText("empty");

		} else {
			macroReply.setText("error");
		}
        
    } 

    NceMessage createMacroCmd(String s) {
		// gather bytes in result
		int macroNum = 0;
		try {
			macroNum = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}

		if (macroNum < 1 | macroNum > 255)
			return null;

		NceMessage m = new NceMessage(5);
		m.setElement(0, 0xAD); 		// Macro cmd
		m.setElement(1, 0x00); 		// addr_h
		m.setElement(2, 0x01); 		// addr_l
		m.setElement(3, 0x01); 		// Macro cmd
		m.setElement(4, macroNum);	// Macro #
		m.setBinary(true);
		m.setReplyLen(REPLY_LEN);
		return m;
    }
}

