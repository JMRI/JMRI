package jmri.jmrit.operations.routes;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 *
 */
public class Xml {

    // Common to operation xml files
    static final String ID = "id"; // NOI18N
    static final String NAME = "name"; // NOI18N
    static final String COMMENT = "comment"; // NOI18N

    static final String TRUE = "true"; // NOI18N
    static final String FALSE = "false"; // NOI18N
    static final String YES = "yes"; // NOI18N
    static final String NO = "no"; // NOI18N

    // Route.java
    static final String ROUTE = "route"; // NOI18N
    static final String LOCATION = "location"; // NOI18N

    // RouteLocation.java
    static final String LOCATION_ID = "locationId"; // NOI18N
    static final String TRAIN_DIRECTION = "trainDirection"; // NOI18N
    static final String MAX_TRAIN_LENGTH = "maxTrainLength"; // NOI18N
    static final String GRADE = "grade"; // NOI18N
    static final String MAX_CAR_MOVES = "maxCarMoves"; // NOI18N
    static final String RANDOM_CONTROL = "randomControl"; // NOI18N
    static final String PICKUPS = "pickups"; // NOI18N
    static final String DROPS = "drops"; // NOI18N
    static final String WAIT = "wait"; // NOI18N
    static final String DEPART_TIME = "departTime"; // NOI18N
    static final String TRAIN_ICON_X = "trainIconX"; // NOI18N
    static final String TRAIN_ICON_Y = "trainIconY"; // NOI18N
    static final String TRAIN_ICON_RANGE_X = "trainIconRangeX"; // NOI18N
    static final String TRAIN_ICON_RANGE_Y = "trainIconRangeY"; // NOI18N
    static final String SEQUENCE_ID = "sequenceId"; // NOI18N

    // RouteManagerXml.java
    static final String ROUTES = "routes"; // NOI18N

}
