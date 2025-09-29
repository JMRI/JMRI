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
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonRosterSocketServiceTest {

    private JsonMockConnection connection = null;
    private final Locale locale = Locale.ENGLISH;

    @BeforeEach
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

    @AfterEach
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
        assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(1, entry.getPropertyChangeListeners().length);
        });
        // add the first time
        instance.listen();
        assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(3, entry.getPropertyChangeListeners().length);
        });
        // don't add the second time
        instance.listen();
        assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(3, entry.getPropertyChangeListeners().length);
        });

        // wait for the initial Bean update notifications to drain from the queue. These generate
        // output messages that we then throw away.
        new org.netbeans.jemmy.QueueTool().waitEmpty();

        // list the groups in a JSON message for assertions
        this.connection.sendMessage(null, 0);
        instance.onMessage(JsonRoster.ROSTER_GROUPS, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = this.connection.getMessage();
        if (this.connection.getMessages().size() !=1) { // error, but first log why
            connection.getMessages().forEach((msg)->{
                log.warn(" message {}", msg);
            });
        }
        assertEquals( 1, this.connection.getMessages().size(), "Single message sent");
        assertNotNull( message, "Message was sent");
        assertTrue( message.isArray(), "Message is array");
        assertEquals( 2, message.size(), "Two groups exist");
        assertTrue( message.findValuesAsText(JSON.NAME).contains("testGroup1"), "Contains group TestGroup1");
        assertTrue( message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(locale)), "Contains group AllEntries");

        // add a roster group and verify message sent by listener
        this.connection.sendMessage(null, 0);
        Roster.getDefault().addRosterGroup("NewRosterGroup");
        assertEquals( 2, this.connection.getMessages().size(), "Two replies sent");
        message = this.connection.getMessage();
        assertNotNull( message, "Message was sent");
        assertEquals( 3, message.size(), "Three groups exist");
        assertTrue( message.findValuesAsText(JSON.NAME).contains("testGroup1"), "Contains group TestGroup1");
        assertTrue( message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(locale)), "Contains group AllEntries");
        assertTrue( message.findValuesAsText(JSON.NAME).contains("NewRosterGroup"), "Contains group NewRosterGroup");

        // rename a roster group and verify message sent by listener
        this.connection.sendMessage(null, 0);

        var newRosterGroup = Roster.getDefault().getRosterGroups().get("NewRosterGroup");
        Assertions.assertNotNull(newRosterGroup);

        newRosterGroup.setName("AgedRosterGroup");
        assertEquals( 1, this.connection.getMessages().size(), "Single message sent");
        message = this.connection.getMessage();
        assertNotNull( message, "Message was sent");
        assertEquals( 3, message.size(), "Three groups exist");
        assertTrue( message.findValuesAsText(JSON.NAME).contains("testGroup1"), "Contains group TestGroup1");
        assertTrue( message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(locale)), "Contains group AllEntries");
        assertTrue( message.findValuesAsText(JSON.NAME).contains("AgedRosterGroup"), "Contains group AgedRosterGroup");
        assertFalse( message.findValuesAsText(JSON.NAME).contains("NewRosterGroup"), "Contains group NewRosterGroup");

        // remove a roster group and verify message sent by listener
        this.connection.sendMessage(null, 0);
        Roster.getDefault().removeRosterGroup(Roster.getDefault().getRosterGroups().get("AgedRosterGroup"));
        assertEquals( 1, this.connection.getMessages().size(), "Single message sent");
        message = this.connection.getMessage();
        assertNotNull( message, "Message was sent");
        assertEquals( 2, message.size(), "Two groups exist");
        assertTrue( message.findValuesAsText(JSON.NAME).contains("testGroup1"), "Contains group TestGroup1");
        assertTrue( message.findValuesAsText(JSON.NAME).contains(Roster.allEntries(locale)), "Contains group AllEntries");
        assertFalse( message.findValuesAsText(JSON.NAME).contains("AgedRosterGroup"), "Contains group NewRosterGroup");
        assertFalse( message.findValuesAsText(JSON.NAME).contains("NewRosterGroup"), "Contains group NewRosterGroup");

        // Set unknown roster group directly as attribute of RosterEntry
        this.connection.sendMessage(null, 0);
        RosterEntry re = Roster.getDefault().getEntryForId("testEntry1");
        assertEquals( 3, re.getPropertyChangeListeners().length, "instance is listening to RosterEntry");
        re.putAttribute(Roster.ROSTER_GROUP_PREFIX + "attribute", "yes");
                JUnitUtil.waitFor(() -> {
            return this.connection.getMessages().size() >= 1;
        }, "Expected message not sent");
        assertEquals( 1, this.connection.getMessages().size(), "One message sent");
        message = connection.getMessage();
        assertNotNull( message, "Message is not null");
        assertEquals( JsonRoster.ROSTER_ENTRY, message.path(JSON.TYPE).asText(),
            "Message contains rosterEntry");

        // Set known roster group directly as attribute of RosterEntry
        Roster.getDefault().addRosterGroup("NewRosterGroup");
        JUnitUtil.waitFor(() -> {
            return Roster.getDefault().getRosterGroupList().contains("NewRosterGroup");
        }, "Roster Group was not added");
        this.connection.sendMessage(null, 0); // clear out messages
        re.putAttribute(Roster.ROSTER_GROUP_PREFIX + "NewRosterGroup", "yes"); // add new group to roster entry
        // wait for all expected messages to be sent before testing messages are as expected
        JUnitUtil.waitFor(() -> {
            return this.connection.getMessages().size() >= 3;
        }, "Three expected messages not sent");
        // Sent updated rosterEntry, rosterGroup, array of rosterGroup
        ArrayNode messages = this.connection.getMessages();
        assertEquals( 3, messages.size(), "3 messages sent");
        // Check that 5 top-level types are in the 3 messages
        List<String> values = messages.findValuesAsText("type");
        values.sort(null); // sort because message order is non-deterministic
        assertArrayEquals( new String[]{JsonRoster.ROSTER_ENTRY, JsonRoster.ROSTER_GROUP,
                JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP},
            values.toArray(new String[5]),
            "Objects are 1 rosterEntry and 4 rosterGroup");

        // Remove known roster group directly as attribute of RosterEntry
        this.connection.sendMessage(null, 0); // clear out messages
        re.deleteAttribute(Roster.ROSTER_GROUP_PREFIX + "NewRosterGroup"); // remove group from roster entry
        // wait for all expected messages to be sent before testing messages are as expected
        JUnitUtil.waitFor(() -> {
            return this.connection.getMessages().size() >= 3;
        }, "Three expected messages not sent");
        // Sent updated rosterEntry, rosterGroup, array of rosterGroup
        messages = this.connection.getMessages();
        assertEquals( 3, messages.size(), "3 messages sent");
        // Check that 5 top-level types are in the 3 messages
        values = messages.findValuesAsText("type");
        values.sort(null); // sort because message order is non-deterministic
        assertArrayEquals( new String[]{JsonRoster.ROSTER_ENTRY, JsonRoster.ROSTER_GROUP,
                JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUP},
            values.toArray(new String[5]),
            "Objects are 1 rosterEntry and 4 rosterGroup");
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
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JsonRoster.ROSTER, data,
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getCode());
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
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JsonRoster.ROSTER, data,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, ex.getCode());
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
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JsonRoster.ROSTER, data,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, ex.getCode());
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
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        // assert we have not been listening
        assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(1, entry.getPropertyChangeListeners().length);
        });
        // onMessage should cause listening to start if it hasn't already
        instance.onMessage(JsonRoster.ROSTER, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = this.connection.getMessage();
        assertNotNull( message, "Message was sent");
        assertEquals(Roster.getDefault().numEntries(), message.size());
        // assert we are listening
        assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(3, entry.getPropertyChangeListeners().length);
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
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        JsonException ex = assertThrows( JsonException.class, () ->
            instance.onMessage(JsonRoster.ROSTER, data,
                new JsonRequest(locale, JSON.V5, "Invalid", 42)),
            "Expected exception not thrown");
        assertEquals( 405, ex.getCode(), "Exception is coded for HTTP invalid method");
        assertEquals( "Method Invalid is not known and not allowed.",
            ex.getLocalizedMessage(), "Exception message for HTTP invalid method");
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
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        // assert we have not been listening
        assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(1, entry.getPropertyChangeListeners().length);
        });
        // onList should cause listening to start if it hasn't already
        instance.onList(JsonRoster.ROSTER, data, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = this.connection.getMessage();
        assertNotNull(message);
        assertEquals(Roster.getDefault().numEntries(), message.size());
        // assert we are listening
        assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(3, entry.getPropertyChangeListeners().length);
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
        assertEquals(2, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(3, entry.getPropertyChangeListeners().length);
        });
        // the connection is closing, stop listening
        instance.onClose();
        // assert we are not listening
        assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(1, entry.getPropertyChangeListeners().length);
        });
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonRosterSocketServiceTest.class);
}
