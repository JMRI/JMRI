// TabbedPreferences.java

package apps.gui3;

import apps.AppConfigBase;
import jmri.jmrix.JmrixConfigPane;

import java.awt.event.*;
import javax.swing.*;

/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.7 $
 */
public class TabbedPreferences extends AppConfigBase {
    
    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?
    public TabbedPreferences() {
        super(4);
        
        for (int i=0; i<getNConnections(); i++) {
            addItem(rb.getString("TabbedLayoutConnection")+(i+1), null, JmrixConfigPane.instance(i), true);
        }
        
        jmri.GuiLafConfigPane gui;
        
        addItem(rb.getString("TabbedLayoutProgrammer"), 
                    "LabelTabbedLayoutProgrammer", new jmri.jmrit.symbolicprog.ProgrammerConfigPane(), true);
        addItem(rb.getString("TabbedLayoutStartupActions"), 
                    "LabelTabbedLayoutStartupActions", new apps.PerformActionPanel(), true);
        addItem(rb.getString("TabbedLayoutCreateButton"), 
                    "LabelTabbedLayoutCreateButton", new apps.CreateButtonPanel(), true);
        addItem(rb.getString("TabbedLayoutStartupFiles"), 
                    "LabelTabbedLayoutStartupFiles", new apps.PerformFilePanel(), true);
        addItem(rb.getString("TabbedLayoutStartupScripts"), 
                    "LabelTabbedLayoutStartupScripts", new apps.PerformScriptPanel(), true);
        addItem(rb.getString("TabbedLayoutGUI"), 
                    "LabelTabbedLayoutGUI", gui = new jmri.GuiLafConfigPane(), true);
        addItem(rb.getString("TabbedLayoutLocale"), 
                    "LabelTabbedLayoutLocale", gui.doLocale(), false);
        addItem(rb.getString("TabbedLayoutRoster"), 
                    "LabelTabbedLayoutRoster", new jmri.jmrit.roster.RosterConfigPane(), true);

        addItem(rb.getString("TabbedLayoutMessages"),
                    "LabelTabbedLayoutMessages",
                    new jmri.jmrit.beantable.usermessagepreferences.UserMessagePreferencesPane(),
                    false);
        
        // horrible hack to make sure GUI is done first
        items.remove(gui);
        items.add(0, gui);

        // finish layout
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
    
    /**
     * Create a new tab containing an item
     *
     * @param title Title of the tab
     * @param labelKey Key in the resource bundle for a label, or null
     * @param item The configuration item to display
     * @param store Should this item be saved for later storage? false means somebody
     *        else is handling the storage responsibility.
     */
    void addItem(String title, String labelKey, JComponent item, boolean store) {
        JComponent p= new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

        if (labelKey != null) {
            // insert label at top
            JTextArea t = new JTextArea(rb.getString(labelKey));
            t.setEditable(false);
            t.setAlignmentX(0.5f);
            t.setPreferredSize(t.getMinimumSize());
            t.setMaximumSize(t.getMinimumSize());
            t.setOpaque(false);
            p.add(t);
        }
        p.add(item);
        p.add(Box.createVerticalGlue());

        pane.add(title, p);
        if (store) items.add(item);  
    }
        
    JTabbedPane pane = new JTabbedPane();
    
    jmri.jmrix.JmrixConfigPane comm1;
    jmri.GuiLafConfigPane guiPrefs;
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TabbedPreferences.class.getName());
    
}
