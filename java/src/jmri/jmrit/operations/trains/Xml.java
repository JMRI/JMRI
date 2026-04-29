package jmri.jmrit.operations.trains;

/**
 * A convenient place to access operations xml element and attribute names.
 *
 * @author Daniel Boudreau Copyright (C) 2013
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

    protected static final String TRUE = "true"; // NOI18N
    protected static final String FALSE = "false"; // NOI18N

    // train.java
    protected static final String TRAIN = "train"; // NOI18N
    protected static final String DESCRIPTION = "description"; // NOI18N
    protected static final String DEPART_DAY = "departDay"; // NOI18N
    protected static final String DEPART_HOUR = "departHour"; // NOI18N
    protected static final String DEPART_MINUTE = "departMinute"; // NOI18N
    protected static final String ROW_COLOR = "rowColor";  // NOI18N
    protected static final String RESET_ROW_COLOR = "resetRowColor";  // NOI18N
    protected static final String ROUTE = "route"; // NOI18N
    protected static final String SKIPS = "skips"; // NOI18N
    protected static final String LOCATION = "location"; // NOI18N
    protected static final String ROUTE_ID = "routeId"; // old format // NOI18N
    protected static final String SKIP = "skip"; // NOI18N
    protected static final String CAR_TYPES = "carTypes"; // NOI18N
    protected static final String TYPES = "types"; // NOI18N
    protected static final String CAR_TYPE = "carType"; // NOI18N
    protected static final String LOCO_TYPE = "locoType"; // NOI18N
    protected static final String CAR_ROAD_OPERATION = "carRoadOperation"; // NOI18N should have been option not operation
    protected static final String CAR_ROAD_OPTION = "carRoadOption"; // NOI18N
    protected static final String CABOOSE_ROAD_OPTION = "cabooseRoadOption"; // NOI18N
    protected static final String LOCO_ROAD_OPTION = "locoRoadOption"; // NOI18N
    protected static final String CAR_ROADS = "carRoads"; // NOI18N
    protected static final String CABOOSE_ROADS = "cabooseRoads"; // NOI18N
    protected static final String LOCO_ROADS = "locoRoads"; // NOI18N
    protected static final String CAR_ROAD = "carRoad"; // NOI18N
    protected static final String LOCO_ROAD = "locoRoad"; // NOI18N
    protected static final String CAR_LOAD_OPTION = "carLoadOption"; // NOI18N
    protected static final String CAR_LOADS = "carLoads"; // NOI18N
    protected static final String CAR_LOAD = "carLoad"; // NOI18N
    protected static final String CAR_OWNER_OPTION = "carOwnerOption"; // NOI18N
    protected static final String BUILT_START_YEAR = "builtStartYear"; // NOI18N
    protected static final String BUILT_END_YEAR = "builtEndYear"; // NOI18N
    protected static final String CAR_OWNERS = "carOwners"; // NOI18N
    protected static final String CAR_OWNER = "carOwner"; // NOI18N

    protected static final String NUMBER_ENGINES = "numberEngines"; // NOI18N
    protected static final String LEG2_ENGINES = "leg2Engines"; // NOI18N
    protected static final String LEG3_ENGINES = "leg3Engines"; // NOI18N

    protected static final String ENGINE_ROAD = "engineRoad"; // NOI18N
    protected static final String LEG2_ROAD = "leg2Road"; // NOI18N
    protected static final String LEG3_ROAD = "leg3Road"; // NOI18N

    protected static final String ENGINE_MODEL = "engineModel"; // NOI18N
    protected static final String LEG2_MODEL = "leg2Model"; // NOI18N
    protected static final String LEG3_MODEL = "leg3Model"; // NOI18N

    protected static final String REQUIRES = "requires"; // NOI18N
    protected static final String CABOOSE_ROAD = "cabooseRoad"; // NOI18N
    protected static final String LEG2_CABOOSE_ROAD = "leg2CabooseRoad"; // NOI18N
    protected static final String LEG3_CABOOSE_ROAD = "leg3CabooseRoad"; // NOI18N

    protected static final String LEG2_OPTIONS = "leg2Options"; // NOI18N
    protected static final String LEG3_OPTIONS = "leg3Options"; // NOI18N

    protected static final String BUILD_NORMAL = "buildNormal"; // NOI18N
    protected static final String TO_TERMINAL = "toTerminal"; // NOI18N
    protected static final String ALLOW_LOCAL_MOVES = "allowLocalMoves"; // NOI18N
    protected static final String ALLOW_THROUGH_CARS = "allowThroughCars"; // NOI18N
    protected static final String ALLOW_RETURN = "allowReturn"; // NOI18N
    protected static final String SERVICE_ALL = "serviceAll"; // NOI18N
    protected static final String BUILD_CONSIST = "buildConsist"; // NOI18N
    protected static final String SEND_CUSTOM_STAGING = "sendCustomStaging"; // NOI18N

    protected static final String BUILT = "built"; // NOI18N
    protected static final String BUILD = "build"; // NOI18N
    protected static final String BUILD_FAILED = "buildFailed"; // NOI18N
    protected static final String BUILD_FAILED_MESSAGE = "buildFailedMessage"; // NOI18N
    protected static final String PRINTED = "printed"; // NOI18N
    protected static final String MODIFIED = "modified"; // NOI18N
    protected static final String SWITCH_LIST_STATUS = "switchListStatus"; // NOI18N
    protected static final String LEAD_ENGINE = "leadEngine"; // NOI18N
    protected static final String STATUS = "status"; // NOI18N
    protected static final String STATUS_CODE = "statusCode"; // NOI18N
    protected static final String OLD_STATUS_CODE = "oldStatusCode"; // NOI18N
    protected static final String TERMINATION_DATE = "TerminationDate"; // NOI18N
    protected static final String REQUESTED_CARS = "RequestedCars"; // NOI18N

    protected static final String CURRENT = "current"; // NOI18N
    protected static final String LEG2_START = "leg2Start"; // NOI18N
    protected static final String LEG3_START = "leg3Start"; // NOI18N
    protected static final String LEG2_END = "leg2End"; // NOI18N
    protected static final String LEG3_END = "leg3End"; // NOI18N
    protected static final String DEPARTURE_TRACK = "departureTrack"; // NOI18N
    protected static final String TERMINATION_TRACK = "terminationTrack"; // NOI18N

    protected static final String SCRIPTS = "scripts"; // NOI18N
    protected static final String AFTER_BUILD = "afterBuild"; // NOI18N
    protected static final String TERMINATE = "terminate"; // NOI18N
    protected static final String MOVE = "move"; // NOI18N

    protected static final String RAIL_ROAD = "railRoad"; // NOI18N
    protected static final String MANIFEST_LOGO = "manifestLogo"; // NOI18N
    protected static final String SHOW_TIMES = "showTimes"; // NOI18N

    // TrainManager.java
    protected static final String OPTIONS = "options"; // NOI18N
    protected static final String TRAIN_OPTIONS = "trainOptions"; // NOI18N
    protected static final String BUILD_MESSAGES = "buildMessages"; // NOI18N
    protected static final String BUILD_REPORT = "buildReport"; // NOI18N
    protected static final String PRINT_PREVIEW = "printPreview"; // NOI18N
    protected static final String OPEN_FILE = "openFile"; // NOI18N
    protected static final String RUN_FILE = "runFile"; // NOI18N
    protected static final String TRAIN_ACTION = "trainAction"; // NOI18N

    protected static final String START_UP = "startUp"; // NOI18N
    protected static final String SHUT_DOWN = "shutDown"; // NOI18N
    
    protected static final String CONDUCTOR_OPTIONS = "conductorOptions"; // NOI18N
    protected static final String SHOW_HYPHEN_NAME = "showHyphenName"; // NOI18N

    protected static final String ROW_COLOR_OPTIONS = "rowColorOptions"; // NOI18N
    protected static final String ROW_COLOR_MANUAL = "rowColorManual"; // NOI18N
    protected static final String ROW_COLOR_BUILT = "rowColorBuilt"; // NOI18N
    protected static final String ROW_COLOR_BUILD_FAILED = "rowColorBuidFailed"; // NOI18N
    protected static final String ROW_COLOR_TRAIN_EN_ROUTE = "rowColorTrainEnRoute"; // NOI18N
    protected static final String ROW_COLOR_TERMINATED = "rowColorTerminated"; // NOI18N
    protected static final String ROW_COLOR_RESET = "rowColorReset"; // NOI18N

    // TrainManagerXml.java
    protected static final String TRAINS = "trains"; // NOI18N

    // TrainManifestText.java
    protected static final String MANIFEST_TEXT_STRINGS = "manifestTextStrings"; // NOI18N
    protected static final String MANIFEST_FOR_TRAIN = "manifestForTrain"; // NOI18N
    protected static final String VALID = "valid"; // NOI18N
    protected static final String SCHEDULED_WORK = "scheduledWork"; // NOI18N
    protected static final String WORK_DEPARTURE_TIME = "workDepartureTime"; // NOI18N
    protected static final String WORK_ARRIVAL_TIME = "workArrivalTime"; // NOI18N
    protected static final String NO_SCHEDULED_WORK = "noScheduledWork"; // NOI18N
    protected static final String NO_SCHEDULED_WORK_ROUTE_COMMENT = "noScheduledWorkRouteComment"; // NOI18N
    protected static final String DEPART_TIME = "departTime"; // NOI18N
    protected static final String TRAIN_DEPARTS_CARS = "trainDepartsCars"; // NOI18N
    protected static final String TRAIN_DEPARTS_LOADS = "trainDepartsLoads"; // NOI18N
    protected static final String TRAIN_TERMINATES = "trainTerminates"; // NOI18N
    protected static final String DESTINATION = "destination"; // NOI18N
    protected static final String FROM = "from"; // NOI18N
    protected static final String TO = "to"; // NOI18N
    protected static final String DEST = "dest"; // NOI18N
    protected static final String FINAL_DEST = "finalDest"; // NOI18N
    protected static final String ADD_HELPERS = "addHelpers"; // NOI18N
    protected static final String REMOVE_HELPERS = "removeHelpers"; // NOI18N
    protected static final String LOCO_CHANGE = "locoChange"; // NOI18N
    protected static final String CABOOSE_CHANGE = "cabooseChange"; // NOI18N
    protected static final String LOCO_CABOOSE_CHANGE = "locoCabooseChange"; // NOI18N

    protected static final String TEXT = "text"; // NOI18N

    // TrainSwitchListText.java
    protected static final String SWITCH_LIST_TEXT_STRINGS = "switchListTextStrings"; // NOI18N
    protected static final String SWICH_LIST_FOR = "switchListFor"; // NOI18N
    protected static final String SCHEDULED_WORK_TRAIN = "scheduledWorkTrain"; // NOI18N

    protected static final String DEPARTS_AT = "departsAt"; // NOI18N
    protected static final String DEPARTS_EXPECTED_ARRIVAL = "departsExpectedArrival"; // NOI18N
    protected static final String DEPARTED_EXPECTED = "departedExpected"; // NOI18N

    protected static final String VISIT_NUMBER = "visitNumber"; // NOI18N
    protected static final String VISIT_NUMBER_DEPARTED = "visitNumberDeparted"; // NOI18N
    protected static final String VISIT_NUMBER_TERMINATES = "visitNumberTerminates"; // NOI18N
    protected static final String VISIT_NUMBER_TERMINATES_DEPARTED = "visitNumberTerminatesDeparted"; // NOI18N
    protected static final String VISIT_NUMBER_DONE = "visitNumberDone"; // NOI18N

    protected static final String TRAIN_DIRECTION_CHANGE = "trainDirectionChange"; // NOI18N
    protected static final String NO_CAR_PICK_UPS = "noCarPickUps"; // NOI18N
    protected static final String NO_CAR_SET_OUTS = "noCarSetOuts"; // NOI18N
    protected static final String TRAIN_DONE = "trainDone"; // NOI18N
    
    protected static final String SWITCH_LIST_TRACK = "switchListTrack"; // NOI18N
    protected static final String HOLD_CAR = "holdCar"; // NOI18N

    // TrainManifestHeaderText.java
    protected static final String MANIFEST_HEADER_TEXT_STRINGS = "manifestHeaderTextStrings"; // NOI18N
    protected static final String ROAD = "road"; // the supported message format options // NOI18N
    protected static final String NUMBER = "number"; // NOI18N
    protected static final String ENGINE_NUMBER = "engineNumber"; // NOI18N
    protected static final String TYPE = "type"; // NOI18N
    protected static final String MODEL = "model"; // NOI18N
    protected static final String HP = "hp"; // NOI18N
    protected static final String LENGTH = "length"; // NOI18N
    protected static final String WEIGHT = "weight"; // NOI18N
    protected static final String LOAD = "load"; // NOI18N
    protected static final String LOAD_TYPE = "load_type"; // NOI18N
    protected static final String COLOR = "color"; // NOI18N
    protected static final String TRACK = "track"; // NOI18N
    //protected static final String DESTINATION = "destination"; // NOI18N
    protected static final String DEST_TRACK = "dest_Track"; // NOI18N
    //protected static final String FINAL_DEST = "Final_Dest"; // NOI18N
    protected static final String FINAL_DEST_TRACK = "fd_Track"; // NOI18N
    //protected static final String LOCATION = "location"; // NOI18N
    protected static final String CONSIST = "consist"; // NOI18N
    protected static final String DCC_ADDRESS = "dcc_addr"; // NOI18N
    protected static final String KERNEL = "kernel"; // NOI18N
    protected static final String OWNER = "owner"; // NOI18N
    protected static final String DIVISION = "division"; // NOI18N
    protected static final String RWE = "rwe"; // NOI18N
    //protected static final String COMMENT = "Comment"; // NOI18N
    protected static final String DROP_COMMENT = "setOut_msg"; // NOI18N
    protected static final String PICKUP_COMMENT = "pickUp_msg"; // NOI18N
    protected static final String HAZARDOUS = "hazardous"; // NOI18N
    protected static final String LAST_TRAIN = "last_train"; // NOI18N
}
