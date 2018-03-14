package jmri.server.json.block;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

    /**
     * Test of doGet method, of class JsonBlockHttpService.
     */
    @Test
    @Ignore
    public void testDoGet() throws Exception {
    }

    /**
     * Test of doPost method, of class JsonBlockHttpService.
     */
    @Test
    @Ignore
    public void testDoPost() throws Exception {
    }

    /**
     * Test of doPut method, of class JsonBlockHttpService.
     */
    @Test
    @Ignore
    public void testDoPut() throws Exception {
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
        // JsonHttpServiceTest.testValidJmriJsonMessage(result);
        // JsonHttpServiceTest.testSchemaValidJson(result.get(0).path(JSON.DATA), instance.doSchema(JsonBlock.BLOCK, true, Locale.ITALY));
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
