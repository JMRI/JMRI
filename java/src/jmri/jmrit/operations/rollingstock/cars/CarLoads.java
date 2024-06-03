package jmri.jmrit.operations.rollingstock.cars;

import java.util.*;

import javax.swing.JComboBox;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManifestHeaderText;

/**
 * Represents the loads that cars can have.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2014
 */
public class CarLoads extends RollingStockAttribute implements InstanceManagerAutoDefault {

    protected Hashtable<String, List<CarLoad>> listCarLoads = new Hashtable<>();
    protected String _emptyName = Bundle.getMessage("EmptyCar");
    protected String _loadName = Bundle.getMessage("LoadedCar");

    public static final String NONE = ""; // NOI18N

    // for property change
    public static final String LOAD_CHANGED_PROPERTY = "CarLoads_Load"; // NOI18N
    public static final String LOAD_TYPE_CHANGED_PROPERTY = "CarLoads_Load_Type"; // NOI18N
    public static final String LOAD_PRIORITY_CHANGED_PROPERTY = "CarLoads_Load_Priority"; // NOI18N
    public static final String LOAD_NAME_CHANGED_PROPERTY = "CarLoads_Name"; // NOI18N
    public static final String LOAD_COMMENT_CHANGED_PROPERTY = "CarLoads_Load_Comment"; // NOI18N
    public static final String LOAD_HAZARDOUS_CHANGED_PROPERTY = "CarLoads_Load_Hazardous"; // NOI18N

    public CarLoads() {
    }

    /**
     * Add a car type with specific loads
     *
     * @param type car type
     */
    public void addType(String type) {
        listCarLoads.put(type, new ArrayList<>());
    }

    /**
     * Replace a car type. Transfers load type, priority, isHardous, drop and
     * load comments.
     *
     * @param oldType old car type
     * @param newType new car type
     */
    public void replaceType(String oldType, String newType) {
        List<String> names = getNames(oldType);
        addType(newType);
        for (String name : names) {
            addName(newType, name);
            setLoadType(newType, name, getLoadType(oldType, name));
            setPriority(newType, name, getPriority(oldType, name));
            setHazardous(newType, name, isHazardous(oldType, name));
            setDropComment(newType, name, getDropComment(oldType, name));
            setPickupComment(newType, name, getPickupComment(oldType, name));
        }
        listCarLoads.remove(oldType);
    }

    /**
     * Gets the appropriate car loads for the car's type.
     *
     * @param type Car type
     *
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
     * @param type Car type
     *
     * @return JComboBox with car loads.
     */
    public JComboBox<String> getComboBox(String type) {
        JComboBox<String> box = new JComboBox<>();
        updateComboBox(type, box);
        return box;

    }

