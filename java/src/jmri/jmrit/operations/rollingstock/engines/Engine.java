package jmri.jmrit.operations.rollingstock.engines;

import java.beans.PropertyChangeEvent;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.routes.RouteLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a locomotive on the layout
 *
 * @author Daniel Boudreau (C) Copyright 2008
 * @version $Revision$
 */
public class Engine extends RollingStock {

    private Consist _consist = null;
    private String _model = NONE;

    EngineModels engineModels = EngineModels.instance();

    public Engine(String road, String number) {
        super(road, number);
        log.debug("New engine ({} {})", road, number);
        addPropertyChangeListeners();
    }

    /**
     * Set the locomotive's model. Note a model has only one length, type, and
     * horsepower rating.
     *
     * @param model
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
            setDirtyAndFirePropertyChange("hp", old, hp); // NOI18N
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
            log.warn("Locomotive ({}) horsepower ({}) isn't a number", toString(), getHp());
            return 0;
        }
    }

    /**
     * Set the locomotive length for this locomotive's model
     *
     * @param length locomotive length
     */
    public void setLength(String length) {
        super.setLength(length);
        try {
           if (getModel().equals(NONE)) {
               return;
           }
           engineModels.setModelLength(getModel(), length);
        } catch(java.lang.NullPointerException npe){
          // failed, but the model may not have been set.
          log.debug("NPE setting length for Engine ({})", toString());
        }
        return;
    }

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
                if (_lengthChange) // return "old" length, used for track reserve changes
                {
                    return _length;
                }
                log.debug("Loco ({}) length has been modified", toString());
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
        } catch(java.lang.NullPointerException npe) {
           // this failed, was the model set?
           log.debug("NPE setting Weight Tons for Engine ({})", toString());
        }
    }

    public String getWeightTons() {
        String weight = null;
        try{
           weight = engineModels.getModelWeight(getModel());
           if (weight == null) {
               weight = NONE;
           }
       } catch(java.lang.NullPointerException npe){
          log.debug("NPE getting Weight Tons for Engine ({})", toString());
          weight = NONE;
       }
       return weight;
    }

    /**
     * Place locomotive in a consist
     *
     * @param consist
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
     * Used to check destination track to see if it will accept locomotive
     *
     * @return status, see RollingStock.java
     */
    public String testDestination(Location destination, Track track) {
        return super.testDestination(destination, track);
    }

    protected void moveRollingStock(RouteLocation old, RouteLocation next) {
        if (old == getRouteLocation()) {
            if (getConsist() == null || (getConsist() != null && getConsist().isLead(this))) {
                if (getTrain() != null && getRouteLocation() != getRouteDestination()
                        && getTrain().getLeadEngine() != this) {
                    log.debug("New lead locomotive ({}) for train ({})", toString(), getTrain().getName());
                    getTrain().setLeadEngine(this);
                    getTrain().createTrainIcon();
                }
            }
        }
        super.moveRollingStock(old, next);
    }

    public void dispose() {
        setConsist(null);
        EngineTypes.instance().removePropertyChangeListener(this);
        EngineLengths.instance().removePropertyChangeListener(this);
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
        if ((a = e.getAttribute(Xml.CONSIST)) != null) {
            Consist c = EngineManager.instance().getConsistByName(a.getValue());
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
        if (getConsist() != null) {
            e.setAttribute(Xml.CONSIST, getConsistName());
            if (getConsist().isLead(this)) {
                e.setAttribute(Xml.LEAD_CONSIST, Xml.TRUE);
                if (getConsist().getConsistNumber() > 0) {
                    e.setAttribute(Xml.CONSIST_NUM,
                            Integer.toString(getConsist().getConsistNumber()));
                }
            }
        }
        return e;
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        EngineManagerXml.instance().setDirty(true);
        super.setDirtyAndFirePropertyChange(p, old, n);
    }

    private void addPropertyChangeListeners() {
        EngineTypes.instance().addPropertyChangeListener(this);
        EngineLengths.instance().addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getTypeName())) {
                if (log.isDebugEnabled()) {
                    log.debug("Loco ({} {}) sees type name change old: ({}) new: ({})", toString(), e.getOldValue(), e
                            .getNewValue()); // NOI18N
                }
                setTypeName((String) e.getNewValue());
            }
        }
        if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_NAME_CHANGED_PROPERTY)) {
            if (e.getOldValue().equals(getLength())) {
                if (log.isDebugEnabled()) {
                    log.debug("Loco ({}) sees length name change old: {} new: {}", toString(), e.getOldValue(), e
                            .getNewValue()); // NOI18N
                }
                setLength((String) e.getNewValue());
            }
        }
    }

    static Logger log = LoggerFactory.getLogger(Engine.class.getName());

}
