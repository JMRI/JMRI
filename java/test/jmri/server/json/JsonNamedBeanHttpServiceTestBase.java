package jmri.server.json;

import jmri.NamedBean;

import org.junit.jupiter.api.*;

import com.fasterxml.jackson.databind.node.NullNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test handling of null, or non-existent Named Beans. Testing of existent, or
 * non-null Named Beans is covered elsewhere.
 *
 * @author Randall Wood Copyright 2018
 * @param <B> type of NamedBean supported in test
 * @param <S> type of JsonNamedBeanHttpService supported in test
 */
public abstract class JsonNamedBeanHttpServiceTestBase<B extends NamedBean, S extends JsonNamedBeanHttpService<B>> extends JsonHttpServiceTestBase<S> {

    protected B bean = null;

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        bean = null;
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
     * invalid NamedBean types.
     *
     * @throws java.lang.Exception on unexpected exceptions
     */
    @Test
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
     * Test of postNamedBean method, of class JsonNamedBeanHttpService with
     * invalid NamedBean types. Uses a JsonTurnoutHttpService since the
     * JsonNamedBeanHttpService is abstract.
     *
     * @throws java.lang.Exception on unexpected exceptions
     */
    @Test
    public void testPostNamedBean() throws Exception {
        String name = "non-existant";
        String type = "non-existant";
        JsonException ex = assertThrows( JsonException.class,
            () -> service.postNamedBean(bean, this.mapper.createObjectNode(),
                name, type, new JsonRequest(locale, JSON.V5, JSON.POST, 42)));
        this.validate(ex.getJsonMessage());
        assertEquals( 404, ex.getCode(), "Error code is HTTP \"not found\"");
        assertEquals( "Object type non-existant named \"non-existant\" not found.",
            ex.getLocalizedMessage(), "Error message is HTTP \"not found\"");
        assertEquals( 42, ex.getId(), "Message Id");
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        assumeTrue( service != null, "protect against JUnit tests in Eclipse that test this class directly");
        JsonException ex = assertThrows( JsonException.class,
            () -> service.doDelete(service.getType(), "non-existant", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals( 405, ex.getCode(), "Code is HTTP METHOD NOT ALLOWED");
        assertEquals( "Deleting " + service.getType() + " is not allowed.", ex.getMessage(), "Message");
        assertEquals( 42, ex.getId(), "ID is 42");
    }
    
    @Test
    public void testDoSchema() throws JsonException {
        testDoSchema(service.getType());
    }
}
