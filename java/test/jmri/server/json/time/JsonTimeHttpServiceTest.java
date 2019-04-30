package jmri.server.json.time;

import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;

public class JsonTimeHttpServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Test
    public void doGetList() throws JsonException {
        JsonTimeHttpService service = new JsonTimeHttpService(new ObjectMapper());
        Timebase manager = InstanceManager.getDefault(Timebase.class);
        JsonNode array = service.doGetList(JsonTimeServiceFactory.TIME, NullNode.getInstance(), locale, 0);
        Assert.assertNotNull(array);
        Assert.assertEquals("One element in array", 1, array.size());
        Assert.assertTrue("First element is a JSON object", array.get(0).isObject());
        Assert.assertEquals("JSON object type is \"time\"", JsonTimeServiceFactory.TIME,
                array.get(0).path(JSON.TYPE).asText());
        Assert.assertTrue("time property", array.get(0).path(JSON.DATA).path(JsonTimeServiceFactory.TIME).isTextual());
        Assert.assertEquals("rate property", manager.getRate(), array.get(0).path(JSON.DATA).path(JSON.RATE).asDouble(), 0.0);
        Assert.assertEquals("running state", JSON.ON, array.get(0).path(JSON.DATA).path(JSON.STATE).asInt());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
