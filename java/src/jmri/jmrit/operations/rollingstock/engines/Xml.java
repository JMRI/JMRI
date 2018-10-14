package jmri.jmrit.operations.rollingstock.engines;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 * 
 *
 */
public class Xml {

    // Common to operation xml files
    static final String NAME = "name"; // NOI18N
    static final String LENGTH = "length"; // NOI18N
    static final String MODEL = "model"; // NOI18N

    static final String TRUE = "true"; // NOI18N
    static final String FALSE = "false"; // NOI18N

    // Engine.java
    static final String ENGINE = "engine"; // NOI18N
    static final String HP = "hp"; // NOI18N
    static final String TYPE = "type"; // NOI18N
    static final String B_UNIT = "bUnit"; // NOI18N

    static final String WEIGHT_TONS = "weightTons"; // NOI18N
    static final String CONSIST = "consist"; // NOI18N
    static final String LEAD_CONSIST = "leadConsist"; // NOI18N
    static final String CONSIST_NUM = "consistNum"; // NOI18N

    // EngineManager.java
    static final String ENGINES_OPTIONS = "enginesOptions"; // NOI18N
    static final String COLUMN_WIDTHS = "columnWidths"; // backwards compatible TODO remove in 2013 after production release // NOI18N
    static final String OPTIONS = "options"; // NOI18N
    static final String CONSISTS = "consists";  // NOI18N
    static final String NEW_CONSISTS = "newConsists";  // NOI18N

    // EngineManagerXml.java
    static final String ENGINES = "engines";  // NOI18N

    // EngineModels.java
    static final String ENGINE_MODELS = "engineModels";  // NOI18N
    static final String MODELS = "models";  // NOI18N

    // EngineTypes.java
    static final String ENGINE_TYPES = "engineTypes";  // NOI18N
    static final String TYPES = "types"; // NOI18N

    // EngineLengths.java
    static final String ENGINE_LENGTHS = "engineLengths";  // NOI18N
    static final String LENGTHS = "lengths"; // NOI18N
    static final String VALUE = "value"; // NOI18N

}
