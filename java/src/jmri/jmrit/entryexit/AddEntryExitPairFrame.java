package jmri.jmrit.entryexit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

/**
 * Create frame for an Add Entry Exit.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class AddEntryExitPairFrame extends jmri.util.JmriJFrame {

    public AddEntryExitPairFrame() {
        super(true, true);
    }

    JButton sendButton;
    AddEntryExitPairPanel nxPanel;

    /**
     * Create and set an AddEntryExitPairFrame on a given LE panel and add menuItems.
     *
     * @param panel the LE panel on which to create the NX frame
     */
    public void initComponents(LayoutEditor panel) {
        // the following code sets the frame's initial state

        nxPanel = new AddEntryExitPairPanel(panel);

        setTitle(Bundle.getMessage("AddEntryExitPoints"));  // NOI18N
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(nxPanel);

        JMenuBar menuBar = new JMenuBar();
        JMenu optMenu = new JMenu(Bundle.getMessage("MenuOptions"));  // NOI18N
        JMenuItem optItem = new JMenuItem(Bundle.getMessage("MenuOptions"));  // NOI18N
        optMenu.add(optItem);
        menuBar.add(optMenu);

        optItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                nxPanel.optionWindow(event);
            }
        });

        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.entryexit.EntryExitFrame", true);  // NOI18N
        // pack for display
        pack();
    }
}
