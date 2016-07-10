package jmri.server.json;

/**
 * Common constants used in the JMRI JSON protocol starting with protocol
 * version 4.0.
 *
 * @author Randall Wood (C) 2013, 2014, 2016
 * @see jmri.jmris.json.JSON
 */
public final class JSON {

    /**
     * JMRI JSON protocol version.
     * <p>
     * Changes to the major number represent a backwards incompatible change in
     * the protocol, while changes to the minor number represent an addition to
     * the protocol.
     * <p>
     * Protocol version 4.0 was first introduced in JMRI 4.3.4.
     * <p>
     * Prior to version 4.0, the JSON servers had a single definition for all
     * tokens used in JSON communications. As of version 4.0, the JSON servers
     * use a modular protocol, fixing as constants only the tokens used for the
     * basic protocol structure as well as some tokens used by multiple modules.
     * <p>
     * {@value #JSON_PROTOCOL_VERSION}
     */
    public static final String JSON_PROTOCOL_VERSION = "4.0"; // NOI18N

    /* JSON structure */
    /**
     * {@value #TYPE}
     */
    public static final String TYPE = "type"; // NOI18N
    /**
     * {@value #LIST}
     */
    public static final String LIST = "list"; // NOI18N
    /**
     * {@value #DATA}
     */
    public static final String DATA = "data"; // NOI18N
    /**
     * {@value #PING}
     */
    public static final String PING = "ping"; // NOI18N
    /**
     * {@value #PONG}
     */
    public static final String PONG = "pong"; // NOI18N
    /**
     * {@value #GOODBYE}
     */
    public static final String GOODBYE = "goodbye"; // NOI18N
    /**
     * {@value #NAME}
     */
    public static final String NAME = "name"; // NOI18N

    /* JSON methods */
    /**
     * {@value #METHOD}
     */
    public static final String METHOD = "method"; // NOI18N
    /**
     * {@value #DELETE}
     */
    public static final String DELETE = "delete"; // NOI18N
    /**
     * {@value #GET}
     */
    public static final String GET = "get"; // NOI18N
    /**
     * {@value #POST}
     */
    public static final String POST = "post"; // NOI18N
    /**
     * {@value #PUT}
     */
    public static final String PUT = "put"; // NOI18N

    /* JSON common tokens */
    /**
     * {@value #COMMENT}
     */
    public static final String COMMENT = "comment"; // NOI18N
    /**
     * {@value #USERNAME}
     */
    public static final String USERNAME = "userName"; // NOI18N
    /**
     * {@value #STATE}
     */
    public static final String STATE = "state"; // NOI18N
    /**
     * {@value #VALUE}
     */
    public static final String VALUE = "value"; // NOI18N
    /**
     * {@value #ID}
     */
    public static final String ID = "id"; // NOI18N
    /**
     * {@value #STATUS}
     */
    public static final String STATUS = "status"; // NOI18N
    /**
     * Numeric status value
     *
     * {@value #STATUS_CODE}
     */
    public static final String STATUS_CODE = "statusCode"; // NOI18N

    /* JSON hello and metadata */
    /**
     * {@value #HELLO}
     */
    public static final String HELLO = "hello"; // NOI18N
    /**
     * {@value #JMRI}
     */
    public static final String JMRI = "JMRI"; // NOI18N
    /**
     * {@value #HEARTBEAT}
     */
    public static final String HEARTBEAT = "heartbeat"; // NOI18N
    /**
     * {@value #RAILROAD}
     */
    public static final String RAILROAD = "railroad"; // NOI18N
    /**
     * {@value #NODE}
     * <p>
     * @since 1.1
     */
    public static final String NODE = "node"; // NOI18N
    /**
     * {@value #ACTIVE_PROFILE}
     * <p>
     * @since 3.0
     */
    public static final String ACTIVE_PROFILE = "activeProfile"; // NOI18N
    /**
     * {@value #FORMER_NODES}
     * <p>
     * @since 1.1
     */
    public static final String FORMER_NODES = "formerNodes"; // NOI18N
    /**
     * {@value #LOCALE}
     * <p>
     * @since 1.1
     */
    public static final String LOCALE = "locale"; // NOI18N

