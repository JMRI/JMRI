package jmri.server.json.logs;

import java.io.DataOutputStream;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonLogsSocketServiceTest {

    @Test
    public void testCtorSuccess() {
        JsonLogsSocketService service = new JsonLogsSocketService(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull(service);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
