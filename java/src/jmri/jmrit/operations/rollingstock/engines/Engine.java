package jmri.jmrit.operations.rollingstock.engines;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Represents a locomotive on the layout
 *
 * @author Daniel Boudreau (C) Copyright 2008
 */
public class Engine extends RollingStock {

    public static final int NCE_REAR_BLOCK_NUMBER = 8;
    public static final int B_UNIT_BLOCKING = 10; // block B Units after NCE Consists
    public static final String HP_CHANGED_PROPERTY = "hp"; // NOI18N

    private Consist _consist = null;
    private String _model = NONE;

    EngineModels engineModels = InstanceManager.getDefault(EngineModels.class);

    public Engine(String road, String number) {
        super(road, number);
        log.debug("New engine ({} {})", road, number);
        addPropertyChangeListeners();
    }

    /**
     * Set the locomotive's model. Note a model has only one length, type, and
     * horsepower rating.
     * 
     * @param model The string model name.
     *
     */
    public void setModel(String model) {
        String old = _model;
        _model = model;
        if (!old.equals(model)) {
            setDirtyAndFirePropertyChange("engine model", old, model); // NOI18N
        }
    }

    public String getModel() {
        return _model;
    }

    /**
     * Set the locomotive type for this locomotive's model
     *
     * @param type Locomotive type: Steam, Diesel, Gas Turbine, etc.
     */
    @Override
    public void setTypeName(String type) {
        if (getModel() == null || getModel().equals(NONE)) {
            return;
        }
        String old = getTypeName();
        engineModels.setModelType(getModel(), type);
        if (!old.equals(type)) {
            setDirtyAndFirePropertyChange(TYPE_CHANGED_PROPERTY, old, type);
        }
    }

    @Override
    public String getTypeName() {
        String type = engineModels.getModelType(getModel());
        if (type == null) {
            type = super.getTypeName();
        }
        return type;
    }

    /**
     * Set the locomotive horsepower rating for this locomotive's model
     *
     * @param hp locomotive horsepower
     */
    public void setHp(String hp) {
        if (getModel().equals(NONE)) {
            return;
        }
        String old = getHp();
        engineModels.setModelHorsepower(getModel(), hp);
        if (!old.equals(hp)) {
            setDirtyAndFirePropertyChange(HP_CHANGED_PROPERTY, old, hp); // NOI18N
        }
    }

    public String getHp() {
        String hp = engineModels.getModelHorsepower(getModel());
        if (hp == null) {
            hp = NONE;
        }
        return hp;
    }

    public int getHpInteger() {
        try {
            return Integer.parseInt(getHp());
        } catch (NumberFormatException e) {
            log.debug("Locomotive ({}) horsepower ({}) isn't a number", toString(), getHp());
            return 0;
        }
    }

    /**
     * Set the locomotive length for this locomotive's model
     *
     * @param length locomotive length
     */
    @Override
    public void setLength(String length) {
        super.setLength(length);
        try {
            if (getModel().equals(NONE)) {
                return;
            }
            engineModels.setModelLength(getModel(), length);
        } catch (java.lang.NullPointerException npe) {
            // failed, but the model may not have been set.
            log.debug("NPE setting length for Engine ({})", toString());
        }
        return;
    }

    @Override
    public String getLength() {
        try {
            String length = super.getLength();
            if (getModel() != null && !getModel().equals(NONE)) {
                length = engineModels.getModelLength(getModel());
            }
            if (length == null) {
                length = NONE;
            }
            if (!length.equals(_length)) {
                // return "old" length, used for track reserve changes
                if (_lengthChange) {
                    return _length;
                }
                log.debug("Loco ({}) length ({}) has been modified from ({})", toString(), length, _length);
                super.setLength(length); // adjust track lengths
            }
            return length;
        } catch (java.lang.NullPointerException npe) {
            log.debug("NPE setting length for Engine ({})", toString());
        }
        return NONE;
    }

    /**
     * Set the locomotive weight for this locomotive's model
     *
     * @param weight locomotive weight
     */
    @Override
    public void setWeightTons(String weight) {
        try {
            if (getModel().equals(NONE)) {
                return;
            }
            String old = getWeightTons();
            super.setWeightTons(weight);
            engineModels.setModelWeight(getModel(), weight);
            if (!old.equals(weight)) {
                setDirtyAndFirePropertyChange("Engine Weight Tons", old, weight); // NOI18N
            }
        } catch (java.lang.NullPointerException npe) {
            // this failed, was the model set?
            log.debug("NPE setting Weight Tons for Engine ({})", toString());
        }
    }

