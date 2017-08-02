package jmri.jmris.json;

import jmri.server.json.JsonException;
import jmri.server.json.block.JsonBlock;
import jmri.server.json.consist.JsonConsist;
import jmri.server.json.light.JsonLight;
import jmri.server.json.memory.JsonMemory;
import jmri.server.json.operations.JsonOperations;
import jmri.server.json.power.JsonPowerServiceFactory;
import jmri.server.json.reporter.JsonReporter;
import jmri.server.json.roster.JsonRoster;
import jmri.server.json.sensor.JsonSensor;
import jmri.server.json.signalHead.JsonSignalHead;
import jmri.server.json.signalMast.JsonSignalMast;
import jmri.server.json.throttle.JsonThrottle;
import jmri.server.json.time.JsonTimeServiceFactory;
import jmri.server.json.turnout.JsonTurnoutServiceFactory;

/**
 * Constants used in the JMRI JSON protocol through version 3.0.
 * <p>
 * With the exception of the constants F0-F28 (see {@link #F}), all object names
 * used in the JMRI JSON protocol are constants in this class.
 *
 * @author Randall Wood (C) 2013, 2014, 2016
 * @see jmri.server.json.JSON
 * @deprecated since 4.5.2; use the equivalent constant from
 * {@link jmri.server.json.JSON} or other classes as documented.
 */
@Deprecated
public final class JSON {

    /**
     * JMRI JSON protocol version.
     * <p>
     * Changes to the major number represent a backwards incompatible change in
     * the protocol, while changes to the minor number represent an addition to
     * the protocol.
     * <p>
     * Protocol version 1.0 was first introduced in JMRI 3.4<br>
     * Protocol version 1.1 was first introduced in JMRI 3.7.1 and finalized in
     * JMRI 3.8<br>
     * Protocol version 2.0 was first introduced in JMRI 3.9.3 and finalized in
     * JMRI 3.10<br>
     * Protocol version 3.0 was first introduced in JMRI 3.11.2.
     * <p>
     * {@value #JSON_PROTOCOL_VERSION}
     */
    public static final String JSON_PROTOCOL_VERSION = jmri.server.json.JSON.JSON_PROTOCOL_VERSION;

    /* JSON structure */
    /**
     * {@value #TYPE}
     */
    public static final String TYPE = jmri.server.json.JSON.TYPE;
    /**
     * {@value #LIST}
     */
    public static final String LIST = jmri.server.json.JSON.LIST;
    /**
     * {@value #DATA}
     */
    public static final String DATA = jmri.server.json.JSON.DATA;
    /**
     * {@value #PING}
     */
    public static final String PING = jmri.server.json.JSON.PING;
    /**
     * {@value #PONG}
     */
    public static final String PONG = jmri.server.json.JSON.PONG;
    /**
     * {@value #GOODBYE}
     */
    public static final String GOODBYE = jmri.server.json.JSON.GOODBYE;
    /**
     * {@value #NAME}
     */
    public static final String NAME = jmri.server.json.JSON.NAME;

    /* JSON methods */
    /**
     * {@value #METHOD}
     */
    public static final String METHOD = jmri.server.json.JSON.METHOD;
    /**
     * {@value #DELETE}
     */
    public static final String DELETE = jmri.server.json.JSON.DELETE;
    /**
     * {@value #GET}
     */
    public static final String GET = jmri.server.json.JSON.GET;
    /**
     * {@value #POST}
     */
    public static final String POST = jmri.server.json.JSON.POST;
    /**
     * {@value #PUT}
     */
    public static final String PUT = jmri.server.json.JSON.PUT;

    /* JSON common tokens */
    /**
     * {@value #COMMENT}
     */
    public static final String COMMENT = jmri.server.json.JSON.COMMENT;
    /**
     * {@value #USERNAME}
     */
    public static final String USERNAME = jmri.server.json.JSON.USERNAME;
    /**
     * {@value #STATE}
     */
    public static final String STATE = jmri.server.json.JSON.STATE;
    /**
     * {@value #VALUE}
     */
    public static final String VALUE = jmri.server.json.JSON.VALUE;
    /**
     * {@value #ID}
     */
    public static final String ID = jmri.server.json.JSON.ID;
    /**
     * {@value #STATUS}
     */
    public static final String STATUS = jmri.server.json.JSON.STATUS;
    /**
     * Numeric status value
     *
     * {@value #STATUS_CODE}
     */
    public static final String STATUS_CODE = jmri.server.json.JSON.STATUS_CODE;

