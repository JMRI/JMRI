package jmri.jmrit.signalling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

/**
 * Frame Entry Exit Frames
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public class AddEntryExitPairFrame extends jmri.util.JmriJFrame {

    public AddEntryExitPairFrame() {
        super(false, true);
    }

    JButton sendButton;
    AddEntryExitPairPanel nxPanel;

    public void initComponents(LayoutEditor panel) throws Exception {
        // the following code sets the frame's initial state

        nxPanel = new AddEntryExitPairPanel(panel);

        setTitle(Bundle.getMessage("AddEntryExitPoints"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(nxPanel);

        JMenuBar menuBar = new JMenuBar();//getJMenuBar();
        JMenuItem options = new JMenuItem(Bundle.getMessage("MenuOptions")); // reuse existing key in jmrit.Bundle
        menuBar.add(options);
        options.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                nxPanel.optionWindow(event);
            }
        });

        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.signalling.EntryExitFrame", true);
        // pack for display
        pack();
    }
}