    @Override
    public String getWeightTons() {
        String weight = null;
        try {
            weight = engineModels.getModelWeight(getModel());
            if (weight == null) {
                weight = NONE;
            }
        } catch (java.lang.NullPointerException npe) {
            log.debug("NPE getting Weight Tons for Engine ({})", toString());
            weight = NONE;
        }
        return weight;
    }

    public void setBunit(boolean bUnit) {
        if (getModel().equals(NONE)) {
            return;
        }
        boolean old = isBunit();
        engineModels.setModelBunit(getModel(), bUnit);
        if (old != bUnit) {
            setDirtyAndFirePropertyChange(TYPE_CHANGED_PROPERTY, old, bUnit);
        }
    }

    public boolean isBunit() {
        try {
            return engineModels.isModelBunit(getModel());
        } catch (java.lang.NullPointerException npe) {
            log.debug("NPE getting is B unit for Engine ({})", toString());
        }
        return false;
    }

    /**
     * Place locomotive in a consist
     * 
     * @param consist The Consist to use.
     *
     */
    public void setConsist(Consist consist) {
        if (_consist == consist) {
            return;
        }
        String old = "";
        if (_consist != null) {
            old = _consist.getName();
            _consist.delete(this);
        }
        _consist = consist;
        String newName = "";
        if (_consist != null) {
            _consist.add(this);
            newName = _consist.getName();
        }

        if (!old.equals(newName)) {
            setDirtyAndFirePropertyChange("consist", old, newName); // NOI18N
        }
    }

    /**
     * Get the consist for this locomotive
     *
     * @return null if locomotive isn't in a consist
     */
    public Consist getConsist() {
        return _consist;
    }

    public String getConsistName() {
        if (_consist != null) {
            return _consist.getName();
        }
        return NONE;
    }

    /**
     * Used to determine if engine is lead engine in a consist
     * 
     * @return true if lead engine in a consist
     */
    public boolean isLead() {
        if (getConsist() != null) {
            return getConsist().isLead(this);
        }
        return false;
    }

    /**
     * Get the DCC address for this engine from the JMRI roster. Does 4
     * attempts, 1st by road and number, 2nd by number, 3rd by dccAddress using
     * the engine's road number, 4th by id.
     * 
     * @return dccAddress
     */
    public String getDccAddress() {
        RosterEntry re = getRosterEntry();
        if (re != null) {
            return re.getDccAddress();
        }
        return NONE;
    }
    
    /**
     * Get the RosterEntry for this engine from the JMRI roster. Does 4
     * attempts, 1st by road and number, 2nd by number, 3rd by dccAddress using
     * the engine's road number, 4th by id.
     * 
     * @return RosterEntry, can be null
     */
    public RosterEntry getRosterEntry() {
        RosterEntry rosterEntry = null;
        // 1st by road name and number
        List<RosterEntry> list =
                Roster.getDefault().matchingList(getRoadName(), getNumber(), null, null, null, null, null);
        if (list.size() > 0) {
            rosterEntry = list.get(0);
            log.debug("Roster Loco found by road and number: {}", rosterEntry.getDccAddress());
            // 2nd by road number
        } else if (!getNumber().equals(NONE)) {
            list = Roster.getDefault().matchingList(null, getNumber(), null, null, null, null, null);
            if (list.size() > 0) {
                rosterEntry = list.get(0);
                log.debug("Roster Loco found by number: {}", rosterEntry.getDccAddress());
            }
        }
        // 3rd by dcc address
        if (rosterEntry == null) {
            list = Roster.getDefault().matchingList(null, null, getNumber(), null, null, null, null);
            if (list.size() > 0) {
                rosterEntry = list.get(0);
                log.debug("Roster Loco found by dccAddress: {}", rosterEntry.getDccAddress());
            }
        }
        // 4th by id
        if (rosterEntry == null) {
            list = Roster.getDefault().matchingList(null, null, null, null, null, null, getNumber());
            if (list.size() > 0) {
                rosterEntry = list.get(0);
                log.debug("Roster Loco found by roster id: {}", rosterEntry.getDccAddress());
            }
        }
        return rosterEntry;
    }

    /**
     * Used to check destination track to see if it will accept locomotive
     *
     * @return status, see RollingStock.java
     */
    @Override
    public String testDestination(Location destination, Track track) {
        return super.testDestination(destination, track);
    }

