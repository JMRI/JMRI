// LogPanel.java

package jmri.jmrit.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.FlowLayout;
import javax.swing.*;

/**
 * User interface for adding an item to the log file.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision$
 */
public class LogPanel extends JPanel {

    // member declarations
    javax.swing.JLabel label = new javax.swing.JLabel("Message:");
    javax.swing.JButton sendButton = new javax.swing.JButton("Add");
    javax.swing.JTextField textField = new javax.swing.JTextField(40);

    public LogPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(label);
        p1.add(textField);
        add(p1);
        add(sendButton);


        sendButton.setToolTipText("Add message to the log file");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });
    }
   
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        log.error(textField.getText());
    }


    static Logger log = LoggerFactory.getLogger(LogPanel.class.getName());

}
