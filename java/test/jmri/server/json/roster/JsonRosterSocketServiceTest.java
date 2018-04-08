package jmri.server.json.roster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
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
 * @author Randall Wood (C) 2016
 */
public class JsonRosterSocketServiceTest {

    private JsonMockConnection connection = null;

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initConfigureManager();
        InstanceManager.setDefault(Roster.class, new Roster("java/test/jmri/server/json/roster/data/roster.xml"));
        connection = new JsonMockConnection((DataOutputStream) null);
    }

    @After
    public void tearDown() throws Exception {
        connection = null;
        JUnitUtil.tearDown();
    }

    /**
     * Test of listen method, and of listeners.
     *
     * @throws java.io.IOException            in event of unexpected exception
     * @throws jmri.JmriException             in event of unexpected exception
     * @throws jmri.server.json.JsonException in event of unexpected exception
     */
    @Test
    public void testListen() throws IOException, JmriException, JsonException {
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        Assert.assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            Assert.assertEquals(1, entry.getPropertyChangeListeners().length);
        });
        // add the first time
        instance.listen();
        Assert.assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            Assert.assertEquals(3, entry.getPropertyChangeListeners().length);
        });
        // don't add the second time
        instance.listen();
        Assert.assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            Assert.assertEquals(3, entry.getPropertyChangeListeners().length);
        });
        // list the groups in a JSON message for assertions
        this.connection.sendMessage((JsonNode) null);
        instance.onMessage(JsonRoster.ROSTER_GROUPS, this.connection.getObjectMapper().createObjectNode(), JSON.GET, Locale.ENGLISH);
        JsonNode message = this.connection.getMessage();
        Assert.assertEquals("Single message sent", 1, this.connection.getMessages().size());
        Assert.assertNotNull("Message was sent", message);
        Assert.assertTrue("Message is array", message.isArray());
        Assert.assertEquals("Two groups exist", 2, message.size());
        Assert.assertTrue("Contains group TestGroup1", message.findValuesAsText(JSON.NAME).contains("testGroup1"));
        Assert.assertTrue("Contains group AllEntries", message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(Locale.ENGLISH)));
        // add a roster group and verify message sent by listener
        this.connection.sendMessage((JsonNode) null);
        Roster.getDefault().addRosterGroup("NewRosterGroup");
        Assert.assertEquals("Single message sent", 1, this.connection.getMessages().size());
        message = this.connection.getMessage();
        Assert.assertNotNull("Message was sent", message);
        Assert.assertEquals("Three groups exist", 3, message.size());
        Assert.assertTrue("Contains group TestGroup1", message.findValuesAsText(JSON.NAME).contains("testGroup1"));
        Assert.assertTrue("Contains group AllEntries", message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(Locale.ENGLISH)));
        Assert.assertTrue("Contains group NewRosterGroup", message.findValuesAsText(JSON.NAME).contains("NewRosterGroup"));
        // rename a roster group and verify message sent by listener
        this.connection.sendMessage((JsonNode) null);
        Roster.getDefault().getRosterGroups().get("NewRosterGroup").setName("AgedRosterGroup");
        Assert.assertEquals("Single message sent", 1, this.connection.getMessages().size());
        message = this.connection.getMessage();
        Assert.assertNotNull("Message was sent", message);
        Assert.assertEquals("Three groups exist", 3, message.size());
        Assert.assertTrue("Contains group TestGroup1", message.findValuesAsText(JSON.NAME).contains("testGroup1"));
        Assert.assertTrue("Contains group AllEntries", message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(Locale.ENGLISH)));
        Assert.assertTrue("Contains group AgedRosterGroup", message.findValuesAsText(JSON.NAME).contains("AgedRosterGroup"));
        Assert.assertFalse("Contains group NewRosterGroup", message.findValuesAsText(JSON.NAME).contains("NewRosterGroup"));
        // remove a roster group and verify message sent by listener
        this.connection.sendMessage((JsonNode) null);
        Roster.getDefault().removeRosterGroup(Roster.getDefault().getRosterGroups().get("AgedRosterGroup"));
        Assert.assertEquals("Single message sent", 1, this.connection.getMessages().size());
        message = this.connection.getMessage();
        Assert.assertNotNull("Message was sent", message);
        Assert.assertEquals("Two groups exist", 2, message.size());
        Assert.assertTrue("Contains group TestGroup1", message.findValuesAsText(JSON.NAME).contains("testGroup1"));
        Assert.assertTrue("Contains group AllEntries", message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(Locale.ENGLISH)));
        Assert.assertFalse("Contains group NewRosterGroup", message.findValuesAsText(JSON.NAME).contains("AgedRosterGroup"));
        Assert.assertFalse("Contains group NewRosterGroup", message.findValuesAsText(JSON.NAME).contains("NewRosterGroup"));
        // Set unknown roster group directly as attribute of RosterEntry
        this.connection.sendMessage((JsonNode) null);
        RosterEntry re = Roster.getDefault().getEntryForId("testEntry1");
        Assert.assertEquals("instance is listening to RosterEntry", 3, re.getPropertyChangeListeners().length);
        re.putAttribute(Roster.ROSTER_GROUP_PREFIX + "attribute", "yes");
        Assert.assertEquals("No message sent", 0, this.connection.getMessages().size());
        // Set known roster group directly as attribute of RosterEntry
        Roster.getDefault().addRosterGroup("NewRosterGroup");
        JUnitUtil.waitFor(() -> {
            return Roster.getDefault().getRosterGroupList().contains("NewRosterGroup");
        }, "Roster Group was not added");
        this.connection.sendMessage((JsonNode) null); // clear out messages
        re.putAttribute(Roster.ROSTER_GROUP_PREFIX + "NewRosterGroup", "yes"); // add new group to roster entry
        JUnitUtil.waitFor(() -> {
            return this.connection.getMessages().size() != 0;
        }, "No messages sent");
        // Sent updated rosterEntry, rosterGroup, array of rosterGroup
        ArrayNode messages = this.connection.getMessages();
        Assert.assertEquals("3 messages sent", 3, messages.size());
        // Check that 5 top-level types are in the 3 messages
        List<String> values = messages.findValuesAsText("type");
        values.sort(null); // sort because message order is non-deterministic
        Assert.assertArrayEquals("Objects are 1 rosterEntry and 4 rosterGroup",
                new String[]{JsonRoster.ROSTER_ENTRY, JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP},
                values.toArray(new String[5]));
    }

    /**
     * Test of onMessage method DELETE on a ROSTER
     *
     * @throws java.io.IOException this is an error, not a failure, in the test
     * @throws jmri.JmriException  this is an error, not a failure, in the test
     */
    @Test
    public void testOnMessageDeleteRoster() throws IOException, JmriException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        try {
            instance.onMessage(JsonRoster.ROSTER, data, JSON.DELETE, locale);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
        }
    }

    /**
     * Test of onMessage method POST on a ROSTER
     *
     * @throws java.io.IOException this is an error, not a failure, in the test
     * @throws jmri.JmriException  this is an error, not a failure, in the test
     */
    @Test
    public void testOnMessagePostRoster() throws IOException, JmriException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode().put(JSON.METHOD, JSON.POST);
        Locale locale = Locale.ENGLISH;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        try {
            instance.onMessage(JsonRoster.ROSTER, data, JSON.POST, locale);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, ex.getCode());
        }
    }

    /**
     * Test of onMessage method PUT on a ROSTER
     *
     * @throws java.io.IOException this is an error, not a failure, in the test
     * @throws jmri.JmriException  this is an error, not a failure, in the test
     */
    @Test
    public void testOnMessagePutRoster() throws IOException, JmriException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode().put(JSON.METHOD, JSON.PUT);
        Locale locale = Locale.ENGLISH;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        try {
            instance.onMessage(JsonRoster.ROSTER, data, JSON.POST, locale);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, ex.getCode());
        }
    }

    /**
     * Test of onMessage method GET on a ROSTER
     *
     * @throws java.io.IOException            this is an error, not a failure,
     *                                        in the test
     * @throws jmri.JmriException             this is an error, not a failure,
     *                                        in the test
     * @throws jmri.server.json.JsonException this is an error, not a failure,
     *                                        in the test
     */
    @Test
    public void testOnMessageGetRoster() throws IOException, JmriException, JsonException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        // assert we have not been listening
        Assert.assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            Assert.assertEquals(1, entry.getPropertyChangeListeners().length);
        });
        // onMessage should cause listening to start if it hasn't already
        instance.onMessage(JsonRoster.ROSTER, data, JSON.GET, locale);
        JsonNode message = this.connection.getMessage();
        Assert.assertNotNull("Message was sent", message);
        Assert.assertEquals(Roster.getDefault().numEntries(), message.size());
        // assert we are listening
        Assert.assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            Assert.assertEquals(3, entry.getPropertyChangeListeners().length);
        });
    }

    /**
     * Test of onMessage method INVALID on a ROSTER
     *
     * @throws java.io.IOException            this is an error, not a failure,
     *                                        in the test
     * @throws jmri.JmriException             this is an error, not a failure,
     *                                        in the test
     * @throws jmri.server.json.JsonException this is an error, not a failure,
     *                                        in the test
     */
    @Test
    public void testOnMessageInvalidRoster() throws IOException, JmriException, JsonException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        instance.onMessage(JsonRoster.ROSTER, data, "Invalid", locale);
        JsonNode message = this.connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals(Roster.getDefault().numEntries(), message.size());
    }

    /**
     * Test of onList method, of class JsonRosterSocketService.
     *
     * @throws java.io.IOException            this is an error, not a failure,
     *                                        in the test
     * @throws jmri.JmriException             this is an error, not a failure,
     *                                        in the test
     * @throws jmri.server.json.JsonException this is an error, not a failure,
     *                                        in the test
     */
    @Test
    public void testOnList() throws IOException, JmriException, JsonException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        // assert we have not been listening
        Assert.assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            Assert.assertEquals(1, entry.getPropertyChangeListeners().length);
        });
        // onList should cause listening to start if it hasn't already
        instance.onList(JsonRoster.ROSTER, data, locale);
        JsonNode message = this.connection.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals(Roster.getDefault().numEntries(), message.size());
        // assert we are listening
        Assert.assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            Assert.assertEquals(3, entry.getPropertyChangeListeners().length);
        });
    }

    /**
     * Test of onClose method, of class JsonRosterSocketService.
     */
    @Test
    public void testOnClose() {
        JsonRosterSocketService instance = new JsonRosterSocketService(new JsonMockConnection((DataOutputStream) null));
        // listen to the roster, since onClose stops listening to the roster
        instance.listen();
        // assert we are listening
        Assert.assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            Assert.assertEquals(3, entry.getPropertyChangeListeners().length);
        });
        // the connection is closing, stop listening
        instance.onClose();
        // assert we are not listening
        Assert.assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            Assert.assertEquals(1, entry.getPropertyChangeListeners().length);
        });
    }

}
