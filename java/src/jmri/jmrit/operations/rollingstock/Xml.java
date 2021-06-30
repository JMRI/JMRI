package jmri.jmrit.operations.rollingstock;

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
    static final String ID = "id"; // NOI18N
    static final String NAME = "name"; // NOI18N
    static final String CAR_TYPES = "carTypes"; // NOI18N
    static final String DIR = "dir"; // NOI18N
    static final String COMMENT = "comment"; // NOI18N
    static final String TRACK = "track"; // NOI18N
    public static final String TYPE = "type"; // NOI18N
    static final String LENGTH = "length"; // NOI18N

    static final String TRUE = "true"; // NOI18N
    static final String FALSE = "false"; // NOI18N

    // RollingStock.java
    static final String ROAD_NUMBER = "roadNumber"; // NOI18N
    static final String ROAD_NAME = "roadName"; // NOI18N
    static final String COLOR = "color"; // NOI18N
    static final String WEIGHT = "weight"; // NOI18N
    static final String WEIGHT_TONS = "weightTons"; // NOI18N
    static final String BUILT = "built"; // NOI18N
    static final String LOCATION = "location"; // NOI18N
    static final String MOVES = "moves"; // NOI18N
    static final String OWNER = "owner"; // NOI18N
    static final String VALUE = "value"; // NOI18N
    static final String RFID = "rfid"; // NOI18N
    static final String TRAIN = "train"; // NOI18N
    static final String TRAIN_ID = "trainId"; // NOI18N
    static final String OUT_OF_SERVICE = "outOfService"; // NOI18N
    static final String DESTINATION = "destination"; // NOI18N
    static final String DES_TRACK = "desTrack"; // NOI18N
    static final String LOC_UNKNOWN = "locUnknown"; // NOI18N
    static final String DATE = "date"; // NOI18N
    static final String BLOCKING = "blocking"; // NOI18N
    static final String SELECTED = "selected"; // NOI18N

    static final String LOCATION_ID = "locationId"; // NOI18N
    static final String SEC_LOCATION_ID = "secLocationId"; // NOI18N
    static final String DESTINATION_ID = "destinationId"; // NOI18N
    static final String SEC_DESTINATION_ID = "secDestinationId"; // NOI18N
    static final String LAST_LOCATION_ID = "lastLocationId"; // NOI18N
    static final String ROUTE_LOCATION_ID = "routeLocationId"; // NOI18N
    static final String ROUTE_DESTINATION_ID = "routeDestinationId"; // NOI18N
    static final String LAST_ROUTE_ID = "lastRouteId"; // NOI18N
    static final String DIVISION_ID_ERROR = "DivisionId"; // NOI18N
    static final String DIVISION_ID = "divisionId"; // NOI18N

    // Car.java
    static final String PASSENGER = "passenger"; // NOI18N
    static final String LOAD = "load"; // NOI18N
    static final String SHIP = "ship"; // NOI18N
}