    /* JSON error */
    /**
     * {@value #ERROR}
     *
     * @see jmri.server.json.JsonException#ERROR
     */
    public static final String ERROR = JsonException.ERROR;
    /**
     * {@value #CODE}
     *
     * @see jmri.server.json.JsonException#CODE
     */
    public static final String CODE = JsonException.CODE;
    /**
     * {@value #MESSAGE}
     *
     * @see jmri.server.json.JsonException#MESSAGE
     */
    public static final String MESSAGE = JsonException.MESSAGE;

    /* JSON hello and metadata */
    /**
     * {@value #HELLO}
     */
    public static final String HELLO = jmri.server.json.JSON.HELLO;
    /**
     * {@value #JMRI}
     */
    public static final String JMRI = jmri.server.json.JSON.JMRI;
    /**
     * {@value #HEARTBEAT}
     */
    public static final String HEARTBEAT = jmri.server.json.JSON.HEARTBEAT;
    /**
     * {@value #RAILROAD}
     */
    public static final String RAILROAD = jmri.server.json.JSON.RAILROAD;
    /**
     * {@value #NODE}
     * <p>
     * @since 1.1
     */
    public static final String NODE = jmri.server.json.JSON.NODE;
    /**
     * {@value #ACTIVE_PROFILE}
     * <p>
     * @since 3.0
     */
    public static final String ACTIVE_PROFILE = jmri.server.json.JSON.ACTIVE_PROFILE;
    /**
     * {@value #FORMER_NODES}
     * <p>
     * @since 1.1
     */
    public static final String FORMER_NODES = jmri.server.json.JSON.FORMER_NODES;
    /**
     * {@value #LOCALE}
     * <p>
     * @since 1.1
     */
    public static final String LOCALE = jmri.server.json.JSON.LOCALE;

    /* JSON list types */
    /**
     * {@value #BLOCKS}
     *
     * @see jmri.server.json.block.JsonBlock#BLOCKS
     */
    public static final String BLOCKS = JsonBlock.BLOCKS;
    /**
     * {@value #CARS}
     */
    public static final String CARS = JsonOperations.CARS;
    /**
     * {@value #CONSISTS}
     */
    public static final String CONSISTS = JsonConsist.CONSISTS;
    /**
     * {@value #ENGINES}
     */
    public static final String ENGINES = jmri.server.json.JSON.ENGINES;
    /**
     * {@value #LIGHTS}
     */
    public static final String LIGHTS = JsonLight.LIGHTS;
    /**
     * {@value #LOCATIONS}
     */
    public static final String LOCATIONS = JsonOperations.LOCATIONS;
    /**
     * {@value #MEMORIES}
     */
    public static final String MEMORIES = JsonMemory.MEMORIES;
    /**
     * {@value #METADATA}
     */
    public static final String METADATA = jmri.server.json.JSON.METADATA;
    /**
     * {@value #PANELS}
     */
    public static final String PANELS = jmri.server.json.JSON.PANELS;
    /**
     * {@value #REPORTERS}
     *
     * @see jmri.server.json.reporter.JsonReporter#REPORTERS
     */
    public static final String REPORTERS = JsonReporter.REPORTERS;
    /**
     * {@value #ROSTER}
     *
     * @see jmri.server.json.roster.JsonRoster#ROSTER
     *
     */
    public static final String ROSTER = JsonRoster.ROSTER;
    /**
     * {@value #ROSTER_GROUP}
     *
     * @since 2.0
     * @see jmri.server.json.roster.JsonRoster#ROSTER_GROUP
     */
    public static final String ROSTER_GROUP = JsonRoster.ROSTER_GROUP;
    /**
     * {@value #ROSTER_GROUPS}
     *
     * @see jmri.server.json.roster.JsonRoster#ROSTER_GROUPS
     */
    public static final String ROSTER_GROUPS = JsonRoster.ROSTER_GROUPS;
    /**
     * {@value #ROUTES}
     */
    public static final String ROUTES = jmri.server.json.JSON.ROUTES;
    /**
     * {@value #SENSORS}
     *
     * @see jmri.server.json.sensor.JsonSensor#SENSORS
     */
    public static final String SENSORS = JsonSensor.SENSORS;
    /**
     * {@value #SIGNAL_HEADS}
     *
     * @see jmri.server.json.signalHead.JsonSignalHead#SIGNAL_HEADS
     */
    public static final String SIGNAL_HEADS = JsonSignalHead.SIGNAL_HEADS;
    /**
     * {@value #SIGNAL_MASTS}
     *
     * @see jmri.server.json.signalMast.JsonSignalMast#SIGNAL_MASTS
     */
    public static final String SIGNAL_MASTS = JsonSignalMast.SIGNAL_MASTS;
    /**
     * {@value #TRAINS}
     */
    public static final String TRAINS = JsonOperations.TRAINS;
    /**
     * {@value #TURNOUTS}
     */
    public static final String TURNOUTS = JsonTurnoutServiceFactory.TURNOUTS;
    /**
     * {@value #NETWORK_SERVICES}
     */
    public static final String NETWORK_SERVICES = jmri.server.json.JSON.NETWORK_SERVICES;

