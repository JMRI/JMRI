package jmri.server.json.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;

public class JsonOperationsHttpServiceTest {

    private JsonOperationsHttpService service;
    private Locale locale = Locale.ENGLISH;

    @Test
    public void testCarType() throws JsonException {
        JsonNode result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(), locale, 42);
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(33, result.size());
        result = service.doPut(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(), locale, 42);
        assertNotNull(result);
        assertEquals(JsonOperations.CAR_TYPE, result.path(JSON.TYPE).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.CARS).isArray());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.LOCATIONS).isArray());
        JsonNode result2 = service.doGet(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(), locale, 42);
        assertEquals(result, result2);
        result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(), locale, 42);
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(34, result.size());
        assertEquals(JsonOperations.CAR_TYPE, result.path(0).path(JSON.TYPE).asText());
        assertEquals("test1", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(result.path(0).path(JSON.DATA).path(JsonOperations.CARS).isArray());
        assertTrue(result.path(0).path(JSON.DATA).path(JsonOperations.LOCATIONS).isArray());
        service.doDelete(JsonOperations.CAR_TYPE, "test1", NullNode.getInstance(), locale, 42);
        result = service.doGetList(JsonOperations.CAR_TYPE, NullNode.getInstance(), locale, 42);
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(33, result.size());
    }

    @Test
    public void testKernel() throws JsonException {
        JsonNode result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(), locale, 42);
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
        result = service.doPut(JsonOperations.KERNEL, "test1", NullNode.getInstance(), locale, 42);
        assertNotNull(result);
        assertEquals(JsonOperations.KERNEL, result.path(JSON.TYPE).asText());
        assertEquals("test1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(JSON.DATA).path(JsonOperations.WEIGHT).asInt());
        assertTrue(result.path(JSON.DATA).path(JsonOperations.CARS).isArray());
        JsonNode result2 = service.doGet(JsonOperations.KERNEL, "test1", NullNode.getInstance(), locale, 42);
        assertEquals(result, result2);
        result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(), locale, 42);
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(1, result.size());
        assertEquals(JsonOperations.KERNEL, result.path(0).path(JSON.TYPE).asText());
        assertEquals("test1", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JSON.LENGTH).asInt());
        assertEquals(0, result.path(0).path(JSON.DATA).path(JsonOperations.WEIGHT).asInt());
        assertTrue(result.path(0).path(JSON.DATA).path(JsonOperations.CARS).isArray());
        service.doDelete(JsonOperations.KERNEL, "test1", NullNode.getInstance(), locale, 42);
        result = service.doGetList(JsonOperations.KERNEL, NullNode.getInstance(), locale, 42);
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(0, result.size());
    }

    @Test
    public void testDeleteInUseKernel() throws JsonException {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        Kernel k1 = manager.newKernel("test1");
        Car c1 = manager.newRS("road", "1");
        c1.setLength("40");
        c1.setWeight("1000");
        c1.setWeightTons("10");
        c1.setLoadName("L");
        c1.setKernel(k1);
        String deleteToken = null;
        try {
            service.doDelete(JsonOperations.KERNEL, "test1", NullNode.getInstance(), locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            deleteToken = ex.getAdditionalData().path(JSON.FORCE_DELETE).asText();
            assertTrue(ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).isArray());
            assertEquals(1, ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).size());
            assertEquals(JsonOperations.CAR,
                    ex.getJsonMessage().path(JSON.DATA).path(JSON.CONFLICT).path(0).path(JSON.TYPE).asText());
        }
        assertNotNull(deleteToken);
        ObjectNode data = service.getObjectMapper().createObjectNode().put(JSON.FORCE_DELETE, deleteToken);
        service.doDelete(JsonOperations.KERNEL, "test1", data, locale, 42);
        assertNull(c1.getKernel());
    }

    @Before
    public void setUp() {
        service = new JsonOperationsHttpService(new ObjectMapper());
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        service = null;
        JUnitUtil.tearDown();
    }
}