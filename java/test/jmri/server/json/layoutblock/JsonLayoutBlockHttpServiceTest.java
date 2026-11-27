package jmri.server.json.layoutblock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.awt.Color;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.server.json.memory.JsonMemory;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link JsonLayoutBlockHttpService}.
 */
public class JsonLayoutBlockHttpServiceTest extends JsonHttpServiceTestBase<JsonLayoutBlockHttpService> {

    @Test
    public void testDoPostColors() throws JsonException {
        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LB1");
        String name = lb.getSystemName();
        lb.setBlockTrackColor(Color.BLACK);
        lb.setBlockOccupiedColor(Color.BLACK);
        lb.setBlockExtraColor(Color.BLACK);
        JsonRequest request = new JsonRequest(locale, JSON.V5, JSON.POST, 42);
        // each color only changes its own setting
        JsonNode data = mapper.createObjectNode().put(JSON.NAME, name).put(JsonLayoutBlock.OCCUPIED_COLOR, "red");
        validateData(JsonLayoutBlock.LAYOUTBLOCK, data, false);
        JsonNode result = service.doPost(JsonLayoutBlock.LAYOUTBLOCK, name, data, request);
        validate(result);
        assertEquals(Color.RED, lb.getBlockOccupiedColor());
        assertEquals(Color.BLACK, lb.getBlockTrackColor());
        assertEquals(Color.BLACK, lb.getBlockExtraColor());
        data = mapper.createObjectNode().put(JSON.NAME, name).put(JsonLayoutBlock.EXTRA_COLOR, "blue");
        service.doPost(JsonLayoutBlock.LAYOUTBLOCK, name, data, request);
        assertEquals(Color.BLUE, lb.getBlockExtraColor());
        assertEquals(Color.BLACK, lb.getBlockTrackColor());
        assertEquals(Color.RED, lb.getBlockOccupiedColor());
        data = mapper.createObjectNode().put(JSON.NAME, name).put(JsonLayoutBlock.TRACK_COLOR, "green");
        service.doPost(JsonLayoutBlock.LAYOUTBLOCK, name, data, request);
        assertEquals(Color.GREEN, lb.getBlockTrackColor());
        assertEquals(Color.RED, lb.getBlockOccupiedColor());
        assertEquals(Color.BLUE, lb.getBlockExtraColor());
    }

    @Test
    public void testDoPostSensorAndMemory() throws JsonException {
        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LB1");
        String name = lb.getSystemName();
        // neither has a user name
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Memory memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        JsonNode data = mapper.createObjectNode().put(JSON.NAME, name).put(JsonLayoutBlock.OCCUPANCY_SENSOR, "IS1").put(JsonMemory.MEMORY, "IM1");
        validateData(JsonLayoutBlock.LAYOUTBLOCK, data, false);
        JsonNode result = service.doPost(JsonLayoutBlock.LAYOUTBLOCK, name, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        validate(result);
        assertEquals(sensor, lb.getOccupancySensor());
        assertEquals(memory, lb.getMemory());
    }

    @Test
    public void testClientSchema() throws JsonException {
        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LB1");
        String name = lb.getSystemName();
        lb.setComment("a comment");
        JsonNode data = mapper.createObjectNode().put(JSON.NAME, name).putNull(JSON.COMMENT);
        validateData(JsonLayoutBlock.LAYOUTBLOCK, data, false);
        service.doPost(JsonLayoutBlock.LAYOUTBLOCK, name, data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertNull(lb.getComment());
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        LayoutBlockManager manager = InstanceManager.getDefault(LayoutBlockManager.class);
        String name = manager.createNewLayoutBlock(null, "LB1").getSystemName();
        JsonRequest request = new JsonRequest(locale, JSON.V5, JSON.DELETE, 42);
        service.doDelete(JsonLayoutBlock.LAYOUTBLOCK, name, NullNode.getInstance(), request);
        assertNull(manager.getBySystemName(name));
        JsonException ex = assertThrows(JsonException.class,
                () -> service.doDelete(JsonLayoutBlock.LAYOUTBLOCK, name, NullNode.getInstance(), request));
        assertEquals(404, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();
        service = new JsonLayoutBlockHttpService(mapper);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