    /* JSON list types */
    /**
     * {@value #BLOCKS}
     */
    public static final String BLOCKS = "blocks"; // NOI18N
    /**
     * {@value #CARS}
     */
    public static final String CARS = "cars"; // NOI18N
    /**
     * {@value #CONSISTS}
     */
    public static final String CONSISTS = "consists"; // NOI18N
    /**
     * {@value #ENGINES}
     */
    public static final String ENGINES = "engines"; // NOI18N
    /**
     * {@value #LIGHTS}
     */
    public static final String LIGHTS = "lights"; // NOI18N
    /**
     * {@value #LOCATIONS}
     */
    public static final String LOCATIONS = "locations"; // NOI18N
    /**
     * {@value #MEMORIES}
     */
    public static final String MEMORIES = "memories"; // NOI18N
    /**
     * {@value #METADATA}
     */
    public static final String METADATA = "metadata"; // NOI18N
    /**
     * {@value #PANELS}
     */
    public static final String PANELS = "panels"; // NOI18N
    /**
     * {@value #REPORTERS}
     */
    public static final String REPORTERS = "reporters"; // NOI18N
    /**
     * {@value #ROUTES}
     */
    public static final String ROUTES = "routes"; // NOI18N
    /**
     * {@value #SENSORS}
     */
    public static final String SENSORS = "sensors"; // NOI18N
    /**
     * {@value #SIGNAL_HEADS}
     */
    public static final String SIGNAL_HEADS = "signalHeads"; // NOI18N
    /**
     * {@value #SIGNAL_MASTS}
     */
    public static final String SIGNAL_MASTS = "signalMasts"; // NOI18N
    /**
     * {@value #TRAINS}
     */
    public static final String TRAINS = "trains"; // NOI18N
    /**
     * {@value #NETWORK_SERVICES}
     */
    public static final String NETWORK_SERVICES = "networkServices"; // NOI18N

    /* JSON data types */
    /**
     * {@value #BLOCK}
     */
    public static final String BLOCK = "block"; // NOI18N
    /**
     * {@value #CAR}
     */
    public static final String CAR = "car"; // NOI18N
    /**
     * {@value #CONSIST}
     */
    public static final String CONSIST = "consist"; // NOI18N
    /**
     * {@value #ENGINE}
     */
    public static final String ENGINE = "engine"; // NOI18N
    /**
     * {@value #LIGHT}
     */
    public static final String LIGHT = "light"; // NOI18N
    /**
     * {@value #LOCATION}
     */
    public static final String LOCATION = "location"; // NOI18N
    /**
     * {@value #LOCATION_ID}
     */
    public static final String LOCATION_ID = "locationId"; // NOI18N
    /**
     * {@value #MEMORY}
     */
    public static final String MEMORY = "memory"; // NOI18N
    /**
     * {@value #NETWORK_SERVICE}
     *
     * @since 2.0
     */
    public static final String NETWORK_SERVICE = "networkService"; // NOI18N
    /**
     * {@value #PANEL}
     */
    public static final String PANEL = "panel"; // NOI18N
    /**
     * {@value #PROGRAMMER}
     */
    public static final String PROGRAMMER = "programmer"; // NOI18N
    /**
     * {@value #ROUTE}
     */
    public static final String ROUTE = "route"; // NOI18N
    /**
     * {@value #SENSOR}
     */
    public static final String SENSOR = "sensor"; // NOI18N
    /**
     * {@value #SIGNAL_HEAD}
     */
    public static final String SIGNAL_HEAD = "signalHead"; // NOI18N
    /**
     * {@value #SIGNAL_MAST}
     */
    public static final String SIGNAL_MAST = "signalMast"; // NOI18N
    /**
     * {@value #REPORTER}
     */
    public static final String REPORTER = "reporter"; // NOI18N
    /**
     * {@value #THROTTLE}
     */
    public static final String THROTTLE = "throttle"; // NOI18N
    /**
     * {@value #TRAIN}
     */
    public static final String TRAIN = "train"; // NOI18N
    /**
     * {@value #USE_EXTRA_COLOR}
     */
    public static final String USE_EXTRA_COLOR = "useExtraColor"; // NOI18N
    /**
     * {@value #BLOCK_COLOR}
     */
    public static final String BLOCK_COLOR = "blockColor"; // NOI18N
    /**
     * {@value #TRACK_COLOR}
     */
    public static final String TRACK_COLOR = "trackColor"; // NOI18N
    /**
     * {@value #BLOCK_OCCUPIED_COLOR}
     */
    public static final String BLOCK_OCCUPIED_COLOR = "blockOccupiedColor"; // NOI18N
    /**
     * {@value #BLOCK_EXTRA_COLOR}
     */
    public static final String BLOCK_EXTRA_COLOR = "blockExtraColor"; // NOI18N

