package jmri.server.json.layoutblock;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonLayoutBlockSocketServiceTest {

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
     * @throws java.io.IOException if unexpectedly unable to write to connection
     * @throws jmri.JmriException on unexpected error handling LayoutBlock
     * @throws jmri.server.json.JsonException on unexpected error handling JSON
     */
    @Test
    public void testBlockChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(connection);
        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LayoutBlock1");
        Assert.assertNotNull("Required LayoutBlock not created", lb);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, lb.getSystemName());
        Assert.assertEquals("Block has only one listener", 1, lb.getNumPropertyChangeListeners());
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message, JSON.POST, Locale.ENGLISH);
        Assert.assertEquals("Block is being listened to by service", 2, lb.getNumPropertyChangeListeners());
        connection.sendMessage((JsonNode) null);
        lb.redrawLayoutBlockPanels();
        Assert.assertEquals("Message is LayoutBlock1", lb.getSystemName(), connection.getMessage().path(JSON.DATA).path(JSON.NAME).asText());
        // test IOException handling when listening by triggering execption and
        // observing that block1 is no longer being listened to
        connection.setThrowIOException(true);
        lb.redrawLayoutBlockPanels();
        Assert.assertEquals("Block is no longer listened to by service", 1, lb.getNumPropertyChangeListeners());
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK, message, JSON.POST, Locale.ENGLISH);
        Assert.assertEquals("Block is being listened to by service", 2, lb.getNumPropertyChangeListeners());
        instance.onClose();
        Assert.assertEquals("Block is no longer listened to by service", 1, lb.getNumPropertyChangeListeners());
    }

    /**
     * Test of onMessage method, of class JsonLayoutBlockSocketService.
     *
     * @throws java.lang.Exception for unexpected errors
     */
    @Test
    public void testOnMessage() throws Exception {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LayoutBlock1");
        Assert.assertNotNull("LayoutBlock is created", lb);
        Assert.assertEquals("LayoutBlock has 1 listener", 1, lb.getPropertyChangeListeners().length);
        // test GETs
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(connection);
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\"}"),
                JSON.GET, Locale.ENGLISH);
        // onMessage causes a listener to be added to requested LayoutBlocks if not already listening
        Assert.assertEquals("LayoutBlock has 2 listeners", 2, lb.getPropertyChangeListeners().length);
        // test POSTs
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\", \"userName\":\"LayoutBlock2\"}"),
                JSON.GET, Locale.ENGLISH);
        // onMessage causes a listener to be added to requested LayoutBlocks if not already listening
        Assert.assertEquals("LayoutBlock has 2 listeners", 2, lb.getPropertyChangeListeners().length);
        Assert.assertEquals("LayoutBlock user name is changed", "LayoutBlock2", lb.getUserName());
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\", \"comment\":\"this is a comment\"}"),
                JSON.GET, Locale.ENGLISH);
        Assert.assertEquals("LayoutBlock has comment", "this is a comment", lb.getComment());
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\", \"comment\":null}"),
                JSON.GET, Locale.ENGLISH);
        Assert.assertNull("LayoutBlock has no comment", lb.getComment());
        // test PUTSs
        try {
            instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                    instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\", \"userName\":\"LayoutBlock2\"}"),
                    JSON.PUT,
                    Locale.ENGLISH);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Error code is HTTP \"method not allowed\"", 405, ex.getCode());
            Assert.assertEquals("Error message is \"not allowed\"", "Putting layoutBlock is not allowed.", ex.getMessage());
        }
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
        Assert.assertNotNull("LayoutBlock1 is created", lb1);
        Assert.assertNotNull("LayoutBlock2 is created", lb2);
        Assert.assertEquals("LayoutBlock1 has 1 listener", 1, lb1.getPropertyChangeListeners().length);
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(connection);
        instance.onList(JsonLayoutBlock.LAYOUTBLOCK, null, Locale.ENGLISH);
        // onList adds a listener to all LayoutBlocks
        Assert.assertEquals("LayoutBlock1 has 2 listeners", 2, lb1.getPropertyChangeListeners().length);
        JsonNode message = connection.getMessage();
        Assert.assertTrue("Message is an array", message.isArray());
        Assert.assertEquals("All LayoutBlocks are listed", manager.getNamedBeanList().size(), message.size());
    }

    /**
     * Test of onClose method, of class JsonLayoutBlockSocketService.
     *
     * @throws java.lang.Exception for unexpected errors
     */
    @Test
    public void testOnClose() throws Exception {
        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "LayoutBlock1");
        Assert.assertNotNull("LayoutBlock is created", lb);
        Assert.assertEquals("LayoutBlock has 1 listener", 1, lb.getPropertyChangeListeners().length);
        JsonLayoutBlockSocketService instance = new JsonLayoutBlockSocketService(new JsonMockConnection((DataOutputStream) null));
        instance.onMessage(JsonLayoutBlock.LAYOUTBLOCK,
                instance.getConnection().getObjectMapper().readTree("{\"name\":\"" + lb.getSystemName() + "\"}"),
                JSON.GET, Locale.ENGLISH);
        // onMessage causes a listener to be added to requested LayoutBlocks
        Assert.assertEquals("LayoutBlock has 2 listeners", 2, lb.getPropertyChangeListeners().length);
        instance.onClose();
        // onClose removes listeners
        Assert.assertEquals("LayoutBlock has 1 listener", 1, lb.getPropertyChangeListeners().length);
    }

}
