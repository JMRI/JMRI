// TabbedPreferences.java

package apps.gui3;

import jmri.*;
import jmri.jmrit.XmlFile;
import jmri.util.swing.JmriPanel;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.1 $
 */
public class TabbedPreferences extends JmriPanel {

    protected String configFilename = "jmriprefs3.xml";
    
    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?
    public TabbedPreferences() {
        
        JTabbedPane p = new JTabbedPane();
        p.add("Connection 1", comm1 = jmri.jmrix.JmrixConfigPane.instance(1));
        p.add("GUI", guiPrefs = new jmri.GuiLafConfigPane());
        p.add("Programmer", new jmri.jmrit.symbolicprog.ProgrammerConfigPane());
        p.add("Actions", new apps.PerformActionPanel());
        p.add("Buttons", new apps.CreateButtonPanel());
        p.add("Files", new apps.PerformFilePanel());
        p.add("Scripts", new apps.PerformScriptPanel());
        p.add("Roster", new jmri.jmrit.roster.RosterConfigPane());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(p);
        JButton save = new JButton("Save");
        add(save);
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    preferencesSavePressed();
                }
            });
    }
    
    jmri.jmrix.JmrixConfigPane comm1;
    jmri.GuiLafConfigPane guiPrefs;
    
    void preferencesSavePressed() {
        saveContents();
    }
    
    void saveContents(){
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();

        // put the new items on the persistance list
        InstanceManager.configureManagerInstance().registerPref(comm1);
        InstanceManager.configureManagerInstance().registerPref(guiPrefs);

        // write file
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        // decide whether name is absolute or relative
        File file = new File(configFilename);
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+configFilename);
        }

        InstanceManager.configureManagerInstance().storePrefs(file);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TabbedPreferences.class.getName());
    
}


