package jmri.jmrit.operations.locations;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2012
 * 
 *
 */
public class Xml {

    // Common to operation xml files
    static final String ID = "id"; // NOI18N
    static final String NAME = "name"; // NOI18N
    static final String CAR_TYPES = "carTypes"; // NOI18N
    static final String DIR = "dir"; // NOI18N
    static final String COMMENT = "comment"; // NOI18N
    static final String SCHEDULE = "schedule"; // NOI18N
    static final String TRACK = "track"; // NOI18N
    static final String TYPES = "types"; // NOI18N
    static final String LOCO_TYPE = "locoType"; // NOI18N
    static final String CAR_TYPE = "carType"; // NOI18N

    static final String TRUE = "true"; // NOI18N
    static final String FALSE = "false"; // NOI18N

    // LocationManager.java
    static final String LOCATIONS = "locations"; // NOI18N

    // Location.java
    static final String LOCATION = "location"; // NOI18N
    static final String OPS = "ops"; // NOI18N
    static final String SWITCH_LIST = "switchList"; // NOI18N
    static final String SWITCH_LIST_STATE = "switchListState"; // NOI18N
    static final String PRINTER_NAME = "printerName"; // NOI18N

    static final String EAST_TRAIN_ICON_X = "eastTrainIconX"; // NOI18N
    static final String EAST_TRAIN_ICON_Y = "eastTrainIconY"; // NOI18N
    static final String WEST_TRAIN_ICON_X = "westTrainIconX"; // NOI18N
    static final String WEST_TRAIN_ICON_Y = "westTrainIconY"; // NOI18N
    static final String NORTH_TRAIN_ICON_X = "northTrainIconX"; // NOI18N
    static final String NORTH_TRAIN_ICON_Y = "northTrainIconY"; // NOI18N
    static final String SOUTH_TRAIN_ICON_X = "southTrainIconX"; // NOI18N
    static final String SOUTH_TRAIN_ICON_Y = "southTrainIconY"; // NOI18N
    static final String TRAIN_ICON_RANGE_X = "trainIconRangeX"; // NOI18N
    static final String TRAIN_ICON_RANGE_Y = "trainIconRangeY"; // NOI18N
    static final String READER = "idReader"; // NOI18N

    static final String PHYSICAL_LOCATION = "physicalLocation"; // NOI18N
    static final String SWITCH_LIST_COMMENT = "switchListComment"; // NOI18N
    static final String SECONDARY = "secondary"; // early version of operations called tracks "secondary" // NOI18N

    // Track.java
    static final String LOC_TYPE = "locType"; // NOI18N
    static final String LENGTH = "length"; // NOI18N
    static final String MOVES = "moves"; // NOI18N
    static final String BLOCKING_ORDER = "blockingOrder"; // NOI18N
    static final String CAR_ROAD_OPERATION = "carRoadOperation"; // NOI18N misspelled should have been carRoadOption
    static final String CAR_ROAD_OPTION = "carRoadOption"; // NOI18N
    static final String CAR_ROADS = "carRoads"; // NOI18N
    static final String CAR_ROAD = "carRoad"; // NOI18N
    static final String CAR_LOAD_OPTION = "carLoadOption"; // NOI18N
    static final String CAR_SHIP_LOAD_OPTION = "carShipLoadOption"; // NOI18N
    static final String CAR_LOADS = "carLoads"; // NOI18N
    static final String CAR_SHIP_LOADS = "carShipLoads"; // NOI18N
    static final String CAR_LOAD = "carLoad"; // NOI18N
    static final String DROP_IDS = "dropIds"; // NOI18N
    static final String DROP_ID = "dropId"; // NOI18N
    static final String DROP_OPTION = "dropOption"; // NOI18N
    static final String PICKUP_IDS = "pickupIds"; // NOI18N
    static final String PICKUP_ID = "pickupId"; // NOI18N
    static final String PICKUP_OPTION = "pickupOption"; // NOI18N
    static final String COMMENTS = "comments"; // NOI18N
    static final String BOTH = "both"; // NOI18N
    static final String PICKUP = "pickup"; // NOI18N
    static final String SETOUT = "setout"; // NOI18N
    static final String TRACK_DESTINATION_OPTION = "trackDestinationOption"; // NOI18N
    static final String DESTINATIONS = "destinations"; // NOI18N
    static final String DESTINATION = "destination"; // NOI18N

    static final String SCHEDULE_ID = "scheduleId"; // NOI18N
    static final String ITEM_ID = "itemId"; // NOI18N
    static final String ITEM_COUNT = "itemCount"; // NOI18N
    static final String FACTOR = "factor"; // NOI18N
    static final String SCHEDULE_MODE = "scheduleMode"; // NOI18N
    static final String ALTERNATIVE = "alternative"; // NOI18N
    static final String LOAD_OPTIONS = "loadOptions"; // NOI18N
    static final String BLOCK_OPTIONS = "blockOptions"; // NOI18N
    static final String ORDER = "order"; // NOI18N
    static final String POOL = "pool"; // NOI18N
    static final String MIN_LENGTH = "minLength"; // NOI18N
    static final String IGNORE_USED_PERCENTAGE = "ignoreUsedPercentage"; // NOI18N
    static final String HOLD_CARS_CUSTOM = "holdCustomLoads"; // NOI18N
    static final String ONLY_CARS_WITH_FD = "onlyCarWithFD"; // NOI18N

}
