package jmri.server.json.trainrecord;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.BLOCK;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.BLOCKS;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.CONFIDENCE;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.EMPTY_BLOCK;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.FRONT_KNOWN;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.PLACED;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.ROSTER_ENTRY;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.TOKEN;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.TRAIN_RECORD;
import static jmri.server.json.trainrecord.JsonTrainRecordServiceFactory.TRAIN_RECORDS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

import jmri.BasicRosterEntry;
import jmri.BooleanPermission;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.PermissionManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.jmrit.trainrecord.PermissionsTrainTracking;
import jmri.jmrit.trainrecord.TrainCorrections;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;
import jmri.util.ThreadingUtil;

/**
 * Train records over JSON: where train tracking places each train, and the
 * operator's corrections. Reads and corrections are made on the layout thread
 * (Swing thread), where train tracking runs.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class JsonTrainRecordHttpService extends JsonHttpService {

    /**
     * @param mapper the mapper for the messages
     */
    public JsonTrainRecordHttpService(@Nonnull ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode parameters, JsonRequest request) throws JsonException {
        TrainRecord record = record(name, request);
        AtomicReference<ObjectNode> data = new AtomicReference<>();
        ThreadingUtil.runOnLayout(() -> data.set(data(record)));
        return message(TRAIN_RECORD, data.get(), request.id);
    }

    /**
     * Correct train tracking: {@code block} places the named train in that
     * Block alone, and {@code emptyBlock} takes every train out of that
     * Block. Both need the Correct where trains are permission.
     */
    @Override
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        TrainRecord record = record(name, request);
        String here = data.path(BLOCK).asText("");
        String empty = data.path(EMPTY_BLOCK).asText("");
        if (here.isEmpty() && empty.isEmpty()) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                    Bundle.getMessage(request.locale, "ErrorCorrectionMissing", BLOCK, EMPTY_BLOCK), request.id);
        }
        if (!InstanceManager.getDefault(PermissionManager.class).hasAtLeastRemotePermission(
                data.path(TOKEN).asText(""), PermissionsTrainTracking.PERMISSION_CORRECT_TRAINS,
                BooleanPermission.BooleanValue.TRUE)) {
            throw new JsonException(HttpServletResponse.SC_FORBIDDEN,
                    Bundle.getMessage(request.locale, "ErrorCorrectNotPermitted"), request.id);
        }
        Block emptied = empty.isEmpty() ? null : block(empty, request);
        Block block = here.isEmpty() ? null : block(here, request);
        ThreadingUtil.runOnLayout(() -> {
            if (emptied != null) {
                TrainCorrections.blockIsEmpty(emptied);
            }
            if (block != null) {
                TrainCorrections.trainIsHere(record, block);
            }
        });
        return doGet(type, name, data, request);
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        ArrayNode array = mapper.createArrayNode();
        ThreadingUtil.runOnLayout(() -> {
            for (TrainRecord r : InstanceManager.getDefault(TrainRecordManager.class).getRecords()) {
                array.add(message(TRAIN_RECORD, data(r), request.id));
            }
        });
        return message(array, request.id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case TRAIN_RECORD:
            case TRAIN_RECORDS:
                return doSchema(type, server,
                        "jmri/server/json/trainrecord/trainRecord-server.json",
                        "jmri/server/json/trainrecord/trainRecord-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    /**
     * The data for a train record. Call on the layout thread (Swing thread).
     *
     * @param record the record
     * @return its data
     */
    @Nonnull
    ObjectNode data(@Nonnull TrainRecord record) {
        ObjectNode data = mapper.createObjectNode();
        data.put(NAME, record.getName());
        BasicRosterEntry entry = record.getFirstRosterEntry();
        if (entry != null) {
            data.put(ROSTER_ENTRY, entry.getId());
        } else {
            data.putNull(ROSTER_ENTRY);
        }
        data.put(PLACED, record.isPlaced());
        ArrayNode blocks = data.putArray(BLOCKS);
        for (Block b : record.getBlocks()) {
            blocks.add(b.getSystemName());
        }
        data.put(FRONT_KNOWN, record.isFrontKnown());
        TrainRecord.Confidence confidence = record.getConfidence();
        if (confidence != null) {
            data.put(CONFIDENCE, confidence.name());
        } else {
            data.putNull(CONFIDENCE); // not placed
        }
        return data;
    }

    @Nonnull
    private static TrainRecord record(@CheckForNull String name, @Nonnull JsonRequest request) throws JsonException {
        TrainRecord record = name == null ? null : InstanceManager.getDefault(TrainRecordManager.class).getByName(name);
        if (record == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, TRAIN_RECORD, name), request.id);
        }
        return record;
    }

    @Nonnull
    private static Block block(@Nonnull String name, @Nonnull JsonRequest request) throws JsonException {
        Block block = InstanceManager.getDefault(BlockManager.class).getBlock(name);
        if (block == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, BLOCK, name), request.id);
        }
        return block;
    }
}
