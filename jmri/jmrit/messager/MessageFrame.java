// MessageFrame.java

package jmri.jmrit.messager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.loconet.*;

/**
 * Frame for sending messages to throttles
 * @author		Bob Jacobsen   Copyright (C) 2003
 * @version             $Revision: 1.1 $
 */
public class MessageFrame extends javax.swing.JFrame {

    // GUI member declarations
    JButton button = new JButton("Send");
    JTextField text = new JTextField(10);

    public MessageFrame() {
        this("Throttle message");
    }

    public MessageFrame(String label) {
        super(label);

        // general GUI config

        // install items in GUI
        getContentPane().setLayout(new FlowLayout());
        getContentPane().add(text);
        getContentPane().add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LnMessageManager.instance().sendMessage(text.getText());
            }
        });
        pack();
    }

    // handle resizing when first shown
    private boolean mShown = false;
    public void addNotify() {
        super.addNotify();
        if (mShown)
            return;
        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
	// and disconnect from the SlotManager
    }
}
