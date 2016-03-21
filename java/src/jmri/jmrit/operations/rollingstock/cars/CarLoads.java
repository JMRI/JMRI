// CarLoads.java
package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Control;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the loads that cars can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 * @version $Revision$
 */
public class CarLoads extends RollingStockAttribute {

    protected Hashtable<String, List<CarLoad>> listCarLoads = new Hashtable<String, List<CarLoad>>();
    protected String _emptyName = Bundle.getMessage("EmptyCar");
    protected String _loadName = Bundle.getMessage("LoadedCar");

    public static final String NONE = ""; // NOI18N

    // for property change
    public static final String LOAD_CHANGED_PROPERTY = "CarLoads_Load"; // NOI18N
    public static final String LOAD_TYPE_CHANGED_PROPERTY = "CarLoads_Load_Type"; // NOI18N
    public static final String LOAD_PRIORITY_CHANGED_PROPERTY = "CarLoads_Load_Priority"; // NOI18N
    public static final String LOAD_NAME_CHANGED_PROPERTY = "CarLoads_Name"; // NOI18N
    public static final String LOAD_COMMENT_CHANGED_PROPERTY = "CarLoads_Load_Comment"; // NOI18N

    public CarLoads() {
    }

    /**
     * record the single instance *
     */
    private static CarLoads _instance = null;

