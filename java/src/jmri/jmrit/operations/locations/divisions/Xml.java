package jmri.jmrit.operations.locations.divisions;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2021
 * 
 *
 */
public class Xml {

    private Xml(){
        //class of constants
    }

    // Common to operation xml files
    protected static final String ID = "id"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String COMMENT = "comment"; // NOI18N

    // DivisionManager.java
    protected static final String DIVISIONS = "divisions"; // NOI18N

    // Location.java
    protected static final String DIVISION = "division"; // NOI18N

}