    /* JSON operations tokens */
    /**
     * {@value #ICON_NAME}
     */
    public static final String ICON_NAME = "iconName"; // NOI18N
    /**
     * {@value #LENGTH}
     */
    public static final String LENGTH = "length"; // NOI18N
    /**
     * {@value #WEIGHT}
     */
    public static final String WEIGHT = "weight"; // NOI18N
    /**
     * {@value #LEAD_ENGINE}
     */
    public static final String LEAD_ENGINE = "leadEngine"; // NOI18N
    /**
     * {@value #CABOOSE}
     */
    public static final String CABOOSE = "caboose"; // NOI18N
    /**
     * {@value #TERMINATE}
     */
    public static final String TERMINATE = "terminate"; // NOI18N
    /**
     * {@value #TRACK}
     *
     * @since 1.1
     */
    public static final String TRACK = "track";
    /**
     * {@value #DATE}
     *
     * @since 1.1
     */
    public static final String DATE = "date";

    /* JSON panel tokens */
    /**
     * {@value #CONTROL_PANEL}
     */
    public static final String CONTROL_PANEL = "Control Panel"; // NOI18N
    /**
     * {@value #LAYOUT_PANEL}
     */
    public static final String LAYOUT_PANEL = "Layout"; // NOI18N
    /**
     * {@value #PANEL_PANEL}
     */
    public static final String PANEL_PANEL = "Panel"; // NOI18N
    /**
     * {@value #URL}
     */
    public static final String URL = "URL"; // NOI18N
    /**
     * {@value #FORMAT}
     */
    public static final String FORMAT = "format"; // NOI18N
    /**
     * {@value #JSON}
     */
    public static final String JSON = "json"; // NOI18N
    /**
     * {@value #XML}
     */
    public static final String XML = "xml"; // NOI18N

    /* JSON programmer tokens */
    /**
     * {@value #MODE}
     */
    public static final String MODE = "mode"; // NOI18N
    /**
     * {@value #NODE_CV}
     */
    public static final String NODE_CV = "CV"; // NOI18N
    /**
     * {@value #OP}
     */
    public static final String OP = "mode"; // NOI18N
    /**
     * {@value #READ}
     */
    public static final String READ = "read"; // NOI18N
    /**
     * {@value #WRITE}
     */
    public static final String WRITE = "write"; // NOI18N

    /* JSON reporter tokens */
    /**
     * {@value #REPORT}
     */
    public static final String REPORT = "report"; // NOI18N
    /**
     * {@value #LAST_REPORT}
     */
    public static final String LAST_REPORT = "lastReport"; // NOI18N

