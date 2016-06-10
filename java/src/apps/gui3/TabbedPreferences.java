package apps.gui3;

import apps.AppConfigBase;
import apps.ConfigBundle;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
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
 * Preferences panels listed in the PreferencesPanel property of the
 * apps.AppsStructureBundle ResourceBundle will be automatically loaded if they
 * implement the {@link jmri.swing.PreferencesPanel} interface.
 *<p>
 * At start-up time, the order for setting the preferences matters.
 * The order in the apps.AppsStructureBundle ResourceBundle must be preserved.
 *<p>
 * Other Preferences Panels will need to be manually added to this file in a
 * manner similar to the WiThrottlePrefsPanel.
 *<p>
 * State is maintained as a bound property with name INITIALIZATION (see value below)
 *<p>
 * JMRI apps (generally) create one object of this type on the main thread
 * as part of initialization, then create a separate "initialize preferences"
 * thread to handle the init() call and adding all the tabs. 
 * Finally, the result is displayed on the Swing thread.
 *
 * @author Bob Jacobsen Copyright 2010
 * @author Randall Wood 2012
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

    ArrayList<PreferencesCatItems> preferencesArray = new ArrayList<>();
    JPanel buttonpanel;
    JList<String> list;
    JButton save;
    JScrollPane listScroller;
    private int initialisationState = 0x00;

    public static final int UNINITIALISED = 0x00;
    public static final int INITIALISING = 0x01;
    public static final int INITIALISED = 0x02;
    public static final String INITIALIZATION = "PROP_INITIALIZATION";

    public TabbedPreferences() {

        /*
         * Adds the place holders for the menu managedPreferences so that any managedPreferences add by
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

    /**
     * Initialize, including loading classes referenced in the PreferencesPanel property of the apps.AppsStructureBundle bundle.
     *<p>
     * Keeps a current state to prevent doing its work twice.
     *
     * @return The current state, which should be INITIALISED if all is well.
     */
    @SuppressWarnings("rawtypes")
    public synchronized int init() {
        if (initialisationState == INITIALISED) {
            return INITIALISED;
        }
        if (initialisationState != UNINITIALISED) {
            return initialisationState;
        }
        this.setInitalisationState(INITIALISING);

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
                        if (!this.getPreferencesPanels().containsKey(className)) {
                            this.addPreferencesPanel((PreferencesPanel) Class.forName(className).newInstance());
                        }
                        ((PreferencesSubPanel) panel).setParent(this.getPreferencesPanels().get(className));
                    }
                    this.addPreferencesPanel(panel);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    log.error("Unable to add preferences class (" + className + ")", e);
                }
            }
        } catch (IOException e) {
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
        this.setInitalisationState(INITIALISED);
        return initialisationState;
    }

    public synchronized void setInitalisationState(int state) { // currently only used in init(), but synchronized in case added elsewhere later
        int old = this.initialisationState;
        this.initialisationState = state;
        this.firePropertyChange(INITIALIZATION, old, state);
    }

    public synchronized int getInitialisationState() { // not an atomic read, because of time between assignment and propertyChange notification in set
        return this.initialisationState;
    }

    public boolean isInitialised() {
        return (this.getInitialisationState() == INITIALISED);
    }

    // package only - for TabbedPreferencesFrame
    boolean isDirty() {
        for (PreferencesPanel panel : this.getPreferencesPanels().values()) {
            if (log.isDebugEnabled()) {
                log.debug("PreferencesPanel {} ({}) is {}.",
                        panel.getClass().getName(),
                        (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                        (panel.isDirty()) ? "dirty" : "clean");
            }
            if (panel.isDirty()) {
                return true;
            }
        }
        return false;
    }

    // package only - for TabbedPreferencesFrame
    boolean invokeSaveOptions() {
        boolean restartRequired = false;
        for (PreferencesPanel panel : this.getPreferencesPanels().values()) {
            if (log.isDebugEnabled()) {
                log.debug("PreferencesPanel {} ({}) is {}.",
                        panel.getClass().getName(),
                        (panel.getTabbedPreferencesTitle() != null) ? panel.getTabbedPreferencesTitle() : panel.getPreferencesItemText(),
                        (panel.isDirty()) ? "dirty" : "clean");
            }
            panel.savePreferences();
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

    void selection(String View) {
        CardLayout cl = (CardLayout) (detailpanel.getLayout());
        cl.show(detailpanel, View);
    }

    public void addPreferencesPanel(PreferencesPanel panel) {
        this.getPreferencesPanels().put(panel.getClass().getName(), panel);
        addItem(panel.getPreferencesItem(),
                panel.getPreferencesItemText(),
                panel.getTabbedPreferencesTitle(),
                panel.getLabelKey(),
                panel,
                panel.getPreferencesTooltip()
        );
    }

    private void addItem(String prefItem, String itemText, String tabtitle,
            String labelKey, PreferencesPanel item, String tooltip) {
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
            if (getInitialisationState() == INITIALISED) {
                updateJList();
            }
        }
        if (tabtitle == null) {
            tabtitle = itemText;
        }
        itemBeingAdded.addPreferenceItem(tabtitle, labelKey, item.getPreferencesComponent(), tooltip);
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
        list = new JList<>(new Vector<>(getChoices()));
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

    static class PreferencesCatItems implements java.io.Serializable {

        /*
         * This contains details of all list managedPreferences to be displayed in the
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
            JScrollPane scroller = new JScrollPane(tab.getPanel());
            scroller.setBorder(BorderFactory.createEmptyBorder());
            tabbedPane.addTab(tab.getTitle(), null, scroller, tab.getToolTip());

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
         * or it returns a JTabbedFrame if there are multiple managedPreferences for the menu
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

    private final static Logger log = LoggerFactory.getLogger(TabbedPreferences.class.getName());

}
