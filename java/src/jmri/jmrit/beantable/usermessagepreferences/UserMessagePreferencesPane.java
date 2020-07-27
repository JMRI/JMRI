package jmri.jmrit.beantable.usermessagepreferences;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import jmri.UserPreferencesManager;
import jmri.jmrit.beantable.AudioTableAction;
import jmri.jmrit.beantable.BlockTableAction;
import jmri.jmrit.beantable.LRouteTableAction;
import jmri.jmrit.beantable.LightTableAction;
import jmri.jmrit.beantable.LogixTableAction;
import jmri.jmrit.beantable.MemoryTableAction;
import jmri.jmrit.beantable.ReporterTableAction;
import jmri.jmrit.beantable.RouteTableAction;
import jmri.jmrit.beantable.SensorTableAction;
import jmri.jmrit.beantable.SignalGroupTableAction;
import jmri.jmrit.beantable.SignalHeadTableAction;
import jmri.jmrit.beantable.SignalMastTableAction;
import jmri.jmrit.beantable.TransitTableAction;
import jmri.jmrit.beantable.TurnoutTableAction;
import jmri.swing.PreferencesPanel;
import jmri.util.swing.JmriPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 * Pane to show User Message Preferences.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 */
@ServiceProvider(service = PreferencesPanel.class)
public class UserMessagePreferencesPane extends JmriPanel implements PreferencesPanel {

    UserPreferencesManager p;

    public UserMessagePreferencesPane() {
        super();
        p = jmri.InstanceManager.getDefault(UserPreferencesManager.class);
        p.addPropertyChangeListener((PropertyChangeEvent e) -> {
            if (e.getPropertyName().equals(UserPreferencesManager.PREFERENCES_UPDATED)) {
                refreshOptions();
            }
        });
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setMinimumMessagePref();
        add(tab);
    }

    private synchronized void setMinimumMessagePref() {
        //This ensures that as a minimum that the following items are at least initialised and appear in the preference panel
        p.setClassDescription(AudioTableAction.class.getName());
        p.setClassDescription(BlockTableAction.class.getName());

        p.setClassDescription(LightTableAction.class.getName());
        p.setClassDescription(LogixTableAction.class.getName());
        p.setClassDescription(LRouteTableAction.class.getName());
        p.setClassDescription(MemoryTableAction.class.getName());

        p.setClassDescription(ReporterTableAction.class.getName());
        p.setClassDescription(RouteTableAction.class.getName());

        p.setClassDescription(SensorTableAction.class.getName());
        p.setClassDescription(SignalGroupTableAction.class.getName());
        p.setClassDescription(SignalHeadTableAction.class.getName());
        p.setClassDescription(SignalMastTableAction.class.getName());

        p.setClassDescription(TransitTableAction.class.getName());
        p.setClassDescription(TurnoutTableAction.class.getName());

        newMessageTab();
    }

    JTabbedPane tab = new JTabbedPane();

    private HashMap<JComboBox<Object>, ListItems> _comboBoxes = new HashMap<>();
    private HashMap<JCheckBox, ListItems> _checkBoxes = new HashMap<>();

