// EngineModels.java
package jmri.jmrit.operations.rollingstock.engines;

import java.util.Hashtable;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import jmri.jmrit.operations.setup.Control;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the various engine models a railroad can have. Each model has a
 * type, horsepower rating and length that is kept here. The program provides
 * some default models for the user. These values can be overridden by the user.
 *
 * Model Horsepower Length Type 
 * 
 * E8 2250 70 Diesel FT 1350 50 Diesel F3 1500 50
 * Diesel F7 1500 50 Diesel F9 1750 50 Diesel GP20 2000 56 Diesel GP30 2250 56
 * Diesel GP35 2500 56 Diesel GP38 2000 59 Diesel GP40 3000 59 Diesel RS1 1000
 * 51 Diesel RS2 1500 52 Diesel RS3 1600 51 Diesel RS11 1800 53 Diesel RS18 1800
 * 52 Diesel RS27 2400 57 Diesel RSD4 1600 52 Diesel SD26 2650 61 Diesel SD45
 * 3600 66 Diesel SW1200 1200 45 Diesel SW1500 1500 45 Diesel SW8 800 44 Diesel
 * TRAINMASTER 2400 66 Diesel U28B 2800 60 Diesel
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class EngineModels extends RollingStockAttribute {

    private static final String MODELS = Bundle.getMessage("engineDefaultModels");
    // Horsepower, length, and type have a one to one correspondence with the above MODELS
    private static final String HORSEPOWER = Bundle.getMessage("engineModelHorsepowers");
    private static final String ENGINELENGTHS = Bundle.getMessage("engineModelLengths");
    private static final String ENGINETYPES = Bundle.getMessage("engineModelTypes");
    private static final String ENGINEWEIGHTS = Bundle.getMessage("engineModelWeights");

    public static final String ENGINEMODELS_CHANGED_PROPERTY = "EngineModels"; // NOI18N
    public static final String ENGINEMODELS_NAME_CHANGED_PROPERTY = "EngineModelsName"; // NOI18N

    // protected List<String> _list = new ArrayList<String>();
    protected Hashtable<String, String> _engineHorsepowerHashTable = new Hashtable<String, String>();
    protected Hashtable<String, String> _engineLengthHashTable = new Hashtable<String, String>();
    protected Hashtable<String, String> _engineTypeHashTable = new Hashtable<String, String>();
    protected Hashtable<String, String> _engineWeightHashTable = new Hashtable<String, String>();
    protected Hashtable<String, Boolean> _engineBunitHashTable = new Hashtable<String, Boolean>();

    public EngineModels() {
    }

    /**
     * record the single instance *
     */
    private static EngineModels _instance = null;

    public static synchronized EngineModels instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("EngineModels creating instance");
            }
            // create and load
            _instance = new EngineModels();
            _instance.loadDefaults();
        }
        if (Control.showInstance) {
            log.debug("EngineModels returns instance {}", _instance);
        }
        return _instance;
    }

    protected String getDefaultNames() {
        return MODELS;
    }

    public void dispose() {
        _engineHorsepowerHashTable.clear();
        _engineLengthHashTable.clear();
        _engineTypeHashTable.clear();
        _engineWeightHashTable.clear();
        _engineBunitHashTable.clear();
        super.dispose();
        loadDefaults();
    }

    public void addName(String model) {
        super.addName(model);
        setDirtyAndFirePropertyChange(ENGINEMODELS_CHANGED_PROPERTY, null, model);
    }

    public void deleteName(String model) {
        super.deleteName(model);
        setDirtyAndFirePropertyChange(ENGINEMODELS_CHANGED_PROPERTY, model, null);
    }

    public void replaceName(String oldName, String newName) {
        super.addName(newName);
        setDirtyAndFirePropertyChange(ENGINEMODELS_NAME_CHANGED_PROPERTY, oldName, newName);
        super.deleteName(oldName);
    }

    public void setModelHorsepower(String model, String horsepower) {
        _engineHorsepowerHashTable.put(model, horsepower);
    }

    public String getModelHorsepower(String model) {
        return _engineHorsepowerHashTable.get(model);
    }

    public void setModelLength(String model, String horsepower) {
        _engineLengthHashTable.put(model, horsepower);
    }

    public String getModelLength(String model) {
        return _engineLengthHashTable.get(model);
    }

    public void setModelType(String model, String type) {
        _engineTypeHashTable.put(model, type);
    }

    public String getModelType(String model) {
        return _engineTypeHashTable.get(model);
    }
    
    public void setModelBunit(String model, boolean bUnit) {
        _engineBunitHashTable.put(model, bUnit);
    }
    
    public boolean isModelBunit(String model) {
        if (_engineBunitHashTable.containsKey(model))
            return _engineBunitHashTable.get(model);
        return false;
    }

    public void setModelWeight(String model, String type) {
        _engineWeightHashTable.put(model, type);
    }

    /**
     *
     * @param model The engine model (example GP20)
     * @return This model's weight in tons
     */
    public String getModelWeight(String model) {
        return _engineWeightHashTable.get(model);
    }

    private void loadDefaults() {
        String[] models = MODELS.split(","); // NOI18N
        String[] hps = HORSEPOWER.split(","); // NOI18N
        String[] lengths = ENGINELENGTHS.split(","); // NOI18N
        String[] types = ENGINETYPES.split(","); // NOI18N
        String[] weights = ENGINEWEIGHTS.split(","); // NOI18N
        if (models.length != hps.length || models.length != lengths.length || models.length != types.length
                || models.length != weights.length) {
            log.error("Defaults do not have the right number of items, " + "models=" + models.length + " hps="
                    + hps.length + " lengths=" + lengths.length // NOI18N
                    + " types=" + types.length); // NOI18N
            return;
        }

        for (int i = 0; i < models.length; i++) {
            setModelHorsepower(models[i], hps[i]);
            setModelLength(models[i], lengths[i]);
            setModelType(models[i], types[i]);
            setModelWeight(models[i], weights[i]);
            setModelBunit(models[i], false); // there are no B units in the default files
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-engines.dtd.
     *
     */
    public void store(Element root) {
        store(root, Xml.MODELS, Xml.MODEL, Xml.ENGINE_MODELS);
    }

    public void load(Element root) {
        load(root, Xml.MODELS, Xml.MODEL, Xml.ENGINE_MODELS);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        EngineManagerXml.instance().setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(EngineModels.class.getName());

}
