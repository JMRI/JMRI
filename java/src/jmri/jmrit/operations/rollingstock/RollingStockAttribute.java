package jmri.jmrit.operations.rollingstock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.trains.TrainCommon;

/**
 * Represents an attribute a rolling stock can have. Some attributes are length,
 * color, type, load, road, owner, model etc.
 *
 * @author Daniel Boudreau Copyright (C) 2014
 *
 */
public abstract class RollingStockAttribute extends PropertyChangeSupport {

    protected static final int MIN_NAME_LENGTH = 1;

    public RollingStockAttribute() {
    }

    public void dispose() {
        list.clear();
        //TODO The removal of listeners causes the tests to fail.
        // Need to reload all listeners for the tests to work.
        // Only tests currently call dispose()
        // remove all listeners
//  for (java.beans.PropertyChangeListener p : pcs.getPropertyChangeListeners())
//   pcs.removePropertyChangeListener(p);
    }

    protected List<String> list = new ArrayList<>();

    public String[] getNames() {
        if (list.isEmpty()) {
            list.addAll(Arrays.asList(getDefaultNames().split(",")));
        }
        return list.toArray(new String[0]);
    }

    protected String getDefaultNames() {
        return "Error"; // overridden // NOI18N
    }

    public void setNames(String[] names) {
        if (names.length > 0) {
            java.util.Arrays.sort(names);
            for (String name : names) {
                if (!list.contains(name)) {
                    list.add(name);
                }
            }
        }
    }

    /**
     * Performs number sort before adding to list
     * @param lengths The set of strings to be ordered.
     *
     */
    public void setValues(String[] lengths) {
        if (lengths.length == 0) {
            return;
        }
        try {
            jmri.util.StringUtil.numberSort(lengths);
        } catch (NumberFormatException e) {
            log.error("lengths are not all numeric, list:");
            for (int i = 0; i < lengths.length; i++) {
                try {
                    Integer.parseInt(lengths[i]);
                    log.error("length {} = {}", i, lengths[i]);
                } catch (NumberFormatException ee) {
                    log.error("length {} = {} is not a valid number!", i, lengths[i]);
                }
            }
        }
        for (String length : lengths) {
            if (!list.contains(length)) {
                list.add(length);
            }
        }
    }
    
    public void sort() {
        java.util.Collections.sort(list);
    }

    public void addName(String name) {
        if (name == null) {
            return;
        }
        if (list.contains(name)) {
            return;
        }
        list.add(name);
        sort();
        maxNameLength = 0; // reset maximum name length
        maxNameSubStringLength = 0;
    }

    public void deleteName(String name) {
        list.remove(name);
        maxNameLength = 0; // reset maximum name length
        maxNameSubStringLength = 0;
    }

    public boolean containsName(String name) {
        return list.contains(name);
    }

    public JComboBox<String> getComboBox() {
        JComboBox<String> box = new JComboBox<>();
        updateComboBox(box);
        return box;
    }

    public void updateComboBox(JComboBox<String> box) {
        box.removeAllItems();
        for (String name : getNames()) {
            box.addItem(name);
        }
    }

    protected String maxName = "";
    protected int maxNameLength = 0;
    
    public int getMaxNameLength() {
        if (maxNameLength == 0) {
            maxName = "";
            maxNameLength = getMinNameLength();
            for (String name : getNames()) {
                if (name.length() > maxNameLength) {
                    maxName = name;
                    maxNameLength = name.length();
                }
            }
        }
        return maxNameLength;
    }
    
    protected int maxNameSubStringLength = 0;
    
    public int getMaxNameSubStringLength() {
        if (maxNameSubStringLength == 0) {
            maxName = "";
            maxNameSubStringLength = getMinNameLength();
            for (String name : getNames()) {
                String[] subString = name.split(TrainCommon.HYPHEN);
                if (subString.length > 0 && subString[0].length() > maxNameSubStringLength) {
                    maxName = name;
                    maxNameSubStringLength = subString[0].length();
                }
            }
        }
        return maxNameSubStringLength;
    }
    
    protected int getMinNameLength() {
        return MIN_NAME_LENGTH;
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd and operations-engines.dtd.
     * @param root Common Element for storage.
     * @param eNames New format Element group name
     * @param eName New format Element name
     *
     */
    public void store(Element root, String eNames, String eName) {
        Element names = new Element(eNames);
        for (String name : getNames()) {
            Element e = new Element(eName);
            if (eName.equals(Xml.LENGTH)) {
                e.setAttribute(new Attribute(Xml.VALUE, name));
            } else {
                e.setAttribute(new Attribute(Xml.NAME, name));
            }
            names.addContent(e);
        }
        root.addContent(names);
    }

    public void load(Element root, String eNames, String eName, String oldName) {
        // new format using elements starting version 3.3.1
        if (root.getChild(eNames) != null) {
            List<Element> l = root.getChild(eNames).getChildren(eName);
            Attribute a;
            String[] names = new String[l.size()];
            for (int i = 0; i < l.size(); i++) {
                Element name = l.get(i);
                if ((a = name.getAttribute(Xml.NAME)) != null) {
                    names[i] = a.getValue();
                }
                // lengths use "VALUE"
                if ((a = name.getAttribute(Xml.VALUE)) != null) {
                    names[i] = a.getValue();
                }

            }
            setNames(names);
        } // try old format
        else if (root.getChild(oldName) != null) {
            String[] names = root.getChildText(oldName).split("%%"); // NOI18N
            setNames(names);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RollingStockAttribute.class);

}