    /* JSON data types */
    /**
     * {@value #BLOCK}
     *
     * @see jmri.server.json.block.JsonBlock#BLOCK
     */
    public static final String BLOCK = JsonBlock.BLOCK;
    /**
     * {@value #CAR}
     */
    public static final String CAR = JsonOperations.CAR;
    /**
     * {@value #CONSIST}
     */
    public static final String CONSIST = JsonConsist.CONSIST;
    /**
     * {@value #ENGINE}
     */
    public static final String ENGINE = JsonOperations.ENGINE;
    /**
     * {@value #LIGHT}
     */
    public static final String LIGHT = JsonLight.LIGHT;
    /**
     * {@value #LOCATION}
     */
    public static final String LOCATION = JsonOperations.LOCATION;
    /**
     * {@value #LOCATION_ID}
     */
    public static final String LOCATION_ID = JsonOperations.LOCATION_ID;
    /**
     * {@value #MEMORY}
     */
    public static final String MEMORY = JsonMemory.MEMORY;
    /**
     * {@value #NETWORK_SERVICE}
     *
     * @since 2.0
     */
    public static final String NETWORK_SERVICE = jmri.server.json.JSON.NETWORK_SERVICE;
    /**
     * {@value #PANEL}
     */
    public static final String PANEL = jmri.server.json.JSON.PANEL;
    /**
     * {@value #POWER}
     *
     * @see jmri.server.json.power.JsonPowerServiceFactory#POWER
     */
    public static final String POWER = JsonPowerServiceFactory.POWER;
    /**
     * {@value #PROGRAMMER}
     */
    public static final String PROGRAMMER = jmri.server.json.JSON.PROGRAMMER;
    /**
     * {@value #ROUTE}
     */
    public static final String ROUTE = jmri.server.json.JSON.ROUTE;
    /**
     * {@value #SENSOR}
     *
     * @see jmri.server.json.sensor.JsonSensor#SENSOR
     */
    public static final String SENSOR = JsonSensor.SENSOR;
    /**
     * {@value #SIGNAL_HEAD}
     *
     * @see jmri.server.json.signalHead.JsonSignalHead#SIGNAL_HEAD
     */
    public static final String SIGNAL_HEAD = JsonSignalHead.SIGNAL_HEAD;
    /**
     * {@value #SIGNAL_MAST}
     *
     * @see jmri.server.json.signalMast.JsonSignalMast#SIGNAL_MAST
     */
    public static final String SIGNAL_MAST = JsonSignalMast.SIGNAL_MAST;
    /**
     * {@value #REPORTER}
     *
     * @see jmri.server.json.reporter.JsonReporter#REPORTER
     */
    public static final String REPORTER = JsonReporter.REPORTER;
    /**
     * {@value #ROSTER_ENTRY}
     *
     * @see jmri.server.json.roster.JsonRoster#ROSTER_ENTRY
     */
    public static final String ROSTER_ENTRY = JsonRoster.ROSTER_ENTRY;
    /**
     * {@value #THROTTLE}
     *
     * @see jmri.server.json.throttle.JsonThrottle#THROTTLE
     */
    public static final String THROTTLE = JsonThrottle.THROTTLE;
    /**
     * {@value #TIME}
     *
     * @see jmri.server.json.time.JsonTimeServiceFactory#TIME
     */
    public static final String TIME = JsonTimeServiceFactory.TIME;
    /**
     * {@value #TRAIN}
     */
    public static final String TRAIN = JsonOperations.TRAIN;
    /**
     * {@value #TURNOUT}
     */
    public static final String TURNOUT = jmri.server.json.turnout.JsonTurnoutServiceFactory.TURNOUT;

