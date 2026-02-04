package jmri.server.json.operations;

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
     * {@value #LOCATION}
     */
    public static final String LOCATION = "location";
    /**
     * {@value #LOCATION_COMMENT}
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
     * {@value #LENGTH}
     * <p>
     * Used for rolling stock length in feet or meters
     */
    public static final String LENGTH = "length";
    /**
     * {@value #WEIGHT}
     * <p>
     * Rolling stock or a train's weight in tons
     */
    public static final String WEIGHT = "weight";
    /**
     * {@value #WEIGHT_TONS}
     * <p>
     * Used for model weight of rolling stock in tons
     */
    public static final String WEIGHT_TONS = "weightTons"; // NOI18N
    /**
     * {@value #TYPE}
     */
    public static final String TYPE = "type";
    /**
     * {@value #COLOR}
     */
    public static final String COLOR = "color";
    /**
     * {@value #LOAD}
     */
    public static final String LOAD = "load";
    /**
     * {@value #MODEL}
     */
    public static final String MODEL = "model";
    /**
     * {@value #HP}
     */
    public static final String HP = "hp";
    /**
     * {@value #ROAD}
     */
    public static final String ROAD = "road";
    /**
     * {@value #NUMBER}
     */
    public static final String NUMBER = "number";
    /**
     * {@value #OWNER}
     */
    public static final String OWNER = "owner";
    /**
     * {@value #HAZARDOUS}
     */
    public static final String HAZARDOUS = "hazardous";
    /**
     * {@value #COMMENT}
     */
    public static final String COMMENT = "comment";
    /**
     * {@value #KERNEL}
     */
    public static final String KERNEL = "kernel";
    /**
     * {@value #FINAL_DESTINATION }
     */
    public static final String FINAL_DESTINATION = "final dest";
    /**
     * {@value #FINAL_DEST_TRACK }
     */
    public static final String FINAL_DEST_TRACK = "fd&track";
    /**
     * {@value #RETURN_WHEN_EMPTY}
     */
    public static final String RETURN_WHEN_EMPTY = "rwe";
    /**
     * {@value #SETOUT_COMMENT}
     *
     * @since 1.1
     */
    public static final String SETOUT_COMMENT = "setout msg"; // NOI18N
    /**
     * {@value #PICKUP_COMMENT}
     *
     * @since 1.1
     */
    public static final String PICKUP_COMMENT = "pickup msg"; // NOI18N
    /**
     * {@value #IS_LOCAL}
     *
     * @since 1.1
     */
    public static final String IS_LOCAL = "isLocal";
    /**
     * @since 5.4.0
     */
    public static final String RETURN_WHEN_LOADED = "returnWhenLoaded";
    /**
     * {@value #DIVISION}
     *
     * @since 5.4.0
     */
    public static final String DIVISION = "division";
    /**
     * {@value #LAST_TRAIN}
     */
    public static final String LAST_TRAIN = "last train";
    /**
     * {@value #BLOCKING_ORDER}
     */
    public static final String BLOCKING_ORDER = "block order";
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
    /**
     * {@value #DESTINATION}
     */
    public static final String DESTINATION = "destination";
    /**
     * {@value #DESTINATION_TRACK}
     */
    public static final String DESTINATION_TRACK = "dest&track";
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
