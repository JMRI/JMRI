package jmri.jmrit.log;

import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for adding an item to the log file.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class LogPanel extends JPanel {

    // member declarations
    javax.swing.JLabel label = new javax.swing.JLabel(Bundle.getMessage("LogMessageLabel"));
    javax.swing.JButton sendButton = new javax.swing.JButton(Bundle.getMessage("ButtonAddText"));
    javax.swing.JTextField textField = new javax.swing.JTextField(40);

    public LogPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(label);
        p1.add(textField);
        add(p1);
        add(sendButton);

        sendButton.setToolTipText(Bundle.getMessage("LogSendToolTip"));
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        log.error(textField.getText());
    }

    private final static Logger log = LoggerFactory.getLogger(LogPanel.class);

}
