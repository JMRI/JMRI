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

import java.awt.*;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

import java.lang.reflect.Method;

import javax.swing.*;
import org.jdom.Element;
import java.util.Vector;

/**
 * Provide access to preferences via a 
 * tabbed pane
 * <P>
 * @author	Bob Jacobsen   Copyright 2010
 * @version $Revision: 1.50 $
 */
public class TabbedPreferences extends AppConfigBase {

    @Override
    public String getHelpTarget() { return "package.apps.TabbedPreferences"; }
    @Override
    public String getTitle() { return rb.getString("TitlePreferences"); }
    @Override
    public boolean isMultipleInstances() { return false; }  // only one of these!
    /* static */ ArrayList<Element> preferencesElements = new ArrayList<Element>();

    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?

    JPanel detailpanel = new JPanel();
    final JTabbedPane connectionPanel = new JTabbedPane();
    jmri.jmrit.throttle.ThrottlesPreferencesPane throttlePreferences;
    jmri.jmrit.withrottle.WiThrottlePrefsPanel withrottlePrefsPanel;
    jmri.web.miniserver.MiniServerPrefsPanel miniserverPrefsPanel;
    
    /* static */ ArrayList<Integer> connectionTabInstance = new ArrayList<Integer>();
    /* static */ ArrayList<preferencesCatItems> preferencesArray = new ArrayList<preferencesCatItems>();
    final UserPreferencesManager pref = InstanceManager.getDefault(UserPreferencesManager.class);
    JPanel buttonpanel;
    /* static */ JList list;
    JScrollPane listScroller;
    boolean inital = false;
    
    public TabbedPreferences(){
        
        /*Adds the place holders for the menu items so that any items
        add by third party code is added to the end*/
        preferencesArray.add(new preferencesCatItems("CONNECTIONS", rb.getString("MenuConnections")));
        
        preferencesArray.add(new preferencesCatItems("DEFAULTS", rb.getString("MenuDefaults")));
        
        preferencesArray.add(new preferencesCatItems("FILELOCATIONS", rb.getString("MenuFileLocation")));
        
        preferencesArray.add(new preferencesCatItems("STARTUP", rb.getString("MenuStartUp")));
        
        preferencesArray.add(new preferencesCatItems("DISPLAY", rb.getString("MenuDisplay")));
        
        preferencesArray.add(new preferencesCatItems("MESSAGES", rb.getString("MenuMessages")));
        
        preferencesArray.add(new preferencesCatItems("ROSTER", rb.getString("MenuRoster")));
        
        preferencesArray.add(new preferencesCatItems("THROTTLE", rb.getString("MenuThrottle")));
        
        preferencesArray.add(new preferencesCatItems("WITHROTTLE", rb.getString("MenuWiThrottle")));

        preferencesArray.add(new preferencesCatItems("MINISERVER", rb.getString("MenuMiniServer")));

    }
    