    public static synchronized CarLoads instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("CarLoads creating instance");
            }
            // create and load
            _instance = new CarLoads();
        }
        if (Control.showInstance) {
            log.debug("CarLoads returns instance {}", _instance);
        }
        return _instance;
    }

    /**
     * Add a car type with specific loads
     *
     * @param type car type
     */
    public void addType(String type) {
        listCarLoads.put(type, new ArrayList<CarLoad>());
    }

    /**
     * Replace a car type. Transfers load priority, drop and load comments.
     *
     * @param oldType old car type
     * @param newType new car type
     */
    public void replaceType(String oldType, String newType) {
        List<String> names = getNames(oldType);
        addType(newType);
        for (String name : names) {
            addName(newType, name);
            setPriority(newType, name, getPriority(oldType, name));
            setDropComment(newType, name, getDropComment(oldType, name));
            setPickupComment(newType, name, getPickupComment(oldType, name));
        }
        listCarLoads.remove(oldType);
    }

    /**
     * Gets the appropriate car loads for the car's type.
     *
     * @param type
     * @return JComboBox with car loads starting with empty string.
     */
    public JComboBox<String> getSelectComboBox(String type) {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(NONE);
        for (String load : getNames(type)) {
            box.addItem(load);
        }
        return box;
    }

    /**
     * Gets the appropriate car loads for the car's type.
     *
     * @param type
     * @return JComboBox with car loads.
     */
    public JComboBox<String> getComboBox(String type) {
        JComboBox<String> box = new JComboBox<>();
        updateComboBox(type, box);
        return box;

    }
    


    /**
     * Gets a combobox with the available priorities
     *
     * @return JComboBox with car priorities.
     */
    public JComboBox<String> getPriorityComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(CarLoad.PRIORITY_LOW);
        box.addItem(CarLoad.PRIORITY_HIGH);
        return box;
    }

    /**
     * Gets a combobox with the available load types: empty and load
     *
     * @return JComboBox with load types: LOAD_TYPE_EMPTY and LOAD_TYPE_LOAD
     */
    public JComboBox<String> getLoadTypesComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(CarLoad.LOAD_TYPE_EMPTY);
        box.addItem(CarLoad.LOAD_TYPE_LOAD);
        return box;
    }

    /**
     * Gets the load names for a given car type
     *
     * @param type car type
     * @return list of load names
     */
    public List<String> getNames(String type) {
        List<String> names = new ArrayList<String>();
        if (type == null) {
            names.add(getDefaultEmptyName());
            names.add(getDefaultLoadName());
            return names;
        }
        List<CarLoad> loads = listCarLoads.get(type);
        if (loads == null) {
            addType(type);
            loads = listCarLoads.get(type);
        }
        if (loads.size() == 0) {
            loads.add(new CarLoad(getDefaultEmptyName()));
            loads.add(new CarLoad(getDefaultLoadName()));
        }
        for (CarLoad carLoad : loads) {
            names.add(carLoad.getName());
        }
        return names;
    }

    /**
     * Add a load name for the car type.
     *
     * @param type car type.
     * @param name load name.
     */
    public void addName(String type, String name) {
        // don't add if name already exists
        if (containsName(type, name)) {
            return;
        }
        List<CarLoad> loads = listCarLoads.get(type);
        if (loads == null) {
            log.debug("car type ({}) does not exist", type);
            return;
        }
        loads.add(0, new CarLoad(name));
        maxNameLength = 0; // reset maximum name length
        setDirtyAndFirePropertyChange(LOAD_CHANGED_PROPERTY, null, name);
    }

    public void deleteName(String type, String name) {
        List<CarLoad> loads = listCarLoads.get(type);
        if (loads == null) {
            log.debug("car type ({}) does not exist", type);
            return;
        }
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                loads.remove(cl);
                break;
            }
        }
        maxNameLength = 0; // reset maximum name length
        setDirtyAndFirePropertyChange(LOAD_CHANGED_PROPERTY, name, null);
    }

    /**
     * Determines if a car type can have a specific load name.
     *
     * @param type car type.
     * @param name load name.
     * @return true if car can have this load name.
     */
    public boolean containsName(String type, String name) {
        List<String> names = getNames(type);
        return names.contains(name);
    }

    public void updateComboBox(String type, JComboBox<String> box) {
        box.removeAllItems();
        List<String> loads = getNames(type);
        for (String name : loads) {
            box.addItem(name);
        }
    }
    
    /**
     * Returns JComboBox with all load names for every type of car
     */
    public void updateComboBox(JComboBox<String> box) {
        box.removeAllItems();
        List<String> names = new ArrayList<String>();
        for (String type : CarTypes.instance().getNames()) {
            for (String load : getNames(type)) {
                if (!names.contains(load)) {
                    names.add(load);
                }
            }
        }
        java.util.Collections.sort(names);
        for (String load : names) {
            box.addItem(load);
        }
    }

    public void updateRweComboBox(String type, JComboBox<String> box) {
        box.removeAllItems();
        List<String> loads = getNames(type);
        for (String name : loads) {
            if (getLoadType(type, name).equals(CarLoad.LOAD_TYPE_EMPTY)) {
                box.addItem(name);
            }
        }
        // must return with at least one load name
        if (box.getItemCount() == 0) {
            box.addItem(getDefaultEmptyName());
        }
    }

    public void replaceName(String type, String oldName, String newName) {
        addName(type, newName);
        deleteName(type, oldName);
        setDirtyAndFirePropertyChange(LOAD_NAME_CHANGED_PROPERTY, oldName, newName);
    }

    public String getDefaultLoadName() {
        return _loadName;
    }

    public void setDefaultLoadName(String name) {
        String old = _loadName;
        _loadName = name;
        setDirtyAndFirePropertyChange(LOAD_NAME_CHANGED_PROPERTY, old, name);
    }

    public String getDefaultEmptyName() {
        return _emptyName;
    }

    public void setDefaultEmptyName(String name) {
        String old = _emptyName;
        _emptyName = name;
        setDirtyAndFirePropertyChange(LOAD_NAME_CHANGED_PROPERTY, old, name);
    }

    /**
     * Sets the load type, empty or load.
     *
     * @param type     car type.
     * @param name     load name.
     * @param loadType load type: LOAD_TYPE_EMPTY or LOAD_TYPE_LOAD.
     */
    public void setLoadType(String type, String name, String loadType) {
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                String oldType = cl.getLoadType();
                cl.setLoadType(loadType);
                if (!oldType.equals(loadType)) {
                    setDirtyAndFirePropertyChange(LOAD_TYPE_CHANGED_PROPERTY, oldType, loadType);
                }
            }
        }
    }

    /**
     * Get's the load type, empty or load.
     *
     * @param type car type.
     * @param name load name.
     * @return load type, LOAD_TYPE_EMPTY or LOAD_TYPE_LOAD.
     */
    public String getLoadType(String type, String name) {
        if (!containsName(type, name)) {
            if (name != null && name.equals(getDefaultEmptyName())) {
                return CarLoad.LOAD_TYPE_EMPTY;
            }
            return CarLoad.LOAD_TYPE_LOAD;
        }
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                return cl.getLoadType();
            }
        }
        return "error"; // NOI18N
    }

    /**
     * Sets a loads priority.
     *
     * @param type     car type.
     * @param name     load name.
     * @param priority load priority, PRIORITY_LOW or PRIORITY_HIGH.
     */
    public void setPriority(String type, String name, String priority) {
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                String oldPriority = cl.getPriority();
                cl.setPriority(priority);
                if (!oldPriority.equals(priority)) {
                    setDirtyAndFirePropertyChange(LOAD_PRIORITY_CHANGED_PROPERTY, oldPriority, priority);
                }
            }
        }
    }

    /**
     * Get's a load's priority.
     *
     * @param type car type.
     * @param name load name.
     * @return load priority, PRIORITY_LOW or PRIORITY_HIGH.
     */
    public String getPriority(String type, String name) {
        if (!containsName(type, name)) {
            return CarLoad.PRIORITY_LOW;
        }
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                return cl.getPriority();
            }
        }
        return "error"; // NOI18N
    }

    public void setPickupComment(String type, String name, String comment) {
        if (!containsName(type, name)) {
            return;
        }
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                String oldComment = cl.getPickupComment();
                cl.setPickupComment(comment);
                if (!oldComment.equals(comment)) {
                    setDirtyAndFirePropertyChange(LOAD_COMMENT_CHANGED_PROPERTY, oldComment, comment);
                }
            }
        }
    }

    public String getPickupComment(String type, String name) {
        if (!containsName(type, name)) {
            return NONE;
        }
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                return cl.getPickupComment();
            }
        }
        return NONE;
    }

    public void setDropComment(String type, String name, String comment) {
        if (!containsName(type, name)) {
            return;
        }
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                String oldComment = cl.getDropComment();
                cl.setDropComment(comment);
                if (!oldComment.equals(comment)) {
                    setDirtyAndFirePropertyChange(LOAD_COMMENT_CHANGED_PROPERTY, oldComment, comment);
                }
            }
        }
    }

    public String getDropComment(String type, String name) {
        if (!containsName(type, name)) {
            return NONE;
        }
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                return cl.getDropComment();
            }
        }
        return NONE;
    }

    public int getMaxNameLength() {
        if (maxNameLength == 0) {
            maxNameLength = MIN_NAME_LENGTH;
            Enumeration<String> en = listCarLoads.keys();
            while (en.hasMoreElements()) {
                String key = en.nextElement();
                List<CarLoad> loads = listCarLoads.get(key);
                for (CarLoad load : loads) {
                    if (load.getName().length() > maxNameLength) {
                        maxNameLength = load.getName().length();
                    }
                }
            }
        }
        return maxNameLength;
    }

    private List<CarLoad> getSortedList(String type) {
        List<CarLoad> loads = listCarLoads.get(type);
        List<CarLoad> out = new ArrayList<CarLoad>();

        // Sort load names
        String[] loadNames = new String[loads.size()];
        for (int i = 0; i < loads.size(); i++) {
            loadNames[i] = loads.get(i).getName();
        }
        jmri.util.StringUtil.sort(loadNames);
        // return a list sorted by load name
        for (int i = loadNames.length - 1; i >= 0; i--) {
            for (int j = 0; j < loads.size(); j++) {
                if (loadNames[i].equals(loads.get(j).getName())) {
                    out.add(loads.get(j));
                    break;
                }
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public Hashtable<String, List<CarLoad>> getList() {
        return (Hashtable<String, List<CarLoad>>) listCarLoads.clone();
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     */
    public void store(Element root) {
        Element values = new Element(Xml.LOADS);
        // store default load and empty
        Element defaults = new Element(Xml.DEFAULTS);
        defaults.setAttribute(Xml.EMPTY, getDefaultEmptyName());
        defaults.setAttribute(Xml.LOAD, getDefaultLoadName());
        values.addContent(defaults);
        // store loads based on car types
        Enumeration<String> en = listCarLoads.keys();
        while (en.hasMoreElements()) {
            String carType = en.nextElement();
            // check to see if car type still exists
            if (!CarTypes.instance().containsName(carType)) {
                continue;
            }
            List<CarLoad> loads = getSortedList(carType);
            Element xmlLoad = new Element(Xml.LOAD);
            xmlLoad.setAttribute(Xml.TYPE, carType);
            boolean mustStore = false;  // only store loads that aren't the defaults
            for (CarLoad load : loads) {
                // don't store the defaults / low priority / no comment
                if ((load.getName().equals(getDefaultEmptyName()) || load.getName().equals(getDefaultLoadName()))
                        && load.getPriority().equals(CarLoad.PRIORITY_LOW)
                        && load.getPickupComment().equals(CarLoad.NONE) 
                        && load.getDropComment().equals(CarLoad.NONE))
                    continue;
                Element xmlCarLoad = new Element(Xml.CAR_LOAD);
                xmlCarLoad.setAttribute(Xml.NAME, load.getName());
                if (!load.getPriority().equals(CarLoad.PRIORITY_LOW)) {
                    xmlCarLoad.setAttribute(Xml.PRIORITY, load.getPriority());
                    mustStore = true; // must store
                }
                if (!load.getPickupComment().equals(CarLoad.NONE)) {
                    xmlCarLoad.setAttribute(Xml.PICKUP_COMMENT, load.getPickupComment());
                    mustStore = true; // must store
                }
                if (!load.getDropComment().equals(CarLoad.NONE)) {
                    xmlCarLoad.setAttribute(Xml.DROP_COMMENT, load.getDropComment());
                    mustStore = true; // must store
                }
                xmlCarLoad.setAttribute(Xml.LOAD_TYPE, load.getLoadType());
                xmlLoad.addContent(xmlCarLoad);
            }
            if (loads.size() > 2 || mustStore) {
                values.addContent(xmlLoad);
            }
        }
        root.addContent(values);
    }

    public void load(Element e) {
        if (e.getChild(Xml.LOADS) == null) {
            return;
        }
        Attribute a;
        Element defaults = e.getChild(Xml.LOADS).getChild(Xml.DEFAULTS);
        if (defaults != null) {
            if ((a = defaults.getAttribute(Xml.LOAD)) != null) {
                _loadName = a.getValue();
            }
            if ((a = defaults.getAttribute(Xml.EMPTY)) != null) {
                _emptyName = a.getValue();
            }
        }
        @SuppressWarnings("unchecked")
        List<Element> eLoads = e.getChild(Xml.LOADS).getChildren(Xml.LOAD);
        if (log.isDebugEnabled()) {
            log.debug("readFile sees {} car loads", eLoads.size());
        }
        for (Element eLoad : eLoads) {
            if ((a = eLoad.getAttribute(Xml.TYPE)) != null) {
                String type = a.getValue();
                addType(type);
                // old style had a list of names
                if ((a = eLoad.getAttribute(Xml.NAMES)) != null) {
                    String names = a.getValue();
                    String[] loadNames = names.split("%%");// NOI18N
                    jmri.util.StringUtil.sort(loadNames);
                    if (log.isDebugEnabled()) {
                        log.debug("Car load type: {} loads: {}", type, names);
                    }
                    // addName puts new items at the start, so reverse load
                    for (int j = loadNames.length; j > 0;) {
                        addName(type, loadNames[--j]);
                    }
                }
                // new style load and comments
                @SuppressWarnings("unchecked")
                List<Element> eCarLoads = eLoad.getChildren(Xml.CAR_LOAD);
                if (log.isDebugEnabled()) {
                    log.debug("{} car loads for type: {}", eCarLoads.size(), type);
                }
                for (Element eCarLoad : eCarLoads) {
                    if ((a = eCarLoad.getAttribute(Xml.NAME)) != null) {
                        String name = a.getValue();
                        addName(type, name);
                        if ((a = eCarLoad.getAttribute(Xml.PRIORITY)) != null) {
                            setPriority(type, name, a.getValue());
                        }
                        if ((a = eCarLoad.getAttribute(Xml.PICKUP_COMMENT)) != null) {
                            setPickupComment(type, name, a.getValue());
                        }
                        if ((a = eCarLoad.getAttribute(Xml.DROP_COMMENT)) != null) {
                            setDropComment(type, name, a.getValue());
                        }
                        if ((a = eCarLoad.getAttribute(Xml.LOAD_TYPE)) != null) {
                            setLoadType(type, name, a.getValue());
                        }
                    }
                }
            }
        }
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        CarManagerXml.instance().setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarLoads.class.getName());

}
