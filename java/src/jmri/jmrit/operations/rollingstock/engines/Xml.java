package jmri.jmrit.operations.rollingstock.engines;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 * 
 *
 */
public class Xml {

    private Xml(){
        //class of constants
    }

    // Common to operation xml files
    protected static final String NAME = "name"; // NOI18N
    protected static final String LENGTH = "length"; // NOI18N
    protected static final String MODEL = "model"; // NOI18N

    protected static final String TRUE = "true"; // NOI18N
    protected static final String FALSE = "false"; // NOI18N

    // Engine.java
    protected static final String ENGINE = "engine"; // NOI18N
    protected static final String HP = "hp"; // NOI18N
    protected static final String TYPE = "type"; // NOI18N
    protected static final String B_UNIT = "bUnit"; // NOI18N

    protected static final String WEIGHT_TONS = "weightTons"; // NOI18N
    protected static final String CONSIST = "consist"; // NOI18N
    protected static final String LEAD_CONSIST = "leadConsist"; // NOI18N
    protected static final String CONSIST_NUM = "consistNum"; // NOI18N

    // EngineManager.java
    protected static final String ENGINES_OPTIONS = "enginesOptions"; // NOI18N
    protected static final String OPTIONS = "options"; // NOI18N
    protected static final String CONSISTS = "consists";  // NOI18N
    protected static final String NEW_CONSISTS = "newConsists";  // NOI18N

    // EngineManagerXml.java
    protected static final String ENGINES = "engines";  // NOI18N

    // EngineModels.java
    protected static final String ENGINE_MODELS = "engineModels";  // NOI18N
    protected static final String MODELS = "models";  // NOI18N

    // EngineTypes.java
    protected static final String ENGINE_TYPES = "engineTypes";  // NOI18N
    protected static final String TYPES = "types"; // NOI18N

    // EngineLengths.java
    protected static final String ENGINE_LENGTHS = "engineLengths";  // NOI18N
    protected static final String LENGTHS = "lengths"; // NOI18N
    protected static final String VALUE = "value"; // NOI18N

}
