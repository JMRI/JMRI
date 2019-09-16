package jmri.server.json.block;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonBlockSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testBlockChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1");
        // create block *before* creating service to ensure service does not pick up change in number
        // of blocks when creating block for test
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        Assert.assertEquals("Block has only one listener", 1, block1.getNumPropertyChangeListeners());
        JsonBlockSocketService service = new JsonBlockSocketService(connection);
        service.onMessage(JsonBlock.BLOCK, message, JSON.POST, locale, 42);
        Assert.assertEquals("Block is being listened to by service", 2, block1.getNumPropertyChangeListeners());
        JsonNode result = connection.getMessage();
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        block1.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return block1.getState() == Block.OCCUPIED;
        }, "Block to throw");
        result = connection.getMessage();
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        block1.setState(Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return block1.getState() == Block.UNOCCUPIED;
        }, "Block to close");
        Assert.assertEquals(Block.UNOCCUPIED, block1.getState());
        result = connection.getMessage();
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // test IOException handling when listening by triggering exception and
        // observing that block1 is no longer being listened to
        connection.setThrowIOException(true);
        block1.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return block1.getState() == Block.OCCUPIED;
        }, "Block to close");
        Assert.assertEquals(Block.OCCUPIED, block1.getState());
        Assert.assertEquals("Block is no longer listened to by service", 1, block1.getNumPropertyChangeListeners());
        service.onMessage(JsonBlock.BLOCK, message, JSON.POST, locale, 42);
        Assert.assertEquals("Block is being listened to by service", 2, block1.getNumPropertyChangeListeners());
        service.onClose();
        Assert.assertEquals("Block is no longer listened to by service", 1, block1.getNumPropertyChangeListeners());
    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonBlockSocketService service = new JsonBlockSocketService(connection);
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        // Block UNOCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.OFF);
        service.onMessage(JsonBlock.BLOCK, message, JSON.POST, locale, 42);
        Assert.assertEquals(Block.UNOCCUPIED, block1.getState());
        // Block OCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.ON);
        service.onMessage(JsonBlock.BLOCK, message, JSON.POST, locale, 42);
        Assert.assertEquals(Block.OCCUPIED, block1.getState());
        // Block UNKNOWN - remains OCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.UNKNOWN);
        service.onMessage(JsonBlock.BLOCK, message, JSON.POST, locale, 42);
        Assert.assertEquals(Block.OCCUPIED, block1.getState());
        // Block Invalid State
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, 42); // invalid state
        JsonException exception = null;
        try {
            service.onMessage(JsonBlock.BLOCK, message, JSON.POST, locale, 42);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertEquals(Block.OCCUPIED, block1.getState());
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
    }

    @Test
    public void testOnMessagePut() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonBlockSocketService service = new JsonBlockSocketService(connection);
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        // Block UNOCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.OFF);
        service.onMessage(JsonBlock.BLOCK, message, JSON.PUT, locale, 42);
        Block block1 = manager.getBySystemName("IB1");
        Assert.assertNotNull("Block was created by PUT", block1);
        Assert.assertEquals(Block.UNOCCUPIED, block1.getState());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