    public void init(){
        throttlePreferences = new jmri.jmrit.throttle.ThrottlesPreferencesPane();
        withrottlePrefsPanel = new jmri.jmrit.withrottle.WiThrottlePrefsPanel();
        miniserverPrefsPanel = new jmri.web.miniserver.MiniServerPrefsPanel();
        pref.disallowSave();
        list = new JList();
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));
    
        buttonpanel = new JPanel();
        buttonpanel.setLayout(new BoxLayout(buttonpanel,BoxLayout.Y_AXIS));
        buttonpanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 3));
        buttonpanel.add(listScroller);
        
        detailpanel = new JPanel();
        detailpanel.setLayout(new CardLayout());
        detailpanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 6));
        
        GuiLafConfigPane gui = new GuiLafConfigPane();
        items.add(0, gui);
        
        JButton save = new JButton(rb.getString("ButtonSave"));
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    invokeSaveOptions();
                    throttlePreferences.jbSaveActionPerformed(e);
                    withrottlePrefsPanel.storeValues();
                    miniserverPrefsPanel.storeValues();
                    apps.FileLocationPane.save();
                    savePressed();
                    pref.allowSave();
                }
            });
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        setUpConnectionPanel();
        connectionPanel.setSelectedIndex(0);
        
        addItem("CONNECTIONS", rb.getString("MenuConnections"), null, null, 
                connectionPanel, false, null);
                
        addItem("DEFAULTS",rb.getString("MenuDefaults"), rb.getString("TabbedLayoutDefaults"), rb.getString("LabelTabbedLayoutDefaults"), 
            new apps.ManagerDefaultsConfigPane(), true, null);
        
        addItem("FILELOCATIONS", rb.getString("MenuFileLocation"), rb.getString("TabbedLayoutFileLocations"),
                rb.getString("LabelTabbedFileLocations"), new apps.FileLocationPane(), true, null);
        
        addItem("STARTUP", rb.getString("MenuStartUp"), rb.getString("TabbedLayoutStartupActions"),
            rb.getString("LabelTabbedLayoutStartupActions"), new apps.PerformActionPanel(), true, null);
        addItem("STARTUP", rb.getString("MenuStartUp"), rb.getString("TabbedLayoutCreateButton"),
            rb.getString("LabelTabbedLayoutCreateButton"), new apps.CreateButtonPanel(), true, null);
        addItem("STARTUP", rb.getString("MenuStartUp"), rb.getString("TabbedLayoutStartupFiles"),
                rb.getString("LabelTabbedLayoutStartupFiles"), new apps.PerformFilePanel(), true, null);
        addItem("STARTUP", rb.getString("MenuStartUp"), rb.getString("TabbedLayoutStartupScripts"),
                rb.getString("LabelTabbedLayoutStartupScripts"), new apps.PerformScriptPanel(), true, null);

        addItem("DISPLAY", rb.getString("MenuDisplay"), rb.getString("TabbedLayoutGUI"),
                rb.getString("LabelTabbedLayoutGUI"), gui, true, null);
        addItem("DISPLAY", rb.getString("MenuDisplay"), rb.getString("TabbedLayoutLocale"),
                rb.getString("LabelTabbedLayoutLocale"), gui.doLocale(), false, null);
        addItem("DISPLAY", rb.getString("MenuDisplay"), rb.getString("TabbedLayoutConsole"),
                rb.getString("LabelTabbedLayoutConsole"), new apps.SystemConsoleConfigPanel(), true, null);
        
        addItem("MESSAGES", rb.getString("MenuMessages"), null, null, 
            new jmri.jmrit.beantable.usermessagepreferences.UserMessagePreferencesPane(), false, null);
        
        addItem("ROSTER", rb.getString("MenuRoster"), rb.getString("TabbedLayoutProgrammer"), rb.getString("LabelTabbedLayoutProgrammer"), 
            new jmri.jmrit.symbolicprog.ProgrammerConfigPane(true), true, null);        
        addItem("ROSTER", rb.getString("MenuRoster"), rb.getString("TabbedLayoutRoster"),
            rb.getString("LabelTabbedLayoutRoster"), new jmri.jmrit.roster.RosterConfigPane(), true, null);
        
        addItem("THROTTLE", rb.getString("MenuThrottle"), null, null, 
            throttlePreferences, false, null);
        
        addItem("WITHROTTLE", rb.getString("MenuWiThrottle"), null, null, 
                withrottlePrefsPanel, false, null);

        addItem("MINISERVER", rb.getString("MenuMiniServer"), null, null, 
                miniserverPrefsPanel, false, null);
            
        for(int x=0; x<preferencesArray.size(); x++){
            detailpanel.add(preferencesArray.get(x).getPanel(), preferencesArray.get(x).getPrefItem());
        }

        updateJList();
        add(buttonpanel);
        add(new JSeparator(JSeparator.VERTICAL));
        add(detailpanel);
        
        buttonpanel.add(save);
        list.setSelectedIndex(0);
        selection(preferencesArray.get(0).getPrefItem());
        inital = true;
    }
    
    Hashtable<Class<?>, Object> saveHook = new Hashtable<Class<?>, Object>();
    Hashtable<Class<?>, String> saveMethod = new Hashtable<Class<?>, String>();
    /**
     *  Provides a method for preferences dynamically added to the preference window
     *  to have a method ran when the save button is pressed, thus allowing panes 
     *  to put placed into a state where the information can be saved
     * @param object The instance of the pane
     * @param type The class of the object
     * @param strMethod the method to be invoked at save.
     */
    public <T> void addItemToSave(T object, Class<T> type, String strMethod){
        if (!saveHook.containsKey(type)){
            saveHook.put(type, object);
            saveMethod.put(type, strMethod);
        }
    }
    
    void invokeSaveOptions(){
        Enumeration keys = saveHook.keys();
        while ( keys.hasMoreElements() )
           {
            
            Class<?> strClass = (Class<?>)keys.nextElement();
            String strMethod = saveMethod.get(strClass);
            boolean booMethod = false;
            boolean errorInMethod = false;
            try {
                Method method;
                try {
                    method = strClass.getDeclaredMethod (strMethod);
                    method.invoke(saveHook.get(strClass));
                    booMethod = true;
                } catch (java.lang.reflect.InvocationTargetException et){
                    errorInMethod=true;
                    log.error(et.toString());
                } catch (IllegalAccessException ei){
                    errorInMethod=true;
                    log.error(ei.toString());
                }catch (Exception e){
                    log.error(e.toString());
                    booMethod = false;
                }
                
                if ((!booMethod) && (!errorInMethod)){
                    try {
                        method = strClass.getMethod (strMethod);
                        method.invoke(saveHook.get(strClass));
                        booMethod=true;
                    } catch (java.lang.reflect.InvocationTargetException et){
                        errorInMethod=true;
                        booMethod = false;
                        log.error(et.toString());
                    } catch (IllegalAccessException ei){
                        errorInMethod=true;
                        booMethod = false;
                        log.error(ei.toString());
                    } catch (Exception e){
                        log.error(e.toString());
                        booMethod = false;
                    }
                }
                if (!booMethod){
                    log.error("Unable to save Preferences for " + strClass);
                }
            }
            catch (Exception e) {
                log.error("unable to get a class name" + e);
                log.error("Unable to save Preferences for " + strClass);
            }
        }
    }
    
    void setUpConnectionPanel(){
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

        if (count==0){
            addConnection(0,0);
        }
        connectionPanel.addChangeListener(addTabListener);
        newConnectionTab();
    }
    
    public void setUIFontSize(float size){
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        Font f;
        while (keys.hasMoreElements()) {
          Object key = keys.nextElement();
          Object value = UIManager.get (key);
          
          if (value instanceof javax.swing.plaf.FontUIResource){
            System.out.println(key);
            f = UIManager.getFont(key).deriveFont(Font.PLAIN, size);
            System.out.println(f.getName() + " " + f.getFontName() + " " + f.getFamily());
            UIManager.put (key, f);
          }
        }
    }
    

    public void setUIFont(javax.swing.plaf.FontUIResource f){

    java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = UIManager.get (key);
      if (value instanceof javax.swing.plaf.FontUIResource)
        UIManager.put (key, f);
      }
    }
    
    void selection(String View){
        CardLayout cl = (CardLayout) (detailpanel.getLayout());
        cl.show(detailpanel, View);
    }
    
    public void addItem(String prefItem, String itemText, String tabtitle, String labelKey, JComponent item, boolean store, String tooltip){
        preferencesCatItems itemBeingAdded =null;
        for(int x=0; x<preferencesArray.size(); x++){
            if(preferencesArray.get(x).getPrefItem().equals(prefItem)){
                itemBeingAdded=preferencesArray.get(x);
                break;
            }
        }
        if (itemBeingAdded==null){
            itemBeingAdded = new preferencesCatItems(prefItem, itemText);
            preferencesArray.add(itemBeingAdded);
            //As this is a new item in the selection list, we need to update the JList.
            if (inital)
                updateJList();
        }
        if (tabtitle==null)
            tabtitle = itemText;
        itemBeingAdded.addPreferenceItem(tabtitle, labelKey, item, tooltip, store);
        if (store) items.add(item);
    }
    
    /* Method allows for the preference to goto a specific list item */
    
    public void gotoPreferenceItem(String selection, String subCategory){
        selection(selection);
        list.setSelectedIndex(getCategoryIndexFromString(selection));
        if(subCategory==null || subCategory.equals(""))
            return;
        preferencesArray.get(getCategoryIndexFromString(selection)).gotoSubCategory(subCategory);
    }
    /*
    * Returns a List of existing Preference Categories.
    */
    public List<String> getPreferenceMenuList(){
        ArrayList<String> choices = new ArrayList<String>();
        for(int x=0; x<preferencesArray.size(); x++){
            choices.add(preferencesArray.get(x).getPrefItem());
        }
        return choices;
    }
    
    /*
     * Returns a list of Sub Category Items for a give category
     */
    
    public List<String> getPreferenceSubCategory(String category){
        int index = getCategoryIndexFromString(category);
        return preferencesArray.get(index).getSubCategoriesList();
    }
    
    int getCategoryIndexFromString(String category){
        for(int x=0; x<preferencesArray.size(); x++){
            if(preferencesArray.get(x).getPrefItem().equals(category)){
                return(x);
            }
        }
        return -1;
    }
    
    protected ArrayList<String> getChoices() {
        ArrayList<String> choices = new ArrayList<String>();
        for(int x=0; x<preferencesArray.size(); x++){
            choices.add(preferencesArray.get(x).getItemString());
        }
        return choices;
    }
    
    void updateJList(){
        buttonpanel.remove(listScroller);
        if (list.getListSelectionListeners().length>0){
            list.removeListSelectionListener(list.getListSelectionListeners()[0]);
        }
        list = new JList(new Vector<String>(getChoices()));
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));
        
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e){
                preferencesCatItems item = preferencesArray.get(list.getSelectedIndex());
                selection(item.getPrefItem());
            }
        });
        buttonpanel.add(listScroller);    
    }
    
    void addConnection(int tabPosition, final int instance){
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        
        p.add(JmrixConfigPane.instance(instance));
        p.add(Box.createVerticalGlue());
        JPanel b = new JPanel();
        b.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton deleteButton = new JButton(rb.getString("ButtonDeleteConnection"));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                removeTab(e, null, connectionPanel.getSelectedIndex());
            }
        
        });
        b.add(deleteButton);
        
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
            
            connectionPanel.removeChangeListener(addTabListener);
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
            connectionPanel.addChangeListener(addTabListener);
        }
    }

    transient ChangeListener addTabListener = new ChangeListener() {
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
    };

    class preferencesCatItems {
        
        /* This contains details of all list items to be displayed in the preferences*/
        
        String itemText;
        String prefItem;
        JTabbedPane tabbedPane;
        
        ArrayList<tabDetails> tabDetailsArray = new ArrayList<tabDetails>();
        
        preferencesCatItems(String pref, String title){
            prefItem=pref;
            itemText=title;
        }
        
        void addPreferenceItem(String title, String labelkey, JComponent item, String tooltip, boolean store){
            for(int x=0; x<tabDetailsArray.size(); x++){
                if(tabDetailsArray.get(x).getTitle().equals(title)){
                    //If we have a match then we do not need to add it back in.
                    return;
                }
            }
            tabDetailsArray.add(new tabDetails(labelkey, title,  item, tooltip, store));
        }
        
        String getPrefItem(){
            return prefItem;
        }
        
        String getItemString(){
            return itemText;
        }
        
        ArrayList<String> getSubCategoriesList(){
            ArrayList<String> choices = new ArrayList<String>();
            for(int x=0; x<tabDetailsArray.size(); x++){
                choices.add(tabDetailsArray.get(x).getTitle());
            }
            return choices;
        }
        
        /*This returns a JPanel if only one item is configured for a menu item
        or it returns a JTabbedFrame if there are multiple items for the menu */
        
        JComponent getPanel() {
            if(tabDetailsArray.size()==1){
                return tabDetailsArray.get(0).getPanel();
            } else {
                tabbedPane = new JTabbedPane();
                for (int i = 0; i<tabDetailsArray.size(); i++){
                    tabDetails tab = tabDetailsArray.get(i);
                    tabbedPane.addTab(tab.getTitle(), null, tab.getPanel(), tab.getToolTip());
                }
                return tabbedPane;
            }
        }
        
        void gotoSubCategory(String sub){
            if(tabDetailsArray.size()==1)
                return;
            for (int i = 0; i<tabDetailsArray.size(); i++){
                if (tabDetailsArray.get(i).getTitle().equals(sub)){
                    tabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }
        
        class tabDetails{
        
        /* This contains all the JPanels that make up a preferences menus*/
        
            JComponent tabItem;
            String tabTooltip;
            String tabTitle;
            JPanel tabPanel = new JPanel();
            boolean store;
            
            tabDetails(String labelkey, String tabTit, JComponent item, String tooltip, boolean save){
                tabItem = item;
                tabTitle = tabTit;
                tabTooltip = tooltip;
                store = save;

                JComponent p = new JPanel();
                p.setLayout(new BorderLayout());
                if (labelkey != null) {
                    // insert label at top
                    // As this can be multi-line, embed the text within <html>
                    // tags and replace newlines with <br> tag
                    JLabel t = new JLabel("<html>"+labelkey.replace(String.valueOf('\n'), "<br>")+"</html>");
                    t.setHorizontalAlignment(JLabel.CENTER);
                    t.setAlignmentX(0.5f);
                    t.setPreferredSize(t.getMinimumSize());
                    t.setMaximumSize(t.getMinimumSize());
                    t.setOpaque(false);
                    p.add(t, BorderLayout.NORTH);
                }
                p.add(item, BorderLayout.CENTER);
                tabPanel.setLayout(new BorderLayout());
                tabPanel.add(p, BorderLayout.CENTER);
            }
            
            String getToolTip() { return tabTooltip; }
            
            String getTitle() { return tabTitle; }
            
            JPanel getPanel() { return tabPanel;}
            
            void addToStore() { if(store) items.add(tabItem);}
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TabbedPreferences.class.getName());

}