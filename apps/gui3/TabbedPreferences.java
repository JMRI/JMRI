// TabbedPreferences.java

package apps.gui3;

import apps.AppConfigBase;
import jmri.jmrix.JmrixConfigPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.11 $
 */
public class TabbedPreferences extends AppConfigBase {
    
    public String getHelpTarget() { return "package.apps.TabbedPreferences"; }
    public String getTitle() { return rb.getString("TitlePreferences"); }

    String choices[] = {"Connections", "Start Up", "Display", "Messages", "Roster"/*, "Throttle"*/};
    String listRefValues[] = { "CONNECTION", "STARTUP", "DISPLAY", "MESSAGES", "ROSTER"/*, "THROTTLE"*/};

    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?

    JPanel detailpanel = new JPanel();
    JTabbedPane connectionPanel = new JTabbedPane();

    public TabbedPreferences() {
        super();

        final JList list = new JList(choices);
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));
        
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e){
                selection(listRefValues[list.getSelectedIndex()]);
            }
        });

        
        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new BoxLayout(buttonpanel,BoxLayout.Y_AXIS));
        buttonpanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 3));
        buttonpanel.add(listScroller);
        
        JButton save = new JButton("Save");
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    savePressed();
                }
            });
        buttonpanel.add(save);
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        for (int i=0; i<getNConnections(); i++) {
            addItem(connectionPanel, rb.getString("TabbedLayoutConnection")+(i+1), null, JmrixConfigPane.instance(i), true);
        }
        detailpanel.setLayout(new CardLayout());
        detailpanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 6));
        detailpanel.add(connectionPanel, "CONNECTION");
        jmri.GuiLafConfigPane gui;

        JTabbedPane startupPanel = new JTabbedPane();
        JTabbedPane displayPanel = new JTabbedPane();
        JTabbedPane rosterPanel = new JTabbedPane();
        JTabbedPane messagesPanel = new JTabbedPane();
        detailpanel.add(startupPanel, "STARTUP");
        detailpanel.add(displayPanel, "DISPLAY");
        detailpanel.add(rosterPanel, "ROSTER");
        detailpanel.add(new jmri.jmrit.beantable.usermessagepreferences.UserMessagePreferencesPane(), "MESSAGES");

        addItem(rosterPanel, rb.getString("TabbedLayoutProgrammer"),
                    "LabelTabbedLayoutProgrammer", new jmri.jmrit.symbolicprog.ProgrammerConfigPane(true), true);
        addItem(startupPanel, rb.getString("TabbedLayoutStartupActions"),
                    "LabelTabbedLayoutStartupActions", new apps.PerformActionPanel(), true);
        addItem(startupPanel, rb.getString("TabbedLayoutCreateButton"),
                    "LabelTabbedLayoutCreateButton", new apps.CreateButtonPanel(), true);
        addItem(startupPanel, rb.getString("TabbedLayoutStartupFiles"),
                    "LabelTabbedLayoutStartupFiles", new apps.PerformFilePanel(), true);
        addItem(startupPanel, rb.getString("TabbedLayoutStartupScripts"),
                    "LabelTabbedLayoutStartupScripts", new apps.PerformScriptPanel(), true);
        addItem(displayPanel, rb.getString("TabbedLayoutGUI"),
                    "LabelTabbedLayoutGUI", gui = new jmri.GuiLafConfigPane(), true);
        addItem(displayPanel, rb.getString("TabbedLayoutLocale"),
                    "LabelTabbedLayoutLocale", gui.doLocale(), false);
        addItem(rosterPanel, rb.getString("TabbedLayoutRoster"),
                    "LabelTabbedLayoutRoster", new jmri.jmrit.roster.RosterConfigPane(), true);

        // horrible hack to make sure GUI is done first
        items.remove(gui);
        items.add(0, gui);
        
        // finish layout
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        

        
        add(buttonpanel);
        add(new JSeparator(JSeparator.VERTICAL));
        add(detailpanel);
        
        list.setSelectedIndex(0);
    }
    
    void selection(String View){
        CardLayout cl = (CardLayout) (detailpanel.getLayout());
        cl.show(detailpanel, View);
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
    void addItem(JTabbedPane panel, String title, String labelKey, JComponent item, boolean store) {
        JComponent p= new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

        if (labelKey != null) {
            // insert label at top
            JTextArea t = new JTextArea(rb.getString(labelKey));
            t.setEditable(false);
            t.setAlignmentX(0.5f);
            t.setFont((new JLabel()).getFont());
            t.setPreferredSize(t.getMinimumSize());
            t.setMaximumSize(t.getMinimumSize());
            t.setOpaque(false);
            p.add(t);
        }
        p.add(item);
        p.add(Box.createVerticalGlue());

        panel.add(title, p);
        if (store) items.add(item);  
    }
    
    jmri.jmrix.JmrixConfigPane comm1;
    jmri.GuiLafConfigPane guiPrefs;
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TabbedPreferences.class.getName());
    
}
