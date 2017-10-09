package jmri.server.json.layoutblock;

import java.io.DataOutputStream;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JsonLayoutBlockSocketServiceTest {

    @Test
    public void testCTor() {
        JsonMockConnection mc = new JsonMockConnection((DataOutputStream) null);
        JsonLayoutBlockSocketService t = new JsonLayoutBlockSocketService(mc);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonLayoutBlockSocketServiceTest.class);

}
