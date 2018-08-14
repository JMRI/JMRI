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
     * {@value #LOCATIONS}
     */
    public static final String LOCATIONS = "locations"; // NOI18N
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
    public static final String LOCATION = "location"; // NOI18N
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
     * {@value #TRAIN}
     */
    public static final String TRAIN = "train"; // NOI18N
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
     * Prevent instantiation, since this class only contains static values
     */
    private JsonOperations() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}
