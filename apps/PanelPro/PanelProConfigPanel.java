// PanelProConfigPanel.java

package apps.PanelPro;

import jmri.GuiLafConfigPane;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrix.JmrixConfigPane;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.sun.java.util.collections.ArrayList;

/**
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version	$Revision: 1.1 $
 */
public class PanelProConfigPanel extends JPanel {

    public PanelProConfigPanel(String filename) {
        super();
        mConfigFilename = filename;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Communications
        p1 = JmrixConfigPane.instance(1);
        p2 = JmrixConfigPane.instance(2);

        add(p1);
        add(p2);

        // Swing GUI LAF
        add(new GuiLafConfigPane());

        // default programmer configuration
        add(new jmri.jmrit.symbolicprog.ProgrammerConfigPane());

        // put the "Save" button at the bottom
        JButton save = new JButton("Save");
        super.add(save);  // don't want to persist the button!
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    savePressed();
                }
            });

    }

    JmrixConfigPane p1;
    JmrixConfigPane p2;

    public Component add(Component c) {
        clist.add(c);
        super.add(c);
        return c;
    }

    public String getConnection1() {
        return p1.getCurrentProtocolName();
    }
    public String getPort1() {
        return p1.getCurrentProtocolInfo();
    }
    public String getConnection2() {
        return p2.getCurrentProtocolName();
    }
    public String getPort2() {
        return p2.getCurrentProtocolInfo();
    }

    /**
     * Remember items to persist
     */
    ArrayList clist = new ArrayList();

    public void dispose() {
        clist.clear();
    }

    protected String mConfigFilename;

    protected void saveContents() {
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();
        // put the new GUI items on the persistance list
        for (int i = 0; i<clist.size(); i++) {
            InstanceManager.configureManagerInstance().registerPref(clist.get(i));
        }
        // write file
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        File file = new File(XmlFile.prefsDir()+mConfigFilename);

        InstanceManager.configureManagerInstance().storePrefs(file);
    }

    /**
     * Handle the Save button:  Backup the file, write a new one, prompt for
     * what to do next.  To do that, the last step is to present a dialog
     * box prompting the user to end the program.
     */
    public void savePressed() {
        saveContents();

        if (JOptionPane.showConfirmDialog(null,
                                          "Your updated preferences will take effect when the program is restarted. Quit now?",
                                          "Quit now?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            // end the program
            setVisible(false);
            dispose();
            System.exit(0);
        }
        // don't end the program, just close the window
        setVisible(false);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelProConfigPanel.class.getName());

}
