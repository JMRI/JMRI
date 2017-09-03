package jmri.jmrix.jmriclient.json.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JsonClientComponentFactoryTest {

    @Test
    public void testCTor() {
        jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo memo = new jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo();
        JsonClientComponentFactory t = new JsonClientComponentFactory(memo);
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

    // private final static Logger log = LoggerFactory.getLogger(JsonClientComponentFactoryTest.class);

}
