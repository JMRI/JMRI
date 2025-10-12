package jmri.server.json.time;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonTimeHttpServiceTest extends JsonHttpServiceTestBase<JsonTimeHttpService> {

    @Test
    public void doGetList() throws JsonException {
        Timebase manager = InstanceManager.getDefault(Timebase.class);
        JsonNode array = service.doGetList(JSON.TIME, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(array);
        assertEquals( 1, array.size(), "One element in array");
        assertTrue( array.get(0).isObject(), "First element is a JSON object");
        assertEquals( JSON.TIME, array.get(0).path(JSON.TYPE).asText(), "JSON object type is \"time\"");
        assertTrue( array.get(0).path(JSON.DATA).path(JSON.TIME).isTextual(), "time property");
        assertEquals( manager.getRate(), array.get(0).path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0, "rate property");
        assertEquals( JSON.ON, array.get(0).path(JSON.DATA).path(JSON.STATE).asInt(), "running state");
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonTimeHttpService(mapper);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
