package jmri.server.json.block;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonBlockHttpServiceTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initReporterManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testDoGet() throws JmriException, IOException {
        JsonBlockHttpService service = new JsonBlockHttpService(new ObjectMapper());
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        JsonNode result;
        JsonSchemaServiceCache schemaCache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        try {
            result = service.doGet(JsonBlock.BLOCK, "IB1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JsonBlock.BLOCK, result.path(JSON.TYPE).asText());
            Assert.assertEquals("IB1", result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
            block1.setState(Block.OCCUPIED);
            JUnitUtil.waitFor(() -> {
                return block1.getState() == Block.OCCUPIED;
            }, "Block to become occupied");
            result = service.doGet(JsonBlock.BLOCK, "IB1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            block1.setState(Block.UNOCCUPIED);
            result = service.doGet(JsonBlock.BLOCK, "IB1", Locale.ENGLISH);
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPost() throws JmriException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonBlockHttpService service = new JsonBlockHttpService(mapper);
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        JsonSchemaServiceCache schemaCache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        try {
            // set off
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.INACTIVE);
            JsonNode result = service.doPost(JsonBlock.BLOCK, "IB1", message, Locale.ENGLISH);
            Assert.assertEquals(Block.UNOCCUPIED, block1.getState());
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.INACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set on
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.ACTIVE);
            result = service.doPost(JsonBlock.BLOCK, "IB1", message, Locale.ENGLISH);
            Assert.assertEquals(Block.OCCUPIED, block1.getState());
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set unknown - remains on
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.UNKNOWN);
            result = service.doPost(JsonBlock.BLOCK, "IB1", message, Locale.ENGLISH);
            Assert.assertEquals(Block.OCCUPIED, block1.getState());
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertEquals(JSON.ACTIVE, result.path(JSON.DATA).path(JSON.STATE).asInt());
            // set invalid state
            message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, 42); // Invalid value
            JsonException exception = null;
            try {
                service.doPost(JsonBlock.BLOCK, "IB1", message, Locale.ENGLISH);
            } catch (JsonException ex) {
                exception = ex;
            }
            Assert.assertEquals(Block.OCCUPIED, block1.getState());
            Assert.assertNotNull(exception);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testDoPut() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonBlockHttpService service = new JsonBlockHttpService(mapper);
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        JsonSchemaServiceCache schemaCache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        try {
            // add a block
            Assert.assertNull(manager.getBlock("IB1"));
            JsonNode message = mapper.createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, Block.UNOCCUPIED);
            JsonNode result = service.doPut(JsonBlock.BLOCK, "IB1", message, Locale.ENGLISH);
            Assert.assertNotNull(result);
            schemaCache.validateMessage(result, true, Locale.ENGLISH);
            Assert.assertNotNull(manager.getBlock("IB1"));
        } catch (JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * Test of doGetList method, of class JsonBlockHttpService.
     */
    @Test
    public void testDoGetList() throws Exception {
        InstanceManager.getDefault(BlockManager.class).createNewBlock("test");
        JsonBlockHttpService instance = new JsonBlockHttpService(new ObjectMapper());
        JsonNode result = instance.doGetList(JsonBlock.BLOCK, Locale.ITALY);
        Assert.assertEquals(1, result.size());
        InstanceManager.getDefault(JsonSchemaServiceCache.class).validateMessage(result, true, Locale.ENGLISH);
    }

    /**
     * Test of doSchema method, of class JsonBlockHttpService.
     *
     * @throws jmri.server.json.JsonException if something goes wrong
     */
    @Test
    public void testDoSchema() throws JsonException {
        JsonBlockHttpService instance = new JsonBlockHttpService(new ObjectMapper());
        JsonNode block = instance.doSchema(JsonBlock.BLOCK, false, Locale.ITALY);
        JsonNode blocks = instance.doSchema(JsonBlock.BLOCK, false, Locale.ITALY);
        Assert.assertNotNull("Has client schema for block", block);
        Assert.assertNotNull("Has client schema for blocks", blocks);
        Assert.assertEquals("Client schema for block and blocks is the same", block, blocks);
        block = instance.doSchema(JsonBlock.BLOCK, true, Locale.ITALY);
        blocks = instance.doSchema(JsonBlock.BLOCK, true, Locale.ITALY);
        Assert.assertNotNull("Has server schema for block", block);
        Assert.assertNotNull("Has server schema for blocks", blocks);
        Assert.assertEquals("Server schema for block and blocks is the same", block, blocks);
    }

}
