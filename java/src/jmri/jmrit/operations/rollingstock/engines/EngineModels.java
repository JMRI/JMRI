package jmri.jmrit.operations.rollingstock.engines;

import java.util.Hashtable;
import java.util.Set;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrit.operations.rollingstock.RollingStockAttribute;
import org.jdom2.Element;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the various engine models a railroad can have. Each model has a
 * type, horsepower rating, length, and weight that is kept here. The program provides
 * some default models for the user. These values can be overridden by the user.
 * <ul>
 * <li>Model Horsepower Length Weight Type</li>
 *
 * <li>E8 2250 70 150 Diesel</li>
 * <li>FT 1350 50 115 Diesel</li>
 * <li>F3 1500 50 115 Diesel</li>
 * <li>F7 1500 50 115 Diesel</li>
 * <li>F9 1750 50 115 Diesel</li>
 * <li>GP20 2000 56 120 Diesel</li>
 * <li>GP30 2250 56 130 Diesel</li>
 * <li>GP35 2500 56 130 Diesel</li>
 * <li>GP38 2000 59 125 Diesel</li>
 * <li>GP40 3000 59 122 Diesel</li>
 * <li>RS1 1000 51 124 Diesel</li>
 * <li>RS2 1500 52 115 Diesel</li>
 * <li>RS3 1600 51 114 Diesel</li>
 * <li>RS11 1800 53 125 Diesel</li>
 * <li>RS18 1800 52 118 Diesel</li>
 * <li>RS27 2400 57 132 Diesel</li>
 * <li>RSD4 1600 52 179 Diesel</li>
 * <li>SD26 2650 61 164 Diesel</li>
 * <li>SD45 3600 66 195 Diesel</li>
 * <li>SW1200 1200 45 124 Diesel</li>
 * <li>SW1500 1500 45 124 Diesel</li>
 * <li>SW8 800 44 115 Diesel</li>
 * <li>TRAINMASTER 2400 66 188 Diesel</li>
 * <li>U28B 2800 60 126 Diesel</li>
 * </ul>
 *
 * @author Daniel Boudreau Copyright (C) 2008
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
    protected Hashtable<String, String> _engineHorsepowerHashTable = new Hashtable<>();
    protected Hashtable<String, String> _engineLengthHashTable = new Hashtable<>();
    protected Hashtable<String, String> _engineTypeHashTable = new Hashtable<>();
    protected Hashtable<String, String> _engineWeightHashTable = new Hashtable<>();
    protected Hashtable<String, Boolean> _engineBunitHashTable = new Hashtable<>();

    public EngineModels() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     *             {@link jmri.InstanceManager#getDefault(java.lang.Class)}
     *             instead
     */
    @Deprecated
    public static synchronized EngineModels instance() {
        return InstanceManager.getDefault(EngineModels.class);
    }

    @Override
    protected String getDefaultNames() {
        return MODELS;
    }

    @Override
    public void dispose() {
        _engineHorsepowerHashTable.clear();
        _engineLengthHashTable.clear();
        _engineTypeHashTable.clear();
        _engineWeightHashTable.clear();
        _engineBunitHashTable.clear();
        super.dispose();
    }

    @Override
    public void addName(String model) {
        super.addName(model);
        setDirtyAndFirePropertyChange(ENGINEMODELS_CHANGED_PROPERTY, null, model);
    }

    @Override
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
        if (models.length != hps.length ||
                models.length != lengths.length ||
                models.length != types.length ||
                models.length != weights.length) {
            log.error("Defaults do not have the right number of items, " +
                    "models=" +
                    models.length +
                    " hps=" +
                    hps.length +
                    " lengths=" +
                    lengths.length // NOI18N
                    +
                    " types=" +
                    types.length); // NOI18N
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
     * @param root The common Element for operations-engines.dtd.
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
        InstanceManager.getDefault(EngineManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(EngineModels.class);

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(EngineModels.class)) {
                EngineModels instance = new EngineModels();
                instance.loadDefaults();
                return instance;
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(EngineModels.class);
            return set;
        }

    }
}