    /* JSON operations tokens */
    /**
     * {@value #ICON_NAME}
     */
    public static final String ICON_NAME = jmri.server.json.JSON.ICON_NAME;
    /**
     * {@value #LENGTH}
     */
    public static final String LENGTH = jmri.server.json.JSON.LENGTH;
    /**
     * {@value #WEIGHT}
     */
    public static final String WEIGHT = JsonOperations.WEIGHT;
    /**
     * {@value #LEAD_ENGINE}
     */
    public static final String LEAD_ENGINE = JsonOperations.LEAD_ENGINE;
    /**
     * {@value #CABOOSE}
     */
    public static final String CABOOSE = JsonOperations.CABOOSE;
    /**
     * {@value #TERMINATE}
     */
    public static final String TERMINATE = JsonOperations.TERMINATE;
    /**
     * {@value #TRACK}
     *
     * @since 1.1
     */
    public static final String TRACK = JsonOperations.TRACK;
    /**
     * {@value #DATE}
     *
     * @since 1.1
     */
    public static final String DATE = JsonOperations.DATE;

    /* JSON panel tokens */
    /**
     * {@value #CONTROL_PANEL}
     */
    public static final String CONTROL_PANEL = jmri.server.json.JSON.CONTROL_PANEL;
    /**
     * {@value #LAYOUT_PANEL}
     */
    public static final String LAYOUT_PANEL = jmri.server.json.JSON.LAYOUT_PANEL;
    /**
     * {@value #PANEL_PANEL}
     */
    public static final String PANEL_PANEL = jmri.server.json.JSON.PANEL;
    /**
     * {@value #URL}
     */
    public static final String URL = jmri.server.json.JSON.URL;
    /**
     * {@value #FORMAT}
     */
    public static final String FORMAT = jmri.server.json.JSON.FORMAT;
    /**
     * {@value #JSON}
     */
    public static final String JSON = jmri.server.json.JSON.JSON;
    /**
     * {@value #XML}
     */
    public static final String XML = jmri.server.json.JSON.XML;

    /* JSON programmer tokens */
    /**
     * {@value #MODE}
     */
    public static final String MODE = jmri.server.json.JSON.MODE;
    /**
     * {@value #NODE_CV}
     */
    public static final String NODE_CV = jmri.server.json.JSON.NODE_CV;
    /**
     * {@value #OP}
     */
    public static final String OP = jmri.server.json.JSON.OP;
    /**
     * {@value #READ}
     */
    public static final String READ = jmri.server.json.JSON.READ;
    /**
     * {@value #WRITE}
     */
    public static final String WRITE = jmri.server.json.JSON.WRITE;

    /* JSON reporter tokens */
    /**
     * {@value #REPORT}
     *
     * @see jmri.server.json.reporter.JsonReporter#REPORT
     */
    public static final String REPORT = JsonReporter.REPORT;
    /**
     * {@value #LAST_REPORT}
     *
     * @see jmri.server.json.reporter.JsonReporter#LAST_REPORT
     */
    public static final String LAST_REPORT = JsonReporter.LAST_REPORT;

