// TabbedPreferences.java

package apps.gui3;

import apps.AppConfigBase;
import apps.GuiLafConfigPane;

import jmri.jmrix.JmrixConfigPane;
import jmri.UserPreferencesManager;
import jmri.InstanceManager;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.jdom.Element;

/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.37 $
 */
public class TabbedPreferences extends AppConfigBase {
    
    public String getHelpTarget() { return "package.apps.TabbedPreferences"; }
    public String getTitle() { return rb.getString("TitlePreferences"); }
    public boolean isMultipleInstances() { return false; }  // only one of these!
    static ArrayList<Element> preferencesElements = new ArrayList<Element>();
    
    String choices[] = {rb.getString("MenuConnections"), rb.getString("MenuDefaults"), rb.getString("MenuFileLocation"), rb.getString("MenuStartUp"), rb.getString("MenuDisplay"), rb.getString("MenuMessages"), rb.getString("MenuRoster"), rb.getString("MenuThrottle"), rb.getString("MenuWiThrottle")};
    String listRefValues[] = { "CONNECTIONS", "DEFAULTS", "FILELOCATIONS", "STARTUP", "DISPLAY", "MESSAGES", "ROSTER", "THROTTLE", "WITHROTTLE"};

    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?

    JPanel detailpanel = new JPanel();
    final JTabbedPane connectionPanel = new JTabbedPane();
    final jmri.jmrit.throttle.ThrottlesPreferencesPane throttlePreferences = new jmri.jmrit.throttle.ThrottlesPreferencesPane();
    final jmri.jmrit.withrottle.WiThrottlePrefsPanel withrottlePrefsPanel = new jmri.jmrit.withrottle.WiThrottlePrefsPanel();
    
    ArrayList<Integer> connectionTabInstance = new ArrayList<Integer>();
    final UserPreferencesManager pref = InstanceManager.getDefault(UserPreferencesManager.class);

