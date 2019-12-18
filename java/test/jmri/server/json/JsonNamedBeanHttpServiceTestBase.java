package jmri.server.json;

import jmri.NamedBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.NullNode;

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

    @After
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
        try {
            service.doGet(type, name, service.getObjectMapper().createObjectNode(), locale, 42);
            Assert.fail("Expected JsonException not thrown.");
        } catch (JsonException ex) {
            this.validate(ex.getJsonMessage());
            Assert.assertEquals("Error code is HTTP \"internal error\"", 500, ex.getCode());
            Assert.assertEquals("Error message is HTTP \"not found\"", "There was an error; see the JMRI application logs for details.", ex.getLocalizedMessage());
            Assert.assertEquals("Message Id", 42, ex.getId());
        }
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
        try {
            service.getNamedBean(bean, name, type, locale, 0);
            Assert.fail("Expected JsonException not thrown.");
        } catch (JsonException ex) {
            this.validate(ex.getJsonMessage());
            Assert.assertEquals("Error code is HTTP \"not found\"", 404, ex.getCode());
            Assert.assertEquals("Error message is HTTP \"not found\"", "Object type non-existant named \"non-existant\" not found.", ex.getLocalizedMessage());
            Assert.assertEquals("Message Id", 0, ex.getId());
        }
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
        try {
            service.postNamedBean(bean, this.mapper.createObjectNode(), name, type, locale, 42);
            Assert.fail("Expected JsonException not thrown.");
        } catch (JsonException ex) {
            this.validate(ex.getJsonMessage());
            Assert.assertEquals("Error code is HTTP \"not found\"", 404, ex.getCode());
            Assert.assertEquals("Error message is HTTP \"not found\"", "Object type non-existant named \"non-existant\" not found.", ex.getLocalizedMessage());
            Assert.assertEquals("Message Id", 42, ex.getId());
        }
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        try {
            assumeNotNull(service); // protect against JUnit tests in Eclipse that test this class directly
            service.doDelete(service.getType(), "non-existant", NullNode.getInstance(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals("Code is HTTP METHOD NOT ALLOWED", 405, ex.getCode());
            assertEquals("Message", "Deleting " + service.getType() + " is not allowed.", ex.getMessage());
            assertEquals("ID is 42", 42, ex.getId());
        }
    }
    
    @Test
    public void testDoSchema() throws JsonException {
        testDoSchema(service.getType());
    }
}