    /* JSON roster and car/engine (operations) tokens */
    /**
     * {@value #COLOR}
     */
    public static final String COLOR = "color"; // NOI18N
    /**
     * {@value #LOAD}
     */
    public static final String LOAD = "load"; // NOI18N
    /**
     * {@value #MODEL}
     */
    public static final String MODEL = "model"; // NOI18N
    /**
     * {@value #ROAD}
     */
    public static final String ROAD = "road"; // NOI18N
    /**
     * {@value #NUMBER}
     */
    public static final String NUMBER = "number"; // NOI18N
    /**
     * {@value #DESTINATION}
     */
    public static final String DESTINATION = "destination"; // NOI18N
    /**
     * {@value #DESTINATION_TRACK}
     */
    public static final String DESTINATION_TRACK = "dest&track"; // NOI18N
    /**
     * {@value #LOCATION_TRACK}
     */
    public static final String LOCATION_TRACK = "locationTrack"; // NOI18N
    /**
     * {@value #IS_LONG_ADDRESS}
     */
    public static final String IS_LONG_ADDRESS = "isLongAddress"; // NOI18N
    /**
     * {@value #MFG}
     */
    public static final String MFG = "mfg"; // NOI18N
    /**
     * {@value #DECODER_MODEL}
     */
    public static final String DECODER_MODEL = "decoderModel"; // NOI18N
    /**
     * {@value #DECODER_FAMILY}
     */
    public static final String DECODER_FAMILY = "decoderFamily"; // NOI18N
    /**
     * {@value #MAX_SPD_PCT}
     */
    public static final String MAX_SPD_PCT = "maxSpeedPct"; // NOI18N
    /**
     * {@value #FUNCTION_KEYS}
     */
    public static final String FUNCTION_KEYS = "functionKeys"; // NOI18N
    /**
     * {@value #IMAGE}
     *
     * @since 2.0
     */
    public static final String IMAGE = "image"; // NOI18N
    /**
     * {@value #ICON}
     *
     * @since 2.0
     */
    public static final String ICON = "icon"; // NOI18N
    /**
     * {@value #SELECTED_ICON}
     *
     * @since 2.0
     */
    public static final String SELECTED_ICON = "selectedIcon"; // NOI18N
    /**
     * {@value #LABEL}
     */
    public static final String LABEL = "label"; // NOI18N
    /**
     * {@value #LOCKABLE}
     */
    public static final String LOCKABLE = "lockable"; // NOI18N
    /**
     * {@value #GROUP}
     */
    public static final String GROUP = "group"; // NOI18N
    /**
     * {@value #OWNER}
     *
     * @since 1.1
     */
    public static final String OWNER = "owner"; // NOI18N
    /**
     * {@value #SHUNTING_FUNCTION}
     *
     * @since 2.0
     */
    public static final String SHUNTING_FUNCTION = "shuntingFunction"; // NOI18N

    /* JSON route (operations) tokens */
    /**
     * {@value #DIRECTION}
     */
    public static final String DIRECTION = "trainDirection"; // NOI18N
    /**
     * {@value #SEQUENCE}
     */
    public static final String SEQUENCE = "sequenceId"; // NOI18N
    /**
     * {@value #ARRIVAL_TIME}
     *
     * @since 1.1
     */
    public static final String ARRIVAL_TIME = "arrivalTime"; // NOI18N
    /**
     * {@value #EXPECTED_ARRIVAL}
     */
    public static final String EXPECTED_ARRIVAL = "expectedArrivalTime"; // NOI18N
    /**
     * {@value #EXPECTED_DEPARTURE}
     */
    public static final String EXPECTED_DEPARTURE = "expectedDepartureTime"; // NOI18N
    /**
     * {@value #DEPARTURE_TIME}
     */
    public static final String DEPARTURE_TIME = "departureTime"; // NOI18N
    /**
     * {@value #DEPARTURE_LOCATION}
     */
    public static final String DEPARTURE_LOCATION = "trainDepartsName"; // NOI18N
    /**
     * {@value #TERMINATES_LOCATION}
     */
    public static final String TERMINATES_LOCATION = "trainTerminatesName"; // NOI18N
    /**
     * {@value #DESCRIPTION}
     */
    public static final String DESCRIPTION = "description"; // NOI18N
    /**
     * {@value #ROUTE_ID}
     */
    public static final String ROUTE_ID = "routeId"; // NOI18N
    /**
     * {@value #HAZARDOUS}
     *
     * @since 1.1
     */
    public static final String HAZARDOUS = "hazardous"; // NOI18N
    /**
     * {@value #KERNEL}
     *
     * @since 1.1
     */
    public static final String KERNEL = "kernel"; // NOI18N
    /**
     * {@value #FINAL_DESTINATION}
     *
     * @since 1.1
     */
    public static final String FINAL_DESTINATION = "finalDestination"; // NOI18N
    /**
     * {@value #REMOVE_COMMENT}
     *
     * @since 1.1
     */
    public static final String REMOVE_COMMENT = "removeComment"; // NOI18N
    /**
     * {@value #ADD_COMMENT}
     *
     * @since 1.1
     */
    public static final String ADD_COMMENT = "addComment"; // NOI18N
    /**
     * {@value #IS_LOCAL}
     *
     * @since 1.1
     */
    public static final String IS_LOCAL = "isLocal";
    /**
     * {@value #ADD_HELPERS}
     *
     * @since 1.1
     */
    public static final String ADD_HELPERS = "addHelpers";
    /**
     * {@value #CHANGE_CABOOSE}
     *
     * @since 1.1
     */
    public static final String CHANGE_CABOOSE = "changeCaboose";
    /**
     * {@value #CHANGE_ENGINES}
     *
     * @since 1.1
     */
    public static final String CHANGE_ENGINES = "changeEngines";
    /**
     * {@value #REMOVE_HELPERS}
     *
     * @since 1.1
     */
    public static final String REMOVE_HELPERS = "removeHelpers";
    /**
     * {@value #OPTIONS}
     *
     * @since 1.1
     */
    public static final String OPTIONS = "options";
    /**
     * {@value #ADD}
     *
     * As an attribute of a {@link jmri.server.json.roster.JsonRoster#ROSTER}, this is an entry that has been
     * added to the roster.
     *
     * @since 1.1
     */
    public static final String ADD = "add";
    /**
     * {@value #REMOVE}
     *
     * In operations, this indicates the dropping or setting out of a car or
     * engine.
     *
     * As an attribute of a {@link jmri.server.json.roster.JsonRoster#ROSTER}, this is an entry that has been
     * removed from the roster.
     *
     * @since 1.1
     */
    public static final String REMOVE = "remove";
    /**
     * {@value #ADD_AND_REMOVE}
     *
     * @since 1.1
     */
    public static final String ADD_AND_REMOVE = "addAndRemove";
    /**
     * {@value #TOTAL}
     *
     * @since 1.1
     */
    public static final String TOTAL = "total";
    /**
     * {@value #LOADS}
     *
     * @since 1.1
     */
    public static final String LOADS = "loads";
    /**
     * {@value #EMPTIES}
     *
     * @since 1.1
     */
    public static final String EMPTIES = "empties";
    /**
     * {@value #RETURN_WHEN_EMPTY}
     *
     * @since 1.1
     */
    public static final String RETURN_WHEN_EMPTY = "returnWhenEmpty";
    /**
     * {@value #UTILITY}
     *
     * @since 1.1
     */
    public static final String UTILITY = "utility";