    /* JSON roster and car/engine (operations) tokens */
    /**
     * {@value #COLOR}
     */
    public static final String COLOR = jmri.server.json.JSON.COLOR;
    /**
     * {@value #LOAD}
     */
    public static final String LOAD = jmri.server.json.JSON.LOAD;
    /**
     * {@value #MODEL}
     */
    public static final String MODEL = jmri.server.json.JSON.MODEL;
    /**
     * {@value #ROAD}
     */
    public static final String ROAD = jmri.server.json.JSON.ROAD;
    /**
     * {@value #NUMBER}
     */
    public static final String NUMBER = jmri.server.json.JSON.NUMBER;
    /**
     * {@value #DESTINATION}
     */
    public static final String DESTINATION = JsonOperations.DESTINATION;
    /**
     * {@value #DESTINATION_TRACK}
     */
    public static final String DESTINATION_TRACK = JsonOperations.DESTINATION_TRACK;
    /**
     * {@value #LOCATION_TRACK}
     */
    public static final String LOCATION_TRACK = JsonOperations.LOCATION_TRACK;
    /**
     * {@value #IS_LONG_ADDRESS}
     */
    public static final String IS_LONG_ADDRESS = jmri.server.json.JSON.IS_LONG_ADDRESS;
    /**
     * {@value #MFG}
     */
    public static final String MFG = jmri.server.json.JSON.MFG;
    /**
     * {@value #DECODER_MODEL}
     */
    public static final String DECODER_MODEL = jmri.server.json.JSON.DECODER_MODEL;
    /**
     * {@value #DECODER_FAMILY}
     */
    public static final String DECODER_FAMILY = jmri.server.json.JSON.DECODER_FAMILY;
    /**
     * {@value #MAX_SPD_PCT}
     */
    public static final String MAX_SPD_PCT = jmri.server.json.JSON.MAX_SPD_PCT;
    /**
     * {@value #FUNCTION_KEYS}
     */
    public static final String FUNCTION_KEYS = jmri.server.json.JSON.FUNCTION_KEYS;
    /**
     * {@value #IMAGE}
     *
     * @since 2.0
     */
    public static final String IMAGE = jmri.server.json.JSON.IMAGE;
    /**
     * {@value #ICON}
     *
     * @since 2.0
     */
    public static final String ICON = jmri.server.json.JSON.ICON;
    /**
     * {@value #SELECTED_ICON}
     *
     * @since 2.0
     */
    public static final String SELECTED_ICON = jmri.server.json.JSON.SELECTED_ICON;
    /**
     * {@value #LABEL}
     */
    public static final String LABEL = jmri.server.json.JSON.LABEL;
    /**
     * {@value #LOCKABLE}
     */
    public static final String LOCKABLE = jmri.server.json.JSON.LOCKABLE;
    /**
     * {@value #GROUP}
     */
    public static final String GROUP = jmri.server.json.JSON.GROUP;
    /**
     * {@value #OWNER}
     *
     * @since 1.1
     */
    public static final String OWNER = jmri.server.json.JSON.OWNER;
    /**
     * {@value #SHUNTING_FUNCTION}
     *
     * @since 2.0
     */
    public static final String SHUNTING_FUNCTION = jmri.server.json.JSON.SHUNTING_FUNCTION;