    public TabbedPreferences() {
        super();
        
        pref.disallowSave();
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
        
        JButton save = new JButton(rb.getString("ButtonSave"));
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    throttlePreferences.jbSaveActionPerformed(e);
                    withrottlePrefsPanel.storeValues();
                    apps.FileLocationPane.save();
                    savePressed();
                    pref.allowSave();
                }
            });
        buttonpanel.add(save);
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        ArrayList<Object> connList=null;
        int count = 0;
        connList = jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.JmrixConfigPane.class);
        if(connList!=null){
            for (int x = 0; x<connList.size(); x++){
                int newinstance = jmri.jmrix.JmrixConfigPane.getInstanceNumber((jmri.jmrix.JmrixConfigPane)connList.get(x));
                /*This extra check is here a some of the original code automatically created four connection instances
                therefore we need to filter out those that are set to none.*/
                if(!((jmri.jmrix.JmrixConfigPane)connList.get(x)).getCurrentProtocolName().equals(JmrixConfigPane.NONE)){
                    addConnection(count, newinstance);
                    count++;
                }
            }
        }
        /**By default 4 sets of jmrixconfigpanes are created at start up.  However
         * if there are more than 4 connections, the 5+ connections do not have
         * a jmrixconfigpane instance created, thus we need to pick those up.
         */
        connList = jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class);
        if (connList!=null){
            for (int x = 0; x<connList.size(); x++){
                /*This extra check is here a some of the original code automatically created four connection instances
                therefore we need to filter out those that are set to none.*/
                if(!(jmri.jmrix.JmrixConfigPane.instance(count).getCurrentProtocolName().equals(JmrixConfigPane.NONE))){
                    addConnection(count, count);
                    count++;
                }
            }
        }
        
        if (count==0)
            addConnection(0,0);
        newConnectionTab();
        connectionPanel.addChangeListener(new ChangeListener() { 
            // This method is called whenever the selected tab changes 
            public void stateChanged(ChangeEvent evt) { 
                JTabbedPane pane = (JTabbedPane)evt.getSource(); 
                // Get current tab 
                int sel = pane.getSelectedIndex();
                if (sel == -1){
                    addConnectionTab();
                }
                else {
                    String paneTitle = pane.getTitleAt(sel);
                    
                    if (paneTitle.equals("+")){
                        addConnectionTab();
                    }
                }
            } 
        }); 
        detailpanel.setLayout(new CardLayout());
        detailpanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 6));
        detailpanel.add(connectionPanel, "CONNECTIONS");
        GuiLafConfigPane gui;

        JTabbedPane defaultsPanel = new JTabbedPane();
        JTabbedPane startupPanel = new JTabbedPane();
        JTabbedPane displayPanel = new JTabbedPane();
        JTabbedPane rosterPanel = new JTabbedPane();
        JTabbedPane filePanel = new JTabbedPane();
        
        detailpanel.add(defaultsPanel, "DEFAULTS");
        detailpanel.add(startupPanel, "STARTUP");
        detailpanel.add(displayPanel, "DISPLAY");
        detailpanel.add(filePanel, "FILELOCATIONS");
        detailpanel.add(throttlePreferences, "THROTTLE");
        detailpanel.add(withrottlePrefsPanel, "WITHROTTLE");
        detailpanel.add(rosterPanel, "ROSTER");
        detailpanel.add(new jmri.jmrit.beantable.usermessagepreferences.UserMessagePreferencesPane(), "MESSAGES");


        addItem(defaultsPanel, rb.getString("TabbedLayoutDefaults"),
                    "LabelTabbedLayoutDefaults", new apps.ManagerDefaultsConfigPane(), true, null);

        addItem(rosterPanel, rb.getString("TabbedLayoutProgrammer"),
                    "LabelTabbedLayoutProgrammer", new jmri.jmrit.symbolicprog.ProgrammerConfigPane(true), true, null);

        addItem(startupPanel, rb.getString("TabbedLayoutStartupActions"),
                    "LabelTabbedLayoutStartupActions", new apps.PerformActionPanel(), true, null);
        addItem(startupPanel, rb.getString("TabbedLayoutCreateButton"),
                    "LabelTabbedLayoutCreateButton", new apps.CreateButtonPanel(), true, null);
        addItem(startupPanel, rb.getString("TabbedLayoutStartupFiles"),
                    "LabelTabbedLayoutStartupFiles", new apps.PerformFilePanel(), true, null);
        addItem(startupPanel, rb.getString("TabbedLayoutStartupScripts"),
                    "LabelTabbedLayoutStartupScripts", new apps.PerformScriptPanel(), true, null);

        addItem(displayPanel, rb.getString("TabbedLayoutGUI"),
                    "LabelTabbedLayoutGUI", gui = new GuiLafConfigPane(), true, null);
        addItem(displayPanel, rb.getString("TabbedLayoutLocale"),
                    "LabelTabbedLayoutLocale", gui.doLocale(), false, null);
        addItem(displayPanel, rb.getString("TabbedLayoutConsole"),
                    "LabelTabbedLayoutConsole", new apps.SystemConsoleConfigPanel(), true, null);
        addItem(rosterPanel, rb.getString("TabbedLayoutRoster"),
                    "LabelTabbedLayoutRoster", new jmri.jmrit.roster.RosterConfigPane(), true, null);
        addItem(filePanel, rb.getString("TabbedLayoutFileLocations"),
                    "LabelTabbedFileLocations", new apps.FileLocationPane(), true, null);

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
    void addItem(JTabbedPane panel, String title, String labelKey, JComponent item, boolean store, String tooltip) {
        JComponent p = new JPanel();
        //JComponent p = new ButtonTabComponent(panel);
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
        
        panel.addTab(title, null, p, tooltip);
        if (store) items.add(item);  
    }
    
    void addConnection(int tabPosition, final int instance){
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        
        p.add(JmrixConfigPane.instance(instance));
        p.add(Box.createVerticalGlue());
        //p.setToolTipText(JmrixConfigPane.instance(instance).getCurrentProtocolName());
        JPanel b = new JPanel();
        b.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton deleteButton = new JButton(rb.getString("ButtonDeleteConnection"));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                removeTab(e, null, connectionPanel.getSelectedIndex());
            }
        
        });
        b.add(deleteButton);

        //For a future release
        JPanel c = new JPanel();
        c.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        final JCheckBox disable = new JCheckBox("Disable Connection");
        disable.setSelected(JmrixConfigPane.instance(instance).getDisabled());
        disable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                JmrixConfigPane.instance(instance).setDisabled(disable.isSelected());
            }
        });
        c.add(disable);
        JPanel p1 = new JPanel();
        p1.setLayout(new GridLayout(0,2));
        p1.add(c);
        p1.add(b);
        p.add(p1);
        String title;

        if (JmrixConfigPane.instance(instance).getConnectionName()!=null){
            title=JmrixConfigPane.instance(instance).getConnectionName();
        } else if((JmrixConfigPane.instance(instance).getCurrentProtocolName()!=null) && (!JmrixConfigPane.instance(instance).getCurrentProtocolName().equals(JmrixConfigPane.NONE))){
            title = JmrixConfigPane.instance(instance).getCurrentProtocolName();
        } else {
            title = rb.getString("TabbedLayoutConnection")+(tabPosition+1);
            if (connectionPanel.indexOfTab(title)!=-1){
                for (int x=2; x<12;x++){
                    title = rb.getString("TabbedLayoutConnection")+(tabPosition+2);
                    if (connectionPanel.indexOfTab(title)!=-1)
                        break;
                }
            }
        }

        if(JmrixConfigPane.instance(instance).getDisabled()){
            title = "(" + title + ")";
        }
        connectionTabInstance.add(instance);
        connectionPanel.add(title, p);
        connectionPanel.setTitleAt(tabPosition, title);
        connectionPanel.setToolTipTextAt(tabPosition, title);

        if(jmri.jmrix.ConnectionStatus.instance().isConnectionOk(JmrixConfigPane.instance(instance).getCurrentProtocolInfo())){
            connectionPanel.setForegroundAt(tabPosition, Color.black);
        } else {
            connectionPanel.setForegroundAt(tabPosition, Color.red);
        }
        if(JmrixConfigPane.instance(instance).getDisabled()){
            connectionPanel.setForegroundAt(tabPosition, Color.ORANGE);
        }
        //The following is not supported in 1.5, but is in 1.6 left here for future use.