    /* JSON signalling tokens */
    /**
     * {@value #APPEARANCE}
     */
    public static final String APPEARANCE = "appearance"; // NOI18N
    /**
     * {@value #APPEARANCE_NAME}
     */
    public static final String APPEARANCE_NAME = "appearanceName"; // NOI18N
    /**
     * {@value #ASPECT}
     */
    public static final String ASPECT = "aspect"; // NOI18N
    /**
     * {@value #ASPECT_DARK}
     */
    public static final String ASPECT_DARK = "Dark"; // NOI18N
    /**
     * {@value #ASPECT_HELD}
     */
    public static final String ASPECT_HELD = "Held"; // NOI18N
    /**
     * {@value #ASPECT_UNKNOWN}
     */
    public static final String ASPECT_UNKNOWN = "Unknown"; // NOI18N
    /**
     * {@value #TOKEN_HELD}
     */
    public static final String TOKEN_HELD = "held"; // NOI18N
    /**
     * {@value #LIT}
     */
    public static final String LIT = "lit"; // NOI18N

    /* JSON throttle tokens */
    /**
     * {@value #ADDRESS}
     */
    public static final String ADDRESS = "address"; // NOI18N
    /**
     * {@value #FORWARD}
     */
    public static final String FORWARD = "forward"; // NOI18N
    /**
     * {@value #RELEASE}
     */
    public static final String RELEASE = "release"; // NOI18N
    /**
     * {@value #ESTOP}
     */
    public static final String ESTOP = "eStop"; // NOI18N
    /**
     * {@value #IDLE}
     */
    public static final String IDLE = "idle"; // NOI18N
    /**
     * {@value #SPEED}
     */
    public static final String SPEED = "speed"; // NOI18N
    /**
     * {@value #SPEED_STEPS}
     */
    public static final String SPEED_STEPS = "speedSteps"; // NOI18N
    /**
     * Prefix for the throttle function keys (F0-F28).
     * <p>
     * {@value #F}
     */
    public static final String F = "F"; // NOI18N
    /**
     * Used by a {@link jmri.jmris.json.JsonThrottle} to notify clients of the
     * number of clients controlling the same throttle.
     * <p>
     * {@value #CLIENTS}
     *
     * @since 2.0
     */
    public static final String CLIENTS = "clients"; // NOI18N

