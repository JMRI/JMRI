// ProgrammerConfigPane.java

package jmri.jmrit.symbolicprog;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provide GUI to configure symbolic programmer defaults.
 *
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.5 $
 */
public class ProgrammerConfigPane extends JPanel {

    public ProgrammerConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new JLabel("Format:"));
        add(programmerBox = new JComboBox(jmri.jmrit.symbolicprog.ProgDefault.findListOfProgFiles()));
        programmerBox.setSelectedItem(jmri.jmrit.symbolicprog.ProgDefault.getDefaultProgFile());

        // also create the advanced panel
        advancedPanel = new JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.X_AXIS));
        advancedPanel.add(showEmptyTabs = new JCheckBox("Show empty tabs"));
        showEmptyTabs.setSelected(jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame.getShowEmptyPanes());
    }
    JComboBox programmerBox;

    public String getSelectedItem() {
        return (String) programmerBox.getSelectedItem();
    }

    public JPanel getAdvancedPanel() {
        return advancedPanel;
    }

    JPanel advancedPanel;
    JCheckBox showEmptyTabs;

    public boolean getShowEmptyTabs() { return showEmptyTabs.isSelected(); }
}

