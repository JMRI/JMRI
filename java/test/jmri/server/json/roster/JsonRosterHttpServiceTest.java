package jmri.server.json.roster;

import com.fasterxml.jackson.databind.JsonNode;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
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
public class JsonRosterHttpServiceTest extends JsonHttpServiceTestBase {

    private final static String TEST_GROUP1 = "testGroup1";
    private final static String TEST_ENTRY1 = "testEntry1";
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonRosterHttpServiceTest.class);

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        JUnitUtil.initConfigureManager();
        InstanceManager.setDefault(Roster.class, new Roster("java/test/jmri/server/json/roster/data/roster.xml"));
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
        JsonRosterHttpService instance = new JsonRosterHttpService(this.mapper);
        // call with valid first argument
        Assert.assertEquals(Roster.getDefault().numEntries(), instance.doGet(JsonRoster.ROSTER, "", instance.getObjectMapper().createObjectNode(), locale).size());
        Assert.assertEquals(2, instance.doGet(JsonRoster.ROSTER, "", instance.getObjectMapper().createObjectNode(), locale).size());
        // call with invalid first argument
        try {
            instance.doGet(TEST_GROUP1, TEST_GROUP1, instance.getObjectMapper().createObjectNode(), locale);
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
        JsonRosterHttpService instance = new JsonRosterHttpService(this.mapper);
        JsonException exception = null;
        try {
            instance.doPost(JsonRoster.ROSTER, "", this.mapper.createObjectNode(), locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, exception.getCode());
        // rewrite following to provide meaningful test
        try {
            instance.doPost(TEST_GROUP1, "", this.mapper.createObjectNode(), locale);
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
        JsonRosterHttpService instance = new JsonRosterHttpService(this.mapper);
        // call with valid first argument
        Assert.assertEquals(Roster.getDefault().numEntries(), instance.doGet(JsonRoster.ROSTER, "", instance.getObjectMapper().createObjectNode(), locale).size());
        // call with invalid first argument
        try {
            instance.doGet(TEST_GROUP1, TEST_GROUP1, instance.getObjectMapper().createObjectNode(), locale);
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
        JsonRosterHttpService instance = new JsonRosterHttpService(this.mapper);
        // no group name - check only size - it should contain all entries in Roster
        Assert.assertEquals(Roster.getDefault().numEntries(),
                (instance.getRoster(locale, this.mapper.createObjectNode())).size());
        // existant group name - check only size - it should contain all entries in named RosterGroup
        Assert.assertEquals(Roster.getDefault().numGroupEntries(TEST_GROUP1),
                (instance.getRoster(locale, this.mapper.createObjectNode().put(JSON.GROUP, TEST_GROUP1))).size());
        // non-existant group name - check only size - it should be empty
        Assert.assertEquals(0,
                (instance.getRoster(locale, this.mapper.createObjectNode().put(JSON.GROUP, TEST_ENTRY1))).size());
    }

    /**
     * Test of getRosterEntry method, of class JsonRosterHttpService.
     * 
     * @throws JsonException if unexpected exception occurs
     */
    @Test
    public void testGetRosterEntry_Locale_String() throws JsonException {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.mapper);
        // existent entry
        Assert.assertEquals(TEST_ENTRY1, instance.getRosterEntry(locale, TEST_ENTRY1).path(JSON.DATA).path(JSON.NAME).asText());
        // non-existent entry
        try {
            instance.getRosterEntry(locale, TEST_GROUP1);
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
        JsonRosterHttpService instance = new JsonRosterHttpService(this.mapper);
        Assert.assertEquals(TEST_ENTRY1, instance.getRosterEntry(locale, entry).path(JSON.DATA).path(JSON.NAME).asText());
    }

    /**
     * Test of getRosterGroups method, of class JsonRosterHttpService.
     * 
     * @throws JsonException if unexpected exception occurs
     */
    @Test
    public void testGetRosterGroups() throws JsonException {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.mapper);
        Assert.assertEquals(Roster.getDefault().getRosterGroups().size() + 1, instance.getRosterGroups(locale).size());
    }

    /**
     * Test of getRosterGroup method, of class JsonRosterHttpService.
     *
     * @throws JsonException if unexpected exception occurs
     */
    @Test
    public void testGetRosterGroup() throws JsonException {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.mapper);
        // test valid existing group
        JsonNode result = instance.getRosterGroup(locale, TEST_GROUP1);
        this.validate(result);
        Assert.assertEquals(Roster.getDefault().getEntriesInGroup(TEST_GROUP1).size(),
                result.path(JSON.DATA).path(JSON.LENGTH).asInt());
        // test non-existant group
        try {
            instance.getRosterGroup(locale, "non-existant-group");
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            this.validate(ex.getJsonMessage());
            Assert.assertEquals("Error code is HTTP \"not found\"", 404, ex.getCode());
            Assert.assertEquals("Error message is \"not found\"", "Object type rosterGroup named non-existant-group not found.", ex.getMessage());
        }
    }

}
