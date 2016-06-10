package jmri.server.json.roster;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Randall Wood
 */
public class JsonRosterHttpServiceTest extends TestCase {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final static String testGroup1 = "testGroup1";
    private final static String testEntry1 = "testEntry1";
    private final Locale locale = Locale.ENGLISH;
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonRosterHttpServiceTest.class);

    public JsonRosterHttpServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(JsonRosterHttpServiceTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        InstanceManager.setDefault(Roster.class, new Roster("java/test/jmri/server/json/roster/data/roster.xml"));
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
        InstanceManager.deregister(InstanceManager.getDefault(Roster.class), Roster.class);
    }

    /**
     * Tests only that this does not throw an error with a valid call, and
     * throws an error with an invalid call, but does not test the full range of
     * possible valid calls, since this method is merely a switch on it's first
     * argument.
     *
     * @throws java.lang.Exception
     */
    public void testDoGet() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        // call with valid first argument
        assertEquals(Roster.getDefault().numEntries(), instance.doGet(JsonRoster.ROSTER, null, locale).size());
        assertEquals(2, instance.doGet(JsonRoster.ROSTER, null, locale).size());
        // call with invalid first argument
        JsonException exception = null;
        try {
            instance.doGet(testGroup1, testGroup1, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        // false positive warning from Eclipse on following line, please leave in place until we update ecj.jar GitHub #1417
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getCode());
    }

    /**
     * Test of doPost method, of class JsonRosterHttpService.
     *
     * @throws java.lang.Exception
     */
    public void testDoPost() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        JsonException exception = null;
        try {
            instance.doPost(JsonRoster.ROSTER, null, null, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, exception.getCode());
        try {
            instance.doPost(testGroup1, null, null, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        // false positive warning from Eclipse on following line, please leave in place until we update ecj.jar GitHub #1417
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getCode());
    }

    /**
     * Tests only that this does not throw an error with a valid call, and
     * throws an error with an invalid call, but does not test the full range of
     * possible valid calls, since this method is merely a switch on it's first
     * argument.
     *
     * @throws java.lang.Exception
     */
    public void testDoGetList() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        // call with valid first argument
        assertEquals(Roster.getDefault().numEntries(), instance.doGet(JsonRoster.ROSTER, null, locale).size());
        // call with invalid first argument
        JsonException exception = null;
        try {
            instance.doGet(testGroup1, testGroup1, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        // false positive warning from Eclipse on following line, please leave in place until we update ecj.jar GitHub #1417
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getCode());
    }

    /**
     * Test of getRoster method, of class JsonRosterHttpService.
     */
    public void testGetRoster() {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        // no group name - check only size - it should contain all entries in Roster
        assertEquals(Roster.getDefault().numEntries(),
                (instance.getRoster(locale, this.objectMapper.createObjectNode())).size());
        // existant group name - check only size - it should contain all entries in named RosterGroup
        assertEquals(Roster.getDefault().numGroupEntries(testGroup1),
                (instance.getRoster(locale, this.objectMapper.createObjectNode().put(JSON.GROUP, testGroup1))).size());
        // non-existant group name - check only size - it should be empty
        assertEquals(0,
                (instance.getRoster(locale, this.objectMapper.createObjectNode().put(JSON.GROUP, testEntry1))).size());
    }

    /**
     * Test of getRosterEntry method, of class JsonRosterHttpService.
     *
     * @throws java.lang.Exception
     */
    public void testGetRosterEntry_Locale_String() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        // existant entry
        assertEquals(testEntry1, instance.getRosterEntry(locale, testEntry1).path(JSON.DATA).path(JSON.NAME).asText());
        // non-existant entry
        JsonException exception = null;
        try {
            instance.getRosterEntry(locale, testGroup1);
        } catch (JsonException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        // false positive warning from Eclipse on following line, please leave in place until we update ecj.jar GitHub #1417
        assertEquals(HttpServletResponse.SC_NOT_FOUND, exception.getCode());
    }

    /**
     * Test of getRosterEntry method, of class JsonRosterHttpService.
     */
    public void testGetRosterEntry_Locale_RosterEntry() {
        RosterEntry entry = Roster.getDefault().getEntryForId(testEntry1);
        assertNotNull(entry);
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        assertEquals(testEntry1, instance.getRosterEntry(locale, entry).path(JSON.DATA).path(JSON.NAME).asText());
    }

    /**
     * Test of getRosterGroups method, of class JsonRosterHttpService.
     *
     * @throws java.lang.Exception
     */
    public void testGetRosterGroups() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        assertEquals(Roster.getDefault().getRosterGroups().size() + 1, instance.getRosterGroups(locale).size());
    }

    /**
     * Test of getRosterGroup method, of class JsonRosterHttpService.
     *
     * @throws java.lang.Exception
     */
    public void testGetRosterGroup() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        assertEquals(Roster.getDefault().getEntriesInGroup(testGroup1).size(),
                instance.getRosterGroup(locale, testGroup1).path(JSON.DATA).path(JSON.LENGTH).asInt());
    }


        // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading",JsonRosterHttpServiceTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }


}
