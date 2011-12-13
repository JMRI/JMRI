// UserMessagePreferencesPane.java

package jmri.jmrit.beantable.usermessagepreferences;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.swing.*;
import java.awt.*;

/**
 * Pane to show User Message Preferences
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision$
 */
public class UserMessagePreferencesPane extends jmri.util.swing.JmriPanel {

    jmri.UserPreferencesManager p;
    
    public UserMessagePreferencesPane() {
        super();
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        p.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals("PreferencesUpdated")){
                    refreshOptions();
                }
            }
        });
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setMinimumMessagePref();
        add(tab);
        jmri.InstanceManager.tabbedPreferencesInstance().addItemToSave(this, jmri.jmrit.beantable.usermessagepreferences.UserMessagePreferencesPane.class, "updateManager");
    }
    
    
    void setMinimumMessagePref(){
    //This ensures that as a minimum that the following items are at least initialised and appear in the preference panel
        p.setClassDescription(jmri.jmrit.beantable.AudioTableAction.class.getName());
        p.setClassDescription(jmri.jmrit.beantable.BlockTableAction.class.getName());
        
        p.setClassDescription(jmri.jmrit.beantable.LightTableAction.class.getName());
        p.setClassDescription(jmri.jmrit.beantable.LogixTableAction.class.getName());
        p.setClassDescription(jmri.jmrit.beantable.LRouteTableAction.class.getName());
        p.setClassDescription(jmri.jmrit.beantable.MemoryTableAction.class.getName());
        
        p.setClassDescription(jmri.jmrit.beantable.ReporterTableAction.class.getName());
        p.setClassDescription(jmri.jmrit.beantable.RouteTableAction.class.getName());
        
        p.setClassDescription(jmri.jmrit.beantable.SensorTableAction.class.getName());
        p.setClassDescription(jmri.jmrit.beantable.SignalGroupTableAction.class.getName());
        p.setClassDescription(jmri.jmrit.beantable.SignalHeadTableAction.class.getName());
        p.setClassDescription(jmri.jmrit.beantable.SignalMastTableAction.class.getName());
        
        p.setClassDescription(jmri.jmrit.beantable.TransitTableAction.class.getName());
        p.setClassDescription(jmri.jmrit.beantable.TurnoutTableAction.class.getName());
        
        p.setClassDescription(apps.AppConfigBase.class.getName());
        
        newMessageTab();
    }
    
    
    JTabbedPane tab = new JTabbedPane();
    
    private Hashtable<JComboBox, ListItems> _comboBoxes = new Hashtable<JComboBox, ListItems>();
    private Hashtable<JCheckBox, ListItems> _checkBoxes = new Hashtable<JCheckBox, ListItems>();
    
    private void newMessageTab(){
        remove(tab);
        tab = new JTabbedPane();
        
        //might need to redo this so that it doesn't recreate everything all the time.
        _comboBoxes = new Hashtable<JComboBox, ListItems>();
        _checkBoxes = new Hashtable<JCheckBox, ListItems>();
        
        java.util.ArrayList<String> preferenceClassList = p.getPreferencesClasses();
        for (int k = 0; k<preferenceClassList.size(); k++){
            
            String strClass = preferenceClassList.get(k);
            JPanel classholder = new JPanel();
            classholder.setLayout(new BorderLayout());
            
            HashMap<Integer,String> options = null;
            boolean add = false;
            boolean addtoindependant = false;
            if (p.getPreferencesSize(strClass)>1){
                addtoindependant = true;
            }
            JPanel classPanel = new JPanel();
            classPanel.setLayout(new BoxLayout(classPanel, BoxLayout.Y_AXIS));
            classPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            for (int j = 0; j<p.getMultipleChoiceSize(strClass); j++){
                String itemName = p.getChoiceName(strClass, j);
                options = p.getChoiceOptions(strClass, itemName);
                if (options!=null){
                    JComboBox optionBox = new JComboBox();
                    ListItems li = new ListItems(strClass, itemName);
                    _comboBoxes.put(optionBox, li);
                    li.isIncluded(addtoindependant);
                    optionBox.removeAllItems();
                    for (Object value : options.values()) {
                        optionBox.addItem(value);
                    }
                    int current = p.getMultipleChoiceOption(strClass, itemName);
                    
                    if (options.containsKey(current)){
                        optionBox.setSelectedItem(options.get(current));
                    }
                    if (addtoindependant){
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
            java.util.ArrayList<String> singleList = p.getPreferenceList(strClass);
            if (singleList.size()!=0){
                for (int i = 0; i<singleList.size(); i++){
                    String itemName = p.getPreferenceItemName(strClass, i);
                    String description = p.getPreferenceItemDescription(strClass, itemName);
                    if ((description !=null) && (!description.equals(""))){
                        JCheckBox check  = new JCheckBox(description);
                        check.setSelected(p.getPreferenceState(strClass, itemName));
                        ListItems li = new ListItems(strClass, itemName);
                        _checkBoxes.put(check, li);
                        li.isIncluded(addtoindependant);
                        
                        if (addtoindependant){
                            classPanel.add(check);
                            add=true;
                        }
                    }
                }
            }
            if (add) {
                classholder.add(classPanel, BorderLayout.NORTH);
                if (p.getPreferencesSize(strClass)>1){
                    JScrollPane scrollPane = new JScrollPane(classholder);
                    scrollPane.setPreferredSize(new Dimension(300, 300));
                    tab.add(scrollPane, p.getClassDescription(strClass));
                }
            }
        }
        Enumeration<JComboBox> keys = _comboBoxes.keys();
        Hashtable<String, ArrayList<ListItems>> countOfItems = new Hashtable<String, ArrayList<ListItems>>();
        Hashtable<String, ArrayList<JCheckBox>> countOfItemsCheck = new Hashtable<String, ArrayList<JCheckBox>>();
        Hashtable<String, ArrayList<JComboBox>> countOfItemsCombo = new Hashtable<String, ArrayList<JComboBox>>();
        while ( keys.hasMoreElements() )
           {
                JComboBox key = keys.nextElement();
                if (!_comboBoxes.get(key).isIncluded()){
                    String strItem = _comboBoxes.get(key).getItem();
                    if (!countOfItems.containsKey(strItem)){
                        countOfItems.put(strItem, new ArrayList<ListItems>());
                        countOfItemsCombo.put(strItem, new ArrayList<JComboBox>());
                    }
                    
                    ArrayList<ListItems> a = countOfItems.get(strItem);
                    a.add(_comboBoxes.get(key));
                    
                    ArrayList<JComboBox> acb = countOfItemsCombo.get(strItem);
                    acb.add(key);
                }
           }
           
        Enumeration<JCheckBox> cbKeys = _checkBoxes.keys();
        while ( cbKeys.hasMoreElements() )
           {
               JCheckBox key = cbKeys.nextElement();
               if (!_checkBoxes.get(key).isIncluded()){
                    String strItem = _checkBoxes.get(key).getItem();
                    
                    if (!countOfItems.containsKey(strItem)){
                        countOfItems.put(strItem, new ArrayList<ListItems>());
                        countOfItemsCheck.put(strItem, new ArrayList<JCheckBox>());
                    }
                    ArrayList<ListItems> a = countOfItems.get(strItem);
                    a.add(_checkBoxes.get(key));  
                    
                    ArrayList<JCheckBox> acb = countOfItemsCheck.get(strItem);
                    acb.add(key);
               }
           }
        Enumeration<String> skeys = countOfItems.keys();
        JPanel miscPanel = new JPanel();
        miscPanel.setLayout(new BoxLayout(miscPanel, BoxLayout.Y_AXIS));
        
        JPanel mischolder = new JPanel();
        mischolder.setLayout(new BorderLayout());
        mischolder.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        while (skeys.hasMoreElements())
            {
                String item = skeys.nextElement();
                ArrayList<ListItems> a = countOfItems.get(item);
                ArrayList<JCheckBox> chb = countOfItemsCheck.get(item);
                ArrayList<JComboBox> cob = countOfItemsCombo.get(item);
                if (a.size()>1){
                    JPanel tableDeleteTabPanel = new JPanel();
                    tableDeleteTabPanel.setLayout(new BoxLayout(tableDeleteTabPanel, BoxLayout.Y_AXIS));
                    JLabel tableDeleteInfoLabel = new JLabel(p.getChoiceDescription(a.get(0).getClassName(), a.get(0).getItem()), JLabel.CENTER);
                    tableDeleteInfoLabel.setAlignmentX(0.5f);
                    tableDeleteTabPanel.add(tableDeleteInfoLabel);
                    JPanel inside = new JPanel();
                    if (cob!=null){
                        JPanel insideCombo = new JPanel();
                        int gridsize = (int)(Math.ceil( (cob.size()/2.0)));
                        insideCombo.setLayout(new jmri.util.javaworld.GridLayout2(gridsize,2*2, 10, 2));
                        for (int i = 0; i<cob.size(); i++){
                            JComboBox combo = cob.get(i);
                            JLabel _comboLabel = new JLabel(p.getClassDescription(_comboBoxes.get(combo).getClassName()), JLabel.RIGHT);
                            _comboBoxes.get(combo).isIncluded(true);
                            insideCombo.add(_comboLabel);
                            insideCombo.add(combo);
                        }
                        inside.add(insideCombo);
                    }
                    if (chb!=null){
                        JPanel insideCheck = new JPanel();
                        insideCheck.setLayout(new jmri.util.javaworld.GridLayout2(chb.size(),1));
                        for (int i = 0; i<chb.size(); i++){
                            JCheckBox check = chb.get(i);
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
                    tab.add(scrollPane, item);
                } else {
                    JPanel itemPanel = new JPanel();
                    JPanel subItem = new JPanel();
                    subItem.setLayout( new BoxLayout(subItem, BoxLayout.Y_AXIS));
                    itemPanel.setBorder(BorderFactory.createLineBorder(Color.black));
                    subItem.add(new JLabel(p.getClassDescription(a.get(0).getClassName()), JLabel.CENTER));
                    
                    
                    if (countOfItemsCheck.containsKey(item)){
                        subItem.add(countOfItemsCheck.get(item).get(0));
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
    
    static class ListItems{
        String strClass;
        String item;
        boolean included = false;
        
        ListItems(String strClass, String item){
            this.strClass = strClass;
            this.item = item;
        }
        
        String getClassName() { return strClass; }
        
        String getItem() { return item; }
        
        boolean isIncluded() { return included; }
        
        void isIncluded(boolean boo) { included = boo; }
    }
    
    boolean updating = false;
    public void updateManager(){
        updating=true;
        p.setLoading();
        
        Enumeration<JComboBox> keys = _comboBoxes.keys();
        while ( keys.hasMoreElements() )
           {
           JComboBox key = keys.nextElement();
           String strClass = _comboBoxes.get(key).getClassName();
           String strItem = _comboBoxes.get(key).getItem();
           String strSelection = (String)key.getSelectedItem();
           p.setMultipleChoiceOption (strClass, strItem, strSelection);
           }
           
        Enumeration<JCheckBox> ckeys = _checkBoxes.keys();
        while ( ckeys.hasMoreElements() )
           {
           JCheckBox key = ckeys.nextElement();
           String strClass = _checkBoxes.get(key).getClassName();
           String strItem = _checkBoxes.get(key).getItem();
           p.setPreferenceState (strClass, strItem, key.isSelected());
           }
        
        updating=false;
        p.finishLoading();
        refreshOptions();
    }
    
    private void refreshOptions(){
        if (updating){
            return;
        }
        newMessageTab();
    }
}

/* @(#)UserMessagePreferencesPane.java */
