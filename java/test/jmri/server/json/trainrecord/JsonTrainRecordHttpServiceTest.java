package jmri.server.json.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.PermissionManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Train records over JSON, and corrections to them.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class JsonTrainRecordHttpServiceTest extends JsonHttpServiceTestBase<JsonTrainRecordHttpService> {

    @TempDir
    File tempDir;
    private TrainRecord record;
    private Block a;
    private Block b;

    private JsonRequest request(String method) {
        return new JsonRequest(locale, JSON.V5, method, 42);
    }

    @Test
    public void testGet() throws JsonException {
        JsonNode result = service.doGet(JsonTrainRecordServiceFactory.TRAIN_RECORD, "Express",
                mapper.createObjectNode(), request(JSON.GET));
        validate(result);
        JsonNode data = result.path(JSON.DATA);
        assertEquals("Express", data.path(JSON.NAME).asText());
        assertTrue(data.path(JsonTrainRecordServiceFactory.PLACED).asBoolean(), "placed");
        assertEquals("IBA", data.path(JsonTrainRecordServiceFactory.BLOCKS).path(0).asText(), "in Block A");
        assertEquals("CONFIRMED", data.path(JsonTrainRecordServiceFactory.CONFIDENCE).asText());
        assertTrue(data.path(JsonTrainRecordServiceFactory.ROSTER_ENTRY).isNull(), "no roster entry");
    }

    @Test
    public void testGetUnknown() {
        JsonException ex = assertThrows(JsonException.class, () -> service.doGet(
                JsonTrainRecordServiceFactory.TRAIN_RECORD, "Nobody", mapper.createObjectNode(), request(JSON.GET)));
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());
    }

    @Test
    public void testGetList() throws JsonException {
        JsonNode result = service.doGetList(JsonTrainRecordServiceFactory.TRAIN_RECORD, mapper.createObjectNode(),
                request(JSON.GET));
        validate(result);
        JsonNode list = result.path(JSON.DATA);
        assertEquals(1, list.size(), "one record");
        assertEquals("Express", list.path(0).path(JSON.DATA).path(JSON.NAME).asText());
    }

    @Test
    public void testTrainIsHere() throws JsonException {
        ObjectNode data = mapper.createObjectNode().put(JsonTrainRecordServiceFactory.BLOCK, "B");
        JsonNode result = service.doPost(JsonTrainRecordServiceFactory.TRAIN_RECORD, "Express", data,
                request(JSON.POST));
        validate(result);
        assertEquals(List.of(b), record.getBlocks(), "moved to Block B");
        assertEquals("IBB", result.path(JSON.DATA).path(JsonTrainRecordServiceFactory.BLOCKS).path(0).asText());
    }

    @Test
    public void testBlockIsEmpty() throws JsonException {
        ObjectNode data = mapper.createObjectNode().put(JsonTrainRecordServiceFactory.EMPTY_BLOCK, "A");
        JsonNode result = service.doPost(JsonTrainRecordServiceFactory.TRAIN_RECORD, "Express", data,
                request(JSON.POST));
        validate(result);
        assertFalse(record.isPlaced(), "taken off the layout");
        assertTrue(result.path(JSON.DATA).path(JsonTrainRecordServiceFactory.CONFIDENCE).isNull(),
                "no confidence off the layout");
    }

    @Test
    public void testCorrectionNeedsABlock() {
        JsonException ex = assertThrows(JsonException.class, () -> service.doPost(
                JsonTrainRecordServiceFactory.TRAIN_RECORD, "Express", mapper.createObjectNode(), request(JSON.POST)));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        assertEquals(List.of(a), record.getBlocks(), "nothing changed");
    }

    @Test
    public void testUnknownBlock() {
        ObjectNode data = mapper.createObjectNode().put(JsonTrainRecordServiceFactory.BLOCK, "Z");
        JsonException ex = assertThrows(JsonException.class, () -> service.doPost(
                JsonTrainRecordServiceFactory.TRAIN_RECORD, "Express", data, request(JSON.POST)));
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());
        assertEquals(List.of(a), record.getBlocks(), "nothing changed");
    }

    @Test
    public void testCorrectionNotPermitted() {
        PermissionManager refusing = mock(PermissionManager.class);
        when(refusing.hasAtLeastRemotePermission(any(), any(), any())).thenReturn(false);
        InstanceManager.setDefault(PermissionManager.class, refusing);
        ObjectNode data = mapper.createObjectNode().put(JsonTrainRecordServiceFactory.BLOCK, "B")
                .put(JsonTrainRecordServiceFactory.TOKEN, "not logged in");
        JsonException ex = assertThrows(JsonException.class, () -> service.doPost(
                JsonTrainRecordServiceFactory.TRAIN_RECORD, "Express", data, request(JSON.POST)));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, ex.getCode());
        assertEquals(List.of(a), record.getBlocks(), "nothing changed");
    }

    @Test
    public void testDoSchema() throws JsonException {
        testDoSchema(JsonTrainRecordServiceFactory.TRAIN_RECORD);
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalSensorManager();
        service = new JsonTrainRecordHttpService(mapper);
        BlockManager blocks = InstanceManager.getDefault(BlockManager.class);
        a = blocks.provideBlock("IBA");
        a.setUserName("A");
        b = blocks.provideBlock("IBB");
        b.setUserName("B");
        record = InstanceManager.getDefault(TrainRecordManager.class).newRecord("Express");
        ThreadingUtil.runOnLayout(() -> record.place(List.of(a), true));
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        record = null;
        a = null;
        b = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }
}
