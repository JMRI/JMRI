package jmri.server.json;

import java.io.DataOutputStream;
import java.util.Locale;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSocketServiceTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testLocale() throws java.io.IOException {
        JsonConnection connection = new JsonConnection((DataOutputStream) null);
        JsonSocketService<?> instance = new JsonTestSocketService(connection);
        assertEquals( Locale.getDefault(), instance.getLocale(), "Default locale");
        connection.setLocale(Locale.ITALY);
        assertEquals( Locale.ITALY, instance.getLocale(), "Italian locale");
        connection.setLocale(Locale.ENGLISH);
        assertEquals( Locale.ENGLISH, instance.getLocale(), "English locale");

        connection.close();
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonSocketServiceTest.class);

}
