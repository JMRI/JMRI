package jmri.jmrit.operations.rollingstock.cars;

import jmri.InstanceManager;

/**
 * Represents a car load, includes pickup and drop comments.
 *
 * @author Daniel Boudreau (C) 2010
 *
 */
public class CarLoad {

    public static final String NONE = "";

    public static final String PRIORITY_LOW = Bundle.getMessage("PriorityLow");
    public static final String PRIORITY_HIGH = Bundle.getMessage("PriorityHigh");

    public static final String LOAD_TYPE_EMPTY = Bundle.getMessage("EmptyTypeName");
    public static final String LOAD_TYPE_LOAD = Bundle.getMessage("LoadTypeName");

    public static final String SPLIT_CHAR = " & "; // used to combine car type and load in tracks and trains

    String _name;
    String _priority = PRIORITY_LOW;
    String _pickupComment = NONE;
    String _dropComment = NONE;
    String _loadType = LOAD_TYPE_LOAD;

    public CarLoad(String name) {
        setName(name);
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
        if (name.equals(InstanceManager.getDefault(CarLoads.class).getDefaultEmptyName())) {
            setLoadType(LOAD_TYPE_EMPTY);
        }
    }

    public String getPriority() {
        return _priority;
    }

    public void setPriority(String priority) {
        _priority = priority;
    }

    public String getPickupComment() {
        return _pickupComment;
    }

    public void setPickupComment(String comment) {
        _pickupComment = comment;
    }

    public String getDropComment() {
        return _dropComment;
    }

    public void setDropComment(String comment) {
        _dropComment = comment;
    }

    public String getLoadType() {
        return _loadType;
    }

    public void setLoadType(String type) {
        _loadType = type;
    }

}
