package jmri.server.json.layoutblock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Randall Wood Copyright 2018
 */
public class JsonLayoutBlockSocketServiceTest {

    private final Locale locale = Locale.ENGLISH;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    /**
     * Test property change listener on LayoutBlocks.
     *
     * @throws java.io.IOException            if unexpectedly unable to write to
     *                                        connection
     * @throws jmri.JmriException             on unexpected error handling
     *                                        LayoutBlock
     * @throws jmri.server.json.JsonException on unexpected error handling JSON
     */
    @Test
    public void testBlockChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(connection);
        LayoutBlock lb =
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LayoutBlock1");
        assertNotNull( lb, "Required LayoutBlock not created");
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, lb.getSystemName());
        assertEquals( 1, lb.getNumPropertyChangeListeners(), "Block has only one listener");
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, lb.getNumPropertyChangeListeners(), "Block is being listened to by service");
        connection.sendMessage(null, 0);
        lb.redrawLayoutBlockPanels();
        JsonNode result = connection.getMessage();
        assertNotNull(result);
        assertEquals( lb.getSystemName(), result.path(JSON.DATA).path(JSON.NAME).asText(),
            "Message is LayoutBlock1");
        // test IOException handling when listening by triggering exception and
        // observing that block1 is no longer being listened to
        connection.setThrowIOException(true);
        lb.redrawLayoutBlockPanels();
        assertEquals( 1, lb.getNumPropertyChangeListeners(), "Block is no longer listened to by service");
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( 2, lb.getNumPropertyChangeListeners(), "Block is being listened to by service");
        instance.onClose();
        assertEquals( 1, lb.getNumPropertyChangeListeners(), "Block is no longer listened to by service");
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
        assertNotNull( lb, "LayoutBlock is created");
        assertEquals( 1, lb.getPropertyChangeListeners().length, "LayoutBlock has 1 listener");
        // test GETs
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(connection);
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\"}"),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        // onMessage causes a listener to be added to requested LayoutBlocks if
        // not already listening
        assertEquals( 2, lb.getPropertyChangeListeners().length, "LayoutBlock has 2 listeners");
        // test POSTs
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper()
                        .readTree("{\"name\":\"" + lb.getSystemName() + "\", \"userName\":\"LayoutBlock2\"}"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        // onMessage causes a listener to be added to requested LayoutBlocks if
        // not already listening
        assertEquals( 2, lb.getPropertyChangeListeners().length, "LayoutBlock has 2 listeners");
        assertEquals( "LayoutBlock2", lb.getUserName(), "LayoutBlock user name is changed");
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper()
                        .readTree("{\"name\":\"" + lb.getSystemName() + "\", \"comment\":\"this is a comment\"}"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertEquals( "this is a comment", lb.getComment(), "LayoutBlock has comment");
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper()
                        .readTree("{\"name\":\"" + lb.getSystemName() + "\", \"comment\":null}"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertNull( lb.getComment(), "LayoutBlock has no comment");
        // test PUTs
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":null, \"userName\":\"LayoutBlock3\"}"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        assertNotNull( manager.getLayoutBlock("LayoutBlock3"), "New LayoutBlock created");
        // test DELETEs
        // first add a named reference listener to trigger a deletion conflict
        lb.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // do nothing
            }
        }, lb.getUserName(), "Test Listener");
        ObjectNode message =
                instance.getConnection().getObjectMapper().createObjectNode().put(JSON.NAME, lb.getSystemName());
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message,
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42)),
            "Expected exception not thrown");
        assertEquals(409, ex.getCode());
        assertEquals(1, ex.getAdditionalData().path(JSON.CONFLICT).size());
        assertEquals("Test Listener", ex.getAdditionalData().path(JSON.CONFLICT).path(0).asText());
        ObjectNode message2 = message.put(JSON.FORCE_DELETE, ex.getAdditionalData().path(JSON.FORCE_DELETE).asText());

        assertNotNull( manager.getBySystemName(lb.getSystemName()), "LayoutBlock not deleted");
        // will throw if prior catch failed
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message2, new JsonRequest(locale, JSON.V5, JSON.DELETE, 42));
        assertNull( manager.getBySystemName(lb.getSystemName()), "LayoutBlock deleted");
        ex = assertThrows( JsonException.class, () ->
            // deleting again should throw an exception
            instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message2,
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42)),
            "Expected exception not thrown.");
        assertEquals(404, ex.getCode());

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
        assertNotNull( lb1, "LayoutBlock1 is created");
        assertNotNull( lb2, "LayoutBlock2 is created");
        assertEquals( 1, lb1.getPropertyChangeListeners().length, "LayoutBlock1 has 1 listener");
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(connection);
        instance.onList(JsonLayoutBlock.LAYOUTBLOCK, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        // onList should not add a listener to all LayoutBlocks
        assertEquals( 1, lb1.getPropertyChangeListeners().length, "LayoutBlock1 has 1 listener");
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertTrue( message.isArray(), "Message is an array");
        assertEquals( manager.getNamedBeanSet().size(), message.size(), "All LayoutBlocks are listed");
        instance.onClose(); // clean up
    }

    /**
     * Test of onClose method, of class JsonLayoutBlockSocketService.
     *
     * @throws java.lang.Exception for unexpected errors
     */
    @Test
    public void testOnClose() throws Exception {
        LayoutBlock lb =
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LayoutBlock1");
        assertNotNull( lb, "LayoutBlock is created");
        assertEquals( 1, lb.getPropertyChangeListeners().length, "LayoutBlock has 1 listener");
        JsonLayoutBlockSocketService instance =
                new JsonLayoutBlockSocketService(new JsonMockConnection((DataOutputStream) null));
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\"}"),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        // onMessage causes a listener to be added to requested LayoutBlocks
        assertEquals( 2, lb.getPropertyChangeListeners().length, "LayoutBlock has 2 listeners");
        instance.onClose();
        // onClose removes listeners
        assertEquals( 1, lb.getPropertyChangeListeners().length, "LayoutBlock has 1 listener");
    }

}
