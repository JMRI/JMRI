package apps.gui3.tabbedpreferences;

import apps.AppConfigBase;
import apps.ConfigBundle;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.swing.PreferencesPanel;
import jmri.swing.PreferencesSubPanel;
import jmri.util.FileUtil;
import jmri.util.ThreadingUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to preferences via a tabbed pane.
 *
 * Preferences panels provided by a {@link java.util.ServiceLoader} will be
 * automatically loaded if they implement the
 * {@link jmri.swing.PreferencesPanel} interface.
 * <p>
 * JMRI apps (generally) create one object of this type on the main thread as
 * part of initialization, which is then made available via the 
 * {@link InstanceManager}.
 *
 * @author Bob Jacobsen Copyright 2010, 2019
 * @author Randall Wood 2012, 2016
 */
public class TabbedPreferences extends AppConfigBase {

    @Override
    public String getHelpTarget() {
        return "package.apps.TabbedPreferences";
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("TitlePreferences");
    }
    // Preferences Window Title

    @Override
    public boolean isMultipleInstances() {
        return false;
    } // only one of these!

    ArrayList<Element> preferencesElements = new ArrayList<>();

    JPanel detailpanel = new JPanel();
    { 
        // The default panel needs to have a CardLayout
        detailpanel.setLayout(new CardLayout());
    }

    ArrayList<PreferencesCatItems> preferencesArray = new ArrayList<>();
    JPanel buttonpanel;
    JList<String> list;
    JButton save;
    JScrollPane listScroller;

    public TabbedPreferences() {

        /*
         * Adds the place holders for the menu managedPreferences so that any managedPreferences add by
         * third party code is added to the end
         */
        preferencesArray.add(new PreferencesCatItems("CONNECTIONS", rb
                .getString("MenuConnections"), 100));

        preferencesArray.add(new PreferencesCatItems("DEFAULTS", rb
                .getString("MenuDefaults"), 200));

        preferencesArray.add(new PreferencesCatItems("FILELOCATIONS", rb
                .getString("MenuFileLocation"), 300));

        preferencesArray.add(new PreferencesCatItems("STARTUP", rb
                .getString("MenuStartUp"), 400));

        preferencesArray.add(new PreferencesCatItems("DISPLAY", rb
                .getString("MenuDisplay"), 500));

        preferencesArray.add(new PreferencesCatItems("MESSAGES", rb
                .getString("MenuMessages"), 600));

        preferencesArray.add(new PreferencesCatItems("ROSTER", rb
                .getString("MenuRoster"), 700));

        preferencesArray.add(new PreferencesCatItems("THROTTLE", rb
                .getString("MenuThrottle"), 800));

        preferencesArray.add(new PreferencesCatItems("WITHROTTLE", rb
                .getString("MenuWiThrottle"), 900));
                
        // initialization process via init
        init();
    }

    /**
     * Initialize, including loading classes provided by a
     * {@link java.util.ServiceLoader}.
     * <p>
     * This creates a thread which creates items, then
     * invokes the GUI thread to add them in.
     */
    private void init() {
        list = new JList<>();
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(100, 100));

