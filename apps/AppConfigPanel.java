// AppConfigPanel.java

package apps;

import jmri.GuiLafConfigPane;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrix.JmrixConfigPane;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.sun.java.util.collections.ArrayList;

/**
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version	$Revision: 1.4 $
 */
public class AppConfigPanel extends JPanel {

    protected ResourceBundle rb;

    public AppConfigPanel(String filename, int nConnections) {
        super();
        mConfigFilename = filename;

        rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Communications
        p1 = JmrixConfigPane.instance(1);
        p1.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutConnection")));
        addAndRemember(p1);

        // Swing GUI LAF
        JPanel p3;
        addAndRemember(p3 = new GuiLafConfigPane());
        p3.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutGUI")));

        // default programmer configuration
        JPanel p4;
        addAndRemember(p4 = new jmri.jmrit.symbolicprog.ProgrammerConfigPane());
        p4.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutProgrammer")));

        // add button to show advanced section
        add(new JSeparator(JSeparator.HORIZONTAL));
        showAdvanced = new JCheckBox(rb.getString("ButtonShowAdv"));
        JPanel p5 = new JPanel();
        p5.setLayout(new FlowLayout());
        p5.add(showAdvanced);
        add(p5);

        // add advanced section itself
	advancedPane = new JPanel();
        advancedPane.setLayout(new BoxLayout(advancedPane, BoxLayout.Y_AXIS));
        advancedPane.setVisible(false);  // have to click first
        add(advancedPane);
        showAdvanced.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (showAdvanced.isSelected()) {
                    advancedPane.setVisible(true);
                    advancedPane.validate();
                    if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
                    advancedPane.repaint();
                }
                else {
                    advancedPane.setVisible(false);
                    advancedPane.validate();
                    if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
                    advancedPane.repaint();
                }
            }
        });

        // fill advanced section
        if (nConnections>1) {
            p2 = JmrixConfigPane.instance(2);
            p2.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAuxConnection")));
            advancedPane.add(p2);
            clist.add(p2);
        }

        PerformActionPanel action = new PerformActionPanel();
        action.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupActions")));
        advancedPane.add(action);
        clist.add(action);

        PerformFilePanel files = new PerformFilePanel();
        files.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupFiles")));
        advancedPane.add(files);
        clist.add(files);

        // put the "Save" button at the bottom
        JButton save = new JButton(rb.getString("ButtonSave"));
        add(save);  // don't want to persist the button!
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    savePressed();
                }
            });

    }

    JCheckBox showAdvanced;
    JPanel advancedPane;

    JmrixConfigPane p1;
    JmrixConfigPane p2;

    public Component addAndRemember(Component c) {
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
        if (p2==null) return "(none)";
        return p2.getCurrentProtocolName();
    }
    public String getPort2() {
        if (p2==null) return "(none)";
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
                                          rb.getString("MessageLongQuitWarning"),
                                          rb.getString("MessageShortQuitWarning"),
                                          JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            // end the program
            dispose();
            System.exit(0);
        }
        // don't end the program, just close the window
        if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).setVisible(false);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AppConfigPanel.class.getName());

}
