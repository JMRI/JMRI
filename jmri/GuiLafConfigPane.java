// GuiLafConfigPane.java

package jmri;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

/**
 * Provide GUI to configure Swing GUI LAF defaults
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.2 $
 */
public class GuiLafConfigPane extends JPanel {

    java.util.Hashtable installedLAFs;
    ButtonGroup LAFGroup;
    String selectedLAF;

    public GuiLafConfigPane() {
        setLayout(new FlowLayout());

        // find L&F definitions
        UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
        installedLAFs = new java.util.Hashtable(plafs.length);
        for (int i = 0; i < plafs.length; i++){
            installedLAFs.put(plafs[i].getName(), plafs[i].getClassName());
        }
        // make the radio buttons
        LAFGroup = new ButtonGroup();
        Enumeration LAFNames = installedLAFs.keys();
        while (LAFNames.hasMoreElements()) {
            String name = (String)LAFNames.nextElement();
            JRadioButton jmi = new JRadioButton(name);
            add(jmi);
            LAFGroup.add(jmi);
            jmi.setActionCommand(name);
            jmi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        selectedLAF = e.getActionCommand();
                    }
                });
            if (installedLAFs.get(name).equals(UIManager.getLookAndFeel().getClass().getName())) {
                jmi.setSelected(true);
                selectedLAF = name;
            }
        }
    }
    public String getClassName() {
        return LAFGroup.getSelection().getActionCommand();

    }
}

