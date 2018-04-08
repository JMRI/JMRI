package jmri.server.json;

import java.util.Locale;
import jmri.NamedBean;
import jmri.server.json.turnout.JsonTurnoutHttpService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test handling of null, or non-existent Named Beans. Testing of existent, or
 * non-null Named Beans is covered elsewhere.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonNamedBeanHttpServiceTest extends JsonHttpServiceTestBase {

    public JsonNamedBeanHttpServiceTest() {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getNamedBean method, of class JsonNamedBeanHttpService.
     *
     * @throws java.lang.Exception on unexpected exceptions
     */
    @Test
    public void testGetNamedBean() throws Exception {
        NamedBean bean = null;
        String name = "non-existant";
        String type = "non-existant";
        Locale locale = Locale.ENGLISH;
        JsonNamedBeanHttpService instance = new JsonTurnoutHttpService(this.mapper);
        try {
            instance.getNamedBean(bean, name, type, locale);
            Assert.fail("Expected JsonException not thrown.");
        } catch (JsonException ex) {
            this.validate(ex.getJsonMessage());
            Assert.assertEquals("Error code is HTTP \"not found\"", 404, ex.getCode());
            Assert.assertEquals("Error message is HTTP \"not found\"", "Unable to access non-existant non-existant.", ex.getLocalizedMessage());
        }
    }

    /**
     * Test of postNamedBean method, of class JsonNamedBeanHttpService.
     *
     * @throws java.lang.Exception on unexpected exceptions
     */
    @Test
    public void testPostNamedBean() throws Exception {
        NamedBean bean = null;
        String name = "non-existant";
        String type = "non-existant";
        Locale locale = Locale.ENGLISH;
        JsonNamedBeanHttpService instance = new JsonTurnoutHttpService(this.mapper);
        try {
            instance.postNamedBean(bean, this.mapper.createObjectNode(), name, type, locale);
            Assert.fail("Expected JsonException not thrown.");
        } catch (JsonException ex) {
            this.validate(ex.getJsonMessage());
            Assert.assertEquals("Error code is HTTP \"not found\"", 404, ex.getCode());
            Assert.assertEquals("Error message is HTTP \"not found\"", "Unable to access non-existant non-existant.", ex.getLocalizedMessage());
        }
    }

}