    private synchronized void newMessageTab() {
        remove(tab);
        tab = new JTabbedPane();

        //might need to redo this so that it doesn't recreate everything all the time.
        _comboBoxes = new HashMap<>();
        _checkBoxes = new HashMap<>();

        ArrayList<String> preferenceClassList = p.getPreferencesClasses();
        for (String strClass : preferenceClassList) {
            JPanel classholder = new JPanel();
            classholder.setLayout(new BorderLayout());

            HashMap<Integer, String> options;
            boolean add = false;
            boolean addtoindependant = false;
            if (p.getPreferencesSize(strClass) > 1) {
                addtoindependant = true;
            }
            JPanel classPanel = new JPanel();
            classPanel.setLayout(new BoxLayout(classPanel, BoxLayout.Y_AXIS));
            classPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            for (int j = 0; j < p.getMultipleChoiceSize(strClass); j++) {
                String itemName = p.getChoiceName(strClass, j);
                options = p.getChoiceOptions(strClass, itemName);
                if (options != null) {
                    JComboBox<Object> optionBox = new JComboBox<>();
                    ListItems li = new ListItems(strClass, itemName);
                    _comboBoxes.put(optionBox, li);
                    li.isIncluded(addtoindependant);
                    optionBox.removeAllItems();
                    for (Object value : options.values()) {
                        optionBox.addItem(value);
                    }
                    int current = p.getMultipleChoiceOption(strClass, itemName);

                    if (options.containsKey(current)) {
                        optionBox.setSelectedItem(options.get(current));
                    }
                    if (addtoindependant) {
                        JPanel optionPanel = new JPanel();
                        JLabel _comboLabel = new JLabel(p.getChoiceDescription(strClass, itemName), JLabel.LEFT);
                        _comboLabel.setAlignmentX(0.5f);
                        optionPanel.add(_comboLabel);
                        optionPanel.add(optionBox);
                        add = true;
                        classPanel.add(optionPanel);
                    }
                }
            }
            ArrayList<String> singleList = p.getPreferenceList(strClass);
            if (!singleList.isEmpty()) {
                for (int i = 0; i < singleList.size(); i++) {
                    String itemName = p.getPreferenceItemName(strClass, i);
                    String description = p.getPreferenceItemDescription(strClass, itemName);
                    if ((description != null) && (!description.isEmpty())) {
                        JCheckBox check = new JCheckBox(description);
                        check.setSelected(p.getPreferenceState(strClass, itemName));
                        ListItems li = new ListItems(strClass, itemName);
                        _checkBoxes.put(check, li);
                        li.isIncluded(addtoindependant);

                        if (addtoindependant) {
                            classPanel.add(check);
                            add = true;
                        }
                    }
                }
            }
            if (add) {
                classholder.add(classPanel, BorderLayout.NORTH);
                if (p.getPreferencesSize(strClass) > 1) {
                    JScrollPane scrollPane = new JScrollPane(classholder);
                    scrollPane.setPreferredSize(new Dimension(300, 300));
                    scrollPane.setBorder(BorderFactory.createEmptyBorder());
                    tab.add(scrollPane, p.getClassDescription(strClass));
                }
            }
        }
        HashMap<String, ArrayList<ListItems>> countOfItems = new HashMap<>();
        HashMap<String, ArrayList<JCheckBox>> countOfItemsCheck = new HashMap<>();
        HashMap<String, ArrayList<JComboBox<Object>>> countOfItemsCombo = new HashMap<>();

        for (Entry<JComboBox<Object>, ListItems> entry : _comboBoxes.entrySet()) {
            if (!entry.getValue().isIncluded()) {
                String strItem = entry.getValue().getItem();
                if (!countOfItems.containsKey(strItem)) {
                    countOfItems.put(strItem, new ArrayList<>());
                    countOfItemsCombo.put(strItem, new ArrayList<>());
                }
                ArrayList<ListItems> a = countOfItems.get(strItem);
                a.add(entry.getValue());

                ArrayList<JComboBox<Object>> acb = countOfItemsCombo.get(strItem);
                acb.add(entry.getKey());
            }
        }

        for (Entry<JCheckBox, ListItems> entry : _checkBoxes.entrySet()) {
            if (!entry.getValue().isIncluded()) {
                String strItem = entry.getValue().getItem();
                if (!countOfItems.containsKey(strItem)) {
                    countOfItems.put(strItem, new ArrayList<>());
                    countOfItemsCheck.put(strItem, new ArrayList<>());
                }
                ArrayList<ListItems> a = countOfItems.get(strItem);
                a.add(entry.getValue());

                ArrayList<JCheckBox> acb = countOfItemsCheck.get(strItem);
                acb.add(entry.getKey());
            }
        }

        JPanel miscPanel = new JPanel();
        miscPanel.setLayout(new BoxLayout(miscPanel, BoxLayout.Y_AXIS));

        JPanel mischolder = new JPanel();
        mischolder.setLayout(new BorderLayout());
        mischolder.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (Entry<String, ArrayList<ListItems>> entry : countOfItems.entrySet()) {
            ArrayList<ListItems> a = entry.getValue();
            ArrayList<JCheckBox> chb = countOfItemsCheck.get(entry.getKey());
            ArrayList<JComboBox<Object>> cob = countOfItemsCombo.get(entry.getKey());
            if (a.size() > 1) {
                JPanel tableDeleteTabPanel = new JPanel();
                tableDeleteTabPanel.setLayout(new BoxLayout(tableDeleteTabPanel, BoxLayout.Y_AXIS));
                JLabel tableDeleteInfoLabel = new JLabel(p.getChoiceDescription(a.get(0).getClassName(), a.get(0).getItem()), JLabel.CENTER);
                tableDeleteInfoLabel.setAlignmentX(0.5f);
                tableDeleteTabPanel.add(tableDeleteInfoLabel);
                JPanel inside = new JPanel();
                if (cob != null) {
                    JPanel insideCombo = new JPanel();
                    int gridsize = (int) (Math.ceil((cob.size() / 2.0)));
                    insideCombo.setLayout(new jmri.util.javaworld.GridLayout2(gridsize, 2 * 2, 10, 2));
                    for (JComboBox<Object> combo : cob) {
                        JLabel _comboLabel = new JLabel(p.getClassDescription(_comboBoxes.get(combo).getClassName()), JLabel.RIGHT);
                        _comboBoxes.get(combo).isIncluded(true);
                        insideCombo.add(_comboLabel);
                        insideCombo.add(combo);
                    }
                    inside.add(insideCombo);
                }
                if (chb != null) {
                    JPanel insideCheck = new JPanel();
                    insideCheck.setLayout(new jmri.util.javaworld.GridLayout2(chb.size(), 1));
                    for (JCheckBox check : chb) {
                        JLabel _checkLabel = new JLabel(p.getClassDescription(_checkBoxes.get(check).getClassName()), JLabel.RIGHT);
                        _checkBoxes.get(check).isIncluded(true);
                        insideCheck.add(_checkLabel);
                        insideCheck.add(check);
                    }
                    inside.add(insideCheck);
                }
                tableDeleteTabPanel.add(inside);
                JScrollPane scrollPane = new JScrollPane(tableDeleteTabPanel);
                scrollPane.setPreferredSize(new Dimension(300, 300));
                tab.add(scrollPane, entry.getKey());
            } else {
                JPanel itemPanel = new JPanel();
                JPanel subItem = new JPanel();
                subItem.setLayout(new BoxLayout(subItem, BoxLayout.Y_AXIS));
                subItem.add(new JLabel(p.getClassDescription(a.get(0).getClassName()), JLabel.CENTER));

                if (countOfItemsCheck.containsKey(entry.getKey())) {
                    subItem.add(countOfItemsCheck.get(entry.getKey()).get(0));
                    itemPanel.add(subItem);
                    miscPanel.add(itemPanel);
                }
            }
        }

        add(tab);
        mischolder.add(miscPanel, BorderLayout.NORTH);
        JScrollPane miscScrollPane = new JScrollPane(mischolder);
        miscScrollPane.setPreferredSize(new Dimension(300, 300));

        tab.add(miscScrollPane, "Misc items");
        revalidate();
    }

