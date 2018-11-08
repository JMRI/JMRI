package jmri.jmrit.operations.setup;

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

    // Control.java
    static final String CONTROL = "control"; // NOI18N
    static final String BACKWARD_COMPATIBILITY = "backwardCompatibility"; // NOI18N
    static final String SAVE_USING_PRE_2013_FORMAT = "saveUsingPre_2013_Format"; // NOI18N
    static final String LENGTH = "length"; // NOI18N
    static final String MAXIMUM_STRING_LENGTHS = "maximumStringLengths"; // NOI18N
    static final String MAX_LEN_STRING_ATTRIBUTE = "max_len_string_attibute"; // NOI18N
    static final String MAX_LEN_STRING_ROAD_NUMBER = "max_len_string_road_number"; // NOI18N
    static final String MAX_LEN_STRING_PRINT_ROAD_NUMBER = "max_len_string_print_road_number"; // NOI18N
    static final String MAX_LEN_STRING_LOCATION_NAME = "max_len_string_location_name"; // NOI18N
    static final String MAX_LEN_STRING_TRACK_NAME = "max_len_string_track_name"; // NOI18N
    static final String MAX_LEN_STRING_TRACK_LENGTH_NAME = "max_len_string_track_length_name"; // NOI18N
    static final String MAX_LEN_STRING_LENGTH_NAME = "max_len_string_length_name"; // NOI18N
    static final String MAX_LEN_STRING_WEIGHT_NAME = "max_len_string_weight_name"; // NOI18N
    static final String MAX_LEN_STRING_BUILT_NAME = "max_len_string_built_name"; // NOI18N
    static final String MAX_LEN_STRING_TRAIN_NAME = "max_len_string_train_name"; // NOI18N
    static final String MAX_LEN_STRING_ROUTE_NAME = "max_len_string_route_name"; // NOI18N
    static final String MAX_LEN_STRING_AUTOMATION_NAME = "max_len_string_automation_name"; // NOI18N
    static final String REPORTS = "reports"; // NOI18N
    
    static final String ACTIONS = "actions"; // NOI18N
    static final String EXCEL_WAIT_TIME = "excelWaitTime"; // NOI18N

    // Setup.java
    static final String OPERATIONS = "operations"; // NOI18N
    static final String RAIL_ROAD = "railRoad"; // NOI18N
    static final String USE_JMRI_RAILROAD_NAME = "useJmriRailroadName"; // NOI18N
    static final String SETTINGS = "settings"; // NOI18N
    static final String MAIN_MENU = "mainMenu"; // NOI18N
    static final String CLOSE_ON_SAVE = "closeOnSave"; // NOI18N 
    static final String AUTO_SAVE = "autoSave"; // NOI18N
    static final String AUTO_BACKUP = "autoBackup"; // NOI18N
    static final String TRAIN_DIRECTION = "trainDirection"; // NOI18N
    static final String TRAIN_LENGTH = "trainLength"; // NOI18N
    static final String MAX_ENGINES = "maxEngines"; // NOI18N
    static final String HPT = "hpt"; // NOI18N
    static final String SCALE = "scale"; // NOI18N
    static final String CAR_TYPES = "carTypes"; // NOI18N
    static final String SWITCH_TIME = "switchTime"; // NOI18N 
    static final String TRAVEL_TIME = "travelTime"; // NOI18N
    static final String SHOW_VALUE = "showValue"; // NOI18N
    static final String VALUE_LABEL = "valueLabel"; // NOI18N
    static final String SHOW_RFID = "showRfid"; // NOI18N
    static final String RFID_LABEL = "rfidLabel"; // NOI18N
    static final String SETUP = "setup"; // NOI18N

    static final String ROUTER = "router"; // NOI18N
    static final String CAR_ROUTING_ENABLED = "carRoutingEnabled"; // NOI18N
    static final String CAR_ROUTING_VIA_YARDS = "carRoutingViaYards"; // NOI18N
    static final String CAR_ROUTING_VIA_STAGING = "carRoutingViaStaging"; // NOI18N
    static final String FORWARD_TO_YARD = "forwardToYard"; // NOI18N
    static final String ONLY_ACTIVE_TRAINS = "onlyActiveTrains"; // NOI18N
    static final String CHECK_CAR_DESTINATION = "checkCarDestination"; // NOI18N

    static final String LOGGER = "logger"; // NOI18N
    static final String CAR_LOGGER = "carLogger"; // NOI18N
    static final String ENGINE_LOGGER = "engineLogger"; // NOI18N
    static final String TRAIN_LOGGER = "trainLogger"; // NOI18N
    
    static final String MANIFEST_FILE_OPTIONS = "manifestFileOptions"; // NOI18N
    static final String MANIFEST_SAVE = "manifestSave"; // NOI18N

    static final String PRINT_LOC_COMMENTS = "printLocComments"; // NOI18N
    static final String PRINT_ROUTE_COMMENTS = "printRouteComments"; // NOI18N
    static final String PRINT_LOADS_EMPTIES = "printLoadsEmpties"; // NOI18N
    static final String PRINT_TRAIN_SCHEDULE = "printTimetable"; // NOI18N
    static final String USE12HR_FORMAT = "use12hrFormat"; // NOI18N
    static final String PRINT_VALID = "printValid"; // NOI18N
    static final String SORT_BY_TRACK = "sortByTrack"; // NOI18N
    static final String PRINT_HEADERS = "printHeaders"; // NOI18N
    static final String PRINT_CABOOSE_LOAD = "printCabooseLoad"; // NOI18N
    static final String PRINT_PASSENGER_LOAD = "printPassengerLoad"; // NOI18N
    
    static final String LENGTH_UNIT = "lengthUnit"; // NOI18N
    static final String YEAR_MODELED = "yearModeled"; // NOI18N

    static final String PICKUP_ENG_FORMAT = "pickupEngFormat"; // NOI18N
    static final String DROP_ENG_FORMAT = "dropEngFormat"; // NOI18N
    static final String PICKUP_CAR_FORMAT = "pickupCarFormat"; // NOI18N
    static final String DROP_CAR_FORMAT = "dropCarFormat"; // NOI18N
    static final String LOCAL_FORMAT = "localFormat"; // NOI18N
    static final String MISSING_CAR_FORMAT = "missingCarFormat"; // NOI18N

    static final String SWITCH_LIST_PICKUP_CAR_FORMAT = "switchListPickupCarFormat"; // NOI18N
    static final String SWITCH_LIST_DROP_CAR_FORMAT = "switchListDropCarFormat"; // NOI18N
    static final String SWITCH_LIST_LOCAL_FORMAT = "switchListLocalFormat"; // NOI18N

    static final String SAME_AS_MANIFEST = "sameAsManifest"; // NOI18N
    static final String REAL_TIME = "realTime"; // NOI18N
    static final String ALL_TRAINS = "allTrains"; // NOI18N
    static final String PAGE_MODE = "pageMode"; // NOI18N, backwards compatible for versions before 3.11
    static final String PAGE_FORMAT = "pageFormat"; // NOI18N
    static final String PAGE_NORMAL = "pageNormal"; // NOI18N
    static final String PAGE_PER_TRAIN = "pagePerTrain"; // NOI18N
    static final String PAGE_PER_VISIT = "pagePerVisit"; // NOI18N
    static final String PRINT_ROUTE_LOCATION = "printRouteLocation"; // NOI18N
    static final String TRACK_SUMMARY = "trackSummary"; // NOI18N

    static final String PREFIX = "prefix"; // NOI18N
    static final String SETTING = "setting"; // NOI18N

    static final String PANEL = "panel"; // NOI18N
    static final String TRAIN_ICONXY = "trainIconXY"; // NOI18N
    static final String TRAIN_ICON_APPEND = "trainIconAppend"; // NOI18N

    static final String FONT_NAME = "fontName"; // NOI18N
    static final String FONT_SIZE = "fontSize"; // NOI18N
    static final String SIZE = "size"; // NOI18N

    static final String PAGE_ORIENTATION = "pageOrientation"; // NOI18N
    static final String MANIFEST = "manifest"; // NOI18N
    static final String SWITCH_LIST = "switchList"; // NOI18N

    static final String MANIFEST_COLORS = "manifestColors"; // NOI18N
    static final String DROP_COLOR = "dropColor"; // NOI18N
    static final String PICKUP_COLOR = "pickupColor"; // NOI18N
    static final String LOCAL_COLOR = "localColor"; // NOI18N

    static final String TAB = "tab"; // NOI18N
    static final String TAB2_LENGTH = "tab2Length"; // NOI18N
    static final String TAB3_LENGTH = "tab3Length"; // NOI18N
    static final String ENABLED = "enabled"; // NOI18N

    static final String COLUMN_FORMAT = "columnFormat"; // NOI18N
    static final String TWO_COLUMNS = "twoColumns"; // NOI18N
    static final String MANIFEST_FORMAT = "manifestFormat";  // NOI18N
    static final String VALUE = "value"; // NOI18N
    static final String STANDARD = "0"; // NOI18N
    static final String TWO_COLUMN = "1"; // NOI18N
    static final String TWO_COLUMN_TRACK = "2"; // NOI18N

    static final String TRUNCATE = "truncate"; // NOI18N
    static final String USE_DEPARTURE_TIME = "useDepartureTime"; // NOI18N
    static final String USE_EDITOR = "useEditor"; // NOI18N
    static final String HAZARDOUS_MSG = "hazardousMsg"; // NOI18N

    static final String MANIFEST_LOGO = "manifestLogo"; // NOI18N

    static final String BUILD_OPTIONS = "buildOptions"; // NOI18N
    static final String AGGRESSIVE = "aggressive"; // NOI18N
    static final String NUMBER_PASSES = "numberPasses";  // NOI18N

    static final String ALLOW_LOCAL_INTERCHANGE = "allowLocalInterchange"; // NOI18N
    static final String ALLOW_LOCAL_SPUR = "allowLocalSiding"; // NOI18N
    static final String ALLOW_LOCAL_YARD = "allowLocalYard"; // NOI18N

    static final String STAGING_RESTRICTION_ENABLED = "stagingRestrictionEnabled"; // NOI18N
    static final String STAGING_TRACK_AVAIL = "stagingTrackAvail"; // NOI18N
    static final String ALLOW_RETURN_STAGING = "allowReturnStaging"; // NOI18N
    static final String PROMPT_STAGING_ENABLED = "promptStagingEnabled"; // NOI18N
    static final String PROMPT_TO_STAGING_ENABLED = "promptToStagingEnabled"; // NOI18N

    static final String GENERATE_CSV_MANIFEST = "generateCsvManifest"; // NOI18N
    static final String GENERATE_CSV_SWITCH_LIST = "generateCsvSwitchList"; // NOI18N

    static final String BUILD_REPORT = "buildReport"; // NOI18N
    static final String LEVEL = "level"; // NOI18N
    static final String INDENT = "indent"; // NOI18N
    static final String ROUTER_LEVEL = "routerLevel"; // NOI18N
    static final String ALWAYS_PREVIEW = "alwaysPreview"; // NOI18N

    static final String OWNER = "owner"; // NOI18N

    static final String ICON_COLOR = "iconColor"; // NOI18N
    static final String NORTH = "north"; // NOI18N
    static final String SOUTH = "south"; // NOI18N
    static final String EAST = "east"; // NOI18N
    static final String WEST = "west"; // NOI18N
    static final String LOCAL = "local"; // NOI18N
    static final String TERMINATE = "terminate"; // NOI18N

    static final String COMMENTS = "comments"; // NOI18N
    static final String MISPLACED_CARS = "misplacedCars"; // NOI18N
    
    static final String DISPLAY = "display"; // NOI18N
    static final String SHOW_TRACK_MOVES = "showTrackMoves"; // NOI18N

    static final String VSD = "vsd"; // NOI18N
    static final String ENABLE_PHYSICAL_LOCATIONS = "enablePhysicalLocations"; // NOI18N

    static final String CATS = "CATS"; // NOI18N
    static final String EXACT_LOCATION_NAME = "exactLocationName"; // NOI18N

}