    /* JSON Sensor and Turnout Tokens */
    /**
     * {@value #INVERTED}
     */
    public static final String INVERTED = "inverted"; // NOI18N

    /* JSON value types */
    /**
     * {@value #NULL}
     */
    public static final String NULL = "null"; // NOI18N
    /**
     * {@value #INTEGER}
     */
    public static final String INTEGER = "int"; // NOI18N

    /* JSON network services tokens */
    /**
     * {@value #PORT}
     */
    public static final String PORT = "port"; // NOI18N

    /* JSON consist tokens */
    /**
     * {@value #POSITION}
     */
    public static final String POSITION = "position"; // NOI18N
    /**
     * {@value #SIZE_LIMIT}
     */
    public static final String SIZE_LIMIT = "sizeLimit"; // NOI18N

    /* Time constants */
    /**
     * {@value #RATE}
     */
    public static final String RATE = "rate"; // NOI18N

    /*
     * JSON State (an unsigned integer)
     */

 /* Common state */
    /**
     * {@value #UNKNOWN}
     * <p>
     * Note that this value deliberately differs from
     * {@link jmri.NamedBean#UNKNOWN}.
     */
    public static final int UNKNOWN = 0x00;

    /* Light and PowerManager state */
    /**
     * {@value #ON}
     */
    public static final int ON = 0x02;
    /**
     * {@value #OFF}
     */
    public static final int OFF = 0x04;

    /* NamedBean state */
    /**
     * {@value #INCONSISTENT}
     */
    public static final int INCONSISTENT = 0x08;

    /* Route state */
    /**
     * {@value #TOGGLE}
     */
    public static final int TOGGLE = 0x08;

    /* Sensor state */
    /**
     * {@value #ACTIVE}
     */
    public static final int ACTIVE = 0x02;
    /**
     * {@value #INACTIVE}
     */
    public static final int INACTIVE = 0x04;

    /* SignalHead state */
    /**
     * {@value #STATE_DARK}
     */
    public static final int STATE_DARK = 0x00;
    /**
     * {@value #RED}
     */
    public static final int RED = 0x01;
    /**
     * {@value #FLASHRED}
     */
    public static final int FLASHRED = 0x02;
    /**
     * {@value #YELLOW}
     */
    public static final int YELLOW = 0x04;
    /**
     * {@value #FLASHYELLOW}
     */
    public static final int FLASHYELLOW = 0x08;
    /**
     * {@value #GREEN}
     */
    public static final int GREEN = 0x10;
    /**
     * {@value #FLASHGREEN}
     */
    public static final int FLASHGREEN = 0x20;
    /**
     * {@value #LUNAR}
     */
    public static final int LUNAR = 0x40;
    /**
     * {@value #FLASHLUNAR}
     */
    public static final int FLASHLUNAR = 0x80;
    /**
     * {@value #STATE_HELD}
     */
    public static final int STATE_HELD = 0x100;

    /* Turnout state */
    /**
     * {@value #CLOSED}
     */
    public static final int CLOSED = 0x02;
    /**
     * {@value #THROWN}
     */
    public static final int THROWN = 0x04;
    /**
     * {@value #UNIT}
     *
     * @since 1.1
     */
    public static final String UNIT = "unit"; // NOI18N

    /* JMRI JSON Client Autoconfiguration support */
    /**
     * {@value #PREFIX}
     *
     * @since 2.0
     */
    public static final String PREFIX = "prefix"; // NOI18N
    /**
     * {@value #SYSTEM_CONNECTION}
     *
     * @since 2.0
     */
    public static final String SYSTEM_CONNECTION = "systemConnection"; // NOI18N
    /**
     * {@value #SYSTEM_CONNECTIONS}
     *
     * @since 2.0
     */
    public static final String SYSTEM_CONNECTIONS = "systemConnections"; // NOI18N

    /* ZeroConf support */
    /**
     * {@value #ZEROCONF_SERVICE_TYPE} Not used within the protocol, but used to
     * support discovery of servers supporting the protocol.
     */
    public static final String ZEROCONF_SERVICE_TYPE = "_jmri-json._tcp.local."; // NOI18N

    /* prevent the constructor from being documented */
    private JSON() {
    }
}
