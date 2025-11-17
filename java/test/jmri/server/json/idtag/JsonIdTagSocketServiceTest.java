package jmri.server.json.idtag;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.server.json.reporter.JsonReporter;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood Copyright 2019
 */
public class JsonIdTagSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testIdTagChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "ID1");
        // create idTag *before* creating service to ensure service does not pick up change in number
        // of idTags when creating idTag for test
        IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);
        IdTag idTag1 = manager.provideIdTag("ID1");
        assertEquals( 1, idTag1.getNumPropertyChangeListeners(), "IdTag has only one listener");
        JsonIdTagSocketService service = new JsonIdTagSocketService(connection);
        service.onMessage(JsonIdTag.IDTAG, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, idTag1.getNumPropertyChangeListeners(), "IdTag is being listened to by service");
        JsonNode result = connection.getMessage();
        assertNotNull(result);
        assertEquals(IdTag.UNSEEN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        idTag1.setWhereLastSeen(reporter1);
        JUnitUtil.waitFor(() -> {
            return idTag1.getState() == IdTag.SEEN;
        }, "IdTag seen");
        result = connection.getMessage();
        assertNotNull(result);
        assertEquals(IdTag.SEEN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals(reporter1.getSystemName(), result.path(JSON.DATA).path(JsonReporter.REPORTER).asText());
        idTag1.setWhereLastSeen(null);
        JUnitUtil.waitFor(() -> {
            return idTag1.getState() == IdTag.UNSEEN;
        }, "IdTag unknown state");
        assertEquals(IdTag.UNSEEN, idTag1.getState());
        result = connection.getMessage();
        assertNotNull(result);
        assertEquals(IdTag.UNSEEN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertTrue(result.path(JSON.DATA).path(JsonReporter.REPORTER).isNull());
        // test IOException handling when listening by triggering exception and
        // observing that idTag1 is no longer being listened to
        connection.setThrowIOException(true);
        idTag1.setWhereLastSeen(reporter1);
        JUnitUtil.waitFor(() -> {
            return idTag1.getState() == IdTag.SEEN;
        }, "IdTag to close");
        assertEquals(IdTag.SEEN, idTag1.getState());
        assertEquals( 1, idTag1.getNumPropertyChangeListeners(), "IdTag is no longer listened to by service");
        service.onMessage(JsonIdTag.IDTAG, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, idTag1.getNumPropertyChangeListeners(), "IdTag is being listened to by service");
        service.onClose();
        assertEquals( 1, idTag1.getNumPropertyChangeListeners(), "IdTag is no longer listened to by service");
    }

    @Test
    public void testOnMessageChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonIdTagSocketService service = new JsonIdTagSocketService(connection);
        IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);
        IdTag idTag1 = manager.provideIdTag("ID1");
        Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        // IdTag UNSEEN
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "ID1").putNull(JsonReporter.REPORTER);
        service.onMessage(JsonIdTag.IDTAG, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(IdTag.UNSEEN, idTag1.getState());
        // IdTag SEEN
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "ID1").put(JsonReporter.REPORTER, "IR1");
        service.onMessage(JsonIdTag.IDTAG, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals(IdTag.SEEN, idTag1.getState());
        assertEquals(reporter1, idTag1.getWhereLastSeen());
        // IdTag Invalid Reporter
        JsonNode message2 = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "ID1").put(JsonReporter.REPORTER, "IR2"); // invalid reporter
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JsonIdTag.IDTAG, message2, new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals(404, ex.getCode());
        assertEquals(reporter1, idTag1.getWhereLastSeen());
    }

    @Test
    public void testOnMessagePut() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode message;
        JsonIdTagSocketService service = new JsonIdTagSocketService(connection);
        IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);
        // IdTag UNSEEN
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "ID1");
        service.onMessage(JsonIdTag.IDTAG, message, new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        IdTag idTag1 = manager.getBySystemName("ID1");
        assertNotNull( idTag1, "IdTag was created by PUT");
        assertEquals(IdTag.UNSEEN, idTag1.getState());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
