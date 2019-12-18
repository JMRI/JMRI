package jmri.server.json.roster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.profile.ProfileManager;
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
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonRosterSocketServiceTest {

    private JsonMockConnection connection = null;
    private Locale locale = Locale.ENGLISH;

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();

        connection = new JsonMockConnection((DataOutputStream) null);

        JUnitUtil.initRosterConfigManager();
        InstanceManager.getDefault(RosterConfigManager.class).setRoster(ProfileManager.getDefault().getActiveProfile(),
                new Roster("java/test/jmri/server/json/roster/data/roster.xml"));
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

        // wait for the initial Bean update notifications to drain from the queue. These generate
        // output messages that we then throw away.
        new org.netbeans.jemmy.QueueTool().waitEmpty();

        // list the groups in a JSON message for assertions
        this.connection.sendMessage((JsonNode) null, 0);
        instance.onMessage(JsonRoster.ROSTER_GROUPS, NullNode.getInstance(), JSON.GET, locale, 0);
        JsonNode message = this.connection.getMessage();
        Assert.assertEquals("Single message sent", 1, this.connection.getMessages().size());
        Assert.assertNotNull("Message was sent", message);
        Assert.assertTrue("Message is array", message.isArray());
        Assert.assertEquals("Two groups exist", 2, message.size());
        Assert.assertTrue("Contains group TestGroup1", message.findValuesAsText(JSON.NAME).contains("testGroup1"));
        Assert.assertTrue("Contains group AllEntries", message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(locale)));

        // add a roster group and verify message sent by listener
        this.connection.sendMessage((JsonNode) null, 0);
        Roster.getDefault().addRosterGroup("NewRosterGroup");
        Assert.assertEquals("Single message sent", 1, this.connection.getMessages().size());
        message = this.connection.getMessage();
        Assert.assertNotNull("Message was sent", message);
        Assert.assertEquals("Three groups exist", 3, message.size());
        Assert.assertTrue("Contains group TestGroup1", message.findValuesAsText(JSON.NAME).contains("testGroup1"));
        Assert.assertTrue("Contains group AllEntries", message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(locale)));
        Assert.assertTrue("Contains group NewRosterGroup", message.findValuesAsText(JSON.NAME).contains("NewRosterGroup"));

        // rename a roster group and verify message sent by listener
        this.connection.sendMessage((JsonNode) null, 0);
        Roster.getDefault().getRosterGroups().get("NewRosterGroup").setName("AgedRosterGroup");
        Assert.assertEquals("Single message sent", 1, this.connection.getMessages().size());
        message = this.connection.getMessage();
        Assert.assertNotNull("Message was sent", message);
        Assert.assertEquals("Three groups exist", 3, message.size());
        Assert.assertTrue("Contains group TestGroup1", message.findValuesAsText(JSON.NAME).contains("testGroup1"));
        Assert.assertTrue("Contains group AllEntries", message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(locale)));
        Assert.assertTrue("Contains group AgedRosterGroup", message.findValuesAsText(JSON.NAME).contains("AgedRosterGroup"));
        Assert.assertFalse("Contains group NewRosterGroup", message.findValuesAsText(JSON.NAME).contains("NewRosterGroup"));

        // remove a roster group and verify message sent by listener
        this.connection.sendMessage((JsonNode) null, 0);
        Roster.getDefault().removeRosterGroup(Roster.getDefault().getRosterGroups().get("AgedRosterGroup"));
        Assert.assertEquals("Single message sent", 1, this.connection.getMessages().size());
        message = this.connection.getMessage();
        Assert.assertNotNull("Message was sent", message);
        Assert.assertEquals("Two groups exist", 2, message.size());
        Assert.assertTrue("Contains group TestGroup1", message.findValuesAsText(JSON.NAME).contains("testGroup1"));
        Assert.assertTrue("Contains group AllEntries", message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(locale)));
        Assert.assertFalse("Contains group NewRosterGroup", message.findValuesAsText(JSON.NAME).contains("AgedRosterGroup"));
        Assert.assertFalse("Contains group NewRosterGroup", message.findValuesAsText(JSON.NAME).contains("NewRosterGroup"));

        // Set unknown roster group directly as attribute of RosterEntry
        this.connection.sendMessage((JsonNode) null, 0);
        RosterEntry re = Roster.getDefault().getEntryForId("testEntry1");
        Assert.assertEquals("instance is listening to RosterEntry", 3, re.getPropertyChangeListeners().length);
        re.putAttribute(Roster.ROSTER_GROUP_PREFIX + "attribute", "yes");
                JUnitUtil.waitFor(() -> {
            return this.connection.getMessages().size() >= 1;
        }, "Expected message not sent");
        Assert.assertEquals("One message sent", 1, this.connection.getMessages().size());
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Message contains rosterEntry", JsonRoster.ROSTER_ENTRY, message.path(JSON.TYPE).asText());

        // Set known roster group directly as attribute of RosterEntry
        Roster.getDefault().addRosterGroup("NewRosterGroup");
        JUnitUtil.waitFor(() -> {
            return Roster.getDefault().getRosterGroupList().contains("NewRosterGroup");
        }, "Roster Group was not added");
        this.connection.sendMessage((JsonNode) null, 0); // clear out messages
        re.putAttribute(Roster.ROSTER_GROUP_PREFIX + "NewRosterGroup", "yes"); // add new group to roster entry
        // wait for all expected messages to be sent before testing messages are as expected
        JUnitUtil.waitFor(() -> {
            return this.connection.getMessages().size() >= 3;
        }, "Three expected messages not sent");
        // Sent updated rosterEntry, rosterGroup, array of rosterGroup
        ArrayNode messages = this.connection.getMessages();
        Assert.assertEquals("3 messages sent", 3, messages.size());
        // Check that 5 top-level types are in the 3 messages
        List<String> values = messages.findValuesAsText("type");
        values.sort(null); // sort because message order is non-deterministic
        Assert.assertArrayEquals("Objects are 1 rosterEntry and 4 rosterGroup",
                new String[]{JsonRoster.ROSTER_ENTRY, JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP},
                values.toArray(new String[5]));

        // Remove known roster group directly as attribute of RosterEntry
        this.connection.sendMessage((JsonNode) null, 0); // clear out messages
        re.deleteAttribute(Roster.ROSTER_GROUP_PREFIX + "NewRosterGroup"); // remove group from roster entry
        // wait for all expected messages to be sent before testing messages are as expected
        JUnitUtil.waitFor(() -> {
            return this.connection.getMessages().size() >= 3;
        }, "Three expected messages not sent");
        // Sent updated rosterEntry, rosterGroup, array of rosterGroup
        messages = this.connection.getMessages();
        Assert.assertEquals("3 messages sent", 3, messages.size());
        // Check that 5 top-level types are in the 3 messages
        values = messages.findValuesAsText("type");
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
            instance.onMessage(JsonRoster.ROSTER, data, JSON.DELETE, locale, 42);
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
            instance.onMessage(JsonRoster.ROSTER, data, JSON.POST, locale, 42);
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
            instance.onMessage(JsonRoster.ROSTER, data, JSON.POST, locale, 42);
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
        instance.onMessage(JsonRoster.ROSTER, data, JSON.GET, locale, 0);
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
        try {
            instance.onMessage(JsonRoster.ROSTER, data, "Invalid", locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("Exception is coded for HTTP invalid method", 405, ex.getCode());
            Assert.assertEquals("Exception message for HTTP invalid method", "Method Invalid is not known and not allowed.", ex.getLocalizedMessage());
        }
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
        instance.onList(JsonRoster.ROSTER, data, locale, 0);
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