    /* JSON route (operations) tokens */
    /**
     * {@value #DIRECTION}
     */
    public static final String DIRECTION = jmri.server.json.JSON.DIRECTION;
    /**
     * {@value #SEQUENCE}
     */
    public static final String SEQUENCE = jmri.server.json.JSON.SEQUENCE;
    /**
     * {@value #ARRIVAL_TIME}
     *
     * @since 1.1
     */
    public static final String ARRIVAL_TIME = jmri.server.json.JSON.ARRIVAL_TIME;
    /**
     * {@value #EXPECTED_ARRIVAL}
     */
    public static final String EXPECTED_ARRIVAL = jmri.server.json.JSON.EXPECTED_ARRIVAL;
    /**
     * {@value #EXPECTED_DEPARTURE}
     */
    public static final String EXPECTED_DEPARTURE = jmri.server.json.JSON.EXPECTED_DEPARTURE;
    /**
     * {@value #DEPARTURE_TIME}
     */
    public static final String DEPARTURE_TIME = jmri.server.json.JSON.DEPARTURE_TIME;
    /**
     * {@value #DEPARTURE_LOCATION}
     */
    public static final String DEPARTURE_LOCATION = jmri.server.json.JSON.DEPARTURE_LOCATION;
    /**
     * {@value #TERMINATES_LOCATION}
     */
    public static final String TERMINATES_LOCATION = jmri.server.json.JSON.TERMINATES_LOCATION;
    /**
     * {@value #DESCRIPTION}
     */
    public static final String DESCRIPTION = jmri.server.json.JSON.DESCRIPTION;
    /**
     * {@value #ROUTE_ID}
     */
    public static final String ROUTE_ID = jmri.server.json.JSON.ROUTE_ID;
    /**
     * {@value #HAZARDOUS}
     *
     * @since 1.1
     */
    public static final String HAZARDOUS = jmri.server.json.JSON.HAZARDOUS;
    /**
     * {@value #KERNEL}
     *
     * @since 1.1
     */
    public static final String KERNEL = jmri.server.json.JSON.KERNEL;
    /**
     * {@value #FINAL_DESTINATION}
     *
     * @since 1.1
     */
    public static final String FINAL_DESTINATION = jmri.server.json.JSON.FINAL_DESTINATION;
    /**
     * {@value #REMOVE_COMMENT}
     *
     * @since 1.1
     */
    public static final String REMOVE_COMMENT = jmri.server.json.JSON.REMOVE_COMMENT;
    /**
     * {@value #ADD_COMMENT}
     *
     * @since 1.1
     */
    public static final String ADD_COMMENT = jmri.server.json.JSON.ADD_COMMENT;
    /**
     * {@value #IS_LOCAL}
     *
     * @since 1.1
     */
    public static final String IS_LOCAL = jmri.server.json.JSON.IS_LOCAL;
    /**
     * {@value #ADD_HELPERS}
     *
     * @since 1.1
     */
    public static final String ADD_HELPERS = jmri.server.json.JSON.ADD_HELPERS;
    /**
     * {@value #CHANGE_CABOOSE}
     *
     * @since 1.1
     */
    public static final String CHANGE_CABOOSE = jmri.server.json.JSON.CHANGE_CABOOSE;
    /**
     * {@value #CHANGE_ENGINES}
     *
     * @since 1.1
     */
    public static final String CHANGE_ENGINES = jmri.server.json.JSON.CHANGE_ENGINES;
    /**
     * {@value #REMOVE_HELPERS}
     *
     * @since 1.1
     */
    public static final String REMOVE_HELPERS = jmri.server.json.JSON.REMOVE_HELPERS;
    /**
     * {@value #OPTIONS}
     *
     * @since 1.1
     */
    public static final String OPTIONS = jmri.server.json.JSON.OPTIONS;
    /**
     * {@value #ADD}
     *
     * As an attribute of a {@link #ROSTER}, this is an entry that has been
     * added to the roster.
     *
     * @since 1.1
     */
    public static final String ADD = jmri.server.json.JSON.ADD;
    /**
     * {@value #REMOVE}
     *
     * In operations, this indicates the dropping or setting out of a car or
     * engine.
     *
     * As an attribute of a {@link #ROSTER}, this is an entry that has been
     * removed from the roster.
     *
     * @since 1.1
     */
    public static final String REMOVE = jmri.server.json.JSON.REMOVE;
    /**
     * {@value #ADD_AND_REMOVE}
     *
     * @since 1.1
     */
    public static final String ADD_AND_REMOVE = jmri.server.json.JSON.ADD_AND_REMOVE;
    /**
     * {@value #TOTAL}
     *
     * @since 1.1
     */
    public static final String TOTAL = jmri.server.json.JSON.TOTAL;
    /**
     * {@value #LOADS}
     *
     * @since 1.1
     */
    public static final String LOADS = jmri.server.json.JSON.LOADS;
    /**
     * {@value #EMPTIES}
     *
     * @since 1.1
     */
    public static final String EMPTIES = jmri.server.json.JSON.EMPTIES;
    /**
     * {@value #RETURN_WHEN_EMPTY}
     *
     * @since 1.1
     */
    public static final String RETURN_WHEN_EMPTY = jmri.server.json.JSON.RETURN_WHEN_EMPTY;
    /**
     * {@value #UTILITY}
     *
     * @since 1.1
     */
    public static final String UTILITY = jmri.server.json.JSON.UTILITY;

    /* JSON signalling tokens */
    /**
     * {@value #APPEARANCE}
     */
    public static final String APPEARANCE = jmri.server.json.JSON.APPEARANCE;
    /**
     * {@value #APPEARANCE_NAME}
     */
    public static final String APPEARANCE_NAME = jmri.server.json.JSON.APPEARANCE_NAME;
    /**
     * {@value #ASPECT}
     */
    public static final String ASPECT = jmri.server.json.JSON.ASPECT;
    /**
     * {@value #ASPECT_DARK}
     */
    public static final String ASPECT_DARK = jmri.server.json.JSON.ASPECT_DARK;
    /**
     * {@value #ASPECT_HELD}
     */
    public static final String ASPECT_HELD = jmri.server.json.JSON.ASPECT_HELD;
    /**
     * {@value #ASPECT_UNKNOWN}
     */
    public static final String ASPECT_UNKNOWN = jmri.server.json.JSON.ASPECT_UNKNOWN;
    /**
     * {@value #TOKEN_HELD}
     */
    public static final String TOKEN_HELD = jmri.server.json.JSON.TOKEN_HELD;
    /**
     * {@value #LIT}
     */
    public static final String LIT = jmri.server.json.JSON.LIT;

