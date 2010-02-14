// TabbedPreferences.java

package apps.gui3;

import jmri.*;
import jmri.jmrit.XmlFile;
import jmri.util.swing.JmriPanel;

import java.util.List;
import java.util.ArrayList;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.2 $
 */
public class TabbedPreferences extends JmriPanel {

    protected String configFilename = "jmriprefs3.xml";
    
    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?
    public TabbedPreferences() {
        
        addItem("Connection 1", jmri.jmrix.JmrixConfigPane.instance(1));
        addItem("Connection 2", jmri.jmrix.JmrixConfigPane.instance(2));
        addItem("Connection 3", jmri.jmrix.JmrixConfigPane.instance(3));
        addItem("Connection 4", jmri.jmrix.JmrixConfigPane.instance(4));
        addItem("Programmer", new jmri.jmrit.symbolicprog.ProgrammerConfigPane());
        addItem("GUI", new jmri.GuiLafConfigPane());
        addItem("Actions", new apps.PerformActionPanel());
        addItem("Buttons", new apps.CreateButtonPanel());
        addItem("Files", new apps.PerformFilePanel());
        addItem("Scripts", new apps.PerformScriptPanel());
        addItem("Roster", new jmri.jmrit.roster.RosterConfigPane());

        pane.add("Messages", new jmri.jmrit.beantable.usermessagepreferences.UserMessagePreferencesPane());
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(pane);
        JButton save = new JButton("Save");
        add(save);
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    preferencesSavePressed();
                }
            });
    }
    
    void addItem(String title, JComponent item) {
        pane.add(title, item);
        items.add(item);  
    }
    
    List<Object> items = new ArrayList<Object>();
    
    JTabbedPane pane = new JTabbedPane();
    
    jmri.jmrix.JmrixConfigPane comm1;
    jmri.GuiLafConfigPane guiPrefs;
    
    void preferencesSavePressed() {
        saveContents();
    }
    
    void saveContents(){
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();

        // put the new items on the persistance list
        for (Object o : items) {
            InstanceManager.configureManagerInstance().registerPref(o);
        }
        
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


