package apps.jmrit.log;

import java.awt.FlowLayout;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for adding an item to the log file.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class LogPanel extends JPanel {

    // member declarations
    JLabel label = new JLabel(Bundle.getMessage("LogMessageLabel"));
    JButton sendButton = new JButton(Bundle.getMessage("ButtonAddText"));
    JTextField textField = new JTextField(40);

    public LogPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(label);
        p1.add(textField);
        add(p1);
        add(sendButton);

        sendButton.setToolTipText(Bundle.getMessage("LogSendToolTip"));
        sendButton.addActionListener(e -> log.error(textField.getText()));
    }

    private final static Logger log = LoggerFactory.getLogger(LogPanel.class);

}
