package jmri.jmrix.acela;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AcelaSensorTest {

    @Test
    public void testCTor() {
        AcelaSensor t = new AcelaSensor("AS1");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void test2StringCTor() {
        AcelaSensor t = new AcelaSensor("AS1","test");
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

    // private final static Logger log = LoggerFactory.getLogger(AcelaSensorTest.class);

}
