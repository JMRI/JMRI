package jmri.server.json.schema;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaHttpServiceTest {

    public JsonSchemaHttpServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of doGet method, of class JsonSchemaHttpService.
     */
    @Test
    public void testDoGet() throws Exception {
        System.out.println("doGet");
        String type = "";
        String name = "";
        Locale locale = null;
        JsonSchemaHttpService instance = null;
        JsonNode expResult = null;
        JsonNode result = instance.doGet(type, name, locale);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of doPost method, of class JsonSchemaHttpService.
     */
    @Test
    public void testDoPost() throws Exception {
        System.out.println("doPost");
        String type = "";
        String name = "";
        JsonNode data = null;
        Locale locale = null;
        JsonSchemaHttpService instance = null;
        JsonNode expResult = null;
        JsonNode result = instance.doPost(type, name, data, locale);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of doGetList method, of class JsonSchemaHttpService.
     */
    @Test
    public void testDoGetList() throws Exception {
        System.out.println("doGetList");
        String type = "";
        Locale locale = null;
        JsonSchemaHttpService instance = null;
        ArrayNode expResult = null;
        ArrayNode result = instance.doGetList(type, locale);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of doSchema method, of class JsonSchemaHttpService.
     */
    @Test
    public void testDoSchema() throws Exception {
        System.out.println("doSchema");
        String type = "";
        boolean server = false;
        Locale locale = null;
        JsonSchemaHttpService instance = null;
        JsonNode expResult = null;
        JsonNode result = instance.doSchema(type, server, locale);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
