// SerialPacketGenPane.java

package jmri.jmrix.powerline.swing.packetgen;

import org.apache.log4j.Logger;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import jmri.util.StringUtil;
import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
/**
 * Frame for user input of Powerline messages
 * @author	Ken Cameron		Copyright (C) 2010
 * derived from:
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @author Dan Boudreau 	Copyright (C) 2007
 * @version $Revision$
 */
public class SerialPacketGenPane extends jmri.jmrix.powerline.swing.PowerlinePanel implements SerialListener {
	
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.powerline.swing.packetgen.SerialPacketGenBundle");

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);
    javax.swing.JCheckBox checkBoxBinCmd = new javax.swing.JCheckBox ();
    javax.swing.JTextField replyLenTextField = new javax.swing.JTextField(2);
    javax.swing.JCheckBox interlockButton = new javax.swing.JCheckBox("Interlock");
    
    private SerialTrafficController tc = null;
    private SerialSystemConnectionMemo memo = null;

    public SerialPacketGenPane() {
        super();
    }

    public void init() {}
    
    public void initContext(Object context) throws Exception {
        if (context instanceof SerialSystemConnectionMemo ) {
            try {
            	this.memo = (SerialSystemConnectionMemo) context;
				initComponents();
			} catch (Exception e) {
				//log.error("BoosterProg initContext failed");
			}
        }
    }

    public String getHelpTarget() { return "package.jmri.jmrix.powerline.swing.packetgen.PowerlinePacketGenPane"; }
    
    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append(rb.getString("DefaultTag"));
    	}
		x.append(": ");
    	x.append(rb.getString("Title"));
        return x.toString(); 
    }

    public void initComponents(SerialSystemConnectionMemo memo) throws Exception {
        this.memo = memo;
        // the following code sets the frame's initial state

        jLabel1.setText("Command:");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setText("");
        packetTextField.setToolTipText("Enter command as hexadecimal bytes separated by a space");
        packetTextField.setMaximumSize(
                                       new Dimension(packetTextField.getMaximumSize().width,
                                                     packetTextField.getPreferredSize().height
                                                     )
                                       );

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(jLabel1);
        add(packetTextField);
        add(interlockButton);
        add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    SerialMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        SerialMessage m = memo.getTrafficController().getSerialMessage(b.length);
        for (int i=0; i<b.length; i++) m.setElement(i, b[i]);
        m.setInterlocked(interlockButton.isSelected());
        return m;
    }

    public void  message(SerialMessage m) {}  // ignore replies
    public void  reply(SerialReply r) {} // ignore replies
    
	static Logger log = Logger.getLogger(SerialPacketGenPane.class.getName());

}

