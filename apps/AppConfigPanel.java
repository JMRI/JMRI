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

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;

/**
 * Basic configuration GUI infrastructure.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version	$Revision: 1.12 $
 */
public class AppConfigPanel extends JPanel {

    protected ResourceBundle rb;

    public AppConfigPanel(String filename, int nConnections) {
        super();
        log.debug("start app");
        mConfigFilename = filename;

        rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Communications
        log.debug("start comm");
        if (p1 == null) p1 = JmrixConfigPane.instance(1);
        p1.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutConnection")));
        addAndRemember(p1);

        // Swing GUI LAF
        log.debug("start laf");
        addAndRemember(p3 = new GuiLafConfigPane());
        p3.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutGUI")));

        // default programmer configuration
        log.debug("start prog");
        jmri.jmrit.symbolicprog.ProgrammerConfigPane p4;
        addAndRemember(p4 = new jmri.jmrit.symbolicprog.ProgrammerConfigPane());
        p4.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutProgrammer")));

        // add button to show advanced section
        log.debug("start adv but");
        add(new JSeparator(JSeparator.HORIZONTAL));
        showAdvanced = new JCheckBox(rb.getString("ButtonShowAdv"));
        showAdvanced.setAlignmentX(1.f);
        JPanel p5 = new JPanel();
        p5.setLayout(new FlowLayout());
        p5.setAlignmentX(1.f);
        p5.add(showAdvanced);
        add(p5);

        // add advanced section itself
        log.debug("start adv");
        advScroll = new JPanel();
        advScroll.setLayout(new BoxLayout(advScroll, BoxLayout.Y_AXIS));
	advancedPane = new JPanel();
        JScrollPane js = new JScrollPane(advancedPane);
        advancedPane.setLayout(new BoxLayout(advancedPane, BoxLayout.Y_AXIS));
        advScroll.setVisible(false);  // have to click first
        advScroll.add(js);
        add(advScroll);
        showAdvanced.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (showAdvanced.isSelected()) {
                    if (!localeAdded) {
                        localeSpace.add(p3.doLocale());
                        localeAdded = true;
                    }
                    advScroll.setVisible(true);
                    advScroll.validate();
                    if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
                    advScroll.repaint();
                }
                else {
                    advScroll.setVisible(false);
                    advScroll.validate();
                    if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
                    advScroll.repaint();
                }
            }
        });

        // fill advanced section
        log.debug("start comm 2");
        if (nConnections>1) {
            if (p2 == null) p2 = JmrixConfigPane.instance(2);
            p2.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAuxConnection")));
            advancedPane.add(p2);
            clist.add(p2);
        }

        // add advanced programmer options
        JPanel advProgSpace = new JPanel();
        advProgSpace.add(p4.getAdvancedPanel());
        advProgSpace.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutProgrammer")));
        advancedPane.add(advProgSpace);

        // reserve space for Locale later
        log.debug("start res locale");
        localeSpace  = new JPanel();
        localeSpace.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLocale")));
        localeAdded = false;
        advancedPane.add(localeSpace);

        log.debug("start act");
        PerformActionPanel action = new PerformActionPanel();
        action.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupActions")));
        advancedPane.add(action);
        clist.add(action);

        log.debug("start button");
        if (Apps.buttonSpace()!=null) {
            CreateButtonPanel buttons = new CreateButtonPanel();
            buttons.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutCreateButton")));
            advancedPane.add(buttons);
            clist.add(buttons);
        }

        log.debug("start file");
        PerformFilePanel files = new PerformFilePanel();
        files.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupFiles")));
        advancedPane.add(files);
        clist.add(files);

        // default roster location configuration
        log.debug("start roster");
        JPanel roster = new jmri.jmrit.roster.RosterConfigPane();
        roster.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutRoster")));
        advancedPane.add(roster);
        clist.add(roster);

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
    JPanel advScroll;
    JPanel advancedPane;

    JPanel localeSpace = null;
    boolean localeAdded = false;

    static JmrixConfigPane p1 = null;
    static JmrixConfigPane p2 = null;
    GuiLafConfigPane p3;

    public Component addAndRemember(Component c) {
        clist.add(c);
        super.add(c);
        return c;
    }

    public static String getConnection1() {
        if (p1 == null) p1 = JmrixConfigPane.instance(1);
        return p1.getCurrentProtocolName();
    }
    public static String getPort1() {
        if (p1 == null) p1 = JmrixConfigPane.instance(1);
        return p1.getCurrentProtocolInfo();
    }
    public static String getConnection2() {
        if (p2 == null) p2 = JmrixConfigPane.instance(2);
        if (p2 == null) return "(none)";
        return p2.getCurrentProtocolName();
    }
    public static String getPort2() {
        if (p2 == null) p2 = JmrixConfigPane.instance(1);
        if (p2 == null) return "(none)";
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
