// TabbedPreferences.java

package apps.gui3;

import apps.AppConfigBase;
import jmri.*;
import jmri.jmrit.XmlFile;
import jmri.jmrix.JmrixConfigPane;

import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.4 $
 */
public class TabbedPreferences extends AppConfigBase {
    
    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?
    public TabbedPreferences() {
        super(4);
        
        for (int i=0; i<getNConnections(); i++)
            addItem(rb.getString("TabbedLayoutConnection")+(i+1), JmrixConfigPane.instance(i));
        addItem(rb.getString("TabbedLayoutProgrammer"), new jmri.jmrit.symbolicprog.ProgrammerConfigPane());
        addItem(rb.getString("TabbedLayoutGUI"), new jmri.GuiLafConfigPane());
        addItem(rb.getString("TabbedLayoutStartupActions"), new apps.PerformActionPanel());
        addItem(rb.getString("TabbedLayoutCreateButton"), new apps.CreateButtonPanel());
        addItem(rb.getString("TabbedLayoutStartupFiles"), new apps.PerformFilePanel());
        addItem(rb.getString("TabbedLayoutStartupScripts"), new apps.PerformScriptPanel());
        addItem(rb.getString("TabbedLayoutRoster"), new jmri.jmrit.roster.RosterConfigPane());

        pane.add("Messages", new jmri.jmrit.beantable.usermessagepreferences.UserMessagePreferencesPane());
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(pane);
        JButton save = new JButton("Save");
        add(save);
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    savePressed();
                }
            });
    }
    
    void addItem(String title, JComponent item) {
        pane.add(title, item);
        items.add(item);  
    }
        
    JTabbedPane pane = new JTabbedPane();
    
    jmri.jmrix.JmrixConfigPane comm1;
    jmri.GuiLafConfigPane guiPrefs;
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TabbedPreferences.class.getName());
    
}
