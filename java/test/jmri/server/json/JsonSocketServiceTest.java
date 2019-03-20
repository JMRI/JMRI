package jmri.server.json;

import java.io.DataOutputStream;
import java.util.Locale;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSocketServiceTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testLocale() {
        JsonConnection connection = new JsonConnection((DataOutputStream) null);
        JsonSocketService<?> instance = new JsonTestSocketService(connection);
        Assert.assertEquals("Default locale", Locale.getDefault(), instance.getLocale());
        instance.setLocale(Locale.ITALY);
        Assert.assertEquals("Default locale", Locale.ITALY, instance.getLocale());
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonSocketServiceTest.class);

}
