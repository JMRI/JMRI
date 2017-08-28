package jmri.jmris.json;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JsonTimeServerTest extends jmri.jmris.AbstractTimeServerTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            // null output string drops characters
            // could be replaced by one that checks for specific outputs
            @Override
            public void write(int b) throws java.io.IOException {
            }
        });
        JsonConnection jc = new JsonConnection(output);
        a = new JsonTimeServer(jc);
    }

    @After
    public void tearDown() {
        a = null;
        JUnitUtil.setUp();
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonTimeServerTest.class.getName());
}
