package jmri.server.json.trainrecord;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;

import org.openide.util.lookup.ServiceProvider;

/**
 * The JSON services for train records.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonTrainRecordServiceFactory
        implements JsonServiceFactory<JsonTrainRecordHttpService, JsonTrainRecordSocketService> {

    /**
     * Type of message for a train record: {@value #TRAIN_RECORD}.
     */
    public static final String TRAIN_RECORD = "trainRecord"; // NOI18N
    /**
     * Type of message for a list of train records: {@value #TRAIN_RECORDS}.
     */
    public static final String TRAIN_RECORDS = "trainRecords"; // NOI18N
    /**
     * The lead loco's roster entry id: {@value #ROSTER_ENTRY}.
     */
    public static final String ROSTER_ENTRY = "rosterEntry"; // NOI18N
    /**
     * Whether the train is on the layout: {@value #PLACED}.
     */
    public static final String PLACED = "placed"; // NOI18N
    /**
     * The Blocks the train is in: {@value #BLOCKS}.
     */
    public static final String BLOCKS = "blocks"; // NOI18N
    /**
     * Whether the first Block holds the train's front: {@value #FRONT_KNOWN}.
     */
    public static final String FRONT_KNOWN = "frontKnown"; // NOI18N
    /**
     * How sure train tracking is of where the train is: {@value #CONFIDENCE}.
     */
    public static final String CONFIDENCE = "confidence"; // NOI18N
    /**
     * Correction: the train is in this Block alone: {@value #BLOCK}.
     */
    public static final String BLOCK = "block"; // NOI18N
    /**
     * Correction: no train is in this Block: {@value #EMPTY_BLOCK}.
     */
    public static final String EMPTY_BLOCK = "emptyBlock"; // NOI18N
    /**
     * The session token from {@code sessionLogin}, for the permission to
     * correct: {@value #TOKEN}.
     */
    public static final String TOKEN = "token"; // NOI18N

    @Override
    public String[] getTypes(String version) {
        return new String[]{TRAIN_RECORD, TRAIN_RECORDS};
    }

    @Override
    public JsonTrainRecordSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonTrainRecordSocketService(connection);
    }

    @Override
    public JsonTrainRecordHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonTrainRecordHttpService(mapper);
    }
}
