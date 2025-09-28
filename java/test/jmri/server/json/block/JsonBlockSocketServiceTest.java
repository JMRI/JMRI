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
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonBlockSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testBlockChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1");
        // create block *before* creating service to ensure service does not pick up change in number
        // of blocks when creating block for test
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        Block block1 = manager.provideBlock("IB1");
        assertEquals( 1, block1.getNumPropertyChangeListeners(), "Block has only one listener");
        JsonBlockSocketService service = new JsonBlockSocketService(connection);
        service.onMessage(JsonBlock.BLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, block1.getNumPropertyChangeListeners(), "Block is being listened to by service");
        JsonNode result = connection.getMessage();
        assertNotNull(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        block1.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return block1.getState() == Block.OCCUPIED;
        }, "Block to throw");
        result = connection.getMessage();
        assertNotNull(result);
        assertEquals(JSON.ON, result.path(JSON.DATA).path(JSON.STATE).asInt());
        block1.setState(Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return block1.getState() == Block.UNOCCUPIED;
        }, "Block to close");
        assertEquals(Block.UNOCCUPIED, block1.getState());
        result = connection.getMessage();
        assertNotNull(result);
        assertEquals(JSON.OFF, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // test IOException handling when listening by triggering exception and
        // observing that block1 is no longer being listened to
        connection.setThrowIOException(true);
        block1.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return block1.getState() == Block.OCCUPIED;
        }, "Block to close");
        assertEquals(Block.OCCUPIED, block1.getState());
        assertEquals( 1, block1.getNumPropertyChangeListeners(), "Block is no longer listened to by service");
        service.onMessage(JsonBlock.BLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, block1.getNumPropertyChangeListeners(), "Block is being listened to by service");
        service.onClose();
        assertEquals( 1, block1.getNumPropertyChangeListeners(), "Block is no longer listened to by service");
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
        service.onMessage(JsonBlock.BLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Block.UNOCCUPIED, block1.getState());
        // Block OCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.ON);
        service.onMessage(JsonBlock.BLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Block.OCCUPIED, block1.getState());
        // Block UNKNOWN - remains OCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.UNKNOWN);
        service.onMessage(JsonBlock.BLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Block.OCCUPIED, block1.getState());
        // Block Invalid State
        JsonNode message2 = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, 42); // invalid state
        JsonException exception = assertThrows( JsonException.class, () ->
            service.onMessage(JsonBlock.BLOCK, message2,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)));
        assertEquals(Block.OCCUPIED, block1.getState());
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
    }

    @Test
    public void testOnMessagePut() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonBlockSocketService service = new JsonBlockSocketService(connection);
        BlockManager manager = InstanceManager.getDefault(BlockManager.class);
        // Block UNOCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IB1").put(JSON.STATE, JSON.OFF);
        service.onMessage(JsonBlock.BLOCK, message, new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        Block block1 = manager.getBySystemName("IB1");
        assertNotNull( block1, "Block was created by PUT");
        assertEquals(Block.UNOCCUPIED, block1.getState());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
