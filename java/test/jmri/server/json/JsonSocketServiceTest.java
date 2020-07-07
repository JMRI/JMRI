package jmri.server.json;

import java.io.DataOutputStream;
import java.util.Locale;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
    public void testLocale() {
        JsonConnection connection = new JsonConnection((DataOutputStream) null);
        JsonSocketService<?> instance = new JsonTestSocketService(connection);
        Assert.assertEquals("Default locale", Locale.getDefault(), instance.getLocale());
        connection.setLocale(Locale.ITALY);
        Assert.assertEquals("Italian locale", Locale.ITALY, instance.getLocale());
        connection.setLocale(Locale.ENGLISH);
        Assert.assertEquals("English locale", Locale.ENGLISH, instance.getLocale());
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonSocketServiceTest.class);

}