        buttonpanel = new JPanel();
        buttonpanel.setLayout(new BoxLayout(buttonpanel, BoxLayout.Y_AXIS));
        buttonpanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 3));

        detailpanel = new JPanel();
        detailpanel.setLayout(new CardLayout());
        detailpanel.setBorder(BorderFactory.createEmptyBorder(6, 3, 6, 6));

        save = new JButton(
                ConfigBundle.getMessage("ButtonSave"),
                new ImageIcon(FileUtil.findURL("program:resources/icons/misc/gui3/SaveIcon.png", FileUtil.Location.INSTALLED)));
        save.addActionListener((ActionEvent e) -> {
            savePressed(invokeSaveOptions());
        });

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        // panels that are dependent upon another panel being added first
        Set<PreferencesPanel> delayed = new HashSet<>();

        // add preference panels registered with the Instance Manager
        for (PreferencesPanel panel : InstanceManager.getList(jmri.swing.PreferencesPanel.class)) {
            if (panel instanceof PreferencesSubPanel) {
                String parent = ((PreferencesSubPanel) panel).getParentClassName();
                if (!this.getPreferencesPanels().containsKey(parent)) {
                    delayed.add(panel);
                } else {
                    ((PreferencesSubPanel) panel).setParent(this.getPreferencesPanels().get(parent));
                }
            }
            if (!delayed.contains(panel)) {
                this.addPreferencesPanel(panel);
            }
        }

        for (PreferencesPanel panel : ServiceLoader.load(PreferencesPanel.class)) {
            if (panel instanceof PreferencesSubPanel) {
                String parent = ((PreferencesSubPanel) panel).getParentClassName();
                if (!this.getPreferencesPanels().containsKey(parent)) {
                    delayed.add(panel);
                } else {
                    ((PreferencesSubPanel) panel).setParent(this.getPreferencesPanels().get(parent));
                }
            }
            if (!delayed.contains(panel)) {
                this.addPreferencesPanel(panel);
            }
        }
        while (!delayed.isEmpty()) {
            Set<PreferencesPanel> iterated = new HashSet<>(delayed);
            iterated.stream().filter((panel) -> (panel instanceof PreferencesSubPanel)).forEach((panel) -> {
                String parent = ((PreferencesSubPanel) panel).getParentClassName();
                if (this.getPreferencesPanels().containsKey(parent)) {
                    ((PreferencesSubPanel) panel).setParent(this.getPreferencesPanels().get(parent));
                    delayed.remove(panel);
                    this.addPreferencesPanel(panel);
                }
            });
        }
        preferencesArray.stream().forEach((preferences) -> {
            detailpanel.add(preferences.getPanel(), preferences.getPrefItem());
        });
        preferencesArray.sort((PreferencesCatItems o1, PreferencesCatItems o2) -> {
            int comparison = Integer.compare(o1.sortOrder, o2.sortOrder);
            return (comparison != 0) ? comparison : o1.getPrefItem().compareTo(o2.getPrefItem());
        });

        updateJList();
        add(buttonpanel);
        add(new JSeparator(JSeparator.VERTICAL));
        add(detailpanel);

        list.setSelectedIndex(0);
        selection(preferencesArray.get(0).getPrefItem());
    }

    // package only - for TabbedPreferencesFrame
    boolean isDirty() {
        // if not for the debug statements, this method could be the one line:
        // return this.getPreferencesPanels().values.stream().anyMatch((panel) -> (panel.isDirty()));
        return this.getPreferencesPanels().values().stream().map((panel) -> {
            // wrapped in isDebugEnabled test to prevent overhead of assembling message
            if (log.isDebugEnabled()) {
                log.debug("PreferencesPanel {} ({}) is {}.",
                        panel.getClass().getName(),
                        (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                        (panel.isDirty()) ? "dirty" : "clean");
            }
            return panel;
        }).anyMatch((panel) -> (panel.isDirty()));
    }

    // package only - for TabbedPreferencesFrame
    boolean invokeSaveOptions() {
        boolean restartRequired = false;
        for (PreferencesPanel panel : this.getPreferencesPanels().values()) {
            // wrapped in isDebugEnabled test to prevent overhead of assembling message
            if (log.isDebugEnabled()) {
                log.debug("PreferencesPanel {} ({}) is {}.",
                        panel.getClass().getName(),
                        (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                        (panel.isDirty()) ? "dirty" : "clean");
            }
            panel.savePreferences();
            // wrapped in isDebugEnabled test to prevent overhead of assembling message
            if (log.isDebugEnabled()) {
                log.debug("PreferencesPanel {} ({}) restart is {}required.",
                        panel.getClass().getName(),
                        (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                        (panel.isRestartRequired()) ? "" : "not ");
            }
            if (!restartRequired) {
                restartRequired = panel.isRestartRequired();
            }
        }
        return restartRequired;
    }

    void selection(String view) {
        CardLayout cl = (CardLayout) (detailpanel.getLayout());
        cl.show(detailpanel, view);
    }

    public void addPreferencesPanel(PreferencesPanel panel) {
        this.getPreferencesPanels().put(panel.getClass().getName(), panel);
        addItem(panel.getPreferencesItem(),
                panel.getPreferencesItemText(),
                panel.getTabbedPreferencesTitle(),
                panel.getLabelKey(),
                panel,
                panel.getPreferencesTooltip(),
                panel.getSortOrder()
        );
    }

    private void addItem(String prefItem, String itemText, String tabTitle,
            String labelKey, PreferencesPanel item, String tooltip, int sortOrder) {
        PreferencesCatItems itemBeingAdded = null;
        for (PreferencesCatItems preferences : preferencesArray) {
            if (preferences.getPrefItem().equals(prefItem)) {
                itemBeingAdded = preferences;
                // the lowest sort order of any panel sets the sort order for
                // the preferences category
                if (sortOrder < preferences.sortOrder) {
                    preferences.sortOrder = sortOrder;
                }
                break;
            }
        }
        if (itemBeingAdded == null) {
            itemBeingAdded = new PreferencesCatItems(prefItem, itemText, sortOrder);
            preferencesArray.add(itemBeingAdded);
            // As this is a new item in the selection list, we need to update
            // the JList.
            updateJList();
        }
        if (tabTitle == null) {
            tabTitle = itemText;
        }
        itemBeingAdded.addPreferenceItem(tabTitle, labelKey, item.getPreferencesComponent(), tooltip, sortOrder);
    }

    /* Method allows for the preference to goto a specific list item */
    public void gotoPreferenceItem(String selection, String subCategory) {

        selection(selection);
        list.setSelectedIndex(getCategoryIndexFromString(selection));
        if (subCategory == null || subCategory.isEmpty()) {
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
        if (subCategory == null || subCategory.isEmpty()) {
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
        List<String> choices = this.getChoices();
        list = new JList<>(choices.toArray(new String[choices.size()]));
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

    public boolean isPreferencesValid() {
        return this.getPreferencesPanels().values().stream().allMatch((panel) -> (panel.isPreferencesValid()));
    }

    @Override
    public void savePressed(boolean restartRequired) {
        ShutDownManager sdm = InstanceManager.getDefault(ShutDownManager.class);
        if (!this.isPreferencesValid() && !sdm.isShuttingDown()) {
            for (PreferencesPanel panel : this.getPreferencesPanels().values()) {
                if (!panel.isPreferencesValid()) {
                    switch (JOptionPane.showConfirmDialog(this,
                            Bundle.getMessage("InvalidPreferencesMessage", panel.getTabbedPreferencesTitle()),
                            Bundle.getMessage("InvalidPreferencesTitle"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE)) {
                        case JOptionPane.YES_OPTION:
                            // abort save and return to broken preferences
                            this.gotoPreferenceItem(panel.getPreferencesItem(), panel.getTabbedPreferencesTitle());
                            return;
                        default:
                            // do nothing
                            break;
                    }
                }
            }
        }
        super.savePressed(restartRequired);
    }

    static class PreferencesCatItems implements java.io.Serializable {

        /*
         * This contains details of all list managedPreferences to be displayed in the
         * preferences
         */
        String itemText;
        String prefItem;
        int sortOrder = Integer.MAX_VALUE;
        JTabbedPane tabbedPane = new JTabbedPane();
        ArrayList<String> disableItemsList = new ArrayList<>();

        private final ArrayList<TabDetails> tabDetailsArray = new ArrayList<>();

        PreferencesCatItems(String pref, String title, int sortOrder) {
            prefItem = pref;
            itemText = title;
            this.sortOrder = sortOrder;
        }

        void addPreferenceItem(String title, String labelkey, JComponent item,
                String tooltip, int sortOrder) {
            for (TabDetails tabDetails : tabDetailsArray) {
                if (tabDetails.getTitle().equals(title)) {
                    // If we have a match then we do not need to add it back in.
                    return;
                }
            }
            TabDetails tab = new TabDetails(labelkey, title, item, tooltip, sortOrder);
            tabDetailsArray.add(tab);
            tabDetailsArray.sort((TabDetails o1, TabDetails o2) -> {
                int comparison = Integer.compare(o1.sortOrder, o2.sortOrder);
                return (comparison != 0) ? comparison : o1.tabTitle.compareTo(o2.tabTitle);
            });
            JScrollPane scroller = new JScrollPane(tab.getPanel());
            scroller.setBorder(BorderFactory.createEmptyBorder());
            ThreadingUtil.runOnGUI(() -> {

                tabbedPane.addTab(tab.getTitle(), null, scroller, tab.getToolTip());

                for (String disableItem : disableItemsList) {
                    if (item.getClass().getName().equals(disableItem)) {
                        tabbedPane.setEnabledAt(tabbedPane.indexOfTab(tab.getTitle()), false);
                        return;
                    }
                }
            });
        }

        String getPrefItem() {
            return prefItem;
        }

        String getItemString() {
            return itemText;
        }

        ArrayList<String> getSubCategoriesList() {
            ArrayList<String> choices = new ArrayList<>();
            for (TabDetails tabDetails : tabDetailsArray) {
                choices.add(tabDetails.getTitle());
            }
            return choices;
        }

        /*
         * This returns a JPanel if only one item is configured for a menu item
         * or it returns a JTabbedFrame if there are multiple managedPreferences for the menu
         */
        JComponent getPanel() {
            if (tabDetailsArray.size() == 1) {
                return tabDetailsArray.get(0).getPanel();
            } else {
                if (tabbedPane.getTabCount() == 0) {
                    for (TabDetails tab : tabDetailsArray) {
                        ThreadingUtil.runOnGUI(() -> {
                            JScrollPane scroller = new JScrollPane(tab.getPanel());
                            scroller.setBorder(BorderFactory.createEmptyBorder());

                            tabbedPane.addTab(tab.getTitle(), null, scroller, tab.getToolTip());

                            for (String disableItem : disableItemsList) {
                                if (tab.getItem().getClass().getName().equals(disableItem)) {
                                    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(tab.getTitle()), false);
                                    return;
                                }
                            }
                        });
                    }
                }
                return tabbedPane;
            }
        }

        void gotoSubCategory(String sub) {
            if (tabDetailsArray.size() == 1) {
                return;
            }
            for (int i = 0; i < tabDetailsArray.size(); i++) {
                if (tabDetailsArray.get(i).getTitle().equals(sub)) {
                    tabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }

        void disableSubCategory(String sub) {
            if (tabDetailsArray.isEmpty()) {
                // So the tab preferences might not have been initialised when
                // the call to disable an item is called therefore store it for
                // later on
                disableItemsList.add(sub);
                return;
            }
            for (int i = 0; i < tabDetailsArray.size(); i++) {
                if ((tabDetailsArray.get(i).getItem()).getClass().getName()
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
            private final int sortOrder;

            TabDetails(String labelkey, String tabTit, JComponent item,
                    String tooltip, int sortOrder) {
                tabItem = item;
                tabTitle = tabTit;
                tabTooltip = tooltip;
                this.sortOrder = sortOrder;

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
                ThreadingUtil.runOnGUI(() -> {
                    tabPanel.setLayout(new BorderLayout());
                    tabPanel.add(p, BorderLayout.CENTER);
                });
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

            int getSortOrder() {
                return sortOrder;
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TabbedPreferences.class);

}
