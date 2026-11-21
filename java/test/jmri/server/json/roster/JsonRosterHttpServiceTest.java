package jmri.server.json.roster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.profile.ProfileManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonRosterHttpServiceTest extends JsonHttpServiceTestBase<JsonRosterHttpService> {

    private static final String TEST_GROUP1 = "testGroup1";
    private static final String TEST_ENTRY1 = "testEntry1";

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonRosterHttpService(mapper);
        JUnitUtil.initConfigureManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.getDefault(RosterConfigManager.class).setRoster(ProfileManager.getDefault().getActiveProfile(),
                new Roster("java/test/jmri/server/json/roster/data/roster.xml"));
    }

    @AfterEach
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
        assertEquals(Roster.getDefault().numEntries(), service.doGet(JsonRoster.ROSTER, "", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0)).size());
        assertEquals(2, service.doGet(JsonRoster.ROSTER, "", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0)).size());
        // call with invalid first argument
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(TEST_GROUP1, TEST_GROUP1, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getCode());
    }

    /**
     * Test of doPost method, of class JsonRosterHttpService.
     */
    @Test
    public void testDoPost() {
        JsonException exception = assertThrows( JsonException.class, () ->
            service.doPost(JsonRoster.ROSTER, "", this.mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, exception.getCode());

        // rewrite following to provide meaningful test
        exception = assertThrows( JsonException.class, () ->
            service.doPost(TEST_GROUP1, "", this.mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertNotNull(exception);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getCode());
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
        assertEquals(Roster.getDefault().numEntries(),
            service.doGet(JsonRoster.ROSTER, "", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)).size());
        // call with invalid first argument
        JsonException ex = assertThrows( JsonException.class, () ->
            service.doGet(TEST_GROUP1, TEST_GROUP1, NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getCode());
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
        assertEquals(Roster.getDefault().numEntries(),
                service.getRoster(locale, this.mapper.createObjectNode(), 0).size());
        validate(JsonRoster.ROSTER_ENTRY, service.getRoster(locale, this.mapper.createObjectNode(), 0));
        // existent group name - check only size - it should contain all entries in named RosterGroup
        assertEquals(Roster.getDefault().numGroupEntries(TEST_GROUP1),
                service.getRoster(locale, this.mapper.createObjectNode().put(JSON.GROUP, TEST_GROUP1), 0).size());
        // non-existent group name - check only size - it should be empty
        assertEquals(0,
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
        JsonNode result = service.getRosterEntry(locale, TEST_ENTRY1, 42);
        assertEquals(TEST_ENTRY1, result.path(JSON.DATA).path(JSON.NAME).asText());
        validate(JsonRoster.ROSTER_ENTRY, result);
        // non-existent entry
        JsonException ex = assertThrows( JsonException.class, () ->
            service.getRosterEntry(locale, TEST_GROUP1, 42),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());
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
        assertNotNull(entry);
        JsonNode result = service.getRosterEntry(locale, entry, 42);
        assertEquals(TEST_ENTRY1, result.path(JSON.DATA).path(JSON.NAME).asText());
        validate(JsonRoster.ROSTER_ENTRY, result);
    }

    @Test
    public void testDoSchema() throws JsonException {
        testDoSchema(JsonRoster.ROSTER);
        testDoSchema(JsonRoster.ROSTER_ENTRY);
        testDoSchema(JsonRoster.ROSTER_GROUP, JsonRoster.ROSTER_GROUPS);
    }

    private void assertInvalid(String type, ObjectNode data, boolean server, String message) {
        JsonRequest request = new JsonRequest(locale, JSON.V5, JSON.GET, 42);
        JsonSchemaServiceCache schemas = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        assertThrows(JsonException.class, () -> schemas.validateData(type, data, server, request), message);
        // consume the warnings logged for the rejected message
        assertNotNull(JUnitAppender.checkForMessageStartingWith("Errors validating"), message);
        while (JUnitAppender.checkForMessageStartingWith("JSON Validation Error") != null) {
            // drain
        }
    }

    // build a rosterEntry that has every optional field populated
    private RosterEntry populatedEntry() {
        RosterEntry entry = Roster.getDefault().getEntryForId(TEST_ENTRY1);
        assertNotNull(entry);
        entry.setRoadName("Test Road");
        entry.setRoadNumber("1234");
        entry.setMfg("Test Mfg");
        entry.setModel("Test Model");
        entry.setOwner("Test Owner");
        entry.setComment("Test comment");
        entry.setShuntingFunction("F3");
        entry.setFunctionLabel(0, "Headlight");
        entry.setFunctionLockable(0, true);
        entry.putAttribute("testKey", "testValue");
        entry.putAttribute("nullKey", null);
        return entry;
    }

    @Test
    public void testServerRosterEntryPopulated() throws JsonException {
        RosterEntry entry = populatedEntry();
        JsonNode result = service.getRosterEntry(locale, entry, 42);
        validate(JsonRoster.ROSTER_ENTRY, result);
        // nullable values are sent as null and must still validate
        entry.setShuntingFunction(null);
        entry.setFunctionLabel(0, null);
        validate(JsonRoster.ROSTER_ENTRY, service.getRosterEntry(locale, entry, 42));
    }

    @Test
    public void testServerRosterEntryRejectsInvalid() throws JsonException {
        ObjectNode data = service.getRosterEntry(locale, TEST_ENTRY1, 42).path(JSON.DATA).deepCopy();
        validateData(JsonRoster.ROSTER_ENTRY, data, true); // baseline is valid
        ObjectNode missing = data.deepCopy();
        missing.remove(JSON.SHUNTING_FUNCTION);
        assertInvalid(JsonRoster.ROSTER_ENTRY, missing, true, "missing shuntingFunction");
        ObjectNode badKey = data.deepCopy();
        ((ObjectNode) badKey.path(JSON.FUNCTION_KEYS).get(0)).put(JSON.NAME, "X0");
        assertInvalid(JsonRoster.ROSTER_ENTRY, badKey, true, "bad function key name");
        ObjectNode badAttribute = data.deepCopy();
        ((ArrayNode) badAttribute.path(JsonRoster.ATTRIBUTES)).addObject().put(JSON.NAME, "noValue");
        assertInvalid(JsonRoster.ROSTER_ENTRY, badAttribute, true, "attribute without value");
    }

    @Test
    public void testClientRosterFilters() {
        ObjectNode data = mapper.createObjectNode();
        validateData(JsonRoster.ROSTER, data, false); // no filters is valid
        data.put(JSON.GROUP, TEST_GROUP1).put(JSON.ROAD, "Test Road").put(JSON.NUMBER, "1234")
                .put(JSON.ADDRESS, "3").put(JSON.MFG, "Test Mfg").put(JSON.DECODER_MODEL, "model")
                .put(JSON.DECODER_FAMILY, "family").put(JSON.NAME, TEST_ENTRY1);
        validateData(JsonRoster.ROSTER, data, false);
        assertInvalid(JsonRoster.ROSTER, mapper.createObjectNode().put("unknown", "filter"), false, "unknown filter");
        assertInvalid(JsonRoster.ROSTER, mapper.createObjectNode().put(JSON.ADDRESS, 3), false, "non-string filter");
    }

    @Test
    public void testClientRosterEntry() {
        // reading only needs the name
        validateData(JsonRoster.ROSTER_ENTRY, mapper.createObjectNode().put(JSON.NAME, TEST_ENTRY1), false);
        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, TEST_ENTRY1)
                .put(JSON.ADDRESS, "1234").put(JSON.ROAD, "Test Road").put(JSON.NUMBER, "1234")
                .put(JSON.MFG, "Test Mfg").put(JSON.MODEL, "Test Model").put(JSON.MAX_SPD_PCT, 75)
                .put(JSON.SHUNTING_FUNCTION, "F3").put(JSON.OWNER, "Test Owner").put(JSON.COMMENT, "Test comment");
        data.putArray(JSON.FUNCTION_KEYS).addObject().put(JSON.NAME, "F0").put(JSON.LABEL, "Headlight")
                .put(JSON.LOCKABLE, true);
        data.putArray(JsonRoster.ATTRIBUTES).addObject().put(JSON.NAME, "testKey").put(JSON.VALUE, "testValue");
        validateData(JsonRoster.ROSTER_ENTRY, data, false);
        // nulls are accepted where the server accepts them
        data.putNull(JSON.SHUNTING_FUNCTION).putNull(JSON.OWNER).putNull(JSON.COMMENT);
        validateData(JsonRoster.ROSTER_ENTRY, data, false);
    }

    @Test
    public void testPostRosterEntry() throws JsonException {
        RosterEntry spy = spyEntry();

        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, TEST_ENTRY1).put(JSON.ADDRESS, "1234")
                .put(JSON.MAX_SPD_PCT, 75).put(JSON.COMMENT, "Test comment");
        data.putArray(JSON.FUNCTION_KEYS).addObject().put(JSON.NAME, "F0").put(JSON.LABEL, "Headlight")
                .put(JSON.LOCKABLE, true);
        data.putArray(JsonRoster.ATTRIBUTES).addObject().put(JSON.NAME, "testKey").put(JSON.VALUE, "testValue");
        validateData(JsonRoster.ROSTER_ENTRY, data, false); // what the schema allows...

        JsonNode result = service.postRosterEntry(locale, TEST_ENTRY1, data, 42); // ...the server applies
        validate(JsonRoster.ROSTER_ENTRY, result);
        JsonNode response = result.path(JSON.DATA);
        assertEquals("1234", response.path(JSON.ADDRESS).asText());
        assertEquals(75, response.path(JSON.MAX_SPD_PCT).asInt());
        assertEquals("Test comment", response.path(JSON.COMMENT).asText());
        assertEquals("Headlight", response.path(JSON.FUNCTION_KEYS).get(0).path(JSON.LABEL).asText());
        assertTrue(response.path(JSON.FUNCTION_KEYS).get(0).path(JSON.LOCKABLE).asBoolean());
        assertEquals("testValue", spy.getAttribute("testKey"));
        verify(spy).updateFile();
    }

    // updateFile() writes the entry's XML file, which does not exist for test entries
    private RosterEntry spyEntry() {
        RosterEntry spy = spy(Roster.getDefault().getEntryForId(TEST_ENTRY1));
        doNothing().when(spy).updateFile();
        Roster.getDefault().removeEntry(Roster.getDefault().getEntryForId(TEST_ENTRY1));
        Roster.getDefault().addEntry(spy);
        return spy;
    }

    @Test
    public void testPostRosterEntryNullsClear() throws JsonException {
        RosterEntry spy = spyEntry();
        spy.setOwner("Test Owner");
        spy.setComment("Test comment");
        spy.setShuntingFunction("F3");

        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, TEST_ENTRY1);
        data.putNull(JSON.OWNER).putNull(JSON.COMMENT).putNull(JSON.SHUNTING_FUNCTION);
        validateData(JsonRoster.ROSTER_ENTRY, data, false);

        JsonNode result = service.postRosterEntry(locale, TEST_ENTRY1, data, 42);
        validate(JsonRoster.ROSTER_ENTRY, result);
        assertEquals("", spy.getComment());
        assertEquals("", spy.getShuntingFunction());
        assertDoesNotThrow(spy::getOwner);
        assertDoesNotThrow(spy::store); // the roster file can still be written
    }

    @Test
    public void testPostRosterEntryPartialFunctionKey() throws JsonException {
        RosterEntry spy = spyEntry();
        spy.setFunctionLabel(2, "Bell");
        spy.setFunctionLockable(2, true);

        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, TEST_ENTRY1);
        data.putArray(JSON.FUNCTION_KEYS).addObject().put(JSON.NAME, "F2").put(JSON.LABEL, "Horn");
        service.postRosterEntry(locale, TEST_ENTRY1, data, 42);
        assertEquals("Horn", spy.getFunctionLabel(2));
        assertTrue(spy.getFunctionLockable(2), "lockable not sent, so unchanged");

        data.putArray(JSON.FUNCTION_KEYS).addObject().put(JSON.NAME, "F2").put(JSON.LOCKABLE, false);
        service.postRosterEntry(locale, TEST_ENTRY1, data, 42);
        assertEquals("Horn", spy.getFunctionLabel(2), "label not sent, so unchanged");
        assertFalse(spy.getFunctionLockable(2));

        spy.setFunctionLockable(2, true);
        data.putArray(JSON.FUNCTION_KEYS).addObject().put(JSON.NAME, "F2").putNull(JSON.LOCKABLE);
        service.postRosterEntry(locale, TEST_ENTRY1, data, 42);
        assertTrue(spy.getFunctionLockable(2), "null lockable, so unchanged");
        data.putArray(JSON.FUNCTION_KEYS).addObject().put(JSON.NAME, "F2").put(JSON.LOCKABLE, "yes");
        service.postRosterEntry(locale, TEST_ENTRY1, data, 42);
        assertTrue(spy.getFunctionLockable(2), "text lockable, so unchanged");
    }

    @Test
    public void testPostRosterEntryRejectedChangesNothing() {
        RosterEntry spy = spyEntry();
        spy.putAttribute("era", "1940s");
        spy.setFunctionLabel(1, "Bell");

        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, TEST_ENTRY1).put(JSON.OWNER, "New Owner");
        data.putArray(JsonRoster.ATTRIBUTES).addObject().put(JSON.NAME, "era").put(JSON.VALUE, "1950s");
        ArrayNode keys = data.putArray(JSON.FUNCTION_KEYS);
        keys.addObject().put(JSON.NAME, "F1").put(JSON.LABEL, "Horn");
        keys.addObject().put(JSON.NAME, "F40").put(JSON.LABEL, "Coupler");
        JsonException ex = assertThrows(JsonException.class,
                () -> service.postRosterEntry(locale, TEST_ENTRY1, data, 42));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        assertEquals("1940s", spy.getAttribute("era"));
        assertEquals("yes", spy.getAttribute(Roster.ROSTER_GROUP_PREFIX + TEST_GROUP1), "other attributes kept");
        assertEquals("Bell", spy.getFunctionLabel(1));
        assertEquals("", spy.getOwner());
        verify(spy, never()).updateFile();
    }

    @Test
    public void testPostRosterEntryFunctionKeysNotArray() throws JsonException {
        RosterEntry spy = spyEntry();
        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, TEST_ENTRY1);
        data.putObject(JSON.FUNCTION_KEYS).putObject("x").put(JSON.NAME, "bad");
        service.postRosterEntry(locale, TEST_ENTRY1, data, 42);
        verify(spy).updateFile();
    }

    @Test
    public void testPostRosterEntryBadFunctionKey() {
        RosterEntry spy = spyEntry();
        assertEquals(28, spy.getMaxFnNumAsInt());
        for (String key : new String[]{"F99999999999", "F-1", "F+3", "X5", "f5", "F", "", "F29"}) {
            ObjectNode data = mapper.createObjectNode().put(JSON.NAME, TEST_ENTRY1);
            data.putArray(JSON.FUNCTION_KEYS).addObject().put(JSON.NAME, key).put(JSON.LABEL, "x");
            JsonException ex = assertThrows(JsonException.class,
                    () -> service.postRosterEntry(locale, TEST_ENTRY1, data, 42), key);
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode(), key);
        }
        assertEquals(null, spy.getFunctionLabel(-1));
        assertEquals(null, spy.getFunctionLabel(3));
        assertEquals(null, spy.getFunctionLabel(5));
    }

    @Test
    public void testClientRosterEntryRejectsInvalid() {
        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, TEST_ENTRY1);
        validateData(JsonRoster.ROSTER_ENTRY, data, false); // baseline is valid
        assertInvalid(JsonRoster.ROSTER_ENTRY, mapper.createObjectNode().put(JSON.ADDRESS, "3"), false,
                "missing name");
        assertInvalid(JsonRoster.ROSTER_ENTRY, data.deepCopy().put(JSON.MAX_SPD_PCT, "fast"), false,
                "non-integer maxSpeedPct");
        assertInvalid(JsonRoster.ROSTER_ENTRY, data.deepCopy().put(JSON.ADDRESS, 3), false, "non-string address");
        assertInvalid(JsonRoster.ROSTER_ENTRY, data.deepCopy().put("unknown", "property"), false,
                "unknown property");
        ObjectNode badKey = data.deepCopy();
        badKey.putArray(JSON.FUNCTION_KEYS).addObject().put(JSON.NAME, "X0");
        assertInvalid(JsonRoster.ROSTER_ENTRY, badKey, false, "bad function key name");
        ObjectNode noAttributeName = data.deepCopy();
        noAttributeName.putArray(JsonRoster.ATTRIBUTES).addObject().put(JSON.VALUE, "orphan");
        assertInvalid(JsonRoster.ROSTER_ENTRY, noAttributeName, false, "attribute without name");
        ObjectNode noAttributeValue = data.deepCopy();
        noAttributeValue.putArray(JsonRoster.ATTRIBUTES).addObject().put(JSON.NAME, "orphan");
        assertInvalid(JsonRoster.ROSTER_ENTRY, noAttributeValue, false, "attribute without value");
        ObjectNode highKey = data.deepCopy();
        highKey.putArray(JSON.FUNCTION_KEYS).addObject().put(JSON.NAME, "F85");
        assertInvalid(JsonRoster.ROSTER_ENTRY, highKey, false, "function key above F84");
        assertInvalid(JsonRoster.ROSTER_ENTRY, data.deepCopy().put(JSON.MAX_SPD_PCT, 101), false,
                "maxSpeedPct above 100");
        assertInvalid(JsonRoster.ROSTER_ENTRY, data.deepCopy().put(JSON.MAX_SPD_PCT, -1), false,
                "negative maxSpeedPct");
        ObjectNode groups = data.deepCopy();
        groups.putArray(JsonRoster.ROSTER_GROUPS).add(TEST_GROUP1);
        assertInvalid(JsonRoster.ROSTER_ENTRY, groups, false, "rosterGroups not settable");
    }

    @Test
    public void testClientRosterGroup() {
        validateData(JsonRoster.ROSTER_GROUP, mapper.createObjectNode().put(JSON.NAME, TEST_GROUP1), false);
        assertInvalid(JsonRoster.ROSTER_GROUP, mapper.createObjectNode(), false, "missing name");
        assertInvalid(JsonRoster.ROSTER_GROUP, mapper.createObjectNode().put(JSON.NAME, TEST_GROUP1).put(JSON.LENGTH, 1),
                false, "unknown property");
    }

    /**
     * Test of getRosterGroups method, of class JsonRosterHttpService.
     * 
     * @throws JsonException if unexpected exception occurs
     */
    @Test
    public void testGetRosterGroups() throws JsonException {
        JsonNode result = service.getRosterGroups(new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        assertEquals(Roster.getDefault().getRosterGroups().size() + 1, result.size());
        validate(JsonRoster.ROSTER_GROUPS, result);
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
        assertEquals(Roster.getDefault().getEntriesInGroup(TEST_GROUP1).size(),
                result.path(JSON.DATA).path(JSON.LENGTH).asInt());
        // test non-existent group
        JsonException ex = assertThrows( JsonException.class, () ->
            service.getRosterGroup(locale, "non-existant-group", 42),
            "Expected exception not thrown");
        this.validate(ex.getJsonMessage());
        assertEquals( 404, ex.getCode(), "Error code is HTTP \"not found\"");
        assertEquals( "Object type rosterGroup named \"non-existant-group\" not found.",
            ex.getMessage(), "Error message is \"not found\"");
    }

}
