package jmri.server.json.idtag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.server.json.reporter.JsonReporter;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2019
 */
public class JsonIdTagHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<IdTag, JsonIdTagHttpService> {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonIdTagHttpService(mapper);
        JUnitUtil.initIdTagManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initReporterManager();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        JUnitUtil.clearShutDownManager();
        super.tearDown();
    }

    @Test
    @Override
    public void testDoGet() throws JmriException, IOException, JsonException {
        IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);
        IdTag idTag1 = manager.provide("ID1");
        Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        JsonNode result;
        // test idTag with defaults
        result = service.doGet(JsonIdTag.IDTAG, "ID1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(JsonIdTag.IDTAG, result.path(JSON.TYPE).asText());
        assertEquals("ID1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(result.path(JSON.DATA).path(JSON.USERNAME).isValueNode());
        assertTrue(result.path(JSON.DATA).path(JSON.USERNAME).isNull());
        assertEquals(IdTag.UNSEEN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertTrue(result.path(JSON.DATA).path(JsonReporter.REPORTER).isNull());
        assertTrue(result.path(JSON.DATA).path(JSON.TIME).isNull());
        // set idTag state and value
        idTag1.setWhereLastSeen(reporter1);
        JUnitUtil.waitFor(() -> {
            return idTag1.getState() == IdTag.SEEN;
        }, "IdTag to be seen");
        result = service.doGet(JsonIdTag.IDTAG, "ID1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(IdTag.SEEN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals(reporter1.getSystemName(), result.path(JSON.DATA).path(JsonReporter.REPORTER).asText());
        assertEquals(new StdDateFormat().format(idTag1.getWhenLastSeen()),
                result.path(JSON.DATA).path(JSON.TIME).asText());
        // change idTag state
        idTag1.setState(IdTag.UNKNOWN);
        result = service.doGet(JsonIdTag.IDTAG, "ID1", NullNode.getInstance(), locale, 0);
        validate(result);
        assertEquals(JSON.UNKNOWN, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals(reporter1.getSystemName(), result.path(JSON.DATA).path(JsonReporter.REPORTER).asText());
        try {
            // add an invalid idTag by using a turnout name instead of a idTag name
            assertNull(manager.getIdTag("IT1"));
            service.doGet(JsonIdTag.IDTAG, "IT1", NullNode.getInstance(), locale, 0);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
    }

    @Test
    public void testDoPost() throws JmriException, IOException, JsonException {
        IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);
        IdTag idTag1 = manager.provideIdTag("ID1");
        // set non-existing reporter
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "ID1").put(JsonReporter.REPORTER, "IR1");
        assertNull("No reporter", idTag1.getWhereLastSeen());
        try {
            service.doPost(JsonIdTag.IDTAG, "ID1", message, locale, 0);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals("404 Not Found", 404, ex.getCode());
        }
        // set existing reporter
        Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        service.doPost(JsonIdTag.IDTAG, "ID1", message, locale, 0);
        assertEquals("IdTag has reporter", reporter1, idTag1.getWhereLastSeen());
        // set null reporter
        message = mapper.createObjectNode().put(JSON.NAME, "ID1").putNull(JsonReporter.REPORTER);
        service.doPost(JsonIdTag.IDTAG, "ID1", message, locale, 0);
        assertNull("No reporter", idTag1.getWhereLastSeen());
        try {
            // add an invalid idTag by using a turnout name instead of a idTag name
            assertNull(manager.getIdTag("IT1"));
            message = mapper.createObjectNode().put(JSON.NAME, "II1").put(JSON.STATE, IdTag.SEEN);
            service.doPost(JsonIdTag.IDTAG, "IT1", message, locale, 0);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
    }

    @Test
    public void testDoPut() throws IOException, JsonException {
        IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);
        // add a idTag
        assertNull(manager.getIdTag("ID1"));
        JsonNode message = mapper.createObjectNode().put(JSON.NAME, "ID1");
        JsonNode result = service.doPut(JsonIdTag.IDTAG, "ID1", message, locale, 0);
        validate(result);
        assertNotNull(manager.getIdTag("ID1"));
        try {
            // add an invalid idTag by using a turnout name instead of a idTag name
            assertNull(manager.getIdTag("IT1"));
            message = mapper.createObjectNode().put(JSON.NAME, "II1");
            service.doPut(JsonIdTag.IDTAG, "", message, locale, 0); // use invalid idTag name
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(400, ex.getCode());
        }
        JUnitAppender.assertErrorMessage("Invalid system name for Reporter: System name must start with \"ID\".");
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        ObjectNode message = mapper.createObjectNode();
        IdTagManager manager = InstanceManager.getDefault(IdTagManager.class);
        manager.provide("ID1");
        // delete an idTag
        assertNotNull(manager.getBeanBySystemName("ID1"));
        service.doDelete(JsonIdTag.IDTAG, "ID1", NullNode.getInstance(), locale, 0);
        assertNull(manager.getBeanBySystemName("ID1"));
        manager.provide("ID1").addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // do nothing
            }
        }, "ID1", "Test Listener");
        // delete an idTag with a named listener ref
        assertNotNull(manager.getBeanBySystemName("ID1"));
        try {
            // first attempt should fail on conflict
            service.doDelete(JsonIdTag.IDTAG, "ID1", NullNode.getInstance(), locale, 0);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(1, ex.getAdditionalData().path(JSON.CONFLICT).size());
            assertEquals("Test Listener", ex.getAdditionalData().path(JSON.CONFLICT).path(0).asText());
            message = message.put(JSON.FORCE_DELETE, ex.getAdditionalData().path(JSON.FORCE_DELETE).asText());
        }
        assertNotNull(manager.getBeanBySystemName("ID1"));
        // will throw if prior catch failed
        service.doDelete(JsonIdTag.IDTAG, "ID1", message, locale, 0);
        assertNull(manager.getBeanBySystemName("ID1"));
        try {
            // deleting again should throw an exception
            service.doDelete(JsonIdTag.IDTAG, "ID1", NullNode.getInstance(), locale, 0);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
    }

    /**
     * Test of doGetList method, of class JsonIdTagHttpService.
     * @throws java.lang.Exception rethrows any exceptions from instance.doGetList()
     */
    @Test
    public void testDoGetList() throws Exception {
        InstanceManager.getDefault(IdTagManager.class).provide("test");
        JsonIdTagHttpService instance = new JsonIdTagHttpService(mapper);
        JsonNode result = instance.doGetList(JsonIdTag.IDTAG, mapper.createObjectNode(), locale, 0);
        assertEquals(1, result.size());
        validate(result);
    }

}
