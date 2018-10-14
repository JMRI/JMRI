package jmri.jmrix.loconet.swing.throttlemsg;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextField;

/**
 * Panel for sending messages to throttles.
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010
 */
public class MessagePanel extends jmri.jmrix.loconet.swing.LnPanel {

    // GUI member declarations
    JButton button = new JButton(Bundle.getMessage("ButtonSend"));
    JTextField text = new JTextField(10);

    public MessagePanel() {
        super();

        // general GUI config
        // install items in GUI
        setLayout(new FlowLayout());
        add(text);
        add(button);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                memo.getLnMessageManager().sendMessage(text.getText());
            }
        });
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.swing.throttlemsg.MessageFrame"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemThrottleMessages"));
    }

}
