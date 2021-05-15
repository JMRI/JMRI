package jmri.server.json.oblock;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 */
public class JsonOblockSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void testOblockChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "OB1");
        // create block *before* creating service to ensure service does not pick up change in number
        // of blocks when creating block for test
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        OBlock oblock1 = manager.provideOBlock("OB1");
        Assert.assertEquals("OBlock has only one listener", 1, oblock1.getNumPropertyChangeListeners());
        JsonOblockSocketService service = new JsonOblockSocketService(connection);
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        Assert.assertEquals("OBlock is being listened to by service", 2, oblock1.getNumPropertyChangeListeners());
        JsonNode result = connection.getMessage();
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        oblock1.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return oblock1.getState() == Block.OCCUPIED;
        }, "OBlock to throw");
        result = connection.getMessage();
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.ON, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        oblock1.setState(Block.UNOCCUPIED);
        JUnitUtil.waitFor(() -> {
            return oblock1.getState() == Block.UNOCCUPIED;
        }, "OBlock to close");
        Assert.assertEquals(Block.UNOCCUPIED, oblock1.getState());
        result = connection.getMessage();
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.OFF, result.path(JSON.DATA).path(JsonOblock.STATUS).asInt());
        // test IOException handling when listening by triggering exception and
        // observing that oblock1 is no longer being listened to
        connection.setThrowIOException(true);
        oblock1.setState(Block.OCCUPIED);
        JUnitUtil.waitFor(() -> {
            return oblock1.getState() == Block.OCCUPIED;
        }, "OBlock to close");
        Assert.assertEquals(Block.OCCUPIED, oblock1.getState());
        Assert.assertEquals("OBlock is no longer listened to by service", 1, oblock1.getNumPropertyChangeListeners());
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        Assert.assertEquals("OBlock is being listened to by service", 2, oblock1.getNumPropertyChangeListeners());
        service.onClose();
        Assert.assertEquals("OBlock is no longer listened to by service", 1, oblock1.getNumPropertyChangeListeners());
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
        Assert.assertEquals(JSON.ALLOCATED, oblock1.getState());
        // OBlock OCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, JSON.ON);
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        Assert.assertEquals(Block.OCCUPIED, oblock1.getState());
        // OBlock UNKNOWN - remains OCCUPIED
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, JSON.UNKNOWN);
        service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        Assert.assertEquals(Block.OCCUPIED, oblock1.getState());
        // OBlock Invalid State
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "OB1").put(JsonOblock.STATUS, 42); // invalid state
        JsonException exception = null;
        try {
            service.onMessage(JsonOblock.OBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertEquals(Block.OCCUPIED, oblock1.getState());
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, exception.getCode());
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
        Assert.assertNotNull("OBlock was created by PUT", oblock1);
        Assert.assertEquals(Block.UNOCCUPIED, oblock1.getState());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
