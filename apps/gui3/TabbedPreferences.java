// TabbedPreferences.java

package apps.gui3;

import apps.AppConfigBase;
import jmri.jmrix.JmrixConfigPane;
//import jmri.util.swing.ButtonTabComponent;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.21 $
 */
public class TabbedPreferences extends AppConfigBase {
    
    public String getHelpTarget() { return "package.apps.AppConfigPanel"; }
    public String getTitle() { return rb.getString("TitlePreferences"); }
    public boolean isMultipleInstances() { return false; }  // only one of these!
    
    String choices[] = {"Connections", "Start Up", "Display", "Messages", "Roster", "Throttle"};
    String listRefValues[] = { "CONNECTION", "STARTUP", "DISPLAY", "MESSAGES", "ROSTER", "THROTTLE"};

    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?

    JPanel detailpanel = new JPanel();
    final JTabbedPane connectionPanel = new JTabbedPane();
    
    ArrayList<Integer> connectionTabInstance = new ArrayList<Integer>();
    

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
        ArrayList<Object> connList=null;
        int count = 0;
        
        connList = jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.JmrixConfigPane.class);
        if(connList!=null){
            for (int x = 0; x<connList.size(); x++){
                int newinstance = jmri.jmrix.JmrixConfigPane.getInstanceNumber((jmri.jmrix.JmrixConfigPane)connList.get(x));
                /*This extra check is here a some of the original code automatically created four connection instances
                therefore we need to filter out those that are set to none.*/
                if(!((jmri.jmrix.JmrixConfigPane)connList.get(x)).getCurrentProtocolName().equals("(none)")){
                    addConnection(count, newinstance);
                    count++;
                }
            }
        
        } else {
            connList = jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class);
            if (connList!=null){
                for (int x = 0; x<connList.size(); x++){
                    /*This extra check is here a some of the original code automatically created four connection instances
                    therefore we need to filter out those that are set to none.*/
                    if(!(jmri.jmrix.JmrixConfigPane.instance(x).getCurrentProtocolName().equals("(none)"))){
                        addConnection(count, x);
                        count++;
                    }
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
        detailpanel.add(connectionPanel, "CONNECTION");
        jmri.GuiLafConfigPane gui;

        JTabbedPane startupPanel = new JTabbedPane();
        JTabbedPane displayPanel = new JTabbedPane();
        JTabbedPane rosterPanel = new JTabbedPane();

        detailpanel.add(startupPanel, "STARTUP");
        detailpanel.add(displayPanel, "DISPLAY");
        detailpanel.add(new jmri.jmrit.throttle.ThrottlesPreferencesPane(), "THROTTLE");
        detailpanel.add(rosterPanel, "ROSTER");
        detailpanel.add(new jmri.jmrit.beantable.usermessagepreferences.UserMessagePreferencesPane(), "MESSAGES");


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
                    "LabelTabbedLayoutGUI", gui = new jmri.GuiLafConfigPane(), true, null);
        addItem(displayPanel, rb.getString("TabbedLayoutLocale"),
                    "LabelTabbedLayoutLocale", gui.doLocale(), false, null);
        addItem(rosterPanel, rb.getString("TabbedLayoutRoster"),
                    "LabelTabbedLayoutRoster", new jmri.jmrit.roster.RosterConfigPane(), true, null);

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
    
    void addConnection(final int tabPosition, int instance){
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        
        p.add(JmrixConfigPane.instance(instance));
        p.add(Box.createVerticalGlue());
        p.setToolTipText(JmrixConfigPane.instance(instance).getCurrentProtocolName());
        JPanel b = new JPanel();
        b.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton deleteButton = new JButton("Delete Connection");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                removeTab(e, null, tabPosition);
            }
        
        });
        b.add(deleteButton);
        p.add(b);
        String title;
        if (JmrixConfigPane.instance(instance).getConnectionName()!=null){
            title=JmrixConfigPane.instance(instance).getConnectionName();
        } else if((JmrixConfigPane.instance(instance).getCurrentProtocolName()!=null) && (!JmrixConfigPane.instance(instance).getCurrentProtocolName().equals("(none)"))){
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
        connectionTabInstance.add(instance);
        connectionPanel.add(title, p);
        connectionPanel.setTitleAt(tabPosition, title);
        connectionPanel.setToolTipTextAt(tabPosition, JmrixConfigPane.instance(instance).getCurrentProtocolName());
        //The following is not supported in 1.5, but is in 1.6 left here for future use.
//        connectionPanel.setTabComponentAt(tabPosition, new ButtonTabComponent(connectionPanel));
        items.add(JmrixConfigPane.instance(instance));
    }
    
    
    void addConnectionTab(){
        if(connectionPanel.indexOfTab("+")>0)
            connectionPanel.removeTabAt(connectionPanel.indexOfTab("+"));
        int newinstance = connectionTabInstance.get(connectionTabInstance.size()-1)+1;
        addConnection(connectionTabInstance.size(), newinstance);
        newConnectionTab();
    }
    
    void newConnectionTab(){
        JComponent p = new JPanel();
        p.add(Box.createVerticalGlue());

        p.setToolTipText("Add New Connection");
        connectionPanel.add("+", p);
        //The following is not supported in 1.5, but is in 1.6 left here for future use.
        //connectionPanel.setTabComponentAt(connectionPanel.getTabCount()-1, null);
        connectionPanel.setSelectedIndex(connectionPanel.getTabCount()-2);
    }
    
    jmri.jmrix.JmrixConfigPane comm1;
    jmri.GuiLafConfigPane guiPrefs;
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TabbedPreferences.class.getName());
    //Unable to do remove tab, via a component in 1.5 but is supported in 1.6
    //left here until a move is made to 1.6 or an alternative method is used.
    private void removeTab(ActionEvent e, JComponent c, int x){
        int i;
        // indexOfTabComponent is not supported in java 1.5
        //i = connectionPanel.indexOfTabComponent(c);
        i = x;

        if (i != -1) {
            int n = JOptionPane.showConfirmDialog(
                null,
                "Do you really want to delete connection " + connectionPanel.getTitleAt(i) + "?",
                "Delete Connection",
                JOptionPane.YES_NO_OPTION);
            if(n!=0)
                return;
            if(connectionPanel.getChangeListeners().length >0)
                connectionPanel.removeChangeListener(connectionPanel.getChangeListeners()[0]);
            int jmrixinstance = connectionTabInstance.get(i);
            
            connectionPanel.remove(i);  //was x
            items.remove(JmrixConfigPane.instance(jmrixinstance));
            JmrixConfigPane.dispose(jmrixinstance);
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
