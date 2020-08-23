package jmri.server.json.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;

public class JsonTimeHttpServiceTest extends JsonHttpServiceTestBase<JsonTimeHttpService> {

    @Test
    public void doGetList() throws JsonException {
        Timebase manager = InstanceManager.getDefault(Timebase.class);
        JsonNode array = service.doGetList(JSON.TIME, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(array);
        assertEquals("One element in array", 1, array.size());
        assertTrue("First element is a JSON object", array.get(0).isObject());
        assertEquals("JSON object type is \"time\"", JSON.TIME,
                array.get(0).path(JSON.TYPE).asText());
        assertTrue("time property", array.get(0).path(JSON.DATA).path(JSON.TIME).isTextual());
        assertEquals("rate property", manager.getRate(), array.get(0).path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        assertEquals("running state", JSON.ON, array.get(0).path(JSON.DATA).path(JSON.STATE).asInt());
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
