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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonRosterHttpServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final static String TEST_GROUP1 = "testGroup1";
    private final static String TEST_ENTRY1 = "testEntry1";
    private final Locale locale = Locale.ENGLISH;
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonRosterHttpServiceTest.class);

    @Before
    public void setUp() throws Exception {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        InstanceManager.setDefault(Roster.class, new Roster("java/test/jmri/server/json/roster/data/roster.xml"));
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }

    /**
     * Tests only that this does not throw an error with a valid call, and
     * throws an error with an invalid call, but does not test the full range of
     * possible valid calls, since this method is merely a switch on it's first
     * argument.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testDoGet() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        // call with valid first argument
        Assert.assertEquals(Roster.getDefault().numEntries(), instance.doGet(JsonRoster.ROSTER, null, locale).size());
        Assert.assertEquals(2, instance.doGet(JsonRoster.ROSTER, null, locale).size());
        // call with invalid first argument
        JsonException exception = null;
        try {
            instance.doGet(TEST_GROUP1, TEST_GROUP1, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getCode());
    }

    /**
     * Test of doPost method, of class JsonRosterHttpService.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testDoPost() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        JsonException exception = null;
        try {
            instance.doPost(JsonRoster.ROSTER, null, null, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, exception.getCode());
        try {
            instance.doPost(TEST_GROUP1, null, null, locale);
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
     * @throws java.lang.Exception
     */
    @Test
    public void testDoGetList() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        // call with valid first argument
        Assert.assertEquals(Roster.getDefault().numEntries(), instance.doGet(JsonRoster.ROSTER, null, locale).size());
        // call with invalid first argument
        JsonException exception = null;
        try {
            instance.doGet(TEST_GROUP1, TEST_GROUP1, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getCode());
    }

    /**
     * Test of getRoster method, of class JsonRosterHttpService.
     */
    @Test
    public void testGetRoster() {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        // no group name - check only size - it should contain all entries in Roster
        Assert.assertEquals(Roster.getDefault().numEntries(),
                (instance.getRoster(locale, this.objectMapper.createObjectNode())).size());
        // existant group name - check only size - it should contain all entries in named RosterGroup
        Assert.assertEquals(Roster.getDefault().numGroupEntries(TEST_GROUP1),
                (instance.getRoster(locale, this.objectMapper.createObjectNode().put(JSON.GROUP, TEST_GROUP1))).size());
        // non-existant group name - check only size - it should be empty
        Assert.assertEquals(0,
                (instance.getRoster(locale, this.objectMapper.createObjectNode().put(JSON.GROUP, TEST_ENTRY1))).size());
    }

    /**
     * Test of getRosterEntry method, of class JsonRosterHttpService.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetRosterEntry_Locale_String() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        // existant entry
        Assert.assertEquals(TEST_ENTRY1, instance.getRosterEntry(locale, TEST_ENTRY1).path(JSON.DATA).path(JSON.NAME).asText());
        // non-existant entry
        JsonException exception = null;
        try {
            instance.getRosterEntry(locale, TEST_GROUP1);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, exception.getCode());
    }

    /**
     * Test of getRosterEntry method, of class JsonRosterHttpService.
     */
    @Test
    public void testGetRosterEntry_Locale_RosterEntry() {
        RosterEntry entry = Roster.getDefault().getEntryForId(TEST_ENTRY1);
        Assert.assertNotNull(entry);
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        Assert.assertEquals(TEST_ENTRY1, instance.getRosterEntry(locale, entry).path(JSON.DATA).path(JSON.NAME).asText());
    }

    /**
     * Test of getRosterGroups method, of class JsonRosterHttpService.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetRosterGroups() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        Assert.assertEquals(Roster.getDefault().getRosterGroups().size() + 1, instance.getRosterGroups(locale).size());
    }

    /**
     * Test of getRosterGroup method, of class JsonRosterHttpService.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetRosterGroup() throws Exception {
        JsonRosterHttpService instance = new JsonRosterHttpService(this.objectMapper);
        Assert.assertEquals(Roster.getDefault().getEntriesInGroup(TEST_GROUP1).size(),
                instance.getRosterGroup(locale, TEST_GROUP1).path(JSON.DATA).path(JSON.LENGTH).asInt());
    }

}
