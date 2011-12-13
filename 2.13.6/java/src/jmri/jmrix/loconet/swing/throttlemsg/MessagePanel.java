// MessagePanel.java

package jmri.jmrix.loconet.swing.throttlemsg;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.loconet.*;

/**
 * Panel for sending messages to throttles.
 * @author		Bob Jacobsen   Copyright (C) 2008, 2010
 * @version             $Revision$
 */
public class MessagePanel extends jmri.jmrix.loconet.swing.LnPanel {

    // GUI member declarations
    JButton button = new JButton("Send");
    JTextField text = new JTextField(10);

    public MessagePanel() {
        super();

        // general GUI config

        // install items in GUI
        setLayout(new FlowLayout());
        add(text);
        add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                memo.getLnMessageManager().sendMessage(text.getText());
            }
        });
    }

    public String getHelpTarget() { return "package.jmri.jmrix.loconet.swing.throttlemsg.MessageFrame"; }
    public String getTitle() { 
        return getTitle(LocoNetBundle.bundle().getString("MenuItemThrottleMessages")); 
    }
    
}
