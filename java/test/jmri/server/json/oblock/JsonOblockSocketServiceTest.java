package jmri.server.json.oblock;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import jmri.Block;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonOblockSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testOblockChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "OB1");
        // create block *before* creating service to ensure service does not pick up change in number
        // of blocks when creating block for test
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        OBlock oblock1 = manager.provideOBlock("OB1");
        assertEquals( 1, oblock1.getNumPropertyChangeListeners(), "OBlock has only one listener");
        JsonOblockSocketService service = new JsonOblockSocketService(connection);
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, oblock1.getNumPropertyChangeListeners(), "OBlock is being listened to by service");
        JsonNode result = connection.getMessage();
        assertNotNull(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        oblock1.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return oblock1.getState() == Block.OCCUPIED;
        }, "OBlock to throw");
        result = connection.getMessage();
        assertNotNull(result);
        assertEquals(JSON.ON, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        oblock1.setState(Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return oblock1.getState() == Block.UNOCCUPIED;
        }, "OBlock to close");
        assertEquals(Block.UNOCCUPIED, oblock1.getState());
        result = connection.getMessage();
        assertNotNull(result);
        assertEquals(JSON.OFF, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        // test IOException handling when listening by triggering exception and
        // observing that oblock1 is no longer being listened to
        connection.setThrowIOException(true);
        oblock1.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return oblock1.getState() == Block.OCCUPIED;
        }, "OBlock to close");
        assertEquals(Block.OCCUPIED, oblock1.getState());
        assertEquals( 1, oblock1.getNumPropertyChangeListeners(), "OBlock is no longer listened to by service");
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, oblock1.getNumPropertyChangeListeners(), "OBlock is being listened to by service");
        service.onClose();
        assertEquals( 1, oblock1.getNumPropertyChangeListeners(), "OBlock is no longer listened to by service");
    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonOblockSocketService service = new JsonOblockSocketService(connection);
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        OBlock oblock1 = manager.provideOBlock("OB1");
        // OBlock ALLOCATED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, JSON.ALLOCATED);
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(JSON.ALLOCATED, oblock1.getState());
        // OBlock OCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, JSON.ON);
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Block.OCCUPIED, oblock1.getState());
        // OBlock UNKNOWN - remains OCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, JSON.UNKNOWN);
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(Block.OCCUPIED, oblock1.getState());
        // OBlock Invalid State
        JsonNode messageEx = connection.getObjectMapper().createObjectNode().
            put(JSON.NAME, "OB1").put(JsonOblock.STATUS, 42); // invalid state
        JsonException exception = assertThrows( JsonException.class, () ->
            service.onMessage(JsonOblock.OBLOCK, messageEx,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)));
        assertEquals(Block.OCCUPIED, oblock1.getState());
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
        // TODO test Train (name?), test Allocated (special values)
    }

    @Test
    public void testOnMessagePut() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonOblockSocketService service = new JsonOblockSocketService(connection);
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        // OBlock UNOCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, Block.UNOCCUPIED);
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        OBlock oblock1 = manager.getBySystemName("OB1");
        assertNotNull( oblock1, "OBlock was created by PUT");
        assertEquals(Block.UNOCCUPIED, oblock1.getState());
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
