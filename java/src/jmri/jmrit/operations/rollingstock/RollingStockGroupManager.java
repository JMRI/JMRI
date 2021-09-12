package jmri.jmrit.operations.rollingstock;

import java.util.*;

import javax.swing.JComboBox;

import jmri.beans.PropertyChangeSupport;

/**
 * 
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public abstract class RollingStockGroupManager extends PropertyChangeSupport {

    public static final String NONE = "";

    protected Hashtable<String, RollingStockGroup<?>> _groupHashTable = new Hashtable<>();

    public static final String LISTLENGTH_CHANGED_PROPERTY = "GroupListLengthChanged"; // NOI18N

    public RollingStockGroupManager() {
    }

    /**
     * Get a comboBox loaded with current group names
     *
     * @return comboBox with group names.
     */
    public JComboBox<String> getComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(NONE);
        for (String name : getNameList()) {
            box.addItem(name);
        }
        return box;
    }

    /**
     * Update an existing comboBox with the current kernel names
     *
     * @param box comboBox requesting update
     */
    public void updateComboBox(JComboBox<String> box) {
        box.removeAllItems();
        box.addItem(NONE);
        for (String name : getNameList()) {
            box.addItem(name);
        }
    }

    /**
     * Get a list of group names
     *
     * @return ordered list of group names
     */
    public List<String> getNameList() {
        List<String> out = new ArrayList<>();
        Enumeration<String> en = _groupHashTable.keys();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        Collections.sort(out);
        return out;
    }

    public int getMaxNameLength() {
        int maxLength = 0;
        for (String name : getNameList()) {
            if (name.length() > maxLength) {
                maxLength = name.length();
            }
        }
        return maxLength;
    }
}
