// TabbedPreferences.java
package apps.gui3;

import apps.AppConfigBase;
import apps.CreateButtonPanel;
import apps.FileLocationPane;
import apps.GuiLafConfigPane;
import apps.ManagerDefaultsConfigPane;
import apps.PerformActionPanel;
import apps.PerformFilePanel;
import apps.PerformScriptPanel;
import apps.SystemConsoleConfigPanel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import jmri.InstanceManager;
import jmri.jmrit.beantable.usermessagepreferences.UserMessagePreferencesPane;
import jmri.jmrit.roster.RosterConfigPane;
import jmri.jmrit.symbolicprog.ProgrammerConfigPane;
import jmri.jmrit.throttle.ThrottlesPreferencesPane;
import jmri.jmrit.withrottle.WiThrottlePrefsPanel;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import jmri.swing.PreferencesPanel;
import jmri.util.FileUtil;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to preferences via a tabbed pane
 *
 * Preferences panels listed in the PreferencesPanel property of the
 * AppsConfigBundle ResourceBundle will be automatically loaded if they
 * implement the {@link jmri.swing.PreferencesPanel} interface.
 *
 * Other Preferences Panels will need to be manually added to this file in a
 * manner similar to the WiThrottlePrefsPanel.
 *
 * @author Bob Jacobsen Copyright 2010
 * @author Randall Wood 2012
 * @version $Revision$
 */
public class TabbedPreferences extends AppConfigBase {

    @Override
    public String getHelpTarget() {
        return "package.apps.TabbedPreferences";
    }

    @Override
    public String getTitle() {
        return rb.getString("TitlePreferences");
    }

    @Override
    public boolean isMultipleInstances() {
        return false;
    } // only one of these!

    ArrayList<Element> preferencesElements = new ArrayList<Element>();
    HashMap<String, PreferencesPanel> preferencesPanels = new HashMap<String, PreferencesPanel>();

    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?
    JPanel detailpanel = new JPanel();
    JTabbedPane connectionPanel = new JTabbedPane();
    ThrottlesPreferencesPane throttlePreferences;
    WiThrottlePrefsPanel withrottlePrefsPanel;

    ArrayList<JmrixConfigPane> connectionTabInstance = new ArrayList<JmrixConfigPane>();
    ArrayList<PreferencesCatItems> preferencesArray = new ArrayList<PreferencesCatItems>();
    JPanel buttonpanel;
    @SuppressWarnings("rawtypes")
    // IDEs in Java 1.7 warn about this, IDEs in Java 1.6 don't.
    JList list; // Java 1.7 changes JList to a generic (JList<String> in this
    // case)
    JButton save;
    JScrollPane listScroller;
    int initalisationState = 0x00;
    Hashtable<Class<?>, Object> saveHook = new Hashtable<Class<?>, Object>();
    Hashtable<Class<?>, String> saveMethod = new Hashtable<Class<?>, String>();
    private static final long serialVersionUID = -6266891995866315885L;

    static final int UNINITIALISED = 0x00;
    static final int INITIALISING = 0x01;
    static final int INITIALISED = 0x02;

    public TabbedPreferences() {

        /*
         * Adds the place holders for the menu items so that any items add by
         * third party code is added to the end
         */
        preferencesArray.add(new PreferencesCatItems("CONNECTIONS", rb
                .getString("MenuConnections")));

        preferencesArray.add(new PreferencesCatItems("DEFAULTS", rb
                .getString("MenuDefaults")));

        preferencesArray.add(new PreferencesCatItems("FILELOCATIONS", rb
                .getString("MenuFileLocation")));

        preferencesArray.add(new PreferencesCatItems("STARTUP", rb
                .getString("MenuStartUp")));

        preferencesArray.add(new PreferencesCatItems("DISPLAY", rb
                .getString("MenuDisplay")));

        preferencesArray.add(new PreferencesCatItems("MESSAGES", rb
                .getString("MenuMessages")));

        preferencesArray.add(new PreferencesCatItems("ROSTER", rb
                .getString("MenuRoster")));

        preferencesArray.add(new PreferencesCatItems("THROTTLE", rb
                .getString("MenuThrottle")));

        preferencesArray.add(new PreferencesCatItems("WITHROTTLE", rb
                .getString("MenuWiThrottle")));
    }

