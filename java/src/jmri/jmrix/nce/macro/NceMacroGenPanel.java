// NceMacroGenPanel.java


package jmri.jmrix.nce.macro;

import jmri.jmrix.nce.*;

import java.awt.*;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Pane for user input of Nce macros
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @author Dan Boudreau 	Copyright (C) 2007
 * Cloned into a Panel by
 * @author kcameron
 * @version $Revision$
 **/

public class NceMacroGenPanel extends jmri.jmrix.nce.swing.NcePanel implements jmri.jmrix.nce.NceListener {
	
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.macro.NceMacroBundle");

	private static final int REPLY_LEN = 1;
	
	// member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel(rb.getString("Macro"));
    javax.swing.JLabel macroText = new javax.swing.JLabel(rb.getString("Reply"));
    javax.swing.JLabel macroReply = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton(rb.getString("Send"));
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(4);
    
    // for padding out panel
    JLabel space1 = new JLabel("                  ");
    JLabel space3 = new JLabel("                       ");

    
    private NceTrafficController tc = null;

    public NceMacroGenPanel() {
    	super();
    }
    
    public void initContext(Object context) throws Exception {
        if (context instanceof NceSystemConnectionMemo ) {
            try {
				initComponents((NceSystemConnectionMemo) context);
			} catch (Exception e) {
				
			}
        }
    }

    public String getHelpTarget() { return "package.jmri.jmrix.nce.macro.NceMacroEditFrame"; }

    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("NCE_");
    	}
		x.append(": ");
    	x.append(rb.getString("TitleNceMacroGen"));
        return x.toString(); 
    }
    
    public void initComponents(NceSystemConnectionMemo memo) throws Exception {
    	this.memo = memo;
        tc = memo.getNceTrafficController();
        // the following code sets the frame's initial state
        
        // set initial state
        macroReply.setText(rb.getString("unknown")); 

        // load tool tips
        sendButton.setToolTipText("Execute NCE macro");
		packetTextField.setToolTipText("Enter macro 0 to 255");
		
		packetTextField.setMaximumSize(new Dimension(packetTextField
				.getMaximumSize().width, packetTextField.getPreferredSize().height));

        
        setLayout(new GridBagLayout());

        addItem(jLabel1, 0, 0);
        addItem(packetTextField, 2, 0);
        addItem(macroText, 0, 1);
        addItem(macroReply, 2, 1);
        //addItem(space1, 0, 2);
        //addItem(space2, 1, 2);
        addItem(space3, 2, 2);
        addItem(sendButton, 0, 3);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
        	public void actionPerformed(java.awt.event.ActionEvent e) {
        		sendButtonActionPerformed(e);
        	}
        });

    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {

		// Send Macro

		NceMessage m = createMacroCmd(packetTextField.getText());
		if (m == null) {
			macroReply.setText("error");
			JOptionPane.showMessageDialog(this,
					rb.getString("EnterMacroNumber"), rb.getString("NceMacro"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		macroReply.setText(rb.getString("waiting"));
		tc.sendNceMessage(m, this);
		
		// Unfortunately, the new command doesn't tell us if the macro is empty
		// so we send old command for status
		NceMessage m2 = createOldMacroCmd(packetTextField.getText());
		tc.sendNceMessage(m2, this);
	}

    public void  message(NceMessage m) {}  // ignore replies
    public void reply(NceReply r) {
		if (r.getNumDataElements() == REPLY_LEN) {

			int recChar = r.getElement(0);
			if (recChar == '!')
				macroReply.setText(rb.getString("okay"));
			if (recChar == '0')
				macroReply.setText(rb.getString("macroEmpty"));

		} else {
			macroReply.setText(rb.getString("error"));
		}
    } 

    
    NceMessage createMacroCmd(String s) {

		int macroNum = 0;
		try {
			macroNum = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}

		if (macroNum < 0 | macroNum > 255)
			return null;
		
		if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {

			// NCE always responds with okay (!) if macro number is in range.
			// We need to send this version of macro command to cause turnout
			// state to change in NCE CS
			NceMessage m = new NceMessage(5);
			m.setElement(0, NceBinaryCommand.ACC_CMD); 		// Macro cmd
			m.setElement(1, 0x00); 		// addr_h
			m.setElement(2, 0x01); 		// addr_l
			m.setElement(3, 0x01); 		// Macro cmd
			m.setElement(4, macroNum); 	// Macro #
			m.setBinary(true);
			m.setReplyLen(REPLY_LEN);
			return m;

		} else {
			
			// NCE responds with okay (!) if macro exist, (0) if not
			NceMessage m = new NceMessage(2);
			m.setElement(0, NceBinaryCommand.MACRO_CMD); 		// Macro cmd
			m.setElement(1, macroNum); 	// Macro #
			m.setBinary(true);
			m.setReplyLen(REPLY_LEN);
			return m;
		}
	}

	NceMessage createOldMacroCmd(String s) {

		int macroNum = 0;
		try {
			macroNum = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}

		if (macroNum < 0 | macroNum > 255)
			return null;

		// NCE responds with okay (!) if macro exist, (0) if not
		NceMessage m = new NceMessage(2);
		m.setElement(0, NceBinaryCommand.MACRO_CMD); // Macro cmd
		m.setElement(1, macroNum); // Macro #
		m.setBinary(true);
		m.setReplyLen(REPLY_LEN);
		return m;
	}
	
    private void addItem(JComponent c, int x, int y ){
    	GridBagConstraints gc = new GridBagConstraints ();
    	gc.gridx = x;
    	gc.gridy = y;
    	gc.weightx = 100.0;
    	gc.weighty = 100.0;
    	add(c, gc);
    }
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.nce.swing.NceNamedPaneAction {


		public Default() {
            super("Open NCE Send Macro Window", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                NceMacroGenPanel.class.getName(), 
                jmri.InstanceManager.getDefault(NceSystemConnectionMemo.class));
        }
    }
}


