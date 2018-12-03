package jmri.jmris.json;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.server.json.JsonMockConnection;
import java.io.DataOutputStream;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JsonProgrammerServerTest {

    @Test
    public void testCTor() {
        JsonProgrammerServer t = new JsonProgrammerServer(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
