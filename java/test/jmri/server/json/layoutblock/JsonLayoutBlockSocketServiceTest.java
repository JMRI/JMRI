package jmri.server.json.layoutblock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonLayoutBlockSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test property change listener on LayoutBlocks.
     *
     * @throws java.io.IOException            if unexpectedly unable to write to
     *                                            connection
     * @throws jmri.JmriException             on unexpected error handling
     *                                            LayoutBlock
     * @throws jmri.server.json.JsonException on unexpected error handling JSON
     */
    @Test
    public void testBlockChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(connection);
        LayoutBlock lb =
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LayoutBlock1");
        assertNotNull("Required LayoutBlock not created", lb);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, lb.getSystemName());
        assertEquals("Block has only one listener", 1, lb.getNumPropertyChangeListeners());
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message, JSON.POST, locale, 42);
        assertEquals("Block is being listened to by service", 2, lb.getNumPropertyChangeListeners());
        connection.sendMessage((JsonNode) null, 0);
        lb.redrawLayoutBlockPanels();
        JsonNode result = connection.getMessage();
        assertNotNull(result);
        assertEquals("Message is LayoutBlock1", lb.getSystemName(), result.path(JSON.DATA).path(JSON.NAME).asText());
        // test IOException handling when listening by triggering exception and
        // observing that block1 is no longer being listened to
        connection.setThrowIOException(true);
        lb.redrawLayoutBlockPanels();
        assertEquals("Block is no longer listened to by service", 1, lb.getNumPropertyChangeListeners());
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message, JSON.POST, locale, 42);
        assertEquals("Block is being listened to by service", 2, lb.getNumPropertyChangeListeners());
        instance.onClose();
        assertEquals("Block is no longer listened to by service", 1, lb.getNumPropertyChangeListeners());
    }

    /**
     * Test of onMessage method, of class JsonLayoutBlockSocketService.
     *
     * @throws java.lang.Exception for unexpected errors
     */
    @Test
    public void testOnMessage() throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        LayoutBlockManager manager = InstanceManager.getDefault(LayoutBlockManager.class);
        LayoutBlock lb = manager.createNewLayoutBlock(null, "LayoutBlock1");
        assertNotNull("LayoutBlock is created", lb);
        assertEquals("LayoutBlock has 1 listener", 1, lb.getPropertyChangeListeners().length);
        // test GETs
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(connection);
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\"}"),
                JSON.GET, locale, 42);
        // onMessage causes a listener to be added to requested LayoutBlocks if not already listening
        assertEquals("LayoutBlock has 2 listeners", 2, lb.getPropertyChangeListeners().length);
        // test POSTs
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper()
                        .readTree("{\"name\":\"" + lb.getSystemName() + "\", \"userName\":\"LayoutBlock2\"}"),
                JSON.POST, locale, 42);
        // onMessage causes a listener to be added to requested LayoutBlocks if not already listening
        assertEquals("LayoutBlock has 2 listeners", 2, lb.getPropertyChangeListeners().length);
        assertEquals("LayoutBlock user name is changed", "LayoutBlock2", lb.getUserName());
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper()
                        .readTree("{\"name\":\"" + lb.getSystemName() + "\", \"comment\":\"this is a comment\"}"),
                JSON.POST, locale, 42);
        assertEquals("LayoutBlock has comment", "this is a comment", lb.getComment());
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper()
                        .readTree("{\"name\":\"" + lb.getSystemName() + "\", \"comment\":null}"),
                JSON.POST, locale, 42);
        assertNull("LayoutBlock has no comment", lb.getComment());
        // test PUTs
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":null, \"userName\":\"LayoutBlock3\"}"),
                JSON.PUT,
                locale, 42);
        assertNotNull("New LayoutBlock created", manager.getLayoutBlock("LayoutBlock3"));
        // test DELETEs
        // first add a named reference listener to trigger a deletion conflict
        lb.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // do nothing
            }
        }, lb.getUserName(), "Test Listener");
        ObjectNode message = instance.getConnection().getObjectMapper().createObjectNode().put(JSON.NAME, lb.getSystemName());
        try {
            instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message, JSON.DELETE, locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(409, ex.getCode());
            assertEquals(1, ex.getAdditionalData().path(JSON.CONFLICT).size());
            assertEquals("Test Listener", ex.getAdditionalData().path(JSON.CONFLICT).path(0).asText());
            message = message.put(JSON.FORCE_DELETE, ex.getAdditionalData().path(JSON.FORCE_DELETE).asText());
        }
        assertNotNull("LayoutBlock not deleted", manager.getBeanBySystemName(lb.getSystemName()));
        // will throw if prior catch failed
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message, JSON.DELETE, locale, 42);
        assertNull("LayoutBlock deleted", manager.getBeanBySystemName(lb.getSystemName()));
        try {
            // deleting again should throw an exception
            instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message, JSON.DELETE, locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
        instance.onClose(); // clean up
    }

    /**
     * Test of onList method, of class JsonLayoutBlockSocketService.
     *
     * @throws java.lang.Exception on unexpected errors
     */
    @Test
    public void testOnList() throws Exception {
        LayoutBlockManager manager = InstanceManager.getDefault(LayoutBlockManager.class);
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        LayoutBlock lb1 = manager.createNewLayoutBlock(null, "LayoutBlock1");
        LayoutBlock lb2 = manager.createNewLayoutBlock(null, "LayoutBlock2");
        assertNotNull("LayoutBlock1 is created", lb1);
        assertNotNull("LayoutBlock2 is created", lb2);
        assertEquals("LayoutBlock1 has 1 listener", 1, lb1.getPropertyChangeListeners().length);
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(connection);
        instance.onList(JsonLayoutBlock.LAYOUTBLOCK, NullNode.getInstance(), locale, 0);
        // onList should not add a listener to all LayoutBlocks
        assertEquals("LayoutBlock1 has 1 listener", 1, lb1.getPropertyChangeListeners().length);
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertTrue("Message is an array", message.isArray());
        assertEquals("All LayoutBlocks are listed", manager.getNamedBeanSet().size(), message.size());
        instance.onClose(); // clean up
    }

    /**
     * Test of onClose method, of class JsonLayoutBlockSocketService.
     *
     * @throws java.lang.Exception for unexpected errors
     */
    @Test
    public void testOnClose() throws Exception {
        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LayoutBlock1");
        assertNotNull("LayoutBlock is created", lb);
        assertEquals("LayoutBlock has 1 listener", 1, lb.getPropertyChangeListeners().length);
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(new JsonMockConnection((DataOutputStream) null));
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\"}"),
                JSON.GET, locale, 42);
        // onMessage causes a listener to be added to requested LayoutBlocks
        assertEquals("LayoutBlock has 2 listeners", 2, lb.getPropertyChangeListeners().length);
        instance.onClose();
        // onClose removes listeners
        assertEquals("LayoutBlock has 1 listener", 1, lb.getPropertyChangeListeners().length);
    }

}
