package jmri.server.json.operations;

import jmri.jmrit.operations.setup.Setup;

/**
 * JSON Tokens used by JSON Operations.
 *
 * @author Randall Wood (C) 2016
 */
public class JsonOperations {

    /**
     * {@value #CARS}
     */
    public static final String CARS = "cars"; // NOI18N
    /**
     * {@value #TRAINS}
     */
    public static final String TRAINS = "trains"; // NOI18N
    /**
     * {@value #CAR}
     */
    public static final String CAR = "car"; // NOI18N
    /**
     * {@value #ENGINE}
     */
    public static final String ENGINE = "engine"; // NOI18N
    /**
     * {@value #ENGINES}
     */
    public static final String ENGINES = "engines"; // NOI18N
    /**
     *
     */
    public static final String LOCATION = Setup.LOCATION.toLowerCase();
    /**
     *
     */
    public static final String LOCATION_COMMENT = "locationComment"; // NOI18N
    /**
     * {@value #LOCATION_ID}
     */
    public static final String LOCATION_ID = "locationId"; // NOI18N
    /**
     * {@value #LOCATION_NAME}
     */
    public static final String LOCATION_NAME = "locationName"; // NOI18N
    /**
     * {@value #LOCATION_UNKNOWN}
     */
    public static final String LOCATION_UNKNOWN = "locationUnknown"; // NOI18N
    /**
     * {@value #TRAIN_ID}
     */
    public static final String TRAIN_ID = "trainId"; // NOI18N
    /**
     * {@value #TRAIN_NAME}
     */
    public static final String TRAIN_NAME = "trainName"; // NOI18N
    /**
     * {@value #TRAIN_ICON_NAME}
     */
    public static final String TRAIN_ICON_NAME = "trainIconName"; // NOI18N
    /**
     * {@value #TRAIN}
     */
    public static final String TRAIN = "train"; // NOI18N
    /**
     * 
     * <p>
     * Used for rolling stock length in feet or meters
     */
    public static final String LENGTH = Setup.LENGTH.toLowerCase();
    /**
     * 
     * <p>
     * Rolling stock or a train's weight in tons
     */
    public static final String WEIGHT = Setup.WEIGHT.toLowerCase();
    /**
     * {@value #WEIGHT_TONS}
     * <p>
     * Used for model weight of rolling stock in tons
     */
    public static final String WEIGHT_TONS = "weightTons"; // NOI18N
    /**
     *
     */
    public static final String TYPE = Setup.TYPE.toLowerCase();
    /**
     * 
     */
    public static final String COLOR = Setup.COLOR.toLowerCase();
    /**
     * 
     */
    public static final String LOAD = Setup.LOAD.toLowerCase();
    /**
     * 
     */
    public static final String MODEL = Setup.MODEL.toLowerCase();
    /**
     * 
     */
    public static final String HP = Setup.HP.toLowerCase();
    /**
     * 
     * 
     */
    public static final String ROAD = Setup.ROAD.toLowerCase();
    /**
     * 
     */
    public static final String NUMBER = Setup.NUMBER.toLowerCase();
    /**
     * 
     *
     */
    public static final String OWNER = Setup.OWNER.toLowerCase();
    /**
     * 
     *
     * 
     */
    public static final String HAZARDOUS = Setup.HAZARDOUS.toLowerCase();
    /**
     * 
     *
     */
    public static final String COMMENT = Setup.COMMENT.toLowerCase();
    /**
     *
     *
     * 
     */
    public static final String KERNEL = Setup.KERNEL.toLowerCase();
    /**
     * 
     *
     * 
     */
    public static final String FINAL_DESTINATION = Setup.FINAL_DEST.toLowerCase();
    /**
     * 
     *
     * 
     */
    public static final String RETURN_WHEN_EMPTY = Setup.RWE.toLowerCase();
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
     * 
     *
     * @since 5.4.0
     */
    public static final String RETURN_WHEN_LOADED = "returnWhenLoaded";
    /**
     * 
     *
     * @since 5.4.0
     */
    public static final String DIVISION = Setup.DIVISION.toLowerCase();
    /**
     * {@value #UTILITY}
     *
     * @since 1.1
     */
    public static final String UTILITY = "utility";
    /**
     * {@value #BUILT}
     */
    public static final String BUILT = "built"; // NOI18N
    /**
     * {@value #LEAD_ENGINE}
     */
    public static final String LEAD_ENGINE = "leadEngine"; // NOI18N
    /**
     * {@value #CABOOSE}
     */
    public static final String CABOOSE = "caboose"; // NOI18N
    /**
     * {@value #FRED}
     */
    public static final String FRED = "fred"; // NOI18N
    /**
     * {@value #PASSENGER}
     */
    public static final String PASSENGER = "passenger"; // NOI18N
    /**
     * {@value #TERMINATE}
     */
    public static final String TERMINATE = "terminate"; // NOI18N
    /**
     * 
     *
     * @since 1.1
     */
    public static final String TRACK = Setup.TRACK.toLowerCase();
    /**
     * {@value #DATE}
     *
     * @since 1.1
     */
    public static final String DATE = "date";
    /**
     *
     */
    public static final String DESTINATION = Setup.DESTINATION.toLowerCase();
    /**
     * 
     */
    public static final String DESTINATION_TRACK = Setup.DEST_TRACK.toLowerCase();
    /**
     * {@value #LOCATION_TRACK}
     */
    public static final String LOCATION_TRACK = "locationTrack"; // NOI18N
    /**
     * {@value #LEAD}
     */
    public static final String LEAD = "lead"; // NOI18N
    /**
     * {@value #CAR_SUB_TYPE}
     */
    public static final String CAR_SUB_TYPE = "carSubType"; // NOI18N
    /**
     * {@value #CAR_TYPE}
     */
    public static final String CAR_TYPE = "carType"; // NOI18N
    /**
     * {@value #CAR_TYPES}
     */
    public static final String CAR_TYPES = "carTypes"; // NOI18N
    /**
     * {@value #ROLLING_STOCK}
     */
    public static final String ROLLING_STOCK = "rollingStock"; // NOI18N
    /**
     * {@value #OUT_OF_SERVICE}
     */
    public static final String OUT_OF_SERVICE = "outOfService"; // NOI18N
    /**
     * {@value #NULL}
     */
    public static final String NULL = "null"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonOperations() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}
