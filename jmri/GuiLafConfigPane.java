// GuiLafConfigPane.java

package jmri;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

/**
 * Provide GUI to configure Swing GUI LAF defaults
 * <P>
 * Provides GUI configuration for SWING LAF by
 * displaying radiobuttons for each LAF implementation available.
 * This information is then persisted separately
 * (e.g. by {@link jmri.configurexml.GuiLafConfigPaneXml})
 * <P>
 * Locale default language and country is also considered a
 * GUI (and perhaps LAF) configuration item.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.3 $
 */
public class GuiLafConfigPane extends JPanel {

    java.util.Hashtable installedLAFs;
    ButtonGroup LAFGroup;
    String selectedLAF;

    public GuiLafConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p;
        doLAF(p = new JPanel());
        add(p);
        doLocale(p = new JPanel());
        add(p);
    }

    void doLAF(JPanel panel) {
        // find L&F definitions
        panel.setLayout(new FlowLayout());
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
            panel.add(jmi);
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
    void doLocale(JPanel panel) {
        // add JComboBoxen for language and country
        panel.setLayout(new FlowLayout());
        locales = Locale.getAvailableLocales();
        localeNames = new String[locales.length];
        for (int i = 0; i<locales.length; i++) {
            localeNames[i] = locales[i].getDisplayName();
        }
        localeBox = new JComboBox(localeNames);
        localeBox.setSelectedItem(Locale.getDefault().getDisplayName());
        panel.add(localeBox);
    }

    JComboBox localeBox;
    Locale[] locales;
    String[] localeNames;

    public Locale getLocale() {
        String desired = (String)localeBox.getSelectedItem();
        for (int i = 0; i<locales.length; i++) {
            if (desired.equals(localeNames[i])) return locales[i];
        }
        return null;
    }

    public String getClassName() {
        return LAFGroup.getSelection().getActionCommand();

    }
}

