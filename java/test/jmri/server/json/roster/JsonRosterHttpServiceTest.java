package jmri.server.json.roster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.profile.ProfileManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonRosterHttpServiceTest extends JsonHttpServiceTestBase<JsonRosterHttpService> {

    private final static String TEST_GROUP1 = "testGroup1";
    private final static String TEST_ENTRY1 = "testEntry1";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonRosterHttpService(mapper);
        JUnitUtil.initConfigureManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.getDefault(RosterConfigManager.class).setRoster(ProfileManager.getDefault().getActiveProfile(),
                new Roster("java/test/jmri/server/json/roster/data/roster.xml"));
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests only that this does not throw an error with a valid call, and
     * throws an error with an invalid call, but does not test the full range of
     * possible valid calls, since this method is merely a switch on it's first
     * argument.
     * 
     * @throws JsonException if unexpected exception occurs
     */
    @Test
    public void testDoGet() throws JsonException {
        // call with valid first argument
        Assert.assertEquals(Roster.getDefault().numEntries(), service.doGet(JsonRoster.ROSTER, "", NullNode.getInstance(), locale, 0).size());
        Assert.assertEquals(2, service.doGet(JsonRoster.ROSTER, "", NullNode.getInstance(), locale, 0).size());
        // call with invalid first argument
        try {
            service.doGet(TEST_GROUP1, TEST_GROUP1, NullNode.getInstance(), locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getCode());
        }
    }

    /**
     * Test of doPost method, of class JsonRosterHttpService.
     */
    @Test
    public void testDoPost() {
        JsonException exception = null;
        try {
            service.doPost(JsonRoster.ROSTER, "", this.mapper.createObjectNode(), locale, 42);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, exception.getCode());
        // rewrite following to provide meaningful test
        try {
            service.doPost(TEST_GROUP1, "", this.mapper.createObjectNode(), locale, 42);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getCode());
    }

    /**
     * Tests only that this does not throw an error with a valid call, and
     * throws an error with an invalid call, but does not test the full range of
     * possible valid calls, since this method is merely a switch on it's first
     * argument.
     * 
     * @throws JsonException if unexpected exception occurs
     */
    @Test
    public void testDoGetList() throws JsonException {
        // call with valid first argument
        Assert.assertEquals(Roster.getDefault().numEntries(), service.doGet(JsonRoster.ROSTER, "", NullNode.getInstance(), locale, 0).size());
        // call with invalid first argument
        try {
            service.doGet(TEST_GROUP1, TEST_GROUP1, NullNode.getInstance(), locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getCode());
        }
    }

    /**
     * Test of getRoster method, of class JsonRosterHttpService.
     *
     * @throws jmri.server.json.JsonException if unable to URL-encode roster
     *                                        entry Ids
     */
    @Test
    public void testGetRoster() throws JsonException {
        // no group name - check only size - it should contain all entries in Roster
        Assert.assertEquals(Roster.getDefault().numEntries(),
                service.getRoster(locale, this.mapper.createObjectNode(), 0).size());
        // existent group name - check only size - it should contain all entries in named RosterGroup
        Assert.assertEquals(Roster.getDefault().numGroupEntries(TEST_GROUP1),
                service.getRoster(locale, this.mapper.createObjectNode().put(JSON.GROUP, TEST_GROUP1), 0).size());
        // non-existent group name - check only size - it should be empty
        Assert.assertEquals(0,
                service.getRoster(locale, this.mapper.createObjectNode().put(JSON.GROUP, TEST_ENTRY1), 0).size());
    }

    /**
     * Test of getRosterEntry method, of class JsonRosterHttpService.
     * 
     * @throws JsonException if unexpected exception occurs
     */
    @Test
    public void testGetRosterEntry_Locale_String() throws JsonException {
        // existent entry
        Assert.assertEquals(TEST_ENTRY1, service.getRosterEntry(locale, TEST_ENTRY1, 42).path(JSON.DATA).path(JSON.NAME).asText());
        // non-existent entry
        try {
            service.getRosterEntry(locale, TEST_GROUP1, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());
        }
    }

    /**
     * Test of getRosterEntry method, of class JsonRosterHttpService.
     *
     * @throws jmri.server.json.JsonException if unable to URL-encode roster
     *                                        entry Id
     */
    @Test
    public void testGetRosterEntry_Locale_RosterEntry() throws JsonException {
        RosterEntry entry = Roster.getDefault().getEntryForId(TEST_ENTRY1);
        Assert.assertNotNull(entry);
        Assert.assertEquals(TEST_ENTRY1, service.getRosterEntry(locale, entry, 42).path(JSON.DATA).path(JSON.NAME).asText());
    }

    /**
     * Test of getRosterGroups method, of class JsonRosterHttpService.
     * 
     * @throws JsonException if unexpected exception occurs
     */
    @Test
    public void testGetRosterGroups() throws JsonException {
        Assert.assertEquals(Roster.getDefault().getRosterGroups().size() + 1, service.getRosterGroups(locale, 0).size());
    }

    /**
     * Test of getRosterGroup method, of class JsonRosterHttpService.
     *
     * @throws JsonException if unexpected exception occurs
     */
    @Test
    public void testGetRosterGroup() throws JsonException {
        // test valid existing group
        JsonNode result = service.getRosterGroup(locale, TEST_GROUP1, 42);
        this.validate(result);
        Assert.assertEquals(Roster.getDefault().getEntriesInGroup(TEST_GROUP1).size(),
                result.path(JSON.DATA).path(JSON.LENGTH).asInt());
        // test non-existent group
        try {
            service.getRosterGroup(locale, "non-existant-group", 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            this.validate(ex.getJsonMessage());
            Assert.assertEquals("Error code is HTTP \"not found\"", 404, ex.getCode());
            Assert.assertEquals("Error message is \"not found\"", "Object type rosterGroup named \"non-existant-group\" not found.", ex.getMessage());
        }
    }

}
