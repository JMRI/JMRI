package jmri.server.json.roster;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.roster.Roster;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Randall Wood
 */
public class JsonRosterSocketServiceTest extends TestCase {

    private final JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);

    public JsonRosterSocketServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(JsonRosterSocketServiceTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        InstanceManager.setDefault(Roster.class, new Roster("java/test/jmri/server/json/roster/data/roster.xml"));
        // clear the last message (if any) from the connection
        this.connection.sendMessage((JsonNode) null);
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
        InstanceManager.deregister(InstanceManager.getDefault(Roster.class), Roster.class);
    }

    /**
     * Test of listen method, of class JsonRosterSocketService.
     */
    public void testListen() {
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(1, entry.getPropertyChangeListeners().length);
        });
        // add the first time
        instance.listen();
        assertEquals(1, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(2, entry.getPropertyChangeListeners().length);
        });
        // don't add the second time
        instance.listen();
        assertEquals(1, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(2, entry.getPropertyChangeListeners().length);
        });
    }

    /**
     * Test of onMessage method DELETE on a ROSTER
     *
     * @throws java.io.IOException this is an error, not a failure, in the test
     * @throws jmri.JmriException  this is an error, not a failure, in the test
     */
    public void testOnMessageDeleteRoster() throws IOException, JmriException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode().put(JSON.METHOD, JSON.DELETE);
        Locale locale = Locale.ENGLISH;
        JsonException exception = null;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        try {
            instance.onMessage(JsonRoster.ROSTER, data, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, exception.getCode());
    }

    /**
     * Test of onMessage method POST on a ROSTER
     *
     * @throws java.io.IOException this is an error, not a failure, in the test
     * @throws jmri.JmriException  this is an error, not a failure, in the test
     */
    public void testOnMessagePostRoster() throws IOException, JmriException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode().put(JSON.METHOD, JSON.POST);
        Locale locale = Locale.ENGLISH;
        JsonException exception = null;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        try {
            instance.onMessage(JsonRoster.ROSTER, data, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, exception.getCode());
    }

    /**
     * Test of onMessage method PUT on a ROSTER
     *
     * @throws java.io.IOException this is an error, not a failure, in the test
     * @throws jmri.JmriException  this is an error, not a failure, in the test
     */
    public void testOnMessagePutRoster() throws IOException, JmriException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode().put(JSON.METHOD, JSON.PUT);
        Locale locale = Locale.ENGLISH;
        JsonException exception = null;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        try {
            instance.onMessage(JsonRoster.ROSTER, data, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, exception.getCode());
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
    public void testOnMessageGetRoster() throws IOException, JmriException, JsonException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode().put(JSON.METHOD, JSON.GET);
        Locale locale = Locale.ENGLISH;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        // assert we have not been listening
        assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(1, entry.getPropertyChangeListeners().length);
        });
        // onMessage should cause listening to start if it hasn't already
        instance.onMessage(JsonRoster.ROSTER, data, locale);
        assertEquals(Roster.getDefault().numEntries(), this.connection.getMessage().size());
        // assert we are listening
        assertEquals(1, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(2, entry.getPropertyChangeListeners().length);
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
    public void testOnMessageInvalidRoster() throws IOException, JmriException, JsonException {
        JsonNode data = this.connection.getObjectMapper().createObjectNode().put(JSON.METHOD, "Invalid");
        Locale locale = Locale.ENGLISH;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        instance.onMessage(JsonRoster.ROSTER, data, locale);
        assertNotNull(this.connection.getMessage());
        assertEquals(Roster.getDefault().numEntries(), this.connection.getMessage().size());
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
    public void testOnList() throws IOException, JmriException, JsonException {
        JsonNode data = null;
        Locale locale = Locale.ENGLISH;
        JsonRosterSocketService instance = new JsonRosterSocketService(this.connection);
        // assert we have not been listening
        assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(1, entry.getPropertyChangeListeners().length);
        });
        // onList should cause listening to start if it hasn't already
        instance.onList(JsonRoster.ROSTER, data, locale);
        assertNotNull(this.connection.getMessage());
        assertEquals(Roster.getDefault().numEntries(), this.connection.getMessage().size());
        // assert we are listening
        assertEquals(1, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(2, entry.getPropertyChangeListeners().length);
        });
    }

    /**
     * Test of onClose method, of class JsonRosterSocketService.
     */
    public void testOnClose() {
        JsonRosterSocketService instance = new JsonRosterSocketService(new JsonMockConnection((DataOutputStream) null));
        // listen to the roster, since onClose stops listening to the roster
        instance.listen();
        // assert we are listening
        assertEquals(1, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(2, entry.getPropertyChangeListeners().length);
        });
        // the connection is closing, stop listening
        instance.onClose();
        // assert we are not listening
        assertEquals(0, Roster.getDefault().getPropertyChangeListeners().length);
        Roster.getDefault().getEntriesInGroup(Roster.ALLENTRIES).stream().forEach((entry) -> {
            assertEquals(1, entry.getPropertyChangeListeners().length);
        });
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonRosterSocketServiceTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }


}