    @SuppressWarnings("rawtypes")
    public synchronized int init() {
        if (initalisationState == INITIALISED) {
            return INITIALISED;
        }
        if (initalisationState != UNINITIALISED) {
            return initalisationState;
        }
        initalisationState = INITIALISING;

        deleteConnectionIconRollOver = new ImageIcon(
                FileUtil.findExternalFilename("program:resources/icons/misc/gui3/Delete16x16.png"));
        deleteConnectionIcon = new ImageIcon(
                FileUtil.findExternalFilename("program:resources/icons/misc/gui3/Delete-bw16x16.png"));
        deleteConnectionButtonSize = new Dimension(
                deleteConnectionIcon.getIconWidth() + 2,
                deleteConnectionIcon.getIconHeight() + 2);
        addConnectionIcon = new ImageIcon(
                FileUtil.findExternalFilename("program:resources/icons/misc/gui3/Add16x16.png"));

        throttlePreferences = new ThrottlesPreferencesPane();
        withrottlePrefsPanel = new WiThrottlePrefsPanel();
        list = new JList();
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));

        buttonpanel = new JPanel();
        buttonpanel.setLayout(new BoxLayout(buttonpanel, BoxLayout.Y_AXIS));
        buttonpanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 3));

        detailpanel = new JPanel();
        detailpanel.setLayout(new CardLayout());
        detailpanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 6));

        GuiLafConfigPane gui = new GuiLafConfigPane();
        items.add(0, gui);

        save = new JButton(
                rb.getString("ButtonSave"),
                new ImageIcon(
                        FileUtil.findExternalFilename("program:resources/icons/misc/gui3/SaveIcon.png")));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                invokeSaveOptions();
                throttlePreferences.jbSaveActionPerformed(e);
                withrottlePrefsPanel.storeValues();
                FileLocationPane.save();
                savePressed();
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        setUpConnectionPanel();
        connectionPanel.setSelectedIndex(0);

        addItem("CONNECTIONS", rb.getString("MenuConnections"), null, null,
                connectionPanel, false, null);

        try {
            addItem("DEFAULTS", rb.getString("MenuDefaults"),
                    rb.getString("TabbedLayoutDefaults"),
                    rb.getString("LabelTabbedLayoutDefaults"),
                    new ManagerDefaultsConfigPane(), true, null);
        } catch (Exception ex) {
            log.error("Error in trying to add defaults to the preferences "
                    + ex.toString());
        }
        try {
            addItem("FILELOCATIONS", rb.getString("MenuFileLocation"),
                    rb.getString("TabbedLayoutFileLocations"),
                    rb.getString("LabelTabbedFileLocations"),
                    new FileLocationPane(), true, null);
        } catch (Exception ex) {
            log.error("Error in trying to add the file locations to the preferences "
                    + ex.toString());
        }
        try {
            addItem("STARTUP", rb.getString("MenuStartUp"),
                    rb.getString("TabbedLayoutStartupActions"),
                    rb.getString("LabelTabbedLayoutStartupActions"),
                    new PerformActionPanel(), true, null);
            addItem("STARTUP", rb.getString("MenuStartUp"),
                    rb.getString("TabbedLayoutCreateButton"),
                    rb.getString("LabelTabbedLayoutCreateButton"),
                    new CreateButtonPanel(), true, null);
            addItem("STARTUP", rb.getString("MenuStartUp"),
                    rb.getString("TabbedLayoutStartupFiles"),
                    rb.getString("LabelTabbedLayoutStartupFiles"),
                    new PerformFilePanel(), true, null);
            addItem("STARTUP", rb.getString("MenuStartUp"),
                    rb.getString("TabbedLayoutStartupScripts"),
                    rb.getString("LabelTabbedLayoutStartupScripts"),
                    new PerformScriptPanel(), true, null);
        } catch (Exception ex) {
            log.error("Error in trying to add the startup items to the preferences "
                    + ex.toString());
        }
        try {
            addItem("DISPLAY", rb.getString("MenuDisplay"),
                    rb.getString("TabbedLayoutGUI"),
                    rb.getString("LabelTabbedLayoutGUI"), gui, true, null);
            addItem("DISPLAY", rb.getString("MenuDisplay"),
                    rb.getString("TabbedLayoutLocale"),
                    rb.getString("LabelTabbedLayoutLocale"), gui.doLocale(),
                    false, null);
            addItem("DISPLAY", rb.getString("MenuDisplay"),
                    rb.getString("TabbedLayoutConsole"),
                    rb.getString("LabelTabbedLayoutConsole"),
                    new SystemConsoleConfigPanel(), true, null);
        } catch (Exception ex) {
            log.error("Error in trying to add display config to the preferences "
                    + ex.toString());
        }
        try {
            addItem("MESSAGES", rb.getString("MenuMessages"), null, null,
                    new UserMessagePreferencesPane(), false, null);
        } catch (Exception ex) {
            log.error("Error in trying to add message items to the preferences "
                    + ex.toString());
        }
        try {
            addItem("ROSTER", rb.getString("MenuRoster"),
                    rb.getString("TabbedLayoutProgrammer"),
                    rb.getString("LabelTabbedLayoutProgrammer"),
                    new ProgrammerConfigPane(true), true, null);
            addItem("ROSTER", rb.getString("MenuRoster"),
                    rb.getString("TabbedLayoutRoster"),
                    rb.getString("LabelTabbedLayoutRoster"),
                    new RosterConfigPane(), true, null);
        } catch (Exception ex) {
            log.error("Error in trying to add roster preferemce "
                    + ex.toString());
        }
        try {
            addItem("THROTTLE", rb.getString("MenuThrottle"), null, null,
                    throttlePreferences, false, null);
        } catch (Exception ex) {
            log.error("Error in trying to add throttle preferences "
                    + ex.toString());
        }
        try {
            addItem("WITHROTTLE", rb.getString("MenuWiThrottle"), null, null,
                    withrottlePrefsPanel, false, null);
        } catch (Exception ex) {
            log.error("Error in trying to add WiThrottle preferences "
                    + ex.toString());
        }
        try {
            // need to replace this mechanism with some mechanism that relies on a
            // cached discovery mechanism for plugins like @ http://code.google.com/p/jspf/
            List<String> classNames = (new ObjectMapper()).readValue(
                    java.util.ResourceBundle.getBundle("apps.AppsStructureBundle").getString("PreferencesPanels"),
                    new TypeReference<List<String>>() {
                    });
            for (String className : classNames) {
                try {
                    PreferencesPanel panel = (PreferencesPanel) Class.forName(
                            className).newInstance();
                    this.preferencesPanels.put(className, panel);
                    addItem(panel.getPreferencesItem(),
                            panel.getPreferencesItemText(),
                            panel.getTabbedPreferencesTitle(),
                            panel.getLabelKey(),
                            panel.getPreferencesComponent(),
                            panel.isPersistant(), panel.getPreferencesTooltip());
                } catch (Exception e) {
                    log.error("Unable to add preferences class (" + className
                            + ")", e);
                }
            }
        } catch (Exception e) {
            log.error("Unable to parse PreferencePanels property", e);
        }
        for (int x = 0; x < preferencesArray.size(); x++) {
            detailpanel.add(preferencesArray.get(x).getPanel(),
                    preferencesArray.get(x).getPrefItem());
        }

        updateJList();
        add(buttonpanel);
        add(new JSeparator(JSeparator.VERTICAL));
        add(detailpanel);

        list.setSelectedIndex(0);
        selection(preferencesArray.get(0).getPrefItem());
        initalisationState = INITIALISED;
        return initalisationState;
    }

    /**
     * Provides a method for preferences dynamically added to the preference
     * window to have a method ran when the save button is pressed, thus
     * allowing panes to put placed into a state where the information can be
     * saved
     *
     * @param <T> The class of the object
     * @param object The instance of the pane
     * @param type The class of the object
     * @param strMethod the method to be invoked at save.
     */
    public <T> void addItemToSave(T object, Class<T> type, String strMethod) {
        if (!saveHook.containsKey(type)) {
            saveHook.put(type, object);
            saveMethod.put(type, strMethod);
        }
    }

    void invokeSaveOptions() {
        Enumeration<Class<?>> keys = saveHook.keys();
        while (keys.hasMoreElements()) {

            Class<?> strClass = keys.nextElement();
            String strMethod = saveMethod.get(strClass);
            boolean booMethod = false;
            boolean errorInMethod = false;
            try {
                Method method;
                try {
                    method = strClass.getDeclaredMethod(strMethod);
                    method.invoke(saveHook.get(strClass));
                    booMethod = true;
                } catch (InvocationTargetException et) {
                    errorInMethod = true;
                    log.error(et.toString());
                } catch (IllegalAccessException ei) {
                    errorInMethod = true;
                    log.error(ei.toString());
                } catch (Exception e) {
                    log.error(e.toString());
                    booMethod = false;
                }

                if ((!booMethod) && (!errorInMethod)) {
                    try {
                        method = strClass.getMethod(strMethod);
                        method.invoke(saveHook.get(strClass));
                        booMethod = true;
                    } catch (InvocationTargetException et) {
                        errorInMethod = true;
                        booMethod = false;
                        log.error(et.toString());
                    } catch (IllegalAccessException ei) {
                        errorInMethod = true;
                        booMethod = false;
                        log.error(ei.toString());
                    } catch (Exception e) {
                        log.error(e.toString());
                        booMethod = false;
                    }
                }
                if (!booMethod) {
                    log.error("Unable to save Preferences for " + strClass);
                }
            } catch (Exception e) {
                log.error("unable to get a class name" + e);
                log.error("Unable to save Preferences for " + strClass);
            }
        }
        for (PreferencesPanel panel : this.preferencesPanels.values()) {
            if (!panel.isPersistant()) {
                panel.savePreferences();
            }
        }
    }

    void setUpConnectionPanel() {
        ArrayList<Object> connList = InstanceManager.configureManagerInstance()
                .getInstanceList(ConnectionConfig.class);
        if (connList != null) {
            for (int x = 0; x < connList.size(); x++) {
                JmrixConfigPane configPane = JmrixConfigPane.instance(x);
                addConnection(x, configPane);
            }
        } else {
            addConnection(0, JmrixConfigPane.createNewPanel());
        }
        connectionPanel.addChangeListener(addTabListener);
        newConnectionTab();
    }

    public void setUIFontSize(float size) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        Font f;
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                f = UIManager.getFont(key).deriveFont(((Font) value).getStyle(), size);
                UIManager.put(key, f);
            }
        }
    }

    public void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    void selection(String View) {
        CardLayout cl = (CardLayout) (detailpanel.getLayout());
        cl.show(detailpanel, View);
    }

    public void addItem(String prefItem, String itemText, String tabtitle,
            String labelKey, JComponent item, boolean store, String tooltip) {
        PreferencesCatItems itemBeingAdded = null;
        for (int x = 0; x < preferencesArray.size(); x++) {
            if (preferencesArray.get(x).getPrefItem().equals(prefItem)) {
                itemBeingAdded = preferencesArray.get(x);
                break;
            }
        }
        if (itemBeingAdded == null) {
            itemBeingAdded = new PreferencesCatItems(prefItem, itemText);
            preferencesArray.add(itemBeingAdded);
            // As this is a new item in the selection list, we need to update
            // the JList.
            if (initalisationState == INITIALISED) {
                updateJList();
            }
        }
        if (tabtitle == null) {
            tabtitle = itemText;
        }
        itemBeingAdded.addPreferenceItem(tabtitle, labelKey, item, tooltip);
        if (store) {
            items.add(item);
        }
    }

    /* Method allows for the preference to goto a specific list item */
    public void gotoPreferenceItem(String selection, String subCategory) {
        selection(selection);
        list.setSelectedIndex(getCategoryIndexFromString(selection));
        if (subCategory == null || subCategory.equals("")) {
            return;
        }
        preferencesArray.get(getCategoryIndexFromString(selection))
                .gotoSubCategory(subCategory);
    }

    /*
     * Returns a List of existing Preference Categories.
     */
    public List<String> getPreferenceMenuList() {
        ArrayList<String> choices = new ArrayList<String>();
        for (int x = 0; x < preferencesArray.size(); x++) {
            choices.add(preferencesArray.get(x).getPrefItem());
        }
        return choices;
    }

    /*
     * Returns a list of Sub Category Items for a give category
     */
    public List<String> getPreferenceSubCategory(String category) {
        int index = getCategoryIndexFromString(category);
        return preferencesArray.get(index).getSubCategoriesList();
    }

    int getCategoryIndexFromString(String category) {
        for (int x = 0; x < preferencesArray.size(); x++) {
            if (preferencesArray.get(x).getPrefItem().equals(category)) {
                return (x);
            }
        }
        return -1;
    }

    public void disablePreferenceItem(String selection, String subCategory) {
        if (subCategory == null || subCategory.equals("")) {
            // need to do something here like just disable the item

        } else {
            preferencesArray.get(getCategoryIndexFromString(selection))
                    .disableSubCategory(subCategory);
        }
    }

    protected ArrayList<String> getChoices() {
        ArrayList<String> choices = new ArrayList<String>();
        for (int x = 0; x < preferencesArray.size(); x++) {
            choices.add(preferencesArray.get(x).getItemString());
        }
        return choices;
    }

    void updateJList() {
        buttonpanel.removeAll();
        if (list.getListSelectionListeners().length > 0) {
            list.removeListSelectionListener(list.getListSelectionListeners()[0]);
        }
        list = new JList(new Vector<String>(getChoices()));
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));

        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                PreferencesCatItems item = preferencesArray.get(list
                        .getSelectedIndex());
                selection(item.getPrefItem());
            }
        });
        buttonpanel.add(listScroller);
        buttonpanel.add(save);
    }

    void addConnection(int tabPosition, final JmrixConfigPane configPane) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(configPane, BorderLayout.CENTER);

        JButton tabCloseButton = new JButton(deleteConnectionIcon);
        tabCloseButton.setPreferredSize(deleteConnectionButtonSize);
        tabCloseButton.setBorderPainted(false);
        tabCloseButton.setRolloverIcon(deleteConnectionIconRollOver);
        tabCloseButton.setVisible(false);

        JPanel c = new JPanel();
        c.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        final JCheckBox disable = new JCheckBox("Disable Connection");
        disable.setSelected(configPane.getDisabled());
        disable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                configPane.setDisabled(disable.isSelected());
            }
        });
        c.add(disable);
        p.add(c, BorderLayout.SOUTH);
        String title;

        if (configPane.getConnectionName() != null) {
            title = configPane.getConnectionName();
        } else if ((configPane.getCurrentProtocolName() != null)
                && (!configPane.getCurrentProtocolName().equals(
                        JmrixConfigPane.NONE))) {
            title = configPane.getCurrentProtocolName();
        } else {
            title = rb.getString("TabbedLayoutConnection") + (tabPosition + 1);
            if (connectionPanel.indexOfTab(title) != -1) {
                for (int x = 2; x < 12; x++) {
                    title = rb.getString("TabbedLayoutConnection")
                            + (tabPosition + 2);
                    if (connectionPanel.indexOfTab(title) != -1) {
                        break;
                    }
                }
            }
        }

        final JPanel tabTitle = new JPanel(new BorderLayout(5, 0));
        tabTitle.setOpaque(false);
        p.setName(title);

        if (configPane.getDisabled()) {
            title = "(" + title + ")";
        }

        JLabel tabLabel = new JLabel(title, JLabel.LEFT);
        tabTitle.add(tabLabel, BorderLayout.WEST);
        tabTitle.add(tabCloseButton, BorderLayout.EAST);

        connectionTabInstance.add(configPane);
        connectionPanel.add(p);
        connectionPanel.setTabComponentAt(tabPosition, tabTitle);

        tabCloseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeTab(e, connectionPanel.indexOfTabComponent(tabTitle));
            }
        });

        connectionPanel.setToolTipTextAt(tabPosition, title);

        if (ConnectionStatus.instance().isConnectionOk(
                configPane.getCurrentProtocolInfo())) {
            tabLabel.setForeground(Color.black);
        } else {
            tabLabel.setForeground(Color.red);
        }
        if (configPane.getDisabled()) {
            tabLabel.setForeground(Color.ORANGE);
        }

        items.add(configPane);
    }

    void addConnectionTab() {
        connectionPanel.removeTabAt(connectionPanel
                .indexOfTab(addConnectionIcon));
        addConnection(connectionTabInstance.size(),
                JmrixConfigPane.createNewPanel());
        newConnectionTab();
    }

    void newConnectionTab() {
        connectionPanel.addTab(null, addConnectionIcon, null,
                rb.getString("ToolTipAddNewConnection"));
        connectionPanel.setSelectedIndex(connectionPanel.getTabCount() - 2);
    }

    JmrixConfigPane comm1;
    GuiLafConfigPane guiPrefs;

    private void removeTab(ActionEvent e, int x) {
        int i;

        i = x;

        if (i != -1) {
            int n = JOptionPane.showConfirmDialog(null, MessageFormat.format(
                    rb.getString("MessageDoDelete"),
                    new Object[]{connectionPanel.getTitleAt(i)}),
                    rb.getString("MessageDeleteConnection"),
                    JOptionPane.YES_NO_OPTION);
            if (n != JOptionPane.YES_OPTION) {
                return;
            }

            JmrixConfigPane configPane = connectionTabInstance.get(i);

            connectionPanel.removeChangeListener(addTabListener);
            connectionPanel.remove(i); // was x
            items.remove(configPane);
            try {
                JmrixConfigPane.dispose(configPane);
            } catch (NullPointerException ex) {
                log.error("Caught Null Pointer Exception while removing connection tab");
            }
            connectionTabInstance.remove(i);
            if (connectionPanel.getTabCount() == 1) {
                addConnectionTab();
            }
            if (x != 0) {
                connectionPanel.setSelectedIndex(x - 1);
            } else {
                connectionPanel.setSelectedIndex(0);
            }
            connectionPanel.addChangeListener(addTabListener);
        }
        activeTab();
    }

    transient ChangeListener addTabListener = new ChangeListener() {
        // This method is called whenever the selected tab changes
        @Override
        public void stateChanged(ChangeEvent evt) {
            JTabbedPane pane = (JTabbedPane) evt.getSource();
            int sel = pane.getSelectedIndex();
            if (sel == -1) {
                addConnectionTab();
                return;
            } else {
                Icon icon = pane.getIconAt(sel);
                if (icon == addConnectionIcon) {
                    addConnectionTab();
                    return;
                }
            }
            activeTab();
        }
    };

    void activeTab() {
        int sel = connectionPanel.getSelectedIndex();
        for (int i = 0; i < connectionPanel.getTabCount() - 1; i++) {
            JPanel panel = (JPanel) connectionPanel.getTabComponentAt(i);
            panel.invalidate();
            Component[] comp = panel.getComponents();
            for (Component c : comp) {
                if (c instanceof JButton) {
                    if (i == sel) {
                        c.setVisible(true);
                    } else {
                        c.setVisible(false);
                    }
                }
            }
        }
    }

    private ImageIcon deleteConnectionIcon;
    private ImageIcon deleteConnectionIconRollOver;
    private Dimension deleteConnectionButtonSize;
    private ImageIcon addConnectionIcon;

    static class PreferencesCatItems implements java.io.Serializable {

        /*
         * This contains details of all list items to be displayed in the
         * preferences
         */
        String itemText;
        String prefItem;
        JTabbedPane tabbedPane = new JTabbedPane();
        ArrayList<String> disableItemsList = new ArrayList<String>();

        ArrayList<TabDetails> TabDetailsArray = new ArrayList<TabDetails>();

        PreferencesCatItems(String pref, String title) {
            prefItem = pref;
            itemText = title;
        }

        void addPreferenceItem(String title, String labelkey, JComponent item,
                String tooltip) {
            for (int x = 0; x < TabDetailsArray.size(); x++) {
                if (TabDetailsArray.get(x).getTitle().equals(title)) {
                    // If we have a match then we do not need to add it back in.
                    return;
                }
            }
            TabDetails tab = new TabDetails(labelkey, title, item, tooltip);
            TabDetailsArray.add(tab);
            tabbedPane.addTab(tab.getTitle(), null, tab.getPanel(),
                    tab.getToolTip());

            for (int i = 0; i < disableItemsList.size(); i++) {
                if (item.getClass().getName().equals(disableItemsList.get(i))) {
                    tabbedPane.setEnabledAt(
                            tabbedPane.indexOfTab(tab.getTitle()), false);
                    return;
                }
            }
        }

        String getPrefItem() {
            return prefItem;
        }

        String getItemString() {
            return itemText;
        }

        ArrayList<String> getSubCategoriesList() {
            ArrayList<String> choices = new ArrayList<String>();
            for (int x = 0; x < TabDetailsArray.size(); x++) {
                choices.add(TabDetailsArray.get(x).getTitle());
            }
            return choices;
        }

        /*
         * This returns a JPanel if only one item is configured for a menu item
         * or it returns a JTabbedFrame if there are multiple items for the menu
         */
        JComponent getPanel() {
            if (TabDetailsArray.size() == 1) {
                return TabDetailsArray.get(0).getPanel();
            } else {
                return tabbedPane;
            }
        }

        void gotoSubCategory(String sub) {
            if (TabDetailsArray.size() == 1) {
                return;
            }
            for (int i = 0; i < TabDetailsArray.size(); i++) {
                if (TabDetailsArray.get(i).getTitle().equals(sub)) {
                    tabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }

        void disableSubCategory(String sub) {
            if (TabDetailsArray.isEmpty()) {
                // So the tab preferences might not have been initialised when
                // the call to disable an item is called therefore store it for
                // later on
                disableItemsList.add(sub);
                return;
            }
            for (int i = 0; i < TabDetailsArray.size(); i++) {
                if ((TabDetailsArray.get(i).getItem()).getClass().getName()
                        .equals(sub)) {
                    tabbedPane.setEnabledAt(i, false);
                    return;
                }
            }
        }

        static class TabDetails implements java.io.Serializable {

            /* This contains all the JPanels that make up a preferences menus */
            JComponent tabItem;
            String tabTooltip;
            String tabTitle;
            JPanel tabPanel = new JPanel();
            //boolean store;

            TabDetails(String labelkey, String tabTit, JComponent item,
                    String tooltip) {
                tabItem = item;
                tabTitle = tabTit;
                tabTooltip = tooltip;

                JComponent p = new JPanel();
                p.setLayout(new BorderLayout());
                if (labelkey != null) {
                    // insert label at top
                    // As this can be multi-line, embed the text within <html>
                    // tags and replace newlines with <br> tag
                    JLabel t = new JLabel("<html>"
                            + labelkey.replace(String.valueOf('\n'), "<br>")
                            + "</html>");
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

            String getToolTip() {
                return tabTooltip;
            }

            String getTitle() {
                return tabTitle;
            }

            JPanel getPanel() {
                return tabPanel;
            }

            JComponent getItem() {
                return tabItem;
            }

        }
    }

    static Logger log = LoggerFactory.getLogger(TabbedPreferences.class.getName());

}
