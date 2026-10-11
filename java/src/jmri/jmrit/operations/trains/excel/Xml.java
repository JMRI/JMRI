package jmri.jmrit.operations.trains.excel;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 *
 */
public class Xml {

    private Xml(){
       // class of constants
    }

    // Common to operation xml files
    protected static final String NAME = "name"; // NOI18N

    // ManifestCreator.java
    protected static final String MANIFEST_CREATOR = "manifestCreator"; // NOI18N
    protected static final String RUN_FILE = "runFile"; // NOI18N
    protected static final String DIRECTORY = "directory"; // NOI18N
    protected static final String COMMON_FILE = "commonFile"; // NOI18N

    // SwitchListCreator
    protected static final String SWITCHLIST_CREATOR = "switchlistCreator"; // NOI18N
}
