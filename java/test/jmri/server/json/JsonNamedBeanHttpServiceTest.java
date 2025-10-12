package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.server.json.turnout.JsonTurnout;
import jmri.server.json.turnout.JsonTurnoutHttpService;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test handling of null, or non-existent Named Beans. Testing of existent, or
 * non-null Named Beans is covered elsewhere.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonNamedBeanHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Turnout, JsonNamedBeanHttpService<Turnout>> {


    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonTurnoutHttpService(mapper);
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of doGet method, of class JsonNamedBeanHttpService with
     * invalid NamedBean types. Uses a JsonTurnoutHttpService since the
     * JsonNamedBeanHttpService is abstract. This only tests the error
     * cases since subclasses test successful cases.
     *
     * @throws java.lang.Exception on unexpected exceptions
     */
    @Test
    @Override
    public void testDoGet() throws Exception {
        String name = "non-existant";
        String type = "non-existant";

        JsonException ex = assertThrows( JsonException.class,
            () -> service.doGet(type, name, service.getObjectMapper().createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)));

        this.validate(ex.getJsonMessage());
        assertEquals( 500, ex.getCode(), "Error code is HTTP \"internal error\"");
        assertEquals( "There was an error; see the JMRI application logs for details.", ex.getLocalizedMessage(),
            "Error message is HTTP \"not found\"");
        assertEquals( 42, ex.getId(), "Message Id");
    }

    /**
     * Test of getNamedBean method, of class JsonNamedBeanHttpService with
     * invalid NamedBean types. Uses a JsonTurnoutHttpService since the
     * JsonNamedBeanHttpService is abstract.
     *
     * @throws java.lang.Exception on unexpected exceptions
     */
    @Test
    @Override
    public void testGetNamedBean() throws Exception {
        String name = "non-existant";
        String type = "non-existant";
        JsonException ex = assertThrows( JsonException.class,
            () -> service.getNamedBean(bean, name, type, new JsonRequest(locale, JSON.V5, JSON.GET, 0)));

        this.validate(ex.getJsonMessage());
        assertEquals( 404, ex.getCode(), "Error code is HTTP \"not found\"");
        assertEquals( "Object type non-existant named \"non-existant\" not found.", ex.getLocalizedMessage(),
            "Error message is HTTP \"not found\"");
        assertEquals( 0, ex.getId(), "Message Id");
    }

    /**
     * Test of getNamedBean method, of class JsonNamedBeanHttpService with a
     * turnout with some property values. Uses a JsonTurnoutHttpService since
     * the JsonNamedBeanHttpService is abstract.
     *
     * @throws java.lang.Exception on unexpected exceptions
     */
    @Test
    public void testGetNamedBeanWithProperties() throws Exception {
        String name = "IT1";
        // retain turnout as NamedBean to ensure only "generic" NamedBean
        // methods are used
        bean = InstanceManager.getDefault(TurnoutManager.class).provide(name);
        bean.setUserName("Turnout 1");
        bean.setComment("Turnout Comment");
        bean.setProperty("foo", "bar");
        bean.setProperty("bar", null);
        JsonNode root = service.getNamedBean(bean, name, JsonTurnout.TURNOUT, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode data = root.path(JSON.DATA);
        assertEquals( bean.getSystemName(), data.path(JSON.NAME).asText(), "Correct system name");
        assertEquals( bean.getUserName(), data.path(JSON.USERNAME).asText(), "Correct user name");
        assertEquals( bean.getComment(), data.path(JSON.COMMENT).asText(), "Correct comment");
        assertTrue( data.path(JSON.PROPERTIES).isArray(), "Has properties");
        assertEquals( 2, data.path(JSON.PROPERTIES).size(), "Has 2 properties");
        assertEquals( 42, root.path(JSON.ID).asInt(), "Message ID");
        data.path(JSON.PROPERTIES).fields().forEachRemaining((property) ->{
            switch (property.getKey()) {
                case "foo":
                    assertEquals( "bar", property.getValue().asText(), "Foo value");
                    break;
                case "bar":
                    assertNull( property.getValue(), "Bar is null");
                    break;
                default:
                    fail("Unexpected property present.");
            }
        });
    }

    /**
     * Test of postNamedBean method, of class JsonNamedBeanHttpService with
     * invalid NamedBean types. Uses a JsonTurnoutHttpService since the
     * JsonNamedBeanHttpService is abstract.
     *
     * @throws java.lang.Exception on unexpected exceptions
     */
    @Test
    @Override
    public void testPostNamedBean() throws Exception {
        String name = "non-existant";
        String type = "non-existant";
        JsonException ex = assertThrows( JsonException.class,
            () -> service.postNamedBean(bean, this.mapper.createObjectNode(), name, type,
                new JsonRequest(locale, JSON.V5, JSON.POST, 42)));
        this.validate(ex.getJsonMessage());
        assertEquals( 404, ex.getCode(), "Error code is HTTP \"not found\"");
        assertEquals( "Object type non-existant named \"non-existant\" not found.",
            ex.getLocalizedMessage(), "Error message is HTTP \"not found\"");
        assertEquals( 42, ex.getId(), "Message Id");
    }
    
    @Test
    @Override
    public void testDoDelete() {
        JsonException ex = assertThrows( JsonException.class,
            () -> service.doDelete(service.getType(), "non-existant", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42)));
        assertEquals( 404, ex.getCode(), "Code is HTTP NOT FOUND");
        assertEquals( "Object type turnout named \"non-existant\" not found.",
            ex.getLocalizedMessage(), "Error message is HTTP \"not found\"");
        assertEquals( 42, ex.getId(), "ID is 42");
    }
}