//        connectionPanel.setTabComponentAt(tabPosition, new ButtonTabComponent(connectionPanel));
        items.add(JmrixConfigPane.instance(instance));
    }
    
    
    void addConnectionTab(){
        connectionPanel.removeTabAt(connectionPanel.indexOfTab("+"));
        int newinstance;
        if (connectionTabInstance.isEmpty())
            newinstance = 0;
        else
            newinstance = connectionTabInstance.get(connectionTabInstance.size()-1)+1;
        addConnection(connectionTabInstance.size(), newinstance);
        newConnectionTab();
    }
    
    void newConnectionTab(){
        pref.disallowSave();
        JComponent p = new JPanel();
        p.add(Box.createVerticalGlue());

        connectionPanel.add("+", p);
        connectionPanel.setToolTipTextAt(connectionPanel.getTabCount()-1, rb.getString("ToolTipAddNewConnection"));
        //The following is not supported in 1.5, but is in 1.6 left here for future use.
        //connectionPanel.setTabComponentAt(connectionPanel.getTabCount()-1, null);
        connectionPanel.setSelectedIndex(connectionPanel.getTabCount()-2);
    }
    
    jmri.jmrix.JmrixConfigPane comm1;
    GuiLafConfigPane guiPrefs;
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TabbedPreferences.class.getName());
    //Unable to do remove tab, via a component in 1.5 but is supported in 1.6
    //left here until a move is made to 1.6 or an alternative method is used.
    private void removeTab(ActionEvent e, JComponent c, int x){
        pref.disallowSave();
        int i;
        // indexOfTabComponent is not supported in java 1.5
        //i = connectionPanel.indexOfTabComponent(c);
        i = x;

        if (i != -1) {
            int n = JOptionPane.showConfirmDialog(
                null,
                MessageFormat.format(rb.getString("MessageDoDelete"), new Object[]{connectionPanel.getTitleAt(i)}),
                rb.getString("MessageDeleteConnection"),
                JOptionPane.YES_NO_OPTION);
            if(n!=JOptionPane.YES_OPTION)
                return;
            if(connectionPanel.getChangeListeners().length >0)
                connectionPanel.removeChangeListener(connectionPanel.getChangeListeners()[0]);
            int jmrixinstance = connectionTabInstance.get(i);
            
            connectionPanel.remove(i);  //was x
            items.remove(JmrixConfigPane.instance(jmrixinstance));
            try{
                JmrixConfigPane.dispose(jmrixinstance);
            } catch (java.lang.NullPointerException ex) {log.error("Caught Null Pointer Exception while removing connection tab"); }
            connectionTabInstance.remove(i);
            if(connectionPanel.getTabCount()==1){
                addConnectionTab();
            }
            connectionPanel.setSelectedIndex(connectionPanel.getTabCount()-2);
            connectionPanel.addChangeListener(new ChangeListener() { 
            // This method is called whenever the selected tab changes 
                public void stateChanged(ChangeEvent evt) { 
                    JTabbedPane pane = (JTabbedPane)evt.getSource(); 
                    // Get current tab 
                    int sel = pane.getSelectedIndex();
                    if (sel == -1){
                        addConnectionTab();
                    }
                    else {
                        String paneTitle = pane.getTitleAt(sel);
                        if (paneTitle.equals("+")){
                            addConnectionTab();
                        }
                    }
                } 
            });
        }        
    }
    /*Unable to do remove tab, via a component in 1.5 but is supported in 1.6
    left here until a move is made to 1.6 or an alternative method is used.*/
    /*
        private class ButtonTabComponent extends JPanel {
        private final JTabbedPane pane;

        public ButtonTabComponent(final JTabbedPane pane) {
            //unset default FlowLayout' gaps
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            if (pane == null) {
                throw new NullPointerException("TabbedPane is null");
            }
            this.pane = pane;
            setOpaque(false);
            
            //make JLabel read titles from JTabbedPane
            JLabel label = new JLabel() {
                public String getText() {
                    int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                    if (i != -1) {
                        return pane.getTitleAt(i);
                    }
                    return null;
                }
            };
            
            add(label);
            //add more space between the label and the button
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            //tab button
            JButton button = new TabButton();
            add(button);
            //add more space to the top of the component
            setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        }

        private class TabButton extends JButton implements ActionListener {
            public TabButton() {
                int size = 17;
                setPreferredSize(new Dimension(size, size));
                setToolTipText("Delete This Connection");
                //Make the button looks the same for all Laf's
                setUI(new BasicButtonUI());
                //No need to be focusable
                setFocusable(false);
                setBorderPainted(false);
                //Making nice rollover effect
                //we use the same listener for all buttons
                addMouseListener(buttonMouseListener);
                setRolloverEnabled(true);
                //Close the proper tab by clicking the button
                addActionListener(this);
                setBackground(Color.RED);
            }

            public void actionPerformed(ActionEvent e) {
                removeTab(e, ButtonTabComponent.this, pane.getSelectedIndex());
            }

            //we don't want to update UI for this button
            public void updateUI() {
            }

            //paint the cross
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                //shift the image for pressed buttons
                if (getModel().isPressed()) {
                    g2.translate(1, 1);
                }
                g2.setStroke(new BasicStroke(2));
                g2.setColor(Color.WHITE);
                if (getModel().isRollover()) {
                    g2.setColor(Color.RED);
                }
                int delta = 6;
                
                g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
                g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
                g2.dispose();
            }
        }
        

        private final MouseListener buttonMouseListener = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setBackground(Color.WHITE);
                }
            }

            public void mouseExited(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setBackground(Color.RED);
                }
            }
        };
    }*/
}