    /**
     * Gets a ComboBox with the available priorities
     *
     * @return JComboBox with car priorities.
     */
    public JComboBox<String> getPriorityComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(CarLoad.PRIORITY_LOW);
        box.addItem(CarLoad.PRIORITY_MEDIUM);
        box.addItem(CarLoad.PRIORITY_HIGH);
        return box;
    }
    
    public JComboBox<String> getHazardousComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(Bundle.getMessage("ButtonNo"));
        box.addItem(Bundle.getMessage("ButtonYes"));
        return box;
    }

    /**
     * Gets a ComboBox with the available load types: empty and load
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
     * Gets a sorted list of load names for a given car type
     *
     * @param type car type
     * @return list of load names
     */
    public List<String> getNames(String type) {
        List<String> names = new ArrayList<>();
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
        if (loads.isEmpty()) {
            loads.add(new CarLoad(getDefaultEmptyName()));
            loads.add(new CarLoad(getDefaultLoadName()));
        }
        for (CarLoad carLoad : loads) {
            names.add(carLoad.getName());
        }
        java.util.Collections.sort(names);
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
        loads.add(new CarLoad(name));
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
        List<String> names = getNames(type);
        for (String name : names) {
            box.addItem(name);
        }
        OperationsPanel.padComboBox(box, getMaxNameLength() + 1);
    }

    /**
     * Update a JComboBox with all load names for every type of car.
     *
     * @param box the combo box to update
     */
    @Override
    public void updateComboBox(JComboBox<String> box) {
        box.removeAllItems();
        List<String> names = new ArrayList<>();
        for (String type : InstanceManager.getDefault(CarTypes.class).getNames()) {
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
    }
    
    public void updateRwlComboBox(String type, JComboBox<String> box) {
        box.removeAllItems();
        List<String> loads = getNames(type);
        for (String name : loads) {
            if (getLoadType(type, name).equals(CarLoad.LOAD_TYPE_LOAD)) {
                box.addItem(name);
            }
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
     * Get the load type, empty or load.
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
     * @param priority load priority, PRIORITY_LOW, PRIORITY_MEDIUM or PRIORITY_HIGH.
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
     * @return load priority, PRIORITY_LOW, PRIORITY_MEDIUM or PRIORITY_HIGH.
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
    
    public void setHazardous(String type, String name, boolean isHazardous) {
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                boolean oldIsHazardous = cl.isHazardous();
                cl.setHazardous(isHazardous);
                if (oldIsHazardous != isHazardous) {
                    setDirtyAndFirePropertyChange(LOAD_HAZARDOUS_CHANGED_PROPERTY, oldIsHazardous, isHazardous);
                }
            }
        }
    }
    
    public boolean isHazardous(String type, String name) {
        if (!containsName(type, name)) {
            return false;
        }
        List<CarLoad> loads = listCarLoads.get(type);
        for (CarLoad cl : loads) {
            if (cl.getName().equals(name)) {
                return cl.isHazardous();
            }
        }
        return false;
    }

    /**
     * Sets the comment for a car type's load
     * @param type the car type
     * @param name the load name
     * @param comment the comment
     */
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
                    maxCommentLength = 0;
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
                    maxCommentLength = 0;
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

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
            justification="I18N of Info Message")
    @Override
    public int getMaxNameLength() {
        if (maxNameLength == 0) {
            maxName = "";
            maxNameLength = MIN_NAME_LENGTH;
            String carTypeName = "";
            Enumeration<String> en = listCarLoads.keys();
            while (en.hasMoreElements()) {
                String cartype = en.nextElement();
                List<CarLoad> loads = listCarLoads.get(cartype);
                for (CarLoad load : loads) {
                    if (load.getName().split(TrainCommon.HYPHEN)[0].length() > maxNameLength) {
                        maxName = load.getName().split(TrainCommon.HYPHEN)[0];
                        maxNameLength = load.getName().split(TrainCommon.HYPHEN)[0].length();
                        carTypeName = cartype;
                    }
                }
            }
            log.info(Bundle.getMessage("InfoMaxLoad", maxName, maxNameLength, carTypeName));
        }
        return maxNameLength;
    }
    
    int maxCommentLength = 0;
    
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SLF4J_FORMAT_SHOULD_BE_CONST",
            justification = "I18N of Info Message")
    public int getMaxLoadCommentLength() {
        if (maxCommentLength == 0) {
            String maxComment = "";
            String carTypeName = "";
            String carLoadName = "";
            Enumeration<String> en = listCarLoads.keys();
            while (en.hasMoreElements()) {
                String carType = en.nextElement();
                List<CarLoad> loads = listCarLoads.get(carType);
                for (CarLoad load : loads) {
                    if (load.getDropComment().length() > maxCommentLength) {
                        maxComment = load.getDropComment();
                        maxCommentLength = load.getDropComment().length();
                        carTypeName = carType;
                        carLoadName = load.getName();
                    }
                    if (load.getPickupComment().length() > maxCommentLength) {
                        maxComment = load.getPickupComment();
                        maxCommentLength = load.getPickupComment().length();
                        carTypeName = carType;
                        carLoadName = load.getName();
                    }
                }
            }
            if (maxCommentLength < TrainManifestHeaderText.getStringHeader_Drop_Comment().length()) {
                maxCommentLength = TrainManifestHeaderText.getStringHeader_Drop_Comment().length();
            }
            if (maxCommentLength < TrainManifestHeaderText.getStringHeader_Pickup_Comment().length()) {
                maxCommentLength = TrainManifestHeaderText.getStringHeader_Pickup_Comment().length();
            }
            log.info(Bundle.getMessage("InfoMaxLoadMessage", maxComment, maxCommentLength,
                    carTypeName, carLoadName));
        }
        return maxCommentLength;
    }

    private List<CarLoad> getSortedList(String type) {
        List<CarLoad> loads = listCarLoads.get(type);
        List<String> names = getNames(type);
        List<CarLoad> out = new ArrayList<>();

        // return a list sorted by load name
        for (String name : names) {
            for (CarLoad carLoad : loads) {
                if (name.equals(carLoad.getName())) {
                    out.add(carLoad);
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

    @Override
    public void dispose() {
        listCarLoads.clear();
        setDefaultEmptyName(Bundle.getMessage("EmptyCar"));
        setDefaultLoadName(Bundle.getMessage("LoadedCar"));
        super.dispose();
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     * @param root The common Element for operations-cars.dtd.
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
            if (!InstanceManager.getDefault(CarTypes.class).containsName(carType)) {
                continue;
            }
            List<CarLoad> loads = getSortedList(carType);
            Element xmlLoad = new Element(Xml.LOAD);
            xmlLoad.setAttribute(Xml.TYPE, carType);
            boolean mustStore = false; // only store loads that aren't the defaults
            for (CarLoad load : loads) {
                // don't store the defaults / low priority / not hazardous / no comment
                if ((load.getName().equals(getDefaultEmptyName()) || load.getName().equals(getDefaultLoadName()))
                        && load.getPriority().equals(CarLoad.PRIORITY_LOW)
                        && !load.isHazardous()
                        && load.getPickupComment().equals(CarLoad.NONE)
                        && load.getDropComment().equals(CarLoad.NONE)) {
                    continue;
                }
                Element xmlCarLoad = new Element(Xml.CAR_LOAD);
                xmlCarLoad.setAttribute(Xml.NAME, load.getName());
                if (!load.getPriority().equals(CarLoad.PRIORITY_LOW)) {
                    xmlCarLoad.setAttribute(Xml.PRIORITY, load.getPriority());
                    mustStore = true; // must store
                }
                if (load.isHazardous()) {
                    xmlCarLoad.setAttribute(Xml.HAZARDOUS, load.isHazardous() ? Xml.TRUE : Xml.FALSE);
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
        List<Element> eLoads = e.getChild(Xml.LOADS).getChildren(Xml.LOAD);
        log.debug("readFile sees {} car loads", eLoads.size());
        for (Element eLoad : eLoads) {
            if ((a = eLoad.getAttribute(Xml.TYPE)) != null) {
                String type = a.getValue();
                addType(type);
                // old style had a list of names
                if ((a = eLoad.getAttribute(Xml.NAMES)) != null) {
                    String names = a.getValue();
                    String[] loadNames = names.split("%%");// NOI18N
                    Arrays.sort(loadNames);
                    log.debug("Car load type: {} loads: {}", type, names);
                    // addName puts new items at the start, so reverse load
                    for (int j = loadNames.length; j > 0;) {
                        addName(type, loadNames[--j]);
                    }
                }
                // new style load and comments
                List<Element> eCarLoads = eLoad.getChildren(Xml.CAR_LOAD);
                log.debug("{} car loads for type: {}", eCarLoads.size(), type);
                for (Element eCarLoad : eCarLoads) {
                    if ((a = eCarLoad.getAttribute(Xml.NAME)) != null) {
                        String name = a.getValue();
                        addName(type, name);
                        if ((a = eCarLoad.getAttribute(Xml.PRIORITY)) != null) {
                            setPriority(type, name, a.getValue());
                        }
                        if ((a = eCarLoad.getAttribute(Xml.HAZARDOUS)) != null) {
                            setHazardous(type, name, a.getValue().equals(Xml.TRUE));
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
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(CarLoads.class);

}
