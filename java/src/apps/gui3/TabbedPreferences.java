// TabbedPreferences.java
package apps.gui3;

import apps.AppConfigBase;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
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
import javax.swing.plaf.FontUIResource;
import jmri.InstanceManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import jmri.swing.PreferencesPanel;
import jmri.swing.PreferencesSubPanel;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to preferences via a tabbed pane.
 *
 * Preferences panels listed in the PreferencesPanel property of the
 * apps.AppsStructureBundle ResourceBundle will be automatically loaded if they
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

    ArrayList<Element> preferencesElements = new ArrayList<>();
    HashMap<String, PreferencesPanel> preferencesPanels = new HashMap<>();

    JPanel detailpanel = new JPanel();
    JTabbedPane connectionPanel = new JTabbedPane();

    ArrayList<JmrixConfigPane> connectionTabInstance = new ArrayList<>();
    ArrayList<PreferencesCatItems> preferencesArray = new ArrayList<>();
    JPanel buttonpanel;
    JList<String> list;
    JButton save;
    JScrollPane listScroller;
    int initalisationState = 0x00;
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

        list = new JList();
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));

        buttonpanel = new JPanel();
        buttonpanel.setLayout(new BoxLayout(buttonpanel, BoxLayout.Y_AXIS));
        buttonpanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 3));

        detailpanel = new JPanel();
        detailpanel.setLayout(new CardLayout());
        detailpanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 6));

        save = new JButton(
                rb.getString("ButtonSave"),
                new ImageIcon(
                        FileUtil.findExternalFilename("program:resources/icons/misc/gui3/SaveIcon.png")));
        save.addActionListener((ActionEvent e) -> {
            savePressed(invokeSaveOptions());
        });

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        setUpConnectionPanel();
        connectionPanel.setSelectedIndex(0);

        addItem("CONNECTIONS", rb.getString("MenuConnections"), null, null,
                connectionPanel, false, null);

        try {
            List<String> classNames = (new ObjectMapper()).readValue(
                    ResourceBundle.getBundle("apps.AppsStructureBundle").getString("PreferencesPanels"),
                    new TypeReference<List<String>>() {
                    });
            for (String className : classNames) {
                try {
                    PreferencesPanel panel = (PreferencesPanel) Class.forName(className).newInstance();
                    if (panel instanceof PreferencesSubPanel) {
                        className = ((PreferencesSubPanel) panel).getParentClassName();
                        if (!this.preferencesPanels.containsKey(className)) {
                            this.addPreferencesPanel((PreferencesPanel) Class.forName(className).newInstance());
                        }
                        ((PreferencesSubPanel) panel).setParent(this.preferencesPanels.get(className));
                    }
                    this.addPreferencesPanel(panel);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    log.error("Unable to add preferences class (" + className + ")", e);
                }
            }
        } catch (Exception e) {
            log.error("Unable to parse PreferencePanels property", e);
        }
        for (PreferencesCatItems preferences : preferencesArray) {
            detailpanel.add(preferences.getPanel(), preferences.getPrefItem());
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

    private boolean invokeSaveOptions() {
        boolean restartRequired = false;
        for (PreferencesPanel panel : this.preferencesPanels.values()) {
            panel.savePreferences();
            if (!restartRequired) {
                restartRequired = panel.isRestartRequired();
            }
        }
        if (!restartRequired) {
            // Since this.preferencesPanels does not include Connections, we
            // loop through the contents of this.connectionTabInstance, to see
            // if any ConnectionConfig indicates a need to restart JMRI.
            for (PreferencesPanel panel : this.connectionTabInstance) {
                restartRequired = panel.isRestartRequired();
                if (restartRequired) {
                    break;
                }
            }
        }
        return restartRequired;
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

    public void addPreferencesPanel(PreferencesPanel panel) {
        this.preferencesPanels.put(panel.getClass().getName(), panel);
        addItem(panel.getPreferencesItem(),
                panel.getPreferencesItemText(),
                panel.getTabbedPreferencesTitle(),
                panel.getLabelKey(),
                panel.getPreferencesComponent(),
                panel.isPersistant(),
                panel.getPreferencesTooltip()
        );
    }

    private void addItem(String prefItem, String itemText, String tabtitle,
            String labelKey, JComponent item, boolean store, String tooltip) {
        PreferencesCatItems itemBeingAdded = null;
        for (PreferencesCatItems preferences : preferencesArray) {
            if (preferences.getPrefItem().equals(prefItem)) {
                itemBeingAdded = preferences;
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
        ArrayList<String> choices = new ArrayList<>();
        for (PreferencesCatItems preferences : preferencesArray) {
            choices.add(preferences.getPrefItem());
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
        ArrayList<String> choices = new ArrayList<>();
        for (PreferencesCatItems preferences : preferencesArray) {
            choices.add(preferences.getItemString());
        }
        return choices;
    }

    void updateJList() {
        buttonpanel.removeAll();
        if (list.getListSelectionListeners().length > 0) {
            list.removeListSelectionListener(list.getListSelectionListeners()[0]);
        }
        list = new JList<String>(new Vector<String>(getChoices()));
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));

        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.addListSelectionListener((ListSelectionEvent e) -> {
            PreferencesCatItems item = preferencesArray.get(list.getSelectedIndex());
            selection(item.getPrefItem());
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
        disable.addActionListener((ActionEvent e) -> {
            configPane.setDisabled(disable.isSelected());
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

        tabCloseButton.addActionListener((ActionEvent e) -> {
            removeTab(e, connectionPanel.indexOfTabComponent(tabTitle));
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

        /**
         *
         */
        private static final long serialVersionUID = 5928584215129175250L;
        /*
         * This contains details of all list items to be displayed in the
         * preferences
         */
        String itemText;
        String prefItem;
        JTabbedPane tabbedPane = new JTabbedPane();
        ArrayList<String> disableItemsList = new ArrayList<>();

        ArrayList<TabDetails> TabDetailsArray = new ArrayList<>();

        PreferencesCatItems(String pref, String title) {
            prefItem = pref;
            itemText = title;
        }

        void addPreferenceItem(String title, String labelkey, JComponent item,
                String tooltip) {
            for (TabDetails tabDetails : TabDetailsArray) {
                if (tabDetails.getTitle().equals(title)) {
                    // If we have a match then we do not need to add it back in.
                    return;
                }
            }
            TabDetails tab = new TabDetails(labelkey, title, item, tooltip);
            TabDetailsArray.add(tab);
            tabbedPane.addTab(tab.getTitle(), null, tab.getPanel(),
                    tab.getToolTip());

            for (String disableItem : disableItemsList) {
                if (item.getClass().getName().equals(disableItem)) {
                    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(tab.getTitle()), false);
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
            ArrayList<String> choices = new ArrayList<>();
            for (TabDetails tabDetails : TabDetailsArray) {
                choices.add(tabDetails.getTitle());
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

            /**
             *
             */
            private static final long serialVersionUID = -7077354592762639878L;
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
