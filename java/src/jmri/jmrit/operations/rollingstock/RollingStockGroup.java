package jmri.jmrit.operations.rollingstock;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A group of rolling stock that is managed as one unit.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2013
 * @param <T> the type of RollingStock in this group
 */
public abstract class RollingStockGroup<T extends RollingStock> {

    protected String _name = "";
    protected T _lead = null;
    protected List<T> _group = new ArrayList<>();

    public RollingStockGroup(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    // for combo boxes
    @Override
    public String toString() {
        return _name;
    }

    public void add(T rs) {
        if (_group.contains(rs)) {
            log.debug("rs ({}) already part of group ({})", rs.toString(), getName());
            return;
        }
        if (_group.size() <= 0) {
            _lead = rs;
        }
        int oldSize = _group.size();
        _group.add(rs);
        firePropertyChange("grouplistLength", Integer.toString(oldSize), Integer.valueOf(_group.size())); // NOI18N
    }

    public void delete(T rs) {
        if (!_group.contains(rs)) {
            log.debug("rs ({}) not part of group ({})", rs.getId(), getName());
            return;
        }
        int oldSize = _group.size();
        _group.remove(rs);
        // need a new lead rs?
        removeLead(rs);
        firePropertyChange("grouplistLength", Integer.toString(oldSize), Integer.valueOf(_group.size())); // NOI18N
    }

    public List<T> getGroup() {
        return _group;
    }

    public int getTotalLength() {
        int length = 0;
        for (T rs : _group) {
            length = length + rs.getTotalLength();
        }
        return length;
    }

    /**
     * Get a group's adjusted weight
     *
     * @return group's weight
     */
    public int getAdjustedWeightTons() {
        int weightTons = 0;
        for (T rs : _group) {
            weightTons = weightTons + rs.getAdjustedWeightTons();
        }
        return weightTons;
    }

    public boolean isLead(T rs) {
        if (rs == _lead) {
            return true;
        }
        return false;
    }

    public T getLead() {
        return _lead;
    }

    /**
     * Gets the number of rolling stock in this group
     *
     * @return number of elements in this group
     */
    public int getSize() {
        return _group.size();
    }

    /**
     * Sets the lead for this group. RollingStock must be part of the group. The
     * rolling stock that make up this group will have the attributes of the
     * lead. However, the length attribute is the sum of all unit lengths plus
     * the coupler lengths.
     *
     * @param rs lead for this group.
     */
    public void setLead(T rs) {
        if (_group.contains(rs)) {
            _lead = rs;
        }
    }

    public void removeLead(T rs) {
        if (isLead(rs) && _group.size() > 0) {
            setLead(_group.get(0));
        }
    }

    public void dispose() {

    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(RollingStockGroup.class);
}
