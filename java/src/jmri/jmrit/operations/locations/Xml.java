package jmri.jmrit.operations.locations;

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
    protected static final String ID = "id"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String CAR_TYPES = "carTypes"; // NOI18N
    protected static final String DIR = "dir"; // NOI18N
    protected static final String COMMENT = "comment"; // NOI18N
    protected static final String SCHEDULE = "schedule"; // NOI18N
    protected static final String TRACK = "track"; // NOI18N
    protected static final String TYPES = "types"; // NOI18N
    protected static final String LOCO_TYPE = "locoType"; // NOI18N
    protected static final String CAR_TYPE = "carType"; // NOI18N

    protected static final String TRUE = "true"; // NOI18N
    protected static final String FALSE = "false"; // NOI18N

    // LocationManager.java
    protected static final String LOCATIONS = "locations"; // NOI18N

    // Location.java
    protected static final String LOCATION = "location"; // NOI18N
    protected static final String OPS = "ops"; // NOI18N
    protected static final String SWITCH_LIST = "switchList"; // NOI18N
    protected static final String SWITCH_LIST_STATE = "switchListState"; // NOI18N
    protected static final String PRINTER_NAME = "printerName"; // NOI18N

    protected static final String EAST_TRAIN_ICON_X = "eastTrainIconX"; // NOI18N
    protected static final String EAST_TRAIN_ICON_Y = "eastTrainIconY"; // NOI18N
    protected static final String WEST_TRAIN_ICON_X = "westTrainIconX"; // NOI18N
    protected static final String WEST_TRAIN_ICON_Y = "westTrainIconY"; // NOI18N
    protected static final String NORTH_TRAIN_ICON_X = "northTrainIconX"; // NOI18N
    protected static final String NORTH_TRAIN_ICON_Y = "northTrainIconY"; // NOI18N
    protected static final String SOUTH_TRAIN_ICON_X = "southTrainIconX"; // NOI18N
    protected static final String SOUTH_TRAIN_ICON_Y = "southTrainIconY"; // NOI18N
    protected static final String TRAIN_ICON_RANGE_X = "trainIconRangeX"; // NOI18N
    protected static final String TRAIN_ICON_RANGE_Y = "trainIconRangeY"; // NOI18N
    protected static final String READER = "idReader"; // NOI18N

    protected static final String PHYSICAL_LOCATION = "physicalLocation"; // NOI18N
    protected static final String SWITCH_LIST_COMMENT = "switchListComment"; // NOI18N
    protected static final String SECONDARY = "secondary"; // early version of operations called tracks "secondary" // NOI18N
    protected static final String DIVISION_ID_ERROR = "DivisionId"; // NOI18N
    protected static final String DIVISION_ID = "divisionId"; // NOI18N

    // Track.java
    protected static final String LOC_TYPE = "locType"; // NOI18N
    protected static final String TRACK_TYPE = "trackType"; // NOI18N
    protected static final String LENGTH = "length"; // NOI18N
    protected static final String MOVES = "moves"; // NOI18N
    protected static final String TRACK_PRIORITY = "trackPriority"; // NOI18N
    protected static final String BLOCKING_ORDER = "blockingOrder"; // NOI18N
    protected static final String CAR_ROAD_OPERATION = "carRoadOperation"; // NOI18N misspelled should have been carRoadOption
    protected static final String CAR_ROAD_OPTION = "carRoadOption"; // NOI18N
    protected static final String CAR_ROADS = "carRoads"; // NOI18N
    protected static final String CAR_ROAD = "carRoad"; // NOI18N
    protected static final String CAR_LOAD_OPTION = "carLoadOption"; // NOI18N
    protected static final String CAR_SHIP_LOAD_OPTION = "carShipLoadOption"; // NOI18N
    protected static final String CAR_LOADS = "carLoads"; // NOI18N
    protected static final String CAR_SHIP_LOADS = "carShipLoads"; // NOI18N
    protected static final String CAR_LOAD = "carLoad"; // NOI18N
    protected static final String DROP_IDS = "dropIds"; // NOI18N
    protected static final String DROP_ID = "dropId"; // NOI18N
    protected static final String DROP_OPTION = "dropOption"; // NOI18N
    protected static final String PICKUP_IDS = "pickupIds"; // NOI18N
    protected static final String PICKUP_ID = "pickupId"; // NOI18N
    protected static final String PICKUP_OPTION = "pickupOption"; // NOI18N
    protected static final String COMMENTS = "comments"; // NOI18N
    protected static final String BOTH = "both"; // NOI18N
    protected static final String PICKUP = "pickup"; // NOI18N
    protected static final String SETOUT = "setout"; // NOI18N
    protected static final String PRINT_MANIFEST = "printManifest"; // NOI18N
    protected static final String PRINT_SWITCH_LISTS = "printSwitchLists"; // NOI18N
    protected static final String TRACK_DESTINATION_OPTION = "trackDestinationOption"; // NOI18N
    protected static final String DESTINATIONS = "destinations"; // NOI18N
    protected static final String DESTINATION = "destination"; // NOI18N

    protected static final String SCHEDULE_ID = "scheduleId"; // NOI18N
    protected static final String ITEM_ID = "itemId"; // NOI18N
    protected static final String ITEM_COUNT = "itemCount"; // NOI18N
    protected static final String FACTOR = "factor"; // NOI18N
    protected static final String SCHEDULE_MODE = "scheduleMode"; // NOI18N
    protected static final String ALTERNATIVE = "alternative"; // NOI18N
    protected static final String LOAD_OPTIONS = "loadOptions"; // NOI18N
    protected static final String BLOCK_OPTIONS = "blockOptions"; // NOI18N
    protected static final String ORDER = "order"; // NOI18N
    protected static final String POOL = "pool"; // NOI18N
    protected static final String MIN_LENGTH = "minLength"; // NOI18N
    protected static final String MAX_LENGTH = "maxLength"; // NOI18N
    protected static final String IGNORE_USED_PERCENTAGE = "ignoreUsedPercentage"; // NOI18N
    protected static final String HOLD_CARS_CUSTOM = "holdCustomLoads"; // NOI18N
    protected static final String ONLY_CARS_WITH_FD = "onlyCarWithFD"; // NOI18N

}
