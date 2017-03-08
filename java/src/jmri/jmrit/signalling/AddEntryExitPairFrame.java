package jmri.jmrit.signalling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

/**
 * Create frame for an Add Entry Exit.
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public class AddEntryExitPairFrame extends jmri.util.JmriJFrame {

    public AddEntryExitPairFrame() {
        super(false, true);
    }

    JButton sendButton;
    AddEntryExitPairPanel nxPanel;

    /**
     * Create and set an AddEntryExitPairFrame on a given LE panel and add menuItems.
     *
     * @param panel the LE panel on which to create the NX frame
     * @throws Exception when an error prevents creating the panel
     */
    public void initComponents(LayoutEditor panel) throws Exception {
        // the following code sets the frame's initial state

        nxPanel = new AddEntryExitPairPanel(panel);

        setTitle(Bundle.getMessage("AddEntryExitPoints"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(nxPanel);

        JMenuBar menuBar = new JMenuBar(); //getJMenuBar();
        JMenuItem options = new JMenuItem(Bundle.getMessage("MenuOptions")); // reuse existing key in jmrit.Bundle
        menuBar.add(options);
        options.addActionListener(new ActionListener() {
            @Override
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