    /* JSON throttle tokens */
    /**
     * {@value #ADDRESS}
     *
     * @see jmri.server.json.throttle.JsonThrottle#ADDRESS
     */
    public static final String ADDRESS = JsonThrottle.ADDRESS;
    /**
     * {@value #FORWARD}
     *
     * @see jmri.server.json.throttle.JsonThrottle#FORWARD
     */
    public static final String FORWARD = JsonThrottle.FORWARD;
    /**
     * {@value #RELEASE}
     *
     * @see jmri.server.json.throttle.JsonThrottle#RELEASE
     */
    public static final String RELEASE = JsonThrottle.RELEASE;
    /**
     * {@value #ESTOP}
     *
     * @see jmri.server.json.throttle.JsonThrottle#ESTOP
     */
    public static final String ESTOP = JsonThrottle.ESTOP;
    /**
     * {@value #IDLE}
     *
     * @see jmri.server.json.throttle.JsonThrottle#IDLE
     */
    public static final String IDLE = JsonThrottle.IDLE;
    /**
     * {@value #SPEED}
     *
     * @see jmri.server.json.throttle.JsonThrottle#SPEED
     */
    public static final String SPEED = JsonThrottle.SPEED;
    /**
     * {@value #SPEED_STEPS}
     *
     * @see jmri.server.json.throttle.JsonThrottle#SPEED_STEPS
     */
    public static final String SPEED_STEPS = JsonThrottle.SPEED_STEPS;
    /**
     * Prefix for the throttle function keys (F0-F28).
     * <p>
     * {@value #F}
     *
     * @see jmri.server.json.throttle.JsonThrottle#F
     */
    public static final String F = JsonThrottle.F;
    /**
     * Used by a {@link jmri.jmris.json.JsonThrottle} to notify clients of the
     * number of clients controlling the same throttle.
     * <p>
     * {@value #CLIENTS}
     *
     * @since 2.0
     * @see jmri.server.json.throttle.JsonThrottle#CLIENTS
     */
    public static final String CLIENTS = JsonThrottle.CLIENTS;

    /* JSON Sensor and Turnout Tokens */
    /**
     * {@value #INVERTED}
     */
    public static final String INVERTED = jmri.server.json.JSON.INVERTED;

    /* JSON value types */
    /**
     * {@value #NULL}
     */
    public static final String NULL = jmri.server.json.JSON.NULL;
    /**
     * {@value #INTEGER}
     */
    public static final String INTEGER = jmri.server.json.JSON.INTEGER;

    /* JSON network services tokens */
    /**
     * {@value #PORT}
     */
    public static final String PORT = jmri.server.json.JSON.PORT;

    /* JSON consist tokens */
    /**
     * {@value #POSITION}
     */
    public static final String POSITION = jmri.server.json.JSON.POSITION;
    /**
     * {@value #SIZE_LIMIT}
     */
    public static final String SIZE_LIMIT = jmri.server.json.JSON.SIZE_LIMIT;

    /* Time constants */
    /**
     * {@value #RATE}
     */
    public static final String RATE = jmri.server.json.JSON.RATE;

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
    public static final String UNIT = jmri.server.json.JSON.UNIT;

    /* JMRI JSON Client Autoconfiguration support */
    /**
     * {@value #PREFIX}
     *
     * @since 2.0
     */
    public static final String PREFIX = jmri.server.json.JSON.PREFIX;
    /**
     * {@value #SYSTEM_CONNECTION}
     *
     * @since 2.0
     */
    public static final String SYSTEM_CONNECTION = jmri.server.json.JSON.SYSTEM_CONNECTION;
    /**
     * {@value #SYSTEM_CONNECTIONS}
     *
     * @since 2.0
     */
    public static final String SYSTEM_CONNECTIONS = jmri.server.json.JSON.SYSTEM_CONNECTIONS;

    /* ZeroConf support */
    /**
     * {@value #ZEROCONF_SERVICE_TYPE} Not used within the protocol, but used to
     * support discovery of servers supporting the protocol.
     */
    public static final String ZEROCONF_SERVICE_TYPE = jmri.server.json.JSON.ZEROCONF_SERVICE_TYPE;

    /* prevent the constructor from being documented */
    private JSON() {
    }
}
