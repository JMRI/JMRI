package jmri.jmrit.operations.rollingstock.cars;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 * 
 *
 */
public class Xml {

    private Xml() {
        // class of constants
    }

    // Common to operation xml files
    protected static final String NAME = "name"; // NOI18N
    protected static final String LOAD = "load"; // NOI18N
    protected static final String TYPE = "type"; // NOI18N

    protected static final String TRUE = "true"; // NOI18N
    protected static final String FALSE = "false"; // NOI18N

    // Car.java
    protected static final String CAR = "car"; // NOI18N
    protected static final String PASSENGER = "passenger"; // NOI18N
    protected static final String HAZARDOUS = "hazardous"; // NOI18N
    protected static final String CABOOSE = "caboose"; // NOI18N
    protected static final String FRED = "fred"; // NOI18N
    protected static final String UTILITY = "utility"; // NOI18N
    protected static final String KERNEL = "kernel"; // NOI18N
    protected static final String LEAD_KERNEL = "leadKernel"; // NOI18N

    protected static final String LOAD_FROM_STAGING = "loadFromStaging"; // NOI18N
    protected static final String WAIT = "wait"; // NOI18N
    protected static final String PICKUP_SCHEDULE_ID = "pickupId"; // NOI18N
    protected static final String ORDER = "order"; // NOI18N
    protected static final String SCHEDULE_ID = "scheduleId"; // NOI18N
    protected static final String NEXT_LOAD = "nextLoad"; // NOI18N
    protected static final String NEXT_DEST_ID = "nextDestId"; // NOI18N
    protected static final String NEXT_DEST_TRACK_ID = "nextDestTrackId"; // NOI18N
    protected static final String PREVIOUS_NEXT_DEST_ID = "previousNextDestId"; // NOI18N
    protected static final String PREVIOUS_NEXT_DEST_TRACK_ID = "previousNextDestTrackId"; // NOI18N 
    protected static final String PREVIOUS_SCHEDULE_ID = "previousScheduleId"; // NOI18N
    protected static final String RWE_DEST_ID = "rweDestId"; // NOI18N
    protected static final String RWE_DEST_TRACK_ID = "rweDestTrackId"; // NOI18N
    protected static final String RWE_LOAD = "rweLoad"; // NOI18N
    protected static final String RWL_DEST_ID = "rwlDestId"; // NOI18N
    protected static final String RWL_DEST_TRACK_ID = "rwlDestTrackId"; // NOI18N
    protected static final String RWL_LOAD = "rwlLoad"; // NOI18N
    protected static final String ROUTE_PATH = "routePath"; // NOI18N

    // CarManager.java
    protected static final String OPTIONS = "options"; // NOI18N
    protected static final String CARS = "cars"; // NOI18N
    protected static final String CARS_OPTIONS = "carsOptions"; // NOI18N
    protected static final String KERNELS = "kernels"; // NOI18N
    protected static final String NEW_KERNELS = "newKernels"; // NOI18N

    // CarTypes.java
    protected static final String CAR_TYPES = "carTypes"; // NOI18N
    protected static final String TYPES = "types"; // NOI18N

    // CarRoads.java
    protected static final String ROAD_NAMES = "roadNames"; // NOI18N
    protected static final String ROADS = "roads"; // NOI18N
    protected static final String ROAD = "road"; // NOI18N 

    // CarOwners.java
    protected static final String CAR_OWNERS = "carOwners"; // NOI18N
    protected static final String OWNERS = "owners"; // NOI18N
    protected static final String OWNER = "owner"; // NOI18N

    // CarColors.java
    protected static final String CAR_COLORS = "carColors"; // NOI18N
    protected static final String COLORS = "colors"; // NOI18N
    protected static final String COLOR = "color"; // NOI18N

    // CarLengths.java
    protected static final String CAR_LENGTHS = "carLengths"; // NOI18N
    protected static final String LENGTHS = "lengths"; // NOI18N
    protected static final String LENGTH = "length"; // NOI18N
    protected static final String VALUE = "value"; // NOI18N

    // CarLoads.java
    protected static final String LOADS = "loads"; // NOI18N
    protected static final String NAMES = "names"; // NOI18N old style had a list of names
    protected static final String DEFAULTS = "defaults"; // NOI18N
    protected static final String EMPTY = "empty"; // NOI18N 
    protected static final String CAR_LOAD = "carLoad"; // NOI18N
    protected static final String PRIORITY = "priority"; // NOI18N 
    protected static final String PICKUP_COMMENT = "pickupComment"; // NOI18N
    protected static final String DROP_COMMENT = "dropComment"; // NOI18N
    protected static final String LOAD_TYPE = "loadType"; // NOI18N

}
