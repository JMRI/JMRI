package jmri.server.json.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Train records over a JSON socket: a client is sent a record again when it
 * changes.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class JsonTrainRecordSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;
    private TrainRecord record;
    private Block a;
    private Block b;

    private static String firstBlock(JsonNode message) {
        return message.path(JSON.DATA).path(JsonTrainRecordServiceFactory.BLOCKS).path(0).asText();
    }

    @Test
    public void testSentAgainWhenItChanges() throws IOException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTrainRecordSocketService service = new JsonTrainRecordSocketService(connection);
        service.onMessage(JsonTrainRecordServiceFactory.TRAIN_RECORD,
                connection.getObjectMapper().createObjectNode().put(JSON.NAME, "Express"),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertNotNull(message, "answered");
        assertEquals("IBA", firstBlock(message), "in Block A");

        ThreadingUtil.runOnLayout(() -> record.place(List.of(b), true));
        message = connection.getMessage();
        assertNotNull(message, "sent again");
        assertEquals("IBB", firstBlock(message), "now in Block B");

        service.onClose();
        int sent = connection.getMessages().size();
        ThreadingUtil.runOnLayout(() -> record.place(List.of(a), true));
        assertEquals(sent, connection.getMessages().size(), "nothing sent after closing");
    }

    @Test
    public void testCorrectionOverSocket() throws IOException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTrainRecordSocketService service = new JsonTrainRecordSocketService(connection);
        service.onMessage(JsonTrainRecordServiceFactory.TRAIN_RECORD,
                connection.getObjectMapper().createObjectNode().put(JSON.NAME, "Express")
                        .put(JsonTrainRecordServiceFactory.BLOCK, "B"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(List.of(b), record.getBlocks(), "moved to Block B");
        assertEquals("IBB", firstBlock(connection.getMessage()), "answered with the record as it is now");
        service.onClose();
    }

    @Test
    public void testList() throws IOException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonTrainRecordSocketService service = new JsonTrainRecordSocketService(connection);
        service.onList(JsonTrainRecordServiceFactory.TRAIN_RECORD, connection.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        assertEquals(1, connection.getMessage().path(JSON.DATA).size(), "the one record");
        ThreadingUtil.runOnLayout(() -> record.place(List.of(b), true));
        assertEquals("IBB", firstBlock(connection.getMessage()), "a change to a listed record is sent");
        service.onClose();
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalSensorManager();
        BlockManager blocks = InstanceManager.getDefault(BlockManager.class);
        a = blocks.provideBlock("IBA");
        a.setUserName("A");
        b = blocks.provideBlock("IBB");
        b.setUserName("B");
        record = InstanceManager.getDefault(TrainRecordManager.class).newRecord("Express");
        ThreadingUtil.runOnLayout(() -> record.place(List.of(a), true));
    }

    @AfterEach
    public void tearDown() {
        record = null;
        a = null;
        b = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