    @Override
    public String getPreferencesItem() {
        return "MESSAGES"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuMessages"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return null;
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        this.updateManager();
    }

    @Override
    public synchronized  boolean isDirty() {
        for (Entry<JComboBox<Object>, ListItems> entry : _comboBoxes.entrySet()) {
            String strClass = entry.getValue().getClassName();
            String strItem = entry.getValue().getItem();
            if (!p.getChoiceOptions(strClass, strItem).get(p.getMultipleChoiceOption(strClass, strItem)).equals(entry.getKey().getSelectedItem())) {
                return true;
            }
        }
        for (Entry<JCheckBox, ListItems> entry : _checkBoxes.entrySet()) {
            String strClass = entry.getValue().getClassName();
            String strItem = entry.getValue().getItem();
            if (p.getPreferenceState(strClass, strItem) != entry.getKey().isSelected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isRestartRequired() {
        return false;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

    static class ListItems {

        String strClass;
        String item;
        boolean included = false;

        ListItems(String strClass, String item) {
            this.strClass = strClass;
            this.item = item;
        }

        String getClassName() {
            return strClass;
        }

        String getItem() {
            return item;
        }

        boolean isIncluded() {
            return included;
        }

        void isIncluded(boolean boo) {
            included = boo;
        }
    }

    boolean updating = false;

    public synchronized void updateManager() {
        updating = true;
        p.setLoading();

        for (Entry<JComboBox<Object>, ListItems> entry : _comboBoxes.entrySet()) {
            String strClass = entry.getValue().getClassName();
            String strItem = entry.getValue().getItem();
            String strSelection = (String) entry.getKey().getSelectedItem();
            p.setMultipleChoiceOption(strClass, strItem, strSelection);
        }

        for (Entry<JCheckBox, ListItems> entry : _checkBoxes.entrySet()) {
            String strClass = entry.getValue().getClassName();
            String strItem = entry.getValue().getItem();
            p.setPreferenceState(strClass, strItem, entry.getKey().isSelected());
        }

        updating = false;
        p.finishLoading();
        refreshOptions();
    }

    private void refreshOptions() {
        if (updating) {
            return;
        }
        newMessageTab();
    }

}