    /**
     * Determine if there's a change in the lead locomotive. There are two
     * possible locations in a train's route. TODO this code places the last
     * loco added to the train as the lead. It would be better if the first one
     * became the lead loco.
     */
    @Override
    protected void moveRollingStock(RouteLocation current, RouteLocation next) {
        if (current == getRouteLocation()) {
            if (getConsist() == null || isLead()) {
                if (getRouteLocation() != getRouteDestination() &&
                        getTrain() != null &&
                        !isBunit() &&
                        getTrain().getLeadEngine() != this) {
                    if (((getTrain().getSecondLegStartLocation() == current &&
                            (getTrain().getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES)) ||
                            ((getTrain().getThirdLegStartLocation() == current &&
                                    (getTrain().getThirdLegOptions() &
                                            Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES))) {
                        log.debug("New lead locomotive ({}) for train ({})", toString(), getTrain().getName());
                        getTrain().setLeadEngine(this);
                        getTrain().createTrainIcon(current);
                    }
                }
            }
        }
        super.moveRollingStock(current, next);
    }

    @Override
    public void dispose() {
        setConsist(null);
        InstanceManager.getDefault(EngineTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineLengths.class).removePropertyChangeListener(this);
        super.dispose();
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in operations-engines.dtd
     *
     * @param e Engine XML element
     */
    public Engine(org.jdom2.Element e) {
        super(e); // MUST create the rolling stock first!
        org.jdom2.Attribute a;
        // must set _model first so locomotive hp, length, type and weight is set properly
        if ((a = e.getAttribute(Xml.MODEL)) != null) {
            _model = a.getValue();
        }
        if ((a = e.getAttribute(Xml.HP)) != null) {
            setHp(a.getValue());
        }
        if ((a = e.getAttribute(Xml.LENGTH)) != null) {
            setLength(a.getValue());
        }
        if ((a = e.getAttribute(Xml.TYPE)) != null) {
            setTypeName(a.getValue());
        }
        if ((a = e.getAttribute(Xml.WEIGHT_TONS)) != null) {
            setWeightTons(a.getValue());
        }
        if ((a = e.getAttribute(Xml.B_UNIT)) != null) {
            setBunit(a.getValue().equals(Xml.TRUE));
        }
        if ((a = e.getAttribute(Xml.CONSIST)) != null) {
            Consist c = InstanceManager.getDefault(EngineManager.class).getConsistByName(a.getValue());
            if (c != null) {
                setConsist(c);
                if ((a = e.getAttribute(Xml.LEAD_CONSIST)) != null && a.getValue().equals(Xml.TRUE)) {
                    _consist.setLead(this);
                }
                if ((a = e.getAttribute(Xml.CONSIST_NUM)) != null) {
                    _consist.setConsistNumber(Integer.parseInt(a.getValue()));
                }
            } else {
                log.error("Consist " + a.getValue() + " does not exist");
            }
        }
        addPropertyChangeListeners();
    }

    boolean verboseStore = false;

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-engines.dtd.
     *
     * @return Contents in a JDOM Element
     */
    public org.jdom2.Element store() {
        org.jdom2.Element e = new org.jdom2.Element(Xml.ENGINE);
        super.store(e);
        e.setAttribute(Xml.MODEL, getModel());
        e.setAttribute(Xml.HP, getHp());
        e.setAttribute(Xml.B_UNIT, (isBunit() ? Xml.TRUE : Xml.FALSE));
        if (getConsist() != null) {
            e.setAttribute(Xml.CONSIST, getConsistName());
            if (isLead()) {
                e.setAttribute(Xml.LEAD_CONSIST, Xml.TRUE);
                if (getConsist().getConsistNumber() > 0) {
                    e.setAttribute(Xml.CONSIST_NUM,
                            Integer.toString(getConsist().getConsistNumber()));
                }
            }
        }
        return e;
    }

    @Override
    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(EngineManagerXml.class).setDirty(true);
        super.setDirtyAndFirePropertyChange(p, old, n);
    }

    private void addPropertyChangeListeners() {
        InstanceManager.getDefault(EngineTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(EngineLengths.class).addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getTypeName())) {
                log.debug("Loco ({} {}) sees type name change old: ({}) new: ({})", toString(), e.getOldValue(), e
                        .getNewValue()); // NOI18N
                setTypeName((String) e.getNewValue());
            }
        }
        if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getLength())) {
                log.debug("Loco ({}) sees length name change old: {} new: {}", toString(), e.getOldValue(), e
                        .getNewValue()); // NOI18N
                setLength((String) e.getNewValue());
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Engine.class);

}
