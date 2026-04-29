package jmri.jmrit.operations.setup;

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
    protected static final String ID = "id"; // NOI18N
    protected static final String NAME = "name"; // NOI18N
    protected static final String COMMENT = "comment"; // NOI18N

    protected static final String TRUE = "true"; // NOI18N
    protected static final String FALSE = "false"; // NOI18N
    protected static final String YES = "yes"; // NOI18N
    protected static final String NO = "no"; // NOI18N

    // Control.java
    protected static final String CONTROL = "control"; // NOI18N
    protected static final String BACKWARD_COMPATIBILITY = "backwardCompatibility"; // NOI18N
    protected static final String SAVE_USING_PRE_2013_FORMAT = "saveUsingPre_2013_Format"; // NOI18N
    protected static final String LENGTH = "length"; // NOI18N
    protected static final String MAXIMUM_STRING_LENGTHS = "maximumStringLengths"; // NOI18N
    protected static final String MAX_LEN_STRING_ATTRIBUTE = "max_len_string_attibute"; // NOI18N
    protected static final String MAX_LEN_STRING_ROAD_NUMBER = "max_len_string_road_number"; // NOI18N
    protected static final String MAX_LEN_STRING_PRINT_ROAD_NUMBER = "max_len_string_print_road_number"; // NOI18N
    protected static final String MAX_LEN_STRING_LOCATION_NAME = "max_len_string_location_name"; // NOI18N
    protected static final String MAX_LEN_STRING_TRACK_NAME = "max_len_string_track_name"; // NOI18N
    protected static final String MAX_LEN_STRING_TRACK_LENGTH_NAME = "max_len_string_track_length_name"; // NOI18N
    protected static final String MAX_LEN_STRING_LENGTH_NAME = "max_len_string_length_name"; // NOI18N
    protected static final String MAX_LEN_STRING_WEIGHT_NAME = "max_len_string_weight_name"; // NOI18N
    protected static final String MAX_LEN_STRING_BUILT_NAME = "max_len_string_built_name"; // NOI18N
    protected static final String MAX_LEN_STRING_TRAIN_NAME = "max_len_string_train_name"; // NOI18N
    protected static final String MAX_LEN_STRING_ROUTE_NAME = "max_len_string_route_name"; // NOI18N
    protected static final String MAX_LEN_STRING_AUTOMATION_NAME = "max_len_string_automation_name"; // NOI18N
    protected static final String REPORTS = "reports"; // NOI18N
    
    protected static final String ACTIONS = "actions"; // NOI18N
    protected static final String EXCEL_WAIT_TIME = "excelWaitTime"; // NOI18N
    
    protected static final String PRINT_OPTIONS = "printOptions"; // NOI18N
    protected static final String DISABLE_PRINT_IF_CUSTOM = "disablePrintIfCustom"; // NOI18N

    protected static final String SPEED_HPT = "speedHpt";
    protected static final String MPH = "MPH";
    
    protected static final String SHOW_CLONES = "showClones";

    // Setup.java
    protected static final String OPERATIONS = "operations"; // NOI18N
    protected static final String RAIL_ROAD = "railRoad"; // NOI18N
    protected static final String USE_JMRI_RAILROAD_NAME = "useJmriRailroadName"; // NOI18N
    protected static final String SETTINGS = "settings"; // NOI18N
    protected static final String MAIN_MENU = "mainMenu"; // NOI18N
    protected static final String CLOSE_ON_SAVE = "closeOnSave"; // NOI18N 
    protected static final String AUTO_SAVE = "autoSave"; // NOI18N
    protected static final String AUTO_BACKUP = "autoBackup"; // NOI18N
    protected static final String TRAIN_DIRECTION = "trainDirection"; // NOI18N
    protected static final String TRAIN_LENGTH = "trainLength"; // NOI18N
    protected static final String MAX_ENGINES = "maxEngines"; // NOI18N
    protected static final String HPT = "hpt"; // NOI18N
    protected static final String SCALE = "scale"; // NOI18N
    protected static final String CAR_TYPES = "carTypes"; // NOI18N
    protected static final String SWITCH_TIME = "switchTime"; // NOI18N 
    protected static final String TRAVEL_TIME = "travelTime"; // NOI18N
    protected static final String SHOW_VALUE = "showValue"; // NOI18N
    protected static final String VALUE_LABEL = "valueLabel"; // NOI18N
    protected static final String SHOW_RFID = "showRfid"; // NOI18N
    protected static final String RFID_LABEL = "rfidLabel"; // NOI18N
    protected static final String SETUP = "setup"; // NOI18N

    protected static final String ROUTER = "router"; // NOI18N
    protected static final String CAR_ROUTING_ENABLED = "carRoutingEnabled"; // NOI18N
    protected static final String CAR_ROUTING_VIA_YARDS = "carRoutingViaYards"; // NOI18N
    protected static final String CAR_ROUTING_VIA_STAGING = "carRoutingViaStaging"; // NOI18N
    protected static final String FORWARD_TO_YARD = "forwardToYard"; // NOI18N
    protected static final String ONLY_ACTIVE_TRAINS = "onlyActiveTrains"; // NOI18N
    protected static final String CHECK_CAR_DESTINATION = "checkCarDestination"; // NOI18N

    protected static final String LOGGER = "logger"; // NOI18N
    protected static final String CAR_LOGGER = "carLogger"; // NOI18N
    protected static final String ENGINE_LOGGER = "engineLogger"; // NOI18N
    protected static final String TRAIN_LOGGER = "trainLogger"; // NOI18N
    
    protected static final String MANIFEST_FILE_OPTIONS = "manifestFileOptions"; // NOI18N
    protected static final String MANIFEST_SAVE = "manifestSave"; // NOI18N

    protected static final String PRINT_LOC_COMMENTS = "printLocComments"; // NOI18N
    protected static final String PRINT_ROUTE_COMMENTS = "printRouteComments"; // NOI18N
    protected static final String PRINT_LOADS_EMPTIES = "printLoadsEmpties"; // NOI18N
    protected static final String PRINT_TRAIN_SCHEDULE = "printTimetable"; // NOI18N
    protected static final String USE12HR_FORMAT = "use12hrFormat"; // NOI18N
    protected static final String PRINT_VALID = "printValid"; // NOI18N
    protected static final String SORT_BY_TRACK = "sortByTrack"; // NOI18N
    protected static final String PRINT_PAGE_HEADER = "printPageHeader"; // NOI18N
    protected static final String PRINT_HEADERS = "printHeaders"; // NOI18N
    protected static final String PRINT_NO_PAGE_BREAKS = "printNoPageBreaks"; // NOI18N
    protected static final String PRINT_CABOOSE_LOAD = "printCabooseLoad"; // NOI18N
    protected static final String PRINT_PASSENGER_LOAD = "printPassengerLoad"; // NOI18N
    protected static final String GROUP_MOVES = "groupCarMoves"; // NOI18N
    protected static final String PRINT_LOCO_LAST = "printLocoLast"; // NOI18N
    
    protected static final String LENGTH_UNIT = "lengthUnit"; // NOI18N
    protected static final String YEAR_MODELED = "yearModeled"; // NOI18N

    protected static final String PICKUP_ENG_FORMAT = "pickupEngFormat"; // NOI18N
    protected static final String DROP_ENG_FORMAT = "dropEngFormat"; // NOI18N
    protected static final String PICKUP_CAR_FORMAT = "pickupCarFormat"; // NOI18N
    protected static final String DROP_CAR_FORMAT = "dropCarFormat"; // NOI18N
    protected static final String LOCAL_FORMAT = "localFormat"; // NOI18N
    protected static final String MISSING_CAR_FORMAT = "missingCarFormat"; // NOI18N

    protected static final String SWITCH_LIST_PICKUP_CAR_FORMAT = "switchListPickupCarFormat"; // NOI18N
    protected static final String SWITCH_LIST_DROP_CAR_FORMAT = "switchListDropCarFormat"; // NOI18N
    protected static final String SWITCH_LIST_LOCAL_FORMAT = "switchListLocalFormat"; // NOI18N

    protected static final String SAME_AS_MANIFEST = "sameAsManifest"; // NOI18N
    protected static final String REAL_TIME = "realTime"; // NOI18N
    protected static final String ALL_TRAINS = "allTrains"; // NOI18N
    protected static final String PAGE_MODE = "pageMode"; // NOI18N, backwards compatible for versions before 3.11
    protected static final String PAGE_FORMAT = "pageFormat"; // NOI18N
    protected static final String PAGE_NORMAL = "pageNormal"; // NOI18N
    protected static final String PAGE_PER_TRAIN = "pagePerTrain"; // NOI18N
    protected static final String PAGE_PER_VISIT = "pagePerVisit"; // NOI18N
    protected static final String PRINT_ROUTE_LOCATION = "printRouteLocation"; // NOI18N
    protected static final String TRACK_SUMMARY = "trackSummary"; // NOI18N

    protected static final String PREFIX = "prefix"; // NOI18N
    protected static final String SETTING = "setting"; // NOI18N

    protected static final String PANEL = "panel"; // NOI18N
    protected static final String TRAIN_ICONXY = "trainIconXY"; // NOI18N
    protected static final String TRAIN_ICON_APPEND = "trainIconAppend"; // NOI18N

    protected static final String FONT_NAME = "fontName"; // NOI18N
    protected static final String FONT_SIZE = "fontSize"; // NOI18N
    protected static final String SIZE = "size"; // NOI18N
    protected static final String PRINT_DUPLEX = "printDuplex"; // NOI18N

    protected static final String PAGE_ORIENTATION = "pageOrientation"; // NOI18N
    protected static final String MANIFEST = "manifest"; // NOI18N
    protected static final String SWITCH_LIST = "switchList"; // NOI18N

    protected static final String MANIFEST_COLORS = "manifestColors"; // NOI18N
    protected static final String DROP_ENGINE_COLOR = "dropEngineColor"; // NOI18N
    protected static final String PICKUP_ENGINE_COLOR = "pickupEngineColor"; // NOI18N
    protected static final String DROP_COLOR = "dropColor"; // NOI18N
    protected static final String PICKUP_COLOR = "pickupColor"; // NOI18N
    protected static final String LOCAL_COLOR = "localColor"; // NOI18N

    protected static final String TAB = "tab"; // NOI18N
    protected static final String TAB2_LENGTH = "tab2Length"; // NOI18N
    protected static final String TAB3_LENGTH = "tab3Length"; // NOI18N
    protected static final String ENABLED = "enabled"; // NOI18N

    protected static final String COLUMN_FORMAT = "columnFormat"; // NOI18N
    protected static final String TWO_COLUMNS = "twoColumns"; // NOI18N
    protected static final String MANIFEST_FORMAT = "manifestFormat";  // NOI18N
    protected static final String VALUE = "value"; // NOI18N
    protected static final String STANDARD = "0"; // NOI18N
    protected static final String TWO_COLUMN = "1"; // NOI18N
    protected static final String TWO_COLUMN_TRACK = "2"; // NOI18N

    protected static final String HEADER_LINES = "headerLines"; // NOI18N
    protected static final String PRINT_HEADER_LINE1 = "printHeaderLine1"; // NOI18N
    protected static final String PRINT_HEADER_LINE2 = "printHeaderLine2"; // NOI18N
    protected static final String PRINT_HEADER_LINE3 = "printHeaderLine3"; // NOI18N

    protected static final String TRUNCATE = "truncate"; // NOI18N
    protected static final String USE_DEPARTURE_TIME = "useDepartureTime"; // NOI18N
    protected static final String USE_EDITOR = "useEditor"; // NOI18N
    protected static final String HAZARDOUS_MSG = "hazardousMsg"; // NOI18N

    protected static final String MANIFEST_LOGO = "manifestLogo"; // NOI18N

    protected static final String BUILD_OPTIONS = "buildOptions"; // NOI18N
    protected static final String AGGRESSIVE = "aggressive"; // NOI18N
    protected static final String NUMBER_PASSES = "numberPasses";  // NOI18N
    protected static final String ON_TIME = "onTime"; // NOI18N
    protected static final String DWELL_TIME = "dwellTime";  // NOI18N

    protected static final String ALLOW_LOCAL_INTERCHANGE = "allowLocalInterchange"; // NOI18N
    protected static final String ALLOW_LOCAL_SPUR = "allowLocalSpur"; // NOI18N
    protected static final String ALLOW_LOCAL_YARD = "allowLocalYard"; // NOI18N
    // next for backward compatibility
    protected static final String ALLOW_LOCAL_SIDING = "allowLocalSiding"; // NOI18N

    protected static final String STAGING_RESTRICTION_ENABLED = "stagingRestrictionEnabled"; // NOI18N
    protected static final String STAGING_TRACK_AVAIL = "stagingTrackAvail"; // NOI18N
    protected static final String ALLOW_RETURN_STAGING = "allowReturnStaging"; // NOI18N
    protected static final String PROMPT_STAGING_ENABLED = "promptStagingEnabled"; // NOI18N
    protected static final String PROMPT_TO_STAGING_ENABLED = "promptToStagingEnabled"; // NOI18N
    protected static final String STAGING_TRY_NORMAL = "stagingTryNormal"; // NOI18N

    protected static final String GENERATE_CSV_MANIFEST = "generateCsvManifest"; // NOI18N
    protected static final String GENERATE_CSV_SWITCH_LIST = "generateCsvSwitchList"; // NOI18N

    protected static final String BUILD_REPORT = "buildReport"; // NOI18N
    protected static final String LEVEL = "level"; // NOI18N
    protected static final String INDENT = "indent"; // NOI18N
    protected static final String ROUTER_LEVEL = "routerLevel"; // NOI18N
    protected static final String ALWAYS_PREVIEW = "alwaysPreview"; // NOI18N

    protected static final String OWNER = "owner"; // NOI18N

    protected static final String ICON_COLOR = "iconColor"; // NOI18N
    protected static final String NORTH = "north"; // NOI18N
    protected static final String SOUTH = "south"; // NOI18N
    protected static final String EAST = "east"; // NOI18N
    protected static final String WEST = "west"; // NOI18N
    protected static final String LOCAL = "local"; // NOI18N
    protected static final String TERMINATE = "terminate"; // NOI18N

    protected static final String COMMENTS = "comments"; // NOI18N
    protected static final String MISPLACED_CARS = "misplacedCars"; // NOI18N
    
    protected static final String DISPLAY = "display"; // NOI18N
    protected static final String SHOW_TRACK_MOVES = "showTrackMoves"; // NOI18N

    protected static final String VSD = "vsd"; // NOI18N
    protected static final String ENABLE_PHYSICAL_LOCATIONS = "enablePhysicalLocations"; // NOI18N

    protected static final String CATS = "CATS"; // NOI18N
    protected static final String EXACT_LOCATION_NAME = "exactLocationName"; // NOI18N
    
    protected static final String DAY_NAME_MAP = "dayNameMapping"; // NOI18N
    protected static final String MAP = "map"; // NOI18N
    protected static final String DAY = "day"; // NOI18N
    protected static final String DAYS = "days"; // NOI18N

}
