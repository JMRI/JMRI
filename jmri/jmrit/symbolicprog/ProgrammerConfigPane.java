// ProgrammerConfigPane.java

package jmri.jmrit.symbolicprog;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provide GUI to configure symbolic programmer defaults
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.2 $
 */
public class ProgrammerConfigPane extends JPanel {

    public ProgrammerConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new JLabel("Format:"));
        add(programmerBox = new JComboBox(jmri.jmrit.symbolicprog.CombinedLocoSelPane.findListOfProgFiles()));
        programmerBox.setSelectedItem(jmri.jmrit.symbolicprog.CombinedLocoSelPane.defaultProgFile);
    }
    JComboBox programmerBox;

    public String getSelectedItem() {
        return (String) programmerBox.getSelectedItem();
    }

}

